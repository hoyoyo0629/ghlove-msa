package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 통합알림(UMS) 템플릿 (AS-IS opmanager/ums - UmsManagerController). 알림 이벤트별로
 * SMS/알림톡/PUSH 3채널을 하나의 템플릿코드 아래 묶어 관리한다. Maps OP_UMS. */
@Entity
@Table(name = "OP_UMS")
@Getter
@Setter
@NoArgsConstructor
public class Ums {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "umsIdSeq")
    @SequenceGenerator(name = "umsIdSeq", sequenceName = "op_ums_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TEMPLATE_CODE")
    private String templateCode;

    @Column(name = "TEMPLATE_NAME")
    private String templateName;

    /** "Y"/"N" - 야간(21시~08시) 발송 허용 여부. */
    @Column(name = "NIGHT_SEND_FLAG")
    private String nightSendFlag;

    @Column(name = "USED_FLAG")
    private String usedFlag;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
