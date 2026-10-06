---
name: admin-date-format-opdate-helper
description: "admin 날짜 표시는 web/support/OpDate 헬퍼(@opDate.ymd / ymdHms)로 통일. AS-IS는 yyyyMMddHHmmss 저장 + 화면에서 DATE_FORMAT. 등록/목록=날짜, 로그=일시"
metadata:
  node_type: project
  type: project
---

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
  추측 금지([[no-invented-features-ask-first]]).

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
