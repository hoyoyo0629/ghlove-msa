---
name: admin-menu-admin-seller-data-and-pagination
description: "메뉴관리(1409) 답례품관리자 트리 데이터 적재 + 페이징 제거(AS-IS는 페이저 없음, 2026-10-07)"
metadata:
  node_type: memory
  type: project
---

**네이티브쿼리 Character 캐스팅 버그**: `MenuTreeRepository.tree()`가 재귀CTE 결과의 1글자
varchar 컬럼(`display_flag`/`status_code`)을 `(String)`으로 바로 캐스팅해서 500 에러
(`ClassCastException: Character cannot be cast to String`, admin.log로 실제 확인). Hibernate가
단일문자 varchar를 Character로 돌려줄 수 있어서다 - `BatchLogRepository`/`QestnarRepository`와
같은 `asString()`(`value == null ? null : value.toString()`) 헬퍼로 교체해서 해결.

**답례품관리자(SELLER) 트리 빈 화면**: `admin.op_menu_seller`가 완전히 0건(root 행도 없음)이라
재귀CTE 시작점이 없어 항상 빈 목록이었다. AS-IS export
(`Desktop\고향사랑e음\1.AS-IS\1. DB\_op_menu_seller__202610071247.sql`)로 52행(root 포함)
그대로 적재(`database/ddl/migration-admin-menu-seller-asis-data.sql`, 적용됨).

**페이징 제거**: 이전 TO-BE는 AS-IS의 결함(LIMIT 10인데 JSP에 페이저 태그가 없어 11번째부터
안 보임)을 "고치려고" 페이저를 추가해뒀었다. 사용자가 실제 화면을 보고 AS-IS처럼 페이징 없이
전체를 보여주는 쪵으로 되돌리라고 지시 - `MenuAdminController.list()`에서 `Pagination`/`page`
파라미터 제거, 템플릿에서 `pagination-wrap` 제거. LIMIT 10 자체를 재현하는 게 아니라 "페이징을
안 한다"(전체 표시)로 해석했다 - AS-IS의 10건 캡은 JSP 결함이지 의도가 아니었다는 컨트롤러
주석이 이미 있었고, 사용자 지시도 "페이징 처리 안 하게"였기 때문.

**상태**: admin compileJava+test+bootJar 통과, DB는 바로 적용됨. **재기동 필요: admin.**
