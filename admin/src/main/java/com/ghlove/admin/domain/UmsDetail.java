package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** UMS 템플릿의 채널별(MESSAGE/ALIM_TALK/PUSH) 상세 설정. Maps OP_UMS_DETAIL. */
@Entity
@Table(name = "OP_UMS_DETAIL")
@Getter
@Setter
@NoArgsConstructor
public class UmsDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "umsDetailIdSeq")
    @SequenceGenerator(name = "umsDetailIdSeq", sequenceName = "op_ums_detail_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UMS_ID")
    private Long umsId;

    @Column(name = "TEMPLATE_CODE")
    private String templateCode;

    /** "MESSAGE"(SMS) / "ALIM_TALK"(카카오알림톡) / "PUSH". */
    @Column(name = "UMS_TYPE")
    private String umsType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "MESSAGE")
    private String message;

    @Column(name = "USED_FLAG")
    private String usedFlag;

    @Column(name = "SEND_TYPE")
    private String sendType;

    @Column(name = "APPLY_CODE")
    private String applyCode;

    @Column(name = "ALIM_TALK_BUTTONS")
    private String alimTalkButtons;

    @Column(name = "FAIL_PROCESS_FLAG")
    private String failProcessFlag;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
