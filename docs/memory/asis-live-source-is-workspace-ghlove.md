---
name: asis-live-source-is-workspace-ghlove
description: localhost:8080 AS-IS가 실제로 도는 소스는 C:\workspace\ghlove(Desktop 1.AS-IS 소스는 분석용 사본) + AS-IS 재기동 방법과 함정
metadata:
  node_type: memory
  type: reference
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T01:34:38.256Z
---

**AS-IS(localhost:8080 opmanager / 9080 api / 3000 frontend)가 실제로 실행되는 소스 = `C:\workspace\ghlove`.**
`Desktop\고향사랑e음\1.AS-IS\0. 소스코드\ghlove`는 분석용 사본이다 - 거기를 고쳐도 실행 중인 AS-IS에 반영 안 되고,
다른 세션들이 verbatim 대조 원본으로 읽으므로 **수정 금지**(2026-10-13 실수로 고쳤다가 원복).

**재기동** (사용자가 요청할 때만, [[build-is-mine-restart-is-users]]):
- 공식 스크립트 `C:\workspace\ghlove\scripts\start-all.bat`(stop-all → 공통 빌드 → `_run-web/_run-api/_run-frontend.bat` 새 창).
  도구에서 Start-Process로 돌리면 stop만 되고 새 창이 안 떠서 **세 서버가 다 내려간 채** 끝난다 →
  대신 같은 명령을 백그라운드로 직접: `gradlew.bat -Dspring.profiles.active=local -x :ghlove-common:copyApplicationYml -x :ghlove-common:processResources :ghlove-web:bootRun`
  (api는 `--init-script scripts/enable-api-bootrun.init.gradle ... :ghlove-api:bootRun`, frontend는 `ghlove-frontend`에서 `node index.js`).
- 빌드가 `Failed to create Jar file ...spring-boot-gradle-plugin-2.7.0.jar` / "다른 프로세스가 파일을 사용 중"이면
  오래 떠 있던 Gradle 7.4.1 데몬이 jar를 잡고 있는 것 → `gradlew.bat --stop` 후 재시도.
- JSP 수정은 bootRun이 소스 webapp을 그대로 서빙하지만, Java(컨트롤러) 수정은 재기동해야 반영된다.

**로컬 테스트용 우회(2026-10-13, 사용자 요청)**: 기탁서 등록의 실명인증(`offgive/sci-call.jsp`)과
행정주소검색(`OffgiveManagerController.getUserAddressInfo/getUserAddressInfoForeign`)에 `localhost` 접근일 때만
항상 성공하는 분기(경기도 41000/시흥시 41390, CI=`MOCKCI`+주민번호)를 넣어 뒀다. TO-BE 우회도 같은 값이다.

**opframework 소스가 없을 때**: `C:\workspace\ghlove\libs\opframework-3.15.0.jar`를 `javap -J-Dfile.encoding=UTF-8 -p -c -constants`로
풀면 AbstractSXSSFExcelView 등 프레임워크 동작을 바이트코드로 정확히 확인할 수 있다(→ admin `AsisSxssfExcelView`).
