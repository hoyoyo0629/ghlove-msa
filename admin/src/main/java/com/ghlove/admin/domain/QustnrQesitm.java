package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** OP_QUSTNR_QESITM - 설문 문항(질문 1개당 1행, 자유서술형만 지원하는 축소 설계). */
@Entity
@Table(name = "OP_QUSTNR_QESITM")
@Getter
@Setter
@NoArgsConstructor
public class QustnrQesitm {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opQustnrQesitmSnSeq")
    @SequenceGenerator(name = "opQustnrQesitmSnSeq", sequenceName = "op_qustnr_qesitm_qustnr_qesitm_sn_seq", allocationSize = 1)
    @Column(name = "QUSTNR_QESITM_SN")
    private Long qustnrQesitmSn;

    @Column(name = "QUSTNR_SN")
    private Long qustnrSn;

    @Column(name = "QESTN_CN")
    private String qestnCn;

    @Column(name = "QESTN_SEQ")
    private Integer qestnSeq;
}
