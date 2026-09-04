package com.ghlove.gift.domain;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
public class MainDisplayItemId implements Serializable {
    private String templateId;
    private Long itemId;

    public MainDisplayItemId(String templateId, Long itemId) {
        this.templateId = templateId;
        this.itemId = itemId;
    }
}
