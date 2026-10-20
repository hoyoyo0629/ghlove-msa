---
name: deferred-features
description: 보류된 기능 목록 - member·donation 잔여·포인트 예약·제공자 포털 분리·판매자 셀프서비스 P1~P8·성인답례품(완료)/주문대행·장바구니 잔여
metadata:
  type: project
---

결정 대기 또는 보류로 기록해 둔 기능 단위 항목.

2026-10-20 메모리 통폐합으로 아래 원본들을 원문 그대로 합쳤다(원본 파일명은 각 절 제목). 새 내용은 해당 절에 덧붙이고 새 파일을 만들지 않는다.

## member-service-deferred-items

> member 서비스 상세분석(2026-09-09) 잔여 5건 - 이벤트 발행 전무, 조회모델, 전자서명, Keycloak, 연동해지. 각각 결정이 필요해 보류


2026-09-09 member 서비스를 3축(AS-IS+RFP+ISP) 기준으로 분석한 결과 중 **결정이 필요해 미조치로 남긴 5건**. 분석 원문·근거는 `docs/member-service-analysis-2026-09-09.md`.

- ~~**이벤트 발행이 전혀 없음**~~ — **[2026-09-22 Phase A 발행 구현완료]** member.lifecycle 토픽에 `MEMBER_JOINED`(signup/walk-in/외부가입)·`LOGIN_SUCCEEDED`·`LOGIN_FAILED`·`MEMBER_WITHDRAWN`·`MEMBER_DORMANT` 발행(`MemberEventPublisher`, 기존 donation/gift/order/point 패턴 동형, publish-only 비차단). build.gradle spring-kafka + application.yml producer + KafkaTopicConfig 추가. 컴파일 OK, **재기동+발행 확인 대기**(ghlove-kafka 9092 기동중). 설계·결정근거 `docs/member-event-publishing-design.md`(사용자 확정: Phase A만·감사는 이벤트축적 방향). **보류(Phase B)**: admin 회원통계/감사 소비 ReadModel은 [[da-design-pending]]로 DA 설계 후. 본인인증(외부연계)·ProviderAccount(포털분리) 이벤트도 보류.
- **ISP 조회모델 3종(회원정보뷰/인증이력뷰/세션뷰) 없음** — point ReadModel과 같은 유형. [[da-design-pending]]이 DB 설계 대기 중이라 같은 시점에 같이 판단하는 게 맞다.
- **전자서명(공동인증서) 등록/해제 없음** — AS-IS `/signRegister` `/signRemove` `/checkSign` `/getNonce` + `users/sign-certificate.html`. ISP도 회원관리 POD SW목록에 MagicLine 4(PKI공인인증, 상용)를 명시해 근거가 2축. **결정 필요**: 다른 외부연계와 같은 의도적 축소로 볼지.
- **Keycloak 미도입** — ISP p.481이 계정통합관리로 Keycloak(OIDC IAM) 채택, AD/LDAP 연동까지 전제. 현재 서비스별 자체 JWT. 로컬 단계에선 합리적 생략이나 **클라우드 이관 시 중앙 IAM이 설계 전제**.
- ~~**디지털원패스/카카오 연동해지 화면 없음**~~ — **[2026-09-22 구현 완료]** AS-IS `onepass_secede.html`/`secede-kakao.html` + `modify.html` 연동해지 버튼 재현. member `AccountUnlinkApiController`(onepass-cancel/kakao-link-clear/kakao-link-secede) + `secedeExternal`/`clearKakaoLink`, `OnePassClient.releaseInterlock()`/`KakaoCertClient.unlink()`(off면 통과), storefront `ProfileView` 버튼 2개 + `OnepassSecedeView`/`KakaoSecedeView`. 매핑: userKeyYN≡loginPathCode300, kakaoUserKeyYN≡500or키보유. 컴파일·빌드 OK, **재기동+E2E 대기**. 상세 `docs/member-social-unlink-parity-audit.md`. **보류**: `onepass-unlink`(탈퇴없는 원패스 부가연동 해제)는 MSA 모델상 성립X, 실 연계해지 API 본체는 외부개방 시.

**Why:** 다섯 건 모두 코드를 더 쓰는 문제가 아니라 인프라/외부연계/설계 범위 결정이 선행돼야 하는 항목이다.

**How to apply:** 같은 세션에서 조치한 것(비밀번호 유효기간 정책, `.list-none` CSS)은 이미 반영·검증됐다. 비밀번호 정책 관련 주의: `op_user.password_expired_date`의 기존 `20240101`은 AS-IS 스키마 이관 placeholder라 정책 도입 시점 기준으로 한 번 밀어놨다(DDL에 마이그레이션 기록) - 다시 초기화하면 전 회원이 즉시 만료 처리되니 주의. 분석은 3축 기준을 따랐다([[asis-porting-principles]]).

## donation-service-deferred-items

> donation 서비스 상세분석(2026-09-09) 잔여 - 납부 시 답례품 선택(RFP 신규요구), point 동기호출, 조회모델, 지도 선택, PG 연계


2026-09-09 donation을 3축(AS-IS+RFP+ISP) 기준으로 분석한 결과 중 미조치 항목. 원문은 `docs/donation-service-analysis-2026-09-09.md`. **이번 턴 코드 변경은 없다**(§5에 이유 기록).

- **기부금 납부 시 답례품 선택** — RFP SFR-003이 "기부금 납부 시 답례품 선택 기능 추가 등 기부 프로세스 통합 개선"을 명시적으로 요구한다. **AS-IS에 없던 신규 개선 요구**라 참고할 원본이 없고, 현재는 기부 완료 후 별도로 답례품몰로 가는 분리된 흐름이다. 기부↔답례품 흐름 통합 설계 결정 필요.
- ~~**`PointClient.creditForDonation()` 동기 호출**~~ → **[2026-09-21 완료] 이벤트 전환.** give-reqmng 승인의 "포인트생성"이 point로 동기 REST 쓰기를 하지 않고, 정상 완료 경로와 똑같이 `donationEventPublisher.publishCompleted()`로 `donation.lifecycle` COMPLETED 이벤트를 재발행하도록 바꿨다(`CntrReqmngService.approve`). donation의 PointClient에서 `creditForDonation` 쓰기 메서드 자체를 제거(조회 전용화). 안전 근거: 소비자 둘 다 멱등(point `existsByRefKeyAndTxnType(cntrSn,EARN)`, admin 통계 `stat_donation_ledger` cntrSn PK upsert)이라 재발행해도 중복 없음. **E2E 실측 완료**: 시드 기부건 승인→point EARN 15000(50000의 30%)+admin 통계 COMPLETED 반영, 2차 재발행에도 EARN 1건·통계 1건 유지(멱등 확인), 검증 후 물리 정리로 잔액 복원. point 엔드포인트 `/api/admin/credit-for-donation`은 admin ResyncService(운영 백필)가 여전히 사용해 **유지**. 트레이드오프: 동기와 달리 승인 즉시 성공+적립은 결과적 정합성(mainline `completeDonation`과 동일 성격). 상세: [[api-vs-kafka-decision-criteria]].
- **ISP 조회모델(기부요약/영수증/한도, 결제상태/대사뷰) 없음** — [[da-design-pending]]과 같은 유형. DB 설계 후 같은 시점에 판단.
- ~~**기부하기 지도 선택(map-select) 없음**~~ → **[2026-09-21 철회] 갭 아님.** AS-IS 운영도 `map-select.html`로 진입하는 버튼·링크·메뉴가 0곳이다(모든 참조가 `goToBack`뿐, 순방향 진입 없음). 콘텐츠관리 메뉴에서 명시 제외(list.jsp:81), list-select의 지도 안내이미지는 "고객 요청 삭제"로 주석처리, LNB 지도탭 하이라이트도 주석처리. 즉 지도선택은 파일만 남은 死화면이고 AS-IS도 목록선택(list-select)만 노출한다. MSA는 이미 `ListSelectView.vue`(목록)를 구현해 운영 동등. 최초 분석이 **파일 존재만 보고 운영 도달가능성을 검증 안 한** 오판(사용자가 라이브 화면에서 발견). [[asis-porting-principles]]. 참고: header의 `show-map-select`는 답례품몰/장바구니 지역필터용 별개 팝업이다.
- **결제/PG 연계 화면 + ISP 결제 이벤트**(결제세션생성/승인요청/정산캡처) — `LocalTaxClient`는 부과등록·수납확인이 구현돼 있고 `enabled:false`(방화벽 미개방)일 뿐이다. 연계 개방 후 착수.
- ~~**하루 중복기부 확인 로직**~~ → **[2026-09-21 완료]** GET /api/donate/today-duplicate + hasTodayDonation(오늘 cntrDe·같은지자체·비취소) + DonateView confirm. 상세 `docs/donation-parity-audit.md` §9.

**[2026-09-22 donation 재대조 라운드 — 잔여 정리]** 기존 갭 대부분 ✅완료(9/21), 이번에 마감:
- ~~지정기부 응원메시지 30자(5-4)~~ **✅완료**: MSA가 100자로 지어놨던 걸 AS-IS 30자로 정합(DonateView maxlength/placeholder/제출검증 alert "30자 까지 입력가능합니다.", 서버 truncate 30). [[asis-porting-principles]] 사례.
- 지정기부 완료취소 불가(5-5) **갭 아님**: AS-IS 원문이 "…취소 안되용~ 테스트 문구"(테스트 잔재). MSA 완료취소 허용이 맞음.
- quick 빠른기부(quick-donation.html) **미사용 시안 → 보류+기록**: AS-IS 소스 주석에 "시안, 어디에도 미연결, 동작 안 함, 신규구현 대상 아님. 실제 기부는 donation-main.html" 명시. [[asis-porting-principles]] 유형.
- **잔여 실작업**: 응원메시지 인라인 편집(saveCheerMsg, AS-IS는 기부내역 탭 재편집) 미구현 — MSA는 제출시점 수집으로 적응(결정 필요). 나머지는 아래 외부연계·조회모델·RFP 신규만 남음.

**충족 확인된 것(재조사 불필요):** 연간 기부한도 이중 검증(완료 시점 재검증 포함), 기부완료 이벤트→포인트 적립(RFP ★), 영수증·기부확인증·국세청 연계, 기탁·오프라인, 지정기부사업, 명예기부자. 기부 상태코드는 AS-IS 4종 중 900(서울 세외수입 수납배치)만 없고 그 연계에 종속돼 의도적 축소. **CSS 링크 문제 없음**(클래스 단위 대조로 확인) - 이 결함 유형의 실사례는 order의 `mypage-order.css` 하나뿐이다.

**주의:** 연말정산 세액공제 안내(AS-IS guide3)는 **이미 `/honor`에 구현돼 있다** - 없다고 오판한 적이 있으니 다시 착수하지 말 것([[asis-porting-principles]]).

## point-reservation-unused-decision-deferred

> SFR-004 '포인트 예약/예약해제'는 ISP에 정식 설계됐으나 실제 호출부가 0곳인 미사용 기능 - 체크아웃 재배선 vs 회원화면 숨김 결정을 2026-09-09 보류


SFR-004의 "포인트 예약 및 예약해제"가 어디서 쓰이는지 2026-09-09 조사한 결과와 보류 결정.

**조사 결과 (실측):**
- **ISP 상세설계에는 명시적으로 설계돼 있다** (2026-09-09 3축 기준 재검토로 추가 확인). `docs/isp-detailed-design-summary.md`의 이벤트스토밍·바운디드컨텍스트 표(원본 p.415/427)에 포인트관리 명령 = "포인트적립, **예약/사용, 예약해제**, 환불, 소멸", 도메인 이벤트 = "포인트적립됨/**예약됨**/사용됨/**해제됨**/환불됨/소멸됨"으로 나온다. 엔티티 명세(원본 p.367)도 단위기능에 "예약/예약해제"를 포함한다. **즉 근거가 RFP 한 줄뿐이라는 이전 판단은 틀렸다 - ISP 기준으로는 정식 설계 기능이다.**
- 요구사항 근거는 `docs/requirements.md:59` 한 줄뿐 - "포인트 관리 기능: 적립, 차감(주문결제), **예약/예약해제**, 소멸 처리, 복원·조정".
- **AS-IS(`C:\workspace\ghlove`)에는 예약 개념 자체가 없다.** 기부포인트 매퍼(`sqlmapper/cubrid/give-point-mapper.xml`)·서비스 어디에도 대응 코드가 없고 테이블도 없다. 즉 AS-IS 재현이 아니라 이 프로젝트 신규 기능이다.
- **다른 서비스가 예약 API를 호출하는 곳이 0곳.** 실제 주문 결제는 `ORDER_CREATED` 이벤트 → `PointService.deductForOrder()`로 **즉시 차감**하고, 취소 시 `restoreForOrder()`로 복원한다(`point/.../event/OrderSagaListener.java`). 예약 단계를 타지 않는다.
- 그래서 `point`의 `/reservations` 화면은 회원이 직접 예약을 만들어보는 **수동 테스트용**으로만 남아 마이페이지에 노출돼 있다.

**원래 의도:** 결제 확정 전 "일단 잡아두기"(hold). 주문 생성 시 원장을 깎지 않고 예약 행만 남겨 가용잔액을 줄이고, 확정 시 실제 차감, 실패 시 예약만 해제한다. 즉시 차감은 실패 시 보상 트랜잭션(복원)이 필요하지만 예약 방식은 상태만 되돌리면 되어 SAGA가 단순해진다. 지금은 그 **틀만 구현**된 상태다.

**Why 보류:** 두 선택지 중 어느 쪽인지가 제품 결정이라 사용자가 판단할 사항 - (a) order 체크아웃을 예약→확정 2단계로 재배선해 요구사항대로 실제 사용, (b) 틀만 유지하되 회원용 화면은 감추거나 운영자 도구로 이동. 현행(회원 마이페이지 노출)은 AS-IS에도 없고 실제 업무 흐름도 없어 혼란만 남는다는 점은 합의됨.

**How to apply:** 다시 꺼낼 때 전제 - 이 경로는 2026-09-09에 **지자체 필수로 이미 고쳐졌다**(`PT_POINT_RESERVATION.LOCGOV_CODE` 추가, `reserve`/`usePoints`가 지자체별 가용잔액으로 검사, `confirmReservation`이 3-인자 `consumeLots` 사용, 위험한 2-인자 오버로드 삭제). 그 전에는 지자체 없이 lot을 잠식해 마이페이지 잔여(원장 합계)와 장바구니 잔여(lot 합계)가 어긋나는 버그가 있었고 오염 데이터도 보정했다. (a)를 고르면 order 체크아웃(`CartService`/`CheckoutController`)이 `ORDER_CREATED` 즉시차감 대신 예약→확정을 타도록 바꾸고 SAGA 보상 경로를 예약해제로 교체해야 한다. 분석은 AS-IS 대조로 나온 결과다([[asis-porting-principles]]).

## provider-portal-split-deferred

> 제공자 포털 별도앱 분리 여부는 운영데이터 적재/개발DB 구축 후 재결정 — 현행(gift 내 /seller/*) 유지


답례품 제공자 포털을 **별도 프론트엔드 앱으로 뺄지**는 2026-09-09에 **보류**했다. 잠정 결정은
**현행 유지** — gift 서비스(8084) 안의 Thymeleaf `/seller/*`. 최종 판단은 **운영데이터가 적재되거나
개발DB가 구축돼 실제 제공자 데이터·화면 규모가 드러난 뒤**로 미뤘다.

**Why:** "관리자 콘솔과 합칠 것인가"는 이미 닫힌 문제다(AS-IS 화면ID가 관리자 `UI_M` 244개 /
제공자 `UI_S` 44개로 교차 0건, ISP 프론트 3분할, N2SF 관리자망 VPN·IP제어는 민간 사업자에게 부여
불가). 게다가 현재도 admin(8086)과 별도 프로세스라 분리 요건은 충족돼 있다. 남은 건 배포 단위
문제인데, 제공자 화면이 답례품·주문·정산에 밀착돼 있어 지금 떼면 API 왕복만 늘어난다. **진짜
문제는 위치가 아니라 규모** — AS-IS 44개 대비 현재 `seller-dashboard.html` 1개뿐이다.

**How to apply:** 이 주제가 다시 나오면 "합칠까 나눌까"가 아니라 **채우는 작업**(제공자 포털 화면 +
자체 로그인)을 먼저 제안할 것. 별도 앱 분리는 ① 제공자 화면 20~30개 초과로 배포 주기가 어긋나거나
② 제공자 전용 인증·보안 정책(전용 도메인, IP 화이트리스트)이 생기거나 ③ 트래픽이 대민 조회와
간섭할 때 검토한다. 상세 근거와 이행 작업 11건은 `docs/account-model-design.md`에 있다.
관련: [[domain-code-facts]], policy-content-db-migration-deferred(보관: docs/archive/memory-fix-log-2026-10.md)

**[2026-09-22 보강]** ISP 원문이 TO-BE **프론트엔드 3종(대민웹/관리자운영웹/답례품제공자웹)** 을 명시(p.11/217/439/445, docs/isp-detailed-design-summary.md). 현 MSA 실측: **온전한 독립 프론트엔드는 2개**(storefront=대민 Vue, admin=운영 Thymeleaf). 답례품제공자웹은 아직 독립앱 아님 — gift 서비스 임베드 Thymeleaf(`SellerPortalController`→`/seller/dashboard`→seller-dashboard.html, seller API는 `SellerApiController`). **결정(사용자, 2026-09-22): 제공자웹 분리는 계속 보류(A안).** gift 옵션/각인/추가구성 **판매자 등록화면을 현행 gift 판매자 포털(Thymeleaf)에 API-first로 얹고**(비즈니스 로직은 gift 서비스 REST에, 화면은 얇게 소비), 훗날 제공자웹 신설 시 **백엔드는 재사용·뷰만 재스킨 이식**. 즉 A안의 임시부채=판매자 포털 Thymeleaf 몇 장 증가. 제공자웹 재개는 옵션작업과 분리된 별도 결정. [[gift-option-multiform-redesign]] [[thymeleaf-duplicate-cleanup-deferred]]

## gift-seller-selfservice-parity

> 판매자(답례품제공자) 셀프서비스 AS-IS 전수대조. 핵심기능 이미 구현+P4 판매자공지 조회 구현 완료. P1~P3·P5(출고/정산/반품=전체주문관리)는 제공자웹분리와 함께 보류, P6~P8 SalesOn 보류.


**판매자 셀프서비스** 전수대조+구현 (2026-09-22). 갭목록: `docs/gift-seller-selfservice-parity-audit.md`.

**이미 구현(유지)**: 답례품 등록/수정/옵션·추가구성/재고조정/판매중지/Q&A답변/본인목록+주문현황 대시보드(SellerPortalController, ROLE_PROVIDER JWT)/입점업체 CRUD.

**P4 판매자 공지 조회 — 구현 완료**: AS-IS SellerISysNoticeController(/seller/sys-notice) 재현. 데이터는 admin 소유(op_sys_notice_seller)라 gift→admin 내부호출.
- admin `SellerNoticeInternalApiController`(/api/seller-notices, X-Internal-Secret): 목록(useYn/displayFlag≠N 노출필터)/상세(조회수증가)/첨부다운로드.
- gift `SellerNoticeClient` + `SellerNoticeController`(/seller/sys-notice/list·detail·file-download) + 템플릿 2개(AS-IS 컬럼 No./제목/조회수/등록일시, 공지라벨·첨부, 검색·총N건). config `ghlove.admin-service.base-url=8086` 추가. 시드 admin.op_sys_notice_seller 2건. 컴파일 OK, **재기동 시 활성**.

**보류(결정)**:
- **P5 정정**: AS-IS SellerOrderController extends OrderManagerController = 운영자 주문관리 전체(송장/배송상태/취소) 상속 → 경량 아님, P1과 동일영역.
- **P1~P3·P5(출고/송장·정산·반품출고)**: 현재 MSA admin(운영관리) 전용. 판매자 셀프 이관은 [[deferred-features]](제공자웹 분리 보류)에 걸림 → 보류.
- **P6~P8(mall 판매자몰·magicline 전자서명·item상품관리·sale-edit·temp-process·categoriesfilter)**: SalesOn 종속·미사용 → [[asis-porting-principles]] 보류+기록.

**⚠ ISP 근거 주의**: "판매자가 직접 출고/정산" 을 ISP 근거로 단정했으나 원본 ISP는 리포에 없고(요약본 docs/isp-detailed-design-summary.md만), 오히려 요약본상 정산=운영관리(admin) 소관(원본 p.259/373/379). 출고 수행주체는 요약본에 명시 없음. → ISP 인용은 원본 확인 후에만.

관련 [[asis-porting-principles]] [[asis-porting-principles]].

## adult-item-and-order-agency-unported

> 2026-10-14 발견한 미이식 2건 - 성인(19세) 답례품 제한은 같은 날 이식 완료, 주문대행(상담원 대리주문) 주문 생성 흐름은 조사 끝·이식 대기


온라인 주문 PRESENT_ORDER 문자·모바일 분할을 이식하다(AS-IS `OrderServiceImpl.saveGiveOrderTempAndPay`) 같은 블록에서 발견. 사용자 "미이식 2건도 구현하자"(2026-10-14).

**1. 성인 답례품 제한 - 이식 완료(2026-10-14, 재기동 대기)**
- AS-IS 실동작만 이식: 매퍼의 `isAdult`/MINOR_ALLOW_FLAG 필터는 setIsAdult 호출자가 없는 죽은 코드라 제외. 목록 화면들은 "19세 답례품 모두
  보여주는 걸로 변경"으로 adultYn을 'Y'로 덮어써 19세 뱃지·상세이동 차단이 실제로는 안 걸린다(goods/index·searchGoods 담기만 buyFlag로 막음).
- 성인 판정 = 올해 - 출생연도 ≥ 19 (`MemberService.isAdult`, `/api/auth/me` adultYn, `/api/admin/members/{id}/adult`).
- 서버 차단: `CartService.checkoutMultiItem` "19세 이상 로그인 사용자만 주문 가능합니다." / 상세 담기·바로선택 alert / 체크아웃 paymentYn(결제버튼 숨김+빨간 안내) /
  DonateGiftSelectView 담기 alert.
- 설정: gift `Seller.adultItemYn`(성인답례품 등록권한), `Gift.adultItemYn`(구매 제한 성인, 판매자 권한 없으면 'N'로 저장 `applyAdultItemYn`).
  admin 입점업체 폼·답례품 폼에 체크박스 - 두 폼 모두 AS-IS와 구성이 달라(업태/업종 행 없음, 답례품 분류 행 없음) **임시 위치**. 폼 재이식은 전수조사 때.
- 렌더 미검증: offtest01(오프라인 권한)로는 두 관리자 폼이 access-denied라 직접 못 봄.

**2. 주문대행 주문 생성 - 조사 끝, 이식 대기**
- AS-IS: 관리자 목록 `opmanager/order-agency/order/list.jsp`(검색 + "주문대행 로그인 페이지 이동" + 행별 "인쇄페이지") → 프론트(3000)
  `/admin/order-agency/{login,cntr-list,goods-list,item-details,step1,step2,order-agency-detail}.html`(약 6,700줄) →
  api `/api/order-agency/*` 11개(`ghlove-api OrderAgencyController`) + `OrderAgencyServiceImpl`(RSA 키 30분·로그인 120분, 관리자 status 9·pCrypto 비번,
  기부자 전화번호+본인인증 CI로 회원 확정, ROLE_AGENCY JWT) + `orderAgencySaveGiveOrderTempAndPay`(OrderServiceImpl:9269~, 약 600줄) +
  테이블 G_AGENCY_LOGIN_CONFIRM_INFO·G_ORDER_AGENCY_ORDER_LOG·(JPA)AgencyPrivateKeyInfo·TempData.
- TO-BE `OrderAgencyAdminController`(/admin/order-agency)는 AS-IS 목록이 아니라 발명된 "주문대행 조회"(전화번호 검색 추가 등) - 재이식 대상.

**Why:** 사용자 원칙 - MSA 전환사업이라 미이식 0이 기본선 ([[asis-porting-principles]]).

**How to apply:** 주문대행 착수 시 [[asis-screen-port-procedure]] 3종 대조부터, 프론트 7화면은 AS-IS처럼 대민(storefront) 쪽 라우트로.
관련 [[service-logic-full-audit-planned]].

## cart-review-remaining-tasks

> 장바구니 심각결함 2건 수정 완료, 잔여과제와 미완 재검증은 docs/cart-review-2026-09-09.md §4에 정리


2026-09-09 장바구니 전면 분석 후 심각결함 2건(체크아웃 부분실패 유령 SAGA 이벤트, 화면금액≠실제차감액)과 중간 4건을 수정했다. **잔여과제·미완 검증은 `docs/cart-review-2026-09-09.md` §4에 전부 정리돼 있으니 장바구니/주문결제를 다시 손대기 전에 그 문서부터 읽는다.**

가장 중요한 미완 항목 둘:
- `OrderSagaPublisher`를 커밋 이후 발행(afterCommit)으로 바꾼 것은 장바구니뿐 아니라 **모든 order.saga 이벤트에 적용**된다. 6개 서비스 실기동 후 gift/point 소비자 타이밍 재검증이 아직 안 됐다.
- 배송비 경로가 실데이터로 검증되지 않았다 — 개발 DB 답례품이 대부분 무료배송(GIFT_SHIPPING_TYPE=1)이라 조건부무료/개당과금 경로를 실호출로 못 봤다.

관련: [[domain-code-facts]], [[production-domain-migration-plan]], [[thymeleaf-duplicate-cleanup-deferred]]
