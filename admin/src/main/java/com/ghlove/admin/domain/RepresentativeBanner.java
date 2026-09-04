package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 지정기부 대표배너 (AS-IS G_REPRST_BANNER, opmanager/designated-donation/banner) - PC/모바일
 *  이미지가 분리된, 최대 15슬롯 순서관리 배너. PROCESS_TYPE으로 여러 레거시 화면이 같은
 *  테이블을 공유했으나(AS-IS), 이 프로젝트는 지정기부 전용으로만 쓴다
 *  (PROCESS_TYPE='DESIGNATED_DONATION' 고정) - 기존 OP_MAIN_BANNER(Banner.java, 메인
 *  캐러셀)와는 완전히 다른 테이블/용도. */
@Entity
@Table(name = "G_REPRST_BANNER")
@Getter
@Setter
@NoArgsConstructor
public class RepresentativeBanner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reprstBannerIdSeq")
    @SequenceGenerator(name = "reprstBannerIdSeq", sequenceName = "g_reprst_banner_reprst_banner_id_seq", allocationSize = 1)
    @Column(name = "REPRST_BANNER_ID")
    private Integer reprstBannerId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "FILE_NAME_PC")
    private String fileNamePc;

    @Column(name = "FILE_NAME_MOBILE")
    private String fileNameMobile;

    @Column(name = "LINK_URL")
    private String linkUrl;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "DISPLAY_ORDER")
    private Integer displayOrder;

    @Column(name = "BANNER_CONTENT")
    private String bannerContent;

    @Column(name = "PROCESS_TYPE")
    private String processType;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    /** DB 컬럼이 실제로는 timestamp 타입이다(String으로 매핑돼있던 걸 발견해 수정 -
     *  이 프로젝트의 다른 CREATED_DATE류 컬럼 대부분이 varchar라 그 관례를 따라 잘못
     *  매핑했었다, 이 테이블만 예외). */
    @Column(name = "FRST_REGIST_PNTTM")
    private java.time.LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private java.time.LocalDateTime lastUpdtPnttm;
}
