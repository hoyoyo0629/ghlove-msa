package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지정기부사업 상태변경 이력 (AS-IS G_DSGN_DNTN_BIZ_APRV_LOG). 배치스캔 당시엔 PK/타임스탬프가
 *  없는 것으로 보여 surrogate LOG_ID/FRST_REGIST_PNTTM을 얹었었지만, 운영 DB 원본 덤프를
 *  다시 받아보니 실제로는 DSGN_DNTN_BIZ_APRV_ID(PK)와 FRST_REG_DT가 이미 있었다 - 그쪽을
 *  정본으로 매핑한다(Java 필드명 logId/frstRegistPnttm은 레포지토리 메서드명 호환을 위해
 *  유지, @Column만 실제 컬럼으로 재매핑). */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_APRV_LOG")
@Getter
@Setter
@NoArgsConstructor
public class DsgnAprvLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dsgnAprvIdSeq")
    @SequenceGenerator(name = "dsgnAprvIdSeq", sequenceName = "g_dsgn_dntn_biz_aprv_log_dsgn_dntn_biz_aprv_id_seq", allocationSize = 1)
    @Column(name = "DSGN_DNTN_BIZ_APRV_ID")
    private Long logId;

    @Column(name = "DSGN_DNTN_BIZ_ID")
    private Long dsgnDntnBizId;

    @Column(name = "APRV_BFR_DSGN_DNTN_BIZ_STTS_CD")
    private String beforeStatusCode;

    @Column(name = "APRV_AFTR_DSGN_DNTN_BIZ_STTS_CD")
    private String afterStatusCode;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegistPnttm;
}
