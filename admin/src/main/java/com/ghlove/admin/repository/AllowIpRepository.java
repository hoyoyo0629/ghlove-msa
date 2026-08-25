package com.ghlove.admin.repository;

import com.ghlove.admin.domain.AllowIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowIpRepository extends JpaRepository<AllowIp, Integer> {
    List<AllowIp> findByDisplayFlagOrderByAllowIpIdDesc(String displayFlag);
}
