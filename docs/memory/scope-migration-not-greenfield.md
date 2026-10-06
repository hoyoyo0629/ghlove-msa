---
name: scope-migration-not-greenfield
description: 이 프로젝트는 신규 개발이 아니라 전환사업 - 구현 범위는 "AS-IS 로직 + 제안요청서/ISP에 명시된 신규기능"뿐이고, 근거 없는 임의 추가는 하지 않는다 (2026-09-10 사용자 지시)
metadata:
  type: feedback
---

**ghlove-msa는 신규 개발사업이 아니라 AS-IS(`C:\workspace\ghlove`) 전환사업이다.** 따라서 구현 범위는 딱 두 가지다.

1. **AS-IS에 있는 로직·화면의 재현**
2. **제안요청서(`docs/requirements.md`) 또는 ISP(`docs/isp-detailed-design-summary.md`)에 명시된 신규기능**

**둘 중 어디에도 근거가 없으면 만들지 않는다.** "있으면 좋을 것 같아서", "UX상 자연스러워서" 붙이는 기능은 범위 밖이다.

**Why:** 전환사업의 완료 기준은 "AS-IS와 같게 동작하는가 + 발주처가 요구한 신규기능이 들어갔는가"다. 근거 없는 추가는 검수 대상도 아니면서 유지보수 부담과 AS-IS 대조 노이즈만 늘린다. 실제로 2026-09-10에 헤더 장바구니 개수 배지를 만들었다가 **AS-IS 라이브 레이아웃에 없고 RFP/ISP에도 없다**는 이유로 걷어냈다(AS-IS의 구버전 `header.vue`/`footer.vue`에만 남은 잔재를 라이브 기능으로 오독한 것이 발단이었다 - [[verify-screen-by-content-not-route]]).

**How to apply:** 기능을 추가하기 전에 근거를 먼저 댄다 - AS-IS 파일·줄번호이거나, RFP의 SFR 항목이거나, ISP 페이지다. 근거를 못 대면 만들지 말고 "AS-IS에도 RFP/ISP에도 없다"고 보고한 뒤 사용자 판단을 받는다. 사용자가 요청한 기능이라도 근거가 없으면 **그 사실을 먼저 알리고** 진행한다(나중에 걷어내는 것보다 싸다). 분석·판단 기준 자체는 [[check-as-is-source-when-analyzing]] 참고.
