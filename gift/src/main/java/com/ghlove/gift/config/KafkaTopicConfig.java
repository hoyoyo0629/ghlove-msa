package com.ghlove.gift.config;

import com.ghlove.gift.event.GiftLifecyclePublisher;
import com.ghlove.gift.event.OrderSagaPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declared here too (order service is the "primary" owner) so gift can start independently of order's boot order. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderSagaTopic() {
        return TopicBuilder.name(OrderSagaPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** gift is the producer/owner of this topic (admin consumes it for stats). */
    @Bean
    public NewTopic giftLifecycleTopic() {
        return TopicBuilder.name(GiftLifecyclePublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
