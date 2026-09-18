package com.ghlove.point.readmodel;

import java.io.Serializable;
import java.util.Objects;

/** {@link RmExpiringPoint} 복합키 (회원 × 만료일 × 지자체). */
public class RmExpiringPointId implements Serializable {

    private Long userId;
    private String expirationDate;
    private String locgovCode;

    public RmExpiringPointId() {
    }

    public RmExpiringPointId(Long userId, String expirationDate, String locgovCode) {
        this.userId = userId;
        this.expirationDate = expirationDate;
        this.locgovCode = locgovCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RmExpiringPointId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId)
                && Objects.equals(expirationDate, that.expirationDate)
                && Objects.equals(locgovCode, that.locgovCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, expirationDate, locgovCode);
    }
}
