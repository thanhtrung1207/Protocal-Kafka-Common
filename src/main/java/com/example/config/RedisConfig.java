package com.example.config;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class RedisConfig implements Serializable {
    @Value("${datasource.redis.host}")
    public String redisUri;

    @Value("${datasource.redis.port}")
    public String port;

    @Value("${datasource.redis.username}")
    public String username;

    @Value("${datasource.redis.password}")
    public String password;

    @Value("${datasource.redis.key_prefix}")
    public String keyPrefix;

    @Value("${datasource.redis.nx_ttl_seconds}")
    public long ttl;
}
