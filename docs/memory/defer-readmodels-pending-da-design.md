---
name: defer-readmodels-pending-da-design
description: 모든 조회모델(ReadModel/rwd_*/조회전용 프로젝션) 작업은 DA 설계안이 나올 때까지 보류. 특정 도메인이 아니라 전 도메인 공통.
metadata: 
  node_type: memory
  type: project
  modified: 2026-09-22T08:47:18.580Z
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
---

**사용자 결정(2026-09-22)**: 조회모델(ReadModel) 작업은 **전부 보류**하고, **DA쪽 설계안이 나오면** 그때 진행한다. 특정 도메인만이 아니라 **모든 도메인의 조회모델**이 동일.

**Why:** 조회모델 스키마·프로젝션 구조는 DA(데이터아키텍트) 설계안에 좌우되므로, 지금 만들면 나중에 어긋나 헛일이 될 수 있다. [[defer-saleson-dependent-unused-features]](SalesOn 미사용 보류)·[[point-readmodel-paused-pending-db-design]](point 조회모델 중지)와 같은 맥락.

**How to apply:**
- 갭목록에서 "조회모델/ReadModel/rwd_*/조회전용 프로젝션/목록 표시 캐시 보강"이 나오면 **기록만 하고 보류**. 억지로 만들지 않는다.
- 해당 예: order 멀티아이템 Phase 6 ②(admin 주문목록 조회모델), point 조회모델, donation 조회모델, member 조회모델 등.
- 이미 만들어진 조회모델은 그대로 둔다([[defer-saleson-dependent-unused-features]]의 "이미 구현된 건 유지"와 동일 원칙).
- 조회모델이 아닌 실사용 표시 버그(예: [[order-single-item-vs-multiitem-decision]] Phase 6 ③ point 주문번호 '#' 앞부분 표시는 displayOrderCode로 이미 완료)는 보류 대상 아님.

관련 [[point-readmodel-paused-pending-db-design]] [[order-single-item-vs-multiitem-decision]] [[defer-saleson-dependent-unused-features]].
