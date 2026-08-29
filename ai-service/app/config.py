from pydantic_settings import BaseSettings, SettingsConfigDict
from typing import List


class Settings(BaseSettings):
    APP_NAME: str = "Enterprise RAG AI Service"
    API_V1_STR: str = "/api/v1/ai"
    PORT: int = 8000
    HOST: str = "0.0.0.0"
    DEBUG: bool = False

    # Chunking Configuration
    DEFAULT_CHUNK_SIZE: int = 800
    DEFAULT_CHUNK_OVERLAP: int = 150

    # Embedding Configuration
    EMBEDDING_MODEL_NAME: str = "all-MiniLM-L6-v2"
    VECTOR_DIMENSION: int = 384

    # Kafka Event Pipeline Configuration
    KAFKA_BOOTSTRAP_SERVERS: str = "localhost:9092"
    KAFKA_INGESTION_TOPIC: str = "document-ingestion-events"
    KAFKA_STATUS_TOPIC: str = "document-status-events"
    KAFKA_CONSUMER_GROUP: str = "enterprise-rag-ai-group"
    KAFKA_ENABLED: bool = True

    # CORS
    CORS_ORIGINS: List[str] = [
        "http://localhost:5173",
        "http://localhost:8800",
        "http://127.0.0.1:5173",
        "http://127.0.0.1:8800",
    ]

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=True,
        extra="allow",
    )


settings = Settings()
