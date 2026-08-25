package com.ghlove.member.repository;

import com.ghlove.member.domain.UserRole;
import com.ghlove.member.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    List<UserRole> findByUserId(Long userId);
}
