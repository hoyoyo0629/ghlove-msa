---
name: admin-dashboard-day-port
description: 통계>보고서>일일 현황보고(shop-statistics/dashboard/day) AS-IS 이식 - 표 3개는 AS-IS도 빈 틀, 실기능은 행안부 일일/주간보고 JSON→문안·엑셀·복사 버튼
metadata:
  type: project
---

2026-10-14 이식. 이전 TierB 구현(결제건수·포인트·취소·신규가입 표)은 AS-IS에 없는 발명이라 통째로 교체
(`dashboardDay`·`dashboardDayReport` 제거, 월별 `dashboard/month`는 유지).

- AS-IS 화면 자체가 거의 빈 틀: 표 3개는 모델에 없는 `${total}`을 읽어 값 칸이 전부 공백, "2024년/2023년" 하드코딩,
  차트 canvas는 그리지도 않음, 검색 onclick `fnSearch()`는 정의 없음(옮기지 않음). 그대로 비워 둠.
- 실제 기능은 버튼: 초기화 / 검색(POST) / 엑셀(`Shop.downloadExcelOrder(url, startDate, false)` → 키 없는 `?20261014`
  쿼리스트링, 양식 틀+신청건수·판매액 "0"만) / 행안부 일일보고·주간보고(`daily.report.js`가 `/report/daily/{date}`,
  `/{start}/{end}` JSON을 받아 문안 조립 → textarea) / 복사.
- ★종료일 input은 `id="endDate"` 필수 - daily.report.js 생성자가 전역 `endDate`를 읽음(AS-IS는 form:input이 id 자동부여).
- ★daily.report.js의 fetch URL `/opmanager/shop-statistics/...` → `/shop-statistics/...`로 TO-BE 사본만 패치(admin에 /opmanager 매핑 없음).
- 구현: admin `ShopDashboardDayController`(화면·JSON·엑셀)·`DailyReportService`(조립), donation/point/member 각
  `ReportDailyAdminApiController`(`/api/admin/report/daily-*` 집계), admin `DonationClient/PointClient/MemberClient` 메서드 추가.
- 검증(2026-09-30 기준): 누계 31건/22,482,000원, 일반(온라인)21건·지정5건·오프라인5건/110,000, 답례품 신청 13건/461,000P,
  회원 39(일일)/45(주간 전체) 모두 DB 직접조회와 일치. 엑셀 POI 복호화로 양식 확인.
- 근사/공백: `itemRegCount`=지자체 있는 gift 품목 수(AS-IS giftA+B 근사), `admin.op_call` 비어 있어 콜수 0, 민간플랫폼(LINK_INSTT_CD) 데이터 없어 0.
- admin+donation+point+member 재기동 필요. 커밋 안 함(요청 시에만).
