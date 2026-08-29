package com.vish.enterprise_rag.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import com.vish.enterprise_rag.events.DocumentStatusEvent;

/**
 * Modern Java 25 & Spring Boot 4 / Spring Kafka configuration.
 * Uses JacksonJsonSerializer, JacksonJsonDeserializer, VirtualThreadTaskExecutor, and DefaultErrorHandler.
 */
@EnableKafka
@Configuration
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    public static final String INGESTION_TOPIC = "document-ingestion-events";
    public static final String STATUS_TOPIC = "document-status-events";
    public static final String BACKEND_CONSUMER_GROUP = "enterprise-rag-backend-group";

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public NewTopic documentIngestionTopic() {
        return TopicBuilder.name(INGESTION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic documentStatusTopic() {
        return TopicBuilder.name(STATUS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // =========================================================================
    // Producer Configuration (Jackson 3 / JacksonJsonSerializer)
    // =========================================================================
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
        
        // High-reliability enterprise producer settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // =========================================================================
    // Consumer Configuration (Jackson 3 / JacksonJsonDeserializer)
    // =========================================================================
    @Bean
    public ConsumerFactory<String, DocumentStatusEvent> documentStatusConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, BACKEND_CONSUMER_GROUP);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        JacksonJsonDeserializer<DocumentStatusEvent> jsonDeserializer =
                new JacksonJsonDeserializer<>(DocumentStatusEvent.class, false);
        jsonDeserializer.trustedPackages("com.vish.enterprise_rag.events");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    /**
     * Listener Container Factory with VirtualThreadTaskExecutor and DefaultErrorHandler (3 retries, 1s backoff).
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DocumentStatusEvent> documentStatusListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DocumentStatusEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(documentStatusConsumerFactory());

        // Java 21/25 Virtual Thread executor for high-concurrency non-blocking event consumption
        factory.getContainerProperties().setListenerTaskExecutor(
                new VirtualThreadTaskExecutor("kafka-status-")
        );

        // Resilient error handler: retry 3 times with 1-second backoff before dead-letter logging
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> log.error(
                        "Exhausted all retries for record with key '{}' on topic '{}'. Error: {}",
                        consumerRecord.key(), consumerRecord.topic(), exception.getMessage()
                ),
                new FixedBackOff(1000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }
}
