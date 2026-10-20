---
name: admin-port-progress
description: admin 영역·화면별 이식 진척 한 줄 요약(시스템관리·회원관리·고객센터·소규모영역·특정사업·기부금관리·통계 매출/보고서/외국인/접속/관심지자체·답례품관리 카테고리/그룹/제공자/대표배너/답례품·이벤트·메인·로그인·주문 수기등록·ISMS·전수 parity). 상세 원장은 docs/as-is-parity.md 22절
metadata:
  type: project
---

영역별 이식 원장은 2026-10-20부터 `docs/as-is-parity.md` **22절**(제목 = 아래 이름)에 있다. 이식을 진행하면 그 절을 고치고, 이 파일에는 상태 한 줄만 고친다. 새 영역은 22절에 `###` 절을 추가한다.

- **admin-system-area-port-progress** — 시스템관리(1000>1400) 영역 25화면 AS-IS 이식 원장과 진척 - 팝업관리·공통코드 완료, AS-IS JSP 경로 매핑표, 발견된 구조 갭(연계로그 4화면→1화면)
- **admin-member-area-port-progress** — 회원관리 영역 8화면 AS-IS 이식 **완료**(7화면 이식 + 4701 이식대상없음) - AS-IS JSP/컨트롤러 매핑표, 구조갭(4402·4501) 해소, 담당자 사용여부 9/2↔ACTIVE/LOCKED 변환 규칙
- **admin-customer-center-area-port-progress** — 고객센터 영역(11화면) 이식 원장 - AS-IS 대조표·구조갭 6건·TO-BE 축소 실측
- **admin-small-areas-port-progress** — 소규모 메뉴영역 묶음 이식 원장(기부혜택증3·오프라인기부금접수2·대시보드1·커뮤니티5) - 사용자가 정한 순서, 구조갭·AS-IS 매핑
- **admin-designated-donation-area-port-progress** — 특정사업 기부 관리(17000) 영역 이식 원장 - AS-IS 대조표·갭 실측·구현순서. 기부금관리/통계를 뒤로 미룬 이유 포함
- **admin-give-state-area-port-progress** — 기부금관리(14000) 영역 AS-IS vs TO-BE 4축(JSP+매퍼+컨트롤러+ServiceImpl) 대조 분석 완료, 구현은 아직 착수 전(2026-10-12) - 재개 시 참고용 상세 스펙
- **admin-sales-day-statistics-port** — 통계>일자별 매출(shop-statistics/sales/day) AS-IS 이식 - 17칸 2단헤더·취소 음수 규칙·주문방법 축은 TO-BE에 데이터가 없어 "-
- **admin-report-general-port** — 통계>보고서>총괄 현황(shop-statistics/report/general) AS-IS 이식 - 사전집계 통계표 14종을 AS-IS 이름 그대로 신설·적재, 탭4개 화면+14시트 엑셀. 통계 배치는 TO-BE에 없음
- **admin-dashboard-day-port** — 통계>보고서>일일 현황보고(shop-statistics/dashboard/day) AS-IS 이식 - 표 3개는 AS-IS도 빈 틀, 실기능은 행안부 일일/주간보고 JSON→문안·엑셀·복사 버튼
- **admin-foreigner-give-stat-all-port** — 통계>외국인 기부 현황>전체(6711) 이식 - 기부금 모금현황 전체 화면에 foreignerFlag 분기, 메뉴 URL 누락이 원인, AS-IS 검색(POST) 405 결함 해소
- **admin-visit-stats-port** — 통계>접속통계(stats/visit/index) 신규 이식 - OP_VISIT_COUNT 1,096행 적재, 메뉴 URL을 AS-IS 값으로 교정, 접속 수집 쪽은 아직 없음
- **admin-interest-locgov-port** — 통계>관심지자체(statistics/interest-locgov) AS-IS 전면 재구현 - 이전 구현은 텍스트검색·"전체연도"·발명된 막대그래프 등 구조가 전혀 달랐다
- **admin-categories-port** — 답례품관리>카테고리관리(16101) AS-IS 이식 - OP_CATEGORY 1~4차 트리(jsTree 드래그 이동)를 gift.public.op_category에 올림, 메뉴 URL을 /categories로 교체, 알려진 한계(GNB는 gift_subcategory를 읽음)
- **admin-categories-team-group-port** — 답례품관리>카테고리 그룹 관리(16102) AS-IS 이식 - 대그룹+1차 카테고리 그룹 목록/등록/수정/삭제/정렬순서변경, 메뉴 URL→/categories-team-group, 안 옮긴 AS-IS 도달불가 기능, 알려진 한계
- **admin-seller-manage-port** — 답례품관리>답례품제공자 관리(16151) AS-IS 이식 - 목록/등록/수정 + 계정(OP_SELLER_USER) 같이 저장, 사용자 결정 2건(계정 AS-IS처럼·실데이터 9,229건 보류), 안 옮긴 것(shadow 로그인 등), 알려진 한계
- **admin-representative-banner-port** — 답례품관리>대표배너관리(16251) AS-IS 이식 - G_REPRST_BANNER PROCESS_TYPE=ITEM, 지정기부 배너와 서비스 공유, 옛 /admin/main-banners 삭제, 이미지 크기 맞춤(ThumbUtils) 추가
- **admin-item-manage-port** — 답례품관리>답례품관리(16301) 목록 AS-IS 이식 - 목록/일괄처리/삭제신청/변경사항팝업/지자체 대표·오프라인 대표 답례품. 등록·수정 폼과 엑셀은 미이식, 상태코드 숫자 전환 포함
- **admin-featured-event-port-progress** — 이벤트 관리(featured, 메뉴 16601 /admin/featured) AS-IS 이식 진척 - OP_FEATURED=지자체별 이벤트(class0/type1). 목록 완료, 등록/수정 폼 잔여
- **admin-main-dashboard-asis-port** — 관리자 메인(/admin) AS-IS main/index.jsp 재이식(2026-10-15) - 발명 위젯 제거, 권한별 위젯·5개 서비스 집계 API·콜 등록 팝업·로그인 직후 오프라인 담당자 안내. REPORT_DASHBOARD는 배치 미이식이라 0
- **admin-login-asis-parity-2026-10-14** — 관리자 로그인(/admin/login) AS-IS login_main.jsp 재이식 - 결과코드 응답·로컬 인증번호 자동입력·권한요청 진입·잠김화면·비밀번호 변경 팝업·관리자 공지 팝업·로그인 국민비서 문자. 남은 판단(EMPTY_CI) 포함
- **admin-order-manual-registration-port-progress** — AS-IS '관리자 주문 수기/엑셀 등록'(OrderAdminServiceImpl) 기능을 TO-BE에 포팅, 7210 정합성검증이 보는 AB코드 주문 생성경로 확보(2026-10-08)
- **admin-isms-config-port-progress** — 시스템관리 > ISMS관리(/isms-config) AS-IS 동기화 완료(2026-10-07) - 발명 시드 제거, 실데이터 12건+정렬 버그 수정
- **as-is-parity-audit-progress** — AS-IS↔TO-BE 전수 parity 대조 진척: 6개 서비스(donation·member·point·gift·order·admin) 모두 완료. 남은 것은 common 버킷 + 영역별 심도검증 + 갭 구현
