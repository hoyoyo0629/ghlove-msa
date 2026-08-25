package com.ghlove.point.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * No-hardcoding principle: code-type data (transaction types, system config
 * values) is always looked up from this table, never encoded as Java
 * enums/constants.
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

    @Column(name = "ORDERING")
    private Integer ordering;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "CODE_VALUE")
    private String codeValue;
}
