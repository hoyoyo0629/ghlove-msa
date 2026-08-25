package com.ghlove.member.repository;

import com.ghlove.member.domain.UserRoleRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRequestRepository extends JpaRepository<UserRoleRequest, Long> {
    List<UserRoleRequest> findByStatusOrderByRequestIdDesc(String status);

    List<UserRoleRequest> findByUserIdOrderByRequestIdDesc(Long userId);

    boolean existsByUserIdAndRequestedRoleAndStatus(Long userId, String requestedRole, String status);
}
