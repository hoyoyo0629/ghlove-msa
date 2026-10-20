---
name: admin-screen-pitfalls
description: admin(Thymeleaf) 화면 이식 함정 모음 - th:onclick 문자열·인라인JS 대괄호·count_title span·h3 덮어쓰기·푸터 조각·공통 전역·csrf meta·라디오 label·JSP 꼬리 style·인터셉터 예외·로그인 게이트 exclude·@opDate·메뉴 노출 규칙·엑셀 No 방향·년도 select·크럼/LNB 스크립트·공통JS가 끼우는 DOM
metadata:
  type: feedback
---

admin 화면을 이식·수정할 때 실제로 터졌던 함정과 해결 규칙.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## admin-thymeleaf-onclick-string-hazard

> th:onclick(DOM 이벤트 속성)에 문자열 변수식을 넣으면 렌더링 중 TemplateProcessingException으로 응답이 끊긴다 - th:attr로 전환+가드테스트 추가(2026-10-07)


**증상**: 1405 사용자 권한그룹 관리(`/admin/roles`) 목록에서 "수정" 버튼·메뉴경로·행이 1건만
보이는 등 화면이 통째로 깨져 보임. 브라우저는 view-source 백지, DevTools Network
"Failed to load response data"(= `ERR_INCOMPLETE_CHUNKED_ENCODING`).

**원인**: [role-admin/list.html](../../admin/src/main/resources/templates/role-admin/list.html)의
`th:onclick="|fnGrpUpdate('${row.role.authority}')|"`. Thymeleaf 3.1부터 `th:onclick` 등
`th:onXXX`(DOM 이벤트 속성, `StandardDOMEventAttributeTagProcessor`)는 "신뢰할 수 없는 컨텍스트"로
취급되어, 그 안의 `${...}` 변수식이 **숫자·불린이 아니면** `TemplateProcessingException("Only
variable expressions returning numbers or booleans are allowed...")`을 던진다. `authority`가
문자열이라 렌더링 중 예외가 났고, 그 시점엔 이미 헤더·상단 네비 마크업이 응답으로 flush된
뒤라(8KB 버퍼 초과) 에러페이지도 못 띄우고 응답이 그대로 끊겼다(`ERROR
s.e.ErrorMvcAutoConfiguration$StaticView : Cannot render error page ... response has already
been committed`). 서버 콘솔(디버그 로그)에서 그 스택트레이스를 직접 봐야만 확정할 수 있었다 -
DB·템플릿 소스·컴파일 결과만 정적으로 훑어서는 "캐시 문제"로 오판했다
([[asis-porting-principles]] 류와 같은 교훈: 실제 렌더링 결과를 봐야 한다).

**조치(2026-10-07)**: 코드베이스 전체에서 `th:on[a-z]+=` 패턴 10곳(7개 파일: role-admin/list,
codes/list, designated/analysis-locgov×2, content/banner-list, give/give-statistics-{locgov,
operate-list}, survey-admin/form, policy/form, batch-job/list)을 전부 `th:attr="onclick=|...|"`로
전환(이미 community/qna-admin/manual-admin 등 15곳 이상에서 쓰던 안전한 패턴 - th:attr은 이
"신뢰 컨텍스트" 제한이 없다). 숫자 파라미터(batchJobId 등)는 지금은 안 터지지만 같은 함정이라
함께 바꿨다.

**가드**: `ThymeleafOnEventAttributeHazardTest` 추가 - 템플릿에 `th:on[a-zA-Z]+=`가 하나라도
있으면 빌드 실패. [[admin-screen-pitfalls]](대괄호2개 가드)와 같은 클래스의
"컴파일로는 안 잡히고 렌더링해봐야 터지는" Thymeleaf 함정.

## thymeleaf-js-inline-bracket-hazard

> th:inline=javascript 블록의 여는 대괄호 2개는 Thymeleaf 식으로 파싱돼 렌더링이 중단된다(브라우저는 ERR_INCOMPLETE_CHUNKED_ENCODING 200). JS 주석 안도 예외 아님


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

## admin-count-title-span-block-css-trap

> count_title h5 안의 <span>을 th:remove="tag" 없이 쓰면 "총 N건"이 세 줄로 쪼개짐(opmanager.css가 h5 span을 block으로 바꿈) - 5개 화면에서 실제로 터짐, 2026-10-14 전부 수정


`admin/.../static/content/opmanager/css/opmanager.css:1123`:

```css
.count_title h5 span { display: block; margin-top: 8px; margin-left: 3px; ... }
```

AS-IS JSP는 `<h5>총 <c:out value='${count}'/>건</h5>`처럼 **태그 없이** 숫자를 찍어서 이 규칙이
문제가 안 됐다. Thymeleaf로 옮기며 `<h5>총 <span th:text="${count}">0</span>건</h5>`처럼 숫자를
`<span>`으로 감싸면, 그 span이 `display:block`이 돼 "총"/숫자/"건"이 **세 줄로 쪼개져 보인다**
(가로 플렉스 레이아웃이 아니라 세로로 깨짐 - 레이아웃 스크린샷으로만 잡힌다, HTML 구조 검토로는
안 보임).

기존 코드 40여 화면 대부분(`member-admin/list.html`, `locgov-admin/list.html` 등)은 이미
`<span th:text="${count}" th:remove="tag">0</span>`처럼 **`th:remove="tag"`**를 붙여 렌더링
결과에서 span 태그 자체를 제거하는 식으로 피해가고 있었다. 그런데 이 패턴을 빠뜨린 화면이
2026-10-14 기준 5곳 있었다: `statistics-locgov/list.html`(관심지자체, 같은 세션 작업분) ·
`designated/approval-log-popup.html` · `featured-admin/list.html` · 신규 작성한
`shop-statistics/sales-locgov-mois.html`/`sales-locgov.html`. 지자체별 구매현황
([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-sales-day-statistics-port)) 작업 중 스크린샷 검증으로 발견해 5곳 전부 수정했다.

**Why**: Thymeleaf 템플릿에서 숫자를 동적으로 찍으려면 보통 `<span th:text="...">`로 감싸는
것이 직관적인데, 이 코드베이스의 AS-IS CSS 중 일부가 "별도 태그 없는 텍스트"를 전제로 짜여
있어서 그 직관이 깨진다.

**How to apply**: `count_title` 영역(`총 N건` 안내문)에 숫자를 넣을 때는 항상
`<span th:text="${count}" th:remove="tag">0</span>` 형태로 쓸 것(span을 완전히 없애는
`th:remove="tag"` 필수). 새 목록화면을 포팅할 때 이 패턴이 맞는지 스크린샷으로 확인하는 걸
체크리스트에 넣을 것 - HTML만 봐서는 깨진 걸 못 잡는다.

## admin-h3-title-overwritten-by-menu-name

> 운영관리 화면의 h3 제목은 op.manager.js가 3단 메뉴명으로 덮어쓴다 - 템플릿에 뭘 적든 화면엔 메뉴명이 보인다(.custom 클래스만 예외)


`static/content/modules/op.manager.js:46`:

```js
$('.contents h3').eq(0).not('.custom').find('span').text(thirdMenuName.replace('- ', ''));
```

**첫 번째 `.contents h3`의 `<span>` 텍스트를 LNB 3단 메뉴명으로 덮어쓴다.** AS-IS에서 그대로
복사해 온 자산이라 AS-IS도 같은 동작이고, 그래서 AS-IS JSP 상당수가 `<h3><span></span></h3>`처럼
**빈 span**을 둔다. 덮어쓰기를 피하려면 h3에 `class="custom"`을 준다.

**Why:** 이걸 모르면 화면 이식할 때 두 가지로 헛짚는다.
- JSP의 h3에 적힌 문구(자료실 list.jsp는 `${op:message('MENU_5110')}` = "기금사업소개 및
  공지사항", form.jsp는 "글쓰기")를 "AS-IS 화면에 그렇게 보이는 복사 흔적"으로 오해한다.
  실제로는 JS가 메뉴명("자료실")으로 덮어써서 **화면 결과는 멀쩡하다**. 2026-10-12 자료실
  이식 중 Playwright 검증이 FAIL로 잡아내 확인했다.
- 반대로 h3가 비어 있는 AS-IS JSP(지자체공지사항 등)를 보고 "제목이 없는 화면"으로 오해한다.

**How to apply:** 화면 이식 시 h3 span에는 적당한 기본값(메뉴명이나 AS-IS 원문)을 넣어 두고,
**화면에 실제로 보이는 제목은 `op_menu`의 3단 메뉴명이라는 전제로 검증**한다. 제목이 메뉴명과
달라야 하는 화면이라면 그때만 `class="custom"`을 쓴다(AS-IS에 그런 화면이 있는지 먼저 확인).
Playwright로 제목을 단언할 때 기대값은 **메뉴명**이다.

## admin-footer-fragment-must-be-added-per-screen

> AS-IS는 푸터가 전역 레이아웃이라 모든 운영관리 화면에 있는데 TO-BE는 템플릿마다 직접 넣어야 해서 42화면이 빠져 있었다


AS-IS는 `layouts/common/inc_footer.jsp`를 **전역 레이아웃**에서 포함해 모든 운영관리 화면에
푸터(저작권 한 줄)가 깔린다. TO-BE는 레이아웃 데코레이터가 없어 **각 템플릿이 직접**
`<div th:replace="~{fragments/footer :: admin-footer}"></div>`를 넣어야 하고, 빠뜨리면
그 화면만 푸터 없이 뜬다 - 화면을 열어보기 전엔 드러나지 않는다.

**2026-10-12 전수 점검 결과**: `class="opmanager"` 화면 269개 중 **42개가 누락**이었다
(사용자가 관리자매뉴얼에서 발견). 커뮤니티 19 · 이번 고객센터 라운드 산출물 14
(locgov-notice·notice-admin·data-board-admin·maintenance-admin·seller-notice-admin·qna-admin) ·
manual-admin 3 · faq-admin 2 · inquiry-admin 2 · designated 2. 전부 `</body>` 직전에 추가했고
Playwright로 13개 대표 화면에서 `#footer` 렌더를 확인했다.

**Why:** 푸터는 눈에 잘 안 띄는 요소라 화면을 만들 때 빠뜨리기 쉽고, 컴파일·테스트로는
절대 안 잡힌다. AS-IS에는 **예외 없이 있는** 요소라 누락은 곧 parity 불일치다.

**How to apply:**
- 새 운영관리 화면을 만들 때 템플릿 뼈대에 푸터를 **처음부터** 포함한다
  (헤더 `admin-nav :: header()` / lnb / breadcrumb / **footer** 네 조각이 한 세트다).
- 팝업 화면(별도 창, `admin-nav :: header()`를 안 쓰는 템플릿)은 푸터가 없는 게 맞다.
- 점검 한 줄:
  ```bash
  for f in $(grep -rl 'class="opmanager"' templates); do
    grep -q "admin-nav :: header" "$f" || continue
    grep -q "fragments/footer" "$f" || echo "$f"
  done
  ```

## admin-inc-common-globals-missing

> AS-IS inc_common.jsp가 모든 운영관리 페이지에 깔던 전역(isMobileLayer, userId)이 TO-BE에 없어 Common.popup이 ReferenceError로 죽어 13개 화면의 팝업 버튼이 전부 먹통이었다


**2026-10-06, 사용자가 1406(관리자 권한 승인관리)의 상태·이력 팝업, 1405(권한그룹 관리)의
'권한그룹 생성' 버튼이 "먹통"이라고 신고.** 링크·함수·팝업 화면은 다 있는데 클릭하면 아무 일도
안 일어났다. 근본 원인은 개별 화면이 아니라 공통 전역 누락이었다.

**원인:** AS-IS는 `layouts/common/inc_common.jsp`가 **모든 페이지 하단**에 전역 몇 개를
`<script>`로 깔아둔다. TO-BE 공통 head(`fragments/opmanager-head.html`)는 그중
`RequestContext`·`OP_MANAGER_TIMEOUT` 만 옮겨서, 나머지 둘이 빠져 있었다:
- **`isMobileLayer`** — `op.common.js:642` `Common.popup`의 **첫 줄**이 읽는다. 전역이 없으면
  함수 진입 즉시 `ReferenceError: isMobileLayer is not defined` 로 죽어 **모든 팝업이 실패**했다.
  복사해 온 자산이라 참조를 지울 수도 없다. AS-IS는 `shopContext.mobileLayer`('true'면 true)이고
  운영관리 콘솔은 데스크톱이라 **항상 false** → false면 `window.open`, true면 body 안 iframe 레이어.
- **`userId`** — `op.common.js:2160` `Common.userId()` 가 읽는다(현재 호출부는 없지만 자산이 참조).

**영향 범위:** `Common.popup` 을 쓰는 **운영관리 13개 화면 / 호출 23곳 전부**였다 - 1404 권한그룹
수정, 1405 권한그룹 생성, 1406 상태·이력, 메뉴/공통코드/지자체/배치/이메일설정/회원상세/탈퇴회원/
엑셀다운로드로그/오프라인담당자 등. 한 화면 버그가 아니라 공통 조각 하나로 다 고쳐진다.

**조치:** `opmanager-head.html` 에 `var isMobileLayer = false;` 와
`var userId = ${loginManager?.userId}` 를 추가. 2026-10-02에 같은 유형으로 `RequestContext`
(op.link.js가 읽는 전역)를 이미 한 번 메꿨는데([[asis-screen-port-procedure]] 공통 head 주석),
그때 `isMobileLayer`·`userId` 를 빠뜨린 것이다.

**교훈:** 복사해 온 jQuery 자산(op.common.js/op.manager.js)이 **전역 변수에 의존**하는데 그 전역은
AS-IS에서 inc_common.jsp/inc_head.jsp가 깔아준다. TO-BE 공통 head로 옮길 때 **JSP가 깔던 전역을
전수로 떠서** 넣어야 한다. 한 번에 안 떠서 팝업·링크가 화면마다 조용히 죽는 걸 반복 발견 중이다.
`op.common.js`/`op.manager.js`에서 선언 없이 참조되는 식별자를 grep으로 훑어 남은 게 없는지
점검할 것(확인된 소비: isMobileLayer, userId, RequestContext, OP_MANAGER_TIMEOUT,
OP_MANAGER_TIMEOUT_TYPE, OP_CONTEXT_PATH, OP_LANGUAGE - 전부 지금 head에 있음).

**검증 한계(미완):** 팝업이 실제로 열리는 것은 **로그인 세션이 필요**해 런타임으로 못 눌러봤다
(admin01 평문 비번이 어디에도 문서화돼 있지 않고, 비번 추측 시도는 분류기가 막는다 - 정당하다).
빌드·기동·인라인 가드테스트는 통과. **사용자가 재기동 후 직접 클릭 확인**해야 최종 확정된다.

## admin-opmanager-ajax-needs-csrf-meta

> admin에서 op.common.js(AS-IS opmanager 공용 JS) 쓰는 화면은 _csrf/_csrf_header meta 필수, 없으면 모든 $.ajax가 조용히 취소됨


**AS-IS opmanager 공용 스크립트 `op.common.js`를 로드하는 admin 화면은 `<head>`에 반드시 아래 두 meta를 넣어야 한다. 없으면 모든 ajax가 발사 전 취소되고 화면이 조용히 빈다.**

```html
<meta name="_csrf_header" content="X-CSRF-TOKEN">
<meta name="_csrf" content="">
```

**Why:** op.common.js의 전역 `$.ajaxSetup({beforeSend})`가 `xhr.setRequestHeader(Common.getCsrfHeader(), Common.getCsrf())`를 호출한다. 이 두 함수는 `<meta name="_csrf_header">`/`<meta name="_csrf">`를 읽어 없으면 `''`를 반환 → `setRequestHeader('', '')`는 **빈 헤더명이라 SyntaxError**로 터지고, beforeSend 예외로 `$.post`가 요청도 못 보내고 중단된다(서버 로그·op_manager_action_log에 흔적 0, 차트/목록 공란). AS-IS opmanager 레이아웃은 Spring Security가 이 meta를 채워 내려줬으나, TO-BE admin은 Spring Security/CSRF가 없다([[asis-porting-principles]] 스키마 치환과 같은 성격). 헤더명만 유효하면 되고 토큰은 빈 값이어도 서버가 무시한다.

**How to apply:**
- 2026-10-01 지정기부 월별통계(`designated/analysis-month.html`)에서 이 함정으로 차트가 안 떠서 meta 2개 추가해 해결. [[asis-porting-principles]]로 op.common.js를 그대로 쓰는 화면은 전부 해당.
- 진단법: ajax가 서버에 안 닿고(admin.log·감사로그에 요청 없음) 화면만 비면 CSRF meta 누락부터 의심. 콘솔엔 setRequestHeader SyntaxError가 뜬다.
- admin은 Spring Security가 없다(세션+AdminAuthInterceptor 게이팅). 그래서 CSRF 토큰 자체가 없음=정상.

## asis-radio-needs-sibling-label

> AS-IS form:radiobutton은 input+형제 label(for) 구조라, label로 감싸면 .input-form flex가 죽어 CSS가 틀어진다


AS-IS 운영관리 화면의 라디오/체크박스는 Spring `form:radiobutton` 이 만들어낸
**`<input id="xxx1">` + 형제 `<label for="xxx1">`** 구조다. TO-BE에서 `<label><input>텍스트</label>`
로 감싸 두면 화면이 틀어진다.

**Why:** `opmanager.css:2496` 의
`.admin_wrap .input-form { display:flex; gap:5px; align-items:center }` 는 input과 label이
**각각 flex 아이템일 때만** 간격·수직정렬이 먹는다. label로 감싸면 자식이 label 하나뿐이라 gap이
사라지고, `opmanager.css:414` 의 `input[type=radio] { margin-right:-5px !important }` 때문에
글자가 라디오에 달라붙는다. AS-IS CSS를 그대로 복사해 썼는데도 안 나오는 전형적인 마크업 불일치다
([[asis-porting-principles]]).

**How to apply:** `.input-form` 안의 라디오는 반드시 `<input id><label for>` 형제 구조로 쓴다.
검증은 눈이 아니라 실측으로 — AS-IS와 TO-BE에서 `gapPx`(label.left - input.right)와 중심선 Y를
같은 스크립트로 찍어 비교한다. 정상값은 **gapPx 0**(margin -5px가 gap 5px를 상쇄), input/label 중심선 일치.

**★2026-10-12 보강 - `.input-form` 래퍼를 쓸지 말지는 화면마다 다르다. 반드시 AS-IS JSP를 보고 정할 것.**
내가 5110 지자체공지사항을 만들 때 5111 공지사항 패턴을 보고 래퍼를 넣었는데, AS-IS
`locgov-notice/form.jsp`·`edit.jsp`에는 **래퍼가 없다**(`flex_box gap-12` 안에 input·label 4개가
직접 들어간다). 래퍼가 있는 건 `notice/form.jsp`(5111)뿐이다. 둘은 렌더 결과가 다르다 -
실측 **래퍼 있으면 gapPx 0**(.input-form gap 5px − margin −5px), **없으면 gapPx 7**
(flex gap 12px − margin 5px). 2026-10-12에 5110 두 화면을 AS-IS대로 되돌렸다.
즉 이 메모의 규칙은 "래퍼를 씌워라"가 아니라 "**래퍼 안에서는 input+label이 형제여야 한다**"이다.

2026-10-08 특정사업 지자체별통계·월별통계 2화면 수정 완료. **남은 곳 2개**:
`admin/.../templates/access/form.html`, `admin/.../templates/featured-admin/list.html`
(화면이 달라 각각 AS-IS 확인 후 처리 - 래퍼 유무까지 원본에서 확인할 것).

## asis-jsp-tail-style-block

> AS-IS JSP 끝의 페이지 전용 <style> 블록을 빠뜨리면 공용 CSS가 화면을 깨뜨린다 - 이식 시 JSP를 끝까지 읽을 것. 2026-10-07 전수 점검 완료(실결함 2건)


AS-IS opmanager JSP는 **파일 맨 끝(</script> 다음)에 페이지 전용 `<style>` 블록**을 두는 경우가 많다
(실측 **116개 화면**). 본문·JS만 옮기고 이 꼬리를 놓치면, 공용 `opmanager.css`가 그 화면에서
의도와 다르게 먹는다. 공용 CSS를 verbatim 복사해도 **페이지 오버라이드까지 옮겨야** parity다.

### 2026-10-07 전수 점검 결과 (사용자 지시 B안)

AS-IS 116개 화면에서 선택자 **342개**를 뽑아 TO-BE 템플릿+정적 CSS와 대조.
그중 **마크업이 실제로 그 클래스를 쓰는데 규칙이 없는 것**만 진짜 결함으로 판정했다.

**실결함 2건 - 둘 다 고침:**

1. `community/faqBbs/list.jsp` → `templates/community/faqBbs/list.html`
   `.admin_wrap .tabs li.active { border-bottom: 1px solid #999; }`
   공용 CSS는 활성 탭 아래 테두리를 **흰색으로 뚫는다**(탭 한 줄 전제). 담당자용 FAQ는 탭이
   두 줄이라 선택 시 밑줄이 끊겼다. AS-IS도 같은 이유로 덮어쓴 것.
2. `user/login_main.jsp` → `templates/admin/login.html`
   `.disabled-div { pointer-events: none; opacity: 0.6; }`
   **기능 결함이었다.** JS가 이메일 인증 단계에서 앞 단계에 이 클래스를 붙여 잠그는데
   규칙이 없어 **클래스만 붙고 계속 클릭·수정이 됐다.**

**오탐으로 제외한 것(기록해 둠 - 다시 조사하지 말 것):**
- `.sortable-placeholder td`(designated-donation 4화면) - **AS-IS도 `.sortable()` 호출이 0건**인
  죽은 스타일. TO-BE는 노출순서를 입력칸+[순서변경] 일괄저장으로 구현했고 그게 AS-IS 동작이다.
- `.board_write_table .label p`(access/write.jsp) - AS-IS `.label` 안은 `<p>`가 아니라
  `<span class="required_mark">`다. AS-IS에서도 안 먹는 규칙.
- `.offCharge`(user/pki/form.jsp) - TO-BE의 `offChargerPasswordInit`(팝업명) 오탐. 클래스 사용 없음.
- order·item·shop-statistics·catalog·seller 등 **미이식/보류 영역**은 범위 밖.

### 다음에 화면을 이식할 때

**JSP는 끝까지 읽는다.** `</script>` 뒤에 `<style>`이 더 있는지 확인하고, 있으면 함께 옮긴다.
옮긴 뒤에는 **왜 필요한지 주석을 단다** - 공용 CSS와 중복돼 보여 나중에 지워지기 쉽다.
판정 기준: *마크업이 그 클래스를 쓰는가*. 쓰지 않으면 스타일 누락이 아니라 기능 미이식이다.

관련: [[asis-screen-port-procedure]] · [[asis-porting-principles]] · [[asis-porting-principles]]

## admin-interceptor-asis-exempt-paths

> AS-IS OpmanagerHandlerInterceptor.isManagerPage 예외(40개) 경로는 TO-BE WebConfig excludePathPatterns에도 넣어야 한다 - 안 넣으면 메뉴권한 없는 롤이 공통 조회를 부를 때 권한없음 HTML이 와서 화면이 깨진다(15102 시군구 undefined)


AS-IS는 관리자 로그인·메뉴권한 검사를 Spring Security가 아니라 `OpmanagerHandlerInterceptor`가 하고(SecurityConfig에
/opmanager/** 인증 규칙 없음), `isManagerPage()`의 예외 경로는 **로그인·메뉴권한 둘 다 안 본다**. TO-BE `AdminAuthInterceptor`는
경로를 메뉴(가장 긴 접두 menu_url)로 판정해 권한 없으면 /admin/access-denied로 302 → ajax가 HTML 문자열을 받는다.

**Why:** 2026-10-13 오프라인 담당자(ROLE_ADMIN_7)로 15102 테스트 시 `/give-state/options-by-locgovCode`가 기부금전체현황(14103)
메뉴로 판정돼 302 → 화면 JS가 HTML을 한 글자씩 돌아 시군구가 전부 "undefined" → 그 값으로 부과요청이 나가 "지방세외 시스템 연결에
실패하였습니다."(사용자는 우회 실패로 오인). 같은 날 `/offgive/sci-result`, `/give-state/options-by-locgovCode` 예외 추가로 해결.

**2026-10-14 1라운드 이식**: /healthcheck("Server is alive."), /error/404, /offgive/pdbtest(예외 추가, pCrypto 없음 → ERROR칸),
/file/**(시스템관리자 검사 - 사용자 결정으로 downloadFile·sendMail에도 검사 추가, AS-IS는 무검사), /fileDownload/downloadByProgramData
(PRJ_NOTICE, 파일명 토큰은 donation SealCipher로 pCrypto 대체 + storefront 공지 첨부). 대상 아님: present/login·transfer(AS-IS 컨트롤러
없음), log/exceldownload-reason(AS-IS 자체가 500). **2라운드(미착수)**: 로그인 서브시스템 "화면·흐름만 AS-IS"(사용자 결정, BCrypt·세션은
TO-BE 유지) - login 화면/메시지/임시비번/PKI·금융인증/잠금/비번변경, manager-request 폼 계열, accessdenied, popup/temp, emailModifyInfo.

**How to apply:** 화면 이식 때 그 화면이 부르는 공통 조회가 AS-IS 예외 목록에 있으면 TO-BE excludePathPatterns에도 넣는다.
AS-IS 예외 중 TO-BE에 아직 대응 경로가 없는 것(이식 시 같이 처리): designated-donation/partList, welfareCenter/wlfrCntrMngList,
user/popup/emailModifyInfo/, log/exceldownload-reason(→TO-BE /common/opmanager/privacy-access-log, 메뉴 미매칭이라 현재 통과).
나머지는 로그인·PKI·매직라인·파일서버(/file/*) 경로라 TO-BE 로그인 흐름이 따로 있다. 점검법: 화면 URL을 op_menu
(status_code='1') 최장 접두로 판정해 그 롤의 op_menu_right가 있는지 본다(메뉴 미매칭은 TO-BE에선 통과).

## admin-login-gate-whitelist-gaps

> admin 로그인 게이트가 화이트리스트라 새 화면을 넣을 때마다 빠뜨린다 - 13개 화면이 로그인 없이 열려 있었고 그 중 3개는 NPE 500이었다


admin의 로그인 게이트는 `WebConfig#addInterceptors` 의 **화이트리스트**다
(`adminAuthInterceptor.addPathPatterns(...)` 에 적힌 경로만 보호한다).
**새 운영자 화면을 만들 때 여기에 추가하지 않으면 그 화면은 로그인 없이 열린다.**

### 2026-10-13 전수 점검 결과 - 13개 화면이 게이트 밖이었다

사용자가 전체 재기동 후 **통계 > 지자체별에서 Whitelabel 500**을 만난 것이 단서였다.
`admin.log` 의 실제 예외:
`NullPointerException: Cannot invoke "Manager.getAuthority()" because "manager" is null`
→ `MenuService.isLocgovScoped(MenuService.java:93)` → `GiveStatisticsController.locgov`.

세션이 없는데도 **로그인으로 리다이렉트되지 않고 컨트롤러까지 들어와서** 터진 것이다.
원인은 목록에 `"/give-statistics"` 만 있고 **`"/give-statistics/**"` 가 없었던 것**.

| 경로 | 수정 전 | 비고 |
|---|---|---|
| `/give-statistics/locgov` | **500** | 사용자가 본 화면 |
| `/give-statistics/operate` | **500** | |
| `/admin/internal-inquiry` | **500** | |
| `/give-statistics/all` | 200 | |
| `/admin/content-satisfaction` | 200 | |
| `/admin/delivery-companies` | 200 | 표 12행 렌더 |
| `/admin/ranking` | 200 | |
| `/admin/shipment-returns` | 200 | |
| `/open-api/clients` · `/open-api/stats` | 200 | 민간개방 API **관리화면**(외부 API는 Kong) |
| `/point-history` · `/point-history/daily` | 200 | **표 69행이 그대로 노출** |
| `/statistics/interest-locgov` | 200 | |

전부 `WebConfig` 에 추가해 **13개 모두 302(로그인)** 로 바뀐 것을 확인했고,
공개로 남아야 하는 경로(`/notices`·`/faqs`·`/events`·`/data-board`·`/qna/*`·`/api/*`·
`/auth/cert-login`·`/admin/login`·`/admin/access-denied`·`/admin/manager-requests/{new,complete}`)는
수정 전후 응답이 같아 회귀가 없다.

**Why:** 500은 눈에 띄지만 **200으로 조용히 열려 있는 쪽이 더 위험하다**(로그인 없이 운영 데이터
노출). 컴파일·테스트로는 절대 안 잡히고, 평소 브라우저에는 세션이 남아 있어 개발 중에도 안 드러난다.
재기동으로 세션이 날아간 직후에만 보인다.

**How to apply:**
- 새 운영자 화면을 만들면 **WebConfig 게이트 목록에 `"/경로"` 와 `"/경로/**"` 를 같이** 넣는다
  (둘 중 하나만 넣는 것이 이번 사고의 형태다).
- 점검 한 줄 - 세션 없이 때려 보고 302가 아니면 게이트 밖이다:
  ```bash
  curl -s -o /dev/null -w '%{http_code} %{redirect_url}\n' http://localhost:8086/some-admin-path
  ```
- 전수 점검은 컨트롤러의 클래스 레벨 `@RequestMapping` 값을 모아 같은 방식으로 돌리면 된다
  (메서드 레벨만 모으면 클래스 접두어가 빠져 오탐·누락이 난다 - 이번에 겪었다).

### 같은 날 B안으로 전환 완료 - 화이트리스트 → 블랙리스트

위 13개를 추가한 뒤(A안), 사용자가 **근본 수정(B안)을 선택**해 바로 뒤집었다.

```java
.addPathPatterns("/**")
.excludePathPatterns(/* 공개 경로만 */)
```

이제 실패 방향이 **"빠뜨리면 열린다" → "빠뜨리면 막힌다"** 로 바뀐다.
조용한 정보노출보다 눈에 띄는 차단이 낫다는 판단.

**제외(공개) 목록은 실측으로 뽑았다** - 347개 경로를 세션 없이 전부 호출해 공개로 응답하던 것만:
- 정적: `/content/**` `/css/**` `/images/**` `/fonts/**` `/favicon.ico` `/uploads/**`
  `/admin/uploads/**` · **`/error`**(막으면 오류가 로그인 리다이렉트로 둔갑한다)
- 진입: `/admin/login(/**)` `/admin/logout` `/admin/access-denied` `/auth/cert-login(/**)`
  `/auth-swap` `/admin/manager-requests/{new,complete,confirm}` `/op_security_logout`
- 고객센터 공개: `/notices` `/data-board` `/events` `/faqs` `/qna` (+ 각 `/**`) `/policies/**` `/surveys/**`
- API: `/api/**` `/common/message(s)`

★ **인터셉터는 리소스 핸들러 요청에도 걸린다** - 정적 경로를 빼먹으면 CSS·JS가 로그인으로
튕겨 화면이 통째로 깨진다. 전환할 때 가장 위험한 지점이다.

**검증(전/후 전수 비교)**: 같은 347개 경로를 A안 jar와 B안 jar에서 각각 호출해 비교했다.
- **공개(200) 22개 집합이 완전히 동일**(diff 차이 없음)
- 달라진 것은 **5건뿐이고 전부 "존재하지 않는 경로"의 404 → 302**
  (`/form`, `/multipart/form-data`, 중복결합된 경로 등 - 라우트 추출 과정의 가짜 경로다).
  없는 admin 경로를 404로 알려주지 않게 된 것이라 오히려 낫다.
- 로그인 후 22개 대표 화면(통계·특정사업·커뮤니티·회원·고객센터 등) 전부 200,
  로그인 튕김 0, **정적자원이 302로 새는 일 0**.
- admin `test`+`bootJar` EXIT=0.

**How to apply(바뀐 규칙):** 이제 운영자 화면은 **아무것도 안 해도 보호된다.**
대신 **공개 화면/API를 새로 만들면 `excludePathPatterns` 에 반드시 추가**해야 한다.
안 하면 고객 화면이 로그인으로 튕긴다 - 증상이 바로 보이므로 놓치기 어렵다.

## admin-date-format-opdate-helper

> admin 날짜 표시는 web/support/OpDate 헬퍼(@opDate.ymd / ymdHms)로 통일. AS-IS는 yyyyMMddHHmmss 저장 + 화면에서 DATE_FORMAT. 등록/목록=날짜, 로그=일시


**2026-10-06, 사용자 지적으로 운영관리 날짜 표시 전수 교정.** 1406 상태/이력 팝업이 원시
`yyyyMMddHHmmss`(14자리)를 그대로 보여준 데서 시작해 "같은 패턴 다른 화면도 다 고치라"고 했다.

**원인:** AS-IS는 날짜를 `*_PNTTM`/`CREATED_DATE`(VARCHAR(14) yyyyMMddHHmmss)로 저장하고
**화면에선 매퍼의 `DATE_FORMAT(col,'%Y-%m-%d')`(또는 EL `op:date`)로 포맷**해 내려준다.
TO-BE는 그 값을 원시 문자열로 들고 있어 화면에서 포맷해야 하는데, 일부 화면이 `th:text="${x.createdDate}"`
로 원시 노출하고 있었다(14자리 또는 LocalDateTime ISO).

**공통 부품:** `admin/web/support/OpDate.java`(`@Component("opDate")`). 템플릿에서
`${@opDate.ymd(값)}`(yyyy-MM-dd) / `${@opDate.ymdHms(값)}`(yyyy-MM-dd HH:mm:ss). 숫자만 뽑아
앞 8/14자리로 포맷하므로 **14자리 문자열·LocalDateTime·이미 '-'포함 문자열 모두에 안전(멱등)**.
AS-IS `op:date`/`op:datetime`(DateUtils)의 TO-BE 대응이다. 새 화면도 이걸 쓴다.

**AS-IS 포맷 규칙(실측):**
- **날짜만(`%Y-%m-%d`, ymd)**: `FRST_REGIST_PNTTM`·`LAST_UPDT_PNTTM`·`CREATED_DATE` 등
  **등록/가입/목록** 날짜가 압도적 다수. 휴면회원 LOGIN_DATE도 `%Y-%m-%d`(실측).
- **일시(`%Y-%m-%d %H:%i:%s`, ymdHms)**: **로그인 로그 LOGIN_DATE**(login-log-mapper 실측),
  액션/메뉴사용/개인정보변경 로그, 문자/메일 발송로그(발송'일시'), 연계로그(부과/수납/등록'일시').
- 같은 컬럼명이라도 화면에 따라 날짜/일시가 갈리므로 **화면마다 AS-IS 표시 포맷을 확인**하고 고른다.
  추측 금지([[asis-porting-principles]]).

**이번에 고친 화면(ymd):** access, content/admin-notice, coupon/target-items·users,
designated/form·department-form·departments, gift-inquiries, gift-reviews, locgov-admin/list·dept-popup,
manual-admin, member-admin/list, mobile-category-edit, offgive, person-in-charge/locgov·oper,
point-history, reconciliation/order-point, search-admin, seo-admin, survey-admin, sleep-user,
shop-statistics 상세 4종, 1406 목록.
**(ymdHms):** log/login-list, user-login-list, action-log-list, manager-action-list, user-action-list,
user-change-list, send-log-admin/mail-detail·mail-list, levy-list·levy-stnd-buga·levy-stnd-sunap.
**제외:** `*Text` 필드(이미 컨트롤러에서 포맷), give-reqmng `frstRegisterId`(ID), order-admin(이미 포맷).

**미검증:** 런타임 클릭은 로그인 세션 필요로 못 함. `${@opDate...}`는 렌더 시점 SpEL이라
빌드/기동으로는 오타가 안 잡힌다 - 재기동 후 각 목록에서 날짜가 yyyy-MM-dd(로그는 시분초)로
나오는지 눈으로 확인 필요.

## menu-visibility-verify-against-live-asis

> admin 메뉴 노출규칙은 display_flag가 아니라 STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재 - AS-IS menu-mapper.xml이 DISPLAY_FLAG를 WHERE에 전혀 쓰지 않음. 2026-10-02 AS-IS export 전수대조로 확정·동기화 완료


**AS-IS opmanager nav 노출조건 = `MENU_TYPE=3 AND STATUS_CODE='1' AND OP_MENU_RIGHT에 내 롤 존재`.** `DISPLAY_FLAG`는 `menu-mapper.xml`의 SELECT 목록에만 있고 **WHERE에 단 한 번도 쓰이지 않는다**(`getFirstMenuList`/`getSecondAndThirdMenuList`). 권한조인을 건너뛰는 건 `ROLE_SUPERVISOR` 하나뿐이고, 그 롤은 TO-BE에 없다(`OpmanagerHandlerInterceptor:154,183`).

**Why:** TO-BE `MenuService`가 `display_flag='Y'`로 필터하면서 `ROLE_ADMIN_1~4`를 권한조인에서 우회시키고 있었다. 그래서 AS-IS엔 안 보이는 메뉴(권한행 0건)가 TO-BE엔 노출됐다 - 기부금 운영현황 6900/6901, 답례품 관리자 4701, 기부혜택증 설정 19102. "재시드 display_flag가 AS-IS와 불일치"로 적어뒀던 이전 판단은 **틀렸다**: 6900/6901은 AS-IS도 `display_flag='Y'`였고, 숨겨진 진짜 이유는 `OP_MENU_RIGHT` 행이 0건이었다.

**How to apply:** 메뉴 노출/숨김은 `display_flag`를 만지지 말고 **`op_menu_right` 행 유무 + `status_code`**로 판단한다. TO-BE 전용 신규 화면을 만들면 `op_menu_right`에 롤을 명시해야 nav에 나온다(안 넣으면 조용히 사라짐). nav는 엄격 판정(`MenuService.navVisible`), 화면 접근(`hasAccess`)만 ROLE_ADMIN_1~4 완화를 유지 → "nav 숨김 + URL 직접접근 가능". DB 변경은 즉시 반영(재기동 불필요), 코드 변경은 bootJar+재기동 필요.

**2026-10-02 전수 동기화 완료** (`database/ddl/migration-admin-menu-asis-sync.sql`, 멱등):
- 기준 export: `Desktop\고향사랑e음\1.AS-IS\1. DB\OP_MENU_202610010945.sql` / `OP_MENU_RIGHT_202610010946.sql` (**UTF-8**이다, EUC-KR 아님 - iconv 돌리지 말 것)
- AS-IS 355행 / TO-BE 182행 공통, AS-IS전용 173(대부분 SalesOn 잔재·권한0), TO-BE전용 1(5120 내부문의)
- display_flag 15건 AS-IS값으로 동기화, op_menu_right 누락 18행(ROLE_ADMIN_7/8/10) 보충, 초과 0건, 5120에 ROLE_ADMIN_1~4 부여
- 메뉴명 불일치 0건, 트리 불일치 0건(root parent NULL↔0 표현차뿐)
- **AS-IS에서 보이는데 TO-BE에 메뉴행조차 없는 것은 0개** - 차이는 전부 "행은 있고 menu_url 미등록 = 화면 미구현"
- 롤별 nav 리프 수: AS-IS 1=114/2=87/3=95/4=85/5=53/6=51 vs TO-BE 104/79/85/76/50/48 → **격차 10개가 곧 미구현 화면 수**

**미구현 10개(= TO-BE에 행은 있고 URL 없음)**: 메세지 관리(1402), Q&A(5112), 방문자접속경로(6102), 외국인 기부통계 전체/지자체별(6711/6712), 배치 실행로그(7209), 기금사업 등록/관리(14201), 신규주문·발송준비중(모바일)(16407/16408), 제철식품관(16751). 추가로 특산물관(16801, 롤3·4). 이것이 B군 잔여 백로그의 실측 목록이다. 관련: [[asis-porting-principles]] [[admin-screen-pitfalls]] [[admin-port-progress]](원장: docs/as-is-parity.md 22절 as-is-parity-audit-progress)

## admin-excel-no-column-direction-mismatch

> 엑셀 No 컬럼을 ++no(오름차순)로 짜면 화면의 내림차순 No와 어긋난다 - AS-IS는 화면마다 방향이 다르니 소스를 봐야 한다. 2026-10-14 사용자 지적으로 3곳 발견·수정


지자체별(sales/locgov)·농협 사업자 답례품 현황(sales/nh-item-sales) 엑셀을 새로 만들며 "No"
컬럼을 `int no = 0; ... String.valueOf(++no)`(1부터 오름차순)으로 짰는데, **화면은
`pagination.itemNumber - i.count`(총건수부터 내려오는 내림차순)**을 쓰고 있어서 화면 No.5가
엑셀에서는 No.1로 나오는 불일치가 생겼다. 사용자가 스크린샷으로 직접 대조해 지적했다.

**AS-IS를 다시 보니 화면마다 방향이 다르다** - 짐작으로 통일하면 안 된다:
- `ShopStatisticsMoisExcelView`/`ShopStatisticsLocgovExcelView`(지자체별 매출 엑셀 2종):
  `length - (rowIndex - 2)` → **내림차순**(화면과 일치해야 맞다)
- `MonthLocgovStatExcel`류(농협 사업자 답례품 현황의 컨트롤러 직접 로직): `idx = list.size(); idx--`
  → **내림차순**
- `StatisticsLocgovExcelView`(관심지자체): `++index` → **오름차순** - 이 화면은 AS-IS 자체가
  화면(내림차순)과 엑셀(오름차순)이 서로 다르다. 버그처럼 보이지만 AS-IS가 실제로 그렇다 -
  "맞춰서 고치면" 오히려 AS-IS parity를 깨는 것이니 그대로 둔다.

**How to apply**: 엑셀에 No 컬럼을 추가할 때 "화면과 같은 방향이겠지"로 짐작하지 말고, 반드시
AS-IS 엑셀 View 클래스(`*ExcelView.java`)의 No 로직을 직접 찾아 확인할 것. 다른 화면과
다르더라도 그게 AS-IS 실제 동작이면 그대로 포팅한다. 검증도 HTTP status/content-type만으로
끝내지 말고 POI로 직접 복호화해 셀 값을 읽어 화면과 대조해야 이런 방향 불일치를 잡을 수
있다(이번에 content-type만 확인하고 넘어갔다가 놓쳤다).

## admin-give-statistics-year-select-source

> 통계 화면의 년도 select는 YYYY 공통코드다 - 데이터에 있는 연도만 뽑으면 선택지가 한 개로 줄어 다른 해를 못 본다


AS-IS 통계 화면들(`give/statistics/all`, `give/statistics/locgov`)의 **년도 select 는
`CodeUtils.getCodeList("YYYY")` 공통코드**다. JSP가 `${yyyy}` 를 그대로 돌린다.

**TO-BE는 `giveStateService.availableYears()`**(기부 데이터에 실제로 있는 연도)를 쓰고 있었다.
개발DB에 2026년치만 있어서 **선택지가 "2026" 하나뿐**이었고, 다른 해를 볼 방법이 없었다.
화면이 멀쩡해 보여 눈치채기 어려운 종류의 갭이다(2026-10-13 교정).

- 코드표: `admin.op_common_code` 의 `YYYY` 타입 **10행(2022~2031)**, 그중 `use_yn='Y'` 는
  **2023~2026** 네 개다. AS-IS `getCodeList` 도 사용중인 것만 주므로 결과가 같다.
  (2027 이후는 N - 해가 바뀌면 공통코드 관리에서 켜는 운영 방식으로 보인다.)
- `GiveStatisticsController.yearCodes()` 로 뽑아 **전체(all)·지자체별(locgov) 두 화면이 공유**한다.
  코드가 비어 있으면 예전 방식(데이터 기준)으로 물러선다 - 화면이 통째로 못 쓰게 되는 것보다 낫다.
- 기본 선택은 AS-IS와 같이 **올해**(파라미터가 비면 `LocalDate.now().getYear()`).

**Why:** "데이터에 있는 연도만 보여주면 되지 않나"가 그럴듯해 보이지만, 그러면 **데이터가
없는 해는 조회 자체가 불가능**하다(0건인지 확인할 방법이 없다). AS-IS가 코드표를 쓰는 이유다.

**How to apply:** 통계·현황 화면에 연도 select 를 만들 때는 `YYYY` 공통코드를 쓴다.
다른 화면에도 같은 패턴이 남아 있는지 `availableYears()` 호출부를 확인할 것.

### ★차트 축은 서비스에서 빈 구간을 0으로 채워야 한다 (같은 날 2차 지적)

사용자가 **"시간대별 기부금액이 AS-IS는 0원이라도 그래프가 그려진다"** 고 지적.

AS-IS 쿼리는 `GROUP BY CNTR_HOUR` 라서 **기부가 있는 시간만** 나오고, 0건인 날은 **0행**이다.
그런데 AS-IS `GiveStatisticsServiceImpl` 이 쿼리 결과 위에 **루프를 돌며 빈 구간을 0으로 메운다**:
- `statisticsGiveAllTotalByHour` → `for (i=0; i<=23)` **24행 보장**
- `statisticsGiveAllTotalByMonth` → `for (i=1; i<=12)` **12행 보장**
- (`...ByAge` 도 10~70대 7행 보장 - 그 차트는 AS-IS가 주석처리 상태)

TO-BE `donation GiveStatService` 에는 이 보정이 없어서
- 시간대별: 데이터 없는 날 → **0행 → 축도 선도 없는 빈 그래프**(y축이 -1.0~1.0으로 뜬다)
- 월별: **데이터 있는 달만** x축에 찍혔다(실측 `1,2,3,5,6,8,9` - 4·7·10·11·12월 누락)

→ AS-IS와 같은 자리(서비스 계층)에 채우기를 넣었다. 실측으로 월별 12칸
`4,4,2,0,1,3,0,1,16,0,0,0`, 시간대별 24칸 전부 0(평평한 선이 그려짐) 확인.

**How to apply:** 차트용 집계 API를 만들 때 **GROUP BY 결과를 그대로 내보내지 말 것.**
축이 고정된 차트(월 12·시 24·나이대 7 등)는 서비스에서 빈 구간을 0으로 채워 내보낸다.
안 그러면 "데이터가 없는 날은 그래프가 아예 안 보이는" 증상이 된다.

**참고 - 전체(all) 화면 자체는 이미 AS-IS 이식이 끝나 있었다**: 요약 3카드 +
Chart.js 차트 5종(월별 기부건수 line / 월별 기부건수 bar(온·오프라인 누적) / 월별 기부인원 /
월별 기부금액 / 시간대별 기부금액 + 최근 7일 select), `op.chart.js`·`chart.min.js` 자산,
꼬리 `<style> .hourGraph canvas`, AS-IS가 주석처리한 나이대별 차트 2개 제외까지 그대로다.
**차트 제목이 1·2번 모두 "월별 기부건수"인 것도 AS-IS 그대로**(오타처럼 보이지만 원본이 그렇다).
실측: 카드 22,482,000 / 9 / 31, 캔버스 5개 렌더, 콘솔·HTTP 오류 0.

## admin-shell-asis-parity-restored

> admin 운영콘솔 레이아웃/폰트/JS 드리프트의 근본원인과 해결 - AS-IS 정적자산 통째 복사 + inc_header 중첩(.admin_wrap>#container>.contents>.contents_inner) + 공통 head 조각 + op.manager.js 브레드크럼


**근본원인(2026-10-02 해결):** admin 운영콘솔의 폰트/레이아웃/JS가 계속 AS-IS와 어긋나 매번 땜질하던 이유는 "정적자산을 골라 복사 + 셸 마크업을 이름 바꿔 재작성"이었다. 정적파일 자체는 있었는데 그걸 **활성화하는 AS-IS 뼈대**를 안 깔아 복사된 CSS/폰트/JS가 매칭 안 됐다.

**해결 3축:**
1. **정적자산 통째 복사** — AS-IS 루트 `C:\workspace\ghlove\ghlove-web\static`에서 admin `src/main/resources/static`로: `/content/opmanager`(195, 이미 있었음), `/content/modules`(273개 — 직전엔 **7개뿐**이라 op.main/op.validator/op.file/op.manager/jstree 등 거의 전부 누락 = 드리프트 주범), `/content/images`(228, favicon 포함). 나머지 `/content/{MagicLine4Web,mobile,popup,...}`은 사용자가 직접 복사(admin 무관 대용량). 정적파일은 jar에 패키징되므로 **복사 후 반드시 bootJar 재빌드**해야 반영.
2. **AS-IS 셸 중첩 복원** — `inc_header.jsp` 구조: `<html lang="ko" class="opmanager">` + `#header`(admin_wrap 밖) + `.admin_wrap > #container > (.lnb + lnb_handle) + .contents > .contents_inner`. 이 래퍼는 **생략 불가**: `.admin_wrap .mainArea{width:1440px}`(responsive 차트 폭)·`#container{position:relative}`(`.location` 브레드크럼이 position:absolute; top:90px; right:40px로 여기 기준). 예전 admin-nav.html 주석이 "overflow/position뿐이라 생략가능"이라 단정한 게 차트 미표시·브레드크럼 오위치의 실제 원인이었다(정정함).
3. **공통 head 조각** — `fragments/opmanager-head.html :: headContent`가 AS-IS inc_head 로드목록 verbatim(CSS 6 + JS 12: jquery-1.11/cookie/ui-1.10.4/spin/op.common/op.main/op.validator/op.file/op.shop/op.manager/op.manager.order/css_browser_selector/jstree/bootstrap.min) + csrf meta(X-CSRF-TOKEN/빈값, [[admin-screen-pitfalls]]). `<title>`은 미포함(페이지 자기 title 보존).

**브레드크럼은 서버계산 아님** — op.manager.js `$(function(){ Manager.setLnbHeader() })`(line 338)가 DOM ready에 nav(`.gnb li.on`/`.lnbs li.on`)에서 읽어 `.contents .location a` 3개(상단GNB>섹션>리프)를 채운다. 각 페이지는 `.contents_inner` 최상단에 빈 `.location` 조각만 두면 됨. breadcrumb fragment에 `th:if=${activeMenu != null}` → 메뉴 미등록 화면(예 `/site-config`, `/admin/mobile-category-edit` = op_menu 없음)에선 자동 숨김.

**fragments/admin-nav.html 구조:** `header()`(= #header), `lnb()`(= .lnb + lnb_handle), `breadcrumb()`, 그리고 하위호환용 `admin-nav()`(= header()+lnb() 합성, 미전환 2개 access-denied/cert-login만 사용). 전환 페이지 골격: `header()` → `<div class="admin_wrap"><div id="container">` → `lnb()` → `<div class="contents"><div class="contents_inner">` → `breadcrumb()` → 본문 → 4단 닫기 → footer.

**일괄 전환 방식:** Thymeleaf fragment는 본문을 못 감싸므로(span 불가, layout-dialect는 [[pl-shared-documents]] 걷어낼 때 버려지는 Thymeleaf 전용 종속이라 안 씀) 각 페이지가 래퍼 div를 직접 둘렀다. 변환은 Node 스크립트(scratchpad `shell_rollout.js`)로 `<div class="contents contents_inner">`의 짝 `</div>`를 **div 깊이 카운팅**으로 찾아 교체 + 파일별 div 균형 검증. **210개 전환 완료**(에러 0), 사용자가 목록/폼/차트 여러 화면 재기동 확인(500·콘솔에러 없음·브레드크럼 정상). 스탯/차트 4개(analysis-month/locgov, give-statistics-locgov/all, content-satisfaction/detail)는 본문끝 중복 jquery/spin/op.common 제거 후 전환(head 조각이 이미 로드).

**환경:** 이 PC엔 실제 Python 없음(MS Store 스텁) → 스크립트는 **Node(v24)** 사용. 이 작업은 순수 AS-IS HTML/정적자산이라 프레임워크 중립 = Thymeleaf 걷어내도 재사용됨(버려지지 않음). 관련 [[asis-porting-principles]] [[asis-porting-principles]].

## admin-breadcrumb-append-script-audit

> AS-IS가 상세/폼 화면마다 메뉴경로 끝에 크럼(상세/수정/등록/목록/변경/결과)을 덧붙이는 JS 전수조사(25개) 완료 - 8개 누락분 수정, 2026-10-08


[[admin-screen-pitfalls]]·admin-locgov-charger-edit-layout-and-breadcrumb-fix(보관: docs/archive/memory-fix-log-2026-10.md)에서
발견한 "JSP 스크립트 블록이 마크업만 옮기고 빠진" 패턴을 **전수조사**했다. AS-IS
`opmanager/i18n/**`에서 `div.location`에 크럼을 append하는 화면은 정확히 **25개**
(`grep -rl "div.location.*append"` 로 확정, 다른 셀렉터 변형 없음).

**이미 정상이던 17개**: `content/banner-form.html`(동적 navText), `give/give-statistics-operate-
detail.html`, `log/login-details.html`, `survey-admin/form.html`, `survey-admin/result.html`,
`locgov-admin/edit.html`, `locgov-admin/form.html`, `member-admin/details.html`,
`off-person-in-charge/edit.html`, `off-person-in-charge/form.html` + 이번/저번 라운드에 고친
`person-in-charge/locgov-edit.html`·`oper-edit.html`(별도 메모 참고).

**이번에 새로 찾아 고친 6개 파일(8개 AS-IS 화면 - list.jsp/list_locgov.jsp가 TO-BE 1개
템플릿 공유)**:
| AS-IS | 덧붙이는 크럼 | TO-BE 파일 | 이전 상태 |
|---|---|---|---|
| give-operation/detail.jsp | 상세 | give/give-operation-detail.html | `<script>` 있었지만 이 호출이 없었음 |
| give-point/form.jsp | 상세 | give/give-point-detail.html | `<script>` 태그 자체가 없었음 |
| give-reqmng/list.jsp + list_locgov.jsp | 목록 | give/give-reqmng-list.html | `<script>` 있었지만 이 호출이 없었음 |
| give-reqmng/req_form.jsp | 변경 | give/give-reqmng-form.html | `<script>` 있었지만 이 호출이 없었음 |
| give-state/form.jsp | 상세 | give/give-state-detail.html | `<script>` 태그 자체가 없었음 |
| statistics/locgov/detail.jsp | 상세 | statistics-locgov/detail.html | `<script>` 태그 자체가 없었음 |

**의도적으로 안 건드린 6개(기존 결정 확인)**: `give/statistics/{amount,date,number,person,
personal}/detail.jsp`는 2026-10-02 사용자 확인으로 `give-statistics.html` 하나로 통합됐다
(개별 "상세" 화면 자체가 없어 적용 대상 아님). `log/user/login-log-details.jsp`는 member
서비스의 로그인 세션 단위 조회 API가 없어 미이식 상태(화면 자체가 없음, `log/user-login-
list.html` 주석에 기록됨).

**검증**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**

## admin-lnb-toggle-script-missing

> LNB 섹션 헤더(예: '시스템 관리') 클릭 시 하위메뉴 접기/펼치기 스크립트가 통째로 빠져있던 것을 복원(2026-10-08)


AS-IS `layouts/opmanager/inc_header.jsp`에 섹션 헤더 클릭 토글 스크립트가 있다:

```js
function Lnb(){
    var menu_a = $('.menu > a');
    menu_a.click(function(e) {
        e.preventDefault();
        if (!$(this).hasClass('on')) {
            $(this).addClass('on').next().stop(true,true).slideDown('800');
        } else {
            $(this).removeClass('on');
            $(this).next().stop(true,true).slideUp('800');
        }
    });
}
Lnb();
```

`.menu > a`(LNB 2차 섹션 헤더, 예: "시스템 관리")를 클릭하면 바로 아래 `.depth2`(3차 리프 목록)를
slideDown/slideUp으로 접고 펼친다. TO-BE `fragments/admin-nav.html`의 `lnb()` fragment는
AS-IS와 같은 `.menu > a.on` + `.depth2` 마크업은 이미 옮겨놨지만 **이 스크립트 자체가 아예
없었다** - 사용자가 "클릭해도 토글이 안 되는 것 같다"고 지적해서 발견. 클릭해도 `href="#"`가
없어(서버 렌더 `<a class="on">`에 href 자체가 없음) 아무 반응이 없었다.

**수정**: `admin-nav.html`의 `lnb()` fragment 안, `메뉴 닫기` 앵커 다음에 같은 스크립트를
`th:if="${activeTopMenuId != null}"`로 감싸 추가(AS-IS도 `<c:if test="${main != 'main'}">`
안에만 있다 - lnb 자체가 없는 화면에선 스크립트도 필요 없음).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-privacy-access-modal-reasontype-dropdown

> 개인정보 엑셀 다운로드 사유 모달에 분류코드(#reasonType) select 신규추가 - AS-IS는 공통JS가 열 때마다 DOM에 끼워넣어서 마크업만 보고 '없다'고 오판했던 것, 2026-10-08


**발견 경위**: 오프라인담당자 목록(4601) "전체 목록 엑셀 다운로드" 버튼으로 사용자가 지적.
AS-IS `include/modal/privacy-access.jsp`(공통 모달) 마크업 자체에는 실제로 분류코드 select가
없다 - **예전 조사(코드주석에 남아있던 "REASON_TYPE은 실제로 비어있다" 결론)가 틀렸던 건 아니고
불완전했다.** 진짜 출처는 `op.shop.js`의 `Shop.downloadExcelOrder(url, queryString,
isPrivacyAccess)` 공통 함수 - 모달을 열기 **직전에** `#reasonType`이 없으면 전역 `reasonType`
객체(1~5, `기부금 모금 실적 확인`/`답례품 주문내역 확인`/`택배 송장 출력 및 배송확인`/
`답례품 정산 및 지급 확인`/`기타`)로 select를 만들어 `.modal-body` 바로 앞에 DOM으로 끼워
넣는다. 오프라인담당자 목록의 "전체 목록 엑셀 다운로드"는 `Shop.downloadExcelOrder()`를 거쳐서
열리므로 분류코드가 보이고, 마크업만 보면 없는 것처럼 보인다 - **이 함수는 AS-IS 전역에서
엑셀다운로드 버튼이 있는 화면 대부분이 쓴다**(70개 이상 JSP에서 참조).

**TO-BE는 이미 백엔드가 다 준비돼 있었다** - 1411(엑셀다운로드사유관리) 수정이력 팝업이
`PrivacyAccessLogService.reasonTypeOptions()`로 공통코드 `EXCELDOWNLOAD_REASON_TYPE`(1~5,
AS-IS 전역 reasonType 값과 정확히 동일)을 이미 읽고 있었고, `/common/opmanager/
privacy-access-log` 엔드포인트도 `reasonType` 파라미터를 이미 받아 저장하고 있었다. **빠진
건 다운로드를 처음 트리거하는 공통 모달(`fragments/privacy-access.html`)에 이 select를
보여주는 부분 하나뿐**이었다.

**조치**: AS-IS처럼 "매 화면마다 JS로 즉석 주입"하지 않고, TO-BE 관례대로 **서버에서 한 번에
렌더링**했다.
- `ManagerAuthAdvice`에 `@ModelAttribute("reasonTypeOptions")` 추가 - `PrivacyAccessLogService.
  reasonTypeOptions()`를 모든 화면에 전역 주입(이 모달을 쓰는 화면이 많아 컨트롤러마다
  일일이 넣을 필요 없게, `msg`/`navTree`와 같은 패턴).
- `fragments/privacy-access.html`에 `<select id="reasonType" name="reasonType" title="엑셀
  다운로드타입" class="wd-400 required">` 추가(`.modal-body` 바로 앞 - AS-IS 삽입 위치와 동일),
  `th:each="entry : ${reasonTypeOptions}"`로 1411과 같은 옵션을 뿌린다.
- 변경 핸들러(AS-IS verbatim) - 분류코드를 고르면 그 라벨 텍스트를 사유(`#reason`) textarea에
  그대로 채운다.
- `openPrivacyModal()`이 열 때마다 `#reasonType`도 빈 값으로 리셋(AS-IS의 "이미 있으면
  값만 초기화" 분기와 동일 효과).
- `name="reasonType"`이라 폼 제출(`$form.serialize()`) 시 자동으로 `/common/opmanager/
  privacy-access-log`에 실려가 저장된다 - 서버 쪽은 손대지 않았다(이미 받고 있었음).

**영향 범위**: 공통 모달 하나를 고쳤으므로 이 모달을 쓰는 화면 전부(오프라인담당자·메뉴관리·
주문관리·답례품리뷰·엑셀다운로드로그 등)에 동시에 적용된다 - AS-IS도 전역 공통함수라 전부
같은 동작이었다.

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**
