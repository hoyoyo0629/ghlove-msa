package com.ghlove.donation.event;

import com.ghlove.donation.domain.Donation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Both event types for a given donation are published to the SAME topic,
 * keyed by cntrSn - Kafka only guarantees ordering within a partition for a
 * given key, so a COMPLETED and a later CANCELLED for the same donation must
 * share a topic+key or a consumer can legitimately observe them out of order
 * (this happened in practice: point service processed a CANCELLED event
 * before the COMPLETED event for the same cntrSn because they lived on two
 * separate topics consumed by independent listener threads). The event type
 * travels as a Kafka header rather than a payload field so each event's JSON
 * shape stays exactly what it was.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DonationEventPublisher {

    public static final String TOPIC = "donation.lifecycle";
    public static final String HEADER_EVENT_TYPE = "eventType";
    public static final String EVENT_TYPE_COMPLETED = "COMPLETED";
    public static final String EVENT_TYPE_CANCELLED = "CANCELLED";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompleted(Donation donation) {
        DonationCompletedEvent event = new DonationCompletedEvent(
                donation.getCntrSn(),
                donation.getUserId(),
                donation.getCntrLocgovCode(),
                donation.getCntrAmt(),
                donation.getCntrDe(),
                LocalDateTime.now(),
                donation.getCntrPathCode(),
                donation.getPsitnLocgovCode());
        send(donation.getCntrSn(), event, EVENT_TYPE_COMPLETED);
    }

    public void publishCancelled(Donation donation) {
        DonationCancelledEvent event = new DonationCancelledEvent(
                donation.getCntrSn(),
                donation.getUserId(),
                donation.getCntrLocgovCode(),
                donation.getCntrAmt(),
                LocalDateTime.now());
        send(donation.getCntrSn(), event, EVENT_TYPE_CANCELLED);
    }

    private void send(String key, Object event, String eventType) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(TOPIC, null, key, event,
                List.of(new RecordHeader(HEADER_EVENT_TYPE, eventType.getBytes(StandardCharsets.UTF_8))));

        kafkaTemplate.send(record).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} event for cntrSn={}", eventType, key, ex);
            } else {
                log.info("Published {} event cntrSn={} to partition={} offset={}",
                        eventType, key, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
            }
        });
    }
}
