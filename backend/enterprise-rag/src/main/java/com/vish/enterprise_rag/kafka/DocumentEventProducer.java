package com.vish.enterprise_rag.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.vish.enterprise_rag.config.KafkaConfig;
import com.vish.enterprise_rag.events.DocumentIngestEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Modern Java 25 service for publishing immutable DocumentIngestEvent records to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a DocumentIngestEvent record to Kafka.
     * Uses organizationId as the partition key to guarantee partition-level FIFO ordering per tenant.
     */
    public void sendIngestionEvent(DocumentIngestEvent event) {
        String partitionKey = String.valueOf(event.organizationId());
        log.info("Publishing DocumentIngestEvent to topic '{}' [key={}, docId={}, file='{}']",
                KafkaConfig.INGESTION_TOPIC, partitionKey, event.documentId(), event.filename());

        kafkaTemplate.send(KafkaConfig.INGESTION_TOPIC, partitionKey, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        var metadata = result.getRecordMetadata();
                        log.info("DocumentIngestEvent successfully written: docId={}, partition={}, offset={}",
                                event.documentId(), metadata.partition(), metadata.offset());
                    } else {
                        log.error("Failed to publish DocumentIngestEvent for docId {}: {}",
                                event.documentId(), ex.getMessage(), ex);
                    }
                });
    }
}
