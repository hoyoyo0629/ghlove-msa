---
name: check-as-is-source-when-analyzing
description: "상세분석 시 기능은 AS-IS+제안요청서(RFP)+ISP요약을 종합 판단하고, 레이아웃은 AS-IS를 기준으로 검토한다 (2026-09-09 사용자 지시, 절대 원칙)"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-17T10:29:29.877Z
---

**상세분석을 요청받으면 반드시 이 기준으로 진행한다.** 2026-09-09 사용자가 "절대 잊지 마"라고 못박은 원칙이다.

- **기능적 측면** = AS-IS 소스 + 제안요청서(RFP) + ISP 요약, **셋을 종합**해서 판단한다. 하나만 보고 결론내지 않는다.
- **레이아웃/화면** = **AS-IS를 기준**으로 검토한다. 마크업 구조·클래스명·CSS·열 구성 모두 AS-IS 화면이 정답이다.

**참조할 원본 (실측 확인, 2026-09-09):**
- AS-IS 소스: `C:\workspace\ghlove`
  - `ghlove-api/src/main/java/saleson/api/**` REST 컨트롤러
  - `ghlove-common/` 도메인·서비스·MyBatis 매퍼, SQL은 `src/main/resources/sqlmapper/cubrid/*.xml` (CUBRID)
  - `ghlove-frontend/` Nuxt/Vue 화면. 마이페이지 `mypage/*.html`, 회원 `users/*.html`, 레이아웃 `components/layouts/*.vue`, **CSS `static/css/*.css`**
  - `ghlove-web/`, `ghlove-batch/`, 외부연계 `onepass/ payment/ simpleauth/ magicline/`
- 제안요청서(RFP): `docs/requirements.md` — SFR-001~014 기능요구사항 정리본
- ISP: `docs/isp-detailed-design-summary.md` — 상세설계(6차) 691p PDF 요약. 원본 페이지 번호가 `(원본 p.NNN)`로 달려 있어 필요하면 pdftoppm으로 해당 페이지만 다시 렌더링해 확인 가능.

**Why:** MSA 코드와 주석만 보고 분석하면 (1) 재현이 누락·왜곡된 부분을 "원래 그런 것"으로 오판하고, (2) MSA 주석의 "AS-IS는 이렇다"는 서술 자체가 틀렸는지 검증할 수 없다. 실제로 이 방식으로 여러 결함을 찾았다 - 화면 CSS `/static/` 경로 버그, GNB의 취소반품교환 링크 오류, 국민비서 수신동의가 통째로 disabled로 막혀 있던 재현 누락, `mypage-order.css` 미포함으로 주문목록 표가 맨몸으로 나오던 문제.

**흐름(플로우)은 백엔드 기능이 아니라 프론트 흐름까지 추적해야 한다 (2026-09-17 반복지적).** 사용자가 "여러 번" 요구한 핵심. 백엔드 능력만 재현하고 진입점·UX를 지어내면 안 된다. **사용자 상호작용 흐름은 AS-IS 프론트 `ghlove-frontend/modules/op.saleson.js`(로그인·API 호출 허브) + 컨트롤러 + 서비스를 함께 읽어 "언제/어디서/어떻게 트리거되는지"를 그대로 옮긴다.** 실패 사례(휴면해제): AS-IS는 로그인 중 서버가 `SLEEP_USER` 코드를 내려주면 `op.saleson.js:1169`가 그 자리에서 `confirm("휴면해제 하시겠습니까?")`→`/api/auth/recovery`(자격증명 재입력 없음)로 처리하는데, MSA는 능력(reactivate)만 재현하고 **로그인 링크+아이디/비번 재입력 폼이라는 없는 진입점을 날조**했다(2026-09-17 AS-IS 대조로 바로잡음: 로그인 응답 SLEEP_USER→인라인 confirm→recovery). `users/old/*`처럼 `old/` 폴더나 주석처리된 코드는 라이브가 아니니 실제 활성 경로를 따라간다.

**How to apply:** 대응하는 AS-IS 파일을 먼저 찾아 읽고 MSA 구현과 차이를 짚는다. **화면이면 AS-IS가 링크하는 CSS 목록까지 대조한다** - 클래스명만 베끼고 CSS를 안 들여온 사례가 반복됐다. MSA 주석의 "AS-IS는 …" 서술은 근거로 인용하기 전에 원본으로 확인한다. 차이를 보고할 때는 **"의도적 축소(외부연계 미개방 등)" / "재현 누락" / "버그"를 구분**해서 쓴다 - 이 프로젝트엔 의도적 축소가 많다([[thymeleaf-duplicate-cleanup-deferred]], [[coupon-feature-unused-hide-ui]], [[point-reservation-unused-decision-deferred]]). 화면 문제는 코드만 읽지 말고 실제로 띄워서 확인한다.
