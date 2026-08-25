package com.ghlove.donation.repository;

import com.ghlove.donation.domain.CtbnyOpratnFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CtbnyOpratnFileRepository extends JpaRepository<CtbnyOpratnFile, Long> {

    List<CtbnyOpratnFile> findByRegistSnOrderBySortOrdrAsc(Long registSn);
}
