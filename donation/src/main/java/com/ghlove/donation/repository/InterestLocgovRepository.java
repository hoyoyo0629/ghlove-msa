package com.ghlove.donation.repository;

import com.ghlove.donation.domain.InterestLocgov;
import com.ghlove.donation.domain.InterestLocgovId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterestLocgovRepository extends JpaRepository<InterestLocgov, InterestLocgovId> {

    List<InterestLocgov> findByUserId(Long userId);

    void deleteByLocgovCodeAndUserId(String locgovCode, Long userId);

    void deleteByLocgovCodeInAndUserId(List<String> locgovCodes, Long userId);

    boolean existsByLocgovCodeAndUserId(String locgovCode, Long userId);
}
