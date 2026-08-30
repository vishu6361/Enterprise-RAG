import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.api.v1.ingest import router as ingest_router
from app.api.v1.retrieve import router as retrieve_router
from app.models.schemas import HealthResponse
from app.kafka.producer import kafka_producer
from app.kafka.consumer import kafka_consumer

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info(f"Starting {settings.APP_NAME} on port {settings.PORT}...")
    # Start Kafka Producer and Consumer workers
    if settings.KAFKA_ENABLED:
        await kafka_producer.start()
        await kafka_consumer.start()

    yield

    logger.info(f"Shutting down {settings.APP_NAME}...")
    if settings.KAFKA_ENABLED:
        await kafka_consumer.stop()
        await kafka_producer.stop()


app = FastAPI(
    title=settings.APP_NAME,
    description="Multi-tenant asynchronous AI Knowledge Ingestion and Vector Retrieval Service",
    version="0.1.0",
    lifespan=lifespan,
    docs_url="/docs",
    redoc_url="/redoc",
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register API routes
app.include_router(ingest_router, prefix=settings.API_V1_STR)
app.include_router(retrieve_router, prefix=settings.API_V1_STR)


@app.get("/health", response_model=HealthResponse, tags=["Health"])
async def health_check():
    return HealthResponse(
        status="UP",
        app_name=settings.APP_NAME,
        embedding_model=settings.EMBEDDING_MODEL_NAME,
    )


@app.get("/", tags=["Root"])
async def root():
    return {
        "service": settings.APP_NAME,
        "status": "ONLINE",
        "documentation": "/docs",
        "health": "/health",
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host=settings.HOST, port=settings.PORT, reload=True)
