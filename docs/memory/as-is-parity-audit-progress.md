---
name: as-is-parity-audit-progress
description: "AS-IS↔TO-BE 전수 parity 대조 진척: 6개 서비스(donation·member·point·gift·order·admin) 모두 완료. 남은 것은 common 버킷 + 영역별 심도검증 + 갭 구현"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-01T09:33:25.346Z
---

**서비스별 전수 parity 대조(방식 [[as-is-parity-exhaustive-audit-method]]) 진척.**

- **완료 6/6 서비스**: `docs/{donation,member,point,gift,order,admin}-parity-audit.md`.
  - donation·member·point·gift·order = 2026-09-21~22.
  - **admin = 2026-09-23** (`docs/admin-parity-audit.md`). 09-03 심층감사(part1/2, 133 컨트롤러)를 현재 admin 실물(컨트롤러 51→**103개**)로 재대조 — 당시 "진짜미구현" 대부분이 이미 구축돼 gross 갭은 대부분 닫힘.
- **admin 잔여 순수재현 갭**: 카탈로그 관리(中), 설정허브 잔여 서브화면 ~10종(中~高), 판매자포털 경계정리. 저우선: 공통메시지·SmsConfig·제철·특산·만족도조사·전자서명(MagicLine)·배치로그.
- **[2026-09-23 정정] FAQ 관리자 CRUD는 오탐**: 이미 `LocgFaqAdminController`(`/community/locv-faq`)가 스토어프론트와 같은 `OP_COMMUNITY_LOCGOVFAQ`(일반FAQ, ≈63건)에 CRUD. `OP_FAQ`는 0행·무참조 고아. 잔여 debt: 고아 OP_FAQ 정리·명명혼선·지자체별(locgovCode) FAQ 별개기능 여부 확인.
- **admin 심도검증 V1~V8 전건 완료(2026-09-23)**: V1 설정허브=**O**(SiteConfigExtController가 가입거부·결제·배송희망·PG·전환태그·GA·주문임시 전부, 랭킹=RankingAdmin; 전역 포인트정책만 확인·低), V2 클레임메모=부분O(통합목록뷰만·低), V3 카테고리그룹 신O/구 회원그룹=X(legacy축소·低), V4 로그인배너=**O 흡수**(Banner.bannerType), V5 상품문의=O/내부문의=**X 진짜갭**(中), V6 판매자포털=별도 seller audit 위임(admin전용 구현·셀프이관 보류), V7 방문통계=**X**(접속로깅 인프라 선결·中), V8 지자체공지 RBAC=**O 해소**(isLocgovScoped 필터+effectiveLocgov+requireOwnLocgov).
- **admin 최종 순수재현 갭(구현대상)**: ~~내부문의~~(**2026-09-23 구현완료·재기동 대기**), **방문통계(中·로깅인프라 선결)**, **카탈로그(中·DB이관 선결)**, 저우선(회원그룹·클레임메모뷰·공통메시지류·전자서명·배치로그). FAQ(G1)는 오탐→O.
- **내부문의 구현(2026-09-23)**: `InternalInquiryAdminController`(/admin/internal-inquiry) + QnaAdmin/QnaAdminAnswer 엔티티·InternalInquiryService·템플릿 internal-inquiry/{list,form}. G_QNA_ADMIN/ANSWER 시퀀스 생성, OP_MENU 1903(부모100)+RIGHT(5/6) 시드, 테스트문의 1건(qna_admin_id=1, locgov 11230). 첨부·발송알림은 미구현(추후). 컴파일 OK, **admin 재기동 후 활성**. 검증: 본사계정 /admin/internal-inquiry 목록→답변, 지자체담당자계정 문의작성.
- **admin 셸(메뉴트리+메인대시보드) parity 착수(2026-10-01)**: AS-IS opmanager는 상단 GNB+좌측 LNB를 100% OP_MENU DB로 렌더(3단: GNB→섹션→링크), 상단 노출 11개(+숨김 10개 SalesOn잔재). TO-BE는 임의 7그룹·2단이라 전면 재편 필요. 사용자 결정: 갭화면 **이번에 신규구현**, 통합화면 **AS-IS대로 분리노출**(최대충실도).
  - **1라운드 백본 완료(컴파일OK, 재시드 미적용·재기동 대기)**: ① AS-IS 덤프(`OP_MENU_*`/`OP_MENU_RIGHT_*`, 355행)를 `asis_dump` 스키마 적재→조상인지 가시트리 **172행** 추출. ② 매핑/갭 전수 = `docs/admin-menu-tree-parity.md`(99개 url매핑). ③ 재시드 SQL `database/ddl/migration-admin-menu-asis-reseed.sql`(182메뉴+485권한+내부문의5120+op_menu_id_seq→30000), BEGIN/ROLLBACK 검증통과. ④ `MenuService.navTreeFor` 3단화+`topMenuIdOf`, `ManagerAuthAdvice.activeTopMenuId`=조부모, `fragments/admin-nav.html` GNB(topFirstUrl)+LNB(섹션→리프) 3단. menu_id는 AS-IS 원본 verbatim.
  - **적용법(사용자)**: `migration-admin-menu-asis-reseed.sql`을 psql로 실행 + admin 재빌드·재기동(둘이 짝. 적용만 하고 구 jar면 2단 템플릿이 3단 데이터를 잘못 그림). 내부문의는 1903→5120(고객센터>문의관리)로 이동.
  - **2라운드+ 대기**: ❌ 진짜 재현갭 화면 신규구현(특정사업 통계/배너, 외국인기부, 만족도, 기부혜택증 열람현황, 방문자경로, 배송업체/온라인입금/카드혜택, 기금사업소개, 답례품Q&A/제철/특산/미완료, 배치로그/공통메시지) + 통합화면(주문/클레임/정산)에 상태필터 딥링크. TO-BE 전용 orphan화면(open-api·sla·point-history 등) 메뉴 배치 결정. 메인 대시보드(main/index.jsp) 재현 별도.
  - **코드데이터 원본**: [[asis-table-dump-path]].
- **전수 커버리지 원장(파일단위 back/front/static) 완료(2026-10-01)**: `docs/coverage-ledger.md`. 6개 스토어프론트 버킷(member·donation·point·gift·order·common) 3축 점검 끝. ★정본 back=ghlove-api(53개), front=ghlove-frontend(둘 다 SPA티어; ghlove-web/shop은 SalesOn레거시). admin은 메뉴트리 라운드로 별도.
  - **재현갭(이식대상)**: ①donation 납부게이트웨이([[donation-payment-gateway-port-decision]], 이식결정) ②common 통합검색(totalsearch) ③gift/admin **특산물·소식지/카탈로그·카드뉴스** = AS-IS 비활성(프론트진입점0·소식지admin숨김·데이터0·특산행안부권한만)이라 **이식+TO-BE도 숨김재현**([[as-is-parity-includes-disabled-state]], 2026-10-01 사용자결정) ④admin 메뉴트리 ❌갭들(특정사업통계/배너·외국인기부·만족도·열람현황·방문자경로·배송업체/온라인입금/카드혜택·기금사업·답례품Q&A·배치로그/공통메시지).
  - **보류/도메인N/A(갭아님)**: order PG(포인트단일결제)·point예약·policy원문DB이관.
  - **저위험 TODO**: member 전자서명, donation featured뷰, gift community-business/store back.
- **A라운드(admin 메뉴트리 2라운드) 진행중**: item-4 admin갭 재확인 결과 상당수가 **오탐(화면은 구현됨·메뉴만 미배선)**.
  - **B1 완료(2026-10-01, DB배선만·재기동불요)**: `migration-admin-menu-B1-wire.sql` — 답례품Q&A(16552→/admin/gift-inquiries)·배송업체(1305→/admin/delivery-companies)·특정사업 지자체별/월별통계(17201·17202→/designated-projects/analysis)·특정사업 배너관리(17301→/designated-projects/banners)·기부혜택증 열람현황(19103→/admin/honor-users/view-history) display='Y'로 노출. 권한·컨트롤러 기존.
  - **B2 신규구현**: 만족도조사(5108 cntnts-stsfdg)=**완료**(ContentSatisfaction* + AS-IS 동일 Chart.js 스택막대, 재기동대기). 잔여: 메세지관리(1402 공통메시지)·기금사업 등록/관리(14201 give-notice, 재확인).
  - **B5 통계화면 AS-IS차트 재현(2026-10-01 착수)**: TO-BE 통계화면들이 AS-IS 차트를 표/단순화로 대체해둔 걸 AS-IS대로 교정하는 라운드([[copy-asis-css-js-assets-verbatim]]). **지정기부 월별통계=완료**: donation `DesignatedStatRepository`(네이티브 3종 campaign/amountraised/amount, CUBRID→PG 포팅·라이브검증 통과)+`DesignatedStatService`+API 3종; admin `DesignatedProjectClient` 3메서드+컨트롤러 `/designated-projects/analysis/month`+데이터3종({isSuccess,data})+템플릿 `designated/analysis-month.html`(AS-IS month.jsp verbatim: pie2+stacked bar1+표3, jquery+op.common+chart.min+op.chart 로드). 메뉴 17202→/analysis/month 재배선(적용됨). 스키마 치환: 확정기부=COMPLETED(STTEMNT_PAY_DE 미사용), 사업상태 OPEN/CLOSED→쿼리내 1/2/9 환산, 조인키 DSGN_DNTN_BIZ_ID. 상세 `docs/b5-designated-month-stats-spec.md`. **월별통계는 재기동 후 정상 확인됨(2026-10-01)**. 그 과정에서 두 함정 해결: spin.min.js 미로드(Spinner 예외)·CSRF meta 누락([[admin-opmanager-ajax-needs-csrf-meta]]).
    - **지자체별통계(17201)=완료(2026-10-01)**: AS-IS analysis/locgov.jsp(차트 없는 서버렌더 표+요약) 이식. donation `DesignatedStatRepository.selectLocgovSummary/selectLocgovList`(요약 4종+지자체별 집계, 라이브검증: 총목표 26.9억·총모금 16,662,000·달성율 0.61%·기부건수 5)+`DesignatedStatService.locgovSummary/locgovList`+API 2종(/analysis/locgov/summary·/list). admin `DesignatedProjectClient.locgovStatSummary/locgovStatList`+컨트롤러 `/designated-projects/analysis/locgov`(메모리 페이징·지자체담당자 자기locgov 강제)+템플릿 `designated/analysis-locgov.html`(필터 지자체/사업구분/기간+퀵버튼/상태, 요약표, 지자체별 집계표, 페이징, 행클릭 딥링크; jquery+op.common+CSRF meta). 메뉴 17201→/analysis/locgov 재배선(적용됨). 양쪽 bootJar 빌드OK, **재기동 대기**.
    - **기부통계 '전체'(6706)=완료(2026-10-01)**: AS-IS give/statistics/all/detail.jsp(요약3카드+Chart.js 5종: 월별 건수 line·online/offline stacked bar·인원 line·금액 line·시간대별 line) 이식. donation `GiveStatRepository`(allSummary/allByMonth/allByHour)+`GiveStatService`+`GiveStatAdminApiController`(/api/give-statistics/admin/all/{summary,{year}/month,{date}/hour}, 내부시크릿보호 경로 추가). admin `GiveStatClient`+`GiveStatisticsController`에 `/give-statistics/all` 페이지(요약 서버렌더)+월/시간 ajax({isSuccess,data})+템플릿 `give/give-statistics-all.html`(op.chart+jquery+op.common+spin+CSRF meta). 라이브검증: 2026 요약 31건/9명/22,482,000원. 메뉴 6706→/give-statistics/all 재배선(적용). bootJar OK, **재기동 대기**. 스키마치환: 연=substr(cntr_de,1,4)(STTEMNT_PAY_DE NULL이라), 확정=COMPLETED, 시=substr(frst_regist_pnttm,9,2), online=path '100'/offline '200','300'.
    - **기부통계 '전체' 차트 미표시 미해결(2026-10-01 저녁)**: 재기동 후 요약은 떠도 Chart 5종 안 뜸. 서버측 전부 정상 검증(donation API 200, 스크립트 5종 200, 페이지 렌더). 월/시간 ajax가 서버 미도달(감사로그 0)=브라우저 $.post 전 JS 중단 추정. **사용자가 다음날 F12 Console 에러+Network month 요청 status 확인 예정**. (designated month은 th:inline CDATA였고 이 화면은 plain script인 차이가 단서 후보.)
    - **기부통계 '지자체별'(6707)=완료(2026-10-01)**: AS-IS give/statistics/locgov/list.jsp(차트 없는 서버렌더 표) 이식. donation `GiveStatRepository.locgovList`(연도+지자체 그룹, getGivePersonStatisticsList 포팅)+`GiveStatService.locgovList`+API /api/give-statistics/admin/locgov/list. admin `GiveStatClient.locgovList`+`GiveStatisticsController` `/give-statistics/locgov`(정렬 person/amt/cnt·메모리페이징·지자체담당자 스코프)+템플릿 `give/give-statistics-locgov.html`(필터 지자체/년도, 표 No/년도/지자체/인원/금액/건수, 페이징; 로컬 캐스케이딩, **ajax 없음**이라 전체 화면 버그와 무관). 라이브검증 OK. 메뉴 6707→/give-statistics/locgov 재배선(적용). bootJar OK, 재기동 대기. **보류**: 지자체명 드릴다운(월별 detail), 엑셀.
    - 잔여: 기부통계 **운영현황(6901)**(현재 통합 /give-statistics 유지), shop-statistics report/dashboard/sales, 관심지자체(6801). 기존 `/give-statistics` 통합화면은 6901 분리 완료 전까지 유지.
  - **B3 로깅 선결**: 접속통계(6101)·방문자접속경로(6102).
  - **⏸ 외국인기부통계(6711/6712)**: TO-BE가 "시스템에 내/외국인 구분 자체가 없어 제외" 명시 → 데이터 부재. 사용자 판단 대기.
  - **B4 숨김재현**: 온라인입금계좌(1202)·카드혜택(1203)[결제설정 숨김], 특산물·소식지·카드뉴스[front+back+admin, 숨김].
  - 통합검색(totalsearch)=보류(AS-IS도 서버 비활성·미완성).
- 통합 원장 docs/coverage-ledger.md.
