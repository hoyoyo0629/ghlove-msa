package com.ghlove.admin.repository;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.CommonCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * ★AS-IS ORDERING은 CUBRID 기본 ASC 정렬에서 NULL이 가장 작은 값으로 취급돼 맨 앞에 온다
 * (MySQL 계열과 동일). PostgreSQL ASC 기본값은 반대로 NULL이 맨 뒤다(NULLS LAST) - TEL,
 * ORDER_PAY_TYPE, CANCEL_REASON 등 ORDERING이 비어 있는 코드가 실제로 많아(AS-IS 원본
 * 데이터 그대로임, TO-BE가 지어낸 값이 아니다) 단순 {@code ORDER BY ordering}만 쓰면 그
 * 행들이 AS-IS와 반대쪽 끝에 몰린다. ORDERING으로 정렬하는 쿼리는 전부 {@code NULLS FIRST}를
 * 명시해야 CUBRID와 같은 순서가 나온다(2026-10-07).
 */
public interface CommonCodeRepository extends JpaRepository<CommonCode, CommonCodeId> {

    /**
     * ★2026-10-14: {@code ordering} 뒤에 {@code id} 2차 정렬을 추가했다.
     *
     * <p>ORDER_PAY_TYPE·CANCEL_REASON 처럼 ordering이 전부 NULL(= 전부 동률)인 코드구분이
     * 많은데, 2차 정렬 키가 없으면 <b>동률 사이의 순서를 SQL이 보장하지 않는다</b>
     * (PostgreSQL이 그 순간의 실행계획대로 아무 순서나 돌려줘도 표준 위반이 아니다).
     * 결제타입별 매출(sales/payment) 화면에서 체크박스(GET 요청)와 표 헤더(POST 요청)가
     * 서로 다른 SQL 실행이라 각기 다른 순서로 나올 수 있었다 - 사용자가 실제로 겪은 증상.
     * id 를 2차 키로 고정해 매 호출이 항상 같은 순서를 내도록 한다.
     */
    @Query("select c from CommonCode c where c.codeType = :codeType order by c.ordering nulls first, c.id")
    List<CommonCode> findByCodeTypeOrderByOrdering(@Param("codeType") String codeType);

    /** 위와 같은 이유로 id 2차 정렬 추가 - 공통코드 관리 화면이 새로고침마다 동률 구간
     *  순서가 바뀌어 보이는 것을 막는다. */
    @Query("select c from CommonCode c order by c.codeType, c.ordering nulls first, c.id")
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
     * 검색구분(where)에 따라 ID 또는 LABEL LIKE. 정렬은 ORDER BY ORDERING(NULLS FIRST).
     */
    @Query("""
            select c from CommonCode c
             where c.language = :language
               and (:codeType is null or c.codeType = :codeType)
               and (cast(:idQuery as String) is null
                     or c.id like concat('%', cast(:idQuery as String), '%'))
               and (cast(:labelQuery as String) is null
                     or c.label like concat('%', cast(:labelQuery as String), '%'))
             order by c.ordering nulls first
            """)
    List<CommonCode> search(@Param("language") String language,
                            @Param("codeType") String codeType,
                            @Param("idQuery") String idQuery,
                            @Param("labelQuery") String labelQuery);
}
