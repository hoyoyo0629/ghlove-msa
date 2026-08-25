package com.ghlove.point.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LocgovPointRateId implements Serializable {
    private String stdrYear;
    private String locgovCode;
}
