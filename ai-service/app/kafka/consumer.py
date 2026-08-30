import asyncio
import json
import logging
import os
from typing import Optional
from aiokafka import AIOKafkaConsumer
from app.config import settings
from app.services.parser_service import parser_service
from app.services.chunker_service import chunker_service
from app.services.embedding_service import embedding_service
from app.services.vector_store import vector_store
from app.kafka.producer import kafka_producer

logger = logging.getLogger(__name__)


class KafkaConsumerWorker:
    """Asynchronous Kafka background consumer worker processing document ingestion events."""

    def __init__(self):
        self._consumer: Optional[AIOKafkaConsumer] = None
        self._task: Optional[asyncio.Task] = None
        self._running: bool = False

    async def start(self):
        if not settings.KAFKA_ENABLED:
            logger.info("Kafka consumer disabled in settings.")
            return

        try:
            self._consumer = AIOKafkaConsumer(
                settings.KAFKA_INGESTION_TOPIC,
                bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
                group_id=settings.KAFKA_CONSUMER_GROUP,
                auto_offset_reset="earliest",
                enable_auto_commit=True,
                value_deserializer=lambda m: json.loads(m.decode("utf-8")),
            )
            await self._consumer.start()
            self._running = True
            self._task = asyncio.create_task(self._consume_loop())
            logger.info(
                f"Kafka Consumer listening on '{settings.KAFKA_INGESTION_TOPIC}' "
                f"(group: {settings.KAFKA_CONSUMER_GROUP})"
            )
        except Exception as e:
            logger.error(f"Failed to start Kafka Consumer: {e}")
            self._consumer = None

    async def stop(self):
        self._running = False
        if self._task:
            self._task.cancel()
            try:
                await self._task
            except asyncio.CancelledError:
                pass
        if self._consumer:
            await self._consumer.stop()
            logger.info("Kafka Consumer stopped.")

    async def _consume_loop(self):
        while self._running:
            try:
                async for msg in self._consumer:
                    if not self._running:
                        break
                    await self._process_ingestion_message(msg.value)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"Error in Kafka consumer loop: {e}", exc_info=True)
                await asyncio.sleep(2)

    async def _process_ingestion_message(self, event_data: dict):
        document_id = event_data.get("documentId")
        organization_id = event_data.get("organizationId")
        filename = event_data.get("filename", f"doc_{document_id}")
        file_path = event_data.get("filePath")
        content_type = event_data.get("contentType")

        logger.info(f"Processing DocumentIngestEvent: docId={document_id}, orgId={organization_id}, file={filename}")

        if not document_id or not organization_id or not file_path:
            logger.error(f"Invalid DocumentIngestEvent payload: {event_data}")
            return

        try:
            # 1. Resolve and read the physical file from disk
            resolved_path = self._resolve_file_path(file_path)
            if not os.path.exists(resolved_path):
                raise FileNotFoundError(f"File not found on disk at: {resolved_path}")

            with open(resolved_path, "rb") as f:
                file_bytes = f.read()

            if not file_bytes:
                raise ValueError("File content is empty.")

            # 2. Parse text content
            text_content = parser_service.extract_text(file_bytes, filename, content_type)
            if not text_content or not text_content.strip():
                raise ValueError("No readable text could be extracted from document.")

            # 3. Chunk text
            chunks = chunker_service.chunk_text(
                text=text_content,
                document_id=document_id,
                organization_id=organization_id,
                filename=filename,
            )

            if not chunks:
                raise ValueError("Chunker produced 0 text chunks.")

            # 4. Generate dense vector embeddings
            chunk_texts = [c.text for c in chunks]
            embeddings = embedding_service.embed_texts(chunk_texts)
            for i, chunk in enumerate(chunks):
                chunk.embedding = embeddings[i]

            # 5. Store in multi-tenant vector store (clearing prior vectors first for idempotency)
            vector_store.delete_document_chunks(organization_id=organization_id, document_id=document_id)
            vector_store.store_chunks(chunks)

            logger.info(f"Successfully vectorized and indexed docId={document_id} ({len(chunks)} chunks).")

            # 6. Publish COMPLETED status event to Kafka
            await kafka_producer.send_status_event(
                document_id=document_id,
                organization_id=organization_id,
                status="COMPLETED",
                total_chunks=len(chunks),
                error_message=None,
            )

        except Exception as e:
            logger.error(f"Failed to process ingestion event for docId {document_id}: {e}", exc_info=True)
            # Publish FAILED status event to Kafka
            await kafka_producer.send_status_event(
                document_id=document_id,
                organization_id=organization_id,
                status="FAILED",
                total_chunks=0,
                error_message=str(e),
            )

    def _resolve_file_path(self, file_path: str) -> str:
        """Resolves absolute or relative file path across backend/AI workspaces."""
        if os.path.isabs(file_path) and os.path.exists(file_path):
            return file_path

        # Check relative to backend workspace
        candidates = [
            file_path,
            os.path.join("..", "backend", "enterprise-rag", file_path),
            os.path.join("..", file_path),
            os.path.join(os.getcwd(), file_path),
        ]
        for c in candidates:
            if os.path.exists(c):
                return os.path.abspath(c)

        return file_path


kafka_consumer = KafkaConsumerWorker()
