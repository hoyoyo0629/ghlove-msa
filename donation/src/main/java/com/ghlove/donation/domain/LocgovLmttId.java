package com.ghlove.donation.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class LocgovLmttId implements Serializable {
    private String lmttBgnDe;
    private String lmttEndDe;
    private String locgovCode;
}
