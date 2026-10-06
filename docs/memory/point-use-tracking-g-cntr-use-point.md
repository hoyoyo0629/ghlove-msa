---
name: point-use-tracking-g-cntr-use-point
description: point g_cntr_use_point 기부건↔주문 링크 기록 인프라(2026-09-21). 단 기부포인트 상세화면은 원장(pt_point_ledger)에서 직접 읽으므로 이 테이블은 상세에 안 쓰임(보조 기록)
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-21T04:05:07.598Z
---

**[2026-09-21 정정]** 처음엔 상세의 답례품 주문번호를 채우려고 이 추적을 신설했으나, **USE 원장행(PT_POINT_LEDGER)의 REF_KEY에 이미 주문번호(주문차감=orderId)가 들어 있어 별도 테이블이 불필요**했다. 게다가 상세를 g_cntr_use_point에서만 읽으니 **과거 사용분(추적 이전 데이터)이 안 보이는 버그**가 있었다(사용자 발견). 그래서 **상세는 원장에서 직접 읽도록 바꿨다**(`PointService.pointDetail`): 적립행=EARN(취소 REVERSE 제외), 사용행=USE(취소 RESTORE 제외)+EXPIRE, 주문번호=USE의 refKey, 각 행 러닝잔액 - 목록(ledgerSummary)과 같은 원장을 itemize하므로 합계가 정확히 일치한다. **g_cntr_use_point는 이제 상세화면에 안 쓰이는 보조 기록**이다. 부수로, 과거 EARN 행의 `cntr_amt`(기부액) NULL 시드데이터는 `donation.g_cntr`에서 백필했다(superuser cross-schema UPDATE, ref_key=cntr_sn). 실제 기부건 없는 e2e-* 행은 NULL 유지(빈칸). 구현 원문 `docs/point-service-analysis-2026-09-09.md` §10.

**핵심 불변식 (point 소진/복원 로직 수정 시 반드시 유지):**
- `PointService.consumeLots(userId, amount, locgovCode, orderCode)` — FIFO로 소진하는 **적립 lot마다**(EARN 원장행, `refKey`=기부건번호 CNTR_SN) `g_cntr_use_point`에 1행 기록(cntr_sn, order_code, cntr_use_point, cntr_locgov_code, use_se_code='1'). orderCode가 null/blank면 기록 안 함(수동/참조없는 사용은 취소 삭제 대상이 될 수 없어 제외).
- 3개 소진 경로 전부 orderCode를 넘긴다: `deductForOrder`(주문차감, orderId), `usePoints`(수동, orderCode), `confirmReservation`(예약확정, refKey).
- `restoreForOrder(orderId)`가 `gCntrUsePointRepository.deleteByOrderCode(orderId)`로 그 주문 사용이력을 삭제(AS-IS deleteGiveUsePoint) — 취소분은 상세의 기부건별 사용에서 사라진다.

**구성물:** 엔티티 `GCntrUsePoint`(USE_SN을 시퀀스 `SEQ_G_CNTR_USE_POINT`로 발번, DB PK는 (CNTR_SN,USE_SN) 복합이나 JPA엔 USE_SN 단독 @Id), `GCntrUsePointRepository`(findByCntrSnInAndUseSeCode, deleteByOrderCode). 상세 API `/api/my/points/detail?locgovCode=&year=`, 목록 API `/api/my/points`에 yearLocgovSummary·years 추가, SPA `MyPointDetailView.vue`(라우트 `/mypage/points/detail`)+`MyPointsView` 상세아이콘.

**한계(의도됨):** 소멸(EXPIRE)은 `g_cntr_use_point`에 안 남는다(AS-IS도 동일). 그래서 상세의 기부건별 '사용'은 소멸분 미포함 - 요약카드의 '사용'(원장 usedOf, [[point-readmodel-paused-pending-db-design]] §9의 소멸=사용 처리)과 소멸 발생 시에만 차이. 정상/취소 경로에선 일치한다.

이 테이블은 예전엔 스키마만 있고 비어 있던 레거시였다 - 이제 실제로 쓴다. 분석 기준은 [[check-as-is-source-when-analyzing]].
