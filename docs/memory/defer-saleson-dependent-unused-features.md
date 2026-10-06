---
name: defer-saleson-dependent-unused-features
description: "AS-IS 기준 동일 구현하되, SalesOn(원제품)에 종속된 기능 중 현재 미사용으로 판단되는 것은 기록만 하고 구현은 보류. DA 설계안 확정 후 재개."
metadata:
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T05:36:19.970Z
---

**사용자 기준(2026-09-22)**: 지금 기준으로 AS-IS 확인하고 동일하게 구현하되, **SalesOn(원제품/saleson 패키지)에 종속된 기능 중 현재 미사용이라 판단되는 것은 기록해놓고 구현은 나중으로 미룬다**.

**Why:** DA(데이터아키텍트)쪽에서 설계안이 어떻게 나올지 모르기 때문. SalesOn 종속 미사용 기능을 지금 재현하면 나중에 DA 설계와 어긋나 헛일이 될 수 있다. [[saleson-original-product-leftovers]]가 죽은코드 판정의 최대 변수라는 것과 연결됨.

**How to apply:**
- 갭목록 뽑을 때 각 기능이 (a) 고향사랑 답례품 실사용인지, (b) SalesOn 종속·현재 미사용인지 판정한다.
- (b)로 판정되고 **아직 미구현**이면: **docs 갭목록/메모리에 기록만** 하고 구현은 보류(TODO). 억지로 재현하지 않는다.
- **(b)여도 이미 구현되어 있는 건 그대로 둔다(제거·롤백하지 않음)**. 지우지 말고 유지.
- 판정 근거: MSA DB 데이터 0건 + 화면 숨김/죽은코드 + saleson 패키지 종속 등.
- 이건 [[as-is-parity-includes-disabled-state]](비활성이면 숨김 유지)와 다르다: 저건 "만들되 숨김", 이건 "SalesOn 종속 미사용이면 아예 구현 보류·기록".
- 예: 카테고리 op_category 4단 트리(category_class1~4)+op_item_category — MSA DB 0건, SalesOn 종속 → 보류+기록. 답례품 실사용 카테고리는 GIFT_CATEGORY 대분류 + gift_subcategory 3단 메가메뉴(이미 구현).

관련 [[scope-migration-not-greenfield]] [[as-is-logic-is-the-spec]] [[continue-vs-rebuild-msa-decision]].
