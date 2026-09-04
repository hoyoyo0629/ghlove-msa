# AS-IS opmanager → TO-BE admin 심층 갭 감사 (Part 2, 66개 컨트롤러)

Part1(67개)과 동일 방법론으로 나머지 66개 컨트롤러를 4개 배치(A:콘텐츠/CMS 16개, B:커뮤니티·통계·QnA 20개, C:주문·배송·판매자 19개, D:회원·지자체관리 12개)로 나눠 조사.

## 배치 A: 콘텐츠/CMS (16개)

핵심 발견: MenuManagerController는 1차 감사에서 "커버됨"이었으나 재검증 결과 메뉴 CRUD UI가 전혀 없어 SQL 마이그레이션으로만 메뉴를 바꿀 수 있는 상태로 판정 정정(高 우선순위). NewsletterManagerController는 AS-IS 코드 자체가 서비스/도메인 연결이 전혀 없는 빈 스텁으로 죽은코드 확정. LocgovNoticeManagerController는 화면은 있지만 지자체담당자 RBAC 스코핑이 빠진 실제 권한 버그.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| A1 | MainDisplayItemManagerController | 진짜미구현 | 있음(gift: OP_MAIN_DISPLAY_ITEM) | 中 |
| A2 | MaintenanceController | 진짜미구현 | 있음(admin: G_SR_MAINTENANCE) | 中 |
| A3 | ManualManagerController | 진짜미구현 | 없음(설계 필요) | 中 |
| A4 | MenuManagerController | 진짜미구현(판정정정) | 있음(admin: OP_MENU) | **高** |
| A5 | MessageManagerController | 진짜미구현(아키텍처 불일치 주의) | 있음(admin: OP_COMMON_MESSAGE) | 低 |
| A6 | MobileCategoriesEditManagerController | 진짜미구현 | 있음(gift: OP_CATEGORY_EDIT) | 中 |
| A7 | NewsletterManagerController | AS-IS 죽은코드 | - | - |
| A8 | LocgovNoticeManagerController | 이미 커버됨(RBAC 스코핑 gap) | - | 中(버그수정) |
| A9 | NoticeManagerController | 이미 커버됨 | - | - |
| A10 | SysNoticeSellerController | 진짜미구현 | 있음(admin: OP_SYS_NOTICE_SELLER) | 中 |
| A11 | OpmanagerController | 이미 커버됨(비밀번호정책/계정잠금 gap) | - | - |
| A12 | PopupManagerController | 이미 커버됨(CRUD 완성도 gap 큼: 수정/삭제/이미지업로드 없음) | - | 中(완성도 보완) |
| A13 | RepresentativeBannerManagerController | 진짜미구현 | 있음(admin: G_REPRST_BANNER) | 低 |
| A14 | SearchManagerController | 진짜미구현 | 있음(admin: OP_SEARCH) | 中 |
| A15 | SeoManagerController | 진짜미구현 | 있음(admin: OP_SEO) | 低(storefront 연동 선행 필요) |
| A16 | StyleBookManagerController | 진짜미구현 | 있음(gift: OP_STYLE_BOOK) | 低 |

## 배치 B: 커뮤니티/통계/QnA (20개)

핵심 발견: SendMailLog/SendSmsLog/UMS/알림톡 전부 완전 미구현(1차 감사의 "sms-log 커버됨(추정)" 정정). QnaAdminManagerController는 이름이 QnaAdminController(TO-BE)와 겹쳐 보이지만 실제로는 전혀 다른 도메인(지자체담당자↔본사 내부문의).

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| B1 | OzViewTestController | AS-IS 죽은코드 | - | - |
| B2 | PointManagerController | 진짜미구현(부분) | 있음(point) | 中 |
| B3 | PointCheckManagerController | 이미 커버됨(ReconciliationController) | - | - |
| B4 | QnaItemManagerController | 진짜미구현(부분: 관리자 감독+엑셀만) | - | 低 |
| B5 | QnaManagerController | 이미 커버됨 | - | - |
| B6 | QnaOpenManagerController | 이미 커버됨 | - | - |
| B7 | QnaAdminManagerController(내부문의) | 진짜미구현 | 있음(admin: QNA_ADMIN) | 低 |
| B8 | QustnrManagerController | 진짜미구현 | 없음(설계 필요) | 中 |
| B9 | RankingManagerController | 진짜미구현 | 있음(gift: OP_RANKING) | 中(공개페이지도 없어 함께 필요) |
| B10 | ReportController | 진짜미구현(텍스트리포트 생성) | 기존 대시보드 데이터 재사용 | 低 |
| B11 | StatisticsLocgovManagerController | 진짜미구현 | 있음(donation: G_INTRST_LOCGOV) | 中 |
| B12 | ShopStatisticsManagerController | 이미 커버됨 | - | - |
| B13 | StatsManagerController(방문통계) | 진짜미구현 | 없음(로깅 인프라부터 필요) | 中 |
| B14 | SendMailLogManagerController | 진짜미구현 | 있음(admin: OP_SEND_MAIL_LOG) | 中 |
| B15 | SendSmsLogManagerController | 진짜미구현 | 있음(admin: OP_SEND_SMS_LOG) | 中 |
| B16 | SmsController(인증문자로그) | 진짜미구현 | 있음(admin: TIF_IPS_*) | 低(mock연계라 실효 낮음) |
| B17 | SmsConfigManagerController | 진짜미구현(UMS로 흡수 권장) | 미확인 | 低 |
| B18 | UmsAlimTalkManagerController | 진짜미구현 | 있음(admin: OP_ALIM_TALK_*) | 低 |
| B19 | UmsManagerController | 진짜미구현(통합알림 상위개념) | 있음(admin: OP_UMS_*) | 中 |
| B20 | TransferController | AS-IS 죽은코드(어노테이션 전부 주석처리) | - | - |

## 배치 C: 주문/배송/판매자 (19개)

핵심 발견: **OrderManagerController(주문관리 콘솔) 완전 부재 확정, 우선순위 최고**. order 서비스는 관리자 인증 모델 자체가 없음(코드 주석 "아직 이 MSA에 운영자 로그인 모델이 없다"). 클레임 승인큐 무인증도 이 gap의 일부.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| C1 | OffgiveManagerController | 이미 커버됨(엑셀다운로드만 미커버) | - | - |
| C2 | **OrderManagerController** | **진짜미구현** | 있음(OD_ORDER/OD_CLAIM, 확장 필요) | **高(최우선)** |
| C3 | OrderAgencyController | 진짜미구현(C2 파생) | - | 中 |
| C4 | PayInfoManagerController | 진짜미구현 | 없음(PG모델 재설계 필요) | 中 |
| C5 | RemittanceManagerController | 이미 커버됨(SettlementController로 단순화) | - | - |
| C6 | SeasonalFoodManagerController | 진짜미구현 | 없음 | 低 |
| C7 | SellerController(판매자 셀프포털) | 진짜미구현(의도적 스코프컷, 재검토 필요) | - | 中 |
| C8 | SellerISysNoticeController | 진짜미구현(C7 종속) | - | 低 |
| C9 | SellerManagerController | 이미 커버됨(SellerAdminController, 부가기능만 미커버) | - | - |
| C10 | SellerShipmentController | 진짜미구현 | 없음 | 低 |
| C11 | SellerconfirmManagerController | 진짜미구현(C2/C5 서브셋) | - | 中 |
| C12 | ShipmentManagerController | 진짜미구현(C10 미러) | 없음 | 低 |
| C13 | ShipmentReturnController | 진짜미구현 | 없음 | 低 |
| C14 | ShipmentReturnManagerController | 진짜미구현 | 없음 | 低 |
| C15 | SpecialityItemManagerController | 진짜미구현 | 없음 | 低 |
| C16 | StoreManagerController | **AS-IS 죽은코드**(소스에 "사용 안하는 테이블" 명시) | - | - |
| C17 | StoreInquiryManagerController | 진짜미구현 | 있음(STORE_INQUIRY) | 低 |
| C18 | TempProcessManagerController | 진짜미구현(1차감사 오분류 정정, order/remittance 소관) | 없음 | 中 |
| C19 | WelfareCenterManagerController | 진짜미구현 | 있음(admin) | 中 |

### C2 OrderManagerController 구현 지시서 요약
- 관리자 전체 주문 목록/검색(지자체 스코프)+상태별 처리탭(일괄 상태변경)+상세화면(관리자메모)+클레임 처리큐+엑셀 다운로드/업로드
- 선행 필요: admin↔order 관리자 인증 연동 설계(HeaderAuthAdvice 패턴 재사용), 지자체 스코핑(Order.locgovCode)
- DB 확장: OD_ORDER.ADMIN_MEMO, OD_CLAIM_MEMO(신규), OD_EXCEL_DOWNLOAD_LOG(신규), Claim에 반송비 필드
- 이 gap의 파생: C3(주문대행조회), C11(정산확인큐), C18(보류주문처리)도 함께 해결 가능

## 배치 D: 회원/지자체관리 (12개)

핵심 발견: **지자체 마스터관리, 회원관리 콘솔 전체가 admin에 없다는 기존 gap이 컨트롤러 단위로 확정**. `LocgovSealAdminController`(donation, 무인증 임시화면)는 정식 지자체관리 화면으로 흡수 후 폐기 대상.

| # | 컨트롤러 | 분류 | DB | 우선순위 |
|---|---|---|---|---|
| D1 | **LocgovManagerController**(지자체 마스터관리) | **진짜미구현** | 있음(donation: G_LOCGOV, 미매핑 컬럼 다수) | **高** |
| D2 | **UserManagerController**(매니저계정+회원360도뷰) | **진짜미구현** | 있음(OP_USER 등) | **高** |
| D3 | **GeneralCustomerManagerController**(일반회원 검색/상세) | **진짜미구현** | 있음(OP_USER/OP_USER_DETAIL) | **高** |
| D4 | **SecedeUserManagerController**(탈퇴회원 조회) | **진짜미구현** | 있음(추정, 재확인) | **高** |
| D5 | **SleepUserManagerController**(휴면회원 조회/해제) | **진짜미구현** | 있음(OP_USER) | **高** |
| D6 | LocgovPersonInChargeManagerController | 진짜미구현 | 있음(OP_MANAGER) | 中 |
| D7 | ManagerRequestController | 이미 커버됨 | - | - |
| D8 | OffPersonInChargeManagerController | 진짜미구현(신규 권한티어 ROLE_ADMIN_7/8 필요) | 있음(OP_MANAGER 확장컬럼) | 中 |
| D9 | OperPersonInChargeManagerController | 진짜미구현(D6과 통합 권장) | - | 中 |
| D10 | UserGroupController(역할·메뉴권한 CRUD) | 진짜미구현 | 있음(OP_ROLE/OP_MENU_RIGHT) | 中 |
| D11 | UserLevelManagerController(회원등급) | 진짜미구현 | 있음(OP_USER_LEVEL, order 쿠폰과 이미 연결된 스키마) | 中 |
| D12 | WelfareCenterManagerController | (C19과 중복 배정, C19 참고) | - | - |

### D1/D2~D5 구현 지시서 요약
- **D1 지자체관리**: donation에 `LocgovAdminApiController`(CRUD+포인트지급률+부서이력) 신설, `Locgov.java` 미매핑 컬럼 전부 추가, admin에 `LocgovAdminController`(RBAC: SYS=전체, LOC=자기지자체만) + `LocgovAdminClient`. 기존 `LocgovSealAdminController`(무인증)는 흡수 후 폐기.
- **D2~D5 회원관리**: member에 관리자 전용 검색/상세/상태변경 API 일괄 추가(`/api/admin/members/**`, `/api/admin/secede-users/**`, `/api/admin/sleep-users/**`), admin에 `MemberAdminController`+`MemberAdminClient` 통합 구현(매니저 계정 CRUD는 admin 자체 도메인). DB 신규 테이블 불필요, 기존 OP_USER 계열 재사용.
