package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpenApiUsageStat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpenApiUsageStatRepository extends JpaRepository<OpenApiUsageStat, Long> {

    List<OpenApiUsageStat> findTop500ByOrderByUsageIdDesc();

    long countByConsumerUsername(String consumerUsername);
}
