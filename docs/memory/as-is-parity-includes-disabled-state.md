---
name: as-is-parity-includes-disabled-state
description: "AS-IS 재현 기준점: 화면/서비스에 기능이 있으면 동일하게 다 만든다. 단 AS-IS에서 숨김(CSS)/주석처리/비활성 상태면 우리도 만들되 동일하게 숨김/주석/비활성으로 둔다 — 활성·비활성 상태까지 parity."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T00:56:38.574Z
---

**기준점(2026-09-22, 사용자 지시)**: AS-IS 화면단/서버단에 기능이 존재하면 MSA도 **동일하게 전부 구현**한다. 그러나 AS-IS 소스에서 그 기능이 **CSS 숨김·주석처리·비활성** 상태로 두어져 있으면, MSA도 **기능은 만들어 놓되 동일하게 숨김/주석/비활성** 상태로 둔다.

**Why:** 전환사업은 AS-IS 정본 재현이 목표([[as-is-logic-is-the-spec]], [[scope-migration-not-greenfield]]). 기능을 빼버리면 재현 누락이고, 임의로 켜면 근거 없는 신규 활성화다. AS-IS 운영자가 의도적으로 꺼둔 상태(숨김/주석)까지 그대로 옮겨야 "동일"이 성립한다.

**How to apply:**
- 갭목록/인벤토리에서 "미사용/죽은코드"로 보여도, 화면·서비스 마크업에 존재하면 **구현 대상**이다. 단 노출/활성 여부는 AS-IS 상태를 따른다.
- 예: 답례품 옵션형태 라디오 — AS-IS 판매자 등록폼에서 S(선택형)·S3(3조합형)는 노출, **S2(2조합형)·T(텍스트형)는 `class="hidden"`** → MSA도 S2·T를 만들되 판매자 화면에서 숨김([[gift-option-parity-audit]] §0-1).
- AS-IS 코드가 `<%-- --%>`/`/* */`/주석블록으로 꺼져 있으면 MSA도 해당 부분을 주석/비활성으로 남긴다(삭제·활성화 금지).
- 애매하면([[as-is-parity-exhaustive-audit-method]]) 사용자 확인.
