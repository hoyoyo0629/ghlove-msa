package com.ghlove.admin.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ManagerRequestId implements Serializable {
    private Long userId;
    private Integer reqstSn;
}
