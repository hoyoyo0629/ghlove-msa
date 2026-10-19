---
name: missing-batch-jobs-port-together
description: AS-IS 세외수입 수납내역 배치(cntrSunapBatch) 등 누락된 배치 job은 나중에 일괄 이식 - 그때 기탁서 상세의 임시 "수납처리" 버튼을 제거
metadata:
  type: project
---

2026-10-14 사용자 결정: AS-IS 세외수입시스템 수납내역 배치(`NgDonationBatchServiceImpl.cntrSunapBatch`, 수납완료 처리 + procOffOrder)는
지금 개별로 옮기지 않고, 누락된 배치 job들과 **일괄 이식**한다.

**Why:** 누락 배치가 많아 한 번에 정리하는 편이 낫다는 판단. 그 전까지 기능확인(수납완료 → 포인트 적립 → 답례품 결제)이
필요해 기탁서 상세(admin `/offgive/detail/{sn}`)의 TO-BE 임시 "수납처리" 버튼(`POST /offgive/{sn}/confirm`)을 주석+화면
안내문("[개발 확인용] ... 배치 이식 시 제거됩니다")과 함께 유지한다.

**How to apply:** 배치 일괄 이식 라운드에서 op_batch_job(AS-IS job_method)과 대조해 누락 job 전수 목록부터 만들고,
cntrSunapBatch 이식 시 위 버튼·안내문·confirm 엔드포인트·admin OffgiveClient.confirm·donation `/api/offgive/{sn}/confirm`을 함께 제거한다.
관련: [[offgive-15102-verbatim-port-data-gaps]], [[offgive-gift-order-asis-flow]]

**2026-10-15 이식 완료**: fiveMinuteSunapCheckBatch·dayCompleteSunapCheckBatch(donation)·cntrSunapBatch(order) 이식,
임시 "수납처리" 버튼·confirm 엔드포인트 전부 제거. 로컬은 AS-IS LOCAL 분기처럼 5분 안에 자동 수납처리된다.
배치 전체 진행은 [[batch-job-port-decisions]]·docs/batch-job-port-ledger.md.
