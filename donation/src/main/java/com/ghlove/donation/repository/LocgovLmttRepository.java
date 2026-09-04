package com.ghlove.donation.repository;

import com.ghlove.donation.domain.LocgovLmtt;
import com.ghlove.donation.domain.LocgovLmttId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LocgovLmttRepository extends JpaRepository<LocgovLmtt, LocgovLmttId> {
    List<LocgovLmtt> findByLocgovCodeOrderByLmttBgnDeDesc(String locgovCode);
}
