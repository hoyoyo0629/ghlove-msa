package com.ghlove.order.config;

import com.ghlove.order.event.OrderSagaPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderSagaTopic() {
        return TopicBuilder.name(OrderSagaPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
