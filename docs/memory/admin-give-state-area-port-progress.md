---
name: admin-give-state-area-port-progress
description: "기부금관리(14000) 영역 AS-IS vs TO-BE 4축(JSP+매퍼+컨트롤러+ServiceImpl) 대조 분석 완료, 구현은 아직 착수 전(2026-10-12) - 재개 시 참고용 상세 스펙"
metadata:
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-12T00:40:55.820Z
---

# 기부금관리(14000) 영역 - 분석만 완료, 구현 미착수

2026-10-12 사용자가 `/give-state`(14101 기부금모금현황) 화면이 AS-IS와 많이 다르다고 보고 →
4축(JSP+매퍼SQL+컨트롤러+ServiceImpl) 대조 상세 분석 진행. **이번 세션은 분석만 하고
구현은 보류** - "정리해놨다가 나중에 작업하기 전에 참고하자"는 사용자 지시로 마무리.

## 메뉴 구조

```
14000 기부금관리(top)
 └14100 기부금모금현황(section)
    ├14101 기부금모금현황      /give-state         ✅ 있음(이번에 분석한 화면)
    ├14102 기부포인트현황      /give-point         있음(미대조)
    ├14103 기부금전체현황      /give-state         ⚠ 14101과 URL 중복 - AS-IS는 별도 화면
    ├14104 기부금 운용정보     /give-operation     있음(미대조)
    └14105 기부금·포인트 변경  /give-reqmng        있음(미대조)
 └14200 기금사업소개(section)
    └14201 기금사업 등록/관리  (menu_url 빈값)     ❌ LNB에서 섹션 전체가 사라짐(아래 참고)
```

## "기금사업소개" LNB 누락 원인 (확정, RBAC 아님)

`MenuService.navTreeFor()`가 섹션의 leaf 중 `menuUrl`이 있는 것만 모아 0개면 섹션째
건너뛴다. 14201의 `menu_url`이 빈 값이라 14200 섹션 전체가 LNB에서 사라진 것.
status_code/display_flag/권한행은 전부 정상.

AS-IS 원본에서 14201의 실제 URL은 `/opmanager/give/give-notice/list`
(`GiveNoticeManagerController`, `@RequestProperty(title="기금사업소개")`) -
`boardCode="DONATION"`인 공지글을 관리하는 CRUD 화면(스토어프론트 "기금사업소개" 페이지에
뜨는 지자체 공지 콘텐츠를 관리자가 작성). **TO-BE에 이 화면 자체가 없다** - 과거 리시드 때
"TO-BE 화면 미구현"이라 url을 비워둔 채 방치된 상태. 즉 **신규 구현 대상**(기존 화면 보강이
아니라 admin에 없는 CRUD 한 벌을 새로 만들어야 함).

## 14101 "기부금 모금현황" - AS-IS 실제 동작 (전부 확인됨, 재조사 불필요)

### 컨트롤러 구조 (`GiveStateManagerController`, AS-IS)
- **역할 분기**: `ROLE_ADMIN_1~4`(시스템/행안부) → `list.jsp`(전체 지자체 조회 가능).
  `ROLE_ADMIN_5~6`(지자체) → `list_locgov.jsp`(자기 지자체로 고정, 완전히 다른 레이아웃).
- **GET은 절대 조회 안 함** - mois 역할은 `isSubmit`이 true일 때만 sum/list/count를
  모델에 채운다(GET으로는 `isSubmit`이 설정될 수 없어 항상 빈 화면). locgov 역할은 GET에서
  `isSubmit` 체크조차 없이 **항상** `sum=new GiveState()`(전부 null)·`list=EMPTY_LIST`·
  `count=0`을 깔아놓는다 - 두 역할 다 결과적으로 GET=빈 화면, POST(검색버튼)=실조회.
  이 프로젝트에서 반복 확인된 "검색화면 GET은 조회 안 함" 관례와 동일 - 버그 아님.
- **년도 코드**: `CodeUtils.getCodeList("YYYY")`에서 `id=="2022"`와 빈 id를 명시적으로
  제외. TO-BE `admin.op_common_code`에 YYYY 코드가 이미 있고 2022/2027~2031은
  `use_yn='N'`으로 로드돼 있어(`migration-admin-common-code-asis-load.sql:1199~1209`),
  **`use_yn='Y'`로만 걸러도 결과적으로 AS-IS와 동일**(2023~2026) - 추가 변환 로직 불필요,
  그냥 `CommonCodeRepository`에서 YYYY/Y만 읽으면 됨.

### 집계 쿼리 (`slave-give-state-mapper.xml`, 확인됨)
- **getGiveStateSum**(합계 카드 4개): `G_CNTR`에서
  `DELETE_AT='N' AND cntr_sttus_code='200' AND sttemnt_pay_de IS NOT NULL` + 선택적
  연도/지자체 필터. `CNTR_AMT`(기부모금액 SUM) / `GIVE_PERSONS`(DISTINCT user_id) /
  `CNTR_POINT`(**GC.CNTR_POINT 컬럼 자체의 SUM** - 발생포인트) /
  `CNTR_USE_POINT`(G_CNTR_USE_POINT 서브쿼리 SUM, use_se_code='1'). **4개 카드뿐, 기부건수
  카드는 없다.**
- **getGiveStateList/Count**(목록): 필터가 **`cntr_sttus_code='200'`만**
  (DELETE_AT·sttemnt_pay_de 조건 없음) - **합계와 목록의 상태필터가 AS-IS 자체에서 불일치**.
  연도×지자체로 GROUP BY, `give_cnt`(COUNT(cntr_sn), 목록 컬럼으로만 존재, 카드 아님) 포함.
  답례품사용금액은 행마다 상관 서브쿼리로 `a.cntr_locgov_code = gc.cntr_locgov_code`만
  필수 조건이고 `year(point_use_de)` 필터는 **검색조건에 연도가 있을 때만** 걸린다 -
  연도 미지정이면 그 지자체의 전체기간 합계가 해당 지자체의 모든 연도 행에 동일하게 반복
  표시됨. **단, 컨트롤러가 GET/POST 둘 다에서 `shCntrYear`가 비어 있으면 당해년도로
  강제 채우기 때문에(`StringUtils.defaultIfEmpty(.., today)`) 사용자가 연도를
  명시적으로 지우지 않는 한 이 반복 증상은 잘 드러나지 않는다.**

### 사용자 결정 (2026-10-12): 위 2가지 버그성 동작(상태필터 불일치, 답례품사용금액 반복)은
**AS-IS 그대로 재현** - 더 "합리적인" 쪽으로 통일하지 않는다.

### 화면 (list.jsp / list_locgov.jsp, 확인됨)
| | mois(list.jsp) | locgov(list_locgov.jsp) |
|---|---|---|
| 검색조건 | 년도(전체 옵션 있음) + 시도→시군구 캐스케이딩(`wdrChange` ajax) | 소속지자체(고정표시) + 년도(전체 옵션 **없음**, 필수) |
| 합계카드 | 기부모금액/기부인원/발생포인트/답례품금액 (4개) | 동일 |
| 목록컬럼 | No./년도/광역지자체/기부지자체/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (9, 지자체명 클릭→detail) | No./년도/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (7, 년도 클릭→detail) |
| 출력수 select | 있음(10/20/50/100) | **없음**(itemsPerPage select 자체가 없음) |
| 엑셀 | `list/download-excel` | `list/locgov/download-excel`(별도 URL, 서버가 `getLocgovCode`로 지자체 강제) |
| 엑셀 헤더 | No./년도/광역지자체/기부지자체/기부모금액/**기부건수**/기부인원/발생포인트/답례품금액 (9) | No./년도/기부모금액/기부인원/발생포인트/답례품금액 (**6, 기부건수 없음** - 화면 테이블엔 있는데 엑셀만 뺀 AS-IS 자체 불일치) |
| 초기화 버튼 | `/opmanager/give/give-state/list`로 이동(검색조건 리셋) | 동일 |

### TO-BE 현재 상태 (수정 전)
- `GiveStateController.list()`(`/give-state`)는 역할분기·GET빈화면 없이 **항상** 즉시
  전체집계 표시, 카드 **5개**(기부건수 카드 추가 - AS-IS에 없는 발명), No.컬럼·페이징·엑셀
  없음, 지자체 역할 전용 레이아웃 없음.
- `GiveStateService.search()`가 `DonationLedger`(STAT_DONATION_LEDGER)와
  `PointLedgerStat`(STAT_POINT_LEDGER) 두 ReadModel을 조인해서 집계 - **발생포인트를
  포인트 원장 자신의 `createdDate` 연도로 그룹핑**하는데, AS-IS는 **기부(G_CNTR) 자신의
  연도**로 묶는다(포인트 적립 시각이 아니라 기부일 기준) - 그룹 키가 다르면 숫자가 달라질 수
  있음. **답례품 사용금액**은 AS-IS의 `G_CNTR_USE_POINT`(지자체×사용일 기준) 개념 자체가
  admin에 없어서 포인트 원장의 TXN_TYPE=USE를 대체로 쓰고 있다 - 의미가 다른 데이터.

### 근본원인 조사 결과 (2026-10-12, 구현 안 함 - 아래 보류 사유 때문)
point 서비스의 실제 원장 `PointLedger`(`PT_POINT_LEDGER`)에는 이미
**`stdrYear`**(기부연도 - 주석에 "AS-IS는 year(G_CNTR.CNTR_DE)로 요약을 묶는다"라고 명시),
**`refKey`**(기부건번호), **`cntrAmt`**(기부금액)가 **있다** - AS-IS parity를 염두에 두고
만들어 둔 필드인데, admin으로 가는 `PointLedgerEvent`/`PointLedgerStat` 미러에는 이 필드들이
빠져 있어 묻혀 있었다. "답례품 사용금액"은 더 심해서 admin 쪽에 대응 개념 자체가 없다
(point의 `g_cntr_use_point` 테이블에서 직접 집계해야 함, point-use-tracking 관련
[[point-use-tracking-g-cntr-use-point]] 참고).

**donation.g_cntr.cntr_point 컬럼은 죽은 컬럼이다** - AS-IS와 같은 컬럼명으로 테이블엔
존재하고 `MemberDonationAdminRepository`/`OffgiveAdminRepository`가 **읽기는** 하지만,
실제 기부 완료 시 **아무도 이 컬럼에 값을 쓰지 않는다**(point 서비스의
`pointService.creditForDonation()`은 point 자신의 원장에만 적립하고 donation 스키마로
역기록하지 않음). 즉 일반회원관리 상세화면 등 **다른 화면에서 이 컬럼을 읽는 누적
발생포인트 표시도 라이브 데이터로는 항상 0에 가까울 수 있다** - 이번에 발견했지만
14101 범위 밖이라 별도 기록만 해둠(추후 점검 필요, [[report-structural-landmines-to-user]]
유형).

## 보류 사유 - RFP vs 기존 동결 결정 충돌 (2026-10-12 확정)

위 발생포인트/답례품사용금액을 AS-IS와 정확히 일치시키려면 ① `STAT_POINT_LEDGER`에
stdrYear/refKey/cntrAmt 컬럼 추가(기존 ReadModel 스키마 확장) ② 답례품사용금액용 신규
ReadModel/이벤트 파이프라인 신설이 필요하다. RFP `SFR-009`는 "대민/관리자/제공자 서비스
조회 기능(**기부내역/포인트**/주문배송/정산/통계 등)을 **ReadModel로 통일된 방식 제공**"을
명시하므로 방향 자체는 맞다. 하지만 **2026-09-22 사용자 결정**
([[defer-readmodels-pending-da-design]]) - "모든 조회모델(ReadModel) 신규/확장 작업은
DA(데이터아키텍트) 설계안이 나올 때까지 전부 보류, 이미 만들어진 건 그대로 둔다" - 가
정확히 이 작업을 막는다. 사용자에게 예외 허용 여부를 물어봤고, **"기존 결정대로 DA
설계안 나올 때까지 보류"로 확정**. 즉 14101의 발생포인트/답례품사용금액 집계방식 교정은
**DA 설계안이 나올 때까지 손대지 않는다.**

## 재개 시 체크리스트 (DA 설계안 나오기 전에도 할 수 있는 것 - ReadModel과 무관)

1. **mois/locgov 역할분기 + GET빈화면/POST조회** 구조로 컨트롤러 재작성
2. **카드 4개로 축소**(기부건수 카드 제거 - 발명이었음), 목록에 No.컬럼·기부건수 컬럼 추가
3. **지자체 역할용 별도 템플릿**(locgov 전용, 출력수 select 없음, 지자체 고정표시)
4. **년도 select**: `CommonCodeRepository`에서 `code_type='YYYY' AND use_yn='Y'` 조회
   (추가 필터링 불필요, 이미 2022/2027~2031이 N으로 로드돼 있음)
5. **시도→시군구 캐스케이딩** ajax 엔드포인트(`LocgovClient` 이미 있음, wdrChange 패턴 포팅)
6. **엑셀 다운로드 2종**(mois 9컬럼 vs locgov 6컬럼 - **엑셀만 기부건수 빠지는 AS-IS 자체
   불일치도 그대로 재현**) - 기존 `ExcelDownloadWriter` 공통유틸 재사용 가능
   ([[admin-excel-download-real-xlsx-progress]] 참고)
7. **14103 "기부금전체현황"**: 현재 14101과 URL이 겹쳐 있음(AS-IS `detail_list`/
   `cntr_status.jsp`는 별도 화면) - 분리 신규구현 필요(이번엔 미대조, AS-IS 컨트롤러
   `GiveStateManagerController.viewTest/overallStatusOfDonations` 참고)
8. **14201 기금사업소개**: `GiveNoticeManagerController` 기준 신규 admin CRUD 구현
   (boardCode=DONATION), 완료 후 `op_menu`의 menu_url 채우기
9. 발생포인트/답례품사용금액은 **위 보류 사유가 풀릴 때(DA 설계안 확정) 다시 논의**

관련: [[defer-readmodels-pending-da-design]] [[admin-designated-donation-area-port-progress]]
(같은 유형의 충돌을 먼저 겪은 영역) [[api-vs-kafka-decision-criteria]]
[[point-use-tracking-g-cntr-use-point]] [[admin-excel-download-real-xlsx-progress]]
[[report-structural-landmines-to-user]] (donation.g_cntr.cntr_point 죽은 컬럼 건)
