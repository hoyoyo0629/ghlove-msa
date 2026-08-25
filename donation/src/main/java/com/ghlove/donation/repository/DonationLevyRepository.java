package com.ghlove.donation.repository;

import com.ghlove.donation.domain.DonationLevy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonationLevyRepository extends JpaRepository<DonationLevy, String> {
}
