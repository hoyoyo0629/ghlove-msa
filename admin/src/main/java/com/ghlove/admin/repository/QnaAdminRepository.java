package com.ghlove.admin.repository;

import com.ghlove.admin.domain.QnaAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QnaAdminRepository extends JpaRepository<QnaAdmin, Long> {

    /** 사용중(삭제 아님) 문의를 최신순으로. 지자체/유형/기간 필터는 서비스에서 인메모리 처리
     *  (FaqService와 동일한 소규모 관례). */
    List<QnaAdmin> findByDataStatusCodeOrderByCreatedDateDesc(String dataStatusCode);
}
