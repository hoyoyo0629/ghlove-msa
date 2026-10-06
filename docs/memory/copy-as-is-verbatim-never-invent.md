---
name: copy-as-is-verbatim-never-invent
description: "사용자노출 문구·라벨·저장포맷·표시로직은 AS-IS 소스에서 그대로 복사한다. 내 해석으로 새 용어(예: '각인')를 짓거나 라벨을 바꾸지 말 것. AS-IS에 근거 없으면 짓지 말고 확인. 반복 지적받음."
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-22T09:13:54.156Z
---

**사용자 반복 지적(2026-09-22, 강하게)**: 내가 계속 AS-IS 정본을 안 보고 **내 판단으로 문구를 지어냄**. 예: `itemTextOptionFlag`를 "각인"이라 부르고 화면에 "각인" 라벨을 붙임(AS-IS 정본은 "필수 추가정보"). 표시도 "필수 추가정보 : " 라벨을 임의로 붙이고, 저장포맷도 "값만 ||"로 함 — **AS-IS는 라벨 없이 `formatTextOption`만 쓰고, 저장은 "제목 : 값 || 제목 : 값"**.

**Why:** 전환은 AS-IS 정본 재현이 목표([[as-is-logic-is-the-spec]], [[as-is-parity-includes-disabled-state]]). 내가 지어낸 용어/라벨/포맷은 근거 없는 변경이고, 사용자가 "기준으로 삼으라"고 여러 번 강조했는데도 반복해서 self-판단함. 이건 신뢰를 깎는 반복 실패다.

**How to apply (예외 없이):**
- 사용자에게 보이는 **모든 문구·라벨·버튼·안내·플레이스홀더**는 AS-IS 소스(JSP/HTML/JS)에서 **그대로 복사**하고, 코드에 근거(file:line)를 남긴다.
- **저장 포맷·표시 함수·검증 메시지**도 AS-IS 로직을 그대로 이식한다(예: `formatTextOption`은 cart/index.html·orderDetail.html 원문 그대로: `제목 : 값`, 값 20자 초과 시 `...`, `<br>` 조인, v-html 렌더).
- AS-IS에 해당 문구/로직이 **없으면 짓지 말고** 사용자에게 확인한다. "내 해석으로 더 명확하게"는 금지.
- 새 용어를 만들어 부르지 말 것(각인❌ → 필수 추가정보⭕). 개발용 코드주석에만 보조설명 허용.
- 정정 사례: 필수 추가정보 라벨 "필수 추가정보 입력"(details-main.html:234), "필수 추가정보 등록/사용여부"(form.jsp:1572/1585), 표시는 `formatTextOption` 그대로([[gift-option-multiform-redesign]]).

**★반복 재발(2026-09-22, 사용자 크게 분노 "하루종일 같은 얘기 몇번…")**: 문구·포맷뿐 아니라 **UI 요소의 위치/존재 자체**도 AS-IS를 먼저 확인해야 한다. 응원메시지(cheerMsg)를 AS-IS는 **특정사업 상세 "응원메시지(기부내역)" 탭에서 본인 기부건만 인라인 편집(saveCheerMsg, 30자)** 하는데, 나(및 이전 세션)는 **기부하기 화면(DonateView)에 없던 입력칸을 지어넣고** 거기에 30자까지 덧발라 두 번 일했다. AS-IS 기부하기 화면엔 응원메시지 입력이 아예 없다.
- **How to apply(강화)**: 새 화면요소를 만들기 전에 **"AS-IS의 어느 화면 어느 위치에 있는가"부터 소스로 확인**한다. 없으면 만들지 말고, 있으면 그 위치·조건(예: 본인건만 giveOrder==1)·동작(인라인 편집/버튼/API)까지 그대로 옮긴다. "탭을 채우려면 필요"식으로 위치를 임의 이동/발명 금지.
- 정정 완료: DonateView 응원메시지 입력 제거, DesignatedDetailView 기부내역 탭에 본인건 인라인 입력+"적용"+`POST /api/designated-donation/saveCheerMsg`(30자) 재현.
관련 [[check-as-is-source-when-analyzing]].
