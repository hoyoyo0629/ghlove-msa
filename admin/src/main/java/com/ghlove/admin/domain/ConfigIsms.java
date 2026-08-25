package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** ISMS(정보보호관리체계) 설정 - 광고성 메일/문자 발송 가능 시간대 등 (AS-IS
 *  opmanager/isms/isms-config). ISMS_TYPE: 0=공통, 1=관리자, 2=회원. */
@Entity
@Table(name = "OP_CONFIG_ISMS")
@Getter
@Setter
@NoArgsConstructor
public class ConfigIsms {

    @Id
    @Column(name = "KEY")
    private String key;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "ISMS_TYPE")
    private String ismsType;

    /** yyyyMMddHHmmss. */
    @Column(name = "UPDATE_DATE")
    private String updateDate;
}
