package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 통합알림(UMS) 관리 (AS-IS opmanager/ums) - 템플릿코드별 발송채널(MESSAGE/ALIM_TALK/PUSH)
 *  통합설정의 상위(템플릿) 레코드. 실제 채널별 내용은 {@link UmsDetail}이 갖는다. */
@Entity
@Table(name = "OP_UMS")
@Getter
@Setter
@NoArgsConstructor
public class Ums {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opUmsIdSeq")
    @SequenceGenerator(name = "opUmsIdSeq", sequenceName = "op_ums_id_seq", allocationSize = 1)
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

    /** 야간 발송 허용 여부(Y/N). */
    @Column(name = "NIGHT_SEND_FLAG")
    private String nightSendFlag;

    @Column(name = "TEMPLATE_CODE", nullable = false)
    private String templateCode;

    @Column(name = "TEMPLATE_NAME", nullable = false)
    private String templateName;

    @Column(name = "USED_FLAG")
    private String usedFlag;
}
