import io
import json
import re
from typing import Optional
from pypdf import PdfReader
import docx


class DocumentParserService:
    """Service to parse and extract clean text from multiple file formats."""

    @staticmethod
    def extract_text(file_bytes: bytes, filename: str, content_type: Optional[str] = None) -> str:
        """Extract text based on filename extension and content type."""
        ext = filename.split(".")[-1].lower() if "." in filename else ""

        if ext in ["txt", "log", "csv"]:
            return DocumentParserService._parse_plain_text(file_bytes)
        elif ext in ["md", "markdown"]:
            return DocumentParserService._parse_markdown(file_bytes)
        elif ext == "pdf":
            return DocumentParserService._parse_pdf(file_bytes)
        elif ext in ["docx", "doc"]:
            return DocumentParserService._parse_docx(file_bytes)
        elif ext in ["json"]:
            return DocumentParserService._parse_json(file_bytes)
        else:
            # Fallback to UTF-8 text decoding
            return DocumentParserService._parse_plain_text(file_bytes)

    @staticmethod
    def _parse_plain_text(file_bytes: bytes) -> str:
        for encoding in ["utf-8", "utf-16", "latin-1", "cp1252"]:
            try:
                text = file_bytes.decode(encoding)
                return DocumentParserService._clean_text(text)
            except (UnicodeDecodeError, Exception):
                continue
        return file_bytes.decode("utf-8", errors="replace")

    @staticmethod
    def _parse_markdown(file_bytes: bytes) -> str:
        raw_text = DocumentParserService._parse_plain_text(file_bytes)
        return DocumentParserService._clean_text(raw_text)

    @staticmethod
    def _parse_pdf(file_bytes: bytes) -> str:
        reader = PdfReader(io.BytesIO(file_bytes))
        extracted_pages = []
        for page_num, page in enumerate(reader.pages):
            page_text = page.extract_text()
            if page_text:
                extracted_pages.append(page_text.strip())
        full_text = "\n\n".join(extracted_pages)
        return DocumentParserService._clean_text(full_text)

    @staticmethod
    def _parse_docx(file_bytes: bytes) -> str:
        doc = docx.Document(io.BytesIO(file_bytes))
        paragraphs = [p.text.strip() for p in doc.paragraphs if p.text.strip()]
        for table in doc.tables:
            for row in table.rows:
                row_text = " | ".join(cell.text.strip() for cell in row.cells if cell.text.strip())
                if row_text:
                    paragraphs.append(row_text)
        return DocumentParserService._clean_text("\n\n".join(paragraphs))

    @staticmethod
    def _parse_json(file_bytes: bytes) -> str:
        text = DocumentParserService._parse_plain_text(file_bytes)
        try:
            parsed = json.loads(text)
            return json.dumps(parsed, indent=2)
        except Exception:
            return text

    @staticmethod
    def _clean_text(text: str) -> str:
        """Normalize line breaks and redundant whitespace."""
        text = text.replace("\r\n", "\n").replace("\r", "\n")
        # Strip trailing/leading whitespace from each line and collapse spaces
        lines = [re.sub(r"[ \t]+", " ", line).strip() for line in text.split("\n")]
        text = "\n".join(lines)
        # Replace 3+ newlines with double newline
        text = re.sub(r"\n{3,}", "\n\n", text)
        return text.strip()


parser_service = DocumentParserService()
