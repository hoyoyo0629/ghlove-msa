package com.ghlove.donation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 특정사업 상세설명 이미지의 접근성 설명(AS-IS G_DSGN_DNTN_BIZ_CN_IMG_EXPLN).
 *
 * <p>AS-IS 등록/수정 폼(form.jsp)의 "이미지 설명" 표가 이 표에 저장된다. 상세설명 에디터에
 * 넣은 이미지의 alt 로 쓰이며, AS-IS는 제출 시 <b>이미지 개수와 설명 개수가 같아야만</b>
 * 저장을 허용한다(시각 장애인 접근성).
 *
 * <p>공지쪽 {@link DesignatedNoticeImgExpln} 과 컬럼 구성이 거의 같지만 이 표는 복합키가
 * 아니라 <b>자체 PK(DSGN_DNTN_BIZ_CN_IMG_ID, 시퀀스)</b>를 가진다 - AS-IS DDL 그대로다.
 */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_CN_IMG_EXPLN")
@Getter
@Setter
@NoArgsConstructor
public class DesignatedProjectImgExpln {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DSGN_DNTN_BIZ_CN_IMG_ID")
    private Long imgId;

    @Column(name = "DSGN_DNTN_BIZ_ID", nullable = false)
    private Long dsgnDntnBizId;

    /** 순번(1부터) - 화면의 "순번" 칸이고 readonly 다. */
    @Column(name = "IMG_SEQ", nullable = false)
    private Integer imgSeq;

    @Column(name = "IMG_EXPLN", nullable = false)
    private String imgExpln;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT", nullable = false)
    private LocalDateTime frstRegDt;

    public DesignatedProjectImgExpln(Long dsgnDntnBizId, Integer imgSeq, String imgExpln, Long frstRgtrId) {
        this.dsgnDntnBizId = dsgnDntnBizId;
        this.imgSeq = imgSeq;
        this.imgExpln = imgExpln;
        this.frstRgtrId = frstRgtrId;
        this.frstRegDt = LocalDateTime.now();
    }
}
