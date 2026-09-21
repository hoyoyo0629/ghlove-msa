package com.ghlove.gift.domain;

import java.io.Serializable;
import java.util.Objects;

/** SeasonFood의 복합키 (SEASON_FOOD_MONTH, REG_SEQ). */
public class SeasonFoodId implements Serializable {

    private Integer seasonFoodMonth;
    private Integer regSeq;

    public SeasonFoodId() {
    }

    public SeasonFoodId(Integer seasonFoodMonth, Integer regSeq) {
        this.seasonFoodMonth = seasonFoodMonth;
        this.regSeq = regSeq;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SeasonFoodId that)) {
            return false;
        }
        return Objects.equals(seasonFoodMonth, that.seasonFoodMonth) && Objects.equals(regSeq, that.regSeq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(seasonFoodMonth, regSeq);
    }
}
