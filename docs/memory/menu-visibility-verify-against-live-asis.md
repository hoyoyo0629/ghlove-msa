---
name: menu-visibility-verify-against-live-asis
description: "admin 메뉴 노출규칙은 display_flag가 아니라 STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재 - AS-IS menu-mapper.xml이 DISPLAY_FLAG를 WHERE에 전혀 쓰지 않음. 2026-10-02 AS-IS export 전수대조로 확정·동기화 완료"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-02T06:47:58.430Z
---

**AS-IS opmanager nav 노출조건 = `MENU_TYPE=3 AND STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재`.** `DISPLAY_FLAG`는 `menu-mapper.xml`의 SELECT 목록에만 있고 **WHERE에 단 한 번도 쓰이지 않는다**(`getFirstMenuList`/`getSecondAndThirdMenuList`). 권한조인을 건너뛰는 건 `ROLE_SUPERVISOR` 하나뿐이고, 그 롤은 TO-BE에 없다(`OpmanagerHandlerInterceptor:154,183`).

**Why:** TO-BE `MenuService`가 `display_flag='Y'`로 필터하면서 `ROLE_ADMIN_1~4`를 권한조인에서 우회시키고 있었다. 그래서 AS-IS엔 안 보이는 메뉴(권한행 0건)가 TO-BE엔 노출됐다 - 기부금 운영현황 6900/6901, 답례품 관리자 4701, 기부혜택증 설정 19102. "재시드 display_flag가 AS-IS와 불일치"로 적어뒀던 이전 판단은 **틀렸다**: 6900/6901은 AS-IS도 `display_flag='Y'`였고, 숨겨진 진짜 이유는 `OP_MENU_RIGHT` 행이 0건이었다.

**How to apply:** 메뉴 노출/숨김은 `display_flag`를 만지지 말고 **`op_menu_right` 행 유무 + `status_code`**로 판단한다. TO-BE 전용 신규 화면을 만들면 `op_menu_right`에 롤을 명시해야 nav에 나온다(안 넣으면 조용히 사라짐). nav는 엄격 판정(`MenuService.navVisible`), 화면 접근(`hasAccess`)만 ROLE_ADMIN_1~4 완화를 유지 → "nav 숨김 + URL 직접접근 가능". DB 변경은 즉시 반영(재기동 불필요), 코드 변경은 bootJar+재기동 필요.

**2026-10-02 전수 동기화 완료** (`database/ddl/migration-admin-menu-asis-sync.sql`, 멱등):
- 기준 export: `Desktop\고향사랑e음\1.AS-IS\1. DB\OP_MENU_202610010945.sql` / `OP_MENU_RIGHT_202610010946.sql` (**UTF-8**이다, EUC-KR 아님 - iconv 돌리지 말 것)
- AS-IS 355행 / TO-BE 182행 공통, AS-IS전용 173(대부분 SalesOn 잔재·권한0), TO-BE전용 1(5120 내부문의)
- display_flag 15건 AS-IS값으로 동기화, op_menu_right 누락 18행(ROLE_ADMIN_7/8/10) 보충, 초과 0건, 5120에 ROLE_ADMIN_1~4 부여
- 메뉴명 불일치 0건, 트리 불일치 0건(root parent NULL↔0 표현차뿐)
- **AS-IS에서 보이는데 TO-BE에 메뉴행조차 없는 것은 0개** - 차이는 전부 "행은 있고 menu_url 미등록 = 화면 미구현"
- 롤별 nav 리프 수: AS-IS 1=114/2=87/3=95/4=85/5=53/6=51 vs TO-BE 104/79/85/76/50/48 → **격차 10개가 곧 미구현 화면 수**

**미구현 10개(= TO-BE에 행은 있고 URL 없음)**: 메세지 관리(1402), Q&A(5112), 방문자접속경로(6102), 외국인 기부통계 전체/지자체별(6711/6712), 배치 실행로그(7209), 기금사업 등록/관리(14201), 신규주문·발송준비중(모바일)(16407/16408), 제철식품관(16751). 추가로 특산물관(16801, 롤3·4). 이것이 B군 잔여 백로그의 실측 목록이다. 관련: [[as-is-parity-includes-disabled-state]] [[admin-shell-asis-parity-restored]] [[as-is-parity-audit-progress]]
