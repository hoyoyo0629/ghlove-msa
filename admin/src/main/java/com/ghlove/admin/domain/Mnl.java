package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자매뉴얼 (AS-IS {@code G_MNL} / {@code saleson.shop.manual.domain.Manual}) - 메뉴 5202.
 *
 * <p>한 행이 "공개 화면 한 페이지에 대한 설명"이다. 어느 화면인지는 {@code menu_se_code}가
 * 공통코드 {@code MENU_URL}의 id를 가리켜 정하고, 그 코드의 <b>label이 URL</b>,
 * <b>detail이 화면명</b>이다 - 등록할 때 코드에서 베껴 {@code menu_url}/{@code menu_nm}에 넣는다
 * (AS-IS insertManual의 서브쿼리 두 개).
 *
 * <p><b>★ TO-BE가 같은 용도의 {@code OP_MANUAL}을 새로 만들어 두었는데 그럴 필요가 없었다</b>:
 * AS-IS 표 {@code g_mnl}이 TO-BE 스키마에 이미 있다(AS-IS 덤프에도 있다). 이 프로젝트에서
 * 다섯 번째로 발견된 같은 유형의 중복이다(G_CMNTY_COMMENT·op_manual·OP_SHOP_INQUIRY …).
 * 두 표 모두 0행이라 옮길 데이터는 없었고, {@code op_manual} 표는 남겨 두었다(표 정리는 사용자 판단).
 *
 * <p>첨부는 <b>한 건</b>이고 행 안에 컬럼으로 들어간다({@code file_nm}·{@code orginl_file_nm}·
 * {@code file_ty}). 저장 위치는 업로드 폴더 아래 {@code help}다(AS-IS {@code getUploadPath}).
 */
@Entity
@Table(name = "G_MNL")
@Getter
@Setter
@NoArgsConstructor
public class Mnl {

    /** AS-IS sequenceService.getId("G_MNL"). */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gMnlSeq")
    @SequenceGenerator(name = "gMnlSeq", sequenceName = "admin.g_mnl_seq", allocationSize = 1)
    @Column(name = "MNL_SN")
    private Integer mnlSn;

    /** 공통코드 {@code MENU_URL}의 id - 어느 화면의 매뉴얼인지. */
    @Column(name = "MENU_SE_CODE")
    private String menuSeCode;

    /** 코드의 label(화면 URL)을 베껴 넣은 값. */
    @Column(name = "MENU_URL")
    private String menuUrl;

    /** 코드의 detail(화면명)을 베껴 넣은 값. */
    @Column(name = "MENU_NM")
    private String menuNm;

    @Column(name = "FILE_NM")
    private String fileNm;

    @Column(name = "ORGINL_FILE_NM")
    private String orginlFileNm;

    @Column(name = "FILE_TY")
    private String fileTy;

    /** 조회수 - AS-IS는 등록 시 0을 넣고 올리는 코드가 없다(공개 화면이 없어서다). */
    @Column(name = "INQIRE_CO")
    private Integer inqireCo;

    @Column(name = "MENU_SJ")
    private String menuSj;

    @Column(name = "MENU_CN")
    private String menuCn;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}
