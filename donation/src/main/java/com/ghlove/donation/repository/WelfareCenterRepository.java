package com.ghlove.donation.repository;

import com.ghlove.donation.domain.WelfareCenter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WelfareCenterRepository extends JpaRepository<WelfareCenter, Long> {
    List<WelfareCenter> findAllByOrderByPbadmsWlfrCntrIdDesc();

    List<WelfareCenter> findByLclgvCdOrderByPbadmsWlfrCntrIdDesc(String lclgvCd);
}
