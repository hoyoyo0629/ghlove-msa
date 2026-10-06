---
name: copy-asis-css-js-assets-verbatim
description: AS-IS의 CSS/JS 등 프론트 자산은 그대로 복사해 동일 구현. 대체·단순화·생략 절대 금지(표로 차트 대체 등)
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T05:01:40.930Z
---

**CSS·JS·차트·에셋을 TO-BE에 "없으니 표로/간단히" 대체하지 말 것. AS-IS 파일을 그대로 복사해 동일하게 구현한다.** 사용자가 여러 번 강하게 지적(2026-10-01 포함, "css건 js건 as-is와 동일하게 하라고 몇번을 말하냐").

**Why:** MSA 전환은 AS-IS 화면을 100% 동일 재현하는 것([[port-everything-no-omissions-then-rfp-isp]], [[copy-as-is-verbatim-never-invent]]). 라이브러리가 TO-BE에 없으면 **AS-IS에서 복사해 오면 되는 것**이지, 표·단순화로 바꾸면 화면이 달라진다. "TO-BE에 ~가 없어서"는 대체 사유가 안 됨 — 가져오면 됨.

**How to apply:**
- 화면 구현 전 AS-IS가 쓰는 CSS/JS(차트 등)를 먼저 확인하고, 없으면 AS-IS에서 복사(`ghlove-web/static/content/...`, `ghlove-frontend/...`)해 동일 사용.
- 차트=AS-IS가 Chart.js면 Chart.js로(예: `content/modules/chart.min.js`+`op.chart.js`의 `ChartCommon.drawChart`), 표로 대체 금지.
- 2026-10-01 만족도조사: 처음에 "admin에 차트 라이브러리 없음→표"로 했다가 지적받아 chart.min.js+op.chart.js 복사 후 AS-IS 동일 스택막대로 교정.
- **소급 점검**: 기존에 내가/이전 라운드가 AS-IS 차트/자산을 표·단순화로 바꿔둔 화면(admin give-statistics, 특정사업 analysis 등 통계류) 발견 시 AS-IS대로 교정.
