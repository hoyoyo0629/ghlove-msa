package com.ghlove.donation.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class HonorCntrbtrId implements Serializable {
    private Integer stdrYear;
    private String locgovCode;
    private Long userId;
}
