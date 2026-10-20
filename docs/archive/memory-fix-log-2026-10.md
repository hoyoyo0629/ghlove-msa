# 메모리 수정 일지 보관 (2026-10)

2026-10-20 메모리 통폐합 때 `docs/memory/`에서 뺀 **완료된 수정·데이터 적재 일지** 25개를 원문 그대로 모았다(제목 = 원래 메모리 파일명). 다시 쓸 교훈은 메모리 `admin-screen-pitfalls`·`backend-db-pitfalls` 등에 이미 있다. 변경 이력 자체는 git 커밋에도 남아 있다.

## admin-batch-log-time-format

> 배치 실행로그 조회(7209) 시작시간/종료시간이 HHmmss 원시값으로 보이던 것을 AS-IS처럼 HH:mm:ss로 수정(2026-10-08)


AS-IS `OP_BATCH_EXECUTION.START_TIME`/`END_TIME`은 CUBRID **TIME 컬럼**이라 JDBC가 그대로
"09:30:00" 모양 문자열로 내려주고, AS-IS JSP(`batch-log/list.jsp`)도 포맷 없이 `${list.startTime}`을
그대로 찍는다. TO-BE `admin.op_batch_execution.start_time`/`end_time`은 **VARCHAR(8)** 로
"HHmmss"(예: "013000") 저장이라 그대로 찍으면 콜론 없이 보였다.

**수정**: `BatchLogRepository.BatchLogRow`에 `getStartTimeText()`/`getEndTimeText()` 추가
(6자리 문자열에 콜론 2개 끼워 "HH:mm:ss"로), `batch-log/list.html`에서 `item.startTime`/`endTime`
대신 그 getter를 쓰도록 교체. `getExecutionDateText()`(날짜 포맷)와 같은 패턴.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-delivery-company-asis-data-loaded

> 시스템관리 > 배송업체 관리(ord.op_delivery_company) AS-IS 실데이터 29건 적재 완료(2026-10-07)


admin "배송업체 관리"(`DeliveryCompanyAdminController`, 표는 order 서비스의
`ord.op_delivery_company`)는 테이블/컬럼은 이미 AS-IS와 동일하게 존재했지만 데이터가
0건이었다. AS-IS export(`Desktop\고향사랑e음\1.AS-IS\1. DB\_op_delivery_company__202610071247.sql`)
로 실데이터 29건(한진택배/롯데택배/CJ대한통운 등 실 택배사 + 우편등기 중복 4건(31~34, use_flag
혼재) + id 2000101 '투데이')을 그대로 적재했다.

**적용**: `database/ddl/migration-order-delivery-company-asis-data.sql`(신규, 적용됨) -
`ON CONFLICT DO NOTHING`으로 INSERT 후 `op_delivery_company_delivery_company_id_seq`를
`2000101`로 `setval`(관리자 신규등록 시 ID 충돌 방지, 기존 공통코드/정책 적재 때와 동일 패턴).

**상태**: order 서비스 DB에 바로 적용 완료(psql, docker exec). 코드 변경 없음 - 재기동 불필요,
admin 화면에서 즉시 보인다(order API를 매 요청 다시 조회하는 구조라서).

## admin-excel-download-log-reason-edit-popup

> 엑셀다운로드사유관리(1411) 사유 팝업이 늘 읽기전용이던 것을 AS-IS succChk 조건대로 수정가능하게 복원(2026-10-07)


AS-IS `log/popup/exceldownload-log-detail.jsp`는 `succChk`(현재 로그인 관리자 == 그 로그를
남긴 MANAGER_ID)가 true일 때만 사유타입 select + textarea + "수정" 버튼을 보여주고, 아니면
읽기전용(사유타입 라벨 + 사유 텍스트 + "확인" 버튼만)이다. TO-BE
(`ExcelDownloadLogAdminController.reasonPopup`)는 이 분기 자체가 없어 항상 읽기전용이었다 -
수정 기능도, 그 결과로 만들어지는 수정이력도 실제로 동작할 수 없었다.

**수정**: 컨트롤러가 세션의 로그인 매니저와 `details.managerId`를 비교해 `succChk`를 계산하고,
`PrivacyAccessLogService.reasonTypeOptions()`(신규, `EXCELDOWNLOAD_REASON_TYPE` 공통코드를
등록순서대로)를 모델에 같이 넘긴다. 템플릿은 `succChk`로 편집/읽기전용 두 분기를 그려서
AS-IS와 동일하게 동작한다. 수정 POST는 이미 있던
`PrivacyAccessLogApiController.privacyAccessLogUpdate`(`/common/opmanager/privacy-access-log-update`)
를 그대로 쓴다(AS-IS 폼 필드명 `privacyAccessLogId`/`reason`/`reasonType`과 동일) - 새 엔드포인트
불필요했다. 수정 성공 후 갱신 방식은 AS-IS의 `opener.parent.search()`가 아니라 이 코드베이스가
이미 쓰는 `opener.location.reload()` 관례로 맞췄다(batch-job/form.html 등과 동일 패턴).

**이력보기 팝업도 같이 보강**: `excel-download-log-hist.html`에 AS-IS처럼 "총 N 건" 헤더와
No. 컬럼을 추가했다(컨트롤러가 `count` 모델 속성을 추가로 넘김). 전체 페이징은 추가하지 않았다
- AS-IS도 한 사람이 수정할 수 있는 이력 건수가 애초에 적어 실효성이 낮다.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-locgov-charger-edit-layout-and-breadcrumb-fix

> 지자체담당자관리(4402)·운영관리자(4501) 상세화면의 수정/목록 버튼 레이아웃 + 메뉴경로 '상세' 누락 수정, 2026-10-08


**①버튼 레이아웃**: AS-IS `locgov-charger/edit.jsp`·`oper-charger/edit.jsp`는 수정·삭제·목록
세 버튼이 **같은 `.flex_box gap-08` 안**에 있다(`<c:if>`가 수정/삭제만 감싸고 목록은 같은
div 안 마지막 자식). TO-BE `person-in-charge/locgov-edit.html`·`oper-edit.html`은 목록 버튼을
`.flex_box` **바깥**(`.btn_all`의 직계자식)으로 빼놨었다 - 같은 flex 컨테이너에 있어야 할 게
분리돼 레이아웃이 달라 보였다. `th:if`를 div 자체에 걸어서 생긴 차이로 보인다. `th:block
th:if`로 바꿔 수정/삭제만 조건부로 감싸고 목록은 항상 같은 flex_box 안에 두도록 복원.
oper-edit.html은 추가로 **`mb15` 클래스도 빠져 있었다**(AS-IS `btn_all btn_right mb15`) - 같이
복원.

**②메뉴경로("상세") 누락**: AS-IS 두 JSP 모두 메인 `$(function(){...})` 안에서
```js
$(".contents .contents_inner").find("div.location a").removeClass("on");
$(".contents .contents_inner").find("div.location").append('> <a href="..." class="on">상세</a>');
```
를 실행해 공통 브레드크럼(`Manager.setLnbHeader()`가 1~3번째 크럼을 "회원관리 > 지자체관리 >
지자체담당자관리"로 채운 뒤) 맨 끝에 4번째 "상세" 크럼을 덧붙인다. **이 스크립트 자체가 TO-BE
두 화면 모두에 없었다**(oper-edit.html은 `$(function(){...})` 블록 자체가 없어서 새로 만들어
넣었다) - 그래서 경로가 3단("회원관리 > 지자체관리 > 지자체담당자관리")에서 멈춰 있었다.
1~3번째 크럼 자체(메뉴 트리·`Manager.setLnbHeader()`)는 **이미 정상**이었다 - `admin.op_menu`에
4000(회원관리)→4400(지자체관리, 그룹헤더)→4402(지자체담당자관리) 체인이 AS-IS와 정확히
일치하는 걸 직접 확인했다. 빠진 건 4번째 크럼 스크립트뿐이었다.

[[admin-screen-pitfalls]]와 같은 유형의 갭(JSP의 스크립트 블록이 마크업만 옮기고
누락된 경우) - 이번에 2곳(4402·4501) 찾아 고쳤다. 같은 유형이 다른 상세화면에도 더 있을 수
있어 전수점검은 아직 안 했다(필요하면 요청).

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**

## admin-locgov-search-and-depthist-register-fix

> 지자체관리(4401) 목록 검색에 등록일 넣으면 조회 안 되던 버그(날짜포맷) + 부서코드 이력 팝업 등록자가 항상 빈칸이던 버그(managerId 미전송) 수정, 2026-10-08


**① 목록 검색 - 등록일 넣으면 0건(날짜포맷 버그, [[backend-db-pitfalls]]와
같은 클래스의 세 번째 발견)**: `LocgovSearchParam`이 화면의 `yyyyMMdd` 날짜를 변환 없이
`LocgovAdminClient.search()`에 그대로 넘기고, donation의 `LocgovAdminApiController.parseStart/
parseEnd`는 `LocalDate.parse()`(yyyy-MM-dd 전용)를 쓴다. 날짜를 비우면(null) 필터 자체가
안 걸려 문제가 안 드러나고, **날짜를 넣으면 donation이 500 → `LocgovAdminClient`가 빈 결과로
삼켜버려 "조회 안 됨"으로 보인다** - 정확히 사용자가 관찰한 증상("검색값 넣으면 안 되고 지우면
된다")과 일치. `curl startDate=20261008`(500) vs `startDate=2026-10-08`(정상, 성북구 1건)으로
재현 확정.
조치: `LocgovSearchParam`에 `getSrchStartCreatedForApi()`/`getSrchEndCreatedForApi()` 추가,
`LocgovAdminController.search()`가 그걸 쓰도록 변경.
**이 버그 클래스가 벌써 4번째 발견이다** - admin이 다른 서비스의 날짜범위 API를 부를 때마다
매번 확인이 빠졌다. 비슷한 패턴(`getSrchStartCreated()`를 변환 없이 쓰는 곳)이
`LocgovPersonInChargeAdminController`/`OperPersonInChargeAdminController`(→ admin 내부
`PersonInChargeAdminService`)에도 있는데, 거기는 `LocalDate.parse` 실패 시 **예외를 삼키고
기본값(전체기간)으로 폴백**하는 방식이라 500은 안 나지만 **날짜필터를 조용히 무시한다** -
다른 증상(결과가 0건이 아니라 필터링이 안 먹음)이라 이번엔 손대지 않았다. 재발 방지:
[[backend-db-pitfalls]]에 "admin이 새 날짜범위 검색을 cross-service로
만들 때마다 ForApi 변환 확인" 교훈이 이미 적혀 있다 - 그런데도 이 화면(4401)은 애초에 그
변환이 아예 없었다(처음부터 빠진 것).

**② 부서코드 이력 팝업("등록자" 항상 빈칸) - 관리자 ID 자체를 안 보내고 있었다**: 지자체관리
수정화면에서 저장하면 `LocgovAdminController.update()`가 `locgovAdminClient.update(...)`를
부르는데 **`managerId`를 넘기는 파라미터가 처음부터 없었다**(등록 `register()`도 마찬가지).
donation 쪽 `LocgovAdminService.update/register`는 `managerId`를 받아 `Locgov.lastUpdusrId`·
`LocgovDeptHist.frstRegisterId/lastUpdusrId`에 제대로 쓰는 코드가 이미 있었는데 - admin이
그 값 자체를 아예 안 보내 항상 null로 들어갔다(실측: locgovCode=11000의 부서이력 2건 모두
`lastUpdusrId: null`). 화면의 `DeptHistRow.getRegisterText()`는 `lastUpdusrId`가 없으면 빈
문자열을 돌려주므로 "등록자" 칸이 항상 비어 보였다 - 로직 자체는 정상이었고 **입력값이
없었던 것**.
조치: `LocgovAdminClient.register()`/`update()`에 `Long managerId` 파라미터 추가,
`toMultipart()`가 managerId를 멀티파트 바디에 실어 보내도록 수정, 컨트롤러 두 곳
(`create()`/`update()`)이 `viewer.getUserId()`를 넘기도록 변경.
**검증**: donation API를 managerId=1000(admin01/관리자)으로 직접 호출해 부서코드를 바꿔보니
새 이력 행(`deptHistNo=4`)에 `lastUpdusrId=1000`이 정확히 들어갔다(기존 1~3번 이력은 버그
당시 데이터라 여전히 null - 과거 데이터는 소급되지 않는다, 그대로 둠). 테스트 중 curl 인코딩
문제로 chargerNm/locgovIntrcnCn/bassAdres/processDeptCode가 잠깐 깨졌던 걸 원래 값으로
직접 SQL 복구했다(2026-10-08 적용 완료, 데이터 유실 없음).

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**

## admin-log-screens-asis-sample-data

> 로그 조회 화면 6개(엑셀다운로드사유/서울부과·수납/지방부과·수납/문자전송이력) 빈 테이블에 AS-IS 실데이터 샘플 20~30건씩 적재(2026-10-07)


시스템관리의 로그 조회 화면들이 테이블이 비어 있어 화면 검증이 안 된다는 지적으로, AS-IS
export(`Desktop\고향사랑e음\1.AS-IS\1. DB\_<table>__202610071247.sql`)에서 각각 20~30건씩
샘플을 뽑아 적재했다. 원본 파일들은 수백만~수십억 행(일부는 2.9GB)짜리 전체 운영 로그라
전부 옮기지 않고, 정규식으로 "한 줄짜리 완전한 INSERT 튜플"만 골라 그대로 복사해 썼다(한글
재입력 없음 - 이중 인코딩 위험 회피, [[tooling-and-local-env]] 참고).

**적재 내역:**
| 화면(메뉴) | 테이블 | 건수 | migration 파일 |
|---|---|---|---|
| 엑셀다운로드사유관리(1411) | admin.op_privacy_access_log (task='엑셀 다운로드') | 30 | migration-admin-privacy-access-log-asis-sample.sql |
| 서울부과로그관리(1413) | donation.gif_seoul | 25 | migration-donation-gif-seoul-asis-sample.sql |
| 서울수납로그관리(1414) | donation.gif_etax_sunap | 25 | migration-donation-gif-etax-sunap-asis-sample.sql |
| 지방부과로그관리(1415) | donation.g_next_buga_request | 24(기존시드 1건과 중복 1건 스킵) | migration-donation-g-next-buga-request-asis-sample.sql |
| 지방수납로그관리(1416) | donation.g_next_sunap_response | 25 | migration-donation-g-next-sunap-response-asis-sample.sql |
| 문자전송이력(7208) | admin.tif_ips_sndng_m | 25 | migration-admin-tif-ips-sndng-m-asis-sample.sql |

**스킵한 화면**: 배치 실행로그 조회(7209, admin.op_batch_execution)는 이미 8건이 있어서
"없으면"이라는 사용자 조건에 해당하지 않아 손대지 않았다. 필요하면 별도로 요청할 것.

**암호화 컬럼 주의**: `ip`(op_privacy_access_log)·`prvc_idntfc_info`(tif_ips_sndng_m)·
`nap_nm` 일부 행(gif_seoul)은 AS-IS가 pCrypto로 암호화한 원문 그대로다. TO-BE는 복호화
모듈이 없어 화면에 암호문(`^`...==` 형태)으로 보인다 - 버그 아니라 기존에 이미 알려진
제약([[backend-db-pitfalls]]와 무관, 각 엔티티 주석에도 명시돼 있다).

**[2026-10-07 수정] manager_id를 테스트계정으로 변경함**: 적재 직후엔 관리자ID가 안 보여서
사유 수정팝업의 succChk(본인 작성글만 수정 가능)를 테스트할 방법이 없었다. 엑셀다운로드사유
30건 전부 `manager_id`를 사용자의 테스트계정 `admin01`(user_id=1000)로 UPDATE했다(DB 직접,
`UPDATE admin.op_privacy_access_log SET manager_id = 1000 WHERE task = '엑셀 다운로드'`) -
그 결과 관리자ID 칸도 "admin01"로 정상 표시되고, admin01로 로그인하면 전부 본인 글이라 수정
가능 상태로 보인다. 서울/지방 부과·수납·문자전송이력은 manager_id 개념이 없는 표라 해당 없음.

**상태**: 전부 DB 직접 적재(docker exec psql), 코드 변경 없음. **재기동 불필요** - 각 화면이
매 요청마다 DB를 조회하는 구조라 바로 보인다.

## admin-mail-config-asis-data-loaded

> 이메일 설정(mail-config) 10개 템플릿 중 실제 등록 콘텐츠가 있는 3개(포인트소멸예정·관리자권한승인·거절)를 AS-IS export로 적재 완료(2026-10-07)


AS-IS `OP_MAIL_CONFIG`는 [[admin-screen-pitfalls]]류와 달리 템플릿 10종 중
**실제 제목+본문이 등록된 건 3개뿐**이다(나머지는 행 자체가 없음, 미등록 상태 - AS-IS 그대로
비워 둔다). 사용자가 CUBRID 개발DB에서 export한 `OP_MAIL_CONFIG_202610071417.sql` 기준으로
`migration-admin-mail-config-asis-data.sql`에 담아 적재했다:

- `expiration_point`(포인트소멸예정) - created_date 20221219
- `manager_request_approval`(관리자 권한 승인) - created_date 20221117
- `manager_request_reject`(관리자 권한 거절) - created_date 20221117

셋 다 `BUYER_SEND_FLAG`/`ADMIN_SEND_FLAG` = **'N'(미발송)** 까지 AS-IS 그대로다 - 발송을 켜는
건 이 작업 범위가 아니다([[asis-porting-principles]]). 본문은 AS-IS의 인라인
HTML 이메일 템플릿(로고·대체코드 `{site_name}`/`{login_id}`/`{admin_role}`/`{reject_resn}`/
`{expiration_point}`/`{expiration_date}`/`{search_date}`/`{name}` 등)을 글자 그대로 옮겼다.

기존에 사용자가 화면 테스트용으로 만들어 둔 `order_deposit_wait`(title='입금대기',
buyer_subject='test') 행은 건드리지 않았다 - 사용자 테스트 데이터이고 이번 요청(3개 템플릿)
범위 밖.

같은 화면에서 바로 전에 고친 "메일 대체코드" 팝업의 `bankVirtualNo` 라벨 오류
(`MailTemplateCodes.java`, "가상계좌번호"→AS-IS 실제값 "입금은행정보")는 코드 수정이고,
이 메모는 그 다음에 처리한 **데이터** 적재 건이다.

## admin-menu-admin-seller-data-and-pagination

> 메뉴관리(1409) 답례품관리자 트리 데이터 적재 + 페이징 제거(AS-IS는 페이저 없음, 2026-10-07)


**네이티브쿼리 Character 캐스팅 버그**: `MenuTreeRepository.tree()`가 재귀CTE 결과의 1글자
varchar 컬럼(`display_flag`/`status_code`)을 `(String)`으로 바로 캐스팅해서 500 에러
(`ClassCastException: Character cannot be cast to String`, admin.log로 실제 확인). Hibernate가
단일문자 varchar를 Character로 돌려줄 수 있어서다 - `BatchLogRepository`/`QestnarRepository`와
같은 `asString()`(`value == null ? null : value.toString()`) 헬퍼로 교체해서 해결.

**답례품관리자(SELLER) 트리 빈 화면**: `admin.op_menu_seller`가 완전히 0건(root 행도 없음)이라
재귀CTE 시작점이 없어 항상 빈 목록이었다. AS-IS export
(`Desktop\고향사랑e음\1.AS-IS\1. DB\_op_menu_seller__202610071247.sql`)로 52행(root 포함)
그대로 적재(`database/ddl/migration-admin-menu-seller-asis-data.sql`, 적용됨).

**페이징 제거**: 이전 TO-BE는 AS-IS의 결함(LIMIT 10인데 JSP에 페이저 태그가 없어 11번째부터
안 보임)을 "고치려고" 페이저를 추가해뒀었다. 사용자가 실제 화면을 보고 AS-IS처럼 페이징 없이
전체를 보여주는 쪵으로 되돌리라고 지시 - `MenuAdminController.list()`에서 `Pagination`/`page`
파라미터 제거, 템플릿에서 `pagination-wrap` 제거. LIMIT 10 자체를 재현하는 게 아니라 "페이징을
안 한다"(전체 표시)로 해석했다 - AS-IS의 10건 캡은 JSP 결함이지 의도가 아니었다는 컨트롤러
주석이 이미 있었고, 사용자 지시도 "페이징 처리 안 하게"였기 때문.

**상태**: admin compileJava+test+bootJar 통과, DB는 바로 적용됨. **재기동 필요: admin.**

## admin-oper-charger-authority-status-disable-rules

> 운영관리자(4501) 상세화면의 회원구분·사용여부 라디오 비활성화 4규칙(AS-IS adminRoleCheck) 신규구현 - TO-BE엔 이 로직 자체가 전혀 없었음, 2026-10-08


**발견 경위**: 사용자가 `/admin/person-in-charge/oper/edit/1005`에서 회원구분(authority)이
AS-IS는 disabled인데 TO-BE는 바뀐다고 지적. 확인해보니 `OperPersonInChargeAdminController`는
`editable`(수정/삭제 버튼 노출)만 계산하고, AS-IS `oper-charger/edit.jsp`의
`adminRoleCheck()`(jQuery로 회원구분·사용여부 라디오에 disabled를 다는 함수) **전체가 아예
포팅되지 않은 상태**였다. 사용자가 짚은 건 그 중 일부(그룹 간 전환 금지)였지만 실제로는
4규칙 전부가 없었다.

**AS-IS 4규칙** (각각 true로 켜지기만 하고 끄는 분기가 없어 OR로 누적됨):
1. 대상 또는 보는 사람이 시스템(ROLE_ADMIN_1·2)이면 → 행안부 옵션(3·4) 비활성
2. 대상 또는 보는 사람이 행안부(3·4)이면 → 시스템 옵션(1·2) 비활성
   (1+2를 합치면 **시스템↔행안부 그룹 전환 자체가 불가능** - 같은 그룹 내 주/부 승격만 가능)
3. 주담당자(1·3)가 **본인 자신**을 보고 있으면 → 사용여부 비활성(자기 자신 중지 방지)
4. 부담당자(2·4)는 → 회원구분·사용여부 **둘 다** 비활성

**조치**: `OperPersonInChargeAdminController.applyRoleFlags()` 신규(private static) -
viewer/target의 authority로 `authorityGovDisabled`/`authoritySysDisabled`/`statusDisabled`
세 불린을 계산해 모델에 담고, `oper-edit.html`의 회원구분 4개·사용여부 2개 라디오에
`th:disabled`로 적용했다(지자체담당자관리 4402가 이미 쓰던 `th:disabled` 패턴과 동일 - AS-IS는
jQuery로 하지만 서버에서 직접 거는 게 TO-BE 기존 관례).

제출 시 값 읽기는 그대로 둬도 된다 - `chargerEdit()`의 `$("input[name=authority]:checked").val()`은
disabled라도 이미 checked인 값은 그대로 읽힌다(jQuery `:checked`는 disabled 여부와 무관) -
AS-IS도 서버쪽 추가 검증 없이 이 방식 하나로 막는다.

**검증(실데이터 추적)**: userId=1005(mois02, ROLE_ADMIN_4 행안부 부담당자)를 시스템 주관리자
(admin01, 1000)가 보면 targetIsGov+viewerIsSys 둘 다 걸려 회원구분 **전부** 비활성(그룹이
다르니 어느 쪽도 못 고름), 사용여부는 가능. 같은 행안부 소속(mois01, ROLE_ADMIN_3)이 보면
행안부 옵션만 활성(주/부 승격 가능)·시스템 옵션은 비활성·사용여부도 가능 - 의도대로 동작.

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**

## admin-policy-asis-data-loaded

> 약관관리(OP_POLICY) AS-IS 실데이터 22건 적재 완료(2026-10-07) - 테이블 하나뿐, 컬럼 구조 이미 일치, 시퀀스 보정


policy-content-db-migration-deferred(보관: docs/archive/memory-fix-log-2026-10.md)에서 "운영데이터 받아 적재 후 재검토"로 보류해 둔 것을
사용자가 export해준 `OP_POLICY_202610071425.sql`(22건, 9.1MB - 약관 전문이 커서 큼)로 완료했다.

AS-IS 테이블은 `OP_POLICY` 하나뿐(policy-mapper.xml 확인). TO-BE DDL·`Policy` 엔티티 컬럼이
이미 export와 1:1 일치해(POLICY_ID/POLICY_TYPE/CONTENT/CREATED_DATE/CREATED_USER_ID/TITLE/
EXHIBITION_PERIOD/EXHIBITION_STATUS/UPDATED_DATE/UPDATED_LOGIN_ID/EXHIBITION_START_DATE/
EXHIBITION_END_DATE) 변환 없이 그대로 `psql`로 적재. 적재 후 `op_policy_policy_id_seq`를
최대 ID(2000125) 이후로 `setval` 보정 - 안 했으면 화면에서 신규 등록 시 기존 ID와 충돌했을 것.

**데이터 특징**: POLICY_TYPE별 여러 버전이 날짜순으로 쌓여있다(개인정보처리방침만 15건,
생성일 2022~2026 범위) - AS-IS가 전시기간 지정형 버전관리로 쓰는 것과 일치
(`Policy.java` 클래스 주석의 "버전 관리처럼" 설명 그대로). 타입별 전시중(Y) 행은 정확히
하나씩이다(0=1·1=1·5=1, policy_id=2000125는 title에 "테스트"라고 적혀 있는데 개발DB
원본 그대로라 안 고쳤다). TYPE 2·3(특정상거래법·마케팅이용약관)은 등록된 행이 아예 없다 -
결함이 아니라 AS-IS에 그 타입 데이터가 없는 것(donation의 해당 API는 이 경우 정적
폴백파일을 쓰는데, 이것도 AS-IS와 같은 결과). TYPE 6(개인정보 수집이용 동의) 행이 여럿
있는데, `Policy.getPolicyTypeLabel()`에 '6' 분기가 없어 목록에 구분이 빈칸으로 나오는 것도
AS-IS 그대로([[asis-porting-principles]], 건들지 않음).

**상태**: 순수 데이터 적재, 코드 변경 없음(빌드·재기동 불필요). 화면에서 바로 확인 가능.

## admin-privacy-log-hist-sort-order

> 엑셀다운로드 사유 수정이력 팝업 정렬을 HIST_ID DESC에서 AS-IS와 같은 CREATED_AT DESC로 수정(2026-10-07)


AS-IS `privacy-access-mapper.xml`의 `getPrivacyAccessLogHistListByParam`은
`ORDER BY OP.CREATED_AT DESC`다(HIST_ID 아님). TO-BE
`PrivacyAccessLogHistRepository.findByPrivacyAccessLogIdOrderByHistIdDesc`는 HIST_ID DESC로
정렬하고 있었다 - 보통은 결과가 같지만, admin-log-screens-asis-sample-data(보관: docs/archive/memory-fix-log-2026-10.md)에서 샘플
로그에 "최초 생성" baseline 이력을 나중에(= 더 큰 HIST_ID로) 과거 날짜로 끼워넣었더니 바로
깨졌다(과거 생성 이력이 최근 수정 이력보다 위에 뜸). `findByPrivacyAccessLogIdOrderByCreatedAtDesc`로
교체해서 해결 - 실제 쿼리로 시간역순 확인 완료.

**교훈**: ID 기반 정렬과 시간 기반 정렬은 "보통은 같다"고 가정하지 말 것 - AS-IS 매퍼가 명시한
컬럼을 그대로 쓸 것. 특히 수동으로 과거 시점 데이터를 나중에 삽입하는 시나리오(백필, 샘플
데이터 보강)에서 이 차이가 바로 드러난다.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-sleep-user-seed-data

> 휴면회원관리(admin 4107, /admin/sleep-users) 확인용 가짜 시드 25건 적재(2026-10-08) - member.op_user STATUS_CODE='DORMANT'


`database/ddl/seed-member-sleep-users.sql`(적용 완료) - `member.op_user`에 `dormant01`~
`dormant25` 25건을 STATUS_CODE='DORMANT'로 추가(기존 계정 변경 없음), `op_user_detail`에
주소(전체 시/도 공식명 - "서울특별시"/"경기도" 등, storefront-address-widget-mismatch-deferred(보관: docs/archive/memory-fix-log-2026-10.md)
조사에서 축약형 문제를 인지한 뒤 시드는 전부 정식명으로 작성)까지 같이 넣었다.

최종방문일(login_date)은 **오늘이 아니라 3~13개월 전으로 분산**했다 - AS-IS 자체 설계가
"휴면회원은 정의상 로그인이 오래됐으므로 검색 기본값(오늘)으로는 보통 비어 있고, 운영자가
직접 기간을 넓혀 조회하는 UX"([[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-member-area-port-progress) 4107 항목)라서다. 화면에서
그냥 검색하면 0건이고, 가입일 범위의 **"전체" 또는 "1년" 버튼**을 눌러야 25건이 다 보인다
(testuser01 가입일 검색 때와 같은 패턴 - 버그 아님).

[[backend-db-pitfalls]]를 먼저 고친 뒤에 이 시드를 넣었다 - 그 버그가
남아있었으면 날짜범위를 넓혀도 어차피 member API가 500→빈 결과였을 것이다.

기부누적액·포인트잔액 컬럼용 donation/point 데이터는 넣지 않았다(화면은 값 없으면 '-'로
표시하므로 공백 상태로도 화면 확인은 가능) - 필요하면 추가 요청.

## admin-sms-log-date-columns-and-content

> 문자전송이력 목록의 생성일시/전송시작일시/전송완료일시 컬럼매핑 오류 + 발송내용/오류내용 렌더링을 AS-IS와 동일하게 수정(2026-10-07)


admin-sms-log-phone-status-display(보관: docs/archive/memory-fix-log-2026-10.md)에 이어 문자전송이력(`/admin/send-sms-logs`) 나머지
컬럼도 AS-IS 소스(`ghlove-web`의 `SmsController`+`sms-log/list.jsp`, `ghlove-common`의
`TifIpsSndngMDisplay`/`SmsIpsServiceImpl`/`sms-mapper.xml`)와 전수 대조했다.

**1. 날짜 3컬럼 - 필드 자체가 잘못 매핑돼 있었다(포맷 문제가 아니었다)**

AS-IS JSP 바인딩: "생성일시"→`esbInitTimeF`, "전송시작일시"→`esbTxTimeF`, "전송완료일시"→
`esbComptTimeF`(전부 `DateUtils.datetime()`로 `yyyyMMddHHmmss`→`yyyy-MM-dd HH:mm:ss` 포맷).
`INFO_CRT_DT`는 이 화면에 전혀 안 쓴다(매퍼 SELECT 목록에도 없음) - 적재 시 공통값으로만
기록되는 컬럼이다.

TO-BE는 "생성일시"에 `infoCrtDt`(완전히 다른 컬럼), "전송시작일시"에 `esbInitTime`(한 칸씩
밀림), "전송완료일시"에 `esbComptTime`(우연히 맞음, 포맷 없음)을 쓰고 있었다 - 컬럼 자체가
밀린 버그. `esbTxTime` 필드는 엔티티에 있는데 화면에서 전혀 안 쓰이고 있었다.

수정: 템플릿을 `@opDate.ymdHms(item.esbInitTime)` / `@opDate.ymdHms(item.esbTxTime)` /
`@opDate.ymdHms(item.esbComptTime)`로 교체. 안 쓰던 `IpsSendingMaster.getInfoCrtDtText()`는
삭제(AS-IS도 이 화면에 안 쓴다).

**검색 날짜 필터도 틀려 있었다**: AS-IS는 "생성일자" 라벨이지만 실제로는
`ESB_INIT_TIME BETWEEN CONCAT(start,'000000') AND CONCAT(end,'999999')`로 거른다(매퍼
그대로). TO-BE `SmsIpsService.search()`는 `infoCrtDt`(LocalDateTime)로 거르고 있었다 -
화면에 안 보이는 컬럼으로 검색하던 셈. `search()` 시그니처를 `LocalDateTime`에서
`String searchStartDate/searchEndDate`(yyyyMMdd)로 바꾸고 `esbInitTime` 문자열 BETWEEN
비교로 교체했다. 컨트롤러의 `startOfDay`/`startOfNextDay`/`parse` 변환 헬퍼는 제거(더 이상
필요 없음).

**검색내용(이름,전화번호 등) 필터도 과했다**: AS-IS 매퍼는 `SNDNG_CNTNTS`만 LIKE한다
(`PRVC_IDNTFC_INFO` 조건 없음). TO-BE는 `prvcIdntfcInfo`(CI)까지 같이 LIKE하고 있었다 -
AS-IS에 없는 조건이라 제거.

**2. 발송내용 - 원시값이 아니라 렌더링된 알림문구였다**

AS-IS "발송내용" 컬럼은 `SNDNG_CNTNTS`(파이프 구분 원시값, 예:
`정철교|2023.06.23...|K0000006106|산채냉면 혼합세트|롯데택배|01090293686`) 그대로가 아니라
`TifIpsSndngMDisplay.getSmsContent()` - 문자종류(SVC_ID→SmsType) 24종별 완전히 다른 한국어
안내문구 템플릿에 파이프 항목을 꽂아 넣은 결과를 20자로 잘라 보여준다(전체는 title 툴팁).
JOIN_MEMBERSHIP/OVERPAYMENT/OVERPAYMENT_CANCEL 3종은 `donationVerification.donationLimitAmt()
.getDetail()`(공통코드 `DONATION_LIMIT_AMT`의 올해 DETAIL, 예: "2천만")도 끼워 넣는다.

TO-BE는 `sndngCntnts` 원시값을 그대로, 자르기도 없이 보여주고 있었다 - 완전히 다른 내용.

수정: AS-IS `getSmsContent()`의 24개 분기를 그대로 포팅한
`SmsContentRenderer`(`@Component("smsContent")`, Thymeleaf에서 `@smsContent.render(svcId,
sndngCntnts, limitAmtString)`로 호출)를 신설. `limitAmtString`은
`SmsIpsService.donationLimitAmtDetail()`(공통코드 `DONATION_LIMIT_AMT` + 올해 연도 id 조회,
이미 2024~2030년치가 적재돼 있음 - admin-common-code-asis-sync-2026-10-07(보관: docs/archive/memory-fix-log-2026-10.md))로 구해 모델에
담아 템플릿에 전달.

**3. 오류내용 - 20자 자르기+툴팁 누락**

AS-IS는 `esbErrMsg`도 20자로 자르고 전체는 title 툴팁(발송내용과 동일한 `op:strcut` 패턴).
TO-BE는 자르기 없이 전체를 그대로 찍고 있었다 - 수정.

**새 헬퍼 `TextCut`(`@Component("strcut")`)**: AS-IS EL 함수 `op:strcut`
(`com.onlinepowers.framework.util.StringUtils.strcut`) 재현. ⚠️ **프레임워크 원본 소스가
이 저장소에 없다**(`ghlove/build.gradle`의 `includeBuild '../opframework'`가 주석처리돼
빠져 있어 `DateUtils`/`StringUtils` 등 진짜 구현을 못 봤다) - "N자 초과 시 잘라서 '...' 붙이기"
라는 가장 흔한 규칙으로 재현했을 뿐, 바이트 단위 처리 등 디테일은 확인 못했다. 나중에
`opframework` 소스를 구하면 재검증 필요.

**검증**: `SmsContentRendererTest`(PRESENT_SHIPPING·JOIN_MEMBERSHIP 렌더링, 알 수 없는
코드/빈 값/깨진 파이프 항목에서 예외 대신 빈 문자열), `TextCutTest`, 그리고
`SmsIpsServiceTest.donationLimitAmtDetailReadsSeededCommonCode`(실제 DB에서 "2천만" 조회
확인).

**4. 검색창 레이아웃 + 버튼 색상 (2026-10-07 추가)**

AS-IS는 4열 테이블(`<colgroup>` 220px/auto/220px/auto)로 "문자 구분"+"검색내용"을 한 행에
나란히 배치하고, "생성일자" 행은 `colspan="3"`으로 합쳐서 datepicker 2개 + day_btns(오늘/1주/
1개월/3개월/1년 바로가기, `Common.DateButtonEvent`/`EventHandler.calendarStartDateAndEndDateVaild`
로 동작)까지 포함한다. TO-BE는 2열 3행으로 전부 세로로 쌓여 있었고 day_btns도 없었다 - 구조를
AS-IS대로 재구성.

초기화 버튼도 AS-IS는 `btn btn-default`(검색 버튼과 다른 옅은 색)인데 TO-BE는 검색 버튼과 같은
`btn-dark-gray`를 쓰고 있었다 - `btn-default`로 교정(AS-IS sms-log/list.jsp 원본 class 그대로;
단 exceldownload-log-list.jsp처럼 AS-IS 안에서도 화면별로 버튼 색 조합이 다른 경우가 있어
화면마다 그 화면 자신의 AS-IS JSP를 봐야 한다).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-sms-log-phone-status-display

> 문자전송이력 목록의 전화번호·처리상태 표시를 AS-IS(TifIpsSndngMDisplay)와 동일하게 수정(2026-10-07)


AS-IS 소스(`ghlove-web`/`saleson.common.sms.domain.TifIpsSndngMDisplay`)를 대조해
문자전송이력(`/admin/send-sms-logs`) 목록의 두 컬럼을 바로잡았다.

1. **전화번호**: 암호화/복호화 문제가 아니었다. AS-IS는 `PRVC_IDNTFC_INFO`(회원 CI, 암호화됨)를
   보여주는 게 아니라 발송내용(`SNDNG_CNTNTS`)의 **마지막 `|` 구분 항목**에서 전화번호를
   파싱해 보여준다(`getPhoneNumber()` - 숫자만 뽑아 10~11자리 패턴이면 원문을, 아니면 빈
   문자열을 돌려준다). TO-BE는 `prvcIdntfcInfo`를 그대로 바인딩하고 있던 **필드 오바인딩
   버그**였다.
2. **처리상태**: AS-IS `getEsbStatus()`가 `N→대기`, `S→성공`, `F→실패`로 변환한다. TO-BE는
   `esbStatusCd` 코드값을 그대로 찍고 있었다.

**수정**: `IpsSendingMaster` 엔티티에 `@Transient` getter 두 개 추가 -
`getPhoneNumberText()`(AS-IS `getPhoneNumber` 로직 그대로, 정규식 `^\d{3}\d{3,4}\d{4}$`)와
`getEsbStatusText()`(AS-IS `getEsbStatus` 그대로). `send-log-admin/sms-list.html`에서
`item.prvcIdntfcInfo` → `item.phoneNumberText`, `item.esbStatusCd` → `item.esbStatusText`로
교체.

**참고**: `esbInitTime`/`esbComptTime`(전송시작·완료일시)은 AS-IS도 `DateUtils.datetime()`으로
포맷하는 별도 getter(`getEsbInitTimeF`/`getEsbComptTimeF`)가 있는데, 이번엔 손대지 않았다
(사용자가 전화번호·처리상태 두 가지만 지적했음 - 날짜 포맷도 같은 화면에 또 문제가 있을 수
있어 다음에 확인 필요).

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-survey-date-format-and-preview

> 설문관리(1409 아님 7207) 목록 기간 날짜 포맷 + 미리보기 링크 AS-IS 동일화(2026-10-07)


**기간 날짜 포맷**: `G_QESTNAR.QUSTNR_BGN_DE/QUSTNR_END_DE`는 AS-IS·TO-BE 둘 다 `VARCHAR(8)`
(`yyyyMMdd`)로 저장한다. AS-IS `qustnr-mapper.xml`의 `getQustnrList`는 `DATE_FORMAT(...,
'%Y-%m-%d')`로 포맷해서 내려주는데, TO-BE `QestnarRepository.getQustnrList`는 포맷 없이 원본을
그대로 돌려줬고 템플릿도 그대로 찍어서 "20260803" 식으로 보였다(AS-IS는 "2026-08-03"). SQL을
건드리지 않고 이미 프로젝트 전역에 쓰는 [[admin-screen-pitfalls]] 패턴대로
`survey-admin/list.html`에서 `${@opDate.ymd(item.qustnrBgnDe)}`로 템플릿 레벨에서 포맷했다
(8자리 숫자 문자열을 받는 `ymd()`가 그대로 맞는다 - 이 헬퍼는 길이로만 분기해서 14자리
`yyyyMMddHHmmss`든 8자리 `yyyyMMdd`든 다 처리된다). 참고로 수정/상세 폼의 `getQustnr`은
AS-IS도 포맷 없이 원본을 그대로 내려줘서(`qustnr-mapper.xml` 141번 줄) datepicker 입력값이
AS-IS도 "20260803"으로 보인다 - 이건 AS-IS 자체 동작이라 안 건드렸다(목록만 지적받음).

**미리보기**: 이전 TO-BE `preview(qustnrSn)`는 `/api/surveys/{id}`(JSON API 원본)을 새 창으로
열어서 미리보기가 아니었다. AS-IS는 `saleson.url.frontend + /qustnr/detail_srvy.html?qustnrSn=`을
연다(별도 프론트의 실제 설문 참여 화면). TO-BE storefront에 이미 그 역할의 실화면
(`storefront/src/views/SurveyView.vue`, 라우트 `/survey/:id`)이 있어서 `preview()`가 그
URL(`http://localhost:5173/survey/{id}`)을 열도록 고쳤다. 절대URL 하드코딩은
[[production-domain-migration-plan]]의 기존 29곳과 같은 유형(서비스마다 오리진이 달라 당장은
상대경로가 안 됨) - 그 계획이 진행되면 같이 정리된다.

**상태**: 템플릿만 수정(Java 변경 없음), admin compileJava+test+bootJar 통과. **재기동 필요: admin.**

## admin-common-code-asis-sync-2026-10-07

> 시스템관리 > 공통코드 관리 2건 수정(2026-10-07) - 코드(id) 필드 readonly 제거, ORDERING NULL 정렬이 CUBRID/Postgres 기본값 차이로 반대순서 되던 것 수정


**1) 코드(id) 입력 readonly 제거**: AS-IS `code/form.jsp`는 "코드구분"만 readonly이고 "코드"(id)는
수정화면에서도 입력 가능하다. TO-BE [codes/form.html](../../admin/src/main/resources/templates/codes/form.html)이
`th:readonly="${!isNew}"`를 넣어 수정화면에서 막아 둔 걸 제거했다(AS-IS와 동일하게 항상 편집 가능).
**단 알아둘 것**: AS-IS JSP의 `<form:form>`은 action 속성이 없어 Spring이 현재 URL(쿼리스트링
제외)로 self-post하므로 `whereCodeType`/`whereId`(원래 키) 히든필드가 애초에 없다 - id를 실제로
바꿔 저장했을 때 AS-IS가 제대로 rename하는지는 의심스럽다(코드상 추적 결과 0건 UPDATE로 끝날
가능성). TO-BE는 `update(code.getCodeType(), code.getId(), ...)`가 제출된(새) id로 기존 행을 찾으므로
**id를 바꾸면 "코드를 찾을 수 없습니다" 에러**가 난다 - AS-IS의 모호한 동작을 그대로 재현하진
않았고, 사용자가 명시적으로 요청한 "필드 편집 가능"만 맞췄다. rename을 실제로 지원해야 하면
추가 작업 필요(사용자 확인 후).

**2) 목록 정렬 - ORDERING NULL 처리 차이**: 데이터는 틀리지 않았다(TEL/ORDER_PAY_TYPE/
CANCEL_REASON/EXCHANGE_REASON/RETURN_REASON/REMITTANCE_TYPE 등은 AS-IS 원본부터 ORDERING이
비어 있다 - TO-BE가 지어낸 게 아니라 export 그대로임). 원인은 **DB 엔진 기본 NULL 정렬 방향
차이**: CUBRID(MySQL 계열)는 ASC에서 NULL을 가장 작은 값으로 취급해 맨 앞(NULLS FIRST), PostgreSQL
ASC 기본은 반대로 맨 뒤(NULLS LAST)다. `ORDER BY ordering`만 쓰면 ORDERING이 비어 있는 행들이
AS-IS와 반대쪽 끝에 몰린다. [CommonCodeRepository](../../admin/src/main/java/com/ghlove/admin/repository/CommonCodeRepository.java)의
ordering 정렬 쿼리 3곳(`search`, `findByCodeTypeOrderByOrdering`, `findAllByOrderByCodeTypeAscOrderingAsc`)
전부 `order by ... nulls first`로 명시해 CUBRID와 같은 순서로 맞췄다. `findByCodeTypeOrderByOrdering`은
ManualAdminService/OpEmailService/PrivacyAccessLogService도 같이 쓰므로 그 화면들의 코드 드롭다운
순서도 같이 고쳐졌다.

**★3)[2026-10-07 후속] 테이블 자체가 틀렸다 - ADMIN_COMMON_CODE는 AS-IS에 없다**: 사용자가
"as-is도 admin_common_code를 보는 게 맞는지"를 재확인하라고 지적. AS-IS 소스 전체(41개 매퍼 +
code-mapper.xml) grep 결과 **"ADMIN_COMMON_CODE"는 0건** - AS-IS의 유일한 공통코드 테이블은
`OP_COMMON_CODE`뿐이다(실사용, 60곳 이상에서 셀렉트박스 옵션을 읽음). 그런데 `CommonCode`
엔티티가 지금까지 `ADMIN_COMMON_CODE`(TO-BE가 지은 이름)를 가리키고 있었다 - AS-IS에 없는
이름을 TO-BE가 임의로 만든 것([[asis-porting-principles]]류). 실제로 화면이 읽는 그
테이블의 데이터가 부실했다(MAINTEN_STATE_CODE의 DETAIL이 전부 빈 값 등) - "코드상세가 AS-IS와
다르다"(사용자 지적 3번)의 진짜 원인이었다. 반대로 `OP_COMMON_CODE`(AS-IS 이름과 동일, 앞서
2026-10-02 export로 정확히 적재해 둠)는 아무도 안 읽는 고아 테이블이었다.

**조치**: `migration-admin-common-code-retire-wrong-table.sql`로 ADMIN_COMMON_CODE에만 있던
TO-BE 전용 의미코드 7종(BSNS_PURPS_CODE/DELIVERY_CARRIER/MANAGER_STATUS/NOTICE_CATEGORY/
POLICY_TYPE/POPUP_TYPE/SETTLEMENT_STATUS, 29행)을 OP_COMMON_CODE로 옮긴 뒤(79개 타입 전부
확인), `CommonCode.java`를 `@Table(name="OP_COMMON_CODE")` + 언어 컬럼명 `LANGUAGE`(AS-IS와
동일, 기존 `CODE_LANGUAGE`는 TO-BE가 지은 이름)로 전환. ADMIN_COMMON_CODE는 이제 완전히
미사용 - DROP은 사용자 확인 후(보류 원칙).

**★4) 수정 저장 시 에러**: "정렬순서" 입력이 비어 있는 상태(AS-IS op:negativeNumberToEmpty가
음수/빈값을 빈 문자열로 보여주는 그 상태)로 저장하면 `CommonCode.ordering`(Integer) 자동
바인딩이 터져 500 에러. `@ModelAttribute`에 BindingResult가 없어 컨트롤러 메서드 진입 전에
던져지는 예외라 메서드 내부 try/catch로 못 잡는다 - `CommonCodeController`에 `@InitBinder`로
빈 문자열→null 허용 `CustomNumberEditor` 등록. 겸사겸사 `edit()`에도 `create()`와 같은
try/catch(CommonCodeException) 추가(기존엔 수정만 에러 처리가 빠져 있었다).

**★5)[후속, 같은 날] ADMIN_COMMON_CODE DROP 완료**: 사용자가 "as-is도 admin_common_code를
보는 게 맞는지"를 재확인하라고 지적 → 위 3)에서 AS-IS에 그 이름이 없다는 걸 재확인한 뒤
`DROP TABLE admin.admin_common_code` 실행(FK/뷰 의존 0건 확인 후). `service-admin.sql`에서
`CREATE TABLE ADMIN_COMMON_CODE` + 거기 묶여 있던 INSERT 11건(NOTICE_CATEGORY/POPUP_TYPE/
SETTLEMENT_STATUS×2/DELIVERY_CARRIER/FAQ_TYPE/MANAGER_STATUS/REQST_SE_CODE/BSNS_PURPS_CODE/
11ST_DELIVERY_COMPANY/POLICY_TYPE)도 제거 - 재기동/볼륨초기화 시 되살아나지 않도록. 제거 전
전부 OP_COMMON_CODE에 동등하거나 더 정확한 데이터가 이미 있는지 확인했다(REQST_SE_CODE는
AS-IS 진짜 값이 ROLE_ADMIN_* 코드로 이미 들어있었고, TO-BE가 지었던 LOCALGOV/PROVIDER/OPERATOR는
애초에 틀린 값이었다). 빈 깡통 주석 블록·스테일 주석 4곳(OP_NOTICE 연결 주석, QNA_GROUPS
임포트 주석, xlsx 마스터데이터 주석, POLICY_TYPE 주석)도 OP_COMMON_CODE 기준으로 정정.
`LocgFaqAdminController`의 "FAQ_TYPE이 빈 값으로 나온다" 결함 주석도 이미 해소된 상태였음을
확인해 갱신(별도 migration-admin-faq-type-codes-fix.sql로 전에 고쳐져 있었다). 임시 DB에
`service-admin.sql` 전체를 처음부터 적용해 문법 무결성 확인(EXIT=0).

**상태**: 빌드·테스트·스페어포트 기동·DDL 전체재적용 전부 통과, admin bootJar 완료
(2026-10-07 11:51, DROP은 그 이후 DB에 바로 적용). 재기동 대기.

**★6)[같은 날, git pull 시 발견] 동료가 병행으로 정반대 결론을 이미 커밋해 둔 상태였다**:
`git pull` 하자 `LocgFaqAdminController.java`에서 충돌. 동료쪽(다른 세션)은 같은 날 "두 표가
둘 다 실재하고 각자 다르게 읽힌다"(JPA·`labelsOf()` 53곳/31파일 → ADMIN_COMMON_CODE, 네이티브
SQL 10여곳 → op_common_code)는 걸 먼저 확인해 **"표 통합은 위험하니 보류, 두 표 유지"로 사용자
결정**까지 기록해 뒀었다(admin-common-code-two-tables(보관: docs/archive/memory-fix-log-2026-10.md)). 담당자용 FAQ(11405) 탭이 빈 증상도
그 둘 중 하나로 좁게 고쳐 둔 상태(`migration-admin-cmnty-faq-type-codes-admin-table.sql`로
`CMNTY_FAQ_TYPE` 11건을 ADMIN_COMMON_CODE에도 복제).

**사용자가 동료와 조율 후 확정**: 이쪽(단일 `op_common_code`, AS-IS 실명) 방향이 맞다고 결정.
병합 처리:
- `LocgFaqAdminController.java` 충돌 해소 - 두 경위를 다 적고 최종 결정과 근거(AS-IS 41개 매퍼
  전체에 `ADMIN_COMMON_CODE` 0건)를 명시.
- `CmntyFaqBbsAdminService.java`의 "두 갈래 주의" 주석을 "통합으로 해소됨"으로 갱신.
- 동료가 추가한 `migration-admin-cmnty-faq-type-codes-admin-table.sql`과 기존
  `migration-admin-common-code-asis-gap.sql`(둘 다 `admin.admin_common_code` 대상) 머릿말에
  "DROP돼 재실행하면 relation does not exist" 폐기 공지 추가 - 원문은 그대로 보존.
- `seed-admin-manager-request.sql`의 `UPDATE admin.admin_common_code ...`(REQST_SE_CODE
  LOCALGOV/PROVIDER/OPERATOR 비활성화)는 대상 표가 없어져 주석 처리 - `op_common_code`는
  애초에 그 3개를 가진 적이 없어 실질 영향 없음.
- `docs/memory/admin-common-code-two-tables.md`에 해소 공지 추가(본문은 과거 상태 기록으로 보존).
- 동료가 같이 고친 무관한 버그 3건(Cmnty*AdminRepository의 Timestamp→LocalDateTime 캐스팅
  방어, `admin/login.html`·`community/faqBbs/list.html`의 AS-IS JSP 꼬리 `<style>` 복원
  - [[admin-screen-pitfalls]])은 그대로 유지, 손대지 않음.

**검증**: `CMNTY_FAQ_TYPE`(11405가 쓰는 코드)이 `op_common_code`에 이미 11건(use_yn=Y) 있어
동료 쪽 좁은 패치가 없어져도 결과는 동일함을 확인. `REQST_SE_CODE`도 `op_common_code`가
AS-IS 그대로(ROLE_ADMIN_2/4/6/8/10/11)라 LOCALGOV/PROVIDER/OPERATOR 비활성화가 원래부터
불필요했음을 확인. 72개 겹치는 코드유형의 **행 단위 `use_yn` 전수 대조는 못 했다**
(ADMIN_COMMON_CODE가 이미 DROP된 뒤라 직접 재대조 불가) - 다만 과거 세션이 71유형/1091행
대조를 이미 끝내 갭 2건(ORDER_STATUS 98/99, 이미 양쪽 존재 확인됨)뿐이라고 기록해 둔 바 있어
완전한 암맹은 아님. **다른 개발자 로컬 DB에도 이 DROP을 알려야 한다** - 그쪽 환경엔 아직
ADMIN_COMMON_CODE가 남아있을 것.

## admin-common-code-two-tables

> [해소됨 - 과거 기록] admin 공통코드가 2026-10-07 하루 동안 표 둘로 갈려 있던 사건. 지금은 op_common_code 단일표이고 ADMIN_COMMON_CODE는 DROP됐다


> **⚠ 이 메모는 끝난 사건의 기록이다. 지금 따라야 할 규칙은 여기 없다.**
> 현재 공통코드는 **`admin.op_common_code` 하나뿐**이고 `CommonCode` 엔티티가 그 표를 가리킨다
> (언어 컬럼 `LANGUAGE`). `ADMIN_COMMON_CODE`는 **DROP됐다** - 이 이름이 보이면 옛 코드다.
> 현행 내용은 admin-common-code-asis-sync-2026-10-07(보관: docs/archive/memory-fix-log-2026-10.md)를 본다.

### 무슨 일이 있었나 (2026-10-07 하루)

admin 공통코드를 읽는 경로가 **두 갈래**였고 각자 **다른 표**를 봤다.

| 읽는 경로 | 당시 보던 표 |
|---|---|
| JPA 엔티티 `CommonCode` → `CommonCodeService.labelsOf()` | `admin.ADMIN_COMMON_CODE` (언어컬럼 `code_language`) |
| 네이티브 SQL 10여 곳(Mnl·Qna·OffSrBbs·HonorUser…) | `admin.op_common_code` (언어컬럼 `language`) |

코드유형 71개가 양쪽에 중복이었다. 이 상태에서 **담당자용 FAQ(11405) 질문유형 탭이 '전체'
하나만 보이는 증상**이 났다 - `CMNTY_FAQ_TYPE` 11건이 `op_common_code`에만 있었는데 화면은
JPA 경로였기 때문이다.

처음엔 "두 표가 둘 다 실재하니 화면이 보는 쪽에 데이터를 맞추자"로 좁게 패치했다
(`ADMIN_COMMON_CODE`에도 11건 복제). **그 판단은 뒤집혔다.** 같은 날 병행 조사에서
**AS-IS 소스 41개 매퍼 전체에 `ADMIN_COMMON_CODE`가 0건**임이 확인됐다 - AS-IS의 공통코드
표는 `OP_COMMON_CODE` 하나뿐이고, `ADMIN_COMMON_CODE`는 TO-BE가 지어낸 이름이었다.
사용자 확인 후 **단일표(`op_common_code`)로 통합하고 `ADMIN_COMMON_CODE`는 DROP**했다.

좁은 패치용으로 만들었던 `migration-admin-cmnty-faq-type-codes-admin-table.sql`은
**사용자 결정으로 삭제했다**(대상 표가 없어져 재실행하면 `relation does not exist`).
FAQ 11405는 `CMNTY_FAQ_TYPE`이 원래부터 `op_common_code`에 있었으므로 통합만으로 해결됐고,
`migration-admin-cmnty-faq-type-codes.sql`(원본, `op_common_code` 대상)은 지금도 유효하다.

### 배울 점 - 이게 이 메모를 남겨 두는 이유

1. **"표가 둘 다 실재한다"는 실측이 "둘 다 맞다"는 뜻은 아니다.** 나는 TO-BE 양쪽 표를
   조회해 중복을 확인하는 데서 멈췄고, **AS-IS 소스에 그 표 이름이 있는지**를 묻지 않았다.
   그 한 번의 grep이 결론을 뒤집었다. [[asis-porting-principles]]대로 정본은 AS-IS다.
2. **TO-BE에만 있는 이름은 의심 대상이다.** `ADMIN_COMMON_CODE`·`CODE_LANGUAGE` 모두
   TO-BE 발명이었다. [[asis-porting-principles]]
3. 같은 날 **다른 세션이 정반대 결론을 병행으로 내고 있었다.** 구조적 결함을 발견하면
   좁게 때우기 전에 사용자에게 범위와 선택지를 보고할 것 - [[asis-porting-principles]]

## policy-content-db-migration-deferred

> [2026-10-07 완료] 약관 원문 DB 이관 - OP_POLICY AS-IS 실데이터 22건 적재 완료, 보류 해소. 아래는 보류 당시 기록


**★[2026-10-07 완료] 보류 해소됨.** 사용자가 CUBRID 개발DB에서 export한 `OP_POLICY` 22건을
`admin.op_policy`에 그대로 적재했다(admin-policy-asis-data-loaded(보관: docs/archive/memory-fix-log-2026-10.md)). 타입별 전시중(Y) 행
개수: 0(약관)=1, 1(개인정보처리방침)=1(policy_id=2000125, title에 "테스트"라고 적혀 있음 -
개발DB 데이터라 그대로임, 지어낸 게 아니라 원본 그대로), 5(저작권정책)=1 - 아래 ③에서
걱정했던 "타입 라벨 빈칸" 문제는 실제로 존재하지만(TYPE 6도 getPolicyTypeLabel에 분기 없음)
AS-IS 자체의 동작이라 그대로 둔다. TYPE 2·3(특정상거래법·마케팅이용약관)은 AS-IS에도 행이
아예 없어 폴백(아래 ①)이 그대로 쓰인다 - 결함이 아니라 데이터가 그렇다. `policy_id=1000`
플레이스홀더 행은 이미 없었다(아래 ①·②는 완료 처리로 더 볼 필요 없음).

---
아래는 보류 당시(2026-09-09) 기록:

개인정보처리방침/저작권정책/이용약관 콘텐츠 관리 구조를 2026-09-09에 admin 기준으로 연결했고, **원문 DB 이관은 운영데이터를 받은 뒤로 미뤘다.**

**현재 상태 (코드 연결은 끝남)**
- 원본은 admin 서비스 약관관리(`/policy` CRUD, `admin.op_policy`). admin에 `GET /api/policies/{clause|protect|copyright|marketing|trader-raw}` 조회 API를 열었다(as-is `ApiPolicyController`와 같은 이름 체계, 인증 게이트 대상 아님).
- donation이 `PolicyClient` → `PolicyContentService`로 그걸 읽고, Thymeleaf 화면(`/policy/*`)과 SPA용 JSON API(`/api/policy/{privacy|copyright|auth}`)가 같은 서비스를 공유한다.
- **폴백**: 전시중(`exhibition_status='Y'`) 본문이 없거나 admin이 죽으면 `donation/src/main/resources/policy-content/{privacy,clause,copyright}.html`(총 1.6MB)을 읽는다. 지금은 이 폴백으로 화면이 뜬다.
- `admin.op_policy`의 시드 placeholder 행(policy_id=1000, 이용약관, content 9자)은 219KB 원문을 덮어써서 `exhibition_status='N'`으로 내려두었다. 되돌리려면 `update admin.op_policy set exhibition_status='Y' where policy_id=1000;`
- 조회 규칙은 as-is `policy-mapper.xml getCurrentPolicyByType`과 동일: 타입 일치 + `EXHIBITION_STATUS='Y'` + `CREATED_DATE DESC LIMIT 1`. 전시 시작/종료일은 as-is도 조건에 쓰지 않는다.

**Why:** as-is는 이 셋을 `OP_POLICY` + opmanager 약관관리로 운영했고 개인정보처리방침은 연 2~4회 개정된다(as-is에 `privacy-form20230101`~`20260604` 이력 존재). 서비스 리소스 파일에 박혀 있으면 운영자가 재배포 없이 개정할 수 없어 연결이 필요했다. 반면 원문 적재는 곧 받을 운영데이터로 하는 게 맞아서(임시 원문을 넣고 다시 덮는 낭비를 피함) 이관만 미뤘다.

**How to apply:** 운영데이터를 받아 `admin.op_policy`에 적재한 뒤 재검토할 것 — ①적재 후 `PolicyContentService`의 폴백과 `policy-content/*.html` 1.6MB 삭제 가능, ②admin 약관관리 폼이 1.3MB 위지윅 본문을 받을 수 있는지 확인 필요, ③`POLICY_TYPE` 공통코드가 없어(`admin.op_code` 테이블 자체가 없다) 약관관리 화면의 타입 라벨이 빈 값으로 보일 수 있다, ④회원가입 화면 약관 요약 박스는 as-is가 `getPolicyInfo()`로 DB에서 받아왔는데 to-be는 member `templates/signup.html`에 하드코딩돼 있어 같이 정리 대상이다.

## storefront-survey-dto-fixed

> storefront 설문참여 화면이 보내던 키가 API와 안 맞아 응답이 빈 값으로 저장되던 버그 - 2026-10-03 수정 완료


**해결됨(2026-10-03).** [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-system-area-port-progress)의 설문관리(7207)에서 데이터모델을 AS-IS(`G_QESTNAR` 계열)로 교체했는데 storefront(Vue3 SPA) `src/views/SurveyView.vue`가 따라오지 않아 **응답이 빈 값으로 저장되고 있었다**:

- SPA가 보내는 키는 `rspnsCn`인데 API(AS-IS `QustnrRspnsResult`)가 받는 키는 `qustnrIemSn`/`respondAnswerCn`이라 **서버에서 조용히 버려졌다**(에러도 안 났다 - 행은 `qustnr_iem_sn=0, respond_answer_cn=null`로 쌓인다). 에러보다 나쁜 데이터 손실이었다.
- 모든 문항을 textarea로 그려서 **객관식 선택지가 아예 안 보였다**.

수정 내용:
- 문항 종류(`qestnTyCode`)대로 `rtype`=객관식(선택지 라디오) / `stype`=주관식(textarea). `answer_choise_co`는 AS-IS 관리자 폼도 채우지 않아 사실상 단일선택이라 라디오가 맞다.
- payload를 AS-IS 필드명(`qustnrQesitmSn`/`qustnrIemSn`/`respondAnswerCn`)으로 바꾸고, **답하지 않은 문항은 보내지 않는다**(빈 행 방지). 전부 비면 "응답을 입력해 주세요."
- 연계질문(`parentSn`)은 부모 아래 들여써 보여준다. **AS-IS가 부모 답에 따라 조건부로 펼치는지는 확인 못 했다** - 그 프론트(`qustnr/detail_srvy.html`, 별도 Node 앱 localhost:3000)가 AS-IS 저장소에 없다. 지금은 항상 노출.
- `ETC_ANSWER_CN`(기타 답변)은 AS-IS DTO·컬럼에 있지만 입력 UI 근거를 못 찾아 두지 않았다(null 기록).

**★같이 잡은 서버 버그 - 네이티브 쿼리 리터럴은 `(String)` 캐스팅하면 터진다**: 재기동 후 `/api/surveys/active`가 **500**이었다 - `QestnarRepository.toQestnarRow`에서 `ClassCastException: Character cannot be cast to String`. 원인은 `CASE WHEN ... THEN 'Y' ELSE 'N' END as is_show`처럼 **SQL 리터럴에서 나온 값**을 PostgreSQL이 `bpchar`로 정하고 Hibernate가 길이 1이면 `Character`로 돌려주기 때문. 표가 0행인 동안은 드러나지 않았다. → `asString(Object)`(toString) 헬퍼로 받도록 고쳤고 `BatchLogRepository`(결과 라벨 CASE)도 같이 손봤다. **네이티브 쿼리에서 계산된 문자 컬럼은 앞으로도 `(String) r[n]` 금지.** 전 서비스 grep 결과 단일문자 CASE 리터럴은 이 두 곳뿐이었다.

검증: `npm run build` 통과. 재기동 후 `/api/surveys/active` **200** + DTO 모양 확인(choices 채워짐, 문항4의 parentSn=3). 표가 0행이어서 화면 확인이 불가했으므로 **검증용 설문 시드**를 넣었다 - `database/ddl/seed-admin-survey-sample.sql`(QUSTNR_SN=9001, 객관식 4선택지 + 주관식 + 객관식 3선택지 + 그 연계질문, 노출기간은 어제~한달뒤로 **오늘을 포함**해야 이용자 화면에 보인다, SRVY_TRGT='U'라야 `/api/surveys/active`에 걸린다). `qustnr_iem_sn`은 **문항 안에서만 유일**하다(문항1은 1~4, 문항2는 1) - 라디오 name을 문항별로 묶어야 한다.

## gift-seller-portal-unauthenticated

> gift 판매자 화면의 sellerId 무인증 신뢰 문제 - 2026-09-10 해소 완료 (로그인 기반 + 서비스 계층 소유권 검사)


**2026-09-10 해소됨.** 이전 상태와 무엇을 고쳤는지 기록.

**있었던 문제:** gift의 제공자 화면이 `sellerId`를 쿼리파라미터·hidden input·화면 입력칸("제공자 ID")으로 받아 그대로 신뢰했다. 값만 바꾸면 **남의 답례품을 등록·수정·판매중지·폐지·재고조정**하고 **남의 문의에 답변**할 수 있었다. `GiftService.stop(itemId)`와 `adjustStock(itemId, qty)`, `InquiryService.answer(inquiryId, answer)`는 소유권 검사가 **아예 없었다** - 컨트롤러를 우회하면 그대로 통했다.

**고친 방식:**
- `GiftController.currentSellerId(request)` - GH_AUTH JWT의 userId → `OP_SELLER.MEMBER_USER_ID` → sellerId. **서버가 신원을 결정**하고 요청의 주장은 무시한다. `SellerPortalController`(`/seller/dashboard`)가 이미 쓰던 방식을 그대로 가져왔다.
- 엔드포인트 9개 전환: `GET/POST /register`, `GET/POST /gifts/{id}/edit`, `POST /gifts/{id}/discontinue`, `/stop`, `/stock`, `GET /my`, `GET /my/inquiries`, `POST /inquiries/{id}/answer`.
- `sellerGate()` - 로그인 없으면 member 로그인으로, 로그인은 했으나 연결된 판매자가 없으면 안내 화면(구분하지 않으면 로그인 무한루프).
- **서비스 계층에도 소유권 검사 신설**: `GiftService.stop(itemId, sellerId)`, `adjustStock(itemId, sellerId, qty)`, `InquiryService.answer(inquiryId, sellerId, answer)`. 운영자 대리답변은 `answerAsOperator()`로 분리(운영관리 경로는 내부 시크릿으로 이미 보호).
- 화면에서 신원 주장 제거: `register.html`의 "제공자 ID" 입력칸, `my.html`·`inquiries.html`·`edit.html`의 sellerId hidden/링크 파라미터 삭제.

**검증(실측):** 비로그인 `/my` → 로그인 리다이렉트 / 판매자 아닌 회원 → 안내 화면 / **판매자 아닌 회원이 `sellerId=9001`을 실어 재고 9999 조작 시도 → 차단(재고 46 유지)** / **판매자 9001이 판매자 9002 답례품에 재고·판매중지·폐지 시도 → 3건 모두 차단(재고 30·APPROVED 유지)** / 본인 답례품 정상 조정 성공.

**How to apply:** 판매자 화면을 새로 추가할 때는 반드시 `currentSellerId(request)`로 sellerId를 얻고, 서비스 메서드에도 `sellerId`를 넘겨 소유권을 검사한다. **`@RequestParam Long sellerId`를 다시 만들지 않는다.** 판매자↔회원 연결(`OP_SELLER.MEMBER_USER_ID`)을 admin 화면에서 맺어주는 UI는 아직 없다(DB 시딩만) - [[deferred-features]]와 함께 볼 것.

## pending-verify-dormancy-receipt

> 2026-09-17 구현한 휴면해제·영수증출력 변경은 donation·member 재기동 후 2026-09-18(내일) 5173에서 수동검증 대기 - 컴파일/빌드는 통과


**2026-09-17 저녁 시점: 코드는 완료(compileJava·vite build 통과)했으나 런타임 미검증.** 사용자가 집이라 재기동 불가, 2026-09-18(다음 근무일)에 회사에서 확인 예정. 검증 전까지 이 변경들이 실동작 확인됐다고 단정하지 말 것.

**재기동 필요:** donation(8082)·member(8081). storefront(5173)는 dev면 자동 반영.

**재기동 필요 서비스 추가**: gift(8084)도 — E 수정으로 GiftPublicApi detail DTO·GiftController 변경됨.

**검증 항목:**
1. **영수증 출력**(donation) - 기부내역(MyDonationsView) → 완료건 "영수증출력" → PC는 팝업으로 영수증+PDF, 없는/미완료 건번호는 `alert("영수증 정보가 없습니다.")` 후 창 닫힘, 모바일은 `alert("영수증 출력은 PC에서 확인 부탁드립니다.")`. 신규 엔드포인트 `GET /api/my/receipts/official/{cntrSn}` 때문에 **재기동 전엔 404가 정상**(사용자가 이미 404 목격).
2. **휴면해제**(member) - 휴면(상태4) 계정으로 로그인 → 비번 본인확인 후 `SLEEP_USER` → 로그인 화면에서 `confirm("휴면해제 하시겠습니까?")` → 예 → `/api/auth/recovery`로 해제 → "다시 로그인" 안내. 링크·재입력 폼 없음(AS-IS op.saleson.js:1169 동일).
3. **[AS-IS 충실도 감사 수정분, 2026-09-17]** — 상세는 `docs/as-is-fidelity-audit-2026-09-17.md`:
   - **A1 비번만료/임시비번**(member): passwordType 'T' 회원 로그인 → "임시 비밀번호 사용자 입니다." → find-idpw. 만료일 지난 회원 로그인 → 변경 모달("변경하고 로그인"/"나중에 변경"). (테스트 데이터: OP_USER의 PASSWORD_TYPE='T' 또는 PASSWORD_EXPIRED_DATE 과거일자 세팅 필요)
   - **D1/D2 완료알림**(order/gift): 주문취소·구매확정·반품/교환·문의등록·장바구니삭제 시 완료 문구 뜨는지.
   - **E 재입고·좋아요**(gift): 품절 답례품 상세 "재입고 알림" 버튼, 상품평 "좋아요 N" 버튼.
   - **A3 세션만료 401**(storefront 전용, 재기동 불필요): 로그인 후 쿠키(GH_AUTH) 만료/삭제 상태에서 보호 API 호출(예: 마이페이지 액션) → "로그인 후 이용이 가능합니다." alert + `/login?target=` 이동되는지.
   - **G 체크아웃 검증**(storefront 전용): 체크아웃에서 받는분/연락처/우편번호/배송지/상세주소 비운 채 결제 → 각 필드 alert로 막히는지, 포인트 부족 시 "…포인트가 부족합니다." 뜨는지.

관련: [[thymeleaf-duplicate-cleanup-deferred]](2차 진행분), [[asis-porting-principles]](AS-IS 흐름추적 원칙). 검증 끝나면 이 메모리 삭제.

## static-asset-scoping-2026-09-18

> 2026-09-18 SPA 정적에셋 꼬임 해결 - CSS 전역누수 라우트별 스코핑 + 누락이미지 144개 복사, storefront 전용(재기동 불필요) 시각확인 대기


**2026-09-18 착수.** "Vue3 전환 후 정적요소(css/js/이미지)가 다 꼬였다"는 관찰의 원인 규명·수정. 상세 = `docs/static-asset-audit-2026-09-18.md`.

**핵심 원인 2가지 + 수정:**
1. **CSS 전역 누수** — SPA가 화면 전용 CSS 30개를 `index.html`에 전역 로드 → 화면 간 bleed. AS-IS(MPA)는 페이지별로만 로드함. → `index.html`을 공통 base 9개만 남기고, 화면 전용 CSS는 `storefront/src/router/pageStyles.js`의 라우트→CSS 매핑으로 `beforeEach`에서 주입/회수. 매핑은 AS-IS 각 페이지 `<head>` 링크 목록이 정본.
2. **이미지 404 144개** — AS-IS `static/images` 일부만 `public`에 복사돼 아이콘·별점·체크박스 등 깨짐. AS-IS에서 참조된-누락 144개 복사 → 404가 158→14로 감소. 남은 14개(npay 스프라이트·cclogo·ico_depth1~3 등)는 **AS-IS 전체에도 없음 = 운영도 동일**, 무해.

**JS는 대부분 정상 폐기**(jQuery/Vue2/op.saleson→Vue3+fetch, swiper는 Vue 재구현). counterup/waypoints 애니메이션만 미재현(값은 정적표기, 기능결함 아님).

**홈 충실 재이식(2026-09-18)**: 슬라이더는 npm `swiper` 설치 후 AS-IS main.html `initSwiper` 설정 그대로(slidesPerView1/spaceBetween16/speed800/autoplay6s, **pagination type:'custom'+renderCustom** ← fraction이면 `.swiper-pagination-custom` 스타일 안 먹음, 주입 `.swiper-navigation-icon`은 :deep로 숨김-AS-IS는 `::after` mask-image 화살표). 검색바는 AS-IS `.search-bar.form-conts.btn-ico-wrap`+btn-group 마크업. **`site.css`가 옛 손수 마크업에 맞춘 오버라이드를 갖고 있어 AS-IS 마크업 복원 시 충돌**(검색 border 제거·버튼 절대배치 → 삭제함). AS-IS 마크업 복원할 때마다 site.css 오버라이드 동반 점검 필요. GNB는 라우트 변경 시 자동 닫힘(watch route).

**폰트는 이미 AS-IS와 동일**(`.btn-navi`=`--pc-font-small`=14px/400/Pretendard GOV, 토큰체인 new.css→@import output.css→@import krds_tokens.css 정상). 사용자가 본 `<button>`·폰트차이는 **캐시**였음. 미세 폰트/간격 차이는 **UI/UX 개선 단계로 보류**(사용자 결정 2026-09-18).

**에러페이지 AS-IS화(2026-09-18)**: (1) storefront에 `ErrorView.vue`(AS-IS error/404.html 그대로)+catch-all 라우트(meta.bare)+`error404.png`. 배너 클릭은 같은 오리진이면 `router.push`로 SPA 태워 catch-all→ErrorView. (2) **Whitelabel 근본**: 배너 linkUrl이 `http://localhost:8082/`(donation 백엔드)처럼 절대 URL이라 백엔드 도달→Spring Boot Whitelabel. → 대민 5개 서비스에 `src/main/resources/static/error/404.html`(AS-IS 404, base64 이미지 인라인) 배치, Boot가 404시 자동 서빙(보안필터 없음). **반영엔 서비스 재기동 필요.** 배너 데이터(잘못된 백엔드 절대URL)는 admin에서 정정 권고.

**결함 배치감사(2026-09-18)**: `docs/spa-static-defect-audit-2026-09-18.md`. 근본원인 = AS-IS CSS/JS가 jQuery/부트스트랩 런타임 의존(페이지별 CSS 로딩 + `.active`/`.show` 클래스 토글)인데 Vue3 전환이 화면마다 제각각 재현 → 잠복버그. **탭 버그 근본**: 부트스트랩 `.tab-content>.tab-pane{display:none}`(전역)을 `v-show`로는 못 이김 → 활성 패널에 `:class="{active}"` 필요(FindIdPwView·GiftDetailView 수정). **CSS 매핑 오류 일괄 교정**: orders/order-detail/claims(order_ali→mypage-order(-details)+order-modal), list-select(donation_liemt/selmt), gift-community/seasonal(evt_card), events/event-detail, honor-guide(+donation_doak), find-idpw(login-idse). 누락 CSS 3개(order-modal/mypage-order/mypage-order-details) 복사.

**header_g(답례품/장바구니 전용 헤더) 충실 재이식 완료(2026-09-18)**: `AppHeaderShopping.vue`(전체카테고리 메가메뉴 via `GET /gift/api/categories` + 제철/마을기업 링크 + 답례품 검색) + `MapSelectPopup.vue`(지자체 지도선택 - vtmap 이미지+17시도, 시군구는 `GET /donation/api/locgovs`를 upperLocgovCode로 그룹핑, 선택 시 `/gifts?locgovCode=`). `App.vue`가 쇼핑 라우트(gift-*/cart/events)에서만 AppHeader 아래 마운트(AS-IS는 header_ali+header_g 스택). 스타일은 전역 base(default_ali/layout/new)에 있음.

**header_g 메가메뉴 마무리(2026-09-18)**: 카테고리 응답 필드는 `name`/`children`(내가 처음 label/items로 오바인딩→수정). 그리고 `layout.css`의 `.main_lnb{display:none}`(전역)을 `v-show`로 못 이겨 안 열리던 것 → **인라인 `:style="{display:isOpen?'block':'none'}"`로 강제**(탭과 동일 근본버그: CSS display:none vs v-show/클래스토글). 검색창 흰배경 → AS-IS `input{background:transparent;color:#fff}` 스코프 스타일 추가. **미확인(개발데이터 대기)**: 소고기 등 서브카테고리 `children`이 테스트DB엔 `[]`라 품목(한우) 드릴다운 확인 불가 → 실개발데이터 받으면 확인. MSA 답례품 필터는 대분류·키워드만 지원(서브카테고리 코드 필터 없음, 데이터 후 검토). **미이식**: 제철식품관/마을기업관은 AS-IS가 2단계(키워드카드→상품)인데 MSA GiftListView는 상품그리드 단일 → 별도 재이식 대기.

**제철식품관/마을기업관 이식(2026-09-18)**: AS-IS 제철식품관은 2단계(월별 제철키워드 카드→그 달 상품). 백엔드 추가: `SeasonFood` 엔티티/`SeasonFoodId`/`SeasonFoodRepository`(G_SEASON_FOOD), `GiftService.seasonFoodKeywords()`(1~12월 키워드)·`seasonalGifts(month)`, `GET /api/season-food`+`/api/gifts?mode=seasonal&month=N`. 프론트: `SeasonalView.vue`(season_group 카드→goods-list-group, 스타일은 event.css의 .season_group), 라우트 gift-seasonal→SeasonalView. 마을기업관은 AS-IS도 단일 상품그리드라 GiftListView 그대로(데이터 0건이라 달라보였음). **데이터 생성(docker exec ghlove-postgres psql, gift DB)**: g_season_food 12개월 키워드, g_season_food_item 39건(월별 상품분산), op_seller 9007/9008/9012/9013 community_business_yn='Y'. 상품 이미지는 OP_ITEM_IMAGE에 대부분 존재. **gift(8084) 재기동해야 제철 API 반영**(마을기업은 데이터만이라 즉시 작동, 검증 4건). 개발데이터 오면 g_season_food/g_season_food_item 실데이터로 교체.

**사이트맵 닫힘(2026-09-18)**: `:target`(#modal_sitemap) 방식은 router 이동/replaceState로 브라우저가 :target을 갱신 안 해 안 닫힘 → **공유 상태 `composables/useSitemap.js`(sitemapOpen)로 전환**. AppHeader 전체메뉴버튼·AppFooter 링크가 openSitemap, 모달 `:class="{'is-open':sitemapOpen}"`+닫기버튼+nav클릭 closeSitemap, site.css `#modal_sitemap.is-open`.

**제철/마을기업 지역필터(2026-09-18)**: 백엔드 `seasonalGifts(month, locgovCode)`·`communityBusinessGifts(locgovCode)`에 locgov 필터 추가(matchesLocgov: 시도코드 끝000이면 앞2자리, 시군구면 정확매칭). 프론트 공용 `RegionSelect.vue`(시도 17개+시군구, /donation/api/locgovs 그룹핑, change로 코드 emit). SeasonalView에 `.loc_select_area`(월+RegionSelect+검색/초기화) 추가, Stage2는 "전체 N건"만(운영대로, 돌아가기 버튼 제거). 신규 `CommunityView.vue`(마을기업관 헤더+RegionSelect+상품그리드, gift-community-business 라우트 연결). **gift 재기동 필요**(locgov 필터·season API).

**공용 모달 + alert/confirm 전면 전환(2026-09-18)**: AS-IS op-modal($s.alert/$s.confirm) 대체 = `composables/useModal.js`(modalAlert/modalConfirm, 상태) + `components/GlobalModal.vue`(App.vue 상시 마운트). **모달은 운영과 동일한 순수 bootstrap 구조**(`.modal.show`+`.modal-backdrop.show`+`.modal-dialog-centered`+`.modal-content`; 커스텀 오버레이 CSS 금지 - 그게 크기·클래스 어긋남 원인이었음). `.modal-content`는 MSA bootstrap.css가 운영과 바이트동일(padding:10%/border-radius:20px). **앱 전체 `window.alert/confirm`·bare alert/confirm 85건을 25개 파일에서 modalAlert/`await modalConfirm`으로 전환**(confirm 함수는 async 필요). DonateView: 한글금액("기부금은 X원")=koreanAmount, 한도 축약("2천만")=koreanAmountShort(utils/koreanNumber.js), 적립예상 0원에도 표시. 주의: modalAlert는 non-blocking(window.alert와 달리 await 안하면 즉시 다음 줄 실행) - 성공후 이동/리로드 케이스는 필요시 await.

**상태**: `npm run build` clean. storefront 변경은 재기동 불필요(새로고침), 백엔드(gift 제철 API·static 에러페이지)는 **재기동 필요**. **몰아서 시각확인 대기**(사용자 요청): (1) 결함 배치수정 화면들 레이아웃 (2) 탭 전환(find-idpw·답례품상세) (3) 쇼핑 전용 헤더+메가메뉴+지도선택. 메가메뉴는 gift(8084), 지도선택은 donation(8082) 실행 필요.

관련: [[asis-porting-principles]](CSS 링크목록까지 AS-IS 대조 원칙), [[asis-porting-principles]].

## member-signup-terms-box-asis-content

> 회원가입 화면 약관 요약 박스를 AS-IS agree.vue와 동일하게 DB 연동+비동의 토글까지 구현(2026-10-07) - 개인정보 수집·이용 동의는 타입1이 아니라 타입6


storefront `SignupView.vue`의 "약관 요약 박스" 2개(이용약관/개인정보 수집·이용 동의)가 하드코딩된
플레이스홀더 텍스트였던 걸 AS-IS `agree.vue`(`getPolicyInfo()`)와 동일하게 admin
약관관리(OP_POLICY) 실데이터를 읽도록 바꿨다.

**★핵심 발견**: "개인정보 수집·이용 동의" 체크박스는 POLICY_TYPE **1**(개인정보처리방침)이
아니라 **6**("개인정보 수집·이용 동의")을 쓴다 - AS-IS `agree.vue`의 `collectionAgree`가
그 증거(`PolicyInfo.POLICY_TYPE_COLLECTION_POLICY` 상수). 타입1은 별도의 전체 개인정보처리방침
문서(`/policy/privacy`)이고, 가입 화면 안의 요약 동의 박스는 그것과 다른, 더 짧은 타입6
콘텐츠다. 체크박스 라벨 문구도 AS-IS는 하드코딩이 아니라 DB `title`을 그대로 쓴다(TO-BE도
동일하게 바꿈 - `title` 없으면 기존 하드코딩 문구로 폴백).

**"알림서비스 수신 동의" 박스**는 AS-IS도 DB가 아니라 `agree.vue`에 **하드코딩된 고정 HTML**이다
(국민비서/Email/SMS/알림톡 체크박스 4개는 TO-BE에 이미 있었음 - 그 설명 본문만 없었다). AS-IS
원문을 그대로 옮겨 넣었다(`/users/modify.html` 링크만 TO-BE 경로 `/mypage/profile`로 교체 -
AS-IS에 없는 페이지로 보내는 것 자체가 더 큰 결함이라서).

**★[2026-10-07 추가] "비동의" 토글**: AS-IS는 필수 항목 2개(이용약관/개인정보수집이용) 체크박스
옆에 "비동의" 체크박스가 나란히 있고, 서로소 토글이다 - `selected()`가 동의 체크 시 비동의를
지우고, `selectDisagree()`가 비동의 체크 시 동의를 지운다(각자 `checkResult`/`disagreeResult`
배열에서 상대 값을 제거하는 방식, AS-IS 소스 확인). "전체약관 동의하기"를 누르면(켜든 끄든)
비동의 둘 다 비운다(`allCheck()`). **`disagreeResult`는 가입 API로 전송되지 않는 순수 화면
상호작용**이다(`UserDomainInfo`에 해당 필드 없음, 프론트 전역 grep으로 사용처 0건 확인) - 그래서
백엔드 변경 없이 프론트만으로 구현했다. TO-BE는 배열 대신 불리언 쌍(`agreeTerms`/`disagreeTerms`,
`agreePrivacy`/`disagreePrivacy`)으로 같은 상호배타 동작을 직접 구현(결과 동일).

**빠진 것(의도적, AS-IS도 agree.vue가 실제로 안 씀)**: `otherAgree`(타입4, "개인정보제3자동의")는
AS-IS `JoinController.getPolicyInfo`가 조회는 하지만 `agree.vue` 템플릿이 전혀 렌더링하지
않는 죽은 응답 필드다 - TO-BE도 안 받는다.

**구현**: admin `PolicyApiController.TYPE_BY_NAME`에 `"collection"→"6"` 추가(AS-IS에 URL
이름이 없던 타입이라 `PolicyInfo` 상수명을 따서 새로 붙임) → member에 `PolicyClient`
(donation과 동일 패턴) + `GET /api/signup-policies`(`SignupPolicyApiController`) 추가 →
storefront가 그걸 불러 두 박스에 `v-html`로 채우고, 동의/비동의 상호배타 핸들러 4개
(`onAgreeTermsChange`/`onDisagreeTermsChange`/`onAgreePrivacyChange`/`onDisagreePrivacyChange`)
추가.

**함정**: 새 파일 2개(`PolicyClient.java`/`SignupPolicyApiController.java`) 추가 직후
`./gradlew compileJava`가 **UP-TO-DATE로 스킵**되는 걸 겪었다(Windows 타임스탬프 해상도
문제로 보임) - `--rerun-tasks`로 강제 재컴파일한 뒤에야 jar에 반영됐다. 새 파일 추가 직후
빌드 크기/타임스탬프가 안 바뀌면 의심할 것.

**상태**: admin·member 둘 다 컴파일·테스트·bootJar 통과, jar 안에 새 클래스 포함 확인.
storefront는 Vite라 재기동 불필요(저장 즉시 반영). **재기동 필요: admin, member.**

## storefront-address-widget-mismatch-deferred

> 회원가입·마이페이지 개인정보수정 화면의 주소검색을 AS-IS(행안부 juso.go.kr)와 맞춰 Daum Postcode에서 전환완료(2026-10-08) - 처음엔 외부연계 필요하다고 보류했다가 admin의 기존 juso-popup이 이미 멀쩡히 동작하는 걸 사용자가 확인시켜 같은 날 번복/구현


**★2026-10-08 같은 날 번복: 보류 → 구현 완료.** 처음엔 "행안부 API 도메인별 승인키가 새로
필요할 수 있다"고 보고 보류를 결정했는데, 사용자가 지자체관리(4401) 등록화면에서 **admin의
기존 `/admin/juso-popup`이 지금 이 환경에서 이미 정상 동작**하는 걸 확인시켜줬다 - 즉 새로운
외부연계가 전혀 필요 없었다(행안부 승인키는 "운영 도메인 등록"이 아니라 단순 사용량 추적용
키라 로컬/개발 환경에서도 그대로 작동한다). 아래 "사용자 결정(보류)" 단락은 **그 판단이
틀렸던 경과 기록**으로 남겨두고, 실제 조치는 그 아래 "구현 완료" 단락을 본다.

**구현 완료**: `storefront/public/juso-popup.html`(신규, admin의 `juso-popup.html`을 토대로
마크업/스크립트 이식, confmKey만 이 화면 전용 AS-IS 값 `devU01TX0FVVEgyMDIyMTAwNjE2MzkwNzExMzAzMTg=`
유지 - admin 키와 다르다, 화면마다 다른 키를 쓰는 AS-IS 그대로) + 정적자산 3종(`content/modules/juso/
addrlink.js`, `content/opmanager/css/juso/addrlink.css`+이미지, `content/modules/jquery/
jquery-1.11.0.min.js` - 전부 admin에 이미 포팅된 파일을 그대로 복사). `SignupView.vue`·
`ProfileView.vue`의 `searchAddress()`를 Daum Postcode 호출에서 `window.open('/juso-popup.html', ...)`
+ `window.jusoCallBack = (addrPart1, addrPart2, addrDetail, zipNo) => {...}`(같은 오리진이라
admin처럼 opener 직접호출 방식, postMessage 불필요)로 교체, 저장값은 `addrPart1+addrPart2`
(AS-IS 원본 `vm.param.address = response.data.roadAddrPart1 + response.data.roadAddrPart2`와
동일). `loadDaumPostcode` import는 두 파일에서 제거(DeliveryFormView.vue·CheckoutView.vue는
그대로 Daum 유지 - 이 둘은 원래도 AS-IS와 일치했다). storefront는 Vite dev 서버라 재기동 없이
즉시 반영됨(`public/`은 그대로 서빙, `.vue`는 HMR) - `npm run build`로 빌드 에러만 확인했다.

**발견 경위**: admin 일반회원관리(4101) 상세화면에서 주소 표시형식을 AS-IS와 맞추다가, 실데이터의
시/도명이 축약형("경기"/"서울")인 걸 발견해 회원가입 때 주소를 어떻게 입력받는지 역추적했다.

**AS-IS는 화면마다 주소검색 위젯이 다르다(통일돼 있지 않다)**:
- **회원가입**(`ghlove-frontend/users/join.html`) · **마이페이지 개인정보수정**(`users/modify.html`) ·
  원패스가입(`onepass-join.html`) → **행정안전부 juso.go.kr 팝업**(`users/jusoPopup.html`,
  `business.juso.go.kr/addrlink/addrLinkApiJsonp.do` JSONP 호출). 최종 저장값은
  `roadAddrPart1 + roadAddrPart2`(행안부 API가 항상 전체 공식 시/도명 "서울특별시 강남구 ..."을
  담아 돌려준다 - 지번·법정동 보조정보는 `roadAddrPart2`에 괄호로 붙는다).
- **마이페이지 배송지관리**(`mypage/deliveryInfo.html`) · **주문결제 1단계**(`order/step1.html`) →
  **Daum Postcode**(`components/ui/daum/address.vue`의 `openDaumAddress()`), 저장값은
  `response.roadAddress`(Daum도 보통 전체명을 준다).

**TO-BE 현재 상태(2026-10-08 실측)**:
- `storefront/src/views/SignupView.vue`, `storefront/src/views/mypage/ProfileView.vue` → 둘 다
  `daum.Postcode`를 쓴다(`loadDaumPostcode()` + `data.roadAddress || data.jibunAddress`). **AS-IS와
  다른 위젯**이다. 코드 주석에 "AS-IS 회원가입은 juso.go.kr 팝업이지만 이 프로젝트는 이미
  daum.Postcode로 통일해 다른 화면과 동작을 맞춘다"고 적혀 있었는데, **AS-IS를 확인하지 않고
  임의로 "통일"한 과거 결정**이었다([[asis-porting-principles]] 위반 사례와 같은 유형).
- `storefront/src/views/mypage/DeliveryFormView.vue`, `storefront/src/views/order/CheckoutView.vue` →
  둘 다 `data.roadAddress || data.jibunAddress`로 이미 Daum을 쓴다. **이 둘은 AS-IS와 일치** -
  손댈 필요 없다.

**정리하면 회원가입·개인정보수정 두 화면만 위젯이 틀렸고, 배송지관리·주문결제는 이미 맞다.**

**"서울" vs "서울특별시" 표시차이의 진짜 원인일 가능성**: 행안부 juso.go.kr은 축약형을 주지 않는데
지금 두 화면은 그 API 자체를 안 쓰니, 이게 근본원인일 수 있다. 다만 현재 address input이
`readonly`라 위젯 없이는 입력 자체가 안 되므로, 실제 테스트데이터(`testuser01` 등)가 정말 위젯을
거쳐 들어간 값인지는 별도 확인 필요(과거 코드 상태에서 readonly 아니었을 때 수동입력했을 가능성도
있음 - 미확인).

**참고할 기존 자산**: admin 서비스에 이미 같은 행안부 API로 juso-popup을 포팅해 둔 선례가 있다
(`/admin/juso-popup`, `admin/src/main/resources/templates/juso/juso-popup.html` +
`content/modules/juso/addrlink.js` 등 정적자산, [[admin-port-progress]](원장: docs/as-is-parity.md 22절 admin-member-area-port-progress) 4401 지자체관리
작업 때 이식). storefront SPA에 같은 걸 포팅할 때 구조를 참고할 수 있다. 단, 행안부 API는
도메인별 승인키(confmKey)가 필요해서 storefront(회원 대상 운영 도메인)용 키 발급·외부연계 협의가
먼저 필요할 수 있다.

**사용자 결정(2026-10-08): 지금 당장 고치지 않는다.** "외부연계 개발이 가능해지면 그때 수정하자"
- 행안부 API 키/도메인 승인 등 외부연계가 먼저 정리돼야 착수 가능하다고 판단한 것으로 보임.
이 메모가 그 재개 시점의 체크리스트다: SignupView.vue·ProfileView.vue 두 파일만 juso.go.kr 팝업으로
교체, DeliveryFormView.vue·CheckoutView.vue는 손대지 않음.


**2026-10-15 키 교정**: storefront `public/juso-popup.html`이 AS-IS Vue initData의 개발키(devU01…, 실제로는 안 보내는 죽은 값)를 옮겨 개발키 만료(E0014 "개발승인키 기간이 만료") 후 검색이 막혔다. AS-IS가 실제로 보내는 폼 숨은 칸의 운영키(U01TX0FVVEgyMDIyMTEyNTE0…, admin과 같은 키)로 교체, API 응답 errorCode 0 확인. ★AS-IS에서 값을 옮길 때는 "실제로 전송되는 경로"의 값인지 확인할 것.

## featured-unused-skip

> featured 재검토 필요 - 2026-10-06 '미사용'으로 판단했으나 AS-IS CUBRID OP_FEATURED에 1,063건(이벤트성 데이터)이 있어 틀렸을 수 있음. 사용여부 재분석 보류 중


**★이 메모의 2026-10-06 최초 결론('featured 미사용')은 틀렸을 가능성이 크다. 재분석 보류 중.**

- 처음에 TO-BE `gift.OP_FEATURED` 테이블이 미배포(조회 시 relation 없음)인 것만 보고 "미사용"으로
  판단했다. 그러나 사용자가 **AS-IS 개발DB(CUBRID)에서 `SELECT count(*) FROM OP_FEATURED` = 1,063건**
  이고 내용이 **이벤트 관련 데이터**임을 확인해줬다. 즉 featured는 기획/전시가 아니라 **이벤트로
  실제 사용 중**일 수 있다. [[asis-sources-and-data]]
- 따라서 featured-admin 화면(현재 AS-IS와 컬럼 구성이 다르고 `small-btn` 등 발명 클래스 사용)은
  **AS-IS 충실이식 대상일 수 있다** - "미사용이니 패스"로 단정하지 말 것.
- **다음 할 일**: AS-IS `featured/*`(FeaturedController/FeaturedManagerController + featured-mapper +
  JSP)와 OP_FEATURED 실데이터를 4축으로 재분석해 실제 용도(이벤트?)·화면 구성을 확정하고,
  TO-BE를 AS-IS대로 맞춘다. **AS-IS OP_FEATURED export(1,063건)**:
  `C:\Users\ghlove008\Desktop\고향사랑e음\1.AS-IS\1. DB\OP_FEATURED_202610061741.sql` (2026-10-06 사용자 제공).
- 2026-10-06에 featured-admin 목록 기간 셀만 AS-IS 로직(`99999999`→상시게시/개방형)으로 고쳐둔 상태.
- 사용자 지시: "일단 현재 작업(1405)부터 완료하고 featured는 다시 검토하자."
