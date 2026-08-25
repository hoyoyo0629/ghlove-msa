package com.ghlove.point.config;

import com.ghlove.point.event.OrderSagaPublisher;
import com.ghlove.point.event.PointLedgerPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/** Declared here too (order service is the "primary" owner) so point can start independently of order's boot order. */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderSagaTopic() {
        return TopicBuilder.name(OrderSagaPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /** point is the producer/owner of this topic (admin consumes it for stats). */
    @Bean
    public NewTopic pointLedgerTopic() {
        return TopicBuilder.name(PointLedgerPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
