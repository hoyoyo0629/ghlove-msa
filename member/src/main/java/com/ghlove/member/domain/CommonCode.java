package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * No-hardcoding principle: code-type data (status/user-type/login-path etc.)
 * is always looked up from this table, never encoded as Java enums/constants.
 */
@Entity
@Table(name = "OP_COMMON_CODE")
@IdClass(CommonCodeId.class)
@Getter
@Setter
@NoArgsConstructor
public class CommonCode {

    @Id
    @Column(name = "CODE_TYPE")
    private String codeType;

    @Id
    @Column(name = "CODE_LANGUAGE")
    private String language;

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "LABEL")
    private String label;

    /** SYSTEM_CONFIG류 코드에서 실제 설정값을 담는 용도 (하드코딩 금지 원칙). */
    @Column(name = "CODE_VALUE")
    private String codeValue;

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "USE_YN")
    private String useYn;
}
