---
name: as-is-parity-exhaustive-audit-method
description: "모든 서비스에서 AS-IS 재현은 \"발견되면 그때\" 방식 금지. 프론트 이벤트+서비스 검증로직+매퍼를 전수 대조해 갭 목록을 먼저 만들고 그 목록 기준으로 작업. ISP/RFP 신규·변경 요건은 반드시 사용자와 먼저 확인"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-21T08:03:12.892Z
---

**사용자가 강하게 지시한 표준 작업방식(2026-09-21).** donation에서 중복기부 확인 로직 같은 걸 하나씩 뒤늦게 발견하는 패턴에 대한 강한 불만에서 나옴.

**★제1원칙: AS-IS를 서비스로직도 화면단도 "일단 똑같이" 구현한다.** 임의 축소·단순화·생략 금지. 기본값은 완전 동일 재현이고, 벗어나는 것은 (a) 외부연계 미개방 같은 물리적 차단이나 (b) ISP/RFP 신규·변경 요건일 때뿐이며, 그 경우도 **사용자와 먼저 확인**한다. 과거 "의도적 축소"로 넘긴 것들도 이 원칙으로 재점검 대상.

**진행 순서(2026-09-21 확정): 먼저 5개 서비스(donation·member·point·gift·order) 전부 전수조사해 서비스별 갭 목록(`docs/<service>-parity-audit.md`)을 완성하고, 그 다음에 서비스별로 하나씩 구현한다.** **admin은 나중으로 미룸(이번 라운드 제외).** 즉 "조사·목록화 단계"와 "구현 단계"를 분리 — 목록이 다 나오기 전엔 개별 구현 착수 금지(중복기부처럼 순수재현 확정건도 목록 완성까지 대기). 산출물: `docs/{donation,member,point,gift,order}-parity-audit.md` **5종 완성(2026-09-21)** → 이제 구현 단계 전환 가능(admin 제외).

**규칙(모든 서비스 공통):**
1. **AS-IS 재현은 전수 대조 기반.** 화면 진입/컨트롤러만 훑지 말고, **① 프론트 이벤트(드롭다운 선택·버튼·confirm/alert 등 화면 동작) ② 서비스 검증 로직(모든 if 분기: 한도·중복·주소지제한·기간·본인인증 등) ③ 매퍼/쿼리**를 전부 대조한다.
2. **갭 목록을 먼저 만들고 그 목록 기준으로 작업한다.** "쓰다가 발견되면 그때 고치기" 금지 — 그래야 누락이 없다.
3. **ISP/RFP의 신규 또는 변경 요건이 걸릴 때는 반드시 사용자와 먼저 확인**하고 진행. 임의 구현/판단 금지. (단순 AS-IS 재현은 확인 없이 진행 가능, [[as-is-logic-is-the-spec]] 정본 기준)

**Why:** 지금까지 컨트롤러·매퍼 위주로만 분석해 프론트 이벤트·서비스 검증 분기를 놓쳤고(예: 중복기부 확인, 답례품 옵션 주문반영, map-select 오판), 그때그때 발견되며 신뢰를 잃고 작업이 늘어졌다. 전수 목록 기반이라야 끝이 보인다.

**How to apply:** 서비스별로 AS-IS 소스(화면 html의 @click/@change 이벤트 + `XxxController` + `XxxService`의 검증 분기 + `xxx-mapper.xml`)를 훑어 "AS-IS 동작 → MSA 재현 여부(O/X/부분)" 표를 만든다. 산출물은 `docs/<service>-parity-audit.md` 형태로 관리. 기존 2026-09-09 분석문서들은 이 깊이(프론트 이벤트+검증 분기)까지는 안 갔으므로 보강 대상. 관련 [[check-as-is-source-when-analyzing]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]], [[verify-screen-by-content-not-route]].
