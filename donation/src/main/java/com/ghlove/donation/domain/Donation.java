package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 기부 (undesignated/general donation). Maps a subset of the AS-IS G_CNTR
 * columns - CNTR_SN is a legacy string id (sequence + timestamp convention,
 * not a DB-generated numeric key), and date/timestamp columns are stored as
 * formatted strings (CNTR_DE=yyyyMMdd, FRST_REGIST_PNTTM=yyyyMMddHHmmss) to
 * match the AS-IS schema rather than native DATE/TIMESTAMP.
 */
@Entity
@Table(name = "G_CNTR")
@Getter
@Setter
@NoArgsConstructor
public class Donation {

    @Id
    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "CNTR_DE")
    private String cntrDe;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "CNTR_LOCGOV_CODE")
    private String cntrLocgovCode;

    @Column(name = "CNTR_AMT")
    private BigDecimal cntrAmt;

    @Column(name = "CNTR_STTUS_CODE")
    private String cntrSttusCode;

    @Column(name = "CNTR_PATH_CODE")
    private String cntrPathCode;

    @Column(name = "FRST_REGIST_PNTTM")
    private String frstRegistPnttm;

    @Column(name = "DSGN_DNTN_BIZ_ID")
    private Long dsgnDntnBizId;

    /** 기탁서(오프라인) 접수 시 입금 정보 메모 - AS-IS 컬럼이었으나 미매핑 상태였음. */
    @Column(name = "RCEPT_BANK_CODE")
    private String rceptBankCode;

    @Column(name = "RCEPT_BANK_NM")
    private String rceptBankNm;

    /** 기부자의 주민등록 주소지 지자체 - AS-IS donation-main.html의 "거주지 확인" 결과
     * (op.donation.js의 psitnLocgovCode). 고향사랑기부금법상 본인 주소지 지자체에는
     * 기부할 수 없어, 이 값과 CNTR_LOCGOV_CODE가 같으면 기부가 거부된다. */
    @Column(name = "PSITN_LOCGOV_CODE")
    private String psitnLocgovCode;

    /** 답례품 제공 여부 - AS-IS presentType (100: 제공받음, 300: 제공받지 않음). */
    @Column(name = "RTNPSNT_REQST_CODE")
    private String rtnpsntReqstCode;

    /** 기부자 안내사항/개인정보 처리 동의 여부 (Y/N). */
    @Column(name = "INFO_AGRE_AT")
    private String infoAgreAt;

    /** 특정사업에 기부하기 상세화면 "응원메시지(기부내역)" 탭에 표시되는 기부자 응원 문구. */
    @Column(name = "CHEER_MSG")
    private String cheerMsg;

    /** 오프라인기부(기탁서) 현장접수 시 캡처한 서명 이미지 파일명 - AS-IS MagicLineController
     *  (매직라인 전자서명 연계, signedFormR)를 대체하는 순수 프론트 canvas 서명패드 캡처본
     *  (외부 하드웨어/연계 없이 PNG로 저장). FileStorageService의 "offgive-signature" 하위
     *  폴더에 저장되고 donation.upload.dir이 /uploads/donation/**로 정적 서빙되므로 별도
     *  다운로드 컨트롤러 없이 그 경로를 그대로 참조한다. */
    @Column(name = "SIGNATURE_FILE_NM")
    private String signatureFileNm;
}
