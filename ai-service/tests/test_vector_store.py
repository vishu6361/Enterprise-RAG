import pytest
from app.services.vector_store import MultiTenantVectorStore
from app.models.schemas import ChunkData, ChunkMetadata


def test_tenant_isolation():
    store = MultiTenantVectorStore()

    # Org 101 Chunk
    chunk_org_101 = ChunkData(
        chunk_id="doc_1_chunk_0",
        text="Financial quarterly report for Organization 101.",
        metadata=ChunkMetadata(
            document_id=1,
            organization_id=101,
            chunk_index=0,
            source_filename="q1.txt",
        ),
        embedding=[1.0, 0.0, 0.0, 0.0],
    )

    # Org 202 Chunk (identical content/vector but different organization)
    chunk_org_202 = ChunkData(
        chunk_id="doc_2_chunk_0",
        text="Confidential trade secrets for Organization 202.",
        metadata=ChunkMetadata(
            document_id=2,
            organization_id=202,
            chunk_index=0,
            source_filename="secrets.txt",
        ),
        embedding=[1.0, 0.0, 0.0, 0.0],
    )

    store.store_chunks([chunk_org_101, chunk_org_202])

    # Query for Org 101
    results_101 = store.search(
        query_embedding=[1.0, 0.0, 0.0, 0.0],
        organization_id=101,
        top_k=5,
    )

    assert len(results_101) == 1
    assert results_101[0].document_id == 1
    assert results_101[0].organization_id == 101
    assert "Organization 101" in results_101[0].text

    # Query for Org 202
    results_202 = store.search(
        query_embedding=[1.0, 0.0, 0.0, 0.0],
        organization_id=202,
        top_k=5,
    )

    assert len(results_202) == 1
    assert results_202[0].document_id == 2
    assert results_202[0].organization_id == 202
    assert "Organization 202" in results_202[0].text

    # Query for unknown Org 999 -> returns empty
    results_999 = store.search(
        query_embedding=[1.0, 0.0, 0.0, 0.0],
        organization_id=999,
        top_k=5,
    )
    assert len(results_999) == 0


def test_delete_document_chunks():
    store = MultiTenantVectorStore()

    chunk1 = ChunkData(
        chunk_id="doc_10_chunk_0",
        text="Chunk 0",
        metadata=ChunkMetadata(document_id=10, organization_id=1, chunk_index=0, source_filename="f.txt"),
        embedding=[0.5, 0.5],
    )
    chunk2 = ChunkData(
        chunk_id="doc_10_chunk_1",
        text="Chunk 1",
        metadata=ChunkMetadata(document_id=10, organization_id=1, chunk_index=1, source_filename="f.txt"),
        embedding=[0.5, 0.5],
    )
    store.store_chunks([chunk1, chunk2])

    deleted = store.delete_document_chunks(organization_id=1, document_id=10)
    assert deleted == 2

    # Query should now return 0 results
    results = store.search(query_embedding=[0.5, 0.5], organization_id=1)
    assert len(results) == 0
