---
name: batch-job-port-decisions
description: 누락 배치 일괄 이식(2026-10-15 착수, 10-19 5차까지) 결정 - 화면(op_batch_job) 설정으로 실행, 76행 AS-IS 상태로 등록, BIX5 보류, 포인트 소멸 중지, 중지 job은 구현만. 휴면 2개는 보류. 원장 docs/batch-job-port-ledger.md
metadata:
  type: project
---

2026-10-15 사용자 "배치 작업 들어가자"(메인화면 다음). AS-IS OP_BATCH_JOB 76행(사용 41, 그중 BIX5 20 / 중지 35).
원장: `docs/batch-job-port-ledger.md`(job별 소유 서비스·외부연계·TO-BE 상태, 1~5차).

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
cntrSunapBatch 이식 때 기탁서 임시 "수납처리" 버튼 제거([[missing-batch-jobs-port-together]]). 관련 [[admin-main-dashboard-asis-port]].
