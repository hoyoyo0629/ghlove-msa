package com.ghlove.point.readmodel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 조회 전용 모델 - 회원의 (지자체 × 기부연도)별 포인트 현황 (SFR-004 "지자체별 사용현황",
 * ISP 조회모델 "포인트잔액"). 쓰기모델(PT_POINT_LEDGER)에서 파생되며 원장 이벤트로 갱신된다.
 *
 * <p>{@code remainingAmount}(원장 부호합)와 {@code lotRemaining}(미소진 lot 합)을 같이 갖는다 -
 * 정상이면 두 값이 같고, 어긋나면 지자체 없는 차감 같은 사고가 있었다는 신호다.
 */
@Entity
@Table(name = "PT_RM_LOCGOV_POINT")
@IdClass(RmLocgovPointId.class)
@Getter
@Setter
@NoArgsConstructor
public class RmLocgovPoint {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Id
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    /** 그 해 그 지자체에 기부한 금액 합 (AS-IS 요약표 CNTR_AMT). */
    @Column(name = "CNTR_AMT")
    private BigDecimal cntrAmt = BigDecimal.ZERO;

    @Column(name = "EARNED_AMOUNT")
    private Long earnedAmount = 0L;

    @Column(name = "USED_AMOUNT")
    private Long usedAmount = 0L;

    /** 원장 부호합 - 마이페이지 "잔여 포인트". */
    @Column(name = "REMAINING_AMOUNT")
    private Long remainingAmount = 0L;

    /** 미소진 lot 합 - 장바구니 "잔여 포인트". */
    @Column(name = "LOT_REMAINING")
    private Long lotRemaining = 0L;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
