package com.example.map;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.flink.api.common.functions.MapFunction;

import com.example.model.ElasticsearchSinkModel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ElasticsearchMapFunction<IN,OUT> implements MapFunction<IN, ElasticsearchSinkModel<IN>> {

    private final String indexName;    
    private final String indexDate;    
    private final String idField;    
    private final Set<String>filedsToIgnore;

    public ElasticsearchMapFunction(String indexName, String indexDate, String idField, String... fieldsToIgnore){
        this.indexName = indexName;
        this.indexDate = indexDate;
        this.idField = idField;
        this.filedsToIgnore = new HashSet<>(Arrays.asList(fieldsToIgnore != null ? fieldsToIgnore : new String[0]));
    }

    @Override
    public ElasticsearchSinkModel<IN> map(IN value) throws Exception {
        if (value == null) {
            throw new IllegalAccessException("Input value cannot be null");
        }
        String id = extractId(value);

        IN filteredValue = filterFields(value);

        ElasticsearchSinkModel<IN> esModel = new ElasticsearchSinkModel<>();
        esModel.setIndexName(indexName);
        esModel.setIndexDate(indexDate);
        esModel.setId(id);
        esModel.setSearchIndexName(indexName);
        esModel.setData(filteredValue);

        log.info("✅ Mapped data to ElasticsearchSinkModel: id={} model={}", id, esModel);
        return esModel;
    }

    private String extractId(IN value) throws Exception {
        if (idField == null || idField.isEmpty()) {
            throw new IllegalAccessException("ID field must be specified");
        }
        try{
            Field field = value.getClass().getDeclaredField(idField);
            field.setAccessible(true);
            Object fieldValue = field.get(value);
            if (fieldValue == null) {
                throw new IllegalArgumentException("ID field value cannot be null");
            }
            return String.valueOf(fieldValue);
        }catch(NoSuchFieldException | IllegalAccessException e){
            log.error("Error extracting ID field: {}", idField, e);
            throw new Exception("Failed to extract ID from field " + idField);
        }
    }

    private IN filterFields(IN value) throws Exception {
        for (String fieldName : filedsToIgnore) {
            try {
                Field field = value.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(value, null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                log.warn("Field '{}' not found in object, skipping.", fieldName);
            }
        }
        return value;
    }
    
}
