package com.email.emailservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final Duration TTL = Duration.ofHours(24);

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isProcessed(String messageId) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("email:processed:" + messageId));
    }

    public void markProcessed(String messageId) {
        redisTemplate.opsForValue().set("email:processed:" + messageId, "1", TTL);
    }
}
