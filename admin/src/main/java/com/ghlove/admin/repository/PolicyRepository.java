package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    List<Policy> findAllByOrderByPolicyTypeAscPolicyIdDesc();
}
