package com.example.config;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisConfig implements Serializable{
    @Value("${datasource.redis.host}")
    public String host;

    @Value("${datasource.redis.port}")
    public String port;

    @Value("${datasource.redis.username}")
    public String username;

    @Value("${datasource.redis.password}")
    public String password;

    @Value("${datasource.redis.key_prefix}")
    public String KEY_PREFIX;

    @Value("${datasource.redis.nx_ttl_seconds}")
    public String NX_TTL_SECONDS;

    @Value("${datasource.redis.command_timeout_seconds}")
    public String command_timeout_seconds;

    public static int DEFAULT_REDIS_DATABASE = 0;

    public static RedisURI createURI(String host, int port, String username, String password){
        return createURI(host, port, username, password, DEFAULT_REDIS_DATABASE);
    }
    private static RedisURI createURI(String host, int port, String username, String password, int database){
        Objects.requireNonNull(username,"Username must not be null");
        Objects.requireNonNull(password,"Password must not be null");

        return RedisURI.builder()
            .withHost(host)
            .withPort(port)
            .withDatabase(database)
            .withAuthentication(username,password)
            .build();
    }

    public StatefulRedisConnection getConnectionStandaloneRedis() {
        RedisURI uri = createURI(this.host, Integer.parseInt(this.port), this.username, this.password);
        RedisClient redisClient = RedisClient.create(uri);
        return redisClient.connect();
    }
}
