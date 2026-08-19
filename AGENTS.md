# Enterprise RAG Platform — Development Context

## Purpose

Build an enterprise-ready Retrieval-Augmented Generation (RAG) platform as a learning project. The goal is to understand and implement the system incrementally, making and discussing design decisions along the way—not to generate a large application all at once.

## Target architecture

- **Frontend:** React
- **Enterprise/backend service:** Spring Boot
- **AI service:** Python with FastAPI
- **Document ingestion:** asynchronous processing
- **Infrastructure:** introduce Docker, messaging, storage, and other services only when a concrete stage needs them

The services should have clear responsibilities and communicate through explicit APIs or events. Keep the initial design simple; do not add distributed-system complexity before it solves a real requirement.

## Core domain decisions

- Document visibility is scoped at the organization level.
- Document-level editor permissions control who may modify a document.
- Users are deactivated rather than deleted.
- When an owner is deactivated, document ownership must be reassigned.
- Every ownership reassignment must be audited.
- Retain original uploaded files so documents can be retried or reprocessed.
- Detect duplicate uploads using `organization_id` together with a SHA-256 hash of the file content.

## Development rules

- Build vertically and incrementally: complete a small, testable slice before expanding the platform.
- Prefer understandable, maintainable code over premature abstraction or optimization.
- Explain meaningful design choices and trade-offs before making non-trivial implementation changes.
- Keep application code, schemas, APIs, and infrastructure changes focused on the current stage.
- Add tests with each meaningful behavior; use failures as a learning and design tool.
- Do not introduce a dependency, service, queue, database technology, or framework feature without a stated reason.
- Preserve auditability and organization boundaries whenever working with users, documents, permissions, or ingestion.
- Make assumptions explicit when requirements are incomplete; ask before choosing an option that materially changes product scope.

## Collaboration expectations

The developer implements where possible. Code review and guidance should focus on correctness, trade-offs, security, maintainability, and the next smallest useful step. Avoid replacing learning with large generated implementations.
