package com.ghlove.member.repository;

import com.ghlove.member.domain.UserLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLevelRepository extends JpaRepository<UserLevel, Integer> {

    List<UserLevel> findByGroupCodeOrderByDepthAsc(String groupCode);
}
