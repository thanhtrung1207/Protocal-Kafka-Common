package com.example.filter;

import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.model.Demo;
import com.example.until.Duplicator;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemoFilter extends AbstractRichFunction implements FilterFunction<Demo> {
    private static final Logger logger = LoggerFactory.getLogger(DemoFilter.class);

    @Autowired
    private Duplicator duplicator;

    @Override
    public boolean filter(Demo value) {
        String id = "";
        try {
            if (value == null || value.getAge() == null || value.getAge().isEmpty()) {
                logger.warn("⚠️ Invalid input: Demo object or age is null/empty");
                return false;
            }

            id = value.getId();
            if (!isDeduplicate(id)) {
                logger.info("🔥 ID {} is catch by another processing", id);
                return false;
            }
            
            int age = Integer.parseInt(value.getAge());
            if (age > 18) {
                logger.info("🔥 Filter failed: age > 18");
                return false;
            }

        } catch (NumberFormatException ex) {
            logger.error("🔥 Invalid age format for Demo: {}", value, ex);
            return false;
        } catch (Exception ex) {
            logger.error("🔥 Filter failed for value: {}", value, ex);
            return false;
        }

        logger.info("✅ Success filter: {}", value);
        return true;
    }

    @Override
    public void close() {
        duplicator.close();
    }

    @Override
    public void open(Configuration parameters) {
        duplicator.open();
    }

    private boolean isDeduplicate(String id) {
        try {
            if (duplicator.check(id)) {
                logger.warn("⚠️ ID {} is DUPLICATE", id);
                return false;
            }
            logger.info("✅ ID {} is UNIQUE", id);
            return true;
        } catch (Exception ex) {
            logger.error("🔥 Error checking id: {}", id, ex);
            return false;
        }
    }
}
