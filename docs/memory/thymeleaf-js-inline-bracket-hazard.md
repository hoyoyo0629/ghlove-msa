---
name: thymeleaf-js-inline-bracket-hazard
description: th:inline=javascript 블록의 여는 대괄호 2개는 Thymeleaf 식으로 파싱돼 렌더링이 중단된다(브라우저는 ERR_INCOMPLETE_CHUNKED_ENCODING 200). JS 주석 안도 예외 아님
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T01:12:12.081Z
---

**AS-IS JSP의 JS를 `th:inline="javascript"` 블록으로 옮길 때, 여는 대괄호가 두 개 붙어 있으면
Thymeleaf가 인라인 식으로 읽어 `Could not parse as expression`으로 **렌더링을 중단**한다.**
`//` JS 주석 안도 예외가 아니다(텍스트 노드로 처리된다). 중첩 배열은 **한 칸 띄운다**.
HTML 주석(`<!-- [[memory-name]] -->`)은 처리 대상이 아니라 안전하다.

**Why:** 이미 응답을 내보내던 중이라 500 페이지로 바뀌지 못하고
(`Cannot render error page ... response has already been committed`)
브라우저는 **`ERR_INCOMPLETE_CHUNKED_ENCODING` + 200**을 받는다. 그래서 증상이
"페이지는 떴는데 아래쪽 기능이 없다"로 나타난다 - 2026-10-06 팝업 등록화면에서
**스마트에디터와 날짜 달력이 동시에 사라진** 원인이 이거였다(달력은 문서 로드가
중단돼 ready 핸들러가 안 돌아서다). 원인은 `fragments/smarteditor.html`의
AS-IS 주석 `[["MS UI Gothic", ...]]` 한 줄이고, **그 조각을 쓰는 16개 화면**
(커뮤니티 게시판 5종·이메일 2종·FAQ·명예기부자·메일설정·약관·팝업·자료실)이 같이 깨져 있었다.

**How to apply:**
- 증상이 "200인데 페이지 아래쪽이 없다"면 **서버 로그에서 `TemplateOutputException`을 먼저 찾아라.**
  정적자산 404를 아무리 뒤져도 안 나온다(이번에 자산 18종 전부 200인 걸 확인하고도 헤맸다).
- 가드 테스트가 있다: `admin/src/test/java/com/ghlove/admin/ThymeleafInlineHazardTest.java`
  (전체 템플릿 스캔, 인라인 JS 블록의 `[` `[`는 `${`/`@{`/`#{`가 바로 와야 통과).
  `./gradlew.bat test --tests "com.ghlove.admin.ThymeleafInlineHazardTest"`
- 컴파일·`bootJar`·기동으로는 **안 잡힌다**(요청이 와서 렌더링해야 터진다).
  [[build-is-mine-restart-is-users]]의 여분 포트 기동검증도 이건 못 잡는다.
- 같은 날 발견한 짝: 팝업 저장이 목록 URL에 얹혀 `Ambiguous handler methods`로 검색·페이징이
  전부 500이었다. AS-IS는 **저장 URL = 폼 URL**(`POST write` / `POST edit/{id}`)이고 목록 검색은
  `POST list`로 분리돼 있다. 스캐너: `scratchpad/find-ambiguous2.pl`(컨트롤러별 POST 충돌 탐지).
