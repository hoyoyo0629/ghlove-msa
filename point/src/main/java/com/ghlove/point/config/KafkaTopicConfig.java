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

    /** ISP 도메인 이벤트 "포인트예약됨/해제됨" 토픽 - point가 소유한다. */
    @Bean
    public NewTopic pointReservationTopic() {
        return TopicBuilder.name(com.ghlove.point.event.PointReservationPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
