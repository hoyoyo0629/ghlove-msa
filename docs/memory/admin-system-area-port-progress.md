---
name: admin-system-area-port-progress
description: "시스템관리(1000>1400) 영역 25화면 AS-IS 이식 원장과 진척 - 팝업관리·공통코드 완료, AS-IS JSP 경로 매핑표, 발견된 구조 갭(연계로그 4화면→1화면)"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-03T06:10:26.352Z
---

[[asis-screen-port-procedure]] 절차로 **메뉴 영역별** 진행 중. 첫 영역 = 시스템관리(1000 > 1400), 리프 25개.

**AS-IS JSP 경로 원장** (`ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` 기준):

| id | 메뉴 | AS-IS JSP | TO-BE url | 상태 |
|---|---|---|---|---|
| 1311 | 팝업관리 설정 | `popup/{list,form}.jsp` | /popups | **완료** |
| 1401 | 공통코드 관리 | `code/{list,form}.jsp` | /codes | **완료** |
| 1404 | 사용자 권한 관리 | `user-group/role/list.jsp` | /admin/roles/matrix | **완료** |
| 1405 | 사용자 권한그룹 관리 | `user-group/{list,form}.jsp` | /admin/roles | **완료** |
| 1406 | 관리자 권한 승인관리 | `user/manager-request/list.jsp` + `user/popup/manager-request-{details,history}.jsp` | /admin/manager-requests | **완료** |
| 1105 | ISMS관리 | `isms/isms-config.jsp` | /isms-config | **완료** |
| 7203 | 메인 배너 관리 | **`banner/main/{list,form}.jsp`**(user-login-banner/form.jsp 아님) | /banners | **완료** |
| 1408 | 사용자로그관리 | `log/user/{login-log-list,login-log-details}.jsp` | /log/user-login | **완료**(상세는 미연결 - 아래 참고) |
| 6401 | 관리자로그관리 | `log/{login-log-list,login-log-details,action-log-list}.jsp` | /log/login | **완료** |
| 1312 | 이메일 설정 | `mail-config/{list,form,change-code}.jsp` | /mail-config | **완료**(AS-IS는 목록 미사용→리다이렉트) |
| 1308 | 약관관리 | `config/policy/{list,form,create}.jsp` | /policy | **완료**(create.jsp는 policy/edit/{type} 전용, 1308 아님) |
| 1403 | Batch Job | `batch-job/{list,form}.jsp` | /batch-job | **완료** |
| 1305 | 배송업체 관리 | `delivery-company/{list,form}.jsp` | /admin/delivery-companies | **완료** |
| 1409 | 메뉴관리 | `menu/{list,form,page}.jsp` | /admin/menus | **완료**(page.jsp는 목록의 호출부가 주석처리돼 진입점 없음 - 미이식) |
| 7207 | 설문관리 | `qustnr/{list,form,result}.jsp` | /admin/surveys | **완료**(데이터모델까지 AS-IS로 교체, 아래 ★ 참고) |
| 1411 | 엑셀다운로드사유 | `log/exceldownload-log-list.jsp` | /admin/excel-download-logs | **완료**(표를 AS-IS 개인정보접근로그로 교체) |
| 1413 | 서울세외 부과연계 로그 | `log/gif-seoul-buga-list.jsp` (11컬럼) | /log/levy/seoul-buga | **완료** |
| 1414 | 서울 수납연계 로그 | `log/gif-seoul-sunap-list.jsp` (9컬럼) | /log/levy/seoul-sunap | **완료** |
| 1415 | 지방세외 부과연계 로그 | `log/gif-stnd-buga-list.jsp` (34컬럼) | /log/levy/stnd-buga | **완료** |
| 1416 | 지방세외 수납연계 로그 | `log/gif-stnd-sunap-list.jsp` (37컬럼) | /log/levy/stnd-sunap | **완료** |
| 7208 | 문자전송이력 | `sms-log/list.jsp` | /admin/send-sms-logs | **완료**(원천=TIF_IPS_SNDNG_M) |
| 7210 | 포인트사용 정합성검증 | `point-check/list.jsp` | /reconciliation/order-point | **완료**(order·point에 조회API 신규, 아래 ★) |
| 1410 | 이메일 발송 | `mail/{list,form,detail}.jsp` | /email | **완료**(EMS 리포트는 미연동 - 아래 ★) |
| 1402 | 메세지 관리 | `message/{list,form}.jsp` | /message | **완료**(신규, menu_url 넣음) |
| 7209 | 배치 실행로그 조회 | `batch-log/list.jsp` | /batch-log | **완료**(기록 지점까지, 아래 ★) |

**진척 25 / 25 - 시스템관리 영역 완료(2026-10-03)**. 실제 메뉴 트리에서 `menu_type=3 AND path LIKE '시스템%'`로 뽑은 리프가 정확히 25개이고 전부 menu_url이 채워졌다(이 쿼리로 재확인 가능).

**★구조 갭 1413~1416 해소(2026-10-03) - 내 초기 판단이 틀렸던 건**: AS-IS는 **별개 화면 4개**(컬럼 11/9/34/37, 제목도 각각)인데 TO-BE는 `/log/levy` 한 화면("연계 로그 관리 (국세청 부과·수납)")으로 합쳐 놨었다. 원장에 "원천이 납부게이트웨이 연계라 그 라운드로 보류"로 적어뒀지만 **네 원천표가 이미 TO-BE donation 스키마에 전부 있었다**(전부 0행). 보류가 필요한 건 **쓰기(실제 연계)뿐**이고 조회 화면은 지금 분리 구현할 수 있었다. 사용자가 "시드데이터 생성해서 4개 따로 구현 가능?"이라고 물어 바로 진행했다.
- 원천: 1413 `donation.gif_seoul`(24col) / 1414 `donation.gif_etax_sunap`(15col) / 1415 `donation.g_next_buga_request`(32col) + `g_locgov` LEFT JOIN(`sgb_cd = administ_instt_code`) / 1416 `donation.g_next_sunap_response`(37col). **AS-IS 화면 컬럼과 1:1로 일치**한다.
- donation에 조회 전용 API 4개 신규(`/api/admin/levy-logs/{seoul-buga,seoul-sunap,stnd-buga,stnd-sunap}`, `LevyLinkLogRepository` 네이티브 쿼리 - 표에 PK가 없어 엔티티 없음). admin은 `LevyLogClient` + `LevyLinkLogController`(4라우트, GET+POST) + 템플릿 4개.
- `migration-admin-menu-1413-1416-levy-split.sql`로 menu_url 4개 분리. 기존 `/log/levy`(TO-BE가 만든 DONATION_LEVY 통합조회 + 백필)는 AS-IS에 없는 운영도구라 메뉴에서만 빠지고 경로는 남겼다.
- **검증용 시드데이터** `seed-donation-levy-link-logs.sql`(각 표 3행, 성공/실패 섞음, 재실행 안전 - 'SEED%' 키만 삭제). 날짜는 **오늘 기준**으로 만든다(네 화면 모두 날짜 기본값이 오늘이라 고정 날짜면 아무것도 안 보인다). 개발DB에 `administ_instt_code`가 있는 지자체는 '1234567'(서울특별시 종로구) 한 곳뿐이라 1415 첫 행만 지자체명이 차고 나머지는 빈칸이다 - LEFT JOIN 실패 모습까지 확인 가능. 컬럼 길이 제약 주의: `gif_seoul.error_cd(3)`, `gif_etax_sunap.rst_cd(3)`·`sunap_dt`는 **NOT NULL**(미수납도 빈 문자열), `g_next_sunap_response.lvy_no(6)`·`rcvmt_no(2)`.
- AS-IS verbatim 보존: 1414 **초기화 버튼이 서울'부과' 화면으로 이동**(복사 흔적), 1416 검색폼·목록의 summary/caption이 "지방세외 **부과** 로그"(수납 화면인데), 데이터없음 colspan이 1415는 14·1416은 12(컬럼 34/37), 1415·1416은 결과가 있을 때만 페이지네이션(pagination-wrap 중첩), 결과여부 select title이 "아이디". 날짜 필수 검증 문구도 화면별로 다르다("등록일자/부과일자/수납일자는 필수입니다").

**공통코드 데이터 대조 결과(2026-10-02):** 앱이 읽는 테이블은 `admin.admin_common_code`(컬럼 `code_language`)이고, AS-IS 테이블명 그대로인 `admin.op_common_code`(컬럼 `language`)는 내가 AS-IS export를 적재해 둔 **대조용 사본**이다. AS-IS 71개 code_type/1091행이 이미 전부 이식돼 있었고 행 차이는 `ORDER_STATUS` 98/99(주문취소완료) 2건뿐이라 보충했다(`migration-admin-common-code-asis-gap.sql`). TO-BE 전용 7개 code_type(BSNS_PURPS_CODE/DELIVERY_CARRIER/MANAGER_STATUS/NOTICE_CATEGORY/POLICY_TYPE/POPUP_TYPE/SETTLEMENT_STATUS)은 의도적 추가라 유지. `POP_ADMIN`의 period/period2는 운영 중 바뀌는 팝업기간 값이라 동기화 제외.

**AS-IS 자체 결함 - 고치지 않고 보존했으니 운영 확인 필요(발견 2026-10-02):**
0. **[수정함 - AS-IS와 의도적으로 다름]** 약관관리 목록 제목이 AS-IS에서는 "이용후기(리뷰)관리"로 뜬다. `config/policy/list.jsp`가 `${op:message('M00756')}`(=이용후기(리뷰)관리), summary/caption에 `M00459`(=이용후기(리뷰))를 쓰기 때문(리뷰 화면 복사 흔적). **사용자가 "고치는 게 맞다"고 확인해서** 같은 기능의 상세화면 `form.jsp` 방식 - 제목은 리터럴 "약관관리", caption은 `M00206`(=약관관리) - 에 맞췄다. AS-IS verbatim 원칙의 예외이므로 템플릿에 사유를 주석으로 남겼다.
0-2. **`policy/form.jsp`의 `Link.list()`가 동작하지 않는다** - `op.link.js`가 AS-IS 어디에서도 로드되지 않는다(inc_head.jsp의 JS 목록에 없음). `Link.list`의 폴백이 `location.href = url`이라 그 동작으로 대체했다.
0-3. `Policy.getPolicyTypeLabel()`에 `'6'` 분기가 없어 "개인정보 수집·이용 동의"는 목록 정책구분이 빈칸이다(검색/등록 라디오에는 있음). `'1'`도 라디오는 "개인정보처리방침", 라벨 메서드는 "개인정보취급방침"으로 다르다. 둘 다 AS-IS 그대로 보존.

**AS-IS 자체 결함 2건(권한그룹):**
1. 권한그룹 **생성**: `user-group/form.jsp`의 authority가 `<input type="hidden" id="authority" />`로 **name이 없어** 서버로 전송되지 않는다 → AS-IS `insertRole`이 AUTHORITY=null로 INSERT를 시도하는 상태. 권한코드를 내가 지어내면 AS-IS에 없는 동작이 되므로, 값이 없으면 저장하지 않고 사유를 알리도록 했다.
2. 권한그룹 **삭제** 버튼은 JSP에서 `<%-- --%>` 주석처리돼 노출되지 않는다 → TO-BE도 숨김 유지(엔드포인트만 보존). [[as-is-parity-includes-disabled-state]]
또한 1404/1405가 둘 다 `/admin/roles`를 가리키고 있어 1404를 `/admin/roles/matrix`로 바로잡았다(`migration-admin-menu-1404-roles-matrix.sql`).

**★op_menu 트리 구조를 AS-IS와 동일하게 교정(2026-10-02, `migration-admin-menu-tree-asis-shape.sql`)** - 메뉴관리 화면이 이 구조에 의존해서다:
- AS-IS는 `menu_id=0`('root') 행이 있고 최상위 메뉴의 `menu_parent_id=0`이다. TO-BE는 root 행 없이 최상위가 NULL이었다(전수 대조의 "트리 불일치 21건"이 이것 - 표현차가 아니라 실제 차이였다). AS-IS 계층 쿼리가 `START WITH MENU_ID='0'`이라 root 없으면 트리가 전개되지 않는다.
- `menu_type`(1=상단 2=LNB섹션 3=링크)이 183행 전부 NULL이었다 → 깊이대로 1/21건, 2/41건, 3/121건 채움. 이게 없으면 ID 채번(레벨별 MAX+1000/+100/+1)과 목록 들여쓰기가 깨진다.
- 코드: 최상위 판정을 `Menu.isTopLevel()`(menuId≠0 AND parent가 null이거나 0)로 통일해 4곳 교체(MenuService.navTreeFor, MenuAdminService×2, RoleAdminService.matrixFor). `MenuService.topMenuIdOf`도 root까지 올라가 0을 반환하지 않도록 top에서 멈추게 수정.
- `Menu.menuId`는 `@GeneratedValue` 시퀀스를 **제거**하고 직접 배정으로 바꿨다(AS-IS 채번 규칙을 써야 트리 위치가 ID에 드러난다).
- PostgreSQL엔 CONNECT BY가 없어 `MenuTreeRepository`의 재귀 CTE로 대체(경로·자식수·ORDER SIBLINGS 포함). 183행 정상 확인.

**★`MENU_<id>` 문구코드는 신뢰하지 말고 확인 후 쓸 것** - `MENU_1409`가 문구표에 "엑셀 다운로드 사유관리"로 잘못 등록돼 있다(1409는 메뉴관리). AS-IS menu/list.jsp도 거기에 `MENU_1415`(=지방부과로그관리)를 써서 틀렸다. 검증된 것: MENU_1401/1404/1405는 정확. 쓰기 전에 `select message from admin.op_common_message where id='MENU_<id>'`로 확인하고, 틀리면 AS-IS h3의 한글 리터럴을 쓴다.

**★설문관리(7207)는 데이터모델 자체를 AS-IS로 교체했다(2026-10-02)** - 화면만 고칠 수 없는 케이스였다:
- AS-IS 4개 표: `G_QESTNAR`(설문: 제목/목적/기간/대상 srvy_trgt U=대민 M=관리자 S=답례품제공자) + `G_QUSTNR_QESITM`(문항: qestn_ty_code **rtype=객관식 stype=주관식**, **parent_sn=연계질문**, answer_choise_co) + `G_QUSTNR_IEM`(선택지) + `G_QUSTNR_RSPNS_RESULT`(응답).
- TO-BE는 문항을 자유서술형으로 **단순화한 `op_qustnr`/`op_qustnr_qesitm`/`op_qustnr_rspns`를 따로 만들어** 썼다 → 선택지·주관식 구분·연계질문·설문대상이 전부 없어 AS-IS 화면을 만들 수 없었다. **AS-IS 4개 표는 이미 TO-BE DB에 존재**했고(내가 처음 `%qustnr%`로만 찾아 `g_qestnar`를 놓쳤다) 6개 표 전부 0행이어서 그대로 갈아탔다.
- `QestnarRepository`(네이티브 SQL, 메서드명을 AS-IS 쿼리 id와 동일하게) + `QestnarAdminService`(설문 upsert → 미제출 문항 제거 → 문항 upsert → 미제출 선택지 제거 → 선택지 upsert) + 컨트롤러(JSON 본문 POST, `{isSuccess}` 응답). 채번도 AS-IS대로(설문 MAX+1, QESTN_SN 설문내 MAX+1, IEM 문항내 MAX+1).
- **storefront 참여 API(`SurveyApiController`)도 같은 표로 옮겼다** - 안 옮기면 관리자가 등록한 설문이 이용자 화면에 안 보이는 split-brain이 된다. 문항에 선택지가 생겼으므로 DTO에 `choices`가 추가되고 응답은 `qustnrIemSn`+`respondAnswerCn`을 받는다. **storefront Vue SPA가 이 DTO를 쓰므로 그쪽 수정이 남아 있다(미확인).**
- 쓰이지 않게 된 `op_qustnr*` 전용 엔티티/리포지토리 6개는 삭제했다(이름이 `Qestnar`와 혼동됨). **표 자체는 DROP하지 않았다**(0행, 파괴적 작업이라 사용자 판단 필요).
- AS-IS 오타 보존: `form.jsp`의 취소 버튼이 `class="btn btn-defualt btn-small"`(default 오타)라 AS-IS에서도 스타일이 안 먹는다 - verbatim 유지.

**★이메일 설정(1312) - AS-IS는 목록 화면을 쓰지 않는다(2026-10-02)**: `/mail-config/list`가 첫 템플릿의 등록/수정 화면으로 **리다이렉트**한다("cjh - 2014.05.27 리스트 페이지 삭제" 주석과 함께 본문 전체가 주석처리). 템플릿 전환은 폼 상단 `ul.mail_list`로 한다. 예전 TO-BE는 목록을 두고 "AS-IS가 우연히 지운 것"이라 적어뒀는데 근거 없는 판단이라 AS-IS대로 되돌리고 `mail-config/list.html`을 삭제했다.
- **템플릿 목록도 발명이었다**: TO-BE가 공통코드 `ORDER_STATUS`로 목록을 만들어 써서 회원가입·임시비밀번호·문의답변·휴면안내·관리자 권한 승인/거절 템플릿이 아예 없었다. AS-IS는 `MailTemplate.getTemplateCodes()`의 **고정 10종**(order_deposit_wait/order_cready_payment/order_delivering/expiration_point/member_join/pwsearch/qna_complete/member_sleep/manager_request_approval/manager_request_reject) → `MailTemplateCodes`로 순서까지 옮겼다.
- **표 컬럼 13개 누락**: AS-IS `OP_MAIL_CONFIG`는 23컬럼인데 TO-BE는 10컬럼이어서 화면의 "발송여부"(buyer_send_flag/admin_send_flag)조차 저장 불가였다 → `migration-admin-mail-config-asis-columns.sql`로 보충(23컬럼 확인).
- 메일 대체코드 팝업은 AS-IS 9개 `*Mail.getMap()`을 `MailTemplateCodes.changeCodes()`로 옮겼고, `{under_score}` 변환규칙(AS-IS `StringUtils.convertToUnderScore`)까지 재현했다.
- AS-IS 결함 보존: 삭제 엔드포인트가 `@PostMapping`인데 (지금은 죽은) 목록 JS가 `location.href` GET으로 불러 405가 난다 - TO-BE는 양쪽 메서드를 받아 둔다.

**배송업체 관리(1305)**: 표는 order 서비스 `ord.op_delivery_company`(컬럼은 AS-IS와 동일)이고 admin은 `DeliveryReturnClient`로 cross-service CRUD. order API가 전체목록 조회 + 단건 삭제만 주므로 검색·화면출력(10~1000)·페이징은 admin 메모리에서, 선택삭제는 id별 반복 호출로 구현(order 쪽 API는 수정하지 않았다). AS-IS 폼의 전송방법(sendFlag 라디오)·송장번호 파라미터 행은 주석처리 상태라 hidden으로만 넘긴다.

**★메인 배너 관리(7203) - 원장이 틀렸던 건(2026-10-02)**: 7203의 실제 뷰는 `user-login-banner/form.jsp`가 아니라 **`banner/main/{list,form}.jsp`**다(`MainBannerManagerController./index`가 `ViewUtils.getView("/banner/main/list")`를 리턴). `user-login-banner/form.jsp`는 별도 컨트롤러(`UserLoginBannerManagerController` loginWeb/loginMobile)의 PC/모바일 로그인 배너 화면이고 메뉴 7203과 무관하다.
- **TO-BE가 AS-IS의 이미지 업로드를 발명한 텍스트 입력으로 대체**해 뒀다: `op_main_banner`의 `pc_file_name`/`m_file_name`/`pc_org_file_name`/`m_org_file_name` 4컬럼을 엔티티가 매핑하지 않고 `imageUrl` 입력칸을 쓰고 있었다 → 4컬럼 매핑 + `MainBannerImageStorageService`(저장/스트리밍, 20MB·jpg/jpeg/gif/png) 추가, AS-IS대로 `/banners/mainBanner/pc|m/{bannerId}`로 이미지를 내려준다.
- 라우트도 AS-IS 모양으로: 목록 `/banners`, 등록 `/banners/create`, 수정 `/banners/edit/{id}`, 저장은 FormData ajax(`/banners/create`·`/banners/edit`), 순서 저장 `/banners/change-display-order`("bannerId|순서" 배열). AS-IS에 없던 `POST /banners/{id}`(imageUrl 기반)는 제거.
- 목록은 **등록분 뒤를 10행까지 빈 행으로 채우고 그 자리에 등록 버튼**을 두는 AS-IS 구조이며, AS-IS가 등록 버튼을 3번째 td(사용여부 칸)에 둔 것까지 그대로다.

**Batch Job(1403)**: 표 컬럼은 AS-IS와 동일. 검색(메서드명/작업명·트리거종류·배치상태)·11컬럼·선택삭제·1250x310 등록/수정 팝업(ajax) 복원. 목록의 트리거종류/배치상태/적용여부는 AS-IS 쿼리의 CASE 라벨(심플·크론 / 실행중·정지 / 적용전·적용완료)을 엔티티 `@Transient` getter로 옮겼다. AS-IS에 없던 배치상태 토글 버튼은 제거. **"농협배치테스트" 버튼이 호출하는 `ngDonationBatchService.getNoBugaLocgovList()`(농협 미부과 지자체 조회)는 TO-BE에 미이식** - 성공으로 꾸미지 않고 사유를 알리는 응답으로 두었다(버튼은 AS-IS대로 노출). AS-IS 폼 JS가 `input[name=triggerType]`으로 select를 읽어 그 검증이 항상 통과하는 것도 verbatim 유지.

**ISMS관리(1105)**: AS-IS는 구분(공통/관리자/회원)별 rowspan 묶음 표 하나이고 **모든 입력칸을 JSON 배열로 모아 한 번에** 저장한다. 예전 TO-BE는 행 단위 폼 저장 + 화면에 없는 사용여부 체크박스를 받았는데 AS-IS 방식으로 되돌렸다(rowspan 계산은 컨트롤러가 각 구분 첫 행에만 typeName/typeCount를 채워 내려준다).

**★로그 계열 3화면(2026-10-02)**
- **6401 관리자로그관리**: 표(`op_login_log`)는 AS-IS 컬럼과 동일. 검색(로그인ID/접속IP·접속일 범위·성공여부·권한그룹)·화면출력·7컬럼·페이징 복원 + **상세화면**(로그인ID/권한그룹/접속IP) + 상세 안의 **메뉴사용이력 조각**(ajax, `op_manager_action_log`). AS-IS 핵심 규칙: 상세는 그 로그인 시각 ~ **같은 계정의 다음 로그인 시각**까지를 본다(`LoginLogRepository.findNextLoginDate`). 권한그룹명은 로그인ID→매니저→authority→`op_role.role_name`으로 얻는다(AS-IS는 OP_USER_ROLE 조인). 예전 TO-BE는 최근 200건만 뿌리고 액션이력을 AS-IS에 없는 독립 화면으로 분리해 뒀다(그 화면들은 대응 메뉴가 없어 그대로 남겨둠).
- **1408 사용자로그관리**: 같은 구조, 권한그룹 컬럼 없는 6컬럼. 사용자 로그인 로그는 member 서비스 소관이라 `MemberClient.loginLogs()`로 전체를 받아 admin에서 걸러 페이징한다(member API에 검색 파라미터가 없어서 - member는 수정하지 않았다). **상세(회원 메뉴사용이력)는 미연결** - 로그인 세션 단위 조회 API가 member에 없어 "관리" 칸을 '-'로 두고 템플릿에 사유를 주석으로 남겼다.
- **1411 엑셀다운로드사유 관리**: AS-IS는 **개인정보 접근로그**(`OP_PRIVACY_ACCESS_LOG` + `_HIST`)를 읽는다(AS-IS 컨트롤러가 `PrivacyLogParam`을 쓴다). 두 표가 TO-BE DB에 AS-IS 컬럼 그대로 있어 엔티티·리포지토리를 새로 만들고 화면을 AS-IS로 되돌렸다 - 8컬럼, 사유 50자 절단 + 전문 팝업, 변경이력 2건 이상이면 이력보기 팝업, 등록일 기본 오늘, 기본 20건, **ROLE_ADMIN_1~4가 아니면 본인 것만**.
  - **쓰기 쪽 2026-10-03 해결** - 사용자가 "쓰기 쪽을 op_privacy_access_log로 옮기기"를 택해 AS-IS 2단계 흐름(사유 모달 → 접근로그 → 다운로드)으로 이전했다. 아래 ★엑셀다운로드 기록 지점 항목 참고.

**★이메일 발송(1410) - 2026-10-03**: 예전 TO-BE는 "관리자에게 즉시 메일 한 통"으로 축소돼 있었고 **발송대상 코드를 지어냈다**(A:전체관리자/D:ROLE_ADMIN/O:ROLE_OPERATOR) → AS-IS 코드 **A:권한별 S:답례품 E:개별 L:로그인인증**으로 되돌렸다. 발송시점(즉시/지정+시·분)·첨부파일(12종 10MB)·스마트에디터(기본 메일틀 `EmailDefaultContent.HTML`에 verbatim)·개별수신자 검색·목록 검색/페이징·상세(발송결과·발송인원)·첨부 다운로드 전부 신규. 표 3개(`op_email`/`op_email_detail`/`op_email_file`)는 AS-IS 컬럼과 동일해 DDL 불요.
- 발송은 AS-IS대로 2단계: `POST /email/form`(등록, STATUS='R') → 응답 emailId로 숨은 iframe에 `POST /email/send`. 수신자는 30,000명 단위로 끊어 EMS에 요청(`EmsMailClient`, AS-IS sendEmailData의 JSON 본문·필드명 그대로, `ghlove.integrations.ems.*`, enabled=false 모크).
- **EMS 리포트(EMS_REPORT/EMS_REPORT_DETAIL)는 TO-BE에 없다** - AS-IS는 별도 datasource(CUBRID `ems`, emsDS)로 붙어 발송결과 집계와 발송인원 명단을 읽고 그걸로 상태를 C(전송완료)/P(전송실패)/T(일부성공)로 갱신한다. 그 DB가 없어 집계는 비어 있고 상태는 R/S/F에 머문다 → 상세화면의 발송결과·발송인원 영역이 뜨지 않는다(AS-IS에서 리포트 없을 때와 동일). 갱신 규칙은 `OpEmailService.updateEmailStatus`에 AS-IS 그대로 보존.
- 대상 조회 경로: 권한별=`op_manager.authority`(AS-IS는 OP_USER_ROLE 조인, TO-BE는 매니저가 authority 직접 보유. 정상 상태코드는 AS-IS '9'↔TO-BE 'ACTIVE'), 개별=`MemberAdminClient.search(srchKey=USER_NAME)` 후 **이름 완전일치**만(AS-IS는 암호화 이름 완전일치), 답례품=**gift에 조회 전용 API 신규** `GET /api/admin/sellers/email-send-targets`(AS-IS sendSellerUserList 조건 `STATUS_CODE=2 AND ITEM_APPROVAL_TYPE=1`). `ITEM_APPROVAL_TYPE`은 일부러 gift `Seller` 엔티티에 매핑하지 않았다 - 컬럼 DEFAULT '1'을 admin 등록폼이 NULL로 덮으므로 네이티브 쿼리로만 읽는다.
- **AS-IS 결함 3건(이 화면에 몰려 있다) - 셋 다 사용자 확인 후 고쳤다**: ① **검색어가 동작하지 않았다** - 쿼리는 `searchType`이 'S'/'C'일 때만 제목 LIKE를 거는데 화면 select는 SUBJECT/CONTENT를 보낸다(게다가 'C'=내용 분기마저 SUBJECT를 LIKE한다). → 화면 값대로 제목은 SUBJECT, 내용은 CONTENT를 보도록 **고쳤다**(2026-10-03). ② 검색 버튼이 `search()`를 부르는데 그 함수가 어디에도 없어 AS-IS에선 눌러도 에러만 난다(= 동작하는 등록일 범위 검색조차 못 쓴다) → 폼 submit으로 **살렸다**. ③ 화면출력 select에 change 핸들러가 없어 AS-IS에선 무효 → 다른 운영관리 목록화면 방식대로 **살렸다**. 또 list.jsp의 `<h3><span></span></h3>`가 비어 제목이 안 보여 form/detail과 같은 "이메일 발송"으로 채웠다. No. 칸에 행번호가 아니라 EMAIL_ID를 찍는 것, 취소 버튼의 `btn-defualt` 오타는 verbatim 유지.

**★메세지 관리(1402) - 신규 구현(2026-10-03)**: AS-IS `/opmanager/message`는 **OP_COMMON_MESSAGE 자체를 관리하는 화면**이다(ID 하나당 한국어/일본어 한 행으로 피벗). TO-BE엔 화면도 menu_url도 없었다 → `migration-admin-menu-1402-message.sql`로 `/message` 설정. 검색구분(아이디/메세지)·출력수·5컬럼(체크박스/순번/아이디/한국어/일본어)·선택삭제·등록·수정. **등록은 ID 입력 여부로 갈린다** - 비우면 `M`+5자리 자동채번(AS-IS `CONCAT('M', SUBSTR(CONCAT('00000', SUBSTR(MAX(ID),2,5)+1), -5))`, MENU_ 제외. 현재 다음값 M01705), 적으면 그 ID로 ko/ja 2행 INSERT(MENU_xxxx 직접 등록용). 저장·삭제 후 `CommonMessageService.reload()`로 문구 캐시를 비운다(AS-IS도 매번 reload). **AS-IS 결함 보존**: 수정은 언어별 UPDATE라 ja 행이 없는 ID(2,034개 중 1,936개)는 일본어 입력이 조용히 버려진다 - INSERT로 보충하면 AS-IS에 없던 ja 행이 생기므로 그대로 뒀다. 피벗 쿼리는 네이티브(self left join), `[[asis-message-catalog-loaded]]`의 사전과 같은 표다.

**★배치 실행로그 조회(7209) - 신규 구현(2026-10-03)**: AS-IS `/opmanager/batch-log/list`. `migration-admin-menu-7209-batch-log.sql`로 `/batch-log` 설정. 작업명 검색 + 실행날짜 범위(둘 다 비면 **오늘**) + 출력수(10/50/100/200/500) + 7컬럼. 원천은 `admin.op_batch_execution`(배치구분/실행날짜/시작·종료시간/결과/메시지, **PK 없음** → 엔티티 없이 네이티브 쿼리 전용 `BatchLogRepository`)이고 작업명은 `op_batch_job`과 **LIKE 조인**(`BE.BATCH_TYPE LIKE '%'||BJ.JOB_METHOD||'%'`, 외래키 아님 - AS-IS 그대로). AS-IS verbatim: h3가 메뉴명이 아니라 "배치 로그", 작업검색의 "메서드명" option·트리거종류 칸·선택삭제/신규등록 버튼은 JSP에서 주석처리돼 화면에 없음, 날짜칸 title이 "주문일자 시작일/종료일"(복사 흔적, 안 보임).
- **★기록 지점까지 구현(2026-10-03)**: admin에 내부 전용 `POST /api/admin/batch-executions`(`BatchExecutionInternalApiController`, 공유시크릿 X-Internal-Secret, 같은 키면 UPDATE·없으면 INSERT = AS-IS MERGE 대응)를 두고, @Scheduled 배치가 있는 **donation·point·order 세 서비스에 `BatchExecutionReporter`**를 넣어 보고한다(서비스별 독립 Gradle 프로젝트라 공유모듈이 없어 같은 클래스 3개). 보고 실패는 배치를 죽이지 않고 로그만 남긴다. point·order에는 `ghlove.admin-service.base-url`을 새로 추가했다.
- **트랜잭션 함정**: `@Scheduled`가 `@Transactional` 메서드에 같이 달려 있으면 같은 빈 안에서 감싸 호출할 수 없다(self-invocation → 프록시 미경유 → 트랜잭션 무효). 그래서 트리거를 **별도 빈**으로 분리했다(`DesignatedProjectBatchScheduler`, `PointExpirationBatchScheduler`; order는 이미 `CouponBatchScheduler`로 분리돼 있었다). 원래 서비스 메서드는 수동 실행 API용으로 공개 유지.
- **`op_batch_job` 등록이 없으면 7209에 안 보인다** - 화면이 `BATCH_TYPE LIKE '%'||JOB_METHOD||'%'`로 작업명을 조인하기 때문이다. 실제로 도는 배치는 3개인데 1건만 등록돼 있어 나머지를 등록했다(`seed-admin-batch-job-registrations.sql` - 1001 `PointService.runExpirationBatch`, 1002 `CouponBatchScheduler.runDailyCouponBatch`). **보고하는 batchType 문자열은 job_method와 같게 유지해야 한다.** 쿠폰 배치는 `ghlove.coupon.enabled=false`라 빈이 등록되지 않아 현재 돌지 않는다(등록만 해 둠).
- 실측: 세 batch_type으로 행을 넣고 화면 쿼리를 돌려 작업명·정상/오류 라벨이 전부 정상 조인됨을 확인(검증 행은 삭제).

**★포인트사용 정합성검증(7210) - 2026-10-03, 사용자 승인 후 order·point에 조회전용 API 추가**: AS-IS는 주문번호 하나에 세 곳의 금액을 모아 **결제금액 − 입금대기금액 − 취소금액 == 사용완료 포인트**면 금액일치로 판정하고, 목록 맨 위에 합계 행(정합여부 칸에 차액)을 둔다. 소유 서비스가 달라 한 쿼리 조인이 불가해 **order `GET /api/admin/reconciliation/order-amounts`** + **point `GET /api/admin/reconciliation/cntr-used-points`**를 새로 만들고 admin(`PointCheckService`)이 주문번호로 합친다.
- **TO-BE 주문 모델이 달라 금액 대응을 바꿨다**: `ord.op_order_item`은 **7개 컬럼만 있는 미완성 잔재(0행)**로 `sale_price`/`etc_amt`/`created_date`/`locgov_code`가 없어 AS-IS 쿼리를 돌릴 수 없다(반면 `op_order_item_hold`는 118컬럼 완전체, 0행). 실제 주문은 `ord.od_order`다 → 결제금액=`point_amount`, 취소금액=상태 `CANCELLED`, 입금대기금액=0(TO-BE에 해당 상태 없음), 정산금액(`op_remittance_detail`)·미정주문건수(`op_order_item_hold`)는 쌓이는 게 없어 0. 주문상태는 TO-BE 이름(CONFIRMED 등)을 AS-IS 라벨로 매핑.
- **사용완료 포인트는 `point.g_cntr_use_point`(use_se_code='1')** - AS-IS와 같은 표다. 멀티아이템 주문은 ORDER_CODE가 `주문번호#항목번호`라 `split_part(order_code,'#',1)`로 묶는다(AS-IS는 주문번호 그대로였다).
- 예전 TO-BE는 AS-IS에 없는 대사(`od_order.point_amount` ↔ **포인트 원장** `PT_POINT_LEDGER`)를 검색·컬럼 없이 보여줬다. `ReconciliationService`는 그 점검이라 화면 연결만 끊고 남겨 뒀다(SAGA 드리프트 탐지용).
- 실측: 개발DB 8건 중 3건이 금액불일치로 잡힌다(PARTIALLY_CONFIRMED 주문 2건 + 포인트사용 기록 없는 1건) - 화면이 실제로 작동한다는 뜻.

**★엑셀다운로드 기록 지점을 AS-IS와 같게 옮김(2026-10-03, 사용자 결정)**: AS-IS 흐름은 **2단계**다 - ① 다운로드 버튼 → 사유 모달(`include/modal/privacy-access.jsp`)이 `POST /common/opmanager/privacy-access-log`로 사유를 보내 `OP_PRIVACY_ACCESS_LOG` + `_HIST` 1건을 남기고 ② 성공하면 그때 다운로드 URL로 이동. 즉 **기록은 다운로드를 수행하는 서비스가 아니라 admin이** 한다. order의 CSV export가 `ord.OD_EXCEL_DOWNLOAD_LOG`에 쓰던 것을 제거하고(사유 파라미터도 더 이상 안 보냄) admin으로 옮겼다.
- 신규: `PrivacyTask`·`PrivacyAccess`(AS-IS enum **verbatim** 100행 + 이식된 화면에만 `tobePattern` 추가), `PrivacyAccessLogService`, `PrivacyAccessLogApiController`(`privacy-access-log`, `privacy-access-log-update`), `fragments/privacy-access.html`(모달). 사유 5글자 이상 + 동의 체크 필수, 수정은 **등록 본인만**.
- 모달을 붙인 곳: 주문관리(`/admin/orders/export`), 답례품 후기(`/admin/gift-reviews/export`), 운영유지관리 SR(`/admin/maintenance/export`). AS-IS `PrivacyAccess`에 항목이 없는 다운로드(답례품 목록·명예등급 열람이력)는 AS-IS도 로그를 남기지 않으므로 그대로 뒀다.
- **1411 화면 자체의 버그 3건도 이때 고쳤다**: ① `PrivacyAccessLog`/`Hist` 엔티티가 `manager_id`/`user_id`(bigint)를 String으로, `created_at`(timestamp)을 String으로 매핑하고 있었다(표가 비어 있어 안 드러남) ② AS-IS 목록 쿼리의 **`WHERE TASK='엑셀 다운로드'` 필터가 빠져** 있었다 ③ 본인것만 보기 필터를 `loginId`로 비교했는데 AS-IS는 `MANAGER_ID`(숫자)다. 관리자ID 칸은 MANAGER_ID로 찾은 `op_manager.login_id`이고 검색구분 관리자ID는 **완전일치**, 사유구분은 `EXCELDOWNLOAD_REASON_TYPE` 라벨이다(AS-IS 모달에 사유구분 select가 없어 실제로는 빈칸 - verbatim).
- `admin.op_privacy_access_log.id`에 시퀀스가 없어 INSERT 자체가 불가했다 → `migration-admin-privacy-access-log-seq.sql`로 생성(AS-IS는 `OP_PRIVACY_ACCESS_LOG_SEQ`, initialValue 2152877은 운영 누적값이라 TO-BE는 1부터).
- `admin.ExcelDownloadLog`(+Repository)는 AS-IS에 없는 표라 `@Deprecated` 표시만 하고 남겼다(과거 행 보존, DROP은 사용자 판단).
- 주문관리 목록의 `downloadExcel()`이 `th:inline="none"`이어서 **검색조건이 항상 빈 값으로 나가 현재 검색조건이 무시**되던 것도 함께 고쳤다.

**caption/summary는 화면에 안 보인다**(`opmanager.css:41 caption {display:none}`) - AS-IS가 caption에 엉뚱한 문구코드를 쓴 곳(배송업체 폼의 `M00055`=전체주문 내역 등)은 사용자에게 안 보이므로 verbatim 유지한다. 눈에 보이는 건 `<h3>`뿐이다.

**추가 공통부품:** `templates/common/popup-result.html` = AS-IS `ViewUtils.redirect(url, message, javascript)`의 팝업 응답(`alert(message)` → `opener.fnSearch()` → `self.close()`). AS-IS 운영관리 팝업 화면 전부가 이 패턴이라 재사용한다.

**★[2026-10-06] 이 영역 검색 쿼리 5건이 "검색어 비움" 경로에서 500이었다.** 아침에 사용자가
**팝업관리(1311) 목록**에서 실제로 맞았다 - `ERROR: operator does not exist: character varying ~~ bytea`.
`(:query is null or ... like concat('%', :query, '%'))`에서 Hibernate가 파라미터 타입을 못 정해
null을 bytea로 바인딩한 것이다. `cast(:x as String)`을 양쪽 occurrence에 넣어 고쳤고, 같은 패턴이던
`PopupRepository`(1311) · `PolicyRepository`(1308) · `OpEmailRepository`(1410) ·
`CommonCodeRepository`(1401) · `IpsSendingMasterRepository`(문자발송) **5개를 함께** 처리했다.
`= :x`·`>= :x` 조건은 타입이 잡히므로 손대지 않았다. 네이티브 쿼리와 SQL 조립 방식은 해당 없다.
상세·판정근거는 [[hql-null-param-needs-cast]]. **25화면 "완료" 판정은 화면 렌더만 본 것이고
"검색어 비움" 경로를 호출해 본 적이 없었다** - 이 영역 재점검 시 그 경로를 먼저 볼 것.

**★[2026-10-06 2차] 팝업관리(1311) 실사용 점검에서 치명 결함 3건.** "25/25 완료"는 화면 렌더만
본 판정이었고, **검색·페이징·등록화면이 실제로는 동작하지 않았다.**
1. **검색·페이징이 전부 500** - 저장을 목록 URL에 얹어(`POST /popups`가 목록검색+등록저장 양쪽)
   `IllegalStateException: Ambiguous handler methods`. AS-IS는 **저장 URL = 폼 URL**이고
   (`POST write` / `POST edit/{popupId}`) 목록 검색만 `POST list`다 → TO-BE도
   `POST /popups/new` · `POST /popups/{id}/edit`로 분리했다. 스캐너로 admin·5개 서비스 전수
   재확인 = 다른 충돌 없음(`scratchpad/find-ambiguous2.pl`).
2. **등록/수정 화면이 렌더링 중단** - 스마트에디터 조각의 AS-IS 주석 때문. 에디터와 달력이
   동시에 사라진 원인이고 **그 조각을 쓰는 16개 화면이 같이 깨져 있었다**.
   상세·가드테스트: [[thymeleaf-js-inline-bracket-hazard]]
3. **목록 건수 퍼블리싱** - `.count_title h5 span{display:block}`이라 숫자가 줄바꿈됐다.
   AS-IS는 h5에 평문만 두고 `<span>`은 h5 **밖**(출력수 select, `h5 + span`)에 둔다.
   `th:remove="tag"`로 **44개 화면 / span 62개** 일괄 교정.
또 업로드 규칙 누락(확장자·용량·형태전환 정리·파일삭제)이 이 영역 4개 화면에 있었다 -
`docs/upload-file-parity-audit.md`. **교훈: 이 영역 "완료" 판정은 전부 렌더만 본 것이다.
검색·페이징·저장 경로를 실제로 눌러봐야 한다.**
