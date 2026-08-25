package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpEmail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpEmailRepository extends JpaRepository<OpEmail, Long> {

    List<OpEmail> findAllByOrderByEmailIdDesc();
}
