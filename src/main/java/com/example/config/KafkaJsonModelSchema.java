package com.example.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KafkaJsonModelSchema<T> implements DeserializationSchema<T>, SerializationSchema<T> {
    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .build()
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .findAndRegisterModules();

    private boolean logEnabled = false;
    private Class<T> type;
    
    public KafkaJsonModelSchema(Class<T> clazz) {
        this.type = clazz;
    }

    
    public KafkaJsonModelSchema(Class<T> clazz, boolean isLogEnabled) {
        this.type = clazz;
        this.logEnabled = isLogEnabled; 
    }

    public KafkaJsonModelSchema(TypeInformation<T> typeInformation) {
        this.type = typeInformation.getTypeClass();
    }

    // enable log
    public KafkaJsonModelSchema<T> setLogEnabled(boolean enabled) {
        this.logEnabled = enabled;
        return this;
    }

    @Override
    public TypeInformation<T> getProducedType() {
        return TypeExtractor.getForClass(type);
    }

    @Override
    public byte[] serialize(T element) {
        try {
            return objectMapper.writeValueAsString(element).getBytes();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            log.error("🔥 Failed to parse JSON", e);
        }
        return new byte[0];
    }

    @Override
    public T deserialize(byte[] message) throws IOException {
        // Parse the message
        String data = new String(message, StandardCharsets.UTF_8);
        if (logEnabled) {
            log.info("💡 Kafka data: {}", data);
        }

        if (data != null && data.length() > 0) {
            try {
                return objectMapper.readValue(data, type);
            } catch (Exception e) {
                log.error("🔥 Invalid data format - Exception", e);
            }
        } else {
            log.error("🔥 Invalid data format");
        }
        return null;
    }

    @Override
    public boolean isEndOfStream(T nextElement) {
        return false;
    }
}
