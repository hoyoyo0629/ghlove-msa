package com.ghlove.admin.repository;

import com.ghlove.admin.domain.ManagerRequest;
import com.ghlove.admin.domain.ManagerRequestId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ManagerRequestRepository extends JpaRepository<ManagerRequest, ManagerRequestId> {

    List<ManagerRequest> findByConfmSttusCodeOrderByFrstRegistPnttmDesc(String confmSttusCode);

    List<ManagerRequest> findByUserIdOrderByReqstSnDesc(Long userId);

    Optional<ManagerRequest> findFirstByUserIdOrderByReqstSnDesc(Long userId);

    int countByUserId(Long userId);

    /**
     * AS-IS 관리자 권한 승인관리 목록 - 상태(빈값=전체)와 등록일 범위(FRST_REGIST_PNTTM 앞 8자리)로
     * 걸러 최신순. 아이디/이름/이메일 검색은 이름·이메일이 member 서비스에 있어 서비스단에서 거른다.
     */
    @Query("""
            select r from ManagerRequest r
             where (:confmSttusCode is null or r.confmSttusCode = :confmSttusCode)
               and (:startDate is null or substring(r.frstRegistPnttm, 1, 8) >= :startDate)
               and (:endDate is null or substring(r.frstRegistPnttm, 1, 8) <= :endDate)
             order by r.frstRegistPnttm desc, r.reqstSn desc
            """)
    List<ManagerRequest> search(@Param("confmSttusCode") String confmSttusCode,
                                @Param("startDate") String startDate,
                                @Param("endDate") String endDate);
}
