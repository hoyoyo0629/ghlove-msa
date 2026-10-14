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

    /**
     * 연계기관 코드 (공통코드 `LINK_INSTT_CD`). 고향사랑e음이 아닌 외부 창구(연계기관)를 통해
     * 들어온 기부에 채워진다. 포인트 조회 화면의 "기부처" 열이 이 값으로 표시된다 -
     * 비어 있으면 `고향사랑e음`(AS-IS `mypage/cntrPoint.html:127~129`).
     */
    @Column(name = "LINK_INSTT_CD")
    private String linkInsttCd;

    @Column(name = "FRST_REGIST_PNTTM")
    private String frstRegistPnttm;

    @Column(name = "DSGN_DNTN_BIZ_ID")
    private Long dsgnDntnBizId;

    /**
     * 삭제여부 - AS-IS는 기부 집계 쿼리마다 {@code DELETE_AT='N'}을 건다(기부금모금현황·특정사업
     * 집계 등). 컬럼은 TO-BE에도 있었으나 매핑이 없어 그 조건을 걸 수 없었다.
     */
    @Column(name = "DELETE_AT")
    private String deleteAt;

    /**
     * 수납일(yyyyMMdd) - AS-IS는 집계에서 {@code STTEMNT_PAY_DE IS NOT NULL}(수납완료)까지 요구한다.
     * 2026-10-14부터 수납완료(completeDonation)가 AS-IS처럼 IFNULL(수납확인 수납일, 오늘)을 넣고 과오납 승인이
     * 비운다(그 전 완료분은 migration-donation-sttemnt-pay-de-backfill.sql로 채움). 집계 조건도 AS-IS대로 되살렸다.
     */
    @Column(name = "STTEMNT_PAY_DE")
    private String sttemntPayDe;

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

    /** 전자납부번호 - AS-IS는 폼 제출 전 LevyRelayService(세외수입 부과요청)로 먼저 받아온
     *  값을 등록 시점에 같이 저장한다. 컬럼은 DB엔 이미 있었으나(NOT NULL이라 기부 생성 자체가
     *  실패하던 버그가 있어 nullable로 고친 적 있음) 전자수납 연계가 없던 시절엔 매핑하지
     *  않았었다 - 기탁서 등록(15102) Levy relay 배선과 함께 매핑한다. */
    @Column(name = "ELCTRN_PAY_NO")
    private String elctrnPayNo;

    /** 서울시 대상 여부(Y/N) - AS-IS가 세외수입 연계처(지방세외 차세대 vs 서울시) 분기에 쓴다. */
    @Column(name = "SEOUL_TRGET_AT")
    private String seoulTrgetAt;

    /** 납부유효일자(yyyyMMdd) - AS-IS 기탁서 등록: 서울 오늘+1, 그 외 오늘. 지방세외(현세대) 미납 삭제
     *  배치(deleteNotSunapStndBatch)가 이 날짜가 지난 미납건을 지운다. 컬럼은 원래 있었고 매핑만 없었다. */
    @Column(name = "PAY_VALID_DE")
    private String payValidDe;

    /** 접수자명(기탁서 접수 담당자) - AS-IS G_CNTR.RCEPTER_NM. 온라인 기부는 비어 있다. */
    @Column(name = "RCEPTER_NM")
    private String rcepterNm;
}
