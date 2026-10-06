package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 이메일 발송 (AS-IS opmanager/email - saleson.shop.email.domain.Email).
 *
 * 관리자가 발송대상(권한별 / 답례품 / 개별)을 고르고 제목·첨부파일·내용(스마트에디터)을 작성해
 * 즉시 또는 지정시각에 외부 메일발송(EMS) 시스템으로 보내는 도구다. 표 구조는 AS-IS
 * {@code OP_EMAIL}과 동일하다.
 */
@Entity
@Table(name = "OP_EMAIL")
@Getter
@Setter
@NoArgsConstructor
public class OpEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opEmailIdSeq")
    @SequenceGenerator(name = "opEmailIdSeq", sequenceName = "op_email_email_id_seq", allocationSize = 1)
    @Column(name = "EMAIL_ID")
    private Long emailId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    /** yyyyMMddHHmmss - 즉시(D)면 등록시각, 지정(R)이면 화면에서 고른 시각. */
    @Column(name = "SEND_DATE")
    private String sendDate;

    /** EMAIL_STATUS 공통코드 - R:발송요청 S:발송요청성공 F:발송요청실패 C:전송완료 P:전송실패 T:전송일부성공. */
    @Column(name = "STATUS")
    private String status;

    /** A:권한별 S:답례품 E:개별 L:로그인인증이메일(화면에 없는 내부 발송). */
    @Column(name = "AUTH_TARGET")
    private String authTarget;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    /** 목록/상세에서 보여줄 EMAIL_STATUS 공통코드 라벨 (AS-IS는 쿼리에서 조인해 가져온다). */
    @Transient
    private String statusName;

    /** 발송자 이름 - AS-IS는 FRST_REGISTER_ID로 OP_MANAGER를 LEFT JOIN해 가져온다. */
    @Transient
    private String userName;

    /** D:즉시 R:지정 - 등록 폼에서만 쓰는 값이고 표에는 저장하지 않는다(AS-IS 동일). */
    @Transient
    private String sendType;

    @Transient
    private List<OpEmailDetail> authList = new ArrayList<>();

    @Transient
    private List<OpEmailFile> fileList = new ArrayList<>();

    /**
     * AS-IS Email.isEmailSend() 그대로 - EMS 리포트가 집계된 상태(전송완료/전송실패/일부성공)
     * 에서만 상세화면의 발송결과·발송인원 영역이 뜬다.
     */
    @Transient
    public boolean isEmailSend() {
        return "C".equals(status) || "P".equals(status) || "T".equals(status);
    }

    /** AS-IS op:datetime - yyyyMMddHHmmss 저장값을 날짜시각으로 보여준다. */
    @Transient
    public String getSendDateText() {
        if (sendDate == null || sendDate.length() < 8) {
            return sendDate == null ? "" : sendDate;
        }
        StringBuilder sb = new StringBuilder()
                .append(sendDate, 0, 4).append('-').append(sendDate, 4, 6).append('-').append(sendDate, 6, 8);
        if (sendDate.length() >= 12) {
            sb.append(' ').append(sendDate, 8, 10).append(':').append(sendDate, 10, 12);
            if (sendDate.length() >= 14) {
                sb.append(':').append(sendDate, 12, 14);
            }
        }
        return sb.toString();
    }

    /** AS-IS op:date - 목록의 등록 일자(쿼리에서 %Y%m%d로 잘라 내려주는 값)에 대응. */
    @Transient
    public String getFrstRegistPnttmText() {
        return frstRegistPnttm == null ? "" : frstRegistPnttm.toLocalDate().toString();
    }
}
