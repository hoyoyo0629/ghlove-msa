# 로컬 실행·로그 확인 가이드 (VSCode)

고향사랑e음 MSA 로컬 개발환경(백엔드 6종 + storefront + docker 인프라)을 VSCode에서 띄우고
로그를 확인하는 방법. Windows / PowerShell 기준.

## 구성 요소

| 구분 | 이름 | 포트 | 실행 방식 |
|---|---|---|---|
| 백엔드 | member | 8081 | `gradlew.bat bootRun` |
| 백엔드 | donation | 8082 | `gradlew.bat bootRun` |
| 백엔드 | point | 8083 | `gradlew.bat bootRun` |
| 백엔드 | gift | 8084 | `gradlew.bat bootRun` |
| 백엔드 | order | 8085 | `gradlew.bat bootRun` |
| 백엔드 | admin | 8086 | `gradlew.bat bootRun` |
| 프론트 | storefront | 5173 | `npm run dev` (vite) |
| 인프라 | ghlove-postgres | 15432→5432 | docker |
| 인프라 | ghlove-kafka | 9092 | docker |

## 사전 준비 (최초 1회)

- **JDK 17** — `.vscode/settings.json`에 경로 지정됨(`C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot`).
- **Node.js** — storefront 의존성 설치: `cd storefront; npm install`
- **docker 인프라 기동** — DB/Kafka가 먼저 떠 있어야 백엔드가 정상 부팅됨:
  ```powershell
  docker start ghlove-postgres ghlove-kafka
  ```

---

## 방법 A. VSCode 태스크 (권장 · 개별 터미널에 실시간 로그)

`Ctrl+Shift+P` → **Tasks: Run Task** → 아래 중 선택. (`.vscode/tasks.json`에 정의)

| Task | 동작 |
|---|---|
| **전체 기동 (BE+FE, 개별 터미널)** | 백엔드 6종 + storefront 를 각각 전용 터미널에 기동. 각 터미널에 로그가 실시간으로 흐름 |
| **BE: 전체 기동 (개별 터미널)** | 백엔드 6종만 |
| **BE: member (8081)** … 서비스별 | 특정 서비스 하나만 |
| **FE: storefront (5173)** | 프론트만 |
| **상태 확인** | 각 포트/도커 기동 여부 표 출력 |
| **BE: 전체 중지** | 백엔드 6종 종료 |

- **로그 보기**: 터미널 패널 우측 드롭다운에서 서비스별 터미널을 선택하면 그 서비스 로그가 보임.
- **중지**: 해당 터미널을 클릭하고 `Ctrl+C`, 또는 휴지통 아이콘. 전체는 **BE: 전체 중지** 태스크.
- 개별 터미널 방식은 서비스가 6~7개 열려 무겁다. 그냥 띄워만 둘 거면 아래 **방법 C**가 가볍다.

## 방법 B. 디버깅(브레이크포인트) — Run and Debug

브레이크포인트가 필요할 때만.

1. 좌측 **Run and Debug**(Ctrl+Shift+D) → **Java** 확장이 각 `*Application.java` 를 자동 인식.
2. 실행할 서비스의 `main()` 위 ▶(Run|Debug) 코드렌즈 클릭, 또는 **Spring Boot Dashboard** 확장에서 서비스 선택 후 debug.
3. 로그는 **DEBUG CONSOLE**에 출력, 브레이크포인트에서 멈춤.
> 6개를 동시에 디버그하면 무거우니, 보통은 방법 A/C로 다 띄워두고 **파고들 서비스 1개만** 디버그로 재기동한다.

---

## 방법 C. 스크립트 일괄 기동/중지 (백그라운드 · 로그는 파일로)

창 없이 백그라운드로 띄우고 로그는 `logs\<서비스>.log` 로 모은다. `scripts\` 아래.

> **먼저 읽기 — `.ps1` 이 "running scripts is disabled" 로 안 열릴 때**
> Windows 기본 실행정책(Restricted)에서는 `.\dev-all.ps1` 이 막힌다. 두 가지 중 택1:
> - **(권장, 정책 안 바꿈)** 같은 폴더의 `.cmd` 래퍼를 쓴다 — 내부에서 `-ExecutionPolicy Bypass` 로 감싸 호출하므로 정책과 무관하게 실행됨:
>   `.\scripts\dev-all.cmd -Front`, `.\scripts\dev-status.cmd`, `.\scripts\dev-stop.cmd`, `.\scripts\dev-logs.cmd member`
> - **(한 번만 정책 허용)** `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` 실행 후에는 `.ps1` 을 직접 호출 가능.
> 아래 예시는 `.ps1` 기준이며, `.cmd` 로 바꿔도 인자·동작 동일하다.

```powershell
# 기동
.\scripts\dev-all.ps1                # 백엔드 6종
.\scripts\dev-all.ps1 -Front         # + storefront(5173)
.\scripts\dev-all.ps1 -Infra -Front  # docker(postgres/kafka)까지 먼저 + 백엔드 + 프론트
.\scripts\dev-all.ps1 -Only member,donation   # 지정 서비스만

# 상태
.\scripts\dev-status.ps1

# 로그 실시간 보기 (tail -f). Ctrl+C 로 로그만 빠져나옴(서비스는 계속 떠 있음)
.\scripts\dev-logs.ps1 member
.\scripts\dev-logs.ps1 donation -Tail 200

# 중지
.\scripts\dev-stop.ps1               # 백엔드 6종
.\scripts\dev-stop.ps1 -Front        # + storefront
.\scripts\dev-stop.ps1 -Daemon       # + gradle 데몬까지 정리
```

- 이미 포트가 떠 있는 서비스는 **기동 시 자동으로 건너뜀**(중복 실행 방지).
- 중지는 해당 포트를 잡고 있는 프로세스를 트리째 종료(`taskkill /T /F`).
- 로그 파일은 `.gitignore`(`*.log`)로 커밋에서 제외됨.

> ⚠️ 부팅 중(20~40초, 포트가 아직 안 열린 상태)에 `dev-all` 을 또 실행하면 같은 서비스가 중복 기동될 수 있다. `dev-status` 로 UP 확인 후 다시 실행.

---

## 상태·헬스 확인

```powershell
.\scripts\dev-status.ps1                     # 포트/도커 요약표
netstat -ano | findstr "8081 8082 8083 8084 8085 8086 5173"   # 원시 확인
```

- storefront: http://localhost:5173/
- 각 백엔드는 브라우저보다 storefront/admin을 통해 확인. 직접 확인이 필요하면 서비스별 API(예: `http://localhost:8081/api/home`).

---

## SQL 쿼리 로그 — 파라미터 인라인 (P6Spy)

기능 테스트하며 "실제로 어떤 값으로 쿼리가 나갔는지" 보려고 6개 백엔드 전부에 **P6Spy** 를 끼워 뒀다.
Hibernate 기본 로그는 `... where login_id=?` 처럼 `?` 로만 찍히고 값은 별도 줄에 나오는데, P6Spy 는
**값이 박힌, 복붙해서 바로 실행 가능한 SQL 한 줄**로 찍는다.

로그 형태:
```
[nio-8081-exec-1] p6spy : 0ms | select ... from op_user u1_0 where u1_0.login_id='testuser01'
[nio-8081-exec-1] p6spy : 1ms | insert into op_user_login_log (...) values ('20260917140719','testuser01','100',...)
```
(`실행시간ms | 인라인SQL` 형식. 값이 비어 있는 줄은 commit 등 SQL 없는 이벤트)

**설정 위치** (이미 적용돼 있음):
- `<서비스>/build.gradle` : `developmentOnly 'com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.10.0'`
  → `developmentOnly` 라 `bootRun`/IDE 실행에만 붙고 **운영 bootJar 에는 P6Spy 가 포함되지 않는다.**
- `<서비스>/application.yml` :
  ```yaml
  decorator:
    datasource:
      p6spy:
        enable-logging: true
        logging: slf4j
        multiline: false
        log-format: "%(executionTime)ms | %(sqlSingleLine)"
  logging:
    level:
      p6spy: DEBUG           # ← 이 로거를 끄면(WARN) P6Spy 로그가 사라진다
      # org.hibernate.SQL: DEBUG           # P6Spy 대신 순수 Hibernate(? + 바인딩 별도줄) 로그를 볼 땐 이 둘 주석 해제
      # org.hibernate.orm.jdbc.bind: TRACE
  ```

**끄기/전환**: `logging.level.p6spy` 를 `WARN` 으로 바꾸면 인라인 SQL 로그가 꺼진다. 순수 Hibernate 로그가 필요하면
아래 두 `org.hibernate.*` 주석을 해제하면 된다(둘 다 켜면 중복 출력).

> ⚠️ **로컬 개발 전용.** 인라인 SQL 에는 비밀번호 해시·토큰·개인정보가 **평문**으로 남는다. 운영 프로파일에는 넣지 말 것.
> 반영하려면 서비스 재기동 필요(설정은 기동 시 로드).

## 업무예외 로그 (틀린 비번·검증 실패 등)

앱이 catch 해서 처리하는 **업무예외**(로그인 실패, 검증 실패, 업무규칙 위반 등)도 로그에서 보이도록,
각 서비스의 도메인 예외(`MemberException`·`DonationException`·`PointException`·`GiftException`·
`OrderException`/`ClaimException`·`ExternalAuthException`, admin 의 `ManagerException` 외 7종)는
**생성자에서 자동으로 WARN 로그**를 남긴다.

```
WARN ... c.ghlove.member.service.MemberException : [업무예외] MemberException: 아이디 또는 비밀번호가 올바르지 않습니다.
```

- 이 예외들은 컨트롤러 인라인 `try/catch` 에서 에러응답(200/400 등)으로 바뀌며 삼켜지지만, throw 시점에 로그가 남으므로 추적 가능하다.
- `[업무예외]` 태그로 grep 하면 업무 실패만 모아볼 수 있다:
  ```bash
  ./scripts/dev-logs.sh member | grep "업무예외"
  ```
- **로직 구현 규칙**: 새 업무 실패는 해당 서비스의 `XxxException` 을 throw 하면 자동으로 로그에 남는다.
  예외가 아니라 **결과객체/플래그로 실패를 표현하는 경로**는 로그에 안 남으므로, 그 지점엔 서비스에서 직접 `log.warn(...)` 을 넣어야 한다.

## 로그 파일 인코딩 (한글)

로그 파일(`logs\<서비스>.log`)은 Windows 콘솔 코드페이지(**CP949**)로 기록된다. 어디서 보느냐에 따라:

- **PowerShell** — 변환 없이 그대로 정상. `.\scripts\dev-logs.ps1 member` 또는 `Get-Content logs\member.log -Wait -Tail 50`.
- **Git Bash** — UTF-8 로 읽어서 한글이 깨진다. CP949→UTF-8 변환이 필요:
  ```bash
  ./scripts/dev-logs.sh member            # tail -f + iconv 자동 변환 (권장)
  tail -f logs/member.log | iconv -f CP949 -t UTF-8 -c    # 수동
  ```
- **VSCode 로 .log 열기** — 우하단 인코딩을 "Korean (Windows 949 / EUC-KR)" 로 열면 정상.

## 자주 겪는 문제

- **`.ps1` 실행이 "running scripts is disabled on this system" 로 막힘** — Windows 기본 실행정책(Restricted). `.cmd` 래퍼(`dev-all.cmd` 등)를 쓰거나, `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` 를 한 번 실행. (방법 C 상단 안내 참고)
- **`gradlew.bat` 인식 안 됨 / JAVA_HOME** — VSCode 통합 터미널은 settings.json의 JDK를 쓰지만, 외부 PowerShell에서 실행 시 `JAVA_HOME`이 JDK17을 가리키는지 확인. (스크립트는 cmd 의 현재 디렉터리 미검색 이슈를 피하려고 `.\gradlew.bat` 로 호출한다)
- **포트가 이미 사용 중(BindException)** — `.\scripts\dev-status.ps1` 로 UP인지 확인하고, 좀비 프로세스면 `.\scripts\dev-stop.ps1`.
- **백엔드가 뜨자마자 죽음** — 대개 DB/Kafka 미기동. `docker start ghlove-postgres ghlove-kafka` 후 재기동. 원인은 `logs\<서비스>.log` 상단 스택트레이스 확인.
- **스크립트 한글 깨짐** — `scripts\*.ps1` 은 UTF-8(BOM) 로 저장돼야 Windows PowerShell 5.1에서 정상 실행됨(이미 그렇게 저장돼 있음).
- **로그의 한글이 Git Bash 에서 깨짐** — 로그는 CP949 로 기록된다. `./scripts/dev-logs.sh <서비스>` 로 보거나 `iconv -f CP949 -t UTF-8` 로 변환(위 "로그 파일 인코딩" 참고). PowerShell 에서는 그냥 봐도 정상.
