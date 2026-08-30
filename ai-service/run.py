import uvicorn
from app.config import settings

if __name__ == "__main__":
    print(f"🚀 Starting {settings.APP_NAME} on http://127.0.0.1:{settings.PORT}")
    print(f"📖 Swagger Docs available at http://127.0.0.1:{settings.PORT}/docs")
    uvicorn.run("app.main:app", host="127.0.0.1", port=settings.PORT, reload=True)
