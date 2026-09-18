# AS-IS 인벤토리 — point 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

## 0. 이 도메인의 핵심 — AS-IS에는 "포인트 원장"이 없다

AS-IS에는 **서로 무관한 포인트 체계가 두 개** 있고, 고향사랑e음이 실제로 쓰는 것은 두 번째다.

| | 원제품(SalesOn) 포인트 | **기부포인트 (실사용)** |
|---|---|---|
| 테이블 | `OP_POINT`, `OP_POINT_USED`, `OP_POINT_CONFIG` | `G_CNTR.CNTR_POINT` + `G_CNTR_USE_POINT` |
| 모델 | 만료일별 **버킷(lot)** + 사용이력 | **원장 없음** — 기부 레코드의 컬럼과 사용 테이블을 그때그때 **집계** |
| 적립 진입 | `PointServiceImpl.earnPoint(mode, point)` | 기부 완료 시 `G_CNTR.CNTR_POINT`에 기록 |
| 잔액 | `OP_POINT` 잔량 합 | `CNTR_POINT − SUM(G_CNTR_USE_POINT)` **계산값** |
| 만료 | `expirationPoint()` 배치 | `G_CNTR.POINT_END_DE` 기준 `GivePointExpirationServiceImpl` |

`earnPoint`의 실제 호출처는 **4곳뿐이고 전부 원제품 기능**이다 — `"join"`(회원가입 포인트, `UserServiceImpl:530,575`), `"admin"`(운영자 수동적립, `UserManagerController:1399`), `"return"`(주문취소 환급, `OrderClaimApplyServiceImpl:1130` / `OrderServiceImpl:5495`), `"review"`(상품평 포인트, `ItemServiceImpl:2188`).
→ **기부 완료 적립은 `earnPoint`를 타지 않는다.**

> `"review"` 모드는 `earnPoint`의 `if/else` 분기에 **없다**. `join`/`admin`/`return` 어디에도 해당하지 않아 기본 경로로 떨어진다 — 적립은 되지만 mode별 처리(사유 문구·관리자ID 기록)를 받지 못한다.

### 0-1. MSA의 원장 재설계는 RFP 요건이다
MSA는 `PT_POINT_LEDGER`(원장) + `PT_POINT_BALANCE`(잔액) + `PT_LOCGOV_POINT_RATE`(지자체별 적립률) 3종으로 **재설계**했다. AS-IS 테이블(`op_point*`, `g_cntr_use_point`)은 이관돼 있으나 **자바 참조 0**이다.

이 재설계는 [[scope-migration-not-greenfield]] 위반이 아니다 — RFP **SFR-004**가 명시 요구한다:
- `requirements.md:58` "포인트 **거래 원장과 회원별 잔액 테이블을 분리**하여 관리"
- `:57` "기부금액의 **30%를 기준**으로 지자체별 적립 규칙 적용 (지자체마다 다름 — **하드코딩 금지, DB 관리 필수**)"
- `:59` "적립, 차감(주문결제), **예약/예약해제**, 소멸 처리, 오입금취소/반품에 따른 복원·조정"
- `:61` "거래내역·잔액 조회, 지자체별 사용현황, **만료 예정 포인트 안내**를 CQRS 기반 ReadModel로 제공"
- ISP `:160` "포인트적립/예약/사용/해제/환불/소멸" → 산출물 "포인트잔액/**원장**/만료뷰"

> [[point-reservation-unused-decision-deferred]]에 이미 기록된 대로 예약 기능은 RFP·ISP 양쪽에 있는 정식 신규 요건이며, **(a) 체크아웃 재배선 / (b) 틀만 유지하고 회원화면은 감춤** 중 어느 쪽인지는 보류된 제품 결정이다. 이번 대조에서 근거가 하나 더 붙었다 - RFP `:59`(예약/예약해제)뿐 아니라 `:61`(만료예정 안내 ReadModel)·ISP `:160`(산출물 "포인트잔액/원장/만료뷰")까지 같은 SFR-004 묶음이라, 원장·잔액·만료뷰를 이미 구현한 이상 예약만 빼는 것은 설계 일관성이 떨어진다. 판단은 사용자 몫이다.

## 1. 화면 생사 판정

| AS-IS 화면 | inbound | 판정 | MSA |
|---|---|---|---|
| `mypage/cntrPoint.html` (기부포인트 조회) | 12 | live | `/` (`my.html`) |
| `mypage/cntrPointDetail.html` (지자체별 상세) | 2 | live | `/detail` (`detail.html`) |

포인트를 **사용**하는 화면(장바구니·주문·답례품 상세)은 order/gift 도메인 소관.

## 2. 컨트롤러 엔드포인트

### 2-1. 사용자 (`/api/mypage`)
| 엔드포인트 | live 호출처 | MSA | 판정 |
|---|---|---|---|
| `GET /getCntrPoint` | `cntrPoint.html`, `cntrPointDetail.html`, `orderList.html` | `GET /`, `/api/my/points`, `/api/balance` | 대응있음 |
| `GET /getCntrPointDetail` | `cntrPointDetail.html` | `GET /detail` | 대응있음 |
| `GET /points` (`/api/mypage/2/points`) | 래퍼만 있고 경로 불일치 | — | **죽은코드** (donation §2-3과 동일 원인) |

### 2-2. 운영관리 (ghlove-web) — admin 배정 대상
`PointManagerController`, `GivePointManagerController`, `PointCheckManagerController` 3종. MSA는 `/api/admin/point-history`, `/api/admin/point-daily-stats`, `/api/admin/locgov-point-rate`, `/api/admin/credit-for-donation`로 일부 대응.

## 3. 매퍼 쿼리 (78 + 사장 19)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `point-mapper.xml` | `saleson.shop.point.PointMapper` | 47 | **9** |
| `order-give-point-mapper.xml` | `...order.givepoint.OrderGivePointMapper` | 16 | 0 |
| `give-point-mapper.xml` | `...give.givepoint.GivePointMapper` | 8 | 0 |
| `give-point-expiration-mapper.xml` | `...givepointexpiration.GivePointExpirationMapper` | 5 | 0 |
| `pointcheck-mapper.xml` | `...pointcheck.PointCheckMapper` | 2 | 0 |
| **`starpoint-mapper.xmlx`** | `jp.worldjb.web.StarPointMapper` | **19** | **19 (전부)** |
| **합계** | | **97** | **28** |

### 3-1. `starpoint-mapper.xmlx` — 파일 확장자 때문에 통째 사장
`DatabaseConfig:87`의 스캔 패턴은 `classpath*:sqlmapper/<db>/*-mapper.xml`이다. 이 파일은 확장자가 **`.xmlx`**라 **MyBatis에 로드되지 않는다**. 자바 쪽 `StarPoint` 참조도 **0건**. 원제품(일본 SalesOn) 잔재다.

### 3-2. `point-mapper.xml` 사장 9건
- **XML 고아**(인터페이스 미선언): `getPointConfigById`, `getPointConfigList`, `updatePointConfig`
- 호출 0: `getAvailablePointListByUserId`, `getExpirationPointSendMessage`, `getOrderReturnPointByParam`, `getOrderReturnPointTargetUsedHistoryByParam`, `getPointExpirationDateByPointId`, `getReturnPointListByParam`

> `getOrderReturnPoint*`/`getReturnPointListByParam`이 호출 0이라 "반품 포인트 회수가 안 도는가" 싶지만 아니다. 실제 경로는 `OrderClaimApplyServiceImpl.returnPoint()`(`:1120~1152`)이며 **적립 메서드를 재사용**한다 — `pointService.earnPoint("return", point)`. donation의 `getUserCntrLimit`와 같은 유형(쓰이지 않는 병행 구현).

## 4. 서비스 로직 대조

### 4-1. 기부포인트 잔액 집계 (`getCntrPointInfo`, `mypage-mapper.xml:79~127`)
```sql
SELECT ... , CNTR_POINT - CNTR_USE_POINT AS CNTR_BLCE_POINT
FROM G_CNTR A JOIN G_LOCGOV C ...
WHERE A.CNTR_STTUS_CODE = '200' AND A.DELETE_AT = 'N'
  AND A.STTEMNT_PAY_DE IS NOT NULL       -- 납부일이 있어야 포인트로 인정
GROUP BY A.CNTR_LOCGOV_CODE
```
- 사용포인트: `G_CNTR_USE_POINT`를 **회원+지자체** 기준으로 합산(기부건 단위가 아니다)
- `USER_GRADE`: `G_HONOR_CNTRBTR` 존재 여부로 `'일반'`/`'명예기부자'` — **쿼리는 계산하지만 화면에서 쓰지 않는다**(`cntrPoint.html`에 표시 없음) → 사실상 죽은 컬럼
- `DETAIL`: 연계기관(`LINK_INSTT_CD`) 중 건수 1위를 뽑아 `"OOO 등 N건"`, 없으면 `'고향사랑e음'`

### 4-2. 포인트 반환 (`processReturnPoint`, `PointServiceImpl:762~`)
AS-IS는 새 포인트를 발행하지 않고 **사용이력을 역순으로 되감는다**: `getPointUsedListByOrderCode(orderCode)`로 그 주문의 사용분을 가져와 `remainingPoint`를 복구하고 원본 lot을 되살린다.
MSA는 원장 모델이라 `RESTORE` 행을 추가하고 lot 잔량을 되돌린다 — 모델이 달라 형태는 다르지만 **결과(원래 lot으로 복원)는 같다**.

### 4-3. 만료 정책
| | AS-IS | MSA |
|---|---|---|
| 기부포인트 만료 기준 | `G_CNTR.POINT_END_DE < 오늘` (`give-point-expiration-mapper.xml`) | `PT_POINT_LEDGER.EXPIRATION_DATE` (lot별) |
| 처리 | `updateCntrBlcePointDel` + **`insertCntrUsePoint`** — 소멸분을 **사용 이력으로 기록** | `EXPIRE` 원장행 추가 + 잔액 차감 |
| 원제품 포인트 만료 | `expirationPoint()` 일배치. **휴면회원도 대상**(회원상태코드를 조회에서 제외, `:365` 주석) | 해당 없음(원제품 체계 미사용) |
| 만료 예고 | `expirationPointSendMessage()` (`getExpirationPointSendMessage` 쿼리는 호출 0) | `/api/my/points` 만료예정 + `pt_rm_expiring_point` |
| 만료일 없을 때 | `PointUtils.getExpirationMonth()`가 0이면 **200년 후로 설정**("만료일 없으면 무제한 한 200년쯤 더해버려") | 확인필요 |

### 4-4. **탈퇴 시 잔여 기부포인트 소멸 — 재현누락**
AS-IS `GeneralCustomerServiceImpl:286~296`은 회원 탈퇴 시 `getCntrBlcePointList(userId)` → `updateCntrBlcePointDel` + `insertCntrUsePoint`로 **잔여 기부포인트를 소멸 처리**한다(만료 배치와 같은 처리).
MSA는 탈퇴 화면에 잔여포인트를 **보여주기만** 하고(`PointApiController:114` `/api/locgov-point-summary`) 소멸시키지 않는다. → 탈퇴 회원의 포인트가 원장에 남는다.

## 5. 화면 대조

### 5-1. `cntrPoint.html` → MSA `my.html`
| AS-IS 이벤트 | MSA | 판정 |
|---|---|---|
| `@change="paging"` | 페이징 있음 | 대응있음 |
| `@click="closeTableOverlap()"` (가로스크롤 안내 오버레이 닫기) | `onclick="this.parentElement.style.display='none'"` | 대응있음 |
| `@click="goToLocgovMall(data)"` (답례품 몰 가기) | `td.loc_mall` 버튼 | 대응있음 |

**표 열**
- AS-IS: No / 기부지자체(시·도, 시·군·구) / 적립 / 사용 / 잔여 / 기부처 / 답례품
- MSA: No / **기부연도** / 기부지자체 / **기부금액** / 적립 / 사용 / 잔여 / 기부처 / 답례품
→ MSA에 **기부연도·기부금액 2열이 추가**됐다. RFP/ISP 근거 확인 필요.

**기부처 열이 하드코딩이다** — MSA `my.html:128`은 항상 `고향사랑e음`을 출력한다. AS-IS는 `data.detail`이 비었을 때만 그 문구를 쓰고, 값이 있으면 `"<연계기관> 등 N건"`을 보여준다(`cntrPoint.html:127~129`). → **연계기관 경유 기부의 기부처 표시가 틀린다.**

### 5-2. `cntrPointDetail.html` → MSA `detail.html`
| AS-IS 이벤트 | MSA | 판정 |
|---|---|---|
| `@change="paging"` | 있음 | 대응있음 |
| `@click="goToList()"` (목록으로) | 있음 | 대응있음 |
| `@click="goOrderInfo(data)"` (주문번호 → 주문상세) | 링크 없음 | **재현누락** |
| `@click="closeTableOverlap()"` | 있음 | 대응있음 |

**표 열**
- AS-IS: No / 발생일자 / 기부액(실납부액) / 적립포인트 / 사용포인트 / **잔여포인트** / 답례품 주문번호
- MSA: No / 발생일자 / **구분** / 기부액(실납부액) / 적립포인트 / 사용포인트 / 답례품 주문번호
→ MSA는 **잔여포인트 열이 없고** 원장 모델이라 **구분(txnType)** 열이 추가됐다.

## 6. 조치 후보

**A. 재현누락**
1. **탈퇴 시 잔여 기부포인트 소멸**(§4-4) — 회계상 남으면 안 되는 잔액이다. 가장 시급.
2. **기부처 열 하드코딩**(§5-1) — 연계기관 표시.
3. `cntrPointDetail`의 주문번호 → 주문상세 링크.
4. `cntrPointDetail`의 잔여포인트 열.

**B. 근거 확인 필요**
- MSA `my.html`의 추가 2열(기부연도·기부금액)
- MSA `detail.html`의 구분 열 (원장 모델상 필요해 보이나 RFP 근거 확인)
- `STTEMNT_PAY_DE IS NOT NULL`(납부일 있어야 포인트 인정)이 MSA의 완료상태 판정과 등가인지
- 만료일 미설정 시 AS-IS의 "200년 후" 처리에 대응하는 MSA 동작

**C. 이식 금지 — AS-IS에서 이미 죽은 것**
- `starpoint-mapper.xmlx` 19쿼리(확장자 탓에 미로딩, 자바 참조 0)
- `point-mapper` 사장 9건
- `/api/mypage/2/points`
- `USER_GRADE` 계산(쿼리만 있고 화면 미사용)
- 원제품 포인트 체계 전체(`OP_POINT`/`OP_POINT_USED`/`OP_POINT_CONFIG`) — 회원가입·상품평·관리자적립 포인트는 고향사랑e음 기능이 아니다

**D. 갱신할 판단**
- [[point-reservation-unused-decision-deferred]] — 예약 기능은 RFP SFR-004·ISP 명시 요건. 숨김이 아니라 **체크아웃 재배선**이 맞다.
- [[point-readmodel-paused-pending-db-design]] — "만료 예정 포인트 안내 ReadModel"도 SFR-004 명시 요건.

---

## 7. 조치 결과 (2026-09-10)

### 7-1. 완료 (4건)

**1. 탈퇴 시 잔여 기부포인트 소멸** (§4-4)
AS-IS `GeneralCustomerServiceImpl:286~296`은 탈퇴 처리 안에서 만료 배치와 **똑같은 소멸**을 돈다. MSA는 탈퇴 화면에 잔여를 보여주기만 하고 소멸시키지 않아, 탈퇴 회원의 포인트가 원장에 살아 있었다.
- point: `PointService.expireAllOnWithdrawal(userId)` — 만료 배치와 같은 모양으로 `EXPIRE` 원장행을 남기고 잔액을 깎는다(사유 문구만 "회원 탈퇴에 따른 소멸"로 구분). `POST /api/admin/expire-on-withdrawal`.
- member: `PointClient.expireAllOnWithdrawal()` — **조회 실패를 0으로 삼키는 다른 메서드와 달리 예외를 그대로 올린다.** 소멸에 실패했는데 탈퇴만 진행되면 잔액이 남는다.
- **탈퇴 두 경로 모두**에 걸었다: `MemberService.withdraw`(본인), `AdminMemberService.adminWithdraw`(강제).
- 배선 중 발견: member의 `PointClient`가 `X-Internal-Secret`을 보내지 않아 point의 `/api/admin/*`에서 401이 났다(donation의 `MemberClient`는 보내고 있었다). 헤더를 추가했다.

**2. "기부처" 열 하드코딩 제거** (§5-1)
MSA `my.html:128`이 항상 `고향사랑e음`을 출력하던 것을 AS-IS `DETAIL` 계산으로 교체했다.
- donation: `Donation.linkInsttCd` 필드 추가(컬럼은 있었으나 **엔티티에 매핑돼 있지 않았다**), `DonationService.donationSourcesByLocgov(userId)` — 지자체별로 연계기관을 건수 내림차순으로 세어 1위를 고르고, 둘 이상이면 `"OOO 등 N건"`. `GET /api/donation-sources`.
- point: `LocgovClient.donationSourcesOf(userId)` — 표시용이라 실패 시 빈 맵(그러면 전 행이 `고향사랑e음`, 지금과 같은 모습). `PointLocgovRow.donationSource` 추가.
- AS-IS의 DETAIL 서브쿼리는 상태 필터가 없으므로 여기서도 상태로 거르지 않았다.

**3. 상세화면 주문번호 → 주문상세 링크** (§5-2)
AS-IS `cntrPointDetail.html:121`은 주문번호가 있을 때만 버튼으로 만든다. MSA에서 `REF_KEY`는 거래유형마다 의미가 달라(적립=기부번호, 사용/복원=주문번호) **`USE`/`RESTORE` 행에만** 링크를 건다.

**4. 상세화면 잔여포인트 열** (§5-2)
AS-IS는 SQL 상관 서브쿼리(`CURRENT_CNTR_POINT − CURRENT_CNTR_USE_POINT`)로 **그 시점까지의 누적 잔액**을 만든다. 원장 모델에서는 발생순으로 훑으며 더하면 같은 값이 나온다(`ledgerDetail()`이 이미 `createdDate` 오름차순). `PointDetailRow` 래퍼로 행마다 붙였다.

### 7-2. 검증
member·donation·point 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 100만원 기부 → 적립 | `EARN 300000`, 잔액 300,000 |
| 탈퇴 실행 | `EXPIRE -300000` 원장행 생성, **잔액 0** |
| 연계기관 없음(현 데이터) | 전 행 `고향사랑e음` — AS-IS와 동일 |
| 연계기관 1종 주입 | `{"26350":"농협","46150":"우체국"}` → 화면에 기관명, 나머지는 `고향사랑e음` |
| 같은 지자체에 2종 | `{"46150":"농협 등 2건"}` → **"등 N건" 분기 동작** |
| 잔여포인트 누적 (원장 15행) | `450,000 → 465,000 → 385,000 → … → 338,500` — DB 원장과 일치 |

검증 데이터(회원 1, 기부 1, 포인트원장·잔액, 탈퇴스냅샷, 연계기관 공통코드 2건, `link_instt_cd` 2건)는 전부 삭제·원복 확인.

### 7-3. 미처리 — 확인이 필요한 것
- **`STTEMNT_PAY_DE IS NOT NULL`** (납부일이 있어야 포인트로 인정)이 MSA의 `COMPLETED` 판정과 등가인지. AS-IS는 상태와 **별개로** 납부일 존재를 요구한다.
- MSA `my.html`의 추가 2열(기부연도·기부금액) RFP/ISP 근거.
- 만료일 미설정 시 AS-IS의 "200년 후" 처리에 대응하는 MSA 동작.
- `PointCheckManagerController`(포인트 점검) 대응.
