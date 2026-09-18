package com.ghlove.gift.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 재입고 알림 신청 (AS-IS `OP_RESTOCK_NOTICE`).
 *
 * <p>품절된 답례품 상세화면에서 회원이 "재입고 알림"을 신청해 두면, 재고가 다시 들어왔을 때
 * 알려준다. AS-IS는 상세화면 진입 시 `GET /api/item/restock`으로 이미 신청했는지 확인하고,
 * 신청은 `POST /api/item/restock`으로 한다(`ItemController:850~896`).
 *
 * <p>`SEND_FLAG`는 발송 여부다 - 신청 시 `N`, 재입고 알림을 실제로 보낸 뒤 `Y`가 된다.
 * 알림 발송 자체는 외부 SMS/알림톡 연계라 이 환경에서는 열려 있지 않다.
 */
@Entity
@Table(name = "op_restock_notice")
@Getter
@Setter
@NoArgsConstructor
public class RestockNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESTOCK_NOTICE_ID")
    private Long restockNoticeId;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "USER_ID")
    private Long userId;

    /** N=미발송, Y=발송완료. */
    @Column(name = "SEND_FLAG")
    private String sendFlag = "N";

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
