package com.ghlove.donation.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LocgovDeptHistId implements Serializable {
    private String locgovCode;
    private Integer deptHistNo;
}
