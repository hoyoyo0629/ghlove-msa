package com.ghlove.point.readmodel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RmLocgovPointRepository extends JpaRepository<RmLocgovPoint, RmLocgovPointId> {

    List<RmLocgovPoint> findByUserIdOrderByStdrYearDescLocgovCodeAsc(Long userId);

    void deleteByUserId(Long userId);
}
