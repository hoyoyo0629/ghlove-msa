---
name: admin-privacy-log-hist-sort-order
description: "엑셀다운로드 사유 수정이력 팝업 정렬을 HIST_ID DESC에서 AS-IS와 같은 CREATED_AT DESC로 수정(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

AS-IS `privacy-access-mapper.xml`의 `getPrivacyAccessLogHistListByParam`은
`ORDER BY OP.CREATED_AT DESC`다(HIST_ID 아님). TO-BE
`PrivacyAccessLogHistRepository.findByPrivacyAccessLogIdOrderByHistIdDesc`는 HIST_ID DESC로
정렬하고 있었다 - 보통은 결과가 같지만, [[admin-log-screens-asis-sample-data]]에서 샘플
로그에 "최초 생성" baseline 이력을 나중에(= 더 큰 HIST_ID로) 과거 날짜로 끼워넣었더니 바로
깨졌다(과거 생성 이력이 최근 수정 이력보다 위에 뜸). `findByPrivacyAccessLogIdOrderByCreatedAtDesc`로
교체해서 해결 - 실제 쿼리로 시간역순 확인 완료.

**교훈**: ID 기반 정렬과 시간 기반 정렬은 "보통은 같다"고 가정하지 말 것 - AS-IS 매퍼가 명시한
컬럼을 그대로 쓸 것. 특히 수동으로 과거 시점 데이터를 나중에 삽입하는 시나리오(백필, 샘플
데이터 보강)에서 이 차이가 바로 드러난다.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**
