# Enterprise RAG Platform

An enterprise-oriented Retrieval-Augmented Generation (RAG) platform, built incrementally as a learning project. It will let organizations upload and manage documents, process them for retrieval, and use them to ground AI-assisted answers.

## Architecture

| Area | Planned technology | Responsibility |
| --- | --- | --- |
| Frontend | React | User-facing document and RAG workflows |
| Backend | Spring Boot | Organizations, users, documents, permissions, auditability, and product APIs |
| AI service | Python / FastAPI | Ingestion, processing, retrieval, and AI-oriented capabilities |

Document ingestion will run asynchronously. Supporting infrastructure such as Docker, message queues, object storage, vector storage, and observability will be introduced only when a project stage requires it.

## Current MVP scope

The initial MVP will establish the smallest end-to-end foundation for:

- Organization-scoped document management
- User deactivation instead of deletion
- Document-level editor permissions
- Audited ownership reassignment when an owner is deactivated
- Retaining original uploads for retry and reprocessing
- Duplicate detection using organization ID and a SHA-256 content hash
- Asynchronous ingestion as the basis for later retrieval

Application code has deliberately not been started yet.

## Planned stages

1. Define the domain model, boundaries, and local development workflow.
2. Build core Spring Boot APIs for organizations, users, documents, permissions, and auditing.
3. Add file upload, persistent original-file storage, duplicate detection, and ingestion job tracking.
4. Build the Python/FastAPI ingestion service and connect asynchronous processing.
5. Add chunking, embeddings, retrieval, and a minimal grounded-question workflow.
6. Add a React interface for the core workflows.
7. Strengthen security, observability, reliability, and deployment practices.

Each stage should produce a small, testable result before the next begins.

## Development approach

This project prioritizes learning and deliberate implementation. Keep changes small, discuss important design decisions, and avoid generating broad implementations before the requirements and previous stage are understood. See [AGENTS.md](AGENTS.md) for shared development context and rules.
