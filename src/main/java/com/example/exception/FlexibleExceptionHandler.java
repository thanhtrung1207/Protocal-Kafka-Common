package com.example.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.config.interfaces.StorageStrategy;
import com.example.model.ErrorRecord;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FlexibleExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleExceptionHandler.class);
    private final StorageStrategy storageStrategy;

    public void handleException(ErrorRecord errorRecord) {
        try {
            storageStrategy.store(errorRecord);
        } catch (Exception e) {
            logger.error("❌ Failed to handle exception: {}", errorRecord, e);
        }
    }
}
