# AS-IS 기능 감사 - point(포인트) 서비스

조사일: 2026-09-03
대상: `ghlove` legacy(saleson 백엔드 `ghlove-web` + Vue2 `ghlove-frontend`) → `ghlove-msa` point/admin/storefront

## AS-IS 기능 전수 목록

| 기능명 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 고객 마이페이지 - 기부포인트 조회(지자체별 발생/사용/잔액) | `ghlove-frontend/mypage/cntrPoint.html`, `cntrPointDetail.html` / `saleson.shop.mypage.MypageController#pointSaveList,pointUsedList` | **구현됨** | `storefront/src/views/mypage/MyPointsView.vue` (`locgovSummary`) |
| 소멸 임박 포인트 안내 | `MypageController` `getNextMonthExpirationPointAmountByParam` | **구현됨** | `MyPointsView.vue` `upcomingExpirations` / `PointService.upcomingExpirations` |
| 포인트 자동 적립(기부 발생시) | `GivePointService`, `PT_LOCGOV_POINT_RATE` 사용처 | **구현됨** | `DonationEventListener`, `LocgovPointRate` (지자체별 요율, `domain_point_rate_policy` 메모) |
| 포인트 사용(답례품 결제) | `saleson.shop.order.OrderManagerController`(admin, PointService 연동), 실제 차감 로직은 프레임워크 jar 내부 | **구현됨** | `PointService.deductForOrder` / order SAGA |
| 포인트 예약/예약해제(SFR-004) | 별도 AS-IS 화면 없음(TO-BE 신규 설계, SFR 스펙 기반) | **구현됨** | `PointReservation`, `PointController#reservations`, storefront `PointReservationsView.vue` (round15) |
| 포인트 소멸 배치(FIFO lot) | 배치 잡(소스 미포함, 프레임워크 내부) | **구현됨** | `PointService.runExpirationBatch` / `/batch/expire` |
| 관리자 - 기부 포인트현황(행안부 전체/지자체별 목록+상세) | `saleson.shop.give.givepoint.GivePointManagerController` (`/opmanager/give/give-point/list`,`/detail`) | **구현됨** | `admin/.../web/GivePointController.java`, `templates/give/give-point-list.html`, `give-point-detail.html` (mois/locgov 역할분기까지 일치) |
| 관리자 - 포인트 정합성 검사(주문금액 vs 포인트사용 대사) | `saleson.shop.pointcheck.PointCheckManagerController` (`/opmanager/point-check/list`, view `view_rc_order_amt_vs_point_use_check` 기반) | **구현됨** | `admin/.../templates/reconciliation/order-point.html` (동일 view 재구현이라고 코드 주석에 명시) |
| 관리자 - 전체 포인트 발행내역/사용내역/일별 발생·사용현황 + 엑셀다운로드 | `saleson.shop.point.PointManagerController` (`/opmanager/point/list`, `total-history/list`, `day-group/list` + 각 엑셀다운로드) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 회원 개별 포인트 수동 지급/차감 | `saleson.shop.user.UserManagerController` (`popup/point-create/{pointType}/{userId}`, `PointService.earnPoint`/`deductedPoint`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 전체회원 대상 포인트 일괄 지급 | `UserManagerController` (`customer/point`, `insertPointAllUser`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 선택회원 대상 포인트 일괄 지급 | `UserManagerController` (`customer/point-pay` → `point-setting`, `insertPointPay`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 엑셀 업로드 포인트 일괄 지급 | `UserManagerController` (`customer/point-by-excel`, `insertPointByExcel`) | **미구현** | 아래 gap 상세 참고 |
| 관리자 - 회원별 포인트 내역 조회 팝업(단건 회원, 페이징) | `UserManagerController` (`popup/point/{pointType}/{userId}`) | **부분구현** | `give-point-detail.html`이 지자체×연도 단위로 유사 데이터(건별 발생/사용/잔액)를 제공하지만, 진입 경로가 "회원관리→회원선택"이 아니라 "지자체 선택"이며, admin에 회원(고객) 관리 화면 자체가 없어 "특정 회원 1명의 전체 포인트 내역"을 바로 조회하는 기능은 없음 |
| 관리자 - 포인트 정책 설정(기본포인트%, 가입포인트, 리뷰포인트, 기간별포인트, 사용최소/최대, 만료월, 배송쿠폰만료월) | `saleson.shop.config.ConfigManagerController` (`/opmanager/config/point`) | **의도적 제외 확인됨(신규 확인)** | 아래 근거 참고 |
| 배송비쿠폰(포인트와 별도의 두 번째 포인트 타입) | `PointUtils.SHIPPING_COUPON_CODE`, 여러 opmanager 컨트롤러에서 `avilablePoint2`로 병행 표시 | **의도적 제외 확인됨(신규 확인)** | 아래 근거 참고 |
| 관리자 - 환불처리 화면에서 회원 가용포인트 표시 | `OrderManagerController`(admin) refund 처리 시 `pointService.getAvailablePointByUserId` | **구현됨(간접)** | `admin/.../templates/settlements/detail.html`, `list.html`에서 포인트 관련 표시 확인 |

## Gap 상세

### 우선순위: 높음

**1. 관리자 - 회원 포인트 수동 지급/차감 기능 전체 (개별/전체/선택/엑셀)**

AS-IS `UserManagerController`(`ghlove-web/src/main/java/saleson/shop/user/UserManagerController.java`)는 아래 4가지 방식으로 관리자가 회원에게 포인트를 수동으로 지급하거나 차감할 수 있었다.
- 개별회원: `popup/point-create/{pointType}/{userId}` — `mode`가 지급/차감이면 각각 `pointService.earnPoint("admin", point)` / `pointService.deductedPoint(pointUsed, userId, pointType)` 호출, 사유(`reason`) 필수 입력
- 전체회원 일괄: `customer/point` (`point.jsp`) — "전체회원에게 포인트를 지급하시겠습니까?" 확인 후 `insertPointAllUser`
- 선택회원 일괄: `customer/point-pay` (`point-pay.jsp`) — 회원 목록에서 체크박스로 선택한 회원들에게만 지급, `insertPointPay`
- 엑셀 일괄: `customer/point-by-excel` (`point-by-excel.jsp`) — 엑셀 업로드로 다수 회원 포인트 지급, `insertPointByExcel`

TO-BE `point/src/main/java/com/ghlove/point/service/PointService.java`를 전체 확인한 결과, 주문결제 차감(`deductForOrder`)/복원(`restoreForOrder`)/기부 적립/예약/소멸배치 외에 **관리자가 임의 사유로 포인트를 지급(양수 적립)하는 메서드 자체가 존재하지 않는다.** `usePoints`(사용/차감 테스트용)만 있고 반대 방향(지급)이 없다. 또한 admin 서비스에는 회원(고객) 관리 화면 자체가 없어(templates 디렉토리에 user/member/customer CRUD 화면 부재, `fragments/customer-lnb.html`은 고객센터 게시판 내비게이션일 뿐 회원관리와 무관) 이 기능을 걸 UI 진입점도 없다.

**필요 작업**: (1) point 서비스에 `PointService.grantByAdmin(userId, amount, reason, locgovCode)` 같은 관리자 발급 메서드 추가(원장에 `TXN_TYPE=ADMIN_GRANT` 등으로 남기고 사유 필수), 반대의 `deductByAdmin`도 필요. (2) admin에 회원(고객) 목록/상세 화면이 없다면 최소한 "포인트 지급/차감" 단독 화면(회원ID 또는 검색으로 대상 지정 + 사유 입력)을 신설. (3) 전체회원/선택회원 일괄 지급, 엑셀 업로드 지급은 운영 빈도를 고려해 우선순위 조정 가능하나, 개별회원 수동 지급/차감은 CS 대응에 필수적이라 우선 구현 권장.

### 우선순위: 중간

**2. 관리자 - 포인트 발행/사용 전체 내역 + 일별 통계 + 엑셀다운로드**

AS-IS `saleson.shop.point.PointManagerController`(`/opmanager/point/*`)는 기부포인트현황(GivePoint, 지자체 단위 집계)과는 별개로, 전체 포인트 원장을 기간 검색으로 조회하는 3개 화면을 제공했다.
- `list` — 포인트 발행 내역(원시 트랜잭션 목록)
- `total-history/list` — 포인트 사용 내역
- `day-group/list` — 일별 발생/사용 현황(집계)
- 위 3개 모두 `ROLE_EXCEL`/`ROLE_OPMANAGER` 권한 체크 후 엑셀 다운로드 제공

TO-BE `admin/.../web/GivePointController.java`(`give-point-list.html`)는 지자체×연도 단위로 집계된 뷰만 제공하고, 원시 트랜잭션 단위 조회·일별 집계·엑셀 다운로드는 없다. `admin/src/main/java` 전체를 grep해도 excel 관련 코드가 전혀 없음을 확인했다(`grep -rl "excel|Excel|엑셀"` 결과 0건) — 이는 point 서비스만의 문제가 아니라 admin 콘솔 전반에서 엑셀 다운로드 기능이 프로젝트 차원에서 아직 이관되지 않은 것으로 보인다.

**필요 작업**: point 서비스 또는 admin에 원장 원시 목록 조회 화면(검색조건: 기간, 회원, 지자체) + 일별 집계 화면 추가. 엑셀 다운로드는 point 서비스 하나만의 문제가 아니라 admin 전체의 공통 gap일 가능성이 높으므로, 다른 서비스 감사 결과와 합쳐 "admin 엑셀 다운로드 공통 컴포넌트 도입"으로 한 번에 처리하는 것을 권장.

**3. 관리자 - 회원별 포인트 내역 단건 조회 팝업**

AS-IS `popup/point/{pointType}/{userId}`는 회원관리 화면에서 특정 회원 1명을 클릭하면 그 회원의 포인트(또는 배송쿠폰) 내역을 페이지네이션과 함께 팝업으로 보여줬다(`point-list.jsp` 프래그먼트 재사용). TO-BE `give-point-detail.html`은 유사한 데이터(건별 발생/사용/잔액)를 지자체×연도 단위로 제공하지만, "회원 이름으로 검색"만 가능하고 회원 ID로 직접 진입하는 경로가 없다. 회원관리 화면 자체가 없다는 동일한 근본 원인(gap #1)에서 파생된 문제이므로, #1 해결 시 함께 정리 권장.

### 우선순위: 낮음 (확인 필요, 확정 아님)

**4. 포인트 정책 설정 화면(`/opmanager/config/point`) 일부 항목의 실사용 여부**

이 화면은 기본포인트%, 회원가입시 포인트, 리뷰채택시 포인트, 기간별 포인트, 포인트 사용 최소/최대 금액, 포인트 만료월, 배송쿠폰 만료월 등을 설정하는 saleson 범용 쇼핑몰 프레임워크의 표준 설정 화면이다. 다음 근거로 ghlove의 실제 기부-포인트 비즈니스에는 적용되지 않는 saleson 템플릿 잔재일 가능성이 높다고 판단했다(단, `dormant-saleson-boilerplate-tables` 사례처럼 100% 확정하려면 운영 DB의 `OP_CONFIG` 실제 값 확인이 필요):
- 고객용 포인트 페이지(`ghlove-frontend/mypage/cntrPoint.html`, `cntrPointDetail.html`)에는 "최대 3일 지연 반영" 안내만 있을 뿐, 유효기간/최소·최대 사용한도/가입포인트/리뷰포인트 등 이 설정과 관련된 정책 안내 문구가 전혀 없음
- ghlove의 실제 적립은 "기부 시 지자체별 요율"(`PT_LOCGOV_POINT_RATE`, `domain_point_rate_policy` 메모로 이미 확인됨)로 이루어지는데, 이 설정화면은 "회원가입시/리뷰채택시/방문시" 등 일반 쇼핑몰형 적립 시나리오를 다룸 — 기부 기반 모델과 맞지 않음
- JSP 내부에 이미 "CJH 2016.10.20 미구현 기능" 주석으로 일부 항목이 죽어있다고 스스로 명시된 부분도 존재

다만 "포인트 만료월" 개념 자체는 TO-BE에도 실존하며(FIFO lot expire), 현재는 `CommonCode`(`SYSTEM_CONFIG`/`POINT_EXPIRY_NOTICE_DAYS` 등)로 관리 가능해 실질적으로 흡수된 것으로 판단했다. 전용 "포인트 설정" 화면 신설은 필요성이 확인되기 전까지 보류 권장.

## 완전히 구현 확인됨 (요약)

- 고객 마이페이지 포인트 조회(지자체별 집계, 소멸임박 안내) — `MyPointsView.vue`
- 포인트 자동 적립(기부 이벤트 기반, 지자체별 요율) — `DonationEventListener`, `LocgovPointRate`
- 포인트 사용(주문 결제, SAGA 연동) — `PointService.deductForOrder/restoreForOrder`
- 포인트 예약/예약해제(SFR-004) — `PointReservation`, storefront `PointReservationsView.vue`
- 포인트 소멸 배치(FIFO lot) — `PointService.runExpirationBatch`
- 관리자 기부포인트현황(행안부 전체/지자체별, 역할분기 포함) — `GivePointController`, `give-point-list/detail.html`
- 관리자 포인트 정합성 검사(주문금액-포인트사용 대사) — `reconciliation/order-point.html`
- 배송비쿠폰: AS-IS 자체에서 `mypage/old/`, `order/old/` 폴더에만 남아있는 죽은 saleson 템플릿 기능으로 확인되어 스킵 확정
