package com.example.filter;

import java.time.Duration;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.flink.api.common.functions.AbstractRichFunction;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.config.RedisConfig;
import com.example.model.Demo;
import com.example.until.Duplicator;
import com.example.until.RedisDuplicator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class DemoFilter extends AbstractRichFunction implements FilterFunction<Demo> {
    private static final Logger logger = LoggerFactory.getLogger(DemoFilter.class);

    @Autowired
    private RedisConfig redisConfig;
    
    private transient  Duplicator deduplicator;

    public DemoFilter (RedisConfig redisConfig, Duplicator deduplicator){
        this.redisConfig = redisConfig;
        this.deduplicator = deduplicator;
    }
    @Override
    public boolean filter(Demo value) throws Exception {
        String id = "";
        try {
            if (value == null || value.getAge() == null || value.getAge().isEmpty()) {
                logger.warn("⚠️ Invalid input: Demo object or age is null/empty");
                return false;
            }
            id = value.getId();
            int age = Integer.parseInt(value.getAge());
            if (age > 18) {
                logger.info("🔥 Filter failed: age > 18");
                throw new IllegalArgumentException("Filter failed: age > 18");
            }

            if (isDuplicated(id)) {
                logger.warn("⚠️ Filter id={} -Duplicator by another process", id);
                return false;
            }

        } catch (Exception ex) {
            logger.error("🔥 Filter faile {}", value);
            return false;
        }

        logger.info("✅ Success filter: {}", value);
        return true;
    }
    private Duplicator createDeduplicator() {
        long ttlSeconds = NumberUtils.toLong(redisConfig.NX_TTL_SECONDS);
        long timeoutSeconds = NumberUtils.toLong(redisConfig.command_timeout_seconds);

        return RedisDuplicator.builder()
            .ttl(Duration.ofSeconds(ttlSeconds))
            .commandTimeout(Duration.ofSeconds(timeoutSeconds))
            .build();
    }

    private boolean isDuplicated(String id) {
        try {
            String dedupeKey = String.format("%s%s", redisConfig.KEY_PREFIX, id);
            if (dedupeKey != null && deduplicator.check(dedupeKey)) {
                logger.warn("⚠️ Filter id={} - Duplicator found", id);
                return true;
            }
        }catch(Exception ex){
            logger.error("🔥 Filter Id={} - Failed to check duplicator", id, ex);
        }
        return false;
    }

    @Override
    public void open (Configuration parameters) {
        redisConfig = new RedisConfig();
        if (deduplicator == null) {
            deduplicator = createDeduplicator();
            deduplicator.open();
        }
    }

    @Override
    public void close (){
        if (deduplicator != null) {
            deduplicator.close();
        }
    }
}