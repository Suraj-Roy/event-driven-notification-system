package com.router.notificationrouter.config;

import com.common.constants.KafkaTopics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic requestTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_REQUEST)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_EMAIL)
                .partitions(3)
                .replicas(1)
                .build();
    }


    @Bean
    public NewTopic pushTopic() {
        return TopicBuilder.name(KafkaTopics.NOTIFICATION_PUSH)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
