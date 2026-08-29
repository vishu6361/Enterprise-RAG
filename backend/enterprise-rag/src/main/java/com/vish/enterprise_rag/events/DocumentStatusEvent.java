package com.vish.enterprise_rag.events;

import java.io.Serializable;
import java.time.Instant;

import com.vish.enterprise_rag.enums.DocumentStatus;

/**
 * Immutable Java 25 record representing a document processing status callback event from Kafka.
 */
public record DocumentStatusEvent(
        Long documentId,
        Long organizationId,
        DocumentStatus status,
        Integer totalChunks,
        String errorMessage,
        Instant timestamp
) implements Serializable {

    public DocumentStatusEvent {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId cannot be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status cannot be null");
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
        private DocumentStatus status;
        private Integer totalChunks = 0;
        private String errorMessage;
        private Instant timestamp = Instant.now();

        public Builder documentId(Long documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder status(DocumentStatus status) {
            this.status = status;
            return this;
        }

        public Builder totalChunks(Integer totalChunks) {
            this.totalChunks = totalChunks;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public DocumentStatusEvent build() {
            return new DocumentStatusEvent(
                    documentId,
                    organizationId,
                    status,
                    totalChunks,
                    errorMessage,
                    timestamp
            );
        }
    }
}
