package com.ghlove.member.config;

import com.ghlove.member.event.MemberEventPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic memberLifecycleTopic() {
        return TopicBuilder.name(MemberEventPublisher.TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
