package com.ghlove.order.repository;

import com.ghlove.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedDateDesc(Long userId);

    List<Order> findByOrderIdIn(List<String> orderIds);

    List<Order> findByPointOutcomeOrderByCreatedDateDesc(String pointOutcome);
}
