import json
import logging
from datetime import datetime, timezone
from typing import Optional
from aiokafka import AIOKafkaProducer
from app.config import settings

logger = logging.getLogger(__name__)


class KafkaEventProducer:
    """Asynchronous Kafka producer for publishing document status update events."""

    def __init__(self):
        self._producer: Optional[AIOKafkaProducer] = None

    async def start(self):
        if not settings.KAFKA_ENABLED:
            logger.info("Kafka is disabled in configuration.")
            return

        try:
            self._producer = AIOKafkaProducer(
                bootstrap_servers=settings.KAFKA_BOOTSTRAP_SERVERS,
                value_serializer=lambda v: json.dumps(v).encode("utf-8"),
                key_serializer=lambda k: k.encode("utf-8") if k else None,
            )
            await self._producer.start()
            logger.info(f"Kafka Producer connected to {settings.KAFKA_BOOTSTRAP_SERVERS}")
        except Exception as e:
            logger.error(f"Failed to start Kafka Producer: {e}")
            self._producer = None

    async def stop(self):
        if self._producer:
            await self._producer.stop()
            logger.info("Kafka Producer stopped.")

    async def send_status_event(
        self,
        document_id: int,
        organization_id: int,
        status: str,  # "COMPLETED" or "FAILED"
        total_chunks: int = 0,
        error_message: Optional[str] = None,
    ):
        if not self._producer:
            logger.warning("Kafka producer not active. Skipping status event publish.")
            return

        event_payload = {
            "documentId": document_id,
            "organizationId": organization_id,
            "status": status,
            "totalChunks": total_chunks,
            "errorMessage": error_message,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        }

        try:
            partition_key = str(organization_id)
            await self._producer.send_and_wait(
                topic=settings.KAFKA_STATUS_TOPIC,
                key=partition_key,
                value=event_payload,
            )
            logger.info(
                f"Published DocumentStatusEvent to '{settings.KAFKA_STATUS_TOPIC}': "
                f"docId={document_id}, status={status}, chunks={total_chunks}"
            )
        except Exception as e:
            logger.error(f"Failed to publish status event for docId {document_id}: {e}", exc_info=True)


kafka_producer = KafkaEventProducer()
