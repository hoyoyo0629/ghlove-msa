---
name: admin-categories-team-group-port
description: 답례품관리>카테고리 그룹 관리(16102) AS-IS 이식 - 대그룹+1차 카테고리 그룹 목록/등록/수정/삭제/정렬순서변경, 메뉴 URL→/categories-team-group, 안 옮긴 AS-IS 도달불가 기능, 알려진 한계
metadata:
  type: project
---

2026-10-15 이식. AS-IS `opmanager/categories-team-group/list`(메뉴 16102, AS-IS 실DB status 1 활성 확인). TO-BE 메뉴 16102는 원래
`/admin/category-teams`(대그룹/그룹/**배너/추천상품** 자체 화면 - AS-IS 목록에 없는 발명)를 가리켰다 → AS-IS 화면을 새로 이식하고 메뉴 URL을
`/categories-team-group`으로 교체(옛 화면은 삭제 안 하고 연결만 끊음, [[admin-categories-port]]와 같은 처리).

**구성**: gift `CategoriesTeamGroupAdminService`(네이티브 SQL, op_category_team/group)·`CategoriesTeamGroupAdminApiController`
(`/api/admin/categories-team-group`) ↔ admin `CategoriesTeamGroupAdminClient`·`CategoriesTeamGroupManagerController`(`/categories-team-group/**`)·
템플릿 `categories-team-group/list|group-form.html`. `database/ddl/migration-admin-menu-16102-categories-team-group.sql`(적용 필요).
코드 중복확인은 카테고리관리의 `/categories/code-check`를 그대로 쓴다(AS-IS도 OP_CATEGORY 기준).

**AS-IS 특징(그대로)**
- 상단 구분 select(전체/대그룹)는 display:none 폼 안 → 화면에선 안 보이고 `?categoryType=team|group`으로만 바뀜. 기본(전체)은 대그룹 행 + 하위 그룹 행(tbody별 jQuery UI sortable).
- 정렬순서변경은 **tbody마다 1부터** 번호를 다시 매겨 전부 저장 - 손대지 않은 대그룹의 그룹 순서도 같이 덮어쓴다(검증 중 시드 그룹 0→1로 바뀜, 원복함).
- 대그룹 수정 버튼은 AS-IS가 주석처리 → 대그룹은 보기만. 접속권한 "성인만" 라디오는 hidden 클래스.
- **수정은 SEO 칼럼을 전부 NULL로 덮는다**(폼에 없는 값, DESCRIPTION만 주석처리라 보존) + 숨은 영역(`.email_tb` display:none)의 `categoryCodes`가 같이 전송돼
  REMOVE(목록에 없는 소속 카테고리의 그룹을 0으로; 목록 없으면 전부) → SAVE(목록 카테고리를 이 그룹으로). 공개 1차 카테고리만 목록에 실리므로 **비공개 1차 카테고리는 수정할 때마다 그룹이 0이 된다**(AS-IS 결함 그대로).
- 삭제는 그룹 행만 지운다(소속 카테고리의 그룹 ID는 그대로 남음).
- 등록·수정·순서변경은 JS가 adminRole != SYS면 alert로 막는다. 16102 메뉴 권한이 ROLE_ADMIN_1~4뿐이라 지자체 등은 `/admin/access-denied`로 막혀 alert 도달 불가.

**안 옮긴 것(AS-IS도 화면에서 닿는 길이 없음)**: 대그룹 등록/수정/삭제 화면(`/team/**`, 상품 검색 팝업은 SalesOn 종속), "1차 카테고리로 변경" 팝업(그룹 폼 버튼 주석처리),
`ordering-edit/{type}`(대응 입력칸 없는 죽은 핸들러), GET 불가한 `/group/delete/{id}`, 캐시 갱신, 시퀀스(MAX+1).

**알려진 것**: ①`/ban-word/list-all` 404(`_filter` 클래스 폼 공통, [[playwright-smarteditor-submit-quirk]]에도 기록된 전 화면 공통 갭 - AS-IS는 ShopConfig.banWord 콤마목록 반환, 미구현) ②sortable 자리표시 배경이미지 404는 AS-IS도 경로가 틀려 같음 ③
시드 대그룹 1001·그룹 1000(이전 테스트 데이터)이 목록에 "여름 특가 기획전/농산물 특가"로 남아 있고, 그룹 신규 ID가 MAX+1이라 1001부터 시작.
**테스트 계정 로그인이 바뀜**: 새 로그인은 `#op_username/#op_password` + `idLogin()` → devCode 자동 입력(기존 `#loginId` 스크립트는 hidden이라 실패).

검증: Playwright - 전체/team/group 3모드 목록, 등록(검증 alert 4종·코드중복·확인창·flash 안내), 수정(코드 readonly), 마우스 드래그 정렬+저장, 숨은 영역 추가/삭제 JS, REMOVE 효과(DB), 삭제. DB 원복 확인.
admin·gift 재기동 필요.
