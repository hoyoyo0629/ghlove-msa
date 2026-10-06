---
name: admin-member-area-port-progress
description: "회원관리 영역 8화면 AS-IS 이식 **완료**(7화면 이식 + 4701 이식대상없음) - AS-IS JSP/컨트롤러 매핑표, 구조갭(4402·4501) 해소, 담당자 사용여부 9/2↔ACTIVE/LOCKED 변환 규칙"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-04T07:15:23.549Z
---

[[asis-screen-port-procedure]] 절차로 진행한 **두 번째 메뉴 영역 = 회원관리**(리프 8개) - **2026-10-04 완료**(7화면 이식 + 4701은 AS-IS에서도 죽은 메뉴라 이식대상 없음). 첫 영역 시스템관리는 25/25 완료([[admin-system-area-port-progress]]).

영역 목록은 실측으로 뽑았다 - `menu_type=3 AND top='회원관리'`인 리프 8개, 전부 menu_url 있음.

**AS-IS 경로 원장** (`ghlove-web/src/main/webapp/WEB-INF/views/opmanager/i18n/` + `saleson.shop.user` 컨트롤러):

| id | 메뉴 | AS-IS JSP | AS-IS 컨트롤러(엔드포인트수) | TO-BE url | 상태 |
|---|---|---|---|---|---|
| 4101 | 일반회원관리 | `user/customer/{list,details,cntr-list,point-list}.jsp` + `user/popup/{customer-password-confirm,customer-info-access,customer-secede-write,delivery}.jsp` | `GeneralCustomerManagerController` `/opmanager/user/customer` (10) | /admin/members | **완료**(아래 ★) |
| 4105 | 탈퇴회원리스트 | `user/secede-user/list.jsp` + `user/popup/secede-reason-details.jsp` | `SecedeUserManagerController` (4) | /admin/secede-users | **완료**(아래 ★) |
| 4107 | 휴면회원관리 | `user/sleep-user/list.jsp` | `SleepUserManagerController` (4) | /admin/sleep-users | **완료**(아래 ★) |
| 4401 | 지자체관리 | `user/locgov/{list,form,edit,popup}.jsp` + `user/popup/locgov-point-list.jsp` + `juso/juso-popup.jsp` | `LocgovManagerController` (14) | /admin/locgovs | **완료**(아래 ★) |
| 4402 | 지자체담당자관리 | `user/locgov-charger/{list,edit}.jsp` | `LocgovPersonInChargeManagerController` `/opmanager/user/locgov-charger` (7) | /admin/person-in-charge/**locgov** | **완료**(분리, 아래 ★) |
| 4501 | 운영관리자 | `user/oper-charger/{list,edit}.jsp` | `OperPersonInChargeManagerController` `/opmanager/user/oper-charger` (6) | /admin/person-in-charge/**oper** | **완료**(분리, 아래 ★) |
| 4601 | 오프라인담당자 | `user/off-charger/{list,form,edit}.jsp` + `user/popup/charger-password-init.jsp` | `OffPersonInChargeManagerController` (13) | /admin/off-person-in-charge | **완료**(아래 ★) |
| 4701 | 답례품 관리자 | **없음**(AS-IS url `/opmanager/user/rtnpsnt/list`인데 컨트롤러·JSP가 존재하지 않는다) | - | /seller | **이식대상 없음**(아래 ★) |

**★구조 갭 4402·4501 완전 해소(2026-10-04)** - 아래 경과 참고. 두 메뉴 모두 자기 화면을 가리킨다(`/admin/person-in-charge/locgov`, `/admin/person-in-charge/oper`). 기존 통합화면 `/admin/person-in-charge`는 **어느 메뉴도 가리키지 않는 TO-BE 자체 화면**이라 지우지 않고 남겼다(시스템관리의 `/log/levy`·액션로그 화면과 같은 처리).

**★지자체담당자관리(4402) 완료 - 2026-10-04**: 검색구분·등록일 범위·사용여부·컬럼(권한에 따라 9 또는 10)·상세수정(ajax)·시도/시군구 연동 select 복원.
- **AS-IS 권한 분기(`adminRoleType`)를 그대로 재현**: SYS(ROLE_ADMIN_1~4)는 전체 지자체 + 지자체명 검색·컬럼이 있고 **지자체명이 링크**(아이디·이름은 평문), 지자체를 시도·시군구로 바꿀 수 있다. LOC(5·6)는 **자기 지자체만** 보이고 지자체명 컬럼이 없으며 아이디·이름이 링크다. 등급까지 갈린다 - **부관리자(6)는 회원구분·사용여부 라디오 disabled**, **주관리자(5)가 자기 자신을 볼 때는 사용여부만 disabled**. LOC가 다른 지자체 담당자를 열면 목록으로 되돌린다.
- **AS-IS 비활성 상태 보존**: 삭제 버튼은 권한 조건을 통과해도 `style="display:none;"`이라 **목록·상세 모두에서 안 보인다**. 엔드포인트는 남기고 버튼만 숨긴 채 옮겼다([[as-is-parity-includes-disabled-state]]).
- 상위 지자체 목록은 AS-IS가 공통코드 **WDR**에서 가져오는데 TO-BE 공통코드엔 WDR이 없어 **지자체 목록에서 시도를 추렸다**(값 집합은 동일). 시군구는 AS-IS대로 ajax(`/locgov/{upperLocgovCode}/list`)로 채운다.
- `PersonInChargeAdminService.update`에 `locgovCode` 오버로드 추가. **주담당자 정원(2명) 검사를 "옮겨갈 지자체" 기준으로** 했다 - 다른 지자체로 이동하며 주담당자가 되는 경우 기존 지자체 기준으로 검사하면 정원을 우회할 수 있다.

**(경과) 구조 갭 4402·4501 (2026-10-04)**: 두 메뉴가 같은 menu_url(`/admin/person-in-charge`)이었다 - TO-BE가 두 화면을 `scope` 탭 하나로 **의도적으로 통합**해 뒀다(`docs/as-is-admin-gap-deep-audit-part2.md` D6/D9 권장사항). AS-IS는 **컨트롤러·JSP·검색조건·컬럼이 모두 다른 별개 화면**이라 분리 중이다(시스템관리 1404·1405와 같은 유형).
- **4501 분리 완료** → `/admin/person-in-charge/oper`(`migration-admin-menu-4501-oper-charger.sql`, `OperPersonInChargeAdminController` + `person-in-charge/oper-{list,edit}.html`).
- **4402는 아직 통합화면을 가리킨다** - 통합화면의 기본 scope가 `locgov`라 4402를 누르면 지자체담당자 내용이 나오므로 중간상태로도 앞뒤는 맞다. 4402 전용 화면을 이식할 때 `/admin/person-in-charge/locgov`로 바꾼다.
- 두 화면 차이(이식 시 참고): 대상권한 5·6 vs 1~4 / 삭제허용 1~5 vs **1·3만** / 주담당자 2명 제한 ROLE_ADMIN_5 vs ROLE_ADMIN_3 / 지자체 스코프·지자체명 검색·상위지자체(WDR)·하위지자체 ajax·"주관리자+중지→부관리자 강등"은 **locgov에만** 있음.

**★운영관리자(4501) 완료 - 2026-10-04**: 검색구분(아이디/이름/휴대폰)·등록일 범위(기본 오늘, 전체 포함)·회원구분 체크박스(전체/시스템 주·부/행안부 주·부)·사용여부(전체/사용/중지)·10컬럼·상세수정(ajax)·삭제(ajax) 복원. 4105·4107과 같이 **진입(GET)에서는 조회하지 않는다**.
- `Manager`에 **`phoneNumber`·`denyDate` 매핑 추가**(컬럼은 원래 있었고 매핑만 없었다 - 목록의 휴대폰·중지일자 컬럼용). `PersonInChargeAdminService.update`에 소속부서·직위 오버로드 추가(기존 호출부 영향 없음).
- 권한 게이팅: 수정/삭제 버튼과 소속부서·직위 입력칸은 **ROLE_ADMIN_1·3에만** 노출(AS-IS c:if 그대로). 응답 코드도 AS-IS대로 `SUCC`/`ERR_MAIN_CNT`/`ERR_NOT_ALLOW`/`FAIL`이고 본인 삭제 시 `isLogout=Y`.
- AS-IS verbatim: h3가 `MENU_4501`(운영관리자)이 아니라 **`MENU_1101`="운영자관리"**, colgroup col 2개(컬럼 10개), 아이디·이름·휴대폰은 필수표시(*)인데 **읽기전용**, 목록 버튼 class `btn-defualt` 오타, 중지일자는 '중지'일 때만 표시.
- **AS-IS 결함 1건 고침**: oper 목록의 삭제가 `locgov-charger/delete`로 POST한다(복사 흔적). 버튼이 ROLE_ADMIN_1·3에만 보이고 그 권한은 양쪽에서 허용되므로 관측되는 동작은 동일 - 화면이 분리된 TO-BE에서는 앞뒤가 안 맞아 자기 엔드포인트로 바로잡았다.

**★오프라인담당자(4601) 완료 - 2026-10-04**: AS-IS 13개 엔드포인트 전부(목록 GET/POST·엑셀·사용여부 일괄변경·등록 폼/처리·아이디중복확인·상세·수정·권한이관·삭제·비밀번호초기화 팝업) + 템플릿 4개(`off-person-in-charge/{list,form,edit,password-init}.html`). 이 영역은 소속이 지자체가 아니라 **지점(BANK_CODE + PSITN_NM)**이다(농협 창구 오프라인 기부접수 담당자).
- **권한분기가 이 화면의 본체다**: 목록은 ROLE_ADMIN_1~4·7만. **부담당자(ROLE_ADMIN_8)는 목록 대신 자기 상세로 리다이렉트**되고 그 외 권한은 홈으로. 상세도 8이 남의 것을 열면 자기 것으로 되돌린다. 오프라인담당자(7·8)가 조회하면 목록이 **자기 지점으로 스코프**된다. 상세는 SYS/주담당자에게만 수정·삭제·목록 버튼·구분 라디오·사용여부 행·비밀번호초기화 버튼이 보이고, 부담당자에게는 **'저장' 버튼 하나**뿐이다(사용여부 행 자체가 없다).
- **주담당자 승격 3분기**(AS-IS `getOffPersonInChargeMainCount` = 같은 **BANK_CODE** + STATUS_CODE=9 + 본인 제외): 요청자가 주담당자면 2명 초과 `ERR_MAIN_CNT` / 정확히 2명 + 대상이 남 → **`NEED_AUTH_SWAP`** / 그 외 저장. 요청자가 시스템·행안부 관리자면 **2명부터 바로 차단**(강등 대상을 지정할 방법이 없어서 - AS-IS 주석).
- **권한이관(`/auth-swap`)**: 대상을 주담당자로 올리면서 **요청자 본인을 부담당자 + 중지(DENY_DATE 기록)**로 내린다 → `AUTH_CHANGED_LOGOUT` → 즉시 로그아웃.
- **AS-IS 결함 1건 고침**: 등록(`insertOffPersonInCharge`)이 `String.valueOf(personInCharge.getStatusCode())`로 상태를 넣는데 등록 폼에 사용여부 입력칸이 없어 **statusCode(Long)가 항상 null → 문자열 `"null"`이 STATUS_CODE에 INSERT**된다. 목록·상세가 `STATUS_CODE IN (9,2)`로 걸러지고 로그인은 `='9'`를 요구하므로 **방금 등록한 담당자가 목록에도 안 보이고 로그인도 못 한다**(등록 기능이 사실상 불구). TO-BE는 '사용'(ACTIVE)으로 넣는다.
- AS-IS verbatim 보존: h3가 목록만 `MENU_4601`이고 **등록·상세는 `MENU_1101`="운영자관리"**(복사 흔적) / 등록 폼의 구분 라디오는 **둘 다 disabled**이고 서비스도 권한을 ROLE_ADMIN_8로 고정 → **등록되는 계정은 항상 부담당자** / 비밀번호·이메일도메인 칸 disabled(미입력 시 기본 비밀번호 `nacf1234`, 도메인 `@nonghyup.com` 강제) / **직책명(OFCPS_NM)에 이메일을 그대로 중복 기록**(AS-IS 매퍼 `EMAIL=#{email}, OFCPS_NM=#{email}` - 2025-10-29에 '직책' 칸을 '이메일'로 바꾼 흔적) / 목록 버튼 class `btn-defualt` 오타 / 상세의 사용여부 라벨엔 required_mark 없음 / '담당자 연락처' 행은 AS-IS 주석상태 그대로 / 아이디중복확인 응답은 **점유됐을 때 `SUCC`**(의미가 거꾸로) / 비밀번호 팝업 table의 summary·caption이 "메인 배너관리" / 팝업을 **여는 것만으로 비밀번호가 초기화**된다(GET이 발급까지 수행) / 목록 하단의 숨은 "날짜 셋팅 영역"도 유지(이 화면엔 날짜 입력칸이 없어 읽히지 않는 비활성 마크업).
- `Manager`에 **`infoUpdtDe` 매핑 추가**(컬럼은 원래 있었다). 등록 시 오늘로, 수정 시 **비어 있을 때만** 오늘로 채운다(AS-IS CASE WHEN 그대로) - 부담당자의 "지점정보 미입력" 판정 플래그.
- 검색 일치방식도 AS-IS 그대로: **지점명만 LIKE**이고 아이디·이름·개인번호는 `=`(완전일치). 그래서 `PersonInChargeSearchParam`(등록일을 오늘로 강제)을 쓰지 않고 `OffPersonInChargeSearchParam`을 따로 뒀다.
- 엑셀다운로드는 `PrivacyAccess.OFF_CHARGER`에 **tobePattern 추가**(`/admin/off-person-in-charge/list/download-excel`)해서 기존 사유 모달을 그대로 재사용한다. AS-IS xlsx → TO-BE CSV(컬럼 8개·파일명 어간 동일).
- **검증용 시드**: `database/ddl/seed-admin-off-person-in-charge.sql`(적용 완료) - 개발DB에 ROLE_ADMIN_7/8 계정이 0건이었다. 지점 011에 주담당자 2명(정원 꽉 찬 상태) + 부담당자 2명(1명은 중지), 012에 주 1·부 1, 035에 부 1(infoUpdtDe 빈 상태). 이 구성으로 `ERR_MAIN_CNT`·`NEED_AUTH_SWAP`·지점 스코프를 바로 재현할 수 있다. 목록/정원/스코프/검색 쿼리는 SQL로 대조 검증했다.
- **곁들여 고친 것**: 4402·4501 템플릿 4개가 `location.href="/admin/logout"`(GET)으로 로그아웃하는데 TO-BE `/admin/logout`은 **POST 전용**이라 405였다 → 숨은 `#logoutForm` submit으로 교체.

**★4701 답례품 관리자 = 이식대상 없음 (2026-10-04 실측 확정)**: AS-IS export(`OP_MENU_202610010945.sql`) 28행이
`(4701,4700,'답례품 관리자','/opmanager/user/rtnpsnt/list',1,'Y','2')`다 - **status_code='2'(중지)로 AS-IS에서도 노출되지 않고**,
AS-IS 소스에 `rtnpsnt` 컨트롤러·JSP가 **아예 없다**(`RtnpsntReqstCode`·`RtnpsntInfo` 도메인 클래스 2개만 존재). 즉 죽은 메뉴 행이다.
노출되는 쪽은 **16151 답례품제공자 관리 → `/opmanager/seller/list`(status '1')**로 별개 메뉴이고 TO-BE `/seller`가 그쪽에 대응한다.
[[as-is-parity-includes-disabled-state]]대로 비활성 그대로 두었다 - TO-BE op_menu의 4701 menu_url(`/seller`)은 과거 추정값이고
숨은 메뉴라 도달하지 않는다(건드리지 않았다. 혹시 status를 1로 켜면 16151 화면이 열리는 점만 유의).

**★일반회원관리(4101) 완료 - 2026-10-04**: AS-IS 10개 엔드포인트 전부(목록 GET/POST·상세·기부내역 조각·포인트내역 조각·
비밀번호확인 팝업·개인정보열람 팝업·회원탈퇴 팝업·탈퇴처리·배송지 팝업) + 템플릿 8개
(`member-admin/{list,details,cntr-list,point-list,password-confirm,info-access,secede-write,delivery}.html`).
구버전 `member-admin/detail.html`은 대체돼 삭제했다. 4105·4107과 같이 **진입(GET)에서는 조회하지 않는다**.
- **데이터가 3개 서비스에 걸쳐 있어 admin이 조립한다**: 회원·배송지·탈퇴=member / 기부내역·기부누적액·발생포인트=donation(`g_cntr`) /
  사용포인트=point(`g_cntr_use_point`) / 가입경로·기부형태·민간연계기관 **라벨**=admin `OP_COMMON_CODE`(SBSCRB_SE·CNTR_PATH·LINK_INSTT_CD).
  지자체명은 donation의 `g_locgov`(admin은 `LocgovClient.allLocgovs()`로 코드→이름).
- **★포인트 내역은 AS-IS가 UNION ALL + 러닝잔액이다**: `G_CNTR`(적립 OCC) ∪ `G_CNTR_USE_POINT`(사용)을 등록시각 역순(같으면 USE_SN 역순)으로
  세우고, 각 행의 잔액 = **같은 지자체에서 그 행의 등록시각까지 적립합 − 사용합**이다. 두 표가 다른 서비스에 있어 각자 전량 받아
  `GeneralCustomerAdminController.buildPointRows()`에서 같은 공식으로 계산한다. 적립합은 납부완료 건만, **사용합은 use_se_code를 가리지 않는다**
  (AS-IS 서브쿼리 그대로. 누적합계의 '사용포인트'만 `use_se_code='1'`).
  타임스탬프 타입이 서로 다르다 - donation `g_cntr.frst_regist_pnttm`은 VARCHAR(14), point는 TIMESTAMP → **point API가 yyyyMMddHHmmss 문자열로 내려준다**(비교 가능하게).
- **★AS-IS 조건의 TO-BE 번역 2건(중요)**: ① AS-IS `CNTR_STTUS_CODE='200'` → TO-BE는 낱말(`COMPLETED`/`REQUESTED`/`CANCELLED`).
  ② AS-IS `STTEMNT_PAY_DE IS NOT NULL`(납부일) → **TO-BE는 `g_cntr.sttemnt_pay_de`를 어디서도 채우지 않는다**(납부 게이트웨이 이식이 별도 라운드).
  둘을 AS-IS 문자열 그대로 쓰면 기부내역·누적합계·포인트내역이 **전부 0건**이 된다 → 납부완료 판정은 `cntr_sttus_code='COMPLETED' AND delete_at='N'`으로 옮겼다
  (donation의 기존 `DonationTotalAdminApiController`와 같은 기준). 다만 **기부내역의 '납부일' 칸은 AS-IS 표시로직대로 '미결제', '발생포인트'는 '-'로 보인다**(데이터 공백, 로직은 AS-IS 그대로).
- **member 결함 1건 고침**: `AdminMemberService.search`에 **상태 필터가 없어** 탈퇴·휴면 회원까지 일반회원관리 목록에 나왔다(AS-IS는 `WHERE U.STATUS_CODE = 9`).
  실측 38 ACTIVE / 11 WITHDRAWN - 4101·4105·4107 세 화면의 모집단이 겹쳐 있던 셈이다. `ACTIVE`만 남기도록 고쳤다.
- **member 결함 1건 더 고침**: `adminWithdraw`가 **`LEAVE_USER_ID`(탈퇴처리자)를 남기지 않아** 관리자가 처리한 탈퇴가 4105에서 '회원탈퇴'로 보였다
  (4105의 탈퇴구분·담당자 컬럼이 그 값으로 갈린다). `adminWithdraw(userId, reason, leaveUserId)` 오버로드 추가.
- **AS-IS 결과코드 분기 복원**: 탈퇴는 `SUCC`/`ERR_ALR_SECEDE`/`ERR_ONE_PASS`다. **디지털원패스 회원(`op_user.user_key` 보유)은 관리자가 탈퇴시킬 수 없다** -
  `User.userKey` 매핑 추가(컬럼은 원래 있었다) + `OnePassMemberException` + Detail DTO에 `onePassUser` boolean(원패스 키 자체는 내려주지 않는다).
- **개인정보 열람 이력 복원**: AS-IS는 비마스킹 상세를 보여주기 직전 `G_INDVDLINFO_READNG_HIST`에 (열람자, 대상, 시각)을 남긴다.
  `member.g_indvdlinfo_readng_hist` 테이블은 있었지만 READNG_SN 시퀀스가 없어 `migration-member-indvdlinfo-readng-hist-seq.sql`로 만들고 6개 롤에 GRANT(적용 완료).
  `readng_dt`는 다른 AS-IS 날짜컬럼과 달리 **실제 TIMESTAMP**다. 엑셀 사유로그(`OP_PRIVACY_ACCESS_LOG`, 메뉴 1411)와는 다른 테이블이다.
  기존 TO-BE는 세션에 한 번 비밀번호를 받고 재방문 시 묻지 않는 방식이었는데, AS-IS 방식(**열람할 때마다 팝업 + 이력 적재**)으로 교체했다.
- **TO-BE 전용 기능 보존**: 계정잠금 해제·RBAC 권한 회수(SFR-002)는 AS-IS 4101에 없다 → 엔드포인트는 `MemberAdminController`에 남기고
  상세화면 맨 아래 **"운영 기능 (AS-IS 외)" 블록**으로 분리 노출했다(AS-IS 영역과 섞지 않음). 사용자 확인 필요 사항으로 보고했다.
- 신규 API: donation `/api/admin/member-donations/{cumulative-total,cntr-list,point-occ-rows,totals}`(네이티브 쿼리 `MemberDonationAdminRepository` - 엔티티 `Donation`이 cntr_point·sttemnt_pay_de 등을 매핑하지 않아 조회전용 네이티브로 읽는다),
  point `/api/admin/member-points/{userId}/use-rows`·`/used-totals`, member `/api/admin/members/{userId}/deliveries`·`/pii-access`.
- AS-IS verbatim: h3가 목록·상세 모두 **M00210**(메뉴명 '일반회원관리'가 아니다) / 목록 colgroup col 1개(컬럼 9개) / **엑셀 다운로드 버튼 없음**(구버전 list-old.jsp에만 있었다) /
  아이디·이름 두 칸 모두 상세 링크 / 가입일이 비면 "가입일을 입력 하십시오" / 상세 네비 끝에 "수정"을 덧붙인다(상세인데) / 상세는 "국민비서·SMS 수신동의", 열람팝업은 "SMS 수신동의" /
  열람팝업의 '성별' 행은 AS-IS 주석상태 그대로 / 탈퇴팝업 취소 버튼만 `btn-normal` / 배송지 팝업은 제목만 '관리'이고 **조회 전용**(빈 문구도 "데이터가 없습니다.") /
  포인트내역의 외부 SalesOn 상점 팝업 코드도 주석 그대로.
- **검증**: 모집단(38/11)·누적합계(user 1000 → 기부 13,765,000 / 발생 924,000 / 사용 136,000 / 잔액 788,000)·기부내역 지자체 INNER JOIN·
  포인트내역 OCC/USE 교차정렬을 SQL로 대조했다. 4개 서비스(member·donation·point·admin) bootJar 전부 EXIT=0.

**★지자체관리(4401) 완료 - 2026-10-04. 이로써 회원관리 영역 8/8(4701은 이식대상 없음) 종료.**
AS-IS 14개 엔드포인트 전부(목록 GET/POST·등록 폼/처리·수정 폼/처리·포인트율 ajax조회/적용·포인트율 이력팝업·부서코드 이력팝업·직인뷰어·직인삭제·배경이미지 삭제/조회·지자체삭제) + 템플릿 5개(`locgov-admin/{list,form,edit,dept-popup,point-history-popup}.html`) + **공통 주소검색 팝업 신규**.
- **이 화면은 TO-BE 기능이 이미 거의 다 있었다**(직인 암호화 저장·복호화 뷰어, 배경이미지 PC/MB, 포인트율 이력, 모금제한 g_cntr_lmtt, 부서이력, 기부혜택문구). **갭은 AS-IS 화면 형태·라우트·분기**였다 - 그래서 컨트롤러를 AS-IS 라우트로 재작성하고 템플릿을 AS-IS 구조로 다시 썼다.
- **권한분기(AS-IS getLoginUserAdminRoleCheck)**: SYS(1~4)는 목록+등록+전체수정. **LOC(5·6)는 목록이 없다** - 자기 지자체가 등록돼 있으면 수정화면, 없으면 등록화면으로 바로 보낸다(남의 지자체를 열면 자기 것으로 되돌림). 그 외 권한은 홈. 수정화면 버튼도 SYS는 상단 수정/목록, LOC는 하단 가운데 '저장' 하나다.
- **회계구분(fisSp)은 공통코드가 아니다**: AS-IS가 **지자체코드 3~5자리가 '000'(광역)이면 31/51, 아니면 41/61**을 자바에서 직접 만들어 내려준다(등록화면은 시군구를 고르는 순간 JS가 select를 다시 만든다). 그대로 옮겼다. 실측 지자체는 전부 기초(41/61)이고 fis_sp는 아직 전부 NULL.
- **포인트 지급률은 point 소유**: AS-IS는 `G_CTBNY_SETUP`에 쓰지만 TO-BE에서 실제 적립 계산(`PointService#currentPointRateOf`)이 읽는 표는 `PT_LOCGOV_POINT_RATE`라 그쪽에 쓴다(기존 결정 유지). 이력 팝업도 같은 표를 읽는다 - 연도별 행 자체가 이력이고 작성자명까지 있어 AS-IS 팝업 5컬럼과 1:1로 맞는다. **다만 그 표에 frst_regist_pnttm이 없어** AS-IS의 `IFNULL(last_updt_pnttm, frst_regist_pnttm)` 폴백은 불가 - 변경일이 비면 빈칸이다(실측 시드 일부가 그렇다).
- 목록의 포인트율 컬럼은 AS-IS가 스칼라 서브쿼리였는데 지급률이 다른 서비스라 **일괄 조회 API 신규**: point `GET /api/admin/locgov-point-rates/current?stdrYear&locgovCodes`(+ `LocgovPointRateRepository.findByStdrYearAndLocgovCodeIn`). 행마다 호출하면 N+1.
- **donation 신규 2건**: 직인 삭제(`POST /api/locgov-admin/{code}/seal/delete` → 파일 삭제 + OFFCS_NM·OFFCS_FILE_NM·ORGINL_FILE_NM NULL. AS-IS `updateLocgovOffcsInfo`가 **직인명까지 비운다**), 부서이력 DTO에 `lastUpdusrId` 추가(이력 팝업의 "등록자 이름(아이디)"는 admin이 자기 OP_MANAGER에서 찾는다 - 관리자 계정은 admin 소유).
- **기부금 모금제한은 AS-IS가 지자체 수정 저장 안에서 처리**한다(기간·사유 있으면 insert/update, 비면 delete). TO-BE는 등록/삭제 API가 따로 있어 `applyLmtt()`로 묶었다. 화면의 '사용' 체크박스는 AS-IS대로 `onclick="return false;"`라 직접 켤 수 없고 기간이 유효할 때 스크립트가 자동 체크한다.
- **★공통 주소검색 팝업 신규 이식**: AS-IS `/opmanager/juso-popup`(JusoController + 689줄 JSP)이 지자체관리·오프라인기부접수 등 여러 화면이 공유하는 컴포넌트인데 TO-BE admin에는 **아예 없었다**(모든 주소칸이 맨 텍스트 입력). 행정안전부 주소검색 API를 **브라우저에서 JSONP로 직접** 호출하는 클라이언트 전용 위젯이라 서버 로직이 없다 → `/admin/juso-popup` + `templates/juso/juso-popup.html`로 옮겼다(스크립트로 기계변환, 스크립트릿 0개 잔류). 정적자산(`content/modules/juso/addrlink.js`, `content/opmanager/css/juso/addrlink.css`, 이미지)은 AS-IS 통째복사 때 **이미 들어와 있었다**. AS-IS가 JSP에 박아 둔 승인키·도메인·결과형식(4)만 `ghlove.juso.*` 설정으로 뺐다(기본값 = AS-IS 값). WebConfig 게이트에도 추가.
- AS-IS verbatim: **등록·수정 화면의 h3가 빈 칸**(`<h3><span></span></h3>`)이다 / 목록 colgroup col 1개인데 빈목록 colspan은 9(컬럼 8개 - AS-IS 불일치) / 등록일 범위에 **기본값이 없고 날짜버튼에 '전체'도 없다** / 지자체 칸만 링크(담당자명·연락처는 평문) / **삭제 버튼은 2022.11.18 주석처리** - 엔드포인트만 남기고 버튼은 주석 상태로 / 체크박스 컬럼이 없는데 `checkedEventSet()`이 남아 있음(죽은 코드도 유지) / **명예회원선정기준 블록은 AS-IS에서 전체 주석처리** → 주석 그대로(그래서 honor-amt 검증·전송도 없다. [[honor-tier-thresholds-unset]]와 일치) / 사업자번호 3-2-5·연락처 3분할 입력 / 부서코드 11자리·행정표준기관코드 7자리 / 연도 select는 **올해 이후만** / 직인 파일이 있으면 file input을 숨기고 파일명+삭제(x)만.
- **AS-IS 죽은 코드 2건 미이식(기록)**: `POST /test`(본문이 `System.out.println("test")`뿐인 디버그 스텁), 주석처리된 `GET /imageView/{locgovCode}`.
- TO-BE 전용 "지자체별 기부혜택 안내문구"(SFR-003)는 AS-IS 4401에 없어 **수정화면 맨 아래 "운영 기능(AS-IS 외)" 블록**으로 분리 노출(4101과 같은 처리).
- **검증**: 목록 ordering 정렬 + 올해 지급률 LEFT JOIN(순천시 25.00 / 미설정은 빈칸), 회계구분 분기, 지급률 이력 정렬(변경일 desc→년도 desc), 부서이력 1건·모금제한 0건·직인 2건을 SQL로 대조. admin·donation·point bootJar 전부 EXIT=0.

**TO-BE 기존 자산**: 컨트롤러는 `MemberAdminController`·`MemberBatchAdminController`·`SecedeUserAdminController`·`SleepUserAdminController`·`LocgovAdminController`·`PersonInChargeAdminController`·`OffPersonInChargeAdminController`·`SellerAdminController`가 이미 있고, 템플릿은 `member-admin/{list,detail,batch}.html`·`locgov-admin/{list,form}.html` 등이 있다. 시스템관리 영역의 경험상 **기존 화면은 AS-IS와 상당히 다를 가능성이 높으니 그대로 신뢰하지 말고 AS-IS와 1:1 대조부터 할 것**.

**★회원탈퇴관리(4105) 완료 - 2026-10-03**: 검색구분(아이디/탈퇴사유)·탈퇴일 범위(기본 오늘, 날짜버튼에 **전체(all-1)**가 하나 더 있다)·탈퇴구분(전체/회원탈퇴/관리자탈퇴)·화면출력·6컬럼·탈퇴사유 팝업(600x350) 복원.
- **AS-IS는 진입(GET)에서 조회하지 않는다** - 컨트롤러가 `Collections.EMPTY_LIST`/count 0을 내려주고 검색(POST)해야 조회된다(화면 안내문구 "검색 버튼을 클릭하면 조건에 맞는 검색결과를 확인할 수 있습니다."가 그 설명). 예전 TO-BE는 GET에서 전체를 조회하고 탈퇴구분·검색구분이 없었다. GET/POST를 분리해 재현.
- 6컬럼을 서비스 경계로 나눠 채운다: 탈퇴일·아이디·사유·탈퇴코드·**탈퇴처리자ID**는 member, 사유 라벨은 admin의 `LEAVE_CODE` 공통코드(5종), 담당자(권한그룹+이름)는 admin의 `op_manager`/`op_role`에서 탈퇴처리자ID로 찾는다. AS-IS는 OP_MANAGER→OP_USER 폴백이 있는데 탈퇴 처리자는 관리자이므로 OP_MANAGER 경로만 이식(폴백 미이식).
- **member에 필드 1개 추가(읽기 전용·additive)**: `UserDetail.leaveUserId`(컬럼 `LEAVE_USER_ID`는 원래 있었고 **매핑만 없었다**) + `SecedeRowDto.leaveUserId`. 이게 없으면 탈퇴구분·담당자 2컬럼을 영구히 채울 수 없다(admin이 member.op_user_detail을 직접 못 봄). 탈퇴구분 필터는 member API에 없어 admin 메모리에서 적용.
- AS-IS verbatim: h3가 메뉴명(탈퇴회원리스트)이 아니라 `MENU_4105`="**회원탈퇴관리**", 4번째 컬럼 헤더가 **"탙퇴사유" 오타**, colgroup에 col이 1개뿐(컬럼 6개), 팝업 제목이 "회원탈퇴"(탈퇴사유가 아님), 빈 목록도 표 안 한 행으로.

**★휴면회원관리(4107) 완료 - 2026-10-03**: 검색구분(아이디/이름)·최종 방문일 범위(기본 오늘, 전체(all-1) 포함)·화면출력·8컬럼·휴면회원 해제(ajax) 복원. 4105와 똑같이 **진입(GET)에서는 조회하지 않고 검색(POST)해야 조회된다**. 검색은 화면 안내("※ "이름, 아이디"는 정확하게 입력해야 합니다.")대로 **완전일치** - member API가 부분일치라 admin에서 한 번 더 좁힌다.
- 8컬럼이 세 서비스에 걸쳐 있다: 최종방문일·아이디·이름·**주소**=member, **기부누적액**=donation, **포인트잔액**=point. 예전 TO-BE는 뒤 3컬럼이 아예 없었다.
- **신규 조회전용 API 2개 + member 필드 2개(전부 additive)**: point `GET /api/admin/point-balances?userIds=`(PT_POINT_BALANCE), donation `GET /api/admin/donation-totals?userIds=`(**완료(COMPLETED) 기부만** 합산), member `SleepRowDto.address/addressDetail`. 둘 다 **현재 페이지 userId를 한 번에** 보내는 일괄 조회다(행마다 호출하면 N+1).
- 휴면 해제는 AS-IS대로 ajax `POST /wakeup`(파라미터명 `userIdList`)이고 응답 `{isSuccess, data}`에서 화면은 **data=="SUCC"만 성공**으로 본다(그 외는 "오류가 발생했습니다.").
- AS-IS verbatim: 표 summary/caption이 **"휴먼회원관리" 오타**(휴면→휴먼), colgroup col 2개(컬럼 8개), 금액 `#,###.##`·없으면 '-', 빈 목록 행에만 background 스타일 없음, "휴면회원 해제" 버튼은 목록이 비어도 항상 노출.

**★★담당자 화면의 사용여부는 반드시 경계에서 변환할 것(2026-10-04에 터진 버그)**: AS-IS `OP_MANAGER.STATUS_CODE`는 **'9'(사용)/'2'(중지)** 숫자코드인데 이 프로젝트는 같은 컬럼에 **`ACTIVE`/`LOCKED`**를 쓴다(`ManagerAdminService.STATUS_ACTIVE/STATUS_LOCKED`, 개발DB 실측도 전부 `ACTIVE`). 4402·4501을 처음 이식할 때 AS-IS 값 9/2를 그대로 비교·저장해서 **① 수정화면에서 사용여부 라디오가 아무것도 체크되지 않고 ② 저장하면 '9'/'2'가 들어가 두 체계가 섞이는** 버그가 있었다. 조치:
- `Manager.getAsIsStatusCode()`(@Transient, LOCKED→'2' 그 외 '9') - **템플릿은 이걸 비교**한다(`item.asIsStatusCode == '2'`).
- `PersonInChargeAdminService.toStatusCode('9'|'2')` → ACTIVE/LOCKED - **컨트롤러 저장 직전에 변환**한다.
- `PersonInChargeSearchParam.matchesStatus`도 AS-IS 표기로 바꿔 비교한다.
- **마크업의 라디오 value는 AS-IS대로 9/2 유지**(verbatim). 4601(오프라인담당자)도 사용여부 라디오가 9/2이므로 같은 규칙을 쓸 것.

**주의**: 회원 데이터는 member 서비스 소유다(`member.op_user` 등). admin은 `MemberAdminClient`로 cross-service 조회한다 - member의 검색 API에 파라미터가 부족하면 시스템관리 1408처럼 admin 메모리 필터링으로 우회하거나, 7210처럼 **사용자 승인을 받고** member에 조회 API를 추가한다. member 서비스 쪽 검색 키는 현재 `USER_NAME`/`ADDRESS`/`LOGIN_ID` 3종뿐이고 **page가 0부터**다.
