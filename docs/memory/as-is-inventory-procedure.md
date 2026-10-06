---
name: as-is-inventory-procedure
description: "AS-IS 전수 인벤토리 추출 절차와 산출물 위치 (도메인 순서 member→donation→point→gift→order→admin, 2026-09-10 6도메인+common 전수 완료)"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-09-10T05:14:16.298Z
---

[[as-is-logic-is-the-spec]] / [[scope-migration-not-greenfield]] 기준을 실행하는 구체 절차. 2026-09-10 사용자 지시로 착수했다.

**도메인 순서**: member → donation → point → gift → order → admin **+ 7번째 시트 `common`**(6개 도메인 밖 전 영역 - GNB/LNB·게시판·약관·통합검색·메인·소식지·공통인프라·배치). 2026-09-10 전부 완료, 누적 726행. 도메인마다 표 4개(컨트롤러 엔드포인트 / 서비스 메서드 / 매퍼 쿼리 / 화면 이벤트 핸들러)를 뽑아 MSA와 1:1 매핑한다.
**판정 5분류**: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**. 죽은코드는 지우지 말고 목록에 남기되 따로 표시한다(사용자 지시).

**산출물**
- 서술 분석: `docs/as-is-inventory-<domain>.md`
- 표 데이터: `docs/inventory/<domain>.tsv` (7열: 계층·AS-IS구분·AS-IS항목·근거·MSA대응·판정·비고)
- 엑셀: `docs/inventory/as-is-inventory.xlsx` — `docs/inventory/build-xlsx.ps1` 실행하면 tsv들을 **서비스별 sheet**로 묶어 생성(Excel COM 사용, 판정별 행 색상·틀고정·자동필터 포함). **.ps1은 반드시 UTF-8 BOM으로 저장**해야 한다(Windows PowerShell 5.1이 BOM 없으면 ANSI로 읽어 한글 파서 에러).

**추출 요령 (실측 검증된 것)**
- **소스 인덱스를 먼저 만든다**: `find ghlove-api ghlove-common ghlove-web ghlove-batch -name "*.java" -print0 | xargs -0 grep -Hn "" > /tmp/java_index.txt` — 1.2초, 41MB. 이후 모든 조회를 이 파일 하나에 건다. 매퍼 131개를 모듈 전체 재귀 grep으로 돌리면 120초를 넘겨 타임아웃되지만 인덱스로는 9초다.
- **죽은 화면 판별**: `ghlove-frontend`는 정적 HTML 라우팅이라 inbound 참조가 0이면 도달불가다. 제외 패턴 `-backup.html`, `_bak.html`, `_test.html`, `_YYYYMMDD.html`, `/old/`, `node_modules`. 단 **서버 리다이렉트 착지 페이지**(onepass-result, mobile-auth-result)는 프론트 참조가 0이어도 살아있으므로 Java 쪽 `sendRedirect`/`setUrl`을 반드시 같이 확인한다.
- **엔드포인트 사용여부는 2단계**: `modules/op.saleson.js`는 URL 레지스트리라 URL이 등록돼 있는 것만으로는 증거가 안 된다. 레지스트리의 래퍼 함수명을 뽑아 화면에서 `.<래퍼명>(` 호출을 찾아야 한다.
- **매퍼 사장쿼리 판별에 주입 변수명을 가정하지 말 것**. AS-IS는 master/slave 이중 주입(`slaveSecedeUserMapper`)과 대문자 필드명(`JoinMapper.getUserInfoByUserId(...)`)이 섞여 있어 오탐이 난다. 인덱스에서 `\.<queryId>\(` 존재 여부로 판정하고, 매퍼 인터페이스 선언 여부와 교차검증한다.

**Why:** 화면 단위로 보면 재현 누락이 "원래 그런 것"으로 묻히고, MSA 주석의 "AS-IS는 …" 서술을 검증할 수 없다. 실제로 이 절차 첫 적용(member)에서 AS-IS `/api/auth/*` 회원기능 16개가 `/api/user/*`로 이관된 뒤 남은 죽은코드라는 것, 전자서명 등록화면이 오타(`sign-certificate1.html`)로 도달불가라 등록 없이 검증만 도는 반쪽 상태라는 것, MSA 수신동의 체크박스 4종에 `th:field`가 없어 폼 전송이 통째로 누락된다는 것을 찾았다.

**How to apply:** 도메인 착수 시 인덱스부터 만들고, 화면 생사 → 엔드포인트 → 매퍼 → 서비스로직 → 화면이벤트 순으로 내려간다. 각 판정에는 `파일:라인` 근거를 반드시 남긴다. tsv를 추가한 뒤 `build-xlsx.ps1`을 다시 돌리면 sheet가 누적된다.
