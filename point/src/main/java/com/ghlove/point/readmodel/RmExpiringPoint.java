package com.ghlove.point.readmodel;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 조회 전용 모델 - 만료 예정 포인트 (ISP 조회모델 "만료뷰", SFR-004 "만료 예정 포인트 안내"). */
@Entity
@Table(name = "PT_RM_EXPIRING_POINT")
@IdClass(RmExpiringPointId.class)
@Getter
@Setter
@NoArgsConstructor
public class RmExpiringPoint {

    @Id
    @Column(name = "USER_ID")
    private Long userId;

    /** yyyyMMdd - 원장 lot의 EXPIRATION_DATE와 같은 형식. */
    @Id
    @Column(name = "EXPIRATION_DATE")
    private String expirationDate;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "REMAINING_AMOUNT")
    private Long remainingAmount = 0L;

    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;
}
