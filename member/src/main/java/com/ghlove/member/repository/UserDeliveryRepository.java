package com.ghlove.member.repository;

import com.ghlove.member.domain.UserDelivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDeliveryRepository extends JpaRepository<UserDelivery, Long> {
    List<UserDelivery> findByUserIdOrderByDefaultFlagDescCreatedDateDesc(Long userId);

    Optional<UserDelivery> findByUserDeliveryIdAndUserId(Long userDeliveryId, Long userId);

    int countByUserId(Long userId);
}
