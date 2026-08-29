from typing import List, Optional
from app.models.schemas import ChunkData, ChunkMetadata
from app.config import settings


class TextChunkerService:
    """Service to split large document texts into semantic chunks with sliding overlap."""

    def __init__(
        self,
        default_chunk_size: int = settings.DEFAULT_CHUNK_SIZE,
        default_chunk_overlap: int = settings.DEFAULT_CHUNK_OVERLAP,
    ):
        self.default_chunk_size = default_chunk_size
        self.default_chunk_overlap = default_chunk_overlap

    def chunk_text(
        self,
        text: str,
        document_id: int,
        organization_id: int,
        filename: str,
        chunk_size: Optional[int] = None,
        chunk_overlap: Optional[int] = None,
    ) -> List[ChunkData]:
        size = chunk_size or self.default_chunk_size
        overlap = chunk_overlap or self.default_chunk_overlap

        if overlap >= size:
            overlap = max(0, size // 4)

        if not text or not text.strip():
            return []

        text = text.strip()
        chunks: List[ChunkData] = []
        
        # Recursive text splitting separators: Paragraph -> Sentence -> Word -> Character
        paragraphs = text.split("\n\n")
        
        current_chunk_text = ""
        current_start_offset = 0
        chunk_index = 0

        for para in paragraphs:
            para = para.strip()
            if not para:
                continue

            # If adding this paragraph fits in current chunk
            if len(current_chunk_text) + len(para) + 2 <= size:
                if current_chunk_text:
                    current_chunk_text += "\n\n" + para
                else:
                    current_chunk_text = para
            else:
                # If current chunk has text, save it
                if current_chunk_text:
                    chunks.append(
                        self._create_chunk(
                            text=current_chunk_text,
                            document_id=document_id,
                            organization_id=organization_id,
                            filename=filename,
                            chunk_index=chunk_index,
                            char_start=current_start_offset,
                            char_end=current_start_offset + len(current_chunk_text),
                        )
                    )
                    chunk_index += 1
                    
                    # Prepare next chunk with overlap from the tail of the current chunk
                    overlap_text = self._get_overlap_suffix(current_chunk_text, overlap)
                    current_start_offset += len(current_chunk_text) - len(overlap_text)
                    current_chunk_text = (overlap_text + "\n\n" + para).strip() if overlap_text else para
                else:
                    # Paragraph itself is larger than chunk_size, split by sentences or hard window
                    sub_chunks = self._split_large_block(para, size, overlap)
                    for sc in sub_chunks:
                        chunks.append(
                            self._create_chunk(
                                text=sc,
                                document_id=document_id,
                                organization_id=organization_id,
                                filename=filename,
                                chunk_index=chunk_index,
                                char_start=current_start_offset,
                                char_end=current_start_offset + len(sc),
                            )
                        )
                        chunk_index += 1
                        current_start_offset += len(sc)
                    current_chunk_text = ""

        # Flush remaining text
        if current_chunk_text:
            chunks.append(
                self._create_chunk(
                    text=current_chunk_text,
                    document_id=document_id,
                    organization_id=organization_id,
                    filename=filename,
                    chunk_index=chunk_index,
                    char_start=current_start_offset,
                    char_end=current_start_offset + len(current_chunk_text),
                )
            )

        return chunks

    def _split_large_block(self, text: str, size: int, overlap: int) -> List[str]:
        """Splits a single monolithic paragraph using sliding window."""
        result = []
        start = 0
        while start < len(text):
            end = min(start + size, len(text))
            
            # Try to snap to nearest sentence or space boundary
            if end < len(text):
                last_space = text.rfind(" ", start, end)
                if last_space > start + (size // 2):
                    end = last_space

            result.append(text[start:end].strip())
            if end >= len(text):
                break
            start = max(start + 1, end - overlap)
        return [r for r in result if r]

    def _get_overlap_suffix(self, text: str, overlap: int) -> str:
        if len(text) <= overlap:
            return ""
        suffix = text[-overlap:].strip()
        # Snap to nearest word start
        space_idx = suffix.find(" ")
        if space_idx != -1 and space_idx < len(suffix) // 2:
            suffix = suffix[space_idx + 1 :]
        return suffix

    def _create_chunk(
        self,
        text: str,
        document_id: int,
        organization_id: int,
        filename: str,
        chunk_index: int,
        char_start: int,
        char_end: int,
    ) -> ChunkData:
        chunk_id = f"doc_{document_id}_chunk_{chunk_index}"
        metadata = ChunkMetadata(
            document_id=document_id,
            organization_id=organization_id,
            chunk_index=chunk_index,
            source_filename=filename,
            char_start=char_start,
            char_end=char_end,
        )
        return ChunkData(chunk_id=chunk_id, text=text, metadata=metadata)


chunker_service = TextChunkerService()
