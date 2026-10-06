package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 콘텐츠 만족도(AS-IS G_STSFDG) - 사용자가 화면(menu_url)별로 남긴 만족도 평가.
 * AS-IS CntntsStsfdgManagerController(/opmanager/cntnts-stsfdg)가 URL별로 집계해 보여준다.
 * 조회 전용(평가 자체는 사용자 프론트가 적재). stsfdg 값: 4=매우 만족, 3=만족, 2=불만족, 1=매우 불만족.
 */
@Entity
@Table(name = "G_STSFDG")
@Getter
@Setter
@NoArgsConstructor
public class ContentSatisfaction {

    @Id
    @Column(name = "STSFDG_SN")
    private Integer stsfdgSn;

    @Column(name = "MENU_URL")
    private String menuUrl;

    @Column(name = "MENU_NM")
    private String menuNm;

    /** 4=매우 만족, 3=만족, 2=불만족, 1=매우 불만족. */
    @Column(name = "STSFDG")
    private Integer stsfdg;

    @Column(name = "STSFDG_CN")
    private String stsfdgCn;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;
}
