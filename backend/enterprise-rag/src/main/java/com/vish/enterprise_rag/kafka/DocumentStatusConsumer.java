package com.vish.enterprise_rag.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vish.enterprise_rag.config.KafkaConfig;
import com.vish.enterprise_rag.entities.Document;
import com.vish.enterprise_rag.enums.DocumentStatus;
import com.vish.enterprise_rag.enums.UserActionType;
import com.vish.enterprise_rag.events.DocumentStatusEvent;
import com.vish.enterprise_rag.repositories.read.DocumentReadRepository;
import com.vish.enterprise_rag.repositories.write.DocumentWriteRepository;
import com.vish.enterprise_rag.service.AuditService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Modern Java 25 Kafka listener for DocumentStatusEvent records.
 * Uses pattern matching and virtual-thread-backed execution.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentStatusConsumer {

    private final DocumentReadRepository documentReadRepository;
    private final DocumentWriteRepository documentWriteRepository;
    private final AuditService auditService;

    @Transactional
    @KafkaListener(
            topics = KafkaConfig.STATUS_TOPIC,
            groupId = KafkaConfig.BACKEND_CONSUMER_GROUP,
            containerFactory = "documentStatusListenerContainerFactory"
    )
    public void consumeStatusEvent(DocumentStatusEvent event) {
        log.info("Received DocumentStatusEvent [docId={}, orgId={}, status={}, chunks={}, error='{}']",
                event.documentId(), event.organizationId(), event.status(),
                event.totalChunks(), event.errorMessage());

        Document document = documentReadRepository.findByIdAndIsActiveTrue(event.documentId())
                .orElse(null);

        if (document == null) {
            log.warn("Document ID {} not found or inactive. Discarding status event.", event.documentId());
            return;
        }

        // Apply status and error updates
        document.setStatus(event.status());
        if (event.errorMessage() != null && !event.errorMessage().isBlank()) {
            document.setErrorMessage(event.errorMessage());
        }

        documentWriteRepository.save(document);
        log.info("Document ID {} successfully updated to status: {}", event.documentId(), event.status());

        // Structured audit trail
        String auditMessage = switch (event.status()) {
            case COMPLETED -> String.format("Document vectorization completed successfully (%d chunks indexed)",
                    event.totalChunks() != null ? event.totalChunks() : 0);
            case FAILED -> String.format("Document vectorization failed: %s",
                    event.errorMessage() != null ? event.errorMessage() : "Unknown error");
            default -> String.format("Document status updated to: %s", event.status());
        };

        auditService.audit(UserActionType.UPLOAD_DOCUMENT, document, auditMessage);
    }
}
