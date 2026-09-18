package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    List<Policy> findAllByOrderByPolicyTypeAscPolicyIdDesc();

    /** AS-IS policy-mapper.xml getCurrentPolicyByType와 같은 규칙 - 해당 타입에서 전시중('Y')인
     *  것 중 가장 최근에 등록된 1건(전시 시작/종료일은 AS-IS도 조건에 쓰지 않는다). */
    Optional<Policy> findFirstByPolicyTypeAndExhibitionStatusOrderByCreatedDateDesc(
            String policyType, String exhibitionStatus);
}
