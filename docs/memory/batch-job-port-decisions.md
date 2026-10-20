---
name: batch-job-port-decisions
description: 누락 배치 일괄 이식(2026-10-15 착수, 10-19 5차까지) 결정 - 화면(op_batch_job) 설정으로 실행, 76행 AS-IS 상태로 등록, BIX5 보류, 포인트 소멸 중지, 중지 job은 구현만. 휴면 2개는 보류. 원장 docs/as-is-parity.md 23절(배치 job 이식 원장)
metadata:
  type: project
---

2026-10-15 사용자 "배치 작업 들어가자"(메인화면 다음). AS-IS OP_BATCH_JOB 76행(사용 41, 그중 BIX5 20 / 중지 35).
원장: `docs/as-is-parity.md 23절(배치 job 이식 원장)`(job별 소유 서비스·외부연계·TO-BE 상태, 1~5차).

**결정(사용자, 전부 추천안)**
1. 실행 방식은 AS-IS처럼 **관리 화면(1403) 설정이 실제 스케줄** - 서비스별 고정 @Scheduled를 걷어내고 op_batch_job을 주기적으로 읽어 반영.
2. 76행 전부 AS-IS 상태로 등록. 고향사랑 고유 로직은 구현하되 중지 유지, SalesOn 원제품 job은 행만.
3. BIX5 20개는 행만 등록, 적재 로직 보류(BIX5 표 없음·외부 BI).
4. 포인트 소멸은 AS-IS처럼 **중지**(TO-BE가 매일 돌리던 것 정지).

**2026-10-19 5차까지 완료**: 중지 상태 고유 job(국세청 영수증·관리자 잠금·영수증 임시·삭제건 수납 재확인·연령 통계·포인트 만료 안내 메일) 구현·중지 유지.
**2026-10-19 사용자 지시로 배치 작업 일단 종료.** 남은 10건(휴면·900 리터럴·테스트 수신자·CUBRID 드라이버·연계 비밀값·통계/BIX5·정산예정 문자·넷퍼넬·판매자 출고·nh_export_log)은 원장 "남은 사항" 표에 모아둠 - 결정 받은 뒤 처리.

**Why:** AS-IS는 화면에서 중지하면 실제로 멈추는 구조라, 코드 고정 cron은 운영자가 배치를 통제할 수 없다.
**How to apply:** 새 배치를 이식할 때 @Scheduled에 cron을 박지 말고 job_method 레지스트리(서비스별 *BatchJobs)에 등록할 것. 보고 batchType = job_method.
예비 포트 검증 때 BATCH_STATUS=1인 짧은 주기 job(수납확인 34·2000109 등)이 그 서비스 소유면 잠시 중지 후 복원.
cntrSunapBatch 이식 때 기탁서 임시 "수납처리" 버튼 제거([[batch-job-port-decisions]]). 관련 [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-main-dashboard-asis-port).

## missing-batch-jobs-port-together

> AS-IS 세외수입 수납내역 배치(cntrSunapBatch) 등 누락된 배치 job은 나중에 일괄 이식 - 그때 기탁서 상세의 임시 "수납처리" 버튼을 제거


2026-10-14 사용자 결정: AS-IS 세외수입시스템 수납내역 배치(`NgDonationBatchServiceImpl.cntrSunapBatch`, 수납완료 처리 + procOffOrder)는
지금 개별로 옮기지 않고, 누락된 배치 job들과 **일괄 이식**한다.

**Why:** 누락 배치가 많아 한 번에 정리하는 편이 낫다는 판단. 그 전까지 기능확인(수납완료 → 포인트 적립 → 답례품 결제)이
필요해 기탁서 상세(admin `/offgive/detail/{sn}`)의 TO-BE 임시 "수납처리" 버튼(`POST /offgive/{sn}/confirm`)을 주석+화면
안내문("[개발 확인용] ... 배치 이식 시 제거됩니다")과 함께 유지한다.

**How to apply:** 배치 일괄 이식 라운드에서 op_batch_job(AS-IS job_method)과 대조해 누락 job 전수 목록부터 만들고,
cntrSunapBatch 이식 시 위 버튼·안내문·confirm 엔드포인트·admin OffgiveClient.confirm·donation `/api/offgive/{sn}/confirm`을 함께 제거한다.
관련: [[offgive-gift-order-asis-flow]], [[offgive-gift-order-asis-flow]]

**2026-10-15 이식 완료**: fiveMinuteSunapCheckBatch·dayCompleteSunapCheckBatch(donation)·cntrSunapBatch(order) 이식,
임시 "수납처리" 버튼·confirm 엔드포인트 전부 제거. 로컬은 AS-IS LOCAL 분기처럼 5분 안에 자동 수납처리된다.
배치 전체 진행은 [[batch-job-port-decisions]]·docs/as-is-parity.md 23절(배치 job 이식 원장).
