package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 메인 배너. Maps a subset of the AS-IS OP_MAIN_BANNER columns (이미지는 업로드가 아니라
 *  이미 static 리소스로 배포된 AS-IS 배너 이미지 경로를 그대로 참조한다). */
@Entity
@Table(name = "OP_MAIN_BANNER")
@Getter
@Setter
@NoArgsConstructor
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bannerIdSeq")
    @SequenceGenerator(name = "bannerIdSeq", sequenceName = "op_main_banner_banner_id_seq", allocationSize = 1)
    @Column(name = "BANNER_ID")
    private Integer bannerId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENTS")
    private String contents;

    @Column(name = "LINK_URL")
    private String linkUrl;

    @Column(name = "IMAGE_URL")
    private String imageUrl;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    /** yyyyMMddHHmmss - DB 컬럼이 VARCHAR(14)인 AS-IS 관례(다른 콘텐츠 엔티티들과 동일).
     *  실제로 발견한 버그: 이 필드가 예전엔 LocalDateTime으로 잘못 매핑돼 있어서 배너
     *  등록(createBanner) 자체가 매번 타입 불일치로 실패하고 있었다 - 메인화면에
     *  배너가 하나도 안 쌓여있던(=슬라이드가 안 보이던) 진짜 원인. */
    @Column(name = "CREATED_DATE")
    private String createdDate;
}
