---
name: build-is-mine-restart-is-users
description: "빌드(bootJar까지)는 내가 수행, 재기동만 사용자. compileJava만 돌리고 끝내지 말 것"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T00:15:01.237Z
---

**코드 수정 후 빌드는 내가 끝까지 한다 — `compileJava`만이 아니라 실행 jar를 굽는 `bootJar`(또는 `build`)까지.** 재기동(서비스 restart)은 기본적으로 사용자가 한다(2026-10-01 명시: "빌드는 니가 해줘. 앞으로도." / "재기동은 내가 할께").

**[2026-10-03 보완] 재기동은 "사용자가 요청할 때만" 내가 한다.** 사무실에 있을 때는 원래대로 사용자가 직접 하고, 사무실 밖이라 못 할 때 요청하면 내가 한다("지금은 내가 사무실이 아니니까 할수가 없으니 내가 요청할때만 해줘" / "사무실에선 원래대로 내가 할께"). **먼저 알아서 재기동하지 말 것.**

**Why:** 재기동은 `build/libs/*.jar`를 읽는다. `compileJava`는 `build/classes`만 갱신하고 jar는 그대로라, 컴파일만 하고 "빌드 OK"라 하면 사용자가 재기동해도 옛 jar가 떠서 변경이 반영 안 된다(2026-10-01 지정기부 월별통계: compileeJava만 해서 차트가 안 뜸 → jar 재빌드로 해결). [[commit-only-when-asked]]와 별개 축.

**★[2026-10-06 보완] `bootJar` EXIT=0은 "기동된다"는 증거가 아니다 — 여분 포트로 띄워 확인한다.**
스프링 빈 구성과 `@Query` HQL은 **컴파일이 아니라 기동 시점에** 검증되므로, 빌드만 하고 넘기면
사용자가 재기동할 때 처음 터진다. 실제로 2026-10-05에 `DesignatedAdminService`에 `DonationService`를
주입해 **순환참조**(DonationService→DesignatedAdminService→DonationService)를 만들었는데 bootJar는
EXIT=0이었고, 다음 날 아침 사용자가 재기동하자 **donation이 기동 실패**했다(APPLICATION FAILED TO START).
→ 해결: 코드표 조회는 `CommonCodeRepository`를 직접 주입(서비스 간 공유 모듈이 없어 같은 조회를 중복 보유).

검증 방법(사용자 인스턴스는 건드리지 않는다):
`java -jar build/libs/<svc>-0.0.1-SNAPSHOT.jar --server.port=<여분포트>` 를 띄워
`Started XxxApplication`을 확인하고 바로 내린다. 내부 API는
`X-Internal-Secret`(각 서비스 application.yml `ghlove.internal.admin-secret`) 헤더로 직접 호출해
런타임 동작까지 볼 수 있다. **선결 확인 2가지**: 해당 서비스에 지금 발화할 `@Scheduled`가 없는지,
`ddl-auto`가 `none`인지(둘 다 충족하면 읽기전용 기동이라 부작용이 없다).
Git Bash에서 한글 쿼리파라미터는 CP949로 나갈 수 있으니 **UTF-8 퍼센트인코딩으로 직접** 보낼 것.

**How to apply:**
- 서비스별 자기 gradlew로: `Set-Location C:\workspace\ghlove-msa\<svc>; .\gradlew.bat -q bootJar` (EXIT=0 확인). 각 서비스는 자기 gradlew 사용.
- 끝나면 `build/libs/<svc>-0.0.1-SNAPSHOT.jar` 시각이 소스 수정 시각 이후인지 확인하고, 변경 산출물(템플릿/정적에셋/클래스)이 jar에 포함됐는지 `unzip -l`로 점검.
- 기본은 "재기동하시면 반영됩니다"로 넘긴다. **요청받았을 때만** 내가 재기동한다.
- 재기동 방법: 포트별로 내리고(`8081 member / 8082 donation / 8083 point / 8084 gift / 8085 order / 8086 admin`, `taskkill /PID <pid> /T /F`) 서비스 디렉터리에서 `./gradlew.bat bootRun`을 백그라운드로 올린다(`bootRun`은 소스에서 다시 컴파일하므로 jar와 무관하게 반영된다). 변경한 서비스만 선택적으로 올릴 것.
- **주의: `scripts/dev-all.ps1` 실행은 권한 분류기가 막는다**(Interfere With Workloads). 서비스별 `gradlew.bat bootRun`을 `run_in_background`로 직접 띄우면 통과한다(그마저도 간헐적으로 막히면 Bash↔PowerShell 도구를 바꿔 재시도). 상태확인 `scripts/dev-status.ps1`은 통과한다.
- 내릴 때는 반드시 올리기까지 마칠 것 - 중간에 멈추면 개발환경이 내려간 채로 남는다.
