package com.ghlove.admin.repository;

import com.ghlove.admin.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Integer> {

    List<Menu> findByDisplayFlagOrderByMenuSeq(String displayFlag);

    /** AS-IS menu-mapper.xml의 모든 메뉴 조회가 거는 조건(STATUS_CODE='1')과 동일 - 사용중인 메뉴. */
    List<Menu> findByStatusCodeOrderByMenuSeq(String statusCode);

    List<Menu> findAllByOrderByMenuParentIdAscMenuSeqAsc();

    List<Menu> findByMenuParentId(Integer menuParentId);

    Integer countByMenuParentId(Integer menuParentId);

    /**
     * AS-IS {@code MenuMapper.getAllMenuList} (framework menu-mapper.xml) verbatim 이식 -
     * 1404 사용자 권한 관리 화면 오른쪽의 메뉴권한 체크박스 트리가 쓰는 유일한 조회다.
     *
     * <p>AS-IS가 세 단계 전부 <b>INNER JOIN + {@code STATUS_CODE='1'} + {@code MENU_TYPE} 고정</b>
     * 으로 거는 것이 핵심이다:
     * <ul>
     *   <li><b>미사용(STATUS_CODE='2') 메뉴는 1·2·3단 어디에 있든 화면에 아예 나오지 않는다.</b>
     *       상위가 미사용이면 그 아래 전부 사라진다.</li>
     *   <li>INNER JOIN이라 <b>자식이 없는 1단·2단은 묶음 자체가 나오지 않는다</b>
     *       (빈 섹션을 그리지 않는다).</li>
     *   <li>{@code MENU_TYPE}을 1/2/3으로 못 박아 root 행(menu_id=0)이나 타입이 빈 행은 걸러진다.</li>
     * </ul>
     * 정렬도 AS-IS와 같이 단계별 {@code MENU_SEQ} 오름차순이다.
     *
     * <p>AS-IS는 이 평면 행들을 MyBatis {@code AllMenuListResult} 중첩 resultMap으로 3단 트리로
     * 묶는다 - TO-BE는 {@code RoleAdminService.matrixFor}가 같은 일을 한다.
     */
    @Query(value = """
            select m1.menu_id   as l1_id, m1.menu_name as l1_name,
                   m2.menu_id   as l2_id, m2.menu_name as l2_name,
                   m3.menu_id   as l3_id, m3.menu_name as l3_name
              from admin.op_menu m1
              join admin.op_menu m2
                on m1.menu_id = m2.menu_parent_id
               and m1.menu_type = '1' and m1.status_code = '1'
               and m2.menu_type = '2' and m2.status_code = '1'
              join admin.op_menu m3
                on m2.menu_id = m3.menu_parent_id
               and m3.menu_type = '3' and m3.status_code = '1'
             order by m1.menu_seq asc, m2.menu_seq asc, m3.menu_seq asc
            """, nativeQuery = true)
    List<Object[]> findAllMenuListForRightMatrix();
}
