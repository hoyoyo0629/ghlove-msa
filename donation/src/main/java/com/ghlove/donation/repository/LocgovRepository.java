package com.ghlove.donation.repository;

import com.ghlove.donation.domain.Locgov;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/** admin 지자체관리 목록 검색은 {@link JpaSpecificationExecutor}로 처리한다(모든 조건이
 *  선택적이라 정적 JPQL의 "LIKE CONCAT('%', :param, '%')" 패턴을 썼더니 파라미터가 전부
 *  null인 무조건 검색에서 Postgres가 그 CONCAT 결과의 타입을 추론 못 해
 *  "character varying ~~ bytea" 오류가 났다 - Specification으로 null인 조건은 predicate
 *  자체를 안 만들면 이 문제가 생기지 않는다). */
public interface LocgovRepository extends JpaRepository<Locgov, String>, JpaSpecificationExecutor<Locgov> {
    List<Locgov> findByUseAtOrderByLocgovNm(String useAt);
}
