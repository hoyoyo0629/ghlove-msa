package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 설문조사 (AS-IS opmanager/qustnr - QustnrManagerController, 신규 테이블). AS-IS OP_QUSTNR는
 * 대상/체크문구 등 더 많은 컬럼을 가졌지만 이 라운드 지시서 범위(제목/기간/상태 + 문항 여러개
 * + 결과집계는 총응답수만)에 맞춰 축소 설계했다 - AS-IS 원본 DB에 없던 테이블(신규 설계). */
@Entity
@Table(name = "OP_QUSTNR")
@Getter
@Setter
@NoArgsConstructor
public class Qustnr {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQustnrSnSeq")
    @SequenceGenerator(name = "opQustnrSnSeq", sequenceName = "op_qustnr_qustnr_sn_seq", allocationSize = 1)
    @Column(name = "QUSTNR_SN")
    private Long qustnrSn;

    @Column(name = "QUSTNR_SJ")
    private String qustnrSj;

    /** yyyyMMdd. */
    @Column(name = "QUSTNR_BGN_DE")
    private String qustnrBgnDe;

    /** yyyyMMdd. */
    @Column(name = "QUSTNR_END_DE")
    private String qustnrEndDe;

    /** Y=노출, N=비노출. */
    @Column(name = "IS_SHOW")
    private String isShow;

    @Column(name = "FRST_CRT_ID")
    private Long frstCrtId;

    @Column(name = "FRST_CRT_DT")
    private LocalDateTime frstCrtDt;

    @Column(name = "LAST_MDFCN_ID")
    private Long lastMdfcnId;

    @Column(name = "LAST_MDFCN_DT")
    private LocalDateTime lastMdfcnDt;
}
