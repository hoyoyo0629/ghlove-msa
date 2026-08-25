package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.domain.ManagerRequestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ManagerRequestRepository extends JpaRepository<ManagerRequest, ManagerRequestId> {

    List<ManagerRequest> findByConfmSttusCodeOrderByFrstRegistPnttmDesc(String confmSttusCode);

    List<ManagerRequest> findByUserIdOrderByReqstSnDesc(Long userId);

    Optional<ManagerRequest> findFirstByUserIdOrderByReqstSnDesc(Long userId);

    int countByUserId(Long userId);
}
