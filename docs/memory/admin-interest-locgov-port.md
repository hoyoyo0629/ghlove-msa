---
name: admin-interest-locgov-port
description: 통계>관심지자체(statistics/interest-locgov) AS-IS 전면 재구현 - 이전 구현은 텍스트검색·"전체연도"·발명된 막대그래프 등 구조가 전혀 달랐다
metadata:
  type: project
---

# 통계 > 관심 지자체 (AS-IS `/opmanager/statistics/locgov/like`)

2026-10-14 이식. TO-BE에 있던 구현은 **발명된 축소판**이었다 - 목록·상세를 하나의
`ListResult`(지자체별+월별 합침)로 합치고, 텍스트검색 "지자체명 검색"·"전체 연도" 옵션·
div 막대그래프(`background:#eee; width:200px`) 같은 AS-IS에 전혀 없는 UI를 썼다.
AS-IS 소스(컨트롤러 219줄 + list.jsp 177줄 + detail.jsp 130줄 + ServiceImpl + 매퍼 XML)를
전수 대조해 다시 만들었다.

## AS-IS 사양

**목록(`like`)**
- 지자체 2단 select(WDR→LOCGOV_CODE 캐스케이딩, 텍스트검색 아님) · 초기화/검색 버튼
- `count_title`(총 N건) + `itemsPerPage` select(10/20/50/100, AS-IS 공용 패턴)
- 표 3칸: No./지자체("광역명 시군구명")/등록수, **정렬은 등록수 DESC**
- 지자체명 클릭 → 그 지자체 상세로 이동(연도 무관, AS-IS는 ajax 아니라 `location.href`)
- 좌측 엑셀 버튼(`Shop.downloadExcelOrder(..., true)` = 개인정보 사유 모달 경유)
- 표준 페이지네이션 조각
- ★목록 쿼리(`getLocgovLikeList`)에는 **연도 조건이 없다** - 전체 기간 누적. 연도 select는
  상세 화면에만 있다.
- ★**지자체담당자(scoped)는 목록 자체를 못 본다** - GET/POST 진입 즉시 자기 지자체 상세로
  리다이렉트(`findAdminRoleAndLocgov(null,null)` → `ac.getIsLoc() && ac.isPass()`).

**상세(`like/{locgovCode}`)**
- [목록] 버튼은 **지자체담당자에게는 안 보인다**(`adminRole != 'LOC'`)
- 년도 select(YYYY 공통코드, 매출통계와 같은 함정) + 검색
- 요약카드 2개: **총 등록수(현재기준, 연도 무관 전체)** / **년도별 등록수**(선택 연도만) -
  이름이 비슷해 헷갈리기 쉽지만 분모가 다른 두 숫자다
- Chart.js bar 1개: 월별 등록건수, ajax `POST like/{locgovCode}/{year}`
- ★**다른 지자체 상세를 직접 URL로 접근하면 막힌다**(지자체담당자가 자기 지자체가 아닌
  곳을 보려 하면 차단+리다이렉트)
- ★월별 집계는 AS-IS 서비스가 **1~12월을 전부 채워서** 돌려준다(매출통계와 같은 함정 -
  없으면 차트 x축이 데이터 있는 달만 찍힌다)

**엑셀**: 시트명 `locgov_like`, 제목 "관심 지자체", 헤더 No./지자체/등록수,
파일명 `관심_지자체_yyyyMMddHHmmss.xlsx`, 페이징 없이 전체.

## 구현

- **donation** `InterestLocgovStatsService` 전면 재작성 - `search(shLocgovCode, offset, limit)`
  (목록, COUNT DESC 정렬 후 메모리 페이징) · `totalOf(locgovCode, year)`(총/연도별 분리) ·
  `monthlyOf(locgovCode, year)`(1~12 채움). `InterestLocgovRepository.findByLocgovCode` 신규.
  API 컨트롤러 전면 재작성(`/api/admin/interest-locgov-stats`, `/{code}/total`, `/{code}/{year}/month`).
  기존 `availableYears()`(목록의 "전체 연도" 셀렉트용)는 상세가 YYYY 공통코드를 쓰게 되며
  더 이상 필요 없어져 제거.
- **admin** `StatisticsLocgovClient` 전면 재작성(목록/총계/월별 3개 메서드로 분리).
  `StatisticsLocgovController` 전면 재작성 - GET/POST 목록·상세, ajax 월별, 엑셀.
  `MenuService.isLocgovScoped` 로 지자체담당자 리다이렉트/차단 재현(AS-IS `LocManagerCheck` 대응).
  `PrivacyAccess.GIVE_STATISTICS_LIKE` 에 TO-BE 경로(`/statistics/interest-locgov/excel`) 추가
  (엑셀이 개인정보 사유 모달을 거치므로 등록 안 하면 사유 저장이 거부된다 - 매출통계 때와
  같은 함정, [[admin-excel-download-real-xlsx-progress]] 패턴 참고).
- **템플릿** `statistics-locgov/{list,detail}.html` 전면 재작성.

## 검증 (Playwright, 여분 포트)

- GET 목록: 조회 안 함(0건), 시도 select 18개, 표3칸, 안내문구, 출력수 4종, 엑셀버튼,
  페이지네이션 조각 - 전부 확인
- 검색(조건없음): 총 11건, 1페이지 10행 + 2페이지 1행(itemsPerPage 분할 정상), **등록수
  내림차순** 확인
- WDR 캐스케이딩: 전남광주통합특별시 선택 → 시군구 29개 정상 로드
- 상세(11230, 기본 2026년): 카드1="1"(총) 카드2="0"(2026년 등록 없음) - DB `regist_de=20250115`
  와 일치. **2025년으로 재검색** → 카드2="1", 월별차트 1월=1·나머지 0으로 정확히 찍힘(1~12
  채움 로직 확인)
- 지자체담당자(pwtest_loc, 11230) 목록 접근 → **자동으로 `/11230` 상세로 리다이렉트**,
  [목록] 버튼 숨김 확인 · **다른 지자체(26350) 상세 직접 URL 접근 → 차단**되어 다시
  `/11230`으로 돌아옴
- 엑셀: 사유모달 정상 경유 → 다운로드 `관심_지자체_....xlsx`(9,216바이트) → **POI로 복호화**
  (비밀번호=로그인ID) → 시트 `locgov_like`, 제목 "관심 지자체", 헤더 `No. | 지자체 | 등록수`,
  11건 전부 담김(마지막행 12 = 제목+헤더+11데이터) 확인
- 콘솔/HTTP 오류 0건(엑셀 다운로드 중 뜨는 "다운로드가 진행 중입니다" 안내는 기존 공용
  UX, 오류 아님)
- donation·admin `test`+`bootJar` EXIT=0
