# ghlove-msa 개발환경 셋업 가이드 (PC 이전용)

이 문서는 새 PC에서 이 프로젝트를 그대로 이어서 개발하기 위한 체크리스트다. `git clone`으로
저장소를 받으면 이 문서와 DB 스키마(`database/ddl/*.sql`)는 자동으로 따라온다 — 그 외에
**git에 없는 것들**(로컬 인프라 컨테이너, DB 실데이터, Claude Code 메모리)은 아래 절차대로
새로 만들거나 수동으로 옮겨야 한다.

## 0. 자동으로 옮겨지는 것 / 안 옮겨지는 것

| 항목 | 자동 이전 여부 | 방법 |
|---|---|---|
| ghlove-msa 소스코드 전체 | ✅ (GitHub) | `git clone https://github.com/hoyoyo0629/ghlove-msa.git` |
| DB 스키마(DDL) | ✅ (git에 포함) | 저장소 안 `database/ddl/*.sql` |
| DB 실데이터(테스트/시드 데이터) | ❌ | 이 문서 3단계로 재생성(재생성 스크립트가 곧 DDL 적용) |
| AS-IS 참고 소스코드(`ghlove` 저장소) | ❌ | 아래 "AS-IS 참고소스" 절 참고 - 로컬 전용 GitLab이라 별도 처리 필요 |
| AS-IS 원본 문서(엑셀/화면정의서 등, 바탕화면) | ❌ | `C:\Users\USER\Desktop\고향사랑e음\3.AS-IS\` 폴더를 직접 복사 |
| Claude Code 메모리(`.md`)+대화이력 | ❌ (확실치 않음) | 아래 "Claude Code 메모리" 절 참고 |

## 1. 필수 프로그램

- **JDK 17** (Temurin) - 기본 `java`가 다른 버전이어도 무방, 아래처럼 서비스마다 `JAVA_HOME`을 명시적으로 지정해서 실행한다.
  - 이 PC의 설치 경로: `C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot` (새 PC엔 버전이 다를 수 있으니 Temurin 17 아무 빌드나 설치 후 그 경로로 대체)
- **Docker Desktop** (Windows, WSL2 백엔드)
- Python 3.x (검증 스크립트용, 선택)

## 2. 로컬 인프라 컨테이너 4개

전부 Docker 컨테이너로 뜬다. 순서는 무관하지만 Postgres/Kafka는 각 서비스 기동 전에 반드시 떠 있어야 한다.

### 2-1. Postgres (`ghlove-postgres`, 포트 15432)
```bash
docker volume create ghlove-postgres-data
docker run -d --name ghlove-postgres --restart unless-stopped -p 15432:5432 \
  -e POSTGRES_PASSWORD=ghlove -v ghlove-postgres-data:/var/lib/postgresql/data postgres:17
```

### 2-2. Kafka (`ghlove-kafka`, 포트 9092, KRaft 단일노드)
```bash
docker run -d --name ghlove-kafka --restart unless-stopped -p 9092:9092 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e CLUSTER_ID=Z2hsb3ZlbXNhbG9jYWw \
  apache/kafka:3.7.0
```
`KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR`류 3개 env가 빠지면 컨슈머가 전부 멈춘다(단일 브로커인데 기본 replication-factor=3을 요구해서) - 꼭 포함할 것.

### 2-3. Kong 게이트웨이 (`ghlove-kong`, 포트 8000/8001)
```bash
# git-bash에서 실행 시 MSYS_NO_PATHCONV=1 필수(안 붙이면 컨테이너 내부 경로가 윈도우 경로로 오염됨)
MSYS_NO_PATHCONV=1 docker run -d --name ghlove-kong --restart unless-stopped -p 8000:8000 -p 8001:8001 \
  -e KONG_DATABASE=off -e KONG_DECLARATIVE_CONFIG=/kong/declarative/kong.yaml -e KONG_ADMIN_LISTEN=0.0.0.0:8001 \
  -v "//c/workspace/ghlove-msa/infra/kong/kong-local.yaml:/kong/declarative/kong.yaml:ro" kong:3.8
```
설정파일은 저장소 안 `infra/kong/kong-local.yaml`에 이미 있다(git으로 자동 전달됨). 경로만 새 PC의 클론 위치로 맞추면 된다.

### 2-4. GitLab (`ghlove-gitlab`, 포트 8929) - AS-IS 참고소스용, 선택사항
```bash
cd infra/gitlab && docker compose up -d
```
설정은 저장소 안 `infra/gitlab/docker-compose.yml`에 있다(git으로 자동 전달됨). **단, 이 컨테이너를 새로 띄워도 안은 비어있다** - `ghlove`(AS-IS 참고 소스코드) 저장소 자체는 이 GitLab의 Docker 볼륨 데이터라 git으로 옮겨지지 않는다. AS-IS 참고소스가 필요하면:
- **권장**: `C:\workspace\ghlove` 폴더를 통째로 복사(USB/외장하드/클라우드/네트워크공유)해서 새 PC의 같은 경로에 두면, 이 GitLab 컨테이너 자체는 굳이 안 띄워도 된다(로컬 워킹카피만으로 충분 - 지금까지의 조사 작업도 전부 워킹카피 파일을 직접 읽는 방식이었다).
- 이력까지 필요하면 이 PC에서 `docker run --rm -v gitlab_gitlab_data:/from -v /originally-empty:/to alpine tar cf - -C /from . | ...` 식으로 볼륨을 백업/복원해야 하는데, 일반적으로 불필요하다.

## 3. DB 롤/스키마 생성

```bash
# 1) 서비스별 role+DB 생성 (전부 비밀번호 ghlove, DB명=role명과 동일하되 order/admin만 예외)
docker exec ghlove-postgres psql -U postgres -c "
CREATE ROLE member LOGIN PASSWORD 'ghlove'; CREATE DATABASE member OWNER member;
CREATE ROLE donation LOGIN PASSWORD 'ghlove'; CREATE DATABASE donation OWNER donation;
CREATE ROLE point LOGIN PASSWORD 'ghlove'; CREATE DATABASE point OWNER point;
CREATE ROLE gift LOGIN PASSWORD 'ghlove'; CREATE DATABASE gift OWNER gift;
CREATE ROLE orderdb LOGIN PASSWORD 'ghlove'; CREATE DATABASE orderdb OWNER orderdb;
CREATE ROLE admindb LOGIN PASSWORD 'ghlove'; CREATE DATABASE admindb OWNER admindb;
"

# 2) 각 DB에 해당 DDL 적용 (스키마+공통코드 시드까지 전부 이 파일 안에 있음)
docker exec -i ghlove-postgres psql -U member   -d member   < database/ddl/service-member.sql
docker exec -i ghlove-postgres psql -U donation -d donation < database/ddl/service-donation.sql
docker exec -i ghlove-postgres psql -U point    -d point    < database/ddl/service-point.sql
docker exec -i ghlove-postgres psql -U gift     -d gift     < database/ddl/service-gift.sql
docker exec -i ghlove-postgres psql -U orderdb  -d orderdb  < database/ddl/service-order.sql
docker exec -i ghlove-postgres psql -U admindb  -d admindb  < database/ddl/service-admin.sql
```

**주의**: DDL을 `postgres` 슈퍼유저로 적용하면 새로 생기는 테이블/시퀀스의 owner가 `postgres`가 돼서, 나중에 그 role로 만든 admin CRUD 화면(쿠폰/셀러/브랜드 등)에서 `ERROR: must be owner of table`류 권한 에러가 난다 - 위처럼 **각 서비스 자신의 role로 접속해서** DDL을 적용할 것. 테스트 계정(로그인용)이나 admin01 관리자 계정 같은 건 DDL 시드에 없을 수 있으니, 서비스를 띄운 뒤 회원가입/관리자 계정 발급 화면으로 직접 만든다.

DDL 실행 결과에 경고성 에러(이미 존재하는 시퀀스 재생성 시도 등)가 몇 줄 나올 수 있는데, `CREATE TABLE IF NOT EXISTS`/`ON CONFLICT DO NOTHING` 위주라 대체로 무해하다 - 마지막에 각 서비스를 실제로 띄워보고 정상 응답하는지로 확인한다.

## 4. 서비스 실행

전부 `ghlove-msa/<service>/` 아래 독립 Gradle 프로젝트(멀티모듈 아님, 서비스마다 자기 `gradlew`).

| 서비스 | 포트 | 실행 |
|---|---|---|
| member | 8081 | `cd member && JAVA_HOME="<jdk17경로>" ./gradlew.bat bootRun` |
| donation | 8082 | 위와 동일, `donation/` |
| point | 8083 | 위와 동일, `point/` |
| gift | 8084 | 위와 동일, `gift/` |
| order | 8085 | 위와 동일, `order/` |
| admin | 8086 | 위와 동일, `admin/` |

6개 다 띄운 뒤 `curl http://localhost:8081/`(200) ~ `curl http://localhost:8086/admin/login`(200)으로 헬스체크.

## 5. 알려진 함정 (반복하지 말 것)

- **VSCode Run/Debug로 실행하면** `-parameters` 플래그가 안 붙어서 `@RequestParam String x`류가 런타임에 `IllegalArgumentException`을 던진다 - 각 서비스 `build.gradle`에 이미 `tasks.withType(JavaCompile) { options.compilerArgs << '-parameters' }`가 들어있으니 `gradlew bootRun`이든 VSCode든 문제없이 동작해야 한다(빠져있다면 새로 추가).
- **Docker Desktop이 죽으면 컨테이너 4개(postgres/kafka/kong/gitlab)가 전부 같이 내려간다.** Docker Desktop 재기동 후 `docker start ghlove-postgres ghlove-kafka ghlove-kong ghlove-gitlab`(`--restart unless-stopped`라 자동 복구되는 경우가 많지만 확인은 할 것).
- **redirect: 문자열에 예외 메시지를 그대로 붙이지 말 것** - 한글/공백 포함 시 `Location` 헤더 자체가 안 실리는 조용한 실패가 난다. `URLEncoder.encode(...)`로 감쌀 것.
- **AS-IS 원본 테이블(비auto-increment PK)을 재사용하는 새 엔티티**는 DDL의 시퀀스+DEFAULT뿐 아니라 JPA 엔티티에도 `@GeneratedValue(strategy=SEQUENCE, generator=...)`+`@SequenceGenerator`를 반드시 같이 넣을 것 - 이번 세션에서만 seller/brand/coupon 세 곳에서 이 패턴을 놓쳐 배치스캔 당시 시퀀스가 없는 채로 남아있던 걸 발견+수정했다.
- **크로스서비스 admin 조회용 record의 필드 타입은 owning 서비스의 실제 컬럼타입과 정확히 맞출 것** - JPA 엔티티는 `getXxx()`, record는 `xxx()` 접근법이 다르다는 것도 Thymeleaf 템플릿에서 헷갈리기 쉽다(엔티티를 직접 넘기는 화면은 `${x.field}` 프로퍼티 접근, admin의 record 기반 화면은 `${x.field()}` 메서드 접근).
- **Thymeleaf 삼항연산자는 `${...}` 블록 하나 안에 조건+양쪽 분기를 전부 넣을 것** - `${a} ? b : ${c}`처럼 여러 `${}`로 쪼개면 깨진다.

## 6. Claude Code 메모리(.md)와 대화 이력

이 세션 동안 쌓인 프로젝트 지식(AS-IS 매핑, 발견한 버그, 설계 결정 등)은 `C:\Users\USER\.claude\projects\c--workspace-ghlove-msa\memory\*.md`에 파일로 저장돼 있고, 이 문서(SETUP.md)와 달리 **git 저장소 밖**에 있다 - 계정으로 로그인한다고 자동으로 새 PC에 나타나는지 여부는 보장할 수 없으므로, 확실히 이어가려면 아래 폴더를 USB/클라우드드라이브로 직접 복사해 새 PC의 같은 경로에 두는 것을 권장한다:

```
C:\Users\USER\.claude\projects\c--workspace-ghlove-msa\
```

이 폴더가 없더라도 새 세션은 이 저장소의 코드 자체(구현된 기능, 커밋 이력, 이 SETUP.md)로부터 상당 부분을 다시 파악할 수 있다 - 다만 "왜 그렇게 설계했는지"류의 맥락(예: 정기발행쿠폰을 별도 테이블로 유지한 이유, 회원등급 미구현 관련 결정 등)은 메모리 없이는 복원이 안 되니, 가능하면 폴더째 옮기는 걸 권장한다.

## 7. 현재 프로젝트 상태 (2026-08-25 기준)

6개 서비스(member/donation/point/gift/order/admin) 전부 구현 완료. AS-IS(`ilovegohyang.go.kr`) 기능 대부분을 MSA로 전환 완료한 상태이며, 최근 라운드:
- shop-statistics Tier B (매출/대시보드/선호도 20화면)
- 쿠폰 서브시스템 전체 신규 구현(order 서비스, admin 관리화면, 실제 체크아웃 할인연동까지 E2E 검증 완료)
- 로그인 후 원래화면 복귀 버그 수정(member 서비스 전역)
- 아이디/비밀번호 찾기 AS-IS 방식(본인인증 기반) 재구현

다음 우선순위는 사용자 확인 필요 - 대형 미구현 기능은 모두 종료된 상태.
