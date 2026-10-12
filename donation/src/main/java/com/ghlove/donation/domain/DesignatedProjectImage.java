package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 특정사업 기부 이미지 (AS-IS {@code G_DSGN_DNTN_BIZ_IMG_MNG}).
 *
 * <p>AS-IS 등록 화면은 이미지를 <b>여러 장</b> 올린다({@code prjImageFiles[]}, multiple).
 * TO-BE는 지금까지 {@code G_DSGN_DNTN_BIZ_MNG.image_url} 한 칸만 써서 1장만 저장했는데,
 * 표는 처음부터 이 이름으로 있었다(2026-10-07 사용자 요청으로 멀티 등록 구현).
 *
 * <p>{@code imgSeq}가 노출 순서다(AS-IS도 썸네일에 순번을 찍는다). 대표 이미지는
 * {@code imgSeq}가 가장 작은 것이고, 목록·상세 화면이 쓰는 {@code DesignatedProject.imageUrl}은
 * 그 대표 이미지를 가리키도록 서비스가 같이 맞춘다 - 기존 화면을 건드리지 않기 위해서다.
 */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_IMG_MNG")
@Getter
@Setter
@NoArgsConstructor
public class DesignatedProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DSGN_DNTN_BIZ_IMG_ID")
    private Long dsgnDntnBizImgId;

    /** 사업 id (G_DSGN_DNTN_BIZ_MNG.DSGN_DNTN_BIZ_ID). */
    @Column(name = "DSGN_DNTN_BIZ_ID", nullable = false)
    private Long dsgnDntnBizId;

    /** 저장된 파일명. 화면에 보여줄 때는 서비스가 업로드 경로를 앞에 붙인다. */
    @Column(name = "IMG_NM", nullable = false)
    private String imgNm;

    /** 노출 순서(1부터). */
    @Column(name = "IMG_SEQ", nullable = false)
    private Integer imgSeq;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT", nullable = false)
    private LocalDateTime frstRegDt;

    public DesignatedProjectImage(Long dsgnDntnBizId, String imgNm, Integer imgSeq, Long frstRgtrId) {
        this.dsgnDntnBizId = dsgnDntnBizId;
        this.imgNm = imgNm;
        this.imgSeq = imgSeq;
        this.frstRgtrId = frstRgtrId;
        this.frstRegDt = LocalDateTime.now();
    }
}
