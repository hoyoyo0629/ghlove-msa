package com.ghlove.member.repository;

import com.ghlove.member.domain.UserSns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSnsRepository extends JpaRepository<UserSns, Long> {
    Optional<UserSns> findBySnsTypeAndSnsId(String snsType, String snsId);
}
