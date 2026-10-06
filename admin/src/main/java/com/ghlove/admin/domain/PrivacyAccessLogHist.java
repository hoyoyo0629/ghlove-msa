package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 개인정보 접근로그 사유 변경이력 (AS-IS OP_PRIVACY_ACCESS_LOG_HIST) - 1411의 "이력보기" 팝업.
 * AS-IS는 사유를 처음 등록할 때도 이 표에 한 줄을 남기므로(CommonController.privacyAccessLog가
 * 저장 직후 privacyAccessLogUpdate를 호출한다), 이력이 2건 이상이면 수정된 것이다.
 */
@Entity
@Table(name = "OP_PRIVACY_ACCESS_LOG_HIST")
@Getter
@Setter
@NoArgsConstructor
public class PrivacyAccessLogHist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "privacyAccessLogHistSeq")
    @SequenceGenerator(name = "privacyAccessLogHistSeq",
            sequenceName = "op_privacy_access_log_hist_hist_id_seq", allocationSize = 1)
    @Column(name = "HIST_ID")
    private Long histId;

    @Column(name = "PRIVACY_ACCESS_LOG_ID")
    private Long privacyAccessLogId;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "MANAGER_ID")
    private Long managerId;

    @Column(name = "REASON")
    private String reason;

    @Column(name = "REASON_TYPE")
    private String reasonType;

    /** 화면 표시용 - managerId로 찾은 로그인ID. */
    @Transient
    private String loginId;

    /** 화면 표시용 - EXCELDOWNLOAD_REASON_TYPE 라벨. */
    @Transient
    private String reasonTypeName;

    @Transient
    public String getCreatedAtText() {
        return createdAt == null ? "" : createdAt.toString().replace('T', ' ');
    }
}
