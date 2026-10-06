---
name: no-invented-features-ask-first
description: "★대원칙: AS-IS에 없는 기능/화면/필드/버튼은 발명·추가 절대 금지. 무조건 있는 그대로. 예외는 ISP/RFP의 신규·변경 요청뿐이고 그때도 먼저 사용자에게 물어 결정한다"
metadata:
  node_type: memory
  type: feedback
---

**★대원칙(2026-10-06, 사용자가 강하게 반복 지시):** AS-IS에 없는 기능·화면·필드·버튼·드롭다운·
엔드포인트는 **내 판단으로 발명하거나 추가하지 않는다. 무조건 AS-IS에 있는 그대로.**
유일한 예외는 **ISP/RFP에 신규 기능 추가나 변경 요청이 명시된 경우**뿐이고, 그때도 **먼저
사용자에게 물어보고 결정**한다. 빼지도, 켜지도, 새로 만들지도 말 것.

**Why:** 반복해서 TO-BE가 AS-IS에 없는 것을 지어넣어 왔다. 대표 사례(2026-10-06 관리자 권한
승인관리 상세 팝업): AS-IS 승인은 관리자가 권한을 고르지 않고 **신청구분(reqstSeCode)이 그대로
부여 권한**이 되는데, TO-BE는 "부여 권한" 드롭다운과 "소속 지자체" 입력을 **발명**해 폼에 넣었다.
AS-IS는 라디오(승인/거절) 하나 + 거절 시 거절사유뿐이다. 이런 발명이 쌓이면 "AS-IS 재현"이라는
전제 자체가 무너지고, 사용자가 매번 화면을 열어 발견해 지적하는 악순환이 된다. 사용자 표현:
"as-is에 없는 기능이나 화면은 니 마음대로 발명/추가 절대 금지. 무조건 있는 그대로가 원칙."

**How to apply:**
- 화면/기능을 손대기 전에 **반드시 4축을 메서드 단위로 먼저 다 읽는다**:
  JSP + 매퍼 SQL + 컨트롤러 + ServiceImpl 본문. 읽기 전에 편집 금지([[asis-screen-port-procedure]]).
- 그 분석에서 TO-BE에만 있는 필드/버튼/엔드포인트/로직이 보이면 = **발명이다. 제거하고 AS-IS로
  되돌린다**(되돌리기는 ASK 불필요 - AS-IS가 정본이므로).
- AS-IS에 없는 **새 동작이 TO-BE 아키텍처상 불가피**한 경우(예: admin op_manager가 member와
  분리돼 있어 승인 시 자격증명을 어떻게든 만들어야 함)는 **임의로 정하지 말고 사용자에게 묻는다.**
- ISP/RFP 신규·변경도 **구현 전에 묻는다.** 근거 없이 "좋아 보여서" 추가 금지.
- 비활성/숨김/주석 상태도 AS-IS 그대로 유지한다([[as-is-parity-includes-disabled-state]]).
- 정적요소(CSS/JS/이미지)는 이미 AS-IS에서 전부 복사해 왔다 - "없어서 새로 만든다"가 아니라
  "AS-IS 어디에 있나"를 먼저 찾는다([[copy-asis-css-js-assets-verbatim]]).

관련: [[copy-as-is-verbatim-never-invent]] [[scope-migration-not-greenfield]]
[[port-everything-no-omissions-then-rfp-isp]] [[check-as-is-source-when-analyzing]]
