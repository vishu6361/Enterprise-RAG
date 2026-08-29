import logging
from fastapi import APIRouter, UploadFile, File, Form, HTTPException, status
from app.models.schemas import IngestResponse, IngestTextRequest
from app.services.parser_service import parser_service
from app.services.chunker_service import chunker_service
from app.services.embedding_service import embedding_service
from app.services.vector_store import vector_store

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/ingest", tags=["Ingestion"])


@router.post("/file", response_model=IngestResponse, status_code=status.HTTP_200_OK)
async def ingest_file(
    document_id: int = Form(..., description="Document entity ID"),
    organization_id: int = Form(..., description="Tenant organization ID"),
    file: UploadFile = File(..., description="Raw document file (.pdf, .docx, .txt, .md, .csv, .json)"),
):
    """Processes uploaded document bytes: parsing -> semantic chunking -> embedding -> multi-tenant vector indexing."""
    filename = file.filename or f"doc_{document_id}"
    logger.info(f"Received file for ingestion: {filename} (Doc ID: {document_id}, Org ID: {organization_id})")

    try:
        file_bytes = await file.read()
        if not file_bytes:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Uploaded file is empty.")

        # 1. Parse text from file
        text_content = parser_service.extract_text(file_bytes, filename, file.content_type)
        if not text_content or not text_content.strip():
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="No readable text extracted from file.")

        # 2. Chunk text
        chunks = chunker_service.chunk_text(
            text=text_content,
            document_id=document_id,
            organization_id=organization_id,
            filename=filename,
        )

        if not chunks:
            raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="Failed to produce text chunks.")

        # 3. Generate embeddings
        chunk_texts = [c.text for c in chunks]
        embeddings = embedding_service.embed_texts(chunk_texts)

        for i, chunk in enumerate(chunks):
            chunk.embedding = embeddings[i]

        # 4. Store in vector database
        # Clear any prior chunks for this doc first to avoid duplicate chunks on re-process
        vector_store.delete_document_chunks(organization_id=organization_id, document_id=document_id)
        vector_store.store_chunks(chunks)

        return IngestResponse(
            document_id=document_id,
            organization_id=organization_id,
            filename=filename,
            total_chunks=len(chunks),
            status="COMPLETED",
            message=f"Document '{filename}' successfully parsed into {len(chunks)} vector indexed chunks.",
        )

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Failed to ingest document {filename}: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Ingestion processing error: {str(e)}",
        )


@router.post("/text", response_model=IngestResponse, status_code=status.HTTP_200_OK)
async def ingest_raw_text(payload: IngestTextRequest):
    """Processes raw text directly: semantic chunking -> embedding -> multi-tenant vector indexing."""
    logger.info(f"Ingesting raw text for Doc ID: {payload.document_id}, Org ID: {payload.organization_id}")

    try:
        chunks = chunker_service.chunk_text(
            text=payload.text_content,
            document_id=payload.document_id,
            organization_id=payload.organization_id,
            filename=payload.filename,
            chunk_size=payload.chunk_size,
            chunk_overlap=payload.chunk_overlap,
        )

        if not chunks:
            raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Text content is empty.")

        chunk_texts = [c.text for c in chunks]
        embeddings = embedding_service.embed_texts(chunk_texts)

        for i, chunk in enumerate(chunks):
            chunk.embedding = embeddings[i]

        vector_store.delete_document_chunks(organization_id=payload.organization_id, document_id=payload.document_id)
        vector_store.store_chunks(chunks)

        return IngestResponse(
            document_id=payload.document_id,
            organization_id=payload.organization_id,
            filename=payload.filename,
            total_chunks=len(chunks),
            status="COMPLETED",
            message=f"Text for '{payload.filename}' indexed with {len(chunks)} chunks.",
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error ingesting text payload: {e}", exc_info=True)
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=str(e))


@router.delete("/{organization_id}/{document_id}", status_code=status.HTTP_200_OK)
async def delete_document_vectors(organization_id: int, document_id: int):
    """Purges indexed vector embeddings for a deleted document."""
    deleted_count = vector_store.delete_document_chunks(organization_id=organization_id, document_id=document_id)
    return {"status": "SUCCESS", "deleted_chunks": deleted_count, "document_id": document_id, "organization_id": organization_id}
