---
name: spare-stack-needs-kafka-member-donation
description: admin 화면을 스페어 포트로 실검증할 때 필요한 스택 - gift+donation+member 스페어, 로컬 Kafka 필수(없으면 gift 변경요청이 60초 블록되며 행잠금·커넥션풀 고갈), DB는 원격이라 docker run postgres 클라이언트
metadata:
  type: reference
---

2026-10-19(재부팅 후 전부 DOWN 상태에서) 답례품관리 검증하며 확인한 것. 기존 [[build-is-mine-restart-is-users]]의 스페어 포트 절차를 admin 화면까지 확장한 레시피.

- **DB는 원격 `10.0.110.37:15432`**(docs/local-run-this-pc.md). `docker exec ghlove-postgres …`(옛 마이그레이션 SQL 머리말)는 이 PC에 컨테이너가 없어 안 된다. 대신
  `docker run --rm -e PGPASSWORD=<서비스 application.yml의 password> postgres:16-alpine psql -h <DB 호스트> -p 15432 -U <gift|admindb|member> -d <gift|ghlove_core> -At -c "…"` (Git Bash에선 `MSYS_NO_PATHCONV=1`). psql이 PC에 없다. 호스트·계정·비밀번호는 각 서비스 `config/application.yml`·`application.yml`에서 읽고 메모리에는 적지 않는다.
- **admin 화면 하나를 보려면 스페어 4개**: gift 18084 + donation 18082(지자체 목록 `/api/locgovs`) + member 18081(로그인 EMPTY_CI 판정) + admin 18086, admin에는
  `--ghlove.gift-service.base-url=… --ghlove.donation-service.base-url=… --ghlove.member-service.base-url=…`로 스페어를 가리키게 한다. member를 안 띄우면 pwtest 계정도 로그인 직후 `EMPTY_CI`로 대민 5173(mypage/profile)에 튕긴다.
  전부 `--spring.kafka.listener.auto-startup=false`. 각 모듈 폴더에서 `java -jar build/libs/<svc>-0.0.1-SNAPSHOT.jar`(config/application.yml 덮어쓰기가 cwd 기준).
- **★로컬 Kafka(`infra/kafka-local`)를 먼저 올릴 것**: 안 올리고 gift 변경 API(공개/품절/삭제 등 `publish()`로 이벤트 발행)를 치면 KafkaTemplate.send가 `max.block.ms=60000`만큼 블록되고, 그동안 트랜잭션이 행 잠금을 쥐어
  다음 요청이 `transactionid` 락 대기 → 커넥션풀(total=3) 고갈 → 이후 요청 전부 타임아웃. 처음엔 DB 이상으로 오해했다(pg_stat_activity에서 `idle in transaction`+Lock 대기로 확인).
- **실행 중인 스페어의 jar를 `bootJar`로 덮어쓰지 말 것**: donation 스페어가 "Error setting socket options"로 망가졌다. 빌드 전에 스페어를 내릴 것.
- Playwright: PC에 playwright 모듈이 없어 스크래치패드에 `playwright-core`를 설치하고 캐시된 Chromium(`~/AppData/Local/ms-playwright/chromium-1248/chrome-win64/chrome.exe`)을 `executablePath`로 쓴다. 로그인은 `#op_username`·`#op_password` 채우고 `idLogin()` 호출(개발 프로파일은 devCode 자동입력), 계정은 [[admin-pwtest-manager-accounts]].
- 끝나면 `Get-CimInstance Win32_Process`로 `server.port=1xxxx` 잔여 확인. Docker Desktop과 Kafka 컨테이너도 재부팅 뒤엔 꺼져 있다(컨테이너는 `ghlove-cubrid`만 자동 기동).
