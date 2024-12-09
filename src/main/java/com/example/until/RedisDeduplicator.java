package com.example.until;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import com.example.config.RedisConfig;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisDeduplicator implements Duplicator, Serializable {

    private final RedisConfig redisConfig;
    private transient JedisPool jedisPool;


    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            System.out.println("❌ Redis Deduplicator closed.");
        }
    }

    @Override
    public void open() {
        jedisPool = new JedisPool();
        log.info("✅ Redis Deduplicator initialized.");
    }

    @Override
    public boolean check(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            SetParams setParams = new SetParams()
                .nx()
                .ex((int)redisConfig.ttl);
            String result = jedis.set(
                key, "1", setParams);
            return result == null;
        }
    }
}
