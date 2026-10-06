---
name: port-everything-no-omissions-then-rfp-isp
description: "MSA 전환 기본선: AS-IS front/back/static 전부 누락없이 이식 먼저, RFP/ISP 변경은 그 위에 별도 확인받고 추가"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T02:16:43.700Z
---

MSA 전환(신규구축 아님)의 작업 순서 원칙 — 사용자가 라운드마다 반복 강조(2026-10-01 재강조).

**원칙**
1. AS-IS의 **기능·화면·정적자산을 100% 그대로 이식**하는 것이 기본선. 깨진/비활성/에러 상태여도 그대로([[as-is-parity-includes-disabled-state]], bix5 404 사례).
2. RFP/ISP에 있는 **변경·수정·신규**는 그 위에 얹되, **반드시 별도로 사용자 확인** 후 진행([[scope-migration-not-greenfield]], [[as-is-parity-exhaustive-audit-method]]).
3. 따라서 **front / back / static(css·js·이미지·폰트) 어느 것도 누락이 없어야** 한다. "기능만" 보지 말고 **파일·에셋 단위 커버리지**까지 챙길 것.

**Why:** 전환은 기존 자산을 빠짐없이 옮기는 게 성패 기준. 누락은 곧 기능상실. 사용자가 가장 반복 지적하는 지점.

**How to apply:**
- 라운드 착수 전 **AS-IS 원본 트리(컨트롤러·뷰 JSP·static) ↔ TO-BE** 를 파일 단위로 대조한 **커버리지 원장**(present/missing/gap)을 먼저 만들고 그걸로 구동. "발견되면 그때" 금지.
- back(엔드포인트)·front(뷰/템플릿/Vue)·static(참조된 css·js·img·font 실제 복사여부)을 각각 체크. [[static-asset-scoping-2026-09-18]]처럼 누락 이미지 복사 전례 있음.
- AS-IS 소스: `C:\workspace\ghlove`(ghlove-web/common/api/batch/frontend), 코드데이터는 [[asis-table-dump-path]], 인벤토리 산출물 docs/inventory/([[as-is-inventory-procedure]]).
- admin 메뉴트리 라운드도 이 기준으로: 메뉴뿐 아니라 각 화면의 뷰+static까지 누락 점검.
