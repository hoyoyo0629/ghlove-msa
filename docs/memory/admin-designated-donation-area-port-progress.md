---
name: admin-designated-donation-area-port-progress
description: 특정사업 기부 관리(17000) 영역 이식 원장 - AS-IS 대조표·갭 실측·구현순서. 기부금관리/통계를 뒤로 미룬 이유 포함
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-06T00:15:52.473Z
---

# 특정사업 기부 관리(17000) 이식 원장

2026-10-05 착수. 고객센터 영역(승인순서 4건) 완료 후 사용자 지시
"나중에 처리할 사항으로 남겨두고 다른 메뉴들 구현작업하자"에 따라 고른 영역.

## 왜 이 영역을 먼저 골랐나 (기부금관리·통계를 미룬 이유)

**기부금관리(14000)·통계(6000)는 보류 중인 설계 결정에 정면으로 걸린다.**
TO-BE `GiveStateService` 주석이 그대로 말한다 - "admin은 donation/point 서비스의 DB를 직접 읽을 수
없어(DB per Service) 이미 있는 통계 ReadModel(`STAT_DONATION_LEDGER`/`STAT_POINT_LEDGER`)을 재사용한다".
그런데 [[defer-readmodels-pending-da-design]]에 따라 **조회모델 작업 자체가 DA 설계안까지 보류**다.
그 위에 화면을 더 쌓는 것도, 걷어내고 교차조회/API로 바꾸는 것도 보류 결정을 건드린다.
→ **사용자 결정 필요**: 기부금 현황 화면들을 (a) ReadModel 유지 (b) donation API 호출
(c) 교차스키마 직접조회 중 무엇으로 갈 것인가. 결정 전에는 착수하지 않는다.

**특정사업(17000)은 그 경계가 없다** - TO-BE가 이미 `DesignatedProjectClient`로
**donation 서비스 API를 호출**하는 구조이고(깔끔한 MSA 패턴), 배너는 admin 자체 표
(`op_representative_banner`)다. 그래서 바로 이어서 할 수 있다.

## 메뉴 구조 (실측)

| 메뉴 | 이름 | TO-BE menu_url | 상태 |
|---|---|---|---|
| 17100 | 특정사업 기부 정보 | (그룹) | |
| 17101 | 특정사업 기부 목록 | `/designated-projects` | ✅ 있음 |
| 17102 | 특정사업 기부 등록 | `/designated-projects` | ⚠ **17101과 중복** (등록은 `/new`) |
| 17200 | 특정사업 기부 통계 | (그룹) | |
| 17201 | 지자체별통계 | `/designated-projects/analysis/locgov` | ✅ |
| 17202 | 월별통계 | `/designated-projects/analysis/month` | ✅ |
| 17300 | 특정사업 기부 배너관리 | (그룹) | |
| 17301 | 특정사업 기부 배너관리 | `/designated-projects/banners` | ✅ |

## AS-IS vs TO-BE 실측 (2026-10-05)

AS-IS: `DesignatedDonationManagerController` **1,262줄** + JSP **17개 7,275줄**
(`opmanager/i18n/designated-donation/`). TO-BE: `DesignatedProjectAdminController` 359줄 +
`RepresentativeBannerController` + 템플릿 8개.

| 화면 | AS-IS JSP | TO-BE 템플릿 | 갭 |
|---|---|---|---|
| 목록 17101 | list.jsp **518** | list.html 75 | **14%** ← 최대 갭 |
| 등록/수정 17102 | form.jsp **928** | form.html 243 | **26%** |
| 지자체별통계 17201 | analysis/locgov.jsp 409 | analysis-locgov.html 286 | 70% |
| 월별통계 17202 | analysis/month.jsp 720 | analysis-month.html 612 | 85% |
| 배너 17301 | banner/list 145 + form 298 | banner-list 56 + banner-form 83 | **31%** |
| 참여기관(part) | part/list 350 + form 188 | — | **0%** |
| 권한요청(request) | request/list 322 + form 347 + details 414 + history 91 | — | **0%** |
| 미리보기(preview) | preview.jsp **632** | — | **0%** |
| 공지(notice) | notice/list 268 + form 323 | form.html 안에 일부 | 부분 |
| 기타 | analysis/month_temp2025.jsp 715 · popup-confirm-log 107 | — | temp2025는 1회성 가능성 |

## 확인된 사실 (조사 결과 - 다시 조사하지 말 것)

- **배너는 같은 표를 `PROCESS_TYPE`으로 갈라 쓴다**: AS-IS가 `listParam.setProcessType("DESIGNATED_DONATION")`.
  TO-BE `RepresentativeBannerService`도 **이미 그 상수로 필터**한다(`PROCESS_TYPE` 상수 + 전용 쿼리) -
  대표배너(16251 `/admin/main-banners`)와 섞이지 않는다. op_qna/QNA_TYPE과 같은 패턴이지만 이쪽은 정상.
- AS-IS 배너 엔드포인트는 `/banner/list` GET · `/banner/change-ordering` POST ·
  `/banner/form/{id}` GET+POST · `/banner/delete` POST.
  **TO-BE에는 순서변경·삭제가 없고** 대신 `/{id}/toggle`이 있다(사용여부 전환).
- AS-IS 배너 목록 진입에 가드가 있다: `checkDsgncntrAuthPartInfo() == 0` →
  "부서정보가 없어서 처리가 불가능합니다." 로 리다이렉트. TO-BE에 없다.
- `DesignatedProjectClient`에 이미 있는 API: 목록/단건/등록/수정/공지CRUD/부서/승인로그/
  연도목록/지자체별·월별 통계/월별 캠페인·모금액·금액/지자체통계 요약·목록.
  **없는 것**: 참여기관(part), 권한요청(request), 미리보기(preview), 배너 순서변경·삭제.

## 구현 순서 (내가 정함)

1. **17102 등록 URL 분리** - `/designated-projects/new`로 (현재 17101과 중복)
2. **목록 17101 보강**(518→75, 최대 갭) - 검색조건·컬럼·권한분기 전수 대조
3. **등록/수정 17102 보강**(928→243) - 이미지/기간/부서/승인 흐름
4. **배너 17301 보강** - 순서변경·삭제·부서정보 가드
5. 참여기관(part) 신규 - 538줄
6. 권한요청(request) 신규 - 1,174줄 (승인 흐름이라 권한 결정 필요할 수 있음)
7. 미리보기(preview) 신규 - 632줄
8. 통계 2화면 잔여 보강(70%/85%)

## 진행 기록

### 1 · 4 완료 (2026-10-05) - 등록 URL 분리 + 배너 보강, admin bootJar EXIT=0

**① 17102 등록 URL 분리**: `/designated-projects` → **`/designated-projects/new`**
(17101 목록과 같은 URL이라 메뉴 둘이 같은 화면을 열었다). DB 직접 UPDATE로 적용.

**④ 배너 17301 보강** - AS-IS에 있고 TO-BE에 없던 엔드포인트 2개 + 가드:
- `POST /designated-projects/banners/change-ordering` - 노출순서 **일괄 변경**(ajax, JSON).
  AS-IS는 `id[]`와 `ordering[]`을 **같은 인덱스로 짝지어** 돌며 UPDATE한다(길이가 다르면
  AS-IS는 예외로 터지므로 짝이 맞는 만큼만 처리). 목록에 순서 입력칸 + [순서변경] 버튼 추가.
- `POST /designated-projects/banners/delete` - **★이름은 "삭제"지만 배너 행을 지우지 않는다**.
  AS-IS 매퍼가 `deleteFlag`에 따라 **이미지 파일명만 비우는 UPDATE**다
  (`pc` → `FILE_NAME_PC=''`, `mobile` → `FILE_NAME_MOBILE=''`). 배너를 내리는 수단은 사용여부 전환.
  등록/수정 화면의 이미지 옆에 [x] 링크로 붙였다. 디스크 파일은 AS-IS도 남긴다.
- **부서정보 가드**(AS-IS `checkDsgncntrAuthPartInfo()`) - 문구 "부서정보가 없어서 처리가
  불가능합니다." 그대로. AS-IS 분기: **ROLE_ADMIN_10**(특정사업 담당자)은 자기 부서 id가 없으면 차단 ·
  **ROLE_ADMIN_1~6**(최고관리자·지자체담당자)은 통과(-1) · 그 외 차단.
  **근사 1건**: ROLE_ADMIN_10의 "자기 부서 id" 조회 API가 donation에 없어(있는 건 지자체별 부서
  **목록**) 그 지자체에 부서가 하나도 없으면 막는다 - API가 생기면 그 값으로 교체할 것.
- 사용여부 전환을 행마다 `<form>`을 두던 방식에서 폼 하나 재사용 + JS로 바꿨다(표 안의 중첩 폼 제거).

**검증**(트랜잭션 후 롤백): 목록 범위가 `DESIGNATED_DONATION`만(대표배너 `MAIN`과 안 섞임, 3건 중 2건) ·
순서변경 1→5 / 2→3 반영 · 이미지삭제 후 **행은 남고 pc만 비워지고 모바일·사용여부는 그대로**.

### 2 완료 (2026-10-05) - 목록 17101 보강, admin·donation bootJar EXIT=0

**donation 서비스 API 확장이 선행이었다**(목록이 요구하는 검색·집계가 없었다):
- `GET /api/designated-projects/admin/search` - 검색 8개 파라미터 + 행별 집계 + 요약집계
- `POST /api/designated-projects/admin/bulk-update` - 모금상태/공개여부 일괄 변경
- `DesignatedAdminService.search(...)`에 AS-IS `selectDesignatedDonationList` 규칙 전부 이식
- `Donation` 엔티티에 `DELETE_AT`·`STTEMNT_PAY_DE` 매핑 추가(컬럼은 있었으나 미매핑이라
  AS-IS 조건을 걸 수 없었다)

**★AS-IS 규칙 중 중요한 것들**
- **모금상태는 저장값을 그대로 쓰지 않고 다시 계산한다**: 진행이어도 ① 모금액 > 목표금액
  ② 종료일 경과 중 하나면 **종료**로 보여준다(저장값은 안 바꾼다). **상태 검색도 계산된 값에 적용**된다.
- 달성율 = `floor(모금액 * 10000 / 목표금액) / 100`(소수 2자리), 목표금액 0이면 0
- 기간 검색: 시작·종료 **둘 다 있으면 사업 모금기간과 겹치는** 사업(4가지 OR), 하나만 있으면
  그 날짜가 사업기간 안에 드는 사업
- 지자체 검색: 시군구가 있으면 그것, 없으면 광역 하위 전체
- 부서명 기본값 "부서 미지정" · 지자체명은 "광역명 시군구명" · 정렬 사업ID 내림차순
- 집계는 **기부일이 오늘까지**인 완료 기부만
- IN_DATE(1 기간내 / 2 시작전 / 3 종료후)도 같이 내려준다

**★★집계 결함 1건 수정 - 숫자가 2배 가까이 틀려 있었다**
기존 `designatedDonations()`가 `dsgn_dntn_biz_id **is not null**`만 걸어
**`dsgn_dntn_biz_id = 0`인 일반 기부가 특정사업 집계에 섞여 있었다**(실데이터에 0인 행 18건).
AS-IS는 `> 0`이고 `DELETE_AT='N'`도 함께 건다 → `findDesignatedByStatus`로 교체.
**실측: 23건/29,432,000원 → 5건/16,662,000원**. 지자체별(17201)·월별(17202) 통계 숫자도 같이 바로잡혔다.

**AS-IS 결함 1건 보정**: 사업명 입력칸은 `query`로 전송하는데 목록 SQL은 `searchKeyword`를 본다
(화면이 예전 필드명에서 바뀌었는데 SQL이 따라가지 않았다) → **AS-IS에서는 사업명 검색이 전혀
걸리지 않는다**. TO-BE는 걸리게 했다.

**경계 번역 2건**(이 프로젝트 관례대로 TO-BE 어휘 유지):
- 모금상태: AS-IS `'1'/'2'/'9'` ↔ TO-BE `PENDING/OPEN/CLOSED`. 화면 라디오·일괄처리 버튼은
  AS-IS 값을 그대로 쓰고 컨트롤러가 옮긴다.
- 사업구분: AS-IS 코드유형 `BUSINESS_TYPE` ↔ TO-BE **`DSGN_BSNS_TYPE`**(100~400, 4건).
  AS-IS의 `BUSINESS_SUB_TYPE`·`PRJ_STATUS` 코드표는 TO-BE에 없다(상태 라벨은 화면이 붙인다).

**화면**: 검색 6종(지자체는 **지자체담당자에게 숨김**·자기 지자체 고정) · 요약표 4칸 ·
**컬럼 16개** · 출력수 **10/50/100/200/500** · 하단 일괄처리 4종(**진행(승인처리)은 ROLE_ADMIN_1~5만**) ·
광역→시군구 캐스케이딩(`wdrChange`) · 날짜버튼 5종.
공지사항·승인이력 버튼은 기존 상세화면의 해당 영역으로 보낸다(AS-IS는 별도 화면/팝업).
**엑셀·미리보기는 아직 없어 안내만 띄운다**(없는 화면으로 보내 404를 만들지 않는다).

**검증**: 집계 전환 실측(위) · 사업별 집계·달성율(1000: 1건 10,000원 0.03% / 1106: 4건
16,652,000원 40.00%) · 상태 재계산(두 건 모두 목표 미달·기간 내라 OPEN 유지, OPEN인데 종료일
지난 사업은 현재 0건) · admin·donation `bootJar` EXIT=0.

### 3 완료 (2026-10-05) - 등록/수정 폼 보강, admin·donation bootJar EXIT=0

**추가한 것**
- **읽기전용 3칸(수정 모드에서만)**: 남은 일수 / 모금 된 금액 / 달성률.
  AS-IS도 `mode == 'edit'`일 때만 보여준다. donation에 `GET /api/designated-projects/admin/{id}/stat`
  + `DesignatedAdminService.statOf(id)` 신규(집계 규칙은 목록과 동일).
  **AS-IS `leftDayStr`는 종료일이 지나면 0**으로 본다 - 그대로.
- **상태·공개여부를 select → 라디오**로(AS-IS 형태). 문구도 AS-IS 그대로:
  "진행중(승인)" / "종료" / "대기(승인 전)" · "공개" / "비공개".
- **사업부서 지자체별 재로드**: 지자체를 바꾸면 그 지자체 부서로 다시 채운다
  (AS-IS `getDsgncntrPartList`). admin에 ajax `GET /designated-projects/departments/by-locgov` 신규.
  첫 항목은 AS-IS대로 **"-부서 미지정-"(값 0)**. 라벨도 AS-IS대로 "사업부서"(기존 "담당부서").
  지자체담당자는 처음부터 자기 지자체 부서만 받는다.
- **승인 버튼**(AS-IS `confirmProcess`) - 수정 모드 + ROLE_ADMIN_1~5에게만. 목록의 일괄 승인처리와
  같은 API를 쓴다. 미등록 상태면 "등록 후 승인처리가 가능합니다."(AS-IS 문구).
- **상세 설명·기타 사항을 표 밖 별도 섹션(h3)**으로 분리(AS-IS 구조). 상세 설명 textarea에
  `editor-content` 클래스 부여, 이미지 라벨에 AS-IS 안내 "(600px * 600px)" 추가.
- 목표금액 라벨을 AS-IS대로 "목표금액(원)"으로.

**남긴 갭 2건(데이터·결정 필요)**
1. **사업부문(세부구분)**: AS-IS는 `BUSINESS_SUB_TYPE` 코드의 **필수 캐스케이딩 select**
   (`bsnsTypeChange` → `/bsnsSubType`)다. TO-BE에는 그 코드표가 **없고**(`up_id`를 가진 코드가 0건)
   114개 사업 전부 `dsgn_dntn_biz_se_dtl_cd`가 **빈 값**이다. 그래서 기존 **텍스트 입력 그대로 뒀다** -
   빈 select를 필수로 걸면 저장 자체가 막힌다. 코드 시딩은 데이터 결정 사항(AS-IS 덤프는 DDL만).
2. **사업부서 데이터가 0행**(`g_dsgn_dntn_biz_dept_mng`) - 화면은 동작하지만 "-부서 미지정-"만 보인다.
   배너 화면의 부서정보 가드도 같은 이유로 ROLE_ADMIN_10에게는 실질적으로 막힌다.

**기간 입력 형태**: AS-IS는 8자리 datepicker, TO-BE는 `type="date"`(+컨트롤러 `toYmd` 변환)로
이미 동작하므로 바꾸지 않았다 - 저장 포맷은 같다(yyyyMMdd).

**검증**: 읽기전용 3칸 실측 - 1106(목표 41,630,000 / 모금 16,652,000 / 4건 / **40.00%** / 남은 **87일**) ·
1000(0.03% / 87일) · **204(종료일 20250831 지남 → 남은일수 0**, AS-IS 규칙 확인).
admin·donation `bootJar` EXIT=0.

### 5 완료 (2026-10-05) - 사업부서 관리 보강, admin·donation bootJar EXIT=0

**★원장 수정**: 위 갭표에 "참여기관(part) 0%"로 적었던 것은 **틀렸다**.
AS-IS `part/list.jsp`의 화면 제목이 **"사업부서 관리"**다 - `DesignatedPart`(=`G_DSGN_DNTN_BIZ_DEPT_MNG`)는
참여기관이 아니라 **사업부서**이고, TO-BE에 이미 `departments.html`(94줄)이 있었다.
즉 신규 구현이 아니라 **보강 건**이었다(AS-IS 538줄 vs TO-BE 94줄).

**donation 신규**
- `GET /api/designated-projects/admin/departments/search` - AS-IS `selectDsgncntrPartMngList`
  조건 4종(지자체·부서명·사용유무·등록일 범위), 정렬 부서ID 내림차순
- `GET /api/designated-projects/admin/departments/{deptId}` - 단건(수정화면용)
- `POST /api/designated-projects/admin/departments/{deptId}` - **수정**(AS-IS `updateDsgncntrPartMng`)
- `DepartmentDto`에 `createdDate` 추가(AS-IS 목록의 "등록일시" 칸, `yyyy-MM-dd HH:mm`)

**admin 신규/수정**
- `web/support/DepartmentSearchParam` 신규
- 컨트롤러: `GET/POST /designated-projects/departments(/search)` · `GET /departments/form/{deptId}` ·
  `POST /departments/save`(등록·수정 한 엔드포인트, AS-IS `/part/save`와 같은 모양)
- 템플릿: `designated/departments.html` **전면 재작성**, `designated/department-form.html` **신규**
- `DesignatedProjectClient`: `searchDepartments`·`department`·`updateDepartment` 추가

**AS-IS 그대로**
- 제목 "사업부서 관리" · 검색 4종(**지자체 칸은 ROLE_ADMIN_1~4에게만**) · 날짜버튼 5종
- 컬럼 5개: ID / [지자체 명] / 부서명(수정 링크) / 사용유무 / 등록일시.
  지자체 칸은 코드가 아니라 **"광역명 시군구명"**
- 하단 버튼은 **[신규등록] 하나**(`form/0`)
- 폼: 지자체는 **최고관리자만** 변경 가능(그 외는 자기 지자체 고정) · 저장 문구 등록/수정/실패 그대로
- **부서정보 가드**(`checkDsgncntrAuthPartInfo`)를 목록·폼·저장 전부에 적용(배너와 같은 근사)
- **AS-IS 비활성 유지**: 체크박스 컬럼과 일괄 사용유무 변경은 JSP에서 전부 주석처리 상태
  (`updateListDataLabel` 함수만 남아 있고 버튼이 없다) → 켜지 않았다.
  그래서 **사용유무는 수정화면에서 바꾼다**(TO-BE에 있던 목록 toggle 버튼은 AS-IS에 없어 뺐다).

**시드 추가**: `database/ddl/seed-donation-designated-dept.sql` **적용** - 부서 5건
(강남구 2 / 서천군 1 / 해운대구 1 / 순천시 1(미사용)). 표가 0행이라 화면 확인이 불가했고,
사업 등록폼의 "사업부서" select도 "-부서 미지정-"만 보였다. 이것으로 **③에서 남긴 갭 2번이 해소**된다.

**검증**: 검색조건 4종 실측(지자체 11680→2 · 광역 11000→2 · 부서명 '청소년'→1 · 사용유무 N→1 ·
등록일 최근7일→1) · 수정 경로 트랜잭션 검증 후 롤백(부서명·사용유무·최종수정자 반영, **등록일시 불변**).
admin·donation `bootJar` EXIT=0.

### 2026-10-06 아침 - 기동 실패 2건 수습(내 결함)

사용자가 아침에 재기동하자 **donation이 기동 실패**했다. 원인은 ③에서 내가 넣은 순환참조다:
`DesignatedAdminService`가 사업구분 라벨을 얻으려 `DonationService`를 주입했는데, `DonationService`는
이미 `autoCloseIfGoalReached` 때문에 `DesignatedAdminService`를 주입하고 있었다.
`bootJar`는 EXIT=0이라 못 잡았다 → **코드표 조회는 `CommonCodeRepository`를 직접 주입**해 끊었다
(`DesignatedAdminService.codesOf` private 메서드). [[build-is-mine-restart-is-users]]에 검증 절차 추가.

같은 아침 로그에서 **팝업관리 목록 500**도 발견했다(특정사업과 무관, 시스템관리 영역).
`(:query is null or ... like concat('%', :query, '%'))`의 파라미터가 bytea로 바인딩돼 PostgreSQL이
거절한 것 - admin 5개 리포지터리에 `cast(:x as String)` 적용. 상세는 [[hql-null-param-needs-cast]].

**검증(여분 포트 기동 + 내부 API 직접 호출)**: donation 8092 / admin 8099 모두 `Started ...Application`.
`GET /search` 요약 = **5건 / 16,662,000원**(②의 교정치와 일치) · `bsnsType=100` 필터 정상 ·
부서 검색 전체/지자체/사용유무/부서명(UTF-8) 정상 · `{id}/stat`·`departments/{id}` 정상.
admin·donation `bootJar` EXIT=0, 임시 인스턴스는 모두 내렸다.

### 다음: ⑥ 권한요청(request, 1,174줄) → ⑦ 미리보기(632줄) → ⑧ 통계 2화면 잔여
**재기동 대기**: donation은 2026-10-06 09시 현재 **내려가 있다**(위 기동 실패 후). admin은 떠 있지만
팝업 수정분이 안 들어간 옛 jar다. 사용자가 재기동한 뒤 눈으로 볼 목록 -
5112 답변폼 / 5104 FAQ / 5201·5202 매뉴얼 / 5102 1:1문의 / 17101 목록·등록폼·사업부서 관리 /
**팝업관리 목록(검색어 비운 상태)**.
목록의 **엑셀 다운로드·미리보기**도 아직 안내만 띄우는 상태다(원장 ② 참고).
**③에서 남긴 갭 1번(사업부문 코드표 없음)은 여전히 데이터 결정 대기**다.

### 참고: AS-IS 목록(17101) 요구사항 명세 - ②에서 **전부 반영 완료**
(아래는 ② 착수 전에 뜬 요구사항표다. "donation API 확장이 선행된다"고 적어 두었던 것은
②에서 `GET .../admin/search`·`POST .../admin/bulk-update`·`GET .../admin/{id}/stat`을 만들어
해소되었다 - 다시 선행과제로 읽지 말 것. 남은 것은 **엑셀·미리보기**뿐이다.)
- 검색 6종: 지자체(광역+시군구, ROLE_ADMIN_1~4만 노출) · 사업구분(`DSGN_BSNS_TYPE`, 라벨은 `detail`) ·
  기간(prjStDt~prjEdDt + 날짜버튼 오늘/1주일/한달/3개월/1년) · 사업명 · 상태 라디오(전체/2 진행/9 종료/1 대기) ·
  공개여부 라디오(전체/Y/N)
- 요약 집계표 4칸: 총 목표금액 / 총 모금액 / 총 모금 달성율 / 총 기부건수
- 컬럼 16개: 체크박스/ID/[지자체명]/모금상태/사업구분/사업명(이미지+링크)/기부건수/시작일/종료일/
  목표금액/모금액/달성율/공개여부/사업부서/공지사항(버튼)/승인이력(버튼)/미리보기(버튼)
- 출력수 **10/50/100/200/500**(다른 화면과 다르다)
- 하단 일괄처리 4종: 진행(승인처리)='2'[ROLE_ADMIN_1~5만] / 종료='9' / 공개='Y' / 비공개='N'
- 엑셀 다운로드(`/list/excel-download`)
- 모금상태 표기: '1'→승인 대기, '2'→진행, '9'→종료
