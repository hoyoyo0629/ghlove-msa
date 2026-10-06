package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PolicyRepository extends JpaRepository<Policy, Integer> {

    List<Policy> findAllByOrderByPolicyTypeAscPolicyIdDesc();

    /**
     * AS-IS getPolicyListByParam + sqlSearchWhere(policy-mapper.xml) 그대로.
     * 검색구분에 따라 TITLE 또는 CONTENT LIKE, 정책구분·전시여부는 값이 있을 때만 AND,
     * 정렬은 {@code ORDER BY CREATED_DATE DESC}.
     */
    /* cast(... as String)는 필수다 - 이유는 PopupRepository.search 주석 참고. */
    @Query("""
            select p from Policy p
             where (cast(:titleQuery as String) is null
                     or p.title like concat('%', cast(:titleQuery as String), '%'))
               and (cast(:contentQuery as String) is null
                     or p.content like concat('%', cast(:contentQuery as String), '%'))
               and (:policyType is null or p.policyType = :policyType)
               and (:exhibitionStatus is null or p.exhibitionStatus = :exhibitionStatus)
             order by p.createdDate desc nulls last, p.policyId desc
            """)
    List<Policy> search(@Param("titleQuery") String titleQuery,
                        @Param("contentQuery") String contentQuery,
                        @Param("policyType") String policyType,
                        @Param("exhibitionStatus") String exhibitionStatus);

    /** AS-IS policy-mapper.xml getCurrentPolicyByType와 같은 규칙 - 해당 타입에서 전시중('Y')인
     *  것 중 가장 최근에 등록된 1건(전시 시작/종료일은 AS-IS도 조건에 쓰지 않는다). */
    Optional<Policy> findFirstByPolicyTypeAndExhibitionStatusOrderByCreatedDateDesc(
            String policyType, String exhibitionStatus);
}
