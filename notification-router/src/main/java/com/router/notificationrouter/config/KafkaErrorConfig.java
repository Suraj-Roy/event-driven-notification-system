package com.router.notificationrouter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaErrorConfig {

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<String, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // WHY exponential backoff: 1s → 2s → 4s
        // Gives downstream services time to recover.
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxElapsedTime(15000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);

        // WHY not retryable: These errors will never succeed on retry.
        // Skip directly to DLT.
        handler.addNotRetryableExceptions(
                DeserializationException.class,
                IllegalArgumentException.class
        );

        return handler;
    }
}
