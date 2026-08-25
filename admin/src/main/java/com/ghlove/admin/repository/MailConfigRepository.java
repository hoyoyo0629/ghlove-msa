package com.ghlove.admin.repository;

import com.ghlove.admin.domain.MailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MailConfigRepository extends JpaRepository<MailConfig, Integer> {

    Optional<MailConfig> findByTemplateId(String templateId);
}
