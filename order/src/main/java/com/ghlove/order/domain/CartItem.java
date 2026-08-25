package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 장바구니 (AS-IS cart/index.html). AS-IS는 옵션/텍스트옵션 등 복잡한 상품구성을 갖지만,
 * 이 프로젝트의 답례품은 단일 옵션 상품이라 회원당 답례품 1건 = 장바구니 행 1개로 단순화한다.
 * 결제(체크아웃)는 장바구니 자체를 주문으로 바꾸는 게 아니라, 선택된 행마다 기존
 * OrderService.createOrder()를 그대로 호출해 단일품목 주문 SAGA를 그대로 재사용한다.
 */
@Entity
@Table(name = "OD_CART_ITEM")
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cartItemIdSeq")
    @SequenceGenerator(name = "cartItemIdSeq", sequenceName = "od_cart_item_cart_item_id_seq", allocationSize = 1)
    @Column(name = "CART_ITEM_ID")
    private Long cartItemId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "ITEM_ID")
    private Long itemId;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
