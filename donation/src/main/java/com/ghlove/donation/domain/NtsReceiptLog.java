package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 기부금영수증 국세청연계 로그 - AS-IS opmanager/log의 4개 화면(gif-stnd-buga/
 * gif-stnd-sunap/gif-seoul-buga/gif-seoul-sunap: 표준양식/서울시양식 × 부가(발급)/승인
 * 전송이력)을 하나의 테이블로 통합했다(이 프로젝트에서 이미 여러 번 쓴 관례 - UMS가
 * SMS/알림톡/PUSH를, QNA가 1:1문의/공개Q&A를 통합한 것과 동일 패턴). AS-IS 로컬소스에
 * 이 4개 화면이 참조하는 DTO(NextBugaRequestLogDto 등) 실물이 없어(스테일 체크아웃)
 * 정확한 원본 컬럼 구조를 확인하지 못했고, 국세청 연계 자체가 아직 이 프로젝트에서
 * mock조차 안 된 상태라 이 테이블은 화면만 미리 준비해두고 실제 연동이 붙으면 채워진다
 * (SendMailLog/SendSmsLog와 동일하게 "조회 화면 먼저, 실연동은 후속" 상태). Maps G_NTS_RCIPT_LOG.
 */
@Entity
@Table(name = "G_NTS_RCIPT_LOG")
@Getter
@Setter
@NoArgsConstructor
public class NtsReceiptLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_SN")
    private Long logSn;

    @Column(name = "CNTR_SN")
    private String cntrSn;

    /** "STND_BUGA"(표준양식 부가/발급) / "STND_SUNAP"(표준양식 승인) /
     *  "SEOUL_BUGA"(서울시양식 부가) / "SEOUL_SUNAP"(서울시양식 승인). */
    @Column(name = "LOG_TYPE")
    private String logType;

    @Column(name = "REQUEST_DATE")
    private LocalDateTime requestDate;

    @Column(name = "RESPONSE_CODE")
    private String responseCode;

    @Column(name = "RESPONSE_MESSAGE")
    private String responseMessage;

    /** "SUCCESS"/"FAIL". */
    @Column(name = "PROCESS_STATUS")
    private String processStatus;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
