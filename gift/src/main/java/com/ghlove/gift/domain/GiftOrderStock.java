package com.ghlove.gift.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Tracks stock reservations made in response to order.saga ORDER_CREATED
 * events - both for idempotency (don't double-reserve on Kafka redelivery)
 * and so ORDER_CANCELLED handling knows whether this service actually has
 * anything to restore.
 */
@Entity
@Table(name = "GIFT_ORDER_STOCK")
@Getter
@Setter
@NoArgsConstructor
public class GiftOrderStock {

    @Id
    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
