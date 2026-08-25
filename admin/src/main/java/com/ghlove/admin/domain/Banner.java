package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
