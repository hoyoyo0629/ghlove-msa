package com.ghlove.point.readmodel;

import java.io.Serializable;
import java.util.Objects;

/** {@link RmLocgovPoint} 복합키 (회원 × 지자체 × 기부연도). */
public class RmLocgovPointId implements Serializable {

    private Long userId;
    private String locgovCode;
    private String stdrYear;

    public RmLocgovPointId() {
    }

    public RmLocgovPointId(Long userId, String locgovCode, String stdrYear) {
        this.userId = userId;
        this.locgovCode = locgovCode;
        this.stdrYear = stdrYear;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RmLocgovPointId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(locgovCode, that.locgovCode)
                && Objects.equals(stdrYear, that.stdrYear);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, locgovCode, stdrYear);
    }
}
