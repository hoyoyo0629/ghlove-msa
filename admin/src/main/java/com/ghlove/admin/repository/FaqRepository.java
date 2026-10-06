package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * FAQ ({@code OP_FAQ}). AS-IS는 QueryDSL {@code Predicate}로 조건을 조립하는데
 * ({@code FaqDto.getPredicate()}) TO-BE admin에는 QueryDSL이 없어 조건을 서비스에서 건다 -
 * FAQ는 운영 규모가 수십~수백 건이라 전체를 읽어 걸러도 충분하다(이 프로젝트의 기존 관례).
 */
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByUseYn(String useYn);
}
