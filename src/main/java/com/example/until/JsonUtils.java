package com.example.until;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonGenerator.Feature;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    public static String  mapToString(Object object) throws IOException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.configure(Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
            mapper.registerModule(new JavaTimeModule());

            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Exception", e);
            return null;
        }
    }
}
