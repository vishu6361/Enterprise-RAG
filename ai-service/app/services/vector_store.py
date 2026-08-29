import logging
from typing import List, Optional, Dict
import numpy as np
from app.models.schemas import ChunkData, RetrievedChunk

logger = logging.getLogger(__name__)


class MultiTenantVectorStore:
    """In-memory multi-tenant vector store with strict organization scoping and cosine similarity search."""

    def __init__(self):
        # Key: organization_id -> Dict[chunk_id, ChunkData]
        self._tenants_store: Dict[int, Dict[str, ChunkData]] = {}

    def store_chunks(self, chunks: List[ChunkData]) -> int:
        """Stores or updates chunks categorized under their respective organization_id."""
        stored_count = 0
        for chunk in chunks:
            org_id = chunk.metadata.organization_id
            if org_id not in self._tenants_store:
                self._tenants_store[org_id] = {}

            self._tenants_store[org_id][chunk.chunk_id] = chunk
            stored_count += 1

        logger.info(f"Indexed {stored_count} chunks across multi-tenant vector store.")
        return stored_count

    def delete_document_chunks(self, organization_id: int, document_id: int) -> int:
        """Deletes all chunks belonging to a specific document inside an organization boundary."""
        if organization_id not in self._tenants_store:
            return 0

        org_chunks = self._tenants_store[organization_id]
        to_delete = [
            chunk_id
            for chunk_id, chunk in org_chunks.items()
            if chunk.metadata.document_id == document_id
        ]

        for cid in to_delete:
            del org_chunks[cid]

        logger.info(f"Deleted {len(to_delete)} chunks for document {document_id} in org {organization_id}")
        return len(to_delete)

    def search(
        self,
        query_embedding: List[float],
        organization_id: int,
        document_ids: Optional[List[int]] = None,
        top_k: int = 4,
        min_score: float = 0.0,
    ) -> List[RetrievedChunk]:
        """Performs cosine similarity search strictly scoped to the tenant's organization_id."""
        if organization_id not in self._tenants_store or not self._tenants_store[organization_id]:
            logger.info(f"No indexed chunks found for organization {organization_id}")
            return []

        org_chunks = self._tenants_store[organization_id]
        q_vec = np.array(query_embedding, dtype=np.float32)
        
        # Ensure query vector is normalized
        q_norm = np.linalg.norm(q_vec)
        if q_norm > 0:
            q_vec = q_vec / q_norm

        scored_results: List[RetrievedChunk] = []

        for chunk_id, chunk in org_chunks.items():
            # Apply document_ids filter if specified
            if document_ids is not None and chunk.metadata.document_id not in document_ids:
                continue

            if chunk.embedding is None:
                continue

            c_vec = np.array(chunk.embedding, dtype=np.float32)
            # Dot product on unit vectors = cosine similarity
            score = float(np.dot(q_vec, c_vec))

            # Clamp between 0.0 and 1.0
            score = max(0.0, min(1.0, score))

            if score >= min_score:
                scored_results.append(
                    RetrievedChunk(
                        chunk_id=chunk.chunk_id,
                        document_id=chunk.metadata.document_id,
                        organization_id=chunk.metadata.organization_id,
                        chunk_index=chunk.metadata.chunk_index,
                        source_filename=chunk.metadata.source_filename,
                        text=chunk.text,
                        score=round(score, 4),
                    )
                )

        # Sort by score descending
        scored_results.sort(key=lambda x: x.score, reverse=True)
        return scored_results[:top_k]


vector_store = MultiTenantVectorStore()
