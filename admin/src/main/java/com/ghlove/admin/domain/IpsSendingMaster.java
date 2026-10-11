package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 문자발송 연계(IPS) 발송 마스터 (AS-IS TIF_IPS_SNDNG_M) - 문자전송이력(7208) 화면이 읽는 표.
 * AS-IS는 이 화면을 {@code op_send_sms_log}가 아니라 외부 문자발송 연계 인터페이스 표에서 읽는다
 * (SmsIpsService / TifIpsSndngMDisplay). TO-BE DB에 AS-IS 컬럼 그대로 이미 존재한다(0행).
 *
 * AS-IS 화면 컬럼 대응: 일련번호(insttCrtSn/listSn, 항상 같은 값) · 전화번호(발송내용
 * sndngCntnts 끝 토큰에서 파싱, {@link #getPhoneNumberText()}) · 구분(svcId→SmsType) ·
 * 발송내용(sndngCntnts) · 생성일시(esbInitTime, {@code INFO_CRT_DT} 아님) ·
 * 전송시작일시(esbTxTime) · 전송완료일시(esbComptTime) · 문자처리상태(esbStatusCd→
 * {@link #getEsbStatusText()}) · 오류내용(esbErrMsg). {@code infoCrtDt}는 AS-IS도 이
 * 화면에 안 쓴다(적재 시점 공통값으로만 기록).
 */
@Entity
@Table(name = "TIF_IPS_SNDNG_M")
@Getter
@Setter
@NoArgsConstructor
public class IpsSendingMaster {

    @Id
    @Column(name = "LIST_SN")
    private Long listSn;

    @Column(name = "INFO_CRT_DT")
    private LocalDateTime infoCrtDt;

    /** 서비스 아이디 - SmsType의 code(예: 812-A002)와 같은 값이라 화면의 "구분"으로 쓴다. */
    @Column(name = "SVC_ID")
    private String svcId;

    @Column(name = "SVC_GRP_ID")
    private String svcGrpId;

    @Column(name = "INSTT_CRT_SN")
    private Long insttCrtSn;

    @Column(name = "INSTT_CRT_DOC_ID")
    private String insttCrtDocId;

    /** 개인식별정보 - 수신 전화번호. */
    @Column(name = "PRVC_IDNTFC_INFO")
    private String prvcIdntfcInfo;

    @Column(name = "PRVC_IDNTFC_SE_CD")
    private String prvcIdntfcSeCd;

    @Column(name = "SNDNG_CNTNTS")
    private String sndngCntnts;

    @Column(name = "ESB_IF_ID")
    private String esbIfId;

    @Column(name = "ESB_TX_ID")
    private String esbTxId;

    @Column(name = "ESB_INIT_TIME")
    private String esbInitTime;

    @Column(name = "ESB_TX_TIME")
    private String esbTxTime;

    @Column(name = "ESB_COMPT_TIME")
    private String esbComptTime;

    @Column(name = "ESB_STATUS_CD")
    private String esbStatusCd;

    @Column(name = "ESB_WORK_GBN")
    private String esbWorkGbn;

    @Column(name = "ESB_ERR_MSG")
    private String esbErrMsg;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{3}\\d{3,4}\\d{4}$");

    /**
     * AS-IS {@code TifIpsSndngMDisplay.getPhoneNumber} - 목록의 "전화번호" 컬럼은
     * {@code PRVC_IDNTFC_INFO}(회원 CI, 암호화)가 아니라 발송내용({@code sndngCntnts})의
     * 마지막 {@code |} 구분 항목에서 뽑아 보여준다(발송내용 끝에 수신 전화번호가 들어간다).
     * 숫자만 뽑아 10~11자리 전화번호 모양이 아니면 빈 문자열을 돌려준다.
     */
    @Transient
    public String getPhoneNumberText() {
        if (sndngCntnts == null || sndngCntnts.isBlank()) {
            return "";
        }
        String[] contents = sndngCntnts.split("\\|");
        String phoneNumber = contents[contents.length - 1];
        String digitsOnly = phoneNumber.replaceAll("-", "");
        return PHONE_PATTERN.matcher(digitsOnly).matches() ? phoneNumber : "";
    }

    /** AS-IS {@code TifIpsSndngMDisplay.getEsbStatus} - N=대기, S=성공, F=실패. */
    @Transient
    public String getEsbStatusText() {
        if ("N".equalsIgnoreCase(esbStatusCd)) {
            return "대기";
        } else if ("S".equalsIgnoreCase(esbStatusCd)) {
            return "성공";
        } else if ("F".equalsIgnoreCase(esbStatusCd)) {
            return "실패";
        } else {
            return "";
        }
    }
}
