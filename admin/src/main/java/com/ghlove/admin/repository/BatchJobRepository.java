package com.ghlove.admin.repository;

import com.ghlove.admin.domain.BatchJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchJobRepository extends JpaRepository<BatchJob, Integer> {

    List<BatchJob> findAllByOrderByOrderingAscBatchJobIdAsc();
}
