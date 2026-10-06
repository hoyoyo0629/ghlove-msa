---
name: featured-unused-skip
description: "featured 재검토 필요 - 2026-10-06 '미사용'으로 판단했으나 AS-IS CUBRID OP_FEATURED에 1,063건(이벤트성 데이터)이 있어 틀렸을 수 있음. 사용여부 재분석 보류 중"
metadata:
  node_type: memory
  type: project
---

**★이 메모의 2026-10-06 최초 결론('featured 미사용')은 틀렸을 가능성이 크다. 재분석 보류 중.**

- 처음에 TO-BE `gift.OP_FEATURED` 테이블이 미배포(조회 시 relation 없음)인 것만 보고 "미사용"으로
  판단했다. 그러나 사용자가 **AS-IS 개발DB(CUBRID)에서 `SELECT count(*) FROM OP_FEATURED` = 1,063건**
  이고 내용이 **이벤트 관련 데이터**임을 확인해줬다. 즉 featured는 기획/전시가 아니라 **이벤트로
  실제 사용 중**일 수 있다. [[asis-cubrid-dev-db-access]]
- 따라서 featured-admin 화면(현재 AS-IS와 컬럼 구성이 다르고 `small-btn` 등 발명 클래스 사용)은
  **AS-IS 충실이식 대상일 수 있다** - "미사용이니 패스"로 단정하지 말 것.
- **다음 할 일**: AS-IS `featured/*`(FeaturedController/FeaturedManagerController + featured-mapper +
  JSP)와 OP_FEATURED 실데이터를 4축으로 재분석해 실제 용도(이벤트?)·화면 구성을 확정하고,
  TO-BE를 AS-IS대로 맞춘다. **AS-IS OP_FEATURED export(1,063건)**:
  `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\OP_FEATURED_202610061741.sql` (2026-10-06 사용자 제공).
- 2026-10-06에 featured-admin 목록 기간 셀만 AS-IS 로직(`99999999`→상시게시/개방형)으로 고쳐둔 상태.
- 사용자 지시: "일단 현재 작업(1405)부터 완료하고 featured는 다시 검토하자."
