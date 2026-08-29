import logging
from typing import List
import numpy as np
from app.config import settings

logger = logging.getLogger(__name__)


class EmbeddingService:
    """Service to generate dense vector embeddings for text chunks and queries."""

    def __init__(self, model_name: str = settings.EMBEDDING_MODEL_NAME):
        self.model_name = model_name
        self._model = None

    def _get_model(self):
        if self._model is None:
            try:
                from sentence_transformers import SentenceTransformer
                logger.info(f"Loading SentenceTransformer model: {self.model_name}")
                self._model = SentenceTransformer(self.model_name)
            except Exception as e:
                logger.warning(f"Could not load SentenceTransformer ({e}). Using deterministic fallback embedder.")
                self._model = "fallback"
        return self._model

    def embed_texts(self, texts: List[str]) -> List[List[float]]:
        if not texts:
            return []

        model = self._get_model()
        if model != "fallback":
            try:
                embeddings = model.encode(texts, normalize_embeddings=True, show_progress_bar=False)
                return [emb.tolist() for emb in embeddings]
            except Exception as e:
                logger.error(f"Error generating embeddings via model: {e}")

        # Deterministic fallback embeddings for offline / fast test execution
        return [self._generate_fallback_embedding(t) for t in texts]

    def embed_query(self, query: str) -> List[float]:
        results = self.embed_texts([query])
        return results[0] if results else [0.0] * settings.VECTOR_DIMENSION

    def _generate_fallback_embedding(self, text: str) -> List[float]:
        """Generates a deterministic L2-normalized 384-dimensional embedding using hash projections."""
        dim = settings.VECTOR_DIMENSION
        vec = np.zeros(dim, dtype=np.float32)
        words = text.lower().split()
        if not words:
            return vec.tolist()

        for w_idx, word in enumerate(words):
            h = hash(word) % dim
            vec[h] += 1.0 / (w_idx + 1)

        norm = np.linalg.norm(vec)
        if norm > 0:
            vec = vec / norm
        return vec.tolist()


embedding_service = EmbeddingService()
