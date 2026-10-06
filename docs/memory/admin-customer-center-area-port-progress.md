---
name: admin-customer-center-area-port-progress
description: 고객센터 영역(11화면) 이식 원장 - AS-IS 대조표·구조갭 6건·TO-BE 축소 실측
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-05T04:19:44.588Z
---

[[asis-screen-port-procedure]] 절차로 진행하는 **네 번째 영역 = 고객센터 11화면**(2026-10-04 착수).
앞선 영역: 시스템관리 25/25([[admin-system-area-port-progress]]) · 회원관리 8/8([[admin-member-area-port-progress]]) ·
소규모 묶음([[admin-small-areas-port-progress]] - 기부혜택증3·대시보드1·커뮤니티6 완료).

**남은 영역 실측**(op_menu status_code='1', menu_type=3 기준): 기부금관리 6 · 특정사업 5 · **고객센터 11** · 통계 25 · 답례품관리 30.

## AS-IS ↔ TO-BE 대조표 (2026-10-04 실측)

| id | 메뉴 | AS-IS url · 컨트롤러(줄) | TO-BE url · 컨트롤러(줄) | 상태 |
|---|---|---|---|---|
| 5110 | 지자체공지사항 | `/opmanager/locgov-notice/**` · LocgovNoticeManagerController **259** | `/admin/notices` · OperationContentController 477(공유) | **★URL 충돌** |
| 5111 | 공지사항 | `/opmanager/notice/**` · NoticeManagerController **223** | `/admin/notices` (같음) | **★URL 충돌** |
| 5107 | 자료실 | `/opmanager/data-board/**` · DataboardManagerController **294** | `/admin/data-board` · DataBoardAdminController 124 | 축소 |
| 5102 | 1:1 문의 | `/opmanager/qna` · QnaManagerController **539** | `/admin/shop-inquiries` · ShopInquiryAdminController **36** | **대폭 축소** |
| 5112 | Q&A | `/opmanager/qna-open` · QnaOpenManagerController **575** | **menu_url 비어 있음** | **★미구현** |
| 5104 | FAQ | `/opmanager/faq` · FaqManagerController **247** | `/community/faq-bbs` | **★URL 충돌(11405와) + 미구현** |
| 5113 | 운영관리 SR게시판 | `/opmanager/maintenance/` · MaintenanceController **443** | `/admin/maintenance` · MaintenanceAdminController 236 | 축소 |
| 5114 | 답례품제공자 공지사항 | `/opmanager/sellerNotice` · SysNoticeSellerController **290** | `/admin/seller-notices` · SysNoticeSellerAdminController 203 | 축소 |
| 5120 | 내부문의 관리 | **AS-IS op_menu에 없음**(실측 0건) | `/admin/internal-inquiry` · 134 | **★TO-BE 추가 - 근거 확인 필요** |
| 5201 | 관리자매뉴얼 | `/opmanager/manual/manager/list` · ManualManagerController **410**(5202와 공유) | `/admin/manuals` · ManualAdminController 111 | **★URL 충돌** |
| 5202 | 사용자매뉴얼 | `/opmanager/manual/list` (같은 컨트롤러) | `/admin/manuals` (같음) | **★URL 충돌** |

AS-IS 컨트롤러 합계 **약 3,280줄** vs TO-BE 약 **900줄** → 라인 기준 약 27%.
(5108 만족도 조사는 parent가 **6100**이라 고객센터 소속이 아니다 - AS-IS `/opmanager/cntnts-stsfdg` 204줄 vs TO-BE 38줄.)

## 구조갭 6건 (먼저 정리할 것)
1. **5110 vs 5111** 둘 다 `/admin/notices` - AS-IS는 지자체공지(locgov-notice)와 공지(notice)가 **별도 컨트롤러·별도 표**다.
2. **5201 vs 5202** 둘 다 `/admin/manuals` - AS-IS는 같은 컨트롤러의 `manager/list`와 `list`로 갈린다.
3. **5104 FAQ** → `/community/faq-bbs`가 **11405 담당자용 FAQ와 충돌**. 자기 URL이 필요하다.
4. **5112 Q&A** → menu_url이 빈 값이라 클릭 자체가 안 된다(AS-IS는 `/opmanager/qna-open/list`).
5. **AS-IS 중지 메뉴 5건이 TO-BE에 아예 없다** - 5101 상품평 관리 · 5103 상품문의 · 5105 민원신고 ·
   5106 행사이벤트 · 5109 매뉴얼관리(구). 전부 `display_flag='N', status_code='2'`다.
   [[as-is-parity-includes-disabled-state]]대로 **중지 상태로 등록**해야 한다(11403 선례와 동일).
6. **5120 내부문의 관리는 AS-IS 메뉴에 없다** - TO-BE 추가물이다. [[scope-migration-not-greenfield]]
   ("AS-IS 로직 + RFP/ISP 신규기능만, 근거 없는 추가 금지")에 걸리므로 RFP/ISP 근거를 확인해야 한다.
   삭제는 사용자 판단(이미 134줄 구현돼 있다).

## 이미 찾아 둔 live 결함 (커뮤니티 라운드에서 발견)
- **`FAQ_TYPE` 공통코드 11행이 DB에 없다**: `database/ddl/service-admin.sql`(4962행~)이
  `JOIN`·`DONATE`·`POINT`·`OFFLINE`·`DESIGNATED`·`TAX`·`GIFT`·`ORDER`·`PRIVATE`·`SYSTEM`·`ETC`를
  넣으려는데 **`ADMIN_COMMON_CODE`라는 없는 표 이름**으로 INSERT한다 → 실제 `admin.op_common_code`의
  `FAQ_TYPE`에는 쇼핑몰용 1~6만 있다. 그래서 `op_community_locgovfaq` **63행의 질문유형이
  공개 FAQ(`/faqs`)·관리화면 양쪽에서 빈 값으로 보인다**. 그 11행을 올바른 표에 넣으면 해결.
  (담당자용 FAQ(11405)는 `CMNTY_FAQ_TYPE`을 쓰므로 무관 - [[admin-small-areas-port-progress]])
- **11403 지자체FAQ는 종결됨**: AS-IS 중지 메뉴라 메뉴 등록만 하고 화면은 손대지 않기로 결정(사용자 ②).
  그 화면(`LocgFaqAdminController`, `/community/locv-faq`)이 **공개 FAQ 63행의 관리화면**이다 - 다시 열지 말 것.

## ① 구조갭 정리 - 진행 결과 (2026-10-05)

**(a) AS-IS 중지 메뉴 5건 등록 완료** (`migration-admin-menu-5100-stopped-menus.sql`, 적용):
5101 상품평 관리 · 5106 행사이벤트 · 5103 상품문의 · 5105 민원신고 · 5109 매뉴얼관리(구).
전부 `display_flag='N', status_code='2'`라 nav·breadcrumb에 영향 없고 화면 이식 대상도 아니다(11403 선례와 동일).
op_menu_right은 AS-IS에도 없어 추가하지 않았다.

**(b) ★live 결함 해결 - 공개 FAQ 질문유형이 전부 빈 값이던 것** (`migration-admin-faq-type-codes-fix.sql`, 적용):
- `FAQ_TYPE` 11행 추가(id는 `op_community_locgovfaq.faq_type`에 실제 저장된 짧은 코드 JOIN/DONATE/…,
  라벨은 AS-IS enum `FaqType` 그대로) + **SalesOn 잔재 1~6은 use_yn='N'으로 숨김**(삭제 아님).
  `labelsOf()`가 use_yn='Y'만 추리므로 화면엔 11종만 보인다. FAQ_TYPE 소비처는 `/faqs`·`/api/faqs`·
  `/community/locv-faq` 세 곳뿐이고 모두 이 11종이 맞는 화면이다(실측).
- 데이터 1행 보정: id=1000 '로그인이 안 돼요'가 faq_type='1'(잔재 코드)이어서 'JOIN'으로 맞췄다.
- **검증: 라벨 못 찾는 행 63 → 0**.

**(c) 공지 2화면 분리 완료** (`migration-admin-menu-5110-locgov-notice-split.sql`, 적용 + 코드):
- **AS-IS 분리 규칙 확정**(매퍼까지 실측): 표는 하나(`op_notice`)이고 조회범위만 다르다.
  5111 공지사항 = 검색조건 지자체코드를 **'00000' 고정** → 전체공지만 /
  5110 지자체공지사항 = **LOC 담당자일 때만** 자기 지자체로 스코프, **시스템·행안부는 조건 없이 전부**
  (전체공지 포함 - AS-IS `getLocgovNoticeList`의 조건부 WHERE 그대로). 목록에 지자체명 표시.
- TO-BE: `5110 → /admin/locgov-notices`로 분리. `OperationContentController`에
  `adminLocgovNotices()` 추가 + `fillNoticeList(category, wholeNoticeOnly, …)` 공유.
  템플릿은 하나를 공유하고 `screenTitle`·`listUrl`만 갈린다. 등록·수정·삭제·토글은
  `/admin/notices/...` 한 경로 공유(AS-IS도 같은 표의 같은 자원).
- **★경계 번역**: AS-IS는 전체공지를 `locgov_code='00000'`으로 쓰는데 **TO-BE 데이터는 비워 두었다**
  (실측 빈값 14행 / 지자체코드 16행) → `isWholeNotice()`가 NULL·빈값·'00000'을 모두 전체공지로 본다.
  데이터를 고치지 않은 이유: 공개 공지 화면(`/notices`)이 NULL을 전체로 보고 살아 있어서 건드리면 위험하다.
- WebConfig에 `/admin/locgov-notices` 게이트 추가.

**(d) 매뉴얼 5201/5202 URL 분리는 ②로 넘김** - 이유 2가지:
- **표가 다르다**: AS-IS 매뉴얼은 **`g_mnl`**(mnl_sn·menu_se_code·menu_url·menu_nm·file_nm·file_ty·
  inqire_co·menu_sj·menu_cn)인데 TO-BE는 **자체 설계한 `op_manual`**(title·content·menu_url_code·file_src, 0행)을 쓴다.
  **`g_mnl`은 TO-BE admin 스키마에 이미 있다(0행)** - 커뮤니티 게시판과 똑같은 "AS-IS 표 있는데 골격이 발명" 상황이라
  ②에서 `g_mnl`로 옮겨야 한다. `menu_se_code`는 `op_common_code(code_type='MENU_URL')`와 조인하는
  **대상 메뉴 코드**이고(TO-BE에 33행 있음) 관리자/사용자 구분자가 아니다.
- **분리 규칙이 아직 불명확**: 5201 관리자매뉴얼은 `getAllMenuList()`로 **메뉴 트리(2차→3차) 표**를 그리고,
  5202 사용자매뉴얼은 `getManualList()`로 **검색 목록**을 그린다. 같은 g_mnl 행을 다른 뷰로 보는 것으로 보이는데
  구분 조건을 아직 확정하지 못했다 → 규칙을 확정한 뒤 URL을 나누는 것이 맞다(먼저 나누면 없는 구분을 발명하게 된다).
- 현재 TO-BE 화면(`/admin/manuals`, 평면 목록)은 5202 쪽에 가깝다.

**(e) 5104 FAQ의 URL 변경은 ②(구현)와 함께** - 지금 URL만 옮기면 404가 된다.
- 5104 FAQ: AS-IS 표는 **`OP_FAQ`**(entity `saleson.model.Faq`, faqType=enum FaqType). TO-BE `op_faq` **0행**.
  11403 지자체FAQ(`op_community_locgovfaq`, 63행)와 **다른 표**다 - 혼동 금지.

**(f) 5112 Q&A의 menu_url 빈 값 해소 완료** (`migration-admin-menu-5112-qna-url.sql`, 적용):
`5112 → /qna-admin`. **표 DDL은 필요 없었다**(아래 정정 참고).

## ★ 앞선 추정 정정 2건 (2026-10-05 실측)
1. **"5112 Q&A의 표가 TO-BE에 없다"는 틀렸다.** Q&A는 1:1 문의와 **같은 표를 공유**한다 -
   `op_qna`/`op_qna_answer`/`op_qna_file`/`op_qna_answer_file`(AS-IS 두 컨트롤러가 같은 `QnaService`를 쓴다).
   TO-BE에 네 표 모두 있고 `op_qna` 9행·`op_qna_answer` 4행이 들어 있다. **DDL 신설 불필요.**
   - 구분자는 **`qna_type`**: 1:1문의='0'(INDIVIDUAL) / 상품문의='1'(ITEM) / Q&A='2'(QNA).
     **5112는 AS-IS가 그 필터를 주석 처리**해 두어 사실상 전체를 본다 → [[as-is-parity-includes-disabled-state]]대로
     주석 상태를 유지한다(필터를 켜지 말 것).
   - ★**TO-BE 9행은 qna_type이 전부 NULL**이고 `qna_group`에 3·6·7·8·9·10이 들어 있다 →
     5102가 `qna_type='0'`으로 거르면 **0건**이 된다. 공지 '00000'과 같은 경계 번역이 필요하다(②에서 처리).
   - TO-BE에서 이 표를 쓰는 것: 공개 Q&A 게시판(`QnaController`·`QnaApiController`) + 관리화면 `/qna-admin`(60줄).
     **1:1문의 화면(`/admin/shop-inquiries`)은 `op_qna`가 아니라 TO-BE가 만든 `OP_SHOP_INQUIRY`를 쓴다**
     → ②에서 `op_qna`로 옮겨야 한다(커뮤니티·매뉴얼과 같은 "AS-IS 표 있는데 골격이 발명" 패턴, 세 번째 사례).
2. **"5120 내부문의 관리는 근거 없는 TO-BE 추가"도 틀렸다.** AS-IS에 **`/opmanager/qna-admin`
   (`QnaAdminManagerController`)** 화면이 실재하고 표도 **`g_qna_admin`/`_answer`/`_file`**로 AS-IS 덤프에 있다
   (TO-BE `InternalInquiryAdminController`가 이미 그 표를 쓰고 주석에도 AS-IS 출처를 적어 두었다).
   **AS-IS op_menu export에 행이 없을 뿐**이고 화면 자체는 AS-IS다 → 근거 없는 추가가 아니다.
   TO-BE가 메뉴 행을 만들어 접근 경로를 준 것은 타당하다. **gap 6 해소.**
   - ★**이름 충돌 주의**: TO-BE `/qna-admin` = **공개 Q&A(5112)** 관리화면,
     AS-IS `/opmanager/qna-admin` = **내부문의(5120)**. TO-BE 내부문의는 `/admin/internal-inquiry`다.

## ② 구현 순서 (사용자 승인)
**5112 Q&A → 5104 FAQ → 매뉴얼 2화면(g_mnl 이전+분리) → 1:1문의 보강**

**5112 Q&A 범위 실측**: AS-IS `QnaOpenManagerController` 575줄 / **엔드포인트 16개**
(list GET·POST / delete 일괄 / view/{id} / answer/{id} GET·POST / **download-excel GET·POST** /
**upload-excel GET·POST** / edit/{id} GET·POST / delete/{id} / delete/{id}/answer/{answerId} /
delete-item-image / file-download/{fileId}/{type}) + JSP 6종 **1,164줄**
(list 378 · form 239 · view 215 · answer 183 · download-excel 84 · upload-excel 65).
TO-BE `/qna-admin`은 **3개**(list·detail·answer)뿐 → 13개 + 엑셀 업·다운로드 + 첨부파일이 추가 대상.

### 5112 Q&A 진행 (2026-10-05) - 목록·답변화면·일괄삭제 완료, admin·member bootJar EXIT=0
**신규/수정**
- admin: `repository/QnaOpenAdminRepository`(AS-IS 목록 SQL 이식), `service/QnaOpenAdminService`,
  `web/support/QnaOpenSearchParam`, `web/QnaAdminController` 확장(GET/POST 목록 · `POST /qna-admin/delete` ·
  `GET /qna-admin/answer/{qnaId}`), 템플릿 `qna-admin/{list,answer-form}.html` 신규.
- admin `domain/Qna`에 **누락 컬럼 3개 매핑 추가**: `qna_type`·`data_status_code`·`use_yn`
  (기존 엔티티가 부분 매핑이라 Q&A 조회·삭제에 필요한 컬럼이 없었다).
- member: `AdminMemberService.userLoginIds()`·`userIdsByLoginIdLike()` +
  `GET /api/admin/members/login-ids`·`/api/admin/members/ids-by-login`,
  `UserRepository.findByLoginIdContainingAndStatusCode`. admin `MemberAdminClient`에 대응 메서드 2개.
  → AS-IS가 `OP_USER`를 조인해 `LOGIN_ID`를 뽑는 자리를 member 조회로 대체(회원은 member 소유).
  **검색구분 '아이디'는 member에서 회원ID를 먼저 받아 `user_id IN (...)`으로 거른다.**

**AS-IS 고정조건/동작 그대로**: `qna_type='2'` · `data_status_code='0'` · `display_flag='Y'` · `use_yn='Y'` /
진입(GET) 빈 목록·검색(POST)에서만 조회 / 정렬은 컨트롤러가 `CREATED_DATE`,`DESC`로 고정하는데
**매퍼 CASE가 CREATED_DATE를 QNA_ID로 매핑**해 실제로는 qna_id 기준이다(그대로 둠) /
작성자명 마스킹 `첫글자+"*"+세번째부터`(두 글자면 "김*") / 답변여부는 answer_count로 판정 /
날짜조건은 varchar(14)에 `000000`·`235959`를 붙여 문자열 비교 / 이 화면만 날짜 기준값이 **month3**(다른 화면은 month2).

**AS-IS 비활성 그대로 유지**: 답변을 행으로 펼치는 **UNION ALL 주석처리**(그래서 '구분' 컬럼은 항상 'Q',
제목 앞 답변 아이콘은 절대 안 나옴) / 검색구분 '전체(ALL)'·'이름(USER_NAME)' 주석 / 문의유형 select·answerCount 라디오 주석 /
**엑셀 다운로드는 함수만 있고 호출 버튼이 없다**(게다가 submit 대상 `#listForm`의 action이 1:1문의 목록을 가리키는 복사 흔적) → 버튼 만들지 않음 /
`qnaTypes` 모델값은 항상 빈 목록(`QNA_GROUPS.code_value`가 비어 존재하지 않는 코드유형을 찾는다 - SalesOn 2단분류 잔재).

**AS-IS 규칙(중요)**: 일괄삭제는 **답변이 없는 건만** 지우고 답변 있는 건은 **조용히 건너뛴다**.
삭제는 `DATA_STATUS_CODE='1'` 소프트 삭제다. 답변 제목은 화면에 없고 **"답변입니다."가 hidden 고정값**이다.

**AS-IS 결함 1건 고침**: 마스킹이 `substring(2)`를 무조건 호출해 **한 글자 이름이면 목록 전체가 500**이 된다 → 두 글자 이상만 마스킹.

**데이터 보정 1건**(`migration-admin-qna-type-backfill.sql`, 적용): 시드 9행의 `qna_type`이 전부 NULL이라
AS-IS 조건(`qna_type='2'`)으로는 0건이었다. 내용·용도상 전부 공개 Q&A이고 공개게시판(`/qna`)이 읽는 행이라
**SQL을 느슨하게 바꾸는 대신 데이터에 '2'를 채웠다**.

**내가 만든 버그 1건 - 즉시 수정**: `op_qna_answer.answer_date`가 **varchar**인데 `LocalDateTime`으로
캐스팅해 런타임에 터질 코드였다(빌드는 통과). String으로 바꾸고 화면에서 포맷한다.
`QnaAnswer`에는 `answerUserName`이 없어 `userId`로 운영자명을 조회해 모델에 싣는다. `QnaFile`은 `orgFileName`이다.

**검증(SQL 대조)**: 목록 9건·문의유형 라벨 매핑·답변건수·답변일 / 답변완료 4·미답변 5 /
제목 LIKE '배송' 2 / 문의유형 LIKE '답례품' 2 / 등록일 20260715~ 4건 - 전부 일치.

### 5112 2차 (2026-10-05) - 삭제·첨부 엔드포인트 4개 추가, admin bootJar EXIT=0
- `GET /qna-admin/delete/{qnaId}` (문의글 삭제) · `GET /qna-admin/delete/{qnaId}/answer/{qnaAnswerId}` (답변 삭제) ·
  `POST /qna-admin/delete-item-image` (첨부 삭제) · `GET /qna-admin/file-download/{qnaFileId}/{qnaDetailType}` (첨부 다운로드).
  서비스에 `deleteQna`·`deleteQnaAnswer`·`deleteFile`·`file` 추가. `QnaFileStorageService.resolve()` 신규.
- **★AS-IS 삭제 규칙이 두 군데가 서로 다르다**(둘 다 그대로 옮겼다):
  목록 일괄삭제는 **답변 있으면 건너뛰고**, 답변화면의 [문의글 삭제]는 **답변이 있어도 지운다**(AS-IS가 여기선 검사하지 않음).
  답변 삭제는 소프트가 아니라 **행을 지운다**(매퍼에 소프트삭제 UPDATE가 주석으로 남아 있고 DELETE가 실행된다).
- **AS-IS 결함 1건 보정**: 답변을 지워도 `answer_count`를 갱신하지 않아 목록이 계속 '답변완료'로 보이고
  미답변 검색에서도 빠진다 → 실제 답변 수로 다시 계산한다.
- **내가 만든 오류 1건 수정**: 답변화면의 삭제 버튼을 `location.href`로 짰는데 AS-IS는 **ajax `$.get` +
  `Common.confirm` + `Common.responseHandler`**이고 서버도 JSON을 돌려준다(JsonView). AS-IS 방식으로 바로잡았다.
  첨부 다운로드 링크도 AS-IS대로 **`/file-download/{id}/{qnaDetailType}` 두 조각**으로 고쳤다(화면은 항상 `qna-open`을 보낸다).
- **검증(SQL 트랜잭션 후 롤백)**: 소프트삭제 시 목록 9→8 / 답변 하드삭제 후 `answer_count` 1→0 /
  미답변 검색 건수 재계산 일치(5 = 기존5 − 소프트삭제1 + 답변삭제1).

### 5112 3차 (2026-10-05) - 답변 저장 POST + 국민비서 문자 + 답변화면 parity 보정, admin·member bootJar EXIT=0

**추가한 엔드포인트** (AS-IS 경로 모양 그대로):
`POST /qna-admin/answer/{qnaId}` (답변 등록·수정) · `GET /qna-admin/edit/{qnaId}` · `POST /qna-admin/edit/{qnaId}`.
**발명이던 TO-BE 스켈레톤을 제거**: `GET /qna-admin/{qnaId}` + `templates/qna-admin/detail.html` + `POST /qna-admin/{qnaId}/answer` 삭제.

**★AS-IS 답변 POST의 부가동작 실측 - 실제로 나가는 알림은 하나뿐이다**:
- `sendMailFlag`/`sendSmsFlag`를 "값 없으면 N"으로 정하는데 **form.jsp에 그 체크박스가 아예 없어 항상 'N'**이다 →
  `QnaCompleteMail` 메일 분기와 `UnifiedMessagingService`(UMS) 분기는 **AS-IS에서 한 번도 실행되지 않는다**(SalesOn 잔재).
  같은 이유로 첨부 `detailImageFiles[]`도 **화면에 input이 없고**, 받더라도 insert/update 매퍼가 파일을 저장하지 않는다.
  → [[as-is-parity-includes-disabled-state]]대로 **옮기지 않고 기록만** 했다.
- **실제 알림 = `sendSmsQnaAnswer(qnaId)` 하나**(무조건 호출, 답변 **수정 때도 다시 나간다**).
  국민비서(IPS) 연계라 "발송"은 `TIF_IPS_SNDNG_M`에 한 행 적재하는 것이고 ESB가 집어간다.

**국민비서 발송 경로 신규 이식**(TO-BE에 발송부가 아예 없었다 - 7208 조회화면만 있었음):
- `admin/service/SmsIpsService.java` (AS-IS `SmsIpsServiceImpl.giveSendSms`/`insertTifIpsSndngM`/`initTifIpsSndngM`)
  - 수신동의 **`RECEIVE_SMS='0'`만** 발송 · 수신식별값은 **회원 CI**(`PRVC_IDNTFC_INFO=MBER_CI`) ·
    발송내용 `이름|전화번호`(QNA/가입/탈퇴/비번변경/관리자로그인 공통 2칸) ·
    공통값 `ESB_STATUS_CD='N'`, `ESB_WORK_GBN='I'`, `SVC_ID='812-A022'` · `LIST_SN`=`INSTT_CRT_SN`(같은 채번값)
  - 설정 `ghlove.integrations.sms-ips.{enabled,svc-grp-id,prvc-idntfc-se-cd,esb-if-id}` (AS-IS 전 프로파일 동일값 `812`/`C0090001`/`IF_P01_MIS_IPS_ALM08`), **개발환경 enabled=false**(AS-IS 소스에도 같은 이유의 로컬 가드가 있다)
- `database/ddl/migration-admin-tif-ips-sequence.sql` **적용** - AS-IS CUBRID serial `TIF_IPS_SNDNG_M_LIST_SN` 대체 시퀀스
- member 신규: `AdminMemberService.smsReceiver(userId)`(AS-IS `getQnaUserInfo`의 OP_USER·OP_USER_DETAIL INNER JOIN 대체) +
  `GET /api/admin/members/{userId}/sms-receiver`(없으면 204) + admin `MemberAdminClient.smsReceiver()`
- **AS-IS 결함 보정**: 비회원·탈퇴회원 문의는 `getQnaUserInfo`가 null → `Arrays.asList(null)` → **NPE로 500**
  (답변은 저장된 뒤 화면만 깨진다). TO-BE는 조용히 건너뛴다.

**답변화면(form.jsp) parity 보정 - 내가 1차에서 놓친 것들**:
- 상단 [문의글 삭제][목록]은 **답변이 없을 때만** 나온다(`answerCount == 0`) · 하단 버튼은 `btn_right`
- **답변 작성자 칸은 "역할명 (관리자 로그인ID)"**다(사람 이름이 아니다). AS-IS `getQnaByQnaId`가
  `OP_ROLE.ROLE_NAME`(authority LIKE 'ROLE_ADMIN%') + `OP_MANAGER.LOGIN_ID`를 뽑아 `roleNm`/`answerLoginId`로 내려준다
  → TO-BE는 `op_manager.authority`에서 찾는다(고정 경계 번역)
- 문의 작성자 칸은 `이름 (로그인ID)` + 회원상세 링크(`/admin/members/details/{userId}`), 없으면 이름, 그것도 없으면 `-`
- 작성일·답변일은 **`op:date`=날짜까지만**(목록은 `op:datetime`) ← 1차에서 작성일을 시각까지 찍어 둔 것 수정
- 첨부 칸에 **파일별 삭제 [x]** 버튼(`file_camera_{index}` div + `deleteItemImage`) 추가, 확인문구
  `'파일이 실제로 삭제됩니다.\n삭제하시겠습니까?'`는 `Common.confirm`이 아니라 **브라우저 confirm**
- 첨부 다운로드의 `qnaDetailType`은 **`'Q'`**다(1차에 `qna-open`으로 잘못 보냈다) -
  `'Q'`면 `OP_QNA_FILE`, 그 외면 `OP_QNA_ANSWER_FILE`을 본다(`getFrontQnaOpenFileDetail`)
- 답변 없을 때 textarea 기본값 `"안녕하세요. 고향사랑e음 상담센터입니다.\n\n감사합니다."` (AS-IS 문구 verbatim)
- `$('#qnaAnswer').validator()` · `Common.checkedMaxStringLength('textarea[name=answer]', null, 500)` ·
  검색조건 보존 hidden 폼(`#qnaParam`) + `qnaList()`
- **AS-IS 결함 보정**: `deleteItemImageByItemId`는 디스크 파일도 지운다(`fileStorage.delete`) →
  2차 주석에 "디스크 파일은 AS-IS도 남긴다"고 적은 건 **틀렸다**. `QnaFileStorageService.delete()` 추가해 바로잡음.
- **보안 편차**(기존 방침 적용): 문의 제목·본문은 AS-IS가 `op:nl2br`로 **이스케이프 없이** 렌더 → 저장형 XSS.
  `QnaOpenAdminService.escapeNl2br()`로 이스케이프 후 줄바꿈만 `<br/>`. 커뮤니티 본문(에디터 HTML)과 구분.
- `findFirstByQnaIdOrderByAnswerDateAsc` 추가 - AS-IS `ORDER BY ANSWER_DATE LIMIT 1` 그대로.
  `OP_QNA_ANSWER`에 QNA_ID 유일제약이 없어 2행 이상이면 `findByQnaId`(Optional)가 예외로 터진다.

**★AS-IS `view.jsp`·`edit` 진입점 없음 확인**: `/view/{qnaId}`·`/edit/{qnaId}`로 가는 링크가 JSP·JS 어디에도 없다.
게다가 `qna-open/view.jsp`는 **1:1문의(`/opmanager/qna`) 화면을 잘못 복사해 둔 것**이다 - 제목이 "1:1 문의",
모든 URL이 `/opmanager/qna/...`, 조건이 `${qnaAnswer.answer > '1'}`(본문 문자열을 '1'과 비교하는 무의미한 식).
→ **5112에 /view 화면은 만들지 않았다.** 1:1문의 보강 라운드에서 그 메뉴의 화면으로 다룰 것.
`/edit/{qnaId}` GET은 AS-IS처럼 답변화면과 같은 화면을 **첨부만 비워** 보여주고, POST는 AS-IS 결함(답변만 저장)대로 옮겼다.
답변자 `USER_ID`는 AS-IS가 hidden으로 받지만 값이 결국 로그인 매니저라 **세션에서 직접** 쓴다(결과 동일·위조 불가).

**검증**: admin·member `compileJava`·`bootJar` EXIT=0. IPS 적재 INSERT 모양을 트랜잭션에서 실행 후 롤백
(시퀀스·NOT NULL·`812-A022` 확인, 시퀀스 되돌림).

**엑셀 2건 = AS-IS 비활성 확정 → 5112 종결**:
AS-IS 목록 하단 버튼은 **[삭제] 하나뿐**이고 `downloadExcel()`은 **함수만 있고 호출하는 버튼이 없다**.
엑셀 업로드는 컨트롤러 POST가 **주석 처리**되어 있고 `upload-excel.jsp`는 제목이 일본어("Excel ダウンロード")인
복사본이다. → [[as-is-parity-includes-disabled-state]]대로 **구현하지 않고 비활성 유지**(목록 템플릿 주석에 기록).
**5112 Q&A는 이것으로 완료.**

### 5104 FAQ 완료 (2026-10-05) - 신규 화면 + 공개 FAQ 표·코드 교정, admin bootJar EXIT=0

**★ 두 가지가 어긋나 있었고 둘 다 바로잡았다**:
1. **메뉴 5104 'FAQ'가 `/community/faq-bbs`(커뮤니티 담당자FAQ 11405)를 가리켰다** - 고객센터 FAQ를
   누르면 다른 메뉴의 화면이 열렸다. → `/faq-admin`
2. **공개 FAQ가 엉뚱한 표를 읽고 있었다.** AS-IS 정본은 **`op_faq`**다 - 운영자 5104(`/opmanager/faq`),
   공개 페이지(`/faq/list.html`), 공개 API(`/api/faq`), 그리고 통합검색 뷰 **`view_search_faq`**
   (`op_faq`를 보고 `/faq/list.html`로 링크)까지 전부 같은 표다. 그런데 TO-BE 초기 시드는 FAQ 63건을
   **`op_community_locgovfaq`**(AS-IS에서 **중지된** 메뉴 11403 지자체FAQ의 표)에 넣고 질문유형 코드도
   AS-IS enum이 아닌 **자체 코드**(`JOIN`·`DONATE`…)로 바꿔 두었다. `op_faq`는 **0행**이었다.
   → 라벨 11건이 AS-IS enum과 **글자까지 같아** 코드만 1:1 되돌려 `op_faq`로 이관(63건, 원본은 안 지웠다).

**★질문유형은 공통코드가 아니라 enum이 정본이다**: AS-IS는 세 화면 모두
`enumMapper.get("FaqType")`를 쓴다 → `admin/service/FaqType.java`로 verbatim 이식.
코드 대응(이관 번역표): `JOIN→F_LOGIN · DONATE→F_CNTR_SYSTEM · POINT→F_CNTR_POINT ·
OFFLINE→F_OFF_CNTR · DESIGNATED→F_CNTR_DESIGNATED · TAX→F_API_PLATFORM · GIFT→F_PRESENT_PURC ·
ORDER→F_ORDER · PRIVATE→F_OPEN · SYSTEM→F_SYSTEM · ETC→F_ETC`.
`op_common_code`의 `FAQ_TYPE` 11건(①에서 넣은 것)은 **중지된 11403 화면이 계속 쓰므로 그대로 둔다**
(그 표의 행은 여전히 `JOIN`… 코드다). `FAQ_TYPE` 1~6은 SalesOn 쇼핑몰 잔재로 이미 `use_yn='N'`.

**신규/수정**
- admin 신규: `domain/Faq`(OP_FAQ) · `service/FaqType`(AS-IS enum verbatim) · `repository/FaqRepository` ·
  `service/FaqAdminService` · `service/FaqException` · `web/FaqAdminController`(`/faq-admin`) ·
  템플릿 `faq-admin/{list,form}.html`
- admin 수정: `service/FaqService`(공개 조회를 `op_faq`로) · `web/FaqController`·`web/FaqApiController`
  (질문유형을 enum에서, id를 Long으로) · `WebConfig`(게이트 `/faq-admin`)
- storefront 수정: `FaqListView.vue` 답변 본문 `{{ }}`→`v-html`
- DDL **적용**: `database/ddl/migration-admin-faq-5104.sql`(시퀀스 `admin.op_faq_seq` start 630000 +
  63건 이관 + 메뉴 URL 교정)

**AS-IS 동작 그대로**
- 목록 **진입(GET)은 빈 목록**(`new PageImpl<>(Collections.emptyList())`), 검색(POST)에서만 조회
- 정렬 **`id DESC` 고정**(화면에 정렬 수단 없음) · 검색은 **제목만**(`where=title` hidden 고정,
  검색구분 select는 주석처리 / 조건식의 `all`·`content` 분기는 옮기고 화면은 주석 유지)
- 운영자 목록은 **미사용 건도 보인다**(화면이 useYn을 안 보냄). 공개화면만 `use_yn='Y'`로 거른다
- 저장은 등록·수정 모두 **`use_yn='Y'`로 다시 써 넣는다** = 이 화면에 사용여부를 끄는 수단이 없다
- 삭제는 **행 삭제**(소프트 아님) · **수정 저장 후 목록이 아니라 수정화면으로 되돌아온다**
- 등록폼은 질문유형 select에 **빈 항목이 없고**(전체 옵션 주석처리) 제목에만 required가 붙는다
- 내용은 **스마트에디터 HTML** → 공개화면은 AS-IS처럼 그대로 렌더(`v-html`/`th:utext`)
- 내용 textarea의 title 속성이 M00661("배송/반품/환불/교환안내")인 복사 흔적까지 그대로 뒀다

**★부수 수정: `op.link.js`가 TO-BE에서 죽어 있었다**
AS-IS는 `layouts/common/inc_common.jsp`에서 `RequestContext = {currentUrl, prevPageUrl}`를 내려주는데
TO-BE에는 그 정의가 없어 `Link.view`/`Link.list`를 쓰는 화면에서 **ReferenceError**가 났다
(`policy/form.html`도 해당). `ManagerAuthAdvice`에 `@ModelAttribute("currentUrl")`를 넣고
`fragments/opmanager-head.html`에서 전역 객체를 내려주도록 고쳤다 - Thymeleaf 3.1에서 `#request`가
제거되어 템플릿에서 직접 못 읽는다. `prevPageUrl`은 AS-IS 프레임워크 추적값이라 비워 둔다
(`Link.list`가 인자 URL로 가는 폴백이 돈다).

**검증**: 63건 이관 후 `op_faq` 유형 분포가 원본과 동일(11/10/7/6/5/5/5/4/4/3/3),
`use_yn='Y'` 63건·`created` 결측 0건, 메뉴 5104=`/faq-admin`·11405=`/community/faq-bbs` 분리 확인.
admin `bootJar` EXIT=0.

### 매뉴얼 2화면 완료 (2026-10-05) - 5201/5202 분리 + g_mnl로 교정, admin bootJar EXIT=0

**★구조갭 해소**: AS-IS는 **한 컨트롤러**(`ManualManagerController`, `/opmanager/manual/**`)가 두 화면을 담당한다.
TO-BE는 **5201·5202·중지된 5109 셋 다 `/admin/manuals`**를 가리켜 구분되지 않았다. 이번에 나눴다:
- **5202 사용자매뉴얼 → `/admin/manuals`** (AS-IS `manual/user/*`)
- **5201 관리자매뉴얼 → `/admin/manuals/manager`** (AS-IS `manual/manager/*`)
- 5109 '매뉴얼관리'(구)는 AS-IS 중지 메뉴라 그대로 중지 유지

**★두 화면의 성격이 완전히 다르다(분리 규칙의 근거)**
- **사용자매뉴얼(5202)** = 표 `g_mnl`. 한 행이 "공개 화면 한 페이지에 대한 설명"이다.
  어느 화면인지는 `menu_se_code`가 공통코드 **`MENU_URL`**(33건)의 id를 가리켜 정하고,
  **코드의 label이 URL, detail이 화면명**이라 등록 시 그 두 값을 베껴 넣는다. 첨부 1건.
- **관리자매뉴얼(5201)** = **별도 표가 없다**. 운영자가 볼 수 있는 메뉴를 2단으로 묶어 카드로 늘어놓고
  3단 메뉴마다 **`op_menu.file_nm`/`orginl_file_nm`에 파일을 직접 붙인다**
  (그래서 "등록"이 `op_menu` UPDATE 한 번이다. `op_menu`에 그 두 컬럼이 왜 있는지가 이걸로 설명된다).

**★같은 유형의 중복 5번째 발견**: TO-BE가 사용자매뉴얼용으로 **`op_manual`을 새로 만들어** 두었는데
AS-IS 표 **`g_mnl`이 TO-BE 스키마에 이미 있었다**(AS-IS 덤프에도 있다). 기존 주석에는
"AS-IS 원본 DB에는 이 테이블이 없었다(신규 설계)"고 적혀 있었는데 **사실과 다르다**.
두 표 모두 0행이라 옮길 데이터는 없었다. `op_manual` 표는 남겨 두었다(표 정리는 사용자 판단).

**신규/수정**
- admin 신규: `domain/Mnl`(G_MNL) · `repository/{MnlRepository,MnlAdminRepository,ManagerManualRepository}` ·
  `service/{ManualAdminService,ManualFileStorageService,ManualException}` · `web/support/ManualSearchParam` ·
  템플릿 `manual-admin/{list,form,manager-list,manager-form}.html`
- admin 수정: `web/ManualAdminController` 전면 재작성(두 화면) · `domain/Menu`에 `fileNm`·`orginlFileNm` 추가
- **삭제**: `domain/Manual.java`, `repository/ManualRepository.java`(발명분)
- DDL **적용**: `database/ddl/migration-admin-manual-5201-5202-split.sql`(메뉴 URL 분리 + `admin.g_mnl_seq`)

**AS-IS 동작 그대로**
- 사용자매뉴얼 목록 **진입(GET)은 빈 목록**, 등록일 두 칸은 비어 있으면 **오늘로 채운다**(진입에서도)
- 목록은 `g_mnl`과 `MENU_URL` 코드를 **내부조인**하고 `use_yn='Y'`를 걸어서
  **안 쓰는 코드로 등록된 매뉴얼은 목록에서 사라진다**
- 검색구분 PAGE/SUBJECT/CONTENT 셋('전체'는 주석처리) · 날짜버튼 **오늘·1주일·한달·3달·1년 5개** ·
  출력수 **10/20/50/100**(다른 화면은 100이 없다) · 정렬 `frst_regist_pnttm DESC` 고정
- 같은 화면 코드로 **두 번 등록 불가** → "이미 등록 되어 있습니다."
- 등록폼의 스마트에디터는 **주석처리** 상태 → 평범한 textarea 유지
- 첨부 규칙: **50MB·확장자 16종**(커뮤니티 18종과 다름 - hwpx·7z 없음) ·
  저장명 `yyyyMMddHHmmssSSSS_원본명`(`DATENANO_FORMAT`, 4자리 S) · 폴더 **`help`**
- 삭제는 **행 삭제**, 첨부 삭제는 파일 컬럼만 비움(행 유지)
- 일괄삭제는 응답 본문 결과코드(`SUCC`/`FAIL`)로 성공을 알린다(화면이 그걸 보고 문구 선택)
- 관리자매뉴얼은 **ROLE_ADMIN_1~4(`role=='SYSTEM'`)만 메뉴명이 등록 팝업 링크**가 되고
  그 외 권한은 글자만(다운로드는 누구나). 등록은 500x300 팝업이고 저장 후 **부모창 새로고침+팝업 닫기**
- 관리자매뉴얼의 전체선택·[선택 다운로드]·행별 체크박스는 **전부 주석처리** 상태 → 켜지 않음
  (`downloadCheckManual` 함수만 그대로 둠). 카드 4열 배치는 `pinterest_grid.js`

**AS-IS 결함 3건 보정**
1. 등록·수정이 파일 루프 안에서 UPDATE를 **두 번** 실행한다("파일이 있으면" 뒤에 "파일이 없으면"
   주석을 달고 또 한 번) → 한 번만.
2. 첨부 삭제 매퍼(`deleteManualFile`)가 파라미터를 `mnlSn` 하나만 받는데 SQL에서
   `last_updusr_id = #{lastUpdusrId}`를 쓴다 - MyBatis가 단일 파라미터를 이름 무관하게 꽂아 주므로
   **최종수정자에 매뉴얼 번호가 들어간다** → 로그인 운영자 id.
3. 수정 시 `menu_url`/`menu_nm`을 다시 쓰지 않아 **화면 구분을 바꾸면 저장값이 예전 것으로 남는다**
   (목록은 코드를 조인해 보여주므로 화면에는 안 드러난다) → 코드 값으로 다시 채움.

**PostgreSQL 적응 1건**: 관리자매뉴얼 2단 SQL이 `group by menu_parent_id` 상태에서
`order by m.menu_seq, m.menu_id`를 쓴다(CUBRID 허용, PostgreSQL 오류) → `min()` 집계로.
정렬 1순위가 `menu_parent_id`라 결과 순서는 같다.

**검증**: 사용자매뉴얼 목록 SQL을 트랜잭션에서 행 하나 넣고 실행(페이지구분·작성자·URL/화면명 베끼기
확인) 후 롤백. 관리자매뉴얼 2단/3단 SQL 실측(ROLE_ADMIN_1 → 2단 8묶음, 5100 묶음 아래 3단 9건).
메뉴 URL 분리 확인(5201 `/admin/manuals/manager` · 5202 `/admin/manuals` · 5109 중지 유지).

### 5102 1:1문의 완료 (2026-10-05) - 다른 화면이 올라가 있던 것 교체, admin bootJar EXIT=0

**★메뉴 5102에 엉뚱한 화면이 올라가 있었다**: TO-BE `/admin/shop-inquiries`(36줄, 조회 전용)는
AS-IS **입점문의**(`InquiryManagerController`, `/opmanager/inquiry`)를 옮겨 놓은 것이었고,
표도 AS-IS의 `OP_STORE_INQUIRY`가 아닌 **`OP_SHOP_INQUIRY`를 새로 만들어**(0행) 쓰고 있었다.
1:1문의의 AS-IS 정본은 **`QnaManagerController`(539줄, 엔드포인트 18개)**이고 표는 **`OP_QNA`** -
Q&A(5112)와 **같은 표**다.
→ 화면을 새로 만들고 URL을 **`/admin/inquiries`**로 옮겼다. 발명분(`ShopInquiry` 엔티티·리포지토리·
컨트롤러·템플릿 2종)은 삭제. `op_shop_inquiry`·`op_store_inquiry` 표는 남겨 둠(둘 다 0행).
AS-IS 입점문의는 **op_menu에 행이 없어 메뉴로 접근 불가**한 SalesOn 쇼핑몰 기능이라 별도 판단 대상.

**★QNA_TYPE이 세 화면을 가른다**(AS-IS `Qna` 상수):
`'0'`=**1:1문의(5102)** · `'1'`=상품문의(5103, **중지**) · `'2'`=**공개 Q&A(5112)**.

**★공개 쪽에 그 구분이 아예 빠져 있었다 - 3건 수정(그중 1건은 개인정보 노출)**
1. `QnaService.ask()`가 `qna_type`을 **넣지 않았다** → 마이페이지에서 등록한 1:1문의가
   운영자 5102 화면에 **영원히 안 보일** 상태였다. `'0'`을 넣게 했다.
2. `myInquiries()`에 타입 필터가 없어 **내가 쓴 공개 Q&A 글까지 마이페이지 1:1문의 내역에** 나왔다 → `'0'` 필터.
3. **`publicBoard()`·`publicBoardDetail()`에 타입 필터가 없었다** → 마이페이지 **1:1문의의 제목·작성자가
   공개 Q&A 게시판 목록에 그대로 노출**되고, 비밀글이 아니면 id만 바꿔 **본문까지 열람** 가능했다 → `'2'` 필터.

**신규/수정**
- admin 신규: `repository/QnaIndividualAdminRepository`(AS-IS `getQnaListByParam` 이식) ·
  `service/QnaIndividualAdminService` · `web/support/QnaIndividualSearchParam` ·
  `web/QnaIndividualAdminController`(12 엔드포인트) · 템플릿 `inquiry-admin/{list,answer-form}.html`
- admin 수정: `service/QnaService`(위 3건) · `WebConfig`(게이트 `/admin/inquiries`)
- **삭제**: `domain/ShopInquiry`, `repository/ShopInquiryRepository`, `web/ShopInquiryAdminController`,
  `templates/shop-inquiry-admin/`
- DDL **적용**: `database/ddl/migration-admin-qna-5102-individual.sql`
  (메뉴 URL + 1:1문의 시드 5건·답변 2건 `qna_type='0'` + 두 시퀀스 재설정)

**AS-IS 동작 그대로 / 5112와 다른 점**
- 진입(GET) 빈 목록 · 정렬 `created_date DESC` 고정 · 검색구분 GROUP/LOGIN_ID/SUBJECT/QUESTION
  ('전체'·'이름'은 주석처리) · 상태 라디오 `qnaAnswerCode` ""/0/1
- 컬럼 **7개**(체크박스/No./문의유형/제목/작성자(아이디)/상태/등록일) - 5112는 11개
- **작성자명을 마스킹하지 않는다**(5112 목록은 마스킹한다)
- 목록 상태 문구가 **M00464 '답변대기'** - 검색 라디오의 '답변미완료'와 다르다
- 날짜버튼에 **'전체'(clear)가 있다**(5112에는 없다)
- 답변화면: **답변제목 입력칸이 화면에 있다**(기본값 "문의에 대한 답변입니다.", 5112는 hidden
  "답변입니다.") · 첨부 행은 **첨부가 있을 때만** · 답변자 칸 라벨이 "답변자" · `qnaDetailType` hidden 없음
- **국민비서 문자를 보내지 않는다**(5112는 답변 저장마다 무조건 보낸다). 메일·UMS 분기는
  5112와 같이 화면에 체크박스가 없어 닿지 않는 코드라 옮기지 않았다
- 삭제·첨부는 AS-IS가 두 화면에서 **같은 서비스 메서드**를 부르므로 `QnaOpenAdminService`를 그대로 쓴다
  (같은 규칙·같은 결함 보정 적용)
- `edit/{qnaId}` POST는 5112와 **똑같은 결함**(답변만 저장, 문의 본문 미저장)이고 전송하는 화면도 없다
- 옮기지 않은 조건: `answerCount` 1/2(같은 판정 중복), `itemId`/`sellerId`,
  바깥 쿼리의 `ITEM_NAME`/`ITEM_CODE`/`COMPANY_NAME`/`sido`/`sigungu` -
  전부 **상품문의(5103, 중지)**용이고 `OP_ITEM`/`OP_SELLER`는 TO-BE에서 gift 소유라 조인 불가
- `/view/{qnaId}`는 5112와 같이 **진입점이 없다**(list.jsp의 링크가 주석처리). `qna/view.jsp`가
  5112 쪽으로 잘못 복사된 그 원본이고 `${qnaAnswer.answer > '1'}` 같은 깨진 조건이 있다 → 만들지 않음
- 엑셀 다운로드·업로드도 5112와 같이 **비활성**(호출 버튼 없음 / POST 주석처리). 숨은 iframe만 유지

**남은 갭(이 라운드 밖)**: TO-BE에 **공개 Q&A 글쓰기 경로가 없다**(AS-IS `api/qna` line 541에
`QNA_TYPE_QNA`로 등록하는 엔드포인트가 있다). 공개 게시판이 조회 전용이라 `'2'` 글은 시드뿐이다.

**검증**: 메뉴 URL 5102=`/admin/inquiries`, 타입 분포 `'0'`=5 / `'2'`=9.
목록 SQL 실측(전체 5건 최신순 · 답변완료 2건 · 문의유형 '결제' 검색 1건),
화면별 분리 확인(공개 Q&A 9 / 마이페이지 1:1 회원별 2·2 / 5102 5 / 5112 9).

## 보류 (2026-10-05 사용자 지시 "나중에 처리할 사항으로 남겨두고 다른 메뉴 구현")
승인순서 4건(5112·5104·매뉴얼2·5102)은 완료. 아래는 **다음 기회에** 할 것:
1. **축소 구현 화면 3건 보강** - 5113 운영관리 SR(AS-IS 443 vs TO-BE 236줄) ·
   5114 답례품제공자 공지(290 vs 203) · 5107 자료실(294 vs 124).
   5113이 가장 크고, 커뮤니티 라운드 산출물(`CmntyFileStorageService`,
   `fragments/file-upload-agree.html`)을 재사용할 수 있어 그쪽부터 하는 것이 효율적이다.
2. **공개 Q&A 글쓰기 경로 신규** - AS-IS `api/qna`에 `QNA_TYPE_QNA`로 등록하는 엔드포인트가
   있는데 TO-BE에는 없다(공개 게시판이 조회 전용). [[op-qna-split-by-qna-type]]
3. 5104 FAQ 라운드에서 남긴 표 정리 판단(`op_community_locgovfaq`), 매뉴얼 라운드의 `op_manual`,
   1:1문의 라운드의 `op_shop_inquiry`·`op_store_inquiry` - 전부 0행 또는 중지화면 전용.

**★TO-BE 결함 발견(5112 밖, member 영역) - `RECEIVE_SMS` 인코딩이 두 갈래다**:
AS-IS 정본은 **`0`=수신, `1`=비수신**(`GeneralCustomer` 주석). 그런데 TO-BE는
`MemberService:652`가 **`Y`/`N`**으로 쓰고 `ExternalLoginService:214`는 **`0`**으로 쓴다.
읽는 쪽도 갈린다: `ProfileApiController:62`는 `"Y".equals`, admin `member-admin/details.html`·`info-access.html`은 `== '0'`.
→ 지금 일반가입 회원(`Y`)은 **운영자 상세화면에서 "비동의"로 표시**되고 Q&A 답변 문자 대상에서도 빠진다.
고치려면 member 쓰기/읽기 + 기존 행 데이터 변환(`Y`→`0`, `N`→`1`)이 필요하고, 같은 의심이
`receive_email`·`receive_pbanc`·`receive_kakao`에도 있다. **데이터 변환은 사용자 판단 대상**이라 보류·기록.

**★AS-IS 공통 패턴 재확인**: 공지(5110·5111)·사용자매뉴얼(5202) 모두 **GET 진입은 `count=0`+`EMPTY_LIST`,
POST 검색에서만 실제 조회**한다. 이 프로젝트에서 반복 확인된 패턴이라 이식 시 그대로 유지할 것.
(5201 관리자매뉴얼은 GET·POST 모두 매뉴얼 행을 싣지 않고 메뉴 트리만 싣는다 - JSP가 menuList로 그린다.)

## 재사용할 공용 자산 (커뮤니티 라운드 산출물)
`service/CmntyFileStorageService`(AS-IS 50MB·확장자 18종·`yyyyMMddHHmmssSSS_원본명`·크기표기) ·
`service/CmntyText`(decode·nl2br) · `service/CmntyException` ·
**`fragments/file-upload-agree.html`(자료 업로드 전 보안 점검 모달 - AS-IS는 전역 포함이니 첨부 있는 화면엔 반드시 붙일 것)** ·
`web/support/Pagination` · `fragments/pagination :: manager`.
