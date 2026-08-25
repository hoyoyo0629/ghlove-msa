package com.ghlove.point.repository;

import com.ghlove.point.domain.PointReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointReservationRepository extends JpaRepository<PointReservation, Long> {
    List<PointReservation> findByUserIdOrderByCreatedDateDesc(Long userId);

    @Query("select coalesce(sum(r.amount), 0) from PointReservation r where r.userId = :userId and r.status = 'RESERVED'")
    long sumReservedByUserId(@Param("userId") Long userId);
}
