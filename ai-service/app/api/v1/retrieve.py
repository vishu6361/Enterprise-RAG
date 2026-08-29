import logging
from fastapi import APIRouter, HTTPException, status
from app.models.schemas import RetrieveRequest, RetrieveResponse
from app.services.embedding_service import embedding_service
from app.services.vector_store import vector_store

logger = logging.getLogger(__name__)
router = APIRouter(prefix="/retrieve", tags=["Retrieval"])


@router.post("", response_model=RetrieveResponse, status_code=status.HTTP_200_OK)
async def retrieve_relevant_context(request: RetrieveRequest):
    """Retrieves top-k relevant document chunks strictly scoped within the caller's organization boundary."""
    logger.info(
        f"Retrieval query in Org {request.organization_id}: '{request.query}' "
        f"(top_k={request.top_k}, doc_filter={request.document_ids})"
    )

    try:
        # 1. Embed query text
        query_embedding = embedding_service.embed_query(request.query)

        # 2. Query multi-tenant vector index with strict organization scoping
        results = vector_store.search(
            query_embedding=query_embedding,
            organization_id=request.organization_id,
            document_ids=request.document_ids,
            top_k=request.top_k,
            min_score=request.min_score,
        )

        return RetrieveResponse(
            query=request.query,
            organization_id=request.organization_id,
            total_retrieved=len(results),
            results=results,
        )
    except Exception as e:
        logger.error(f"Failed to execute vector retrieval: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Vector retrieval failed: {str(e)}",
        )
