package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 포인트 거래 원장 (append-only). POINT_AMOUNT는 부호 있는 값
 * (+적립/-사용/-취소회수). 잔액 테이블과 분리 관리 (SFR-004 명시 요구사항).
 *
 * EARN/RESTORE(적립성) 행은 동시에 FIFO "lot"이기도 하다: REMAINING_AMOUNT는
 * 생성 시 POINT_AMOUNT와 같게 시작해서 이후 USE/REVERSE(차감성) 거래가 소비할
 * 때마다 줄어든다 - 소멸 배치가 실제로 안 쓰인 포인트만 정확히 소멸시키기 위함.
 */
@Entity
@Table(name = "PT_POINT_LEDGER")
@Getter
@Setter
@NoArgsConstructor
public class PointLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LEDGER_ID")
    private Long ledgerId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "TXN_TYPE")
    private String txnType;

    @Column(name = "POINT_AMOUNT")
    private Long pointAmount;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "REF_KEY")
    private String refKey;

    @Column(name = "EXPIRATION_DATE")
    private String expirationDate;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "REMAINING_AMOUNT")
    private Long remainingAmount;

    /** 기부연도(yyyy) - AS-IS는 year(G_CNTR.CNTR_DE)로 요약을 묶는다. 적립(EARN) 행은
     *  DONATION_COMPLETED 이벤트의 CNTR_DE에서, 그 밖의 행은 원장 생성연도에서 채운다. */
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    /** 기부금액(실납부액) - AS-IS 요약표의 CNTR_AMT. 적립 행에만 값이 있다. */
    @Column(name = "CNTR_AMT")
    private java.math.BigDecimal cntrAmt;

    /** 적립 행 말고는 기부일을 모른다 - 연도가 비어 있으면 원장 생성연도로 채운다.
     *  조회가 연도로 묶기 때문에 NULL이 섞이면 그 행들이 통째로 빠진다. */
    @PrePersist
    void defaultStdrYear() {
        if (stdrYear == null && createdDate != null) {
            stdrYear = String.valueOf(createdDate.getYear());
        }
    }
}
