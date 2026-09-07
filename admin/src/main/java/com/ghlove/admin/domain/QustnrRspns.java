package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** OP_QUSTNR_RSPNS - 설문 응답. 응답 제출 채널(공개 화면)은 이 라운드 범위 밖이라 지금은
 * "결과집계(총응답수만)" 조회 대상 테이블로만 존재한다. */
@Entity
@Table(name = "OP_QUSTNR_RSPNS")
@Getter
@Setter
@NoArgsConstructor
public class QustnrRspns {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQustnrRspnsSnSeq")
    @SequenceGenerator(name = "opQustnrRspnsSnSeq", sequenceName = "op_qustnr_rspns_rspns_sn_seq", allocationSize = 1)
    @Column(name = "RSPNS_SN")
    private Long rspnsSn;

    @Column(name = "QUSTNR_SN")
    private Long qustnrSn;

    @Column(name = "QUSTNR_QESITM_SN")
    private Long qustnrQesitmSn;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "RSPNS_CN")
    private String rspnsCn;

    @Column(name = "RSPNS_DT")
    private LocalDateTime rspnsDt;
}
