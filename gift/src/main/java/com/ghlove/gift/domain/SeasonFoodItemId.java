package com.ghlove.gift.domain;

import java.io.Serializable;
import java.util.Objects;

/** SeasonFoodItem의 복합키 (ITEM_ID, SEASON_FOOD_MONTH). */
public class SeasonFoodItemId implements Serializable {

    private Long itemId;
    private Integer seasonFoodMonth;

    public SeasonFoodItemId() {
    }

    public SeasonFoodItemId(Long itemId, Integer seasonFoodMonth) {
        this.itemId = itemId;
        this.seasonFoodMonth = seasonFoodMonth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeasonFoodItemId that)) {
            return false;
        }
        return Objects.equals(itemId, that.itemId) && Objects.equals(seasonFoodMonth, that.seasonFoodMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, seasonFoodMonth);
    }
}
