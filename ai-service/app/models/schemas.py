from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any


class ChunkMetadata(BaseModel):
    document_id: int = Field(..., description="Document entity ID from enterprise backend")
    organization_id: int = Field(..., description="Tenant organization ID")
    chunk_index: int = Field(..., description="Sequential index of the chunk within the document")
    source_filename: str = Field(..., description="Original filename of the document")
    char_start: int = Field(default=0, description="Starting character offset in full text")
    char_end: int = Field(default=0, description="Ending character offset in full text")


class ChunkData(BaseModel):
    chunk_id: str = Field(..., description="Unique composite ID: doc_{doc_id}_chunk_{index}")
    text: str = Field(..., description="Extracted chunk text content")
    metadata: ChunkMetadata
    embedding: Optional[List[float]] = None


class IngestTextRequest(BaseModel):
    document_id: int
    organization_id: int
    filename: str
    text_content: str
    chunk_size: Optional[int] = None
    chunk_overlap: Optional[int] = None


class IngestResponse(BaseModel):
    document_id: int
    organization_id: int
    filename: str
    total_chunks: int
    status: str
    message: str


class RetrieveRequest(BaseModel):
    query: str = Field(..., min_length=1, description="Natural language search query")
    organization_id: int = Field(..., description="Tenant organization ID for mandatory isolation")
    document_ids: Optional[List[int]] = Field(default=None, description="Optional filter to specific document IDs")
    top_k: int = Field(default=4, ge=1, le=50, description="Maximum number of relevant chunks to return")
    min_score: float = Field(default=0.0, ge=0.0, le=1.0, description="Minimum cosine similarity threshold")


class RetrievedChunk(BaseModel):
    chunk_id: str
    document_id: int
    organization_id: int
    chunk_index: int
    source_filename: str
    text: str
    score: float = Field(..., description="Cosine similarity score (0.0 to 1.0)")


class RetrieveResponse(BaseModel):
    query: str
    organization_id: int
    total_retrieved: int
    results: List[RetrievedChunk]


class HealthResponse(BaseModel):
    status: str
    app_name: str
    embedding_model: str
