package com.example.filter;

import org.apache.flink.api.common.functions.FilterFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.model.Demo;

public class DemoFilter implements FilterFunction<Demo> {
    private static final Logger logger = LoggerFactory.getLogger(DemoFilter.class);

    @Override
    public boolean filter(Demo value) throws Exception {
        try {
            if (value == null || value.getAge() == null || value.getAge().isEmpty()) {
                logger.warn("⚠️ Invalid input: Demo object or age is null/empty");
                return false;
            }

            int age = Integer.parseInt(value.getAge());
            if (age > 18) {
                logger.info("🔥 Filter failed: age > 18");
                throw new IllegalArgumentException("Filter failed: age > 18");
            }

        } catch (Exception ex) {
            logger.error("🔥 Filter faile {}", value);
            return false;
        }

        logger.info("✅ Success filter: {}", value);
        return true;
    }
}