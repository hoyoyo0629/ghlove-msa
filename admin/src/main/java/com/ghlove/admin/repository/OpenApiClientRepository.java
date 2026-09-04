package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpenApiClient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OpenApiClientRepository extends JpaRepository<OpenApiClient, Long> {

    List<OpenApiClient> findAllByOrderByClientIdDesc();

    List<OpenApiClient> findAllByStatusCode(String statusCode);
}
