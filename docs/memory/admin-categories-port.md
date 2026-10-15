---
name: admin-categories-port
description: 답례품관리>카테고리관리(16101) AS-IS 이식 - OP_CATEGORY 1~4차 트리(jsTree 드래그 이동)를 gift.public.op_category에 올림, 메뉴 URL을 /categories로 교체, 알려진 한계(GNB는 gift_subcategory를 읽음)
metadata:
  type: project
---

2026-10-15 이식. AS-IS `opmanager/categories/list`는 **OP_CATEGORY(1~4차, 코드 12자리 `CCC000000000` 구조)** 를 jsTree로 보여주고
드래그앤드롭으로 이동한다. TO-BE 메뉴 16101은 원래 `/admin/gift-categories`(GIFT_SUBCATEGORY를 고치는 자체 화면)를 가리켰다 - 다른 데이터라
화면을 새로 이식하고 메뉴 URL을 `/categories`로 바꿨다(옛 화면은 삭제하지 않고 메뉴 연결만 끊음).

**구성**: gift `CategoriesAdminService`(네이티브 SQL, `public.op_category`)·`CategoriesAdminApiController`(`/api/admin/categories`) ↔ admin
`CategoriesAdminClient`·`CategoriesManagerController`(`/categories/**`)·템플릿 `categories/list|create|edit.html`(create/edit은 ajax 조각).
메뉴 prefix 매칭(`menuUrl + "/"`) 때문에 menu_url을 `/categories`로 둬야 하위 경로 전부가 16101로 권한·브레드크럼 매칭된다.
`database/ddl/migration-admin-menu-16101-categories.sql`(적용 필요), `migration-gift-category-asis-load.sql`(데이터: 팀1·그룹6·카테고리91행, 개발DB 적용 완료).

**AS-IS 특징(그대로)**
- 등록·수정·삭제는 JS가 `adminRole != 'SYS'`면 alert로 막는다. 그런데 16101 메뉴 권한이 ROLE_ADMIN_1~4뿐이라 지자체(5,6) 등은 아예 `/admin/access-denied` - 권한 alert은 사실상 도달 불가(verbatim 유지).
- 1차 트리는 `ORDER BY CATEGORY_NAME`인데 **CUBRID는 코드포인트 순, PG en_US는 한글을 그렇게 안 정렬** → `collate "C"`로 맞춤(AS-IS 실DB 조회로 확인).
- SEO 영역(#seo)은 AS-IS도 항상 `display:none`이라 화면에선 못 치고 값 채우기/저장만 동작(Playwright에선 evaluate로 값 주입해 검증).
- 수정폼의 "하위 카테고리 등록" 블록 조건이 `level != '2'`라 4차에도 뜨는 AS-IS 이상 동작 그대로(3차 위 추가하면 SQL 에러 - AS-IS도 동일).
- 미리보기 링크는 AS-IS 운영 설정(`view.type=api`)대로 `{frontend}/goods/searchGoods.html?category=…&type=C` 한 개만(프론트 주소는 `ghlove.storefront.url`, 기본 localhost:5173 - 설문 미리보기와 같은 관례). TO-BE storefront엔 그 경로가 없다.
- 안 옮긴 것: AS-IS `/change`, `/change-ordering-category1`(화면이 안 부르는 죽은 엔드포인트), 필터 UI(edit.jsp 마크업에 원래 없음), `updateCategoryUpdatedDate`(쇼핑몰 캐시 갱신 - TO-BE 대응 없음), 시퀀스 서비스(MAX+1 사용).

**함정**: OP_ITEM_FILTER엔 `category_id` 칸이 없다 - AS-IS는 `ITEM_ID IN (SELECT ITEM_ID FROM OP_ITEM_CATEGORY WHERE CATEGORY_ID IN …)`로 지운다(처음 `category_id`로 짜서 등록이 500).
수정 시 필터 초기화는 상위(브레드크럼) 카테고리가 쓰는 필터 그룹은 남긴다(`resetFiltersKeepingAncestors`).
**spare 기동은 각 모듈 디렉터리에서**(`cd gift && java -jar build/libs/…`) - 루트에서 띄우면 `config/application.yml`(개발DB 10.0.110.37)을 못 읽어 localhost:15432로 가서 실패.

**알려진 한계(사용자에게 알림)**: 스토어프론트 GNB 메가메뉴는 여전히 `gift_subcategory`를 읽는다 → 이 화면의 편집이 GNB에 반영되지 않는다(데이터 모델 통합은 별도 결정).
`op_category_team` 1001·`op_category_group` 1000(이전 테스트 시드)이 그룹 select에 "여름 특가 기획전/농산물 특가"로 남아 있다(지우지 않음).

검증: Playwright로 트리 로드(전체 51)·그룹 재조회·노드→수정폼+SEO·검증 alert 7종·등록/수정/하위등록/이동(jsTree move_node, 취소 포함)/삭제(하위·1차 경고문구) 후 DB 91행 복원 확인.
admin·gift 재기동 필요. 관련: [[verify-menu-location-before-building]], [[copy-as-is-verbatim-never-invent]].
