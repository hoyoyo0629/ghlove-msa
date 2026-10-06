---
name: provider-portal-split-deferred
description: 제공자 포털 별도앱 분리 여부는 운영데이터 적재/개발DB 구축 후 재결정 — 현행(gift 내 /seller/*) 유지
metadata: 
  node_type: memory
  type: project
  originSessionId: 983919e3-275b-475e-9468-a8c78fb869e4
  modified: 2026-09-22T01:54:58.263Z
---

답례품 제공자 포털을 **별도 프론트엔드 앱으로 뺄지**는 2026-09-09에 **보류**했다. 잠정 결정은
**현행 유지** — gift 서비스(8084) 안의 Thymeleaf `/seller/*`. 최종 판단은 **운영데이터가 적재되거나
개발DB가 구축돼 실제 제공자 데이터·화면 규모가 드러난 뒤**로 미뤘다.

**Why:** "관리자 콘솔과 합칠 것인가"는 이미 닫힌 문제다(AS-IS 화면ID가 관리자 `UI_M` 244개 /
제공자 `UI_S` 44개로 교차 0건, ISP 프론트 3분할, N2SF 관리자망 VPN·IP제어는 민간 사업자에게 부여
불가). 게다가 현재도 admin(8086)과 별도 프로세스라 분리 요건은 충족돼 있다. 남은 건 배포 단위
문제인데, 제공자 화면이 답례품·주문·정산에 밀착돼 있어 지금 떼면 API 왕복만 늘어난다. **진짜
문제는 위치가 아니라 규모** — AS-IS 44개 대비 현재 `seller-dashboard.html` 1개뿐이다.

**How to apply:** 이 주제가 다시 나오면 "합칠까 나눌까"가 아니라 **채우는 작업**(제공자 포털 화면 +
자체 로그인)을 먼저 제안할 것. 별도 앱 분리는 ① 제공자 화면 20~30개 초과로 배포 주기가 어긋나거나
② 제공자 전용 인증·보안 정책(전용 도메인, IP 화이트리스트)이 생기거나 ③ 트래픽이 대민 조회와
간섭할 때 검토한다. 상세 근거와 이행 작업 11건은 `docs/account-model-design.md`에 있다.
관련: [[honor-tier-thresholds-unset]], [[policy-content-db-migration-deferred]]

**[2026-09-22 보강]** ISP 원문이 TO-BE **프론트엔드 3종(대민웹/관리자운영웹/답례품제공자웹)** 을 명시(p.11/217/439/445, docs/isp-detailed-design-summary.md). 현 MSA 실측: **온전한 독립 프론트엔드는 2개**(storefront=대민 Vue, admin=운영 Thymeleaf). 답례품제공자웹은 아직 독립앱 아님 — gift 서비스 임베드 Thymeleaf(`SellerPortalController`→`/seller/dashboard`→seller-dashboard.html, seller API는 `SellerApiController`). **결정(사용자, 2026-09-22): 제공자웹 분리는 계속 보류(A안).** gift 옵션/각인/추가구성 **판매자 등록화면을 현행 gift 판매자 포털(Thymeleaf)에 API-first로 얹고**(비즈니스 로직은 gift 서비스 REST에, 화면은 얇게 소비), 훗날 제공자웹 신설 시 **백엔드는 재사용·뷰만 재스킨 이식**. 즉 A안의 임시부채=판매자 포털 Thymeleaf 몇 장 증가. 제공자웹 재개는 옵션작업과 분리된 별도 결정. [[gift-option-multiform-redesign]] [[thymeleaf-duplicate-cleanup-deferred]]
