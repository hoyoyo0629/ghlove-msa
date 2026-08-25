package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ConfigIsms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConfigIsmsRepository extends JpaRepository<ConfigIsms, String> {

    List<ConfigIsms> findAllByOrderByOrdering();
}
