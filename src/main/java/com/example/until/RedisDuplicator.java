package com.example.until;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;

import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import com.example.config.RedisConfig;

@Data
@Builder
@Slf4j
public class RedisDuplicator implements Duplicator {
    @Autowired
    private RedisConfig redisConfig;
    private Duration ttl;
    private static final String REDIS_SET_RESULT_OK = "OK";
    private Duration commandTimeout;
    private volatile boolean connecting;
    private StatefulRedisConnection<String, String> connection;  // Use String for both key and value
    private RedisCommands<String, String> redisCall;

    @Override
    public void open() {
        connectRedis();
    }

    @Override
    public void close() {
        if (redisConnected()) {
            connection.close();
        }
    }

    @Override
    public boolean check(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }

        try {
            if (!redisConnected()) {
                // If Redis is disconnected, reconnect
                log.error("🔥 Redis deduplicator - Redis disconnected. Reconnecting...");
                connectRedis();
                throw new RuntimeException("🔥 Redis deduplicator - Redis disconnected");
            }

            String result = redisCall.set(key, "true", SetArgs.Builder.nx());
            log.info("💡 Redis deduplicator - Redis key: {} - Set result: {}", key, result);

            return REDIS_SET_RESULT_OK.equals(result);

        } catch (Exception ex) {
            log.error("🔥 Redis deduplicator - Redis key: {} Exception", key, ex);
            return false; 
        }
    }

    private boolean redisConnected() {
        try {
            return connection != null && connection.isOpen();
        } catch (Exception ex) {
            log.error("🔥 Fail to check Redis connection", ex);
            return false;
        }
    }

    private synchronized void connectRedis() {
        try {
            if (redisConnected() || connecting) {
                return;
            }

            log.info("💡 Connecting to Redis...");
            connecting = true;
            connection = redisConfig.getConnectionStandaloneRedis();
            redisCall = connection.sync();
        } catch (Exception ex) {
            log.error("🔥 Redis deduplicator - Failed to connect to Redis", ex);
        } finally {
            connecting = false;
        }
    }
}
