---
name: admin-shell-asis-parity-restored
description: "admin 운영콘솔 레이아웃/폰트/JS 드리프트의 근본원인과 해결 - AS-IS 정적자산 통째 복사 + inc_header 중첩(.admin_wrap>#container>.contents>.contents_inner) + 공통 head 조각 + op.manager.js 브레드크럼"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-02T03:53:31.714Z
---

**근본원인(2026-10-02 해결):** admin 운영콘솔의 폰트/레이아웃/JS가 계속 AS-IS와 어긋나 매번 땜질하던 이유는 "정적자산을 골라 복사 + 셸 마크업을 이름 바꿔 재작성"이었다. 정적파일 자체는 있었는데 그걸 **활성화하는 AS-IS 뼈대**를 안 깔아 복사된 CSS/폰트/JS가 매칭 안 됐다.

**해결 3축:**
1. **정적자산 통째 복사** — AS-IS 루트 `C:\workspace\ghlove\ghlove-web\static`에서 admin `src/main/resources/static`로: `/content/opmanager`(195, 이미 있었음), `/content/modules`(273개 — 직전엔 **7개뿐**이라 op.main/op.validator/op.file/op.manager/jstree 등 거의 전부 누락 = 드리프트 주범), `/content/images`(228, favicon 포함). 나머지 `/content/{MagicLine4Web,mobile,popup,...}`은 사용자가 직접 복사(admin 무관 대용량). 정적파일은 jar에 패키징되므로 **복사 후 반드시 bootJar 재빌드**해야 반영.
2. **AS-IS 셸 중첩 복원** — `inc_header.jsp` 구조: `<html lang="ko" class="opmanager">` + `#header`(admin_wrap 밖) + `.admin_wrap > #container > (.lnb + lnb_handle) + .contents > .contents_inner`. 이 래퍼는 **생략 불가**: `.admin_wrap .mainArea{width:1440px}`(responsive 차트 폭)·`#container{position:relative}`(`.location` 브레드크럼이 position:absolute; top:90px; right:40px로 여기 기준). 예전 admin-nav.html 주석이 "overflow/position뿐이라 생략가능"이라 단정한 게 차트 미표시·브레드크럼 오위치의 실제 원인이었다(정정함).
3. **공통 head 조각** — `fragments/opmanager-head.html :: headContent`가 AS-IS inc_head 로드목록 verbatim(CSS 6 + JS 12: jquery-1.11/cookie/ui-1.10.4/spin/op.common/op.main/op.validator/op.file/op.shop/op.manager/op.manager.order/css_browser_selector/jstree/bootstrap.min) + csrf meta(X-CSRF-TOKEN/빈값, [[admin-opmanager-ajax-needs-csrf-meta]]). `<title>`은 미포함(페이지 자기 title 보존).

**브레드크럼은 서버계산 아님** — op.manager.js `$(function(){ Manager.setLnbHeader() })`(line 338)가 DOM ready에 nav(`.gnb li.on`/`.lnbs li.on`)에서 읽어 `.contents .location a` 3개(상단GNB>섹션>리프)를 채운다. 각 페이지는 `.contents_inner` 최상단에 빈 `.location` 조각만 두면 됨. breadcrumb fragment에 `th:if=${activeMenu != null}` → 메뉴 미등록 화면(예 `/site-config`, `/admin/mobile-category-edit` = op_menu 없음)에선 자동 숨김.

**fragments/admin-nav.html 구조:** `header()`(= #header), `lnb()`(= .lnb + lnb_handle), `breadcrumb()`, 그리고 하위호환용 `admin-nav()`(= header()+lnb() 합성, 미전환 2개 access-denied/cert-login만 사용). 전환 페이지 골격: `header()` → `<div class="admin_wrap"><div id="container">` → `lnb()` → `<div class="contents"><div class="contents_inner">` → `breadcrumb()` → 본문 → 4단 닫기 → footer.

**일괄 전환 방식:** Thymeleaf fragment는 본문을 못 감싸므로(span 불가, layout-dialect는 [[admin-frontend-is-thymeleaf-not-jsp]] 걷어낼 때 버려지는 Thymeleaf 전용 종속이라 안 씀) 각 페이지가 래퍼 div를 직접 둘렀다. 변환은 Node 스크립트(scratchpad `shell_rollout.js`)로 `<div class="contents contents_inner">`의 짝 `</div>`를 **div 깊이 카운팅**으로 찾아 교체 + 파일별 div 균형 검증. **210개 전환 완료**(에러 0), 사용자가 목록/폼/차트 여러 화면 재기동 확인(500·콘솔에러 없음·브레드크럼 정상). 스탯/차트 4개(analysis-month/locgov, give-statistics-locgov/all, content-satisfaction/detail)는 본문끝 중복 jquery/spin/op.common 제거 후 전환(head 조각이 이미 로드).

**환경:** 이 PC엔 실제 Python 없음(MS Store 스텁) → 스크립트는 **Node(v24)** 사용. 이 작업은 순수 AS-IS HTML/정적자산이라 프레임워크 중립 = Thymeleaf 걷어내도 재사용됨(버려지지 않음). 관련 [[copy-asis-css-js-assets-verbatim]] [[check-as-is-source-when-analyzing]].
