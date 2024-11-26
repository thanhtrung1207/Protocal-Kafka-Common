package com.example.config;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.util.function.SerializableFunction;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class KafkaSinkRecordModelSchema<T> implements KafkaRecordSerializationSchema<T> {

    private static final Logger logger = LoggerFactory.getLogger(KafkaSinkRecordModelSchema.class);

    private static final long serialVersionUID = 1L;

    private String topic;
    private SerializableFunction<T, Object> keyExtractor;
    private SerializableFunction<T, Object> valueExtractor;
    private SerializableFunction<T, Map<String, String>> headerExtractor;

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .build()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    public KafkaSinkRecordModelSchema() {
        this.keyExtractor = el -> null; // null key by default
        this.valueExtractor = el -> el; // use the element itself as value
        this.headerExtractor = el -> new HashMap<>(); // empty headers by default
    }

    public KafkaSinkRecordModelSchema(String topic, SerializableFunction<T, Object> keyExtractor, SerializableFunction<T, Object> valueExtractor) {
        this(); // set default
        this.topic = topic;
        this.keyExtractor = keyExtractor;
        this.valueExtractor = valueExtractor;
    }

    public KafkaSinkRecordModelSchema(String topic, 
                                        SerializableFunction<T, Object> keyExtractor, 
                                        SerializableFunction<T, Object> valueExtractor, 
                                        SerializableFunction<T, Map<String, String>> headerExtractor) {
        this(); // set default values
        this.topic = topic;
        this.keyExtractor = keyExtractor != null ? keyExtractor : this.keyExtractor;
        this.valueExtractor = valueExtractor != null ? valueExtractor : this.valueExtractor;
        this.headerExtractor = headerExtractor != null ? headerExtractor : this.headerExtractor;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(T element, KafkaSinkContext context, Long timestamp) {
        try {
            Iterable<Header> kafkaHeaders = new ArrayList<>();
            Map<String, String> headers = headerExtractor != null ? headerExtractor.apply(element) : new HashMap<>();

            if (headers != null) {
                kafkaHeaders = headers.entrySet()
                        .stream()
                        .map(entry -> new RecordHeader(entry.getKey(), entry.getValue().getBytes()))
                        .collect(Collectors.toList());
            } else {
                log.warn("Headers are null for topic: {} and element: {}", topic, element);
            }


            // Create the ProducerRecord
            return new ProducerRecord<>(
                    topic,
                    null, // Partition can be null for Kafka to auto-assign
                    timestamp != null ? timestamp : Instant.now().toEpochMilli(),
                    objectMapper.writeValueAsBytes(keyExtractor.apply(element)), // Serialized key
                    objectMapper.writeValueAsBytes(valueExtractor.apply(element)), // Serialized value
                    kafkaHeaders // Serialized headers
            );
            
        } catch (JsonProcessingException e) {
            log.error("🔥 Could not serialize record for topic: {} with element: {}. Exception: {}", 
                        topic, element, e.getMessage(), e);
        } catch (Exception e) {
            log.error("🔥 Unexpected error during serialization for topic: {} with element: {}. Exception: {}", 
                        topic, element, e.getMessage(), e);
        }
        return null; // Return null if serialization fails
    }
}
