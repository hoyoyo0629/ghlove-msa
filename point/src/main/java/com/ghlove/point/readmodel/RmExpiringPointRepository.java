package com.ghlove.point.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RmExpiringPointRepository extends JpaRepository<RmExpiringPoint, RmExpiringPointId> {

    List<RmExpiringPoint> findByUserIdAndExpirationDateLessThanEqualOrderByExpirationDateAsc(Long userId, String until);

    void deleteByUserId(Long userId);
}
