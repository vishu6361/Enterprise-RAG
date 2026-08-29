package com.vish.enterprise_rag.events;

import java.io.Serializable;
import java.time.Instant;

/**
 * Immutable Java 25 record representing a document ingestion event dispatched to Kafka.
 */
public record DocumentIngestEvent(
        Long documentId,
        Long organizationId,
        Long ownerId,
        String filename,
        String contentType,
        String filePath,
        String contentHash,
        Instant timestamp
) implements Serializable {

    public DocumentIngestEvent {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId cannot be null");
        }
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long documentId;
        private Long organizationId;
        private Long ownerId;
        private String filename;
        private String contentType;
        private String filePath;
        private String contentHash;
        private Instant timestamp = Instant.now();

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder ownerId(Long ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder contentHash(String contentHash) {
            this.contentHash = contentHash;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public DocumentIngestEvent build() {
            return new DocumentIngestEvent(
                    documentId,
                    organizationId,
                    ownerId,
                    filename,
                    contentType,
                    filePath,
                    contentHash,
                    timestamp
            );
        }
    }
}
