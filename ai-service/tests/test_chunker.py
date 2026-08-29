import pytest
from app.services.chunker_service import chunker_service


def test_chunk_short_text():
    text = "Short sentence that fits inside a single chunk."
    chunks = chunker_service.chunk_text(
        text=text,
        document_id=1,
        organization_id=10,
        filename="short.txt",
        chunk_size=500,
        chunk_overlap=50,
    )
    assert len(chunks) == 1
    assert chunks[0].chunk_id == "doc_1_chunk_0"
    assert chunks[0].metadata.document_id == 1
    assert chunks[0].metadata.organization_id == 10
    assert chunks[0].text == text


def test_chunk_multi_paragraph_with_overlap():
    paragraphs = [
        "Paragraph one with some important business knowledge and information.",
        "Paragraph two continues the discussion about architectural multi-tenancy and data isolation.",
        "Paragraph three covers retrieval augmented generation with dense vector embeddings.",
        "Paragraph four discusses indexing performance and scalability across enterprise tenants.",
    ]
    full_text = "\n\n".join(paragraphs)

    chunks = chunker_service.chunk_text(
        text=full_text,
        document_id=42,
        organization_id=100,
        filename="architecture.md",
        chunk_size=150,
        chunk_overlap=30,
    )

    assert len(chunks) > 1
    for i, c in enumerate(chunks):
        assert c.chunk_id == f"doc_42_chunk_{i}"
        assert c.metadata.organization_id == 100
        assert len(c.text) > 0


def test_empty_text_returns_no_chunks():
    chunks = chunker_service.chunk_text(
        text="   \n\n   ",
        document_id=1,
        organization_id=1,
        filename="empty.txt",
    )
    assert chunks == []
