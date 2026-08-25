package com.ghlove.admin.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class MenuRightId implements Serializable {
    private Integer menuId;
    private String authority;
}
