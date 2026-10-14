---
name: donation-sttemnt-pay-de-populated
description: 2026-10-14부터 g_cntr.sttemnt_pay_de(수납일)를 채운다 - 수납완료=IFNULL(수납확인일,오늘)+delete_at N, 과오납승인=NULL+delete_at Y, 기존 41건 백필, 빼 뒀던 AS-IS "수납일 있음" 조건 복구. "수납완료면 수납일 있음"이 불변식
metadata:
  type: project
---

AS-IS는 수납완료로 바꾸는 모든 UPDATE(ngdonation sunapSuccess/sunapResultSuccess, externalapi sunapSuccess,
give-state sunapUpdateY)에서 STTEMNT_PAY_DE·DELETE_AT='N'을 같이 넣고, 과오납 승인(giveCancelProcess)은 NULL·'Y'로 바꾼다.
TO-BE는 수납일을 donation_levy에만 남기고 g_cntr엔 안 넣어(Donation 엔티티 주석 "항상 비어 있다") 집계마다 AS-IS 조건을
빼고 번역해 왔다 - 기부금 접수관리 목록 수납일 공란으로 사용자가 발견.

**Why:** 화면·엑셀·집계가 AS-IS와 같은 규칙으로 계산되게 하려고(사용자 결정 2026-10-14).

**How to apply:**
- 불변식: TO-BE에서 COMPLETED이면 sttemnt_pay_de가 있다. 그래서 표시 없이 COMPLETED만 거는 나머지 20여 쿼리는 결과가 같다 -
  새 쿼리 이식 때는 AS-IS대로 `sttemnt_pay_de is not null`까지 그대로 쓴다.
- 복구한 곳: UnpaidDonationBatchRepository(미납=sttemnt null, 상태 번역 제거), OffgiveAdminRepository.detail 누적액,
  MemberDonationAdminRepository 5쿼리, GiveStatRepository(연·월도 수납일 기준), DesignatedStatRepository 6곳,
  DonationRepository.findDesignatedByStatus. admin 회원 기부내역 수납일은 yyyy-MM-dd.
- 남은 차이(납부게이트웨이·배치 라운드): 수납일 출처(AS-IS 실제 수납이력 vs TO-BE 만든 /sunap·로컬 가짜), 수납 시점/방법
  (배치·민간개방API·변경신청 300 수납처리 없음), 포인트 칸(point 원장 설계). [[missing-batch-jobs-port-together]],
  [[donation-payment-gateway-port-decision]]
- 데이터: `migration-donation-sttemnt-pay-de-backfill.sql`(levy 수납일 → 없으면 cntr_de).
