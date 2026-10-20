---
name: tooling-and-local-env
description: 도구·로컬 환경 메모 - perl 한글 이중인코딩·AskUserQuestion 한글 그대로·Playwright SmartEditor submit·VS Code JDK 경로·내 PC 설정 푸시 금지(skip-worktree/exclude)·개발DB 교차 권한·스페어 스택(Kafka)·admin 테스트 계정 pwtest
metadata:
  type: reference
---

도구 사용과 로컬 개발 환경에서 알아둘 것.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## perl-double-encoding-when-generating-files

> perl/스크립트로 한글 포함 파일을 생성할 때 @ARGV와 소스 리터럴은 바이트라서 :encoding(UTF-8) 핸들에 쓰면 이중 인코딩된다. 생성 후 반드시 눈으로 확인


**2026-10-06 docs 통합에서 한글이 깨졌고 사용자가 발견했다**("통합한 .md 파일들 중 일부는
한글 인코딩이 깨진거 같은데..?"). 내가 만든 파일을 **열어보지 않고** 완료로 보고한 탓이다.

**Why:** perl에서 `:encoding(UTF-8)` 출력 핸들에 쓰면 "문자열"을 UTF-8로 인코딩한다. 그런데
① `@ARGV` 로 들어온 값과 ② **스크립트 소스에 박힌 한글 리터럴**은 기본적으로 디코딩되지 않은
**바이트**다. 바이트를 문자로 취급해 한 번 더 인코딩하므로 `0xEC` → `0xC3 0xAC` 가 되어
"목차" → "ëª©ì°¨" 가 된다. 파일 본문은 `:encoding(UTF-8)` 로 **읽어서** 제대로 디코딩됐기 때문에
**제목·머리말·섹션명만 깨지고 본문은 정상**이라 눈에 덜 띄었다.

**How to apply:**
- 출력 핸들에 `:encoding(UTF-8)` 을 쓰면 **`use utf8;`(소스 리터럴) + `decode_utf8(@ARGV)`
  (인자) 둘 다** 해야 한다. 하나만 하면 나머지가 깨진다.
- 더 간단한 대안: 인코딩 레이어를 **아예 쓰지 않고** 바이트 그대로 읽고 쓴다
  (`perl -i -pe` 치환은 이쪽이라 안전했다). 섞어 쓰는 게 사고의 원인이다.
- **생성 직후 검증 2단계**: ① `iconv -f UTF-8 -t UTF-8 <파일>` 로 유효성 ②
  `grep -lE "ì|ë|í|ê|â"` 로 모지바케 흔적. 둘 다 통과해야 "됐다"고 말한다.
  U+FFFD 검사만으로는 **안 잡힌다**(이중 인코딩은 유효한 UTF-8이다 - 내가 처음 이걸로
  "정상"이라고 오판했다).
- 더 일반적으로: **파일을 생성·변환했으면 결과물을 열어보고 보고한다.** 같은 유형의 과거 실패가
  이미 있었다(`flashify.pl` 이 복사본을 수정해 파일에 반영되지 않았는데 "40곳 변환"이라고
  출력한 건). 스크립트의 **출력 메시지를 결과로 신뢰하지 말 것.**

## askuserquestion-korean-plain-text-not-escaped

> AskUserQuestion(및 다른 도구 호출)의 한글은 \\uXXXX 이스케이프로 손수 변환하지 말고 그냥 UTF-8 원문을 그대로 쓸 것 - 수동 변환 중 오타로 \"어뙙게\"/\"바맔 모릀\" 같은 깨진 글자가 반복 노출됨


AskUserQuestion(그리고 다른 모든 도구 호출)의 한글 파라미터는 **절대 `\uXXXX` 유니코드
이스케이프로 손수 변환하지 말 것** - 그냥 UTF-8 원문 한글을 그대로 써넣는다(Write/Edit/Bash
등 다른 모든 도구 호출에서 이미 그렇게 하고 있던 것과 동일하게).

**Why:** 과거 한 번 AskUserQuestion 호출에서 JSON 파싱 에러가 난 적이 있었는데, 그 원인을
"한글을 직접 쓰면 안 된다"로 잘못 판단해 한글을 `\uXXXX` 코드로 손수 계산해 써넣는 방식으로
바꿨다. 그런데 16진수 코드를 외워서 손으로 치다 보니 계속 오타가 났고, 그 결과
"어떻게"→"어뙙게", "방식"→"바맔", "모듈"→"모릀" 같은 **깨진 한글이 Question 창에 그대로
노출**되는 일이 여러 번 반복됐다. 사용자가 "며칠 전부터 왜 이러냐"고 직접 지적
(2026-10-12) - 실제 원인은 플랫폼 버그가 아니라 **내가 손으로 이스케이프를 잘못 계산한
자충수**였다. 애초에 이스케이프 자체가 불필요했다 - 도구는 평범한 UTF-8 문자열을 그대로
받는다.

**How to apply:** AskUserQuestion(그 외 어떤 도구든) 파라미터에 한글이 들어갈 때 `\u` 형태의
수동 이스케이프를 절대 쓰지 않는다. 혹시 어떤 호출에서 실제로 JSON 파싱 에러가 다시 나면,
원인을 한글 자체로 단정하지 말고 괄호/인용부호 등 다른 특수문자나 구조적 문제부터 확인한다.

## playwright-smarteditor-submit-quirk

> Playwright로 SmartEditor2(iframe) 폼을 실제 버튼클릭으로 submit하면 세션을 잃은 것처럼 /admin/login으로 튕긴다 - 이미 배포된 faq-admin/create에서도 재현되는 공통 인프라 현상


admin의 `fragments/smarteditor :: init/editor(id)`를 쓰는 등록·수정 화면에서 Playwright로
실제 제출버튼을 클릭하면(iframe 안에 타이핑해 넣었든 아니든) 서버 응답이 `/admin/login`으로
리다이렉트된다. 이미 검증 완료된 `faq-admin/create`로 교차 재현해 확인했다 - **화면별 버그가
아니라 SmartEditor2(2010년대 iframe 기반 에디터) + 레거시 `.validator()` 플러그인 + 헤드리스
Chromium 상호작용에서 생기는 공통 현상**이다.

**Why:** `Common.getEditorContent(id)`는 `editors.getById[id].exec("UPDATE_CONTENTS_FIELD", [])`를
호출하는데, 헤드리스/자동화 환경에서 에디터 인스턴스가 완전히 붙지 않은 채로 제출 이벤트가
발생하면 이 호출이 예외를 던지는 것으로 보인다. 실제 사람이 쓰는 브라우저에서 문제가 되는지는
확인하지 않았다(가능성 낮음 - 운영 화면이라 가정).

**How to apply:** 화면 로직 자체를 검증할 때는 버튼 클릭 대신 폼을 **직접 submit()**한다:
```js
await page.evaluate((content) => {
  document.getElementById('content').value = content;   // 숨은 textarea에 직접 채움
  document.getElementById('notice').submit();            // <form id="notice"> 직접 제출
}, '본문');
```
이렇게 하면 서버 라우팅·검증·저장 로직은 정확히 검증되고(302→200 확인됨), 제출 버튼
클릭이라는 자동화 전용 변수만 피하게 된다. 에디터 자체의 렌더링(툴바·사진/동영상 버튼)은
스크린샷으로 별도 확인하면 된다 - 단, 페이지 로드 직후 스크린샷을 찍으면 iframe이 서버가
내려준 초기 textarea 값을 아직 안 그렸을 수 있다(바인딩 자체는 `page.$eval('#content', el
=> el.value)`로 textarea 원시값을 읽어 확인할 것 - 이건 Thymeleaf 서버 렌더링이라 즉시 정확하다).

부수 발견: 같은 화면에서 `/ban-word/list-all` 404가 콘솔에 찍힌다(금칙어 조회 엔드포인트가
TO-BE에 없음) - smarteditor/validator 공용 자산이 참조하는 것으로 보이며 제출 자체를 막지는
않는다. 전 화면 공통 갭이라 이 메모와 별개로 다룰 것.

## vscode-java-jdk-path-breaks-on-update

> .vscode/settings.json 에 Temurin JDK 절대경로가 박혀 있어, JDK 패치 때마다 폴더명이 바뀌며 'Invalid runtime for JavaSE-17' 경고가 뜬다


`.vscode/settings.json` 의 `java.configuration.runtimes[].path` 와
`java.import.gradle.java.home` 에 Temurin JDK **절대경로**가 박혀 있다.

Temurin 은 패치될 때마다 설치 폴더 이름이 바뀌고 **옛 폴더는 지워진다**:
`jdk-17.0.20.8-hotspot` (17.0.20+8) → `jdk-17.0.20.101-hotspot` (17.0.20.1+1)

그러면 Red Hat Java 확장이
`Invalid runtime for JavaSE-17: The path points to a missing or inaccessible folder`
를 띄우고 **에디터 코드분석만** 죽는다.

**빌드는 멀쩡하다.** Gradle 은 `JAVA_HOME` 을 쓰므로 터미널 빌드/`bootJar` 는 영향 없다.
즉 이 경고는 빌드 실패 신호가 아니다.

### 고치는 법

1. `ls "C:\Program Files\Eclipse Adoptium"` 로 실제 폴더명 확인 (= `JAVA_HOME` 과 일치)
2. settings.json 의 두 줄을 그 경로로 교체
3. VS Code `Ctrl+Shift+P` → **Java: Clean Java Language Server Workspace** → *Restart and delete*
   (단순 새로고침은 실패 캐시가 남아 경고가 재발)

이 파일은 공유 파일이라 수정분을 푸시하면 안 된다 → [[tooling-and-local-env]]

근본 해결책(미적용): 두 줄을 아예 지우면 확장이 `JAVA_HOME` 을 자동 탐지해
업데이트에 영향받지 않는다. 명시 설정 유지를 택해 지금은 경로만 갱신해 둠.

## local-env-changes-never-pushed

> 내 PC 전용 설정 변경은 절대 푸시하지 않는다. 추적 파일은 skip-worktree, 무시 규칙은 .git/info/exclude. 현재 .vscode/settings.json·.claude/settings.local.json 이 skip-worktree 상태


이 저장소는 **다른 사람과 공유**한다(origin: github.com/hoyoyo0629/ghlove-msa).
내 PC 환경에 맞추려고 바꾼 것은 저장소에 올라가면 안 된다.

### 두 가지 수단을 구분해서 쓴다

| 대상 | 수단 |
|---|---|
| 이미 추적 중인 공유 파일인데 내 값만 다름 | `git update-index --skip-worktree <file>` |
| 내 PC에만 있는 폴더/파일을 무시 | `.git/info/exclude` (공유 `.gitignore` 아님) |

`git rm --cached` 는 **쓰지 말 것** — 푸시하면 다른 사람 저장소에서 그 파일이 삭제된다.

### 현재 적용된 것

- `.vscode/settings.json` — skip-worktree. 작업트리는 JDK `jdk-17.0.20.101-hotspot`,
  저장소(HEAD)는 `jdk-17.0.20.8-hotspot` 인 채로 둔다. [[tooling-and-local-env]]
- `.claude/settings.local.json` — skip-worktree(2026-10-12 적용). **이름은 local 인데
  `be67f74 Initial commit` 에 딸려 들어가 추적되고 있었다**(무시 규칙도 없었음). 내용이
  PC별 허용목록(내 PID·내 JDK 경로)이라 고치면 동료 것을 덮어쓴다. 저장소 쪽은
  `.8` 경로와 죽은 항목(특정 PID taskkill 등)이 남은 채로 둔다.
- `.git/info/exclude` — `/doc/`(로컬 참고문서), `/infra/kafka-local/`, `/point/gradle.properties`

확인: `git ls-files -v | findstr "^S"` 가 skip-worktree 목록.

### 함정

skip-worktree 가 걸린 파일은 **다른 사람이 그 파일을 고쳐 올리면 `git pull` 이 거부된다**
(`Your local changes would be overwritten`). 그때만 임시로 푼다:
`--no-skip-worktree` → `git stash` → `pull` → `stash pop` → 다시 `--skip-worktree`.

또한 내 수정이 `git status` 에 안 보이므로, 그 파일을 또 고칠 일이 생기면
"왜 안 올라가지" 로 헤매기 쉽다. 고치기 전에 skip-worktree 목록을 먼저 본다.

`.claude/settings.local.json` 은 **내가 못 고친다** — Bash 쓰기든 편집 도구든 자동 승인
모드가 Self-Modification 으로 막는다(내 권한 허용목록이라서). 내용 정리가 필요하면
바꿀 JSON 전문을 사용자에게 드리고 직접 붙여넣거나 `/permissions` 로 편집하시게 한다.
git 인덱스 조작(`update-index --skip-worktree`)은 막히지 않는다.

## dev-db-cross-schema-grants

> 개발 postgres에서 6개 앱 롤에 두 DB(ghlove_core 5스키마+gift) 교차 SELECT 권한 부여 — DBeaver 재로그인 제거용. 조회전용, 볼륨 초기화 시 재적용


**2026-09-23, 사용자 요청("개발서버니 계정별로 권한 부여해 재로그인 없이 조회·수정")으로 부여.**

- 대상 롤: member·donation·point·orderdb·admindb·gift (전부).
- **ghlove_core**(스키마 admin·donation·member·ord·point·public): 위 롤 전부에 `USAGE, CREATE + ALL(테이블·시퀀스)` + `ALTER DEFAULT PRIVILEGES FOR ROLE postgres`로 향후 테이블 자동 상속. gift 롤엔 `CONNECT`도 신규 부여.
- **gift**(별도 DB, public 93테이블, 소유 gift): 위 롤 전부에 `CONNECT + USAGE, CREATE + ALL` + default privileges(FOR ROLE gift).
- 각 롤 `search_path`를 전 스키마로 세팅(ALTER ROLE ... IN DATABASE ...).
- **2026-09-23 조회전용→ALL로 확장**: 이제 아무 계정으로도 교차 스키마 INSERT/UPDATE/DELETE/DDL 가능(테스트 편의). 운영 절대 금지.
- gift는 물리적 별도 DB라 교차 DB 쿼리 불가(DBeaver 커넥션은 DB별로 따로), 단 같은 계정으로 둘 다 접속 가능.
- 재현 스크립트: `database/ddl/grant-dev-cross-schema-read.sql`. **컨테이너 데이터볼륨 초기화 시 재실행 필요**(카탈로그 변경이라 재기동엔 영향 없음).

## spare-stack-needs-kafka-member-donation

> admin 화면을 스페어 포트로 실검증할 때 필요한 스택 - gift+donation+member 스페어, 로컬 Kafka 필수(없으면 gift 변경요청이 60초 블록되며 행잠금·커넥션풀 고갈), DB는 원격이라 docker run postgres 클라이언트


2026-10-19(재부팅 후 전부 DOWN 상태에서) 답례품관리 검증하며 확인한 것. 기존 [[build-is-mine-restart-is-users]]의 스페어 포트 절차를 admin 화면까지 확장한 레시피.

- **DB는 원격 `10.0.110.37:15432`**(docs/local-run-this-pc.md). `docker exec ghlove-postgres …`(옛 마이그레이션 SQL 머리말)는 이 PC에 컨테이너가 없어 안 된다. 대신
  `docker run --rm -e PGPASSWORD=<서비스 application.yml의 password> postgres:16-alpine psql -h <DB 호스트> -p 15432 -U <gift|admindb|member> -d <gift|ghlove_core> -At -c "…"` (Git Bash에선 `MSYS_NO_PATHCONV=1`). psql이 PC에 없다. 호스트·계정·비밀번호는 각 서비스 `config/application.yml`·`application.yml`에서 읽고 메모리에는 적지 않는다.
- **admin 화면 하나를 보려면 스페어 4개**: gift 18084 + donation 18082(지자체 목록 `/api/locgovs`) + member 18081(로그인 EMPTY_CI 판정) + admin 18086, admin에는
  `--ghlove.gift-service.base-url=… --ghlove.donation-service.base-url=… --ghlove.member-service.base-url=…`로 스페어를 가리키게 한다. member를 안 띄우면 pwtest 계정도 로그인 직후 `EMPTY_CI`로 대민 5173(mypage/profile)에 튕긴다.
  전부 `--spring.kafka.listener.auto-startup=false`. 각 모듈 폴더에서 `java -jar build/libs/<svc>-0.0.1-SNAPSHOT.jar`(config/application.yml 덮어쓰기가 cwd 기준).
- **★로컬 Kafka(`infra/kafka-local`)를 먼저 올릴 것**: 안 올리고 gift 변경 API(공개/품절/삭제 등 `publish()`로 이벤트 발행)를 치면 KafkaTemplate.send가 `max.block.ms=60000`만큼 블록되고, 그동안 트랜잭션이 행 잠금을 쥐어
  다음 요청이 `transactionid` 락 대기 → 커넥션풀(total=3) 고갈 → 이후 요청 전부 타임아웃. 처음엔 DB 이상으로 오해했다(pg_stat_activity에서 `idle in transaction`+Lock 대기로 확인).
- **실행 중인 스페어의 jar를 `bootJar`로 덮어쓰지 말 것**: donation 스페어가 "Error setting socket options"로 망가졌다. 빌드 전에 스페어를 내릴 것.
- Playwright: PC에 playwright 모듈이 없어 스크래치패드에 `playwright-core`를 설치하고 캐시된 Chromium(`~/AppData/Local/ms-playwright/chromium-1248/chrome-win64/chrome.exe`)을 `executablePath`로 쓴다. 로그인은 `#op_username`·`#op_password` 채우고 `idLogin()` 호출(개발 프로파일은 devCode 자동입력), 계정은 [[tooling-and-local-env]].
- 끝나면 `Get-CimInstance Win32_Process`로 `server.port=1xxxx` 잔여 확인. Docker Desktop과 Kafka 컨테이너도 재부팅 뒤엔 꺼져 있다(컨테이너는 `ghlove-cubrid`만 자동 기동).

## admin-pwtest-manager-accounts

> Playwright 등 자동화 검증용으로 만든 admin 테스트 계정 2개(pwtest_sys/pwtest_loc) - 비밀번호·역할·위치


2026-10-12 지자체공지사항(5110) 화면을 Playwright로 검증하면서 `admin.op_manager`에 전용
테스트 계정 2개를 직접 추가했다(기존 `admin01`류 14개 계정은 평문 비번이 어디에도 문서화돼
있지 않아 추측 시도 자체가 부적절 - [[da-design-pending]]).

| user_id | login_id | 비밀번호 | authority | locgov_code | 용도 |
|---|---|---|---|---|---|
| 900001 | `pwtest_sys` | `Test1234!` | ROLE_ADMIN_1 | NULL | 시스템(비지자체) 권한 뷰 검증 |
| 900002 | `pwtest_loc` | `Test1234!` | ROLE_ADMIN_6 | `11230` | 지자체 담당자(LOC) 권한 뷰 검증 |

둘 다 `status_code='ACTIVE'`, bcrypt 해시(`BCryptPasswordEncoder` 기본 강도 10)로 저장했다.
로그인은 2단계(ID/비번 → 이메일 인증번호)인데 개발 프로파일은 `devCode`를 응답에 그대로
실어주므로, 로그인 화면의 `#devCodeSkipBtn`(개발모드 전용 버튼)을 누르면 인증번호 입력 없이
바로 통과한다.

**Why:** 두 역할(LOC 스코프 vs 시스템 무제한)의 화면 분기(등록/삭제 버튼 노출, 조회 범위)를
한 세션 안에서 실제 로그인 플로우로 오가며 확인하려면 비밀번호를 아는 계정이 필요했다.
기존 계정 재사용은 비번 미상이라 불가능했고, 새 계정 생성은 "추측"이 아니라 "새 자격증명
발급"이라 분류기 제약과 무관하다.

**How to apply:** 앞으로 admin 화면을 Playwright로 검증할 때 재사용한다(삭제하지 않고 남겨
둠 - DB가 공유 서버라 다른 개발자도 쓸 수 있다). 역할별 화면 분기를 바꾸는 작업이면 이 두
계정으로 양쪽 다 확인할 것. 계정을 지우거나 비번을 바꿀 경우 이 메모도 갱신해야 한다.
