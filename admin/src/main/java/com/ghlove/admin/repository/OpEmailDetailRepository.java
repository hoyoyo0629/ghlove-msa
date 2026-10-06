package com.ghlove.admin.repository;

import com.ghlove.admin.domain.OpEmailDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** AS-IS emailMapper.getEmailAuthList / insertEmailDetail 대응. */
public interface OpEmailDetailRepository extends JpaRepository<OpEmailDetail, OpEmailDetail.Key> {

    List<OpEmailDetail> findByEmailId(Long emailId);
}
