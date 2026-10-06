---
name: donation-payment-gateway-port-decision
description: "[결정 2026-10-01] donation 납부 게이트웨이 연계(NTS/지방세/서울 etax/지로)를 전부 이식, 단 설정 스왑 가능하게. 기존 보류 번복"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T04:04:04.820Z
---

**[결정 2026-10-01]** donation 납부 게이트웨이 연계를 **이식하기로**. [[donation-service-deferred-items]]의 "PG/납부연계 보류"를 이 범위에서 **번복**.

**대상(AS-IS ghlove-api)**: `donation/NgDonationController`(부과 sntrBugaInsert·수납 etaxSunapInfo/sunapSuccess·contryBuga/Sunap·local-sunap-confirm·giroPay·getPublicKey 등 ~22), `donation/RegionTaxController`(지방세 bugaRequest·sunapProcess·giroPay 등), `donation/SeoulTaxController`(서울 etax). 프론트: ghlove-frontend/donation `external-etax(-2)·external-giro(-2)·giro-success/fail·wetaxInfo(_m)·ngdonation·donationNext(-main)·process·donation-tax·popup-success/fail`.

**방식(사용자 지시)**: "전부 다 이식하되 **추후 연계가 다시 확정되면 관련 설정만 바꿔서 바로 기능을 사용**할 수 있게."
→ 외부 세정/지로/etax 엔드포인트·인증서·키를 **설정(application.yml)+어댑터 경계**로 분리. 확정 전에는 스텁/모의 응답(모의 bugaRequest 등 AS-IS에 mockBugaRequest·mockRsgstadres 선례 있음)으로 흐름 완성, 확정 시 config만 교체해 실연계 전환. DDL/도메인/화면/상태전이는 AS-IS대로 전부 구현.

**순서**: 나머지 커버리지 점검(gift→order→common) **먼저**, 납부연계 구현은 그 다음 라운드. 진척 [[as-is-parity-audit-progress]], 원장 docs/coverage-ledger.md.
