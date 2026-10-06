package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {
    List<CommonCode> findByCodeTypeOrderByOrdering(String codeType);

    List<CommonCode> findAllByOrderByCodeTypeAscOrderingAsc();

    /**
     * AS-IS getCodeTypeList(code-mapper.xml) - 왼쪽 코드구분 패널.
     * {@code WHERE LANGUAGE='ko' AND USE_YN='Y' GROUP BY CODE_TYPE ORDER BY 1}과 동일하게
     * 사용중인 코드만 세고 코드구분명 오름차순으로 준다.
     */
    @Query("""
            select c.codeType, count(c.codeType)
              from CommonCode c
             where c.language = :language and c.useYn = 'Y'
             group by c.codeType
             order by c.codeType
            """)
    List<Object[]> codeTypeCounts(@Param("language") String language);

    /**
     * AS-IS sqlCodeWhere + getCodeList - LANGUAGE 고정, 코드구분 선택 시 그 구분만,
     * 검색구분(where)에 따라 ID 또는 LABEL LIKE. 정렬은 ORDER BY ORDERING.
     */
    @Query("""
            select c from CommonCode c
             where c.language = :language
               and (:codeType is null or c.codeType = :codeType)
               and (cast(:idQuery as String) is null
                     or c.id like concat('%', cast(:idQuery as String), '%'))
               and (cast(:labelQuery as String) is null
                     or c.label like concat('%', cast(:labelQuery as String), '%'))
             order by c.ordering
            """)
    List<CommonCode> search(@Param("language") String language,
                            @Param("codeType") String codeType,
                            @Param("idQuery") String idQuery,
                            @Param("labelQuery") String labelQuery);
}
