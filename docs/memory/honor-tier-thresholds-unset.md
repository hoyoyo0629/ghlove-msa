---
name: honor-tier-thresholds-unset
description: 명예기부자 등급 기준금액(g_locgov STDR_1/2/3LEVEL_AMT)이 전 지자체 0 — 운영데이터 적재 필요
metadata: 
  node_type: memory
  type: project
  originSessionId: 983919e3-275b-475e-9468-a8c78fb869e4
  modified: 2026-09-09T04:22:24.548Z
---

명예기부자(기부혜택증) 등급 산정 기준금액인 `donation.g_locgov`의 `STDR_1LEVEL_AMT`/`STDR_2LEVEL_AMT`/
`STDR_3LEVEL_AMT`가 **2026-09-09 기준 전 지자체 0**이다. 원래는 지자체 담당자가 관리자 화면에서
설정하는 값인데 그 화면이 아직 없어서(`docs/as-is-feature-audit-member.md` §2-5) 채울 방법도 없다.

**Why:** 0을 임계값으로 그대로 쓰면 누적액이 항상 0 이상이라 100원만 기부해도 최고등급("특급")이
붙는다. 2026-09-09에 `DonationService.upsertHonorTier`가 양수 구간만 유효한 기준으로 보도록 방어를
넣어서 지금은 **기준금액이 0인 지자체엔 등급이 아예 부여되지 않는다**(등급 기능이 사실상 비활성).

**How to apply:** 기부혜택증 화면이 "비어 있다"는 얘기가 나오면 버그가 아니라 이 데이터 미설정이
원인이다. 운영데이터를 받거나 지자체 명예사용자 관리 화면을 만들 때 이 세 컬럼을 함께 채워야
등급이 살아난다. 관련: [[policy-content-db-migration-deferred]]
