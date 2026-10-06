package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, String> {

    List<Role> findAllByOrderByRoleSeq();

    /**
     * AS-IS getUserGroupList(usergroup-mapper.xml)의 대상 집합 + 정렬.
     * {@code sqlRoleWhere}의 conditionType='ROLE_ADMIN' 분기와 동일하게
     * ROLE_ADMIN*·ROLE_MALL_SUPERVISOR·ROLE_MD·ROLE_ISMS·ROLE_EXCEL만 추리고
     * {@code ORDER BY opr.CREATED_DATE DESC}를 따른다. 개발DB는 CREATED_DATE가 비어 있어
     * 그것만으로는 순서가 뒤섞이므로 동순위는 ROLE_SEQ로 묶어 안정 정렬한다.
     */
    @Query("""
            select r from Role r
             where r.authority like 'ROLE_ADMIN%'
                or r.authority = 'ROLE_MALL_SUPERVISOR'
                or r.authority like 'ROLE_MD%'
                or r.authority like 'ROLE_ISMS%'
                or r.authority like 'ROLE_EXCEL%'
             order by r.createdDate desc nulls last, r.roleSeq
            """)
    List<Role> findAdminRoles();
}
