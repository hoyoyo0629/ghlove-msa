package com.ghlove.admin.repository;

import com.ghlove.admin.domain.DonationLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationLedgerRepository extends JpaRepository<DonationLedger, String> {
    List<DonationLedger> findByStatus(String status);
}
