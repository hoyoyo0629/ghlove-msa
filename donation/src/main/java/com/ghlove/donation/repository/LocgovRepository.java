package com.ghlove.donation.repository;

import com.ghlove.donation.domain.Locgov;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocgovRepository extends JpaRepository<Locgov, String> {
    List<Locgov> findByUseAtOrderByLocgovNm(String useAt);
}
