package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerLoginEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ManagerLoginEmailRepository extends JpaRepository<ManagerLoginEmail, Long> {

    Optional<ManagerLoginEmail> findFirstByLoginIdOrderByCreatedAtDesc(String loginId);
}
