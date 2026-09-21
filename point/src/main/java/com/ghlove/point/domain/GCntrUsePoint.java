package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 기부포인트 사용이력 (AS-IS G_CNTR_USE_POINT). 포인트를 쓸 때(주문결제/수동사용/예약확정)
 * FIFO로 소진되는 적립 lot(=기부건, {@link PointLedger} EARN 행의 REF_KEY=CNTR_SN)마다
 * 한 행씩 남긴다 - "어느 기부건 포인트를 어느 주문이 얼마나 썼는지"를 기록해
 * 마이페이지 "기부포인트 현황 상세"의 답례품 주문번호를 채운다.
 *
 * <p>주문취소 복원({@code PointService.restoreForOrder})은 AS-IS deleteGiveUsePoint처럼
 * 그 주문의 사용이력 행을 지운다 - 취소분은 사용 집계에서 통째로 사라진다.
 *
 * <p>DB PK는 (CNTR_SN, USE_SN) 복합키지만, USE_SN을 전역 시퀀스로 유일하게 발번하므로
 * JPA에는 USE_SN 단독 식별자로 매핑한다(삽입/조회/삭제에 문제없음).
 */
@Entity
@Table(name = "G_CNTR_USE_POINT")
@Getter
@Setter
@NoArgsConstructor
public class GCntrUsePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gCntrUsePointSeq")
    @SequenceGenerator(name = "gCntrUsePointSeq", sequenceName = "SEQ_G_CNTR_USE_POINT", allocationSize = 1)
    @Column(name = "USE_SN")
    private Integer useSn;

    /** 사용된 적립 lot의 기부건번호 (EARN 원장행의 REF_KEY). */
    @Column(name = "CNTR_SN")
    private String cntrSn;

    /** 사용일자 yyyyMMdd. */
    @Column(name = "POINT_USE_DE")
    private String pointUseDe;

    @Column(name = "CNTR_USE_POINT")
    private Long cntrUsePoint;

    @Column(name = "USER_ID")
    private Long userId;

    /** 주소지 지자체 - 이 사용경로에서는 알 수 없어 빈 값으로 둔다(NOT NULL 충족용). */
    @Column(name = "PSITN_LOCGOV_CODE")
    private String psitnLocgovCode;

    /** 기부(사용) 지자체 코드. */
    @Column(name = "CNTR_LOCGOV_CODE")
    private String cntrLocgovCode;

    /** 사용구분코드 - AS-IS 집계가 use_se_code='1'(정상사용)만 센다. */
    @Column(name = "USE_SE_CODE")
    private String useSeCode;

    /** 답례품 주문번호 (사용을 유발한 주문/참조). */
    @Column(name = "ORDER_CODE")
    private String orderCode;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;
}
