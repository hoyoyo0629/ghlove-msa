package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 지자체별 기부포인트 적립률(연도별). point 서비스는 donation 서비스의 DB를 직접
 * 읽을 수 없으므로(DB per Service) 자체 사본을 보관한다 - 하드코딩 금지 원칙.
 *
 * admin 지자체관리 화면(LocgovAdminController)의 "포인트 지급률 등록/수정+이력조회"가
 * 바로 이 테이블에 쓴다 - AS-IS의 ContributionSetup(G_CTBNY_SETUP.POINT_RATE)이 아니라
 * 여기가 실제 적립 계산(PointService#currentPointRateOf)이 읽는 진짜 대상이라는 걸
 * 확인하고 의도적으로 여기로 연결했다(donation의 G_CTBNY_SETUP.POINT_RATE는 시드 데이터만
 * 있고 어떤 코드에서도 읽지 않는 죽은 컬럼).
 */
@Entity
@Table(name = "PT_LOCGOV_POINT_RATE")
@IdClass(LocgovPointRateId.class)
@Getter
@Setter
@NoArgsConstructor
public class LocgovPointRate {

    @Id
    @Column(name = "STDR_YEAR")
    private String stdrYear;

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "POINT_RATE")
    private BigDecimal pointRate;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    /** admin 매니저 정보는 별도 서비스라 FK 없이 쓰기 시점 이름을 그대로 저장(이력 표시용). */
    @Column(name = "LAST_UPDUSR_NM")
    private String lastUpdusrNm;
}
