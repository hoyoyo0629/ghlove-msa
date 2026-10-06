package com.ghlove.gift.domain;

import java.io.Serializable;
import java.util.Objects;

/** GiftAddition의 복합키 (ITEM_ID, ADDITION_ITEM_ID). */
public class GiftAdditionId implements Serializable {

    private Long itemId;
    private Long additionItemId;

    public GiftAdditionId() {
    }

    public GiftAdditionId(Long itemId, Long additionItemId) {
        this.itemId = itemId;
        this.additionItemId = additionItemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GiftAdditionId that)) {
            return false;
        }
        return Objects.equals(itemId, that.itemId) && Objects.equals(additionItemId, that.additionItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, additionItemId);
    }
}
