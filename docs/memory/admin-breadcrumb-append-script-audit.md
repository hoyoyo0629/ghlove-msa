---
name: admin-breadcrumb-append-script-audit
description: "AS-IS가 상세/폼 화면마다 메뉴경로 끝에 크럼(상세/수정/등록/목록/변경/결과)을 덧붙이는 JS 전수조사(25개) 완료 - 8개 누락분 수정, 2026-10-08"
metadata:
  node_type: memory
  type: project
---

[[admin-lnb-toggle-script-missing]]·[[admin-locgov-charger-edit-layout-and-breadcrumb-fix]]에서
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
