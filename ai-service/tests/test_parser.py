import pytest
from app.services.parser_service import parser_service


def test_parse_plain_text():
    sample_text = b"Hello World!\n\nThis is a sample document for Enterprise RAG.\nTesting clean parsing."
    result = parser_service.extract_text(sample_text, "sample.txt")
    assert "Hello World!" in result
    assert "Enterprise RAG" in result


def test_parse_markdown():
    sample_md = b"# Title\n\n## Section 1\nSome **bold** and *italic* content.\n\n- Bullet 1\n- Bullet 2"
    result = parser_service.extract_text(sample_md, "notes.md")
    assert "# Title" in result
    assert "Section 1" in result
    assert "Bullet 1" in result


def test_parse_json():
    sample_json = b'{"name": "Enterprise RAG", "version": "1.0", "active": true}'
    result = parser_service.extract_text(sample_json, "config.json")
    assert '"name": "Enterprise RAG"' in result
    assert '"version": "1.0"' in result


def test_clean_text_spacing():
    raw_text = "Multiple   spaces    and \r\n\r\n\r\n\r\n extra newlines."
    cleaned = parser_service._clean_text(raw_text)
    assert "   " not in cleaned
    assert "\r" not in cleaned
    assert "Multiple spaces and\n\nextra newlines." == cleaned
