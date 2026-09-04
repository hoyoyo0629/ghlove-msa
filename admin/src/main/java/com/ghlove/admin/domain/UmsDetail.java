package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 통합알림 채널별 설정 (AS-IS opmanager/ums 상세) - 하나의 {@link Ums} 템플릿 아래
 *  MESSAGE(SMS)/ALIM_TALK(카카오)/PUSH 채널별로 여러 건 붙는다. */
@Entity
@Table(name = "OP_UMS_DETAIL")
@Getter
@Setter
@NoArgsConstructor
public class UmsDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opUmsDetailIdSeq")
    @SequenceGenerator(name = "opUmsDetailIdSeq", sequenceName = "op_ums_detail_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;

    /** 알림톡 버튼 정보 (카카오 알림톡 채널일 때만 사용). */
    @Column(name = "ALIM_TALK_BUTTONS")
    private String alimTalkButtons;

    /** 연계 시스템 적용 코드(외부 발송사 템플릿 코드 등). */
    @Column(name = "APPLY_CODE")
    private String applyCode;

    /** 발송 실패시 대체 채널로 재시도할지 여부(Y/N). */
    @Column(name = "FAIL_PROCESS_FLAG")
    private String failProcessFlag;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "SEND_TYPE")
    private String sendType;

    @Column(name = "TEMPLATE_CODE", nullable = false)
    private String templateCode;

    @Column(name = "TITLE")
    private String title;

    /** MESSAGE(문자) / ALIM_TALK(카카오 알림톡) / PUSH(앱푸시). */
    @Column(name = "UMS_TYPE", nullable = false)
    private String umsType;

    @Column(name = "USED_FLAG")
    private String usedFlag;

    @Column(name = "UMS_ID")
    private Long umsId;
}
