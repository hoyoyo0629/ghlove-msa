package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, String> {

    List<Role> findAllByOrderByRoleSeq();
}
