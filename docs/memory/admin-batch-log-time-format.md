---
name: admin-batch-log-time-format
description: "배치 실행로그 조회(7209) 시작시간/종료시간이 HHmmss 원시값으로 보이던 것을 AS-IS처럼 HH:mm:ss로 수정(2026-10-08)"
metadata:
  node_type: memory
  type: project
---

AS-IS `OP_BATCH_EXECUTION.START_TIME`/`END_TIME`은 CUBRID **TIME 컬럼**이라 JDBC가 그대로
"09:30:00" 모양 문자열로 내려주고, AS-IS JSP(`batch-log/list.jsp`)도 포맷 없이 `${list.startTime}`을
그대로 찍는다. TO-BE `admin.op_batch_execution.start_time`/`end_time`은 **VARCHAR(8)** 로
"HHmmss"(예: "013000") 저장이라 그대로 찍으면 콜론 없이 보였다.

**수정**: `BatchLogRepository.BatchLogRow`에 `getStartTimeText()`/`getEndTimeText()` 추가
(6자리 문자열에 콜론 2개 끼워 "HH:mm:ss"로), `batch-log/list.html`에서 `item.startTime`/`endTime`
대신 그 getter를 쓰도록 교체. `getExecutionDateText()`(날짜 포맷)와 같은 패턴.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**
