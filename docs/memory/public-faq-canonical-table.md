---
name: public-faq-canonical-table
description: 공개 고객센터 FAQ의 정본은 op_faq + FaqType enum이다 - 초기 시드가 지자체FAQ 표에 자체코드로 넣어둔 것을 2026-10-05에 교정했다
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-05T00:43:14.133Z
---

**AS-IS 정본**: 고객센터 FAQ는 **`op_faq`** 한 표를 세 곳이 같이 쓴다 -
운영자 FAQ 관리(메뉴 **5104**, `/opmanager/faq`), 공개 페이지(`/faq/list.html`),
공개 API(`/api/faq`). 통합검색 뷰 `view_search_faq`도 `op_faq`를 보고 `/faq/list.html`로 링크한다.
질문유형은 공통코드가 아니라 **Java enum `FaqType`**(`F_LOGIN`·`F_CNTR_SYSTEM`…)이고
AS-IS 세 화면 모두 `enumMapper.get("FaqType")`로 가져온다.

**2026-10-05 교정 전 TO-BE**: 초기 시드가 FAQ 63건을 **`op_community_locgovfaq`**
(AS-IS에서 **중지된** 메뉴 11403 '지자체FAQ'의 표)에 넣고 질문유형 코드도 자체 코드
(`JOIN`·`DONATE`…)로 바꿔 두었으며 공개화면이 거기를 읽고 있었다. `op_faq`는 0행이었고
메뉴 5104는 엉뚱하게 `/community/faq-bbs`(커뮤니티 담당자FAQ 11405)를 가리켰다.

**지금 상태**
- TO-BE 관리화면: `/faq-admin`(`FaqAdminController`) → `op_faq`, 유형은 `admin/service/FaqType`
- TO-BE 공개: `/faqs`·`/api/faqs`(`FaqService`) → `op_faq`, 유형 라벨도 enum
- 63건은 코드 1:1 번역해 `op_faq`로 이관(`database/ddl/migration-admin-faq-5104.sql`).
  **원본 행은 지우지 않았다** - 중지된 11403 화면(`/community/locv-faq`,
  `LocgFaqAdminController`)이 계속 그 표를 보고, 그 행들은 여전히 `JOIN`… 코드다.
  그래서 `op_common_code`의 `FAQ_TYPE` 11건도 **지우지 말 것**(그 화면의 라벨이다).
- 표 정리(`op_community_locgovfaq` 정리 여부)는 사용자 판단 대상으로 남겨 두었다.

FAQ 본문은 **스마트에디터 HTML**이라 공개화면에서 그대로 렌더해야 한다
(AS-IS가 `v-html`을 쓴다 - `th:text`로 두면 태그가 글자로 보인다).
