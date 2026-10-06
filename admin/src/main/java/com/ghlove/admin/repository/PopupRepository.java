package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Popup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PopupRepository extends JpaRepository<Popup, Integer> {
    List<Popup> findAllByOrderByPopupIdDesc();

    List<Popup> findByUseYnOrderByPopupIdDesc(String useYn);

    /** AS-IS displayPopupList - 노출 기준은 POPUP_CLOSE='1'(사용)이다(USE_YN이 아니다). */
    List<Popup> findByPopupCloseOrderByPopupIdDesc(String popupClose);

    /**
     * AS-IS popup-mapper.xml의 {@code sqlPopupWhere} + {@code popupList}를 그대로 옮긴 검색.
     * 팝업상태/팝업형태는 '0'(전체)이면 조건에서 빠지고, 사용기간은 START_DATE >= / END_DATE <=,
     * 제목은 LIKE '%..%'다. 정렬은 POPUP_ID DESC.
     */
    /* ★ LIKE 검색어처럼 <b>concat 안에만</b> 쓰이는 파라미터는 반드시 {@code cast(... as String)}로
     *   타입을 준다. 그냥 {@code :query is null}로 두면 Hibernate가 타입을 못 정해 null을 bytea로
     *   바인딩하고, PostgreSQL이 "operator does not exist: character varying ~~ bytea"로 거절한다
     *   (2026-10-06 아침 팝업관리 목록 500의 원인).
     *   {@code = :x}·{@code >= :x}처럼 비교 대상이 있는 조건은 타입이 잡히므로 cast가 필요 없다. */
    @Query("""
            select p from Popup p
             where (:popupClose is null or p.popupClose = :popupClose)
               and (:popupStyle is null or p.popupStyle = :popupStyle)
               and (:startDate is null or p.startDate >= :startDate)
               and (:endDate is null or p.endDate <= :endDate)
               and (cast(:query as String) is null or p.subject like concat('%', cast(:query as String), '%'))
             order by p.popupId desc
            """)
    List<Popup> search(@Param("popupClose") String popupClose,
                       @Param("popupStyle") String popupStyle,
                       @Param("startDate") String startDate,
                       @Param("endDate") String endDate,
                       @Param("query") String query);
}
