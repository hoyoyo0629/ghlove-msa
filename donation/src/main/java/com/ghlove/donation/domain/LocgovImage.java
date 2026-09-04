package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체 답례품 배경이미지 (G_LOCGOV_IMAGE, PK=LOCGOV_CODE) - admin 지자체관리 등록/수정
 * 화면의 "답례품 배경이미지" PC/MOBILE 업로드(AS-IS user/locgov/form.jsp). 직인과 달리
 * 암호화하지 않고 donation.upload.dir 아래(공개 정적 서빙)에 저장한다. */
@Entity
@Table(name = "G_LOCGOV_IMAGE")
@Getter
@Setter
@NoArgsConstructor
public class LocgovImage {

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "PC_FILE_NAME")
    private String pcFileName;

    @Column(name = "MOBILE_FILE_NAME")
    private String mobileFileName;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}
