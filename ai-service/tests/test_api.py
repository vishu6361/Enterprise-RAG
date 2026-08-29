import io
import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


def test_health_endpoint():
    response = client.get("/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "UP"
    assert "embedding_model" in data


def test_ingest_text_and_retrieve_flow():
    # 1. Ingest raw text into Org 1
    ingest_payload = {
        "document_id": 101,
        "organization_id": 1,
        "filename": "kubernetes_guide.md",
        "text_content": (
            "Kubernetes is an open-source system for automating deployment, scaling, and management "
            "of containerized applications. It groups containers that make up an application into logical units "
            "for easy management and discovery."
        ),
    }
    ingest_res = client.post("/api/v1/ai/ingest/text", json=ingest_payload)
    assert ingest_res.status_code == 200
    data = ingest_res.json()
    assert data["status"] == "COMPLETED"
    assert data["total_chunks"] >= 1

    # 2. Retrieve in Org 1
    retrieve_payload = {
        "query": "What is Kubernetes and how does it manage containers?",
        "organization_id": 1,
        "top_k": 3,
    }
    retrieve_res = client.post("/api/v1/ai/retrieve", json=retrieve_payload)
    assert retrieve_res.status_code == 200
    ret_data = retrieve_res.json()
    assert ret_data["total_retrieved"] >= 1
    assert "Kubernetes" in ret_data["results"][0]["text"]
    assert ret_data["results"][0]["document_id"] == 101

    # 3. Retrieve in Org 2 (Must return 0 results due to strict tenant boundary)
    retrieve_org2 = {
        "query": "Kubernetes container deployment",
        "organization_id": 2,
        "top_k": 3,
    }
    retrieve_org2_res = client.post("/api/v1/ai/retrieve", json=retrieve_org2)
    assert retrieve_org2_res.status_code == 200
    assert retrieve_org2_res.json()["total_retrieved"] == 0


def test_ingest_file_multipart():
    file_content = b"Enterprise RAG architecture uses Spring Boot for enterprise logic and FastAPI for AI."
    files = {
        "file": ("architecture_spec.txt", io.BytesIO(file_content), "text/plain"),
    }
    data = {
        "document_id": "202",
        "organization_id": "5",
    }

    response = client.post("/api/v1/ai/ingest/file", data=data, files=files)
    assert response.status_code == 200
    res_json = response.json()
    assert res_json["status"] == "COMPLETED"
    assert res_json["document_id"] == 202
    assert res_json["organization_id"] == 5
