package com.ghlove.member.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeId implements Serializable {
    private String codeType;
    private String language;
    private String id;
}
