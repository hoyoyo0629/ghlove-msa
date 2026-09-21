# 포인트(point) 서비스 상세분석 — 2026-09-09

분석 기준(사용자 확정): **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본:
- AS-IS: `C:\workspace\ghlove`
  - 매퍼 `ghlove-common/src/main/resources/sqlmapper/cubrid/{give-point,give-point-expiration,order-give-point}-mapper.xml`
  - 화면 `ghlove-frontend/mypage/cntrPoint.html`, `cntrPointDetail.html`
  - 관리자 `ghlove-web/.../opmanager/i18n/give/give-point/{list,list_locgov,form}.jsp`
- RFP: `docs/requirements.md` SFR-004
- ISP: `docs/isp-detailed-design-summary.md` (원본 p.367 엔티티명세, p.415/427 이벤트스토밍, p.521 DB분리)

> **참고**: AS-IS `point-mapper.xml`(47쿼리)는 `OP_POINT`/`OP_POINT_CONFIG`/`OP_POINT_USED`를 다루는 **saleson 쇼핑몰 범용 적립금**으로, 고향사랑 기부포인트(`G_CNTR`/`G_CNTR_USE_POINT`)와 별개 체계다. 이 분석과 MSA 구현 범위는 기부포인트 쪽이다.

---

## 1. 요구사항 대비 충족 현황

| RFP SFR-004 항목 | 상태 | 비고 |
|---|---|---|
| 지자체별 적립규칙 DB 관리(하드코딩 금지) | 충족 | `pt_locgov_point_rate` 6건(연도별, 46150=25%), `DEFAULT_POINT_RATE`/`MAX_POINT_RATE` 공통코드 폴백 |
| 원장/잔액 테이블 분리 | 충족 | `PT_POINT_LEDGER` / `PT_POINT_BALANCE` |
| 적립·차감·소멸·복원·조정 | 충족 | `creditForDonation`/`deductForOrder`/`expire`/`restoreForOrder`/`reverseForDonation` |
| 예약/예약해제 | **부분** | 구현돼 있으나 실제 호출부 0곳 (§3-3) |
| 주문클레임 이벤트 구독 | 충족 | 반품승인도 `publishCancelled()`로 `ORDER_CANCELLED`가 나가 복원됨 — 이벤트 이름만 RFP 문구와 다르고 동작은 동일 |
| SAGA 보상 트랜잭션 | 충족 | `POINT_DEDUCTED`/`POINT_DEDUCT_FAILED` 발행 |
| **CQRS ReadModel 조회 제공** | **미충족** | §3-1 |

ISP 대조:
- 도메인 경계·엔티티(잔액/원장) 설계는 일치.
- ISP 도메인 이벤트 6종(적립됨/**예약됨**/사용됨/**해제됨**/환불됨/소멸됨) 중 **예약됨·해제됨 미발행**(§3-2).
- ISP 조회모델 "포인트잔액/원장/**만료뷰**" — 별도 뷰 없이 원장 직접 집계(§3-1).

---

## 2. 정정 기록

이전 세션에서 "포인트 예약은 근거가 RFP 한 줄뿐이고 AS-IS에 없다"고 판단했으나 **틀렸다**. ISP 이벤트스토밍 표(p.415/427)에 포인트관리 명령 = "포인트적립, **예약/사용, 예약해제**, 환불, 소멸", 도메인 이벤트에 "**포인트예약됨/해제됨**"이 명시돼 있고 엔티티 명세(p.367)도 단위기능에 포함한다. **삭제 후보가 아니라 미완성 기능**이다. AS-IS만 보고 판단한 결과였다.

---

## 3. 기능 갭

### 3-1. CQRS ReadModel 부재 (요구사항 미충족, 중)
RFP "거래내역·잔액·지자체별 사용현황·만료예정을 **CQRS 기반 ReadModel**로 제공", ISP "조회모델: 포인트잔액/원장/만료뷰". 실제는 조회 때마다 원장 직접 집계. `balanceByLocgov()`가 매 호출마다 사용자의 미소진 lot 전체를 읽어 스트림 필터링하며, 장바구니가 **지자체 그룹 수만큼 반복 호출**한다. admin 통계용 이벤트(`PointLedgerEvent`)는 있으나 point 자신의 조회 모델은 없다.

### 3-2. 예약/해제 이벤트 미발행 (요구사항 미충족, 소)
`reserve()`/`releaseReservation()` 안에 publish 호출 0건. 적립/사용/환불/소멸은 `PointLedgerPublisher`로 발행됨.

### 3-3. 예약 기능 실사용 경로 없음
ISP 정식 설계 기능이나 호출부 0곳. 실제 결제는 `ORDER_CREATED` → `deductForOrder` 즉시 차감. 별도 기록: `point-reservation-unused-decision-deferred` 메모리.

### 3-4. `cntrPointDetail` 화면 통째 누락 (재현 누락, 중)
AS-IS는 지자체별 상세(발생일자 / 기부액(실납부액) / 적립 / 사용 / 잔여 / 답례품 주문번호)를 별도 화면으로 제공하고 요약표의 사용포인트 셀에서 아이콘(`short_icon`)으로 진입한다. MSA는 화면·API·라우트·진입링크 전부 없음. AS-IS 쿼리 `getGivePointDetail`/`getGivePointDetailSum`/`getGivePointDetailList` 3개 미구현.

### 3-5. 연도(CNTR_YEAR) 차원 없음 (재현 누락, 중)
AS-IS `getGivePointList`는 `GROUP BY year(CNTR_DE), 지자체` + 연도 DESC 정렬 + 연도범위 검색(`shCntrYearStart/End`). MSA는 지자체로만 그룹핑. 정작 적립률 테이블은 `stdr_year`를 가져 **적립은 연도별 관리, 조회는 연도 무시**인 불일치.

### 3-6. 기부금액(CNTR_AMT) 미표시 (재현 누락, 소)
AS-IS 요약 쿼리는 지자체별 기부금액을 함께 반환.

### 3-7. 소멸 예정 안내 발송 없음 (의도적 축소로 판단)
AS-IS `getGivePointExpirationMailInfoList`로 만료예정자 메일 발송. MSA는 화면 안내만. UMS/메일 연계를 `enabled:false`로 둔 프로젝트 방침과 일관하므로 **의도적 축소**로 분류하되, 연계 개방 시 착수 대상.

---

## 4. 레이아웃 갭 (AS-IS `cntrPoint.html` 기준)

일치 확인: 상단 제목·안내문구·회원명 배지·"최대 3일" 문구, 3칸 요약카드(`mypage-point-list`), 지자체별 표 2단 헤더.
`research-box.css`는 AS-IS가 링크만 하고 실제 클래스를 쓰지 않아 미포함이어도 무방(클래스 대조 확인).

| 항목 | AS-IS | MSA(분석 시점) |
|---|---|---|
| 사용포인트 셀 상세링크(`short_icon`) | 있음 | **없음** (§3-4와 한 세트) |
| 페이지네이션 | `<pagination>` | 없음 |
| 가로 스와이프 안내(`table-overlap`) | 있음 | 없음 |
| 기부처 컬럼 | `data.detail` 우선, 없으면 '고향사랑e음' | '고향사랑e음' 하드코딩 |
| 거래내역 표 | `cntrPointDetail.html`로 분리 | 요약 화면에 같이 붙음, 스타일 없음 |
| 포인트 사용 폼 | 없음 | **회원 화면에 노출**(주문 외 임의 차감 테스트 기능) |
| 예약중/가용잔액·소멸예정 안내 | 없음 | 있음 (ISP 근거 있는 추가분 — 유지 타당) |

---

## 5. 별건 (포인트 서비스 밖)

`donation.g_locgov`에 **`11230`과 `11680`이 둘 다 "강남구"**로 등록돼 있다(행정표준코드상 11230=동대문구, 11680=강남구). 포인트 조회 화면에 강남구가 두 줄로 나오는 원인. 시드 데이터 문제이며, `11230`을 참조하는 답례품명("강남구 한우 선물세트") 등과 함께 정리해야 해서 **이번 범위에서 제외**.

---

## 6. 조치 내역 (2026-09-09)

### 구현
| # | 항목 | 내용 |
|---|---|---|
| 3-4 | **`cntrPointDetail` 대응 화면 신설** | `point/detail.html` + `GET /detail?locgovCode=&year=`. AS-IS와 같은 구성(지자체 배지 → 적립/사용/총잔여 3칸 카드 → 발생일자·구분·기부액·적립·사용·주문번호 표). 요약표 사용포인트 셀에 AS-IS의 `short_icon` 진입 아이콘 추가 |
| 3-5 | **연도 차원 도입** | `PT_POINT_LEDGER`에 `STDR_YEAR` 추가. 적립 행은 `DONATION_COMPLETED` 이벤트의 `CNTR_DE`에서, 나머지는 `@PrePersist`로 원장 생성연도에서 채움. 기존 32행은 생성연도로 백필. 요약을 **기부연도+지자체**로 묶고 연도 내림차순 정렬(AS-IS `getGivePointList`와 동일). `?yearFrom=&yearTo=`로 범위 필터 |
| 3-6 | **기부금액 컬럼** | `PT_POINT_LEDGER.CNTR_AMT` 추가(이벤트의 `cntrAmt` 저장). 요약표에 "기부금액" 열 추가. 마이그레이션 이전 행은 값이 없어 `-` 표시 |
| 3-2 | **예약/해제 이벤트 발행** | `PointReservationEvent` + `PointReservationPublisher`(토픽 `point.reservation`, 헤더 `eventType`=`POINT_RESERVED`/`POINT_RESERVATION_RELEASED`). ISP 도메인 이벤트 "포인트예약됨/해제됨" 충족. 확정은 USE 원장행이 생기며 기존 `PointLedgerEvent`가 "사용됨"을 이미 알리므로 중복 발행하지 않음 |
| 4 | **가로 스와이프 안내** | AS-IS `table-overlap` 블록을 요약·상세 두 화면에 추가 |
| 4 | **수동 차감 기능 게이팅** | AS-IS에 없는 "포인트 사용(테스트)" 폼을 `ghlove.dev.manual-point-use`(기본 false)로 게이팅. 화면 노출·Thymeleaf 엔드포인트·**SPA용 JSON API 세 곳 모두** 차단 — 처음엔 화면과 Thymeleaf만 막아 JSON 경로로 우회 가능했던 것을 같이 닫음 |
| 4 | **거래내역 표 이동** | 요약 화면에 스타일 없이 붙어 있던 거래내역 표를 제거하고 상세 화면으로 옮김(AS-IS의 화면 분리와 동일) |

검증: 요약/상세 화면 렌더 확인(요약 5행 10열 + 상세링크 10개, 상세 15행), 지자체별 원장합계 = lot합계 일치 재확인.

> 연도 검색 UI는 **일부러 넣지 않았다.** AS-IS `cntrPoint.html`에는 검색상자가 없고(연도 범위 조건은 쿼리에만 있으며 관리자 화면에서 쓴다), 레이아웃 기준이 AS-IS이므로 화면에는 두지 않고 URL 파라미터로만 남겼다.

## 7. 잔여 (미조치)

| # | 항목 | 사유 |
|---|---|---|
| 3-1 | CQRS ReadModel 부재 | **착수했다가 DB 설계 완료 후 재개하기로 2026-09-09 중지** — 중간 상태와 재개 시 설계 결정은 §8 |
| 3-3 | 예약 기능 실사용 경로 없음 | 체크아웃 재배선 vs 회원화면 숨김 — 제품 결정 필요(보류 중) |
| 3-7 | 소멸 예정 안내 발송 | UMS/메일 연계 미개방(의도적 축소). 연계 개방 시 착수 |
| 4 | 페이지네이션 | AS-IS에 있으나 현재 데이터 규모에서 체감 없음. 목록이 커지면 착수 |
| 4 | 기부처 컬럼 동적화 | AS-IS `data.detail`(지정기부 사업명)은 donation 연계가 필요 — 지정기부 사업 정보를 이벤트로 실어오거나 조회 API를 붙여야 함 |
| 5 | `g_locgov` 11230/11680 중복 명칭 | donation 시드 데이터 문제이며 `11230`을 참조하는 답례품명 등과 함께 정리해야 해 별도 건 |

---

## 8. ReadModel 작업 중간 상태 (2026-09-09 중지)

DB 설계가 진행 중이라, 테이블 구조가 그 결과와 어긋나 두 번 일하는 것을 피하려고 중간에 멈췄다.

**적용된 것**
- 개발 DB `point` 스키마에 조회 전용 테이블 2개 생성(둘 다 비어 있음)
  - `pt_rm_locgov_point` — PK(user_id, locgov_code, stdr_year), cntr_amt / earned_amount / used_amount / remaining_amount / lot_remaining / updated_date
  - `pt_rm_expiring_point` — PK(user_id, expiration_date, locgov_code), remaining_amount / updated_date
- `database/ddl/service-point.sql` 끝에 위 DDL + 설계 의도 주석
- `point/src/main/java/com/ghlove/point/readmodel/` 6개 파일(엔티티 2, 복합키 2, 리포지토리 2)

**안 한 것**: 프로젝션 서비스, `point.ledger` 자가 구독 리스너, 조회 경로 전환, 기존 데이터 백필, 재동기화 엔드포인트.

**동작 영향 없음** — 자바 코드를 컴파일·배포하지 않았다. 8083의 point jar는 이전 빌드라 새 테이블·클래스를 모른다. 재개 시 `cd point && ./gradlew bootJar` 후 재기동해야 반영된다. 되돌리려면 테이블 2개 drop + 위 6개 파일 삭제 + DDL 블록 제거.

**재개 시 지킬 설계 결정**
1. 쓰기모델은 손대지 않는다. 원장행마다 이미 나가는 `point.ledger`를 point가 스스로 구독해 해당 회원분을 **통째로 재계산**한다 — 델타 누적이 아니라 전량 재계산이라 Kafka at-least-once 재전달·컨슈머 재시작에도 값이 어긋나지 않는다.
2. **차감·예약 가능 여부 판단에는 ReadModel을 쓰지 않는다.** 이벤트 지연만큼 뒤처져 초과 사용이 가능해진다 — `deductForOrder`/`reserve`/`confirmReservation`/`usePoints`와 order가 부르는 `/api/balance-by-locgov`는 계속 쓰기모델을 직접 읽고, ReadModel은 표시용 조회 전용(마이페이지 요약·상세, 만료 안내)으로 쓴다.
3. `remaining_amount`(원장 부호합)와 `lot_remaining`(미소진 lot 합)을 한 행에 나란히 둔다 — 정상이면 같아야 하고, 어긋나면 지자체 없는 차감 같은 사고 신호다(§3-3의 과거 사고와 같은 유형).

---

## 9. 결함 수정 — 취소분이 적립·사용에 이중 가산 (2026-09-10)

### 증상
기능 테스트 중 발견. 100,000원 기부(→30,000P) → 답례품 주문에 30,000P 사용 → **주문 취소** 후 "기부포인트 조회"를 보면 **적립포인트와 사용포인트가 나란히 30,000P씩 부풀어 있었다.** 잔여는 우연히 맞았다.

실측(userId 1063, 제주 50000/2026):

| | 수정 전 | 수정 후 |
|---|---|---|
| 적립 | 60,000 | **30,000** |
| 사용 | 30,000 | **0** |
| 잔여 | 30,000 | 30,000 |

### 원인
`PT_POINT_LEDGER`는 append-only다 — 취소가 원본 행을 지우지 않고 **반대 부호의 행을 덧붙인다**(주문취소=`RESTORE` 양수, 기부취소=`REVERSE` 음수). 그런데 집계가 **거래유형이 아니라 부호로** 적립/사용을 갈랐다:

```java
long earned = rows.stream()...filter(a -> a > 0).sum();   // RESTORE가 적립으로 둔갑
long used   = rows.stream()...filter(a -> a < 0).sum();   // REVERSE가 사용으로 둔갑
```

`잔여 = 적립 - 사용`이라 **총합만 우연히 맞고 두 열이 동시에 부풀었다.** 이 때문에 오래 안 드러났다.

### AS-IS 대조 (판단 근거)
AS-IS는 애초에 이 문제가 생길 수 없는 구조다.
- **적립**: `give-point-mapper.getGivePointSum`이 `SUM(G_CNTR.CNTR_POINT)`를 취하되 **`cntr_sttus_code='200'`(납부완료)만** 센다 → 취소된 기부는 집계에서 통째로 빠진다.
- **사용**: `SUM(G_CNTR_USE_POINT.CNTR_USE_POINT)`인데, 주문취소·환불 시 `deleteGiveUsePoint`로 **사용내역 행 자체를 지운다**(`OrderServiceImpl` 2곳, `OrderRefundServiceImpl` 1곳) → 취소된 사용도 집계에서 빠진다.

즉 **"취소분은 양쪽에서 사라진다"가 AS-IS의 의미**이고, append-only 원장에서 같은 결과를 내려면 거래유형으로 상계해야 한다.

### 조치
`PointService`에 집계기를 하나로 모으고 네 군데 호출부를 전부 그쪽으로 돌렸다.

- `earnedOf(rows)` = **EARN + REVERSE** (회수는 음수라 적립에서 차감)
- `usedOf(rows)` = **-(USE + RESTORE + EXPIRE)** (복원은 양수라 사용에서 차감)
- `donatedAmountOf(rows)` = 적립 행의 기부금액 합에서 **REVERSE 행이 달린 CNTR_SN 제외** — AS-IS가 `cntr_sttus_code='200'`만 세는 것과 같은 의미다. 기존에는 취소된 기부 10,000원이 기부금액 열에 남아 있었다(강남구 110,000원 → 100,000원으로 정정됨).

**소멸(EXPIRE)은 사용 쪽에 넣었다.** 화면에 소멸 칸이 따로 없어(AS-IS도 3열) 여기서 빼면 `적립 - 사용`이 실제 잔액과 어긋난다. admin `PointHistoryAdminService`가 이미 "소멸은 발행이 아니라 소진이라 사용 쪽으로 집계한다"고 같은 결정을 해 둔 것과 맞췄다.

**고친 호출부 4곳** — `ledgerSummaryByYearAndLocgov`(기부포인트 조회 목록), `ledgerSummaryByLocgov`(지자체별 요약), `totalEarnedOf`/`totalUsedOf`(총 적립·총 사용 카드 + SPA `/api/my/points`), `PointController.detail`(상세 3칸 카드).

**상세화면 표(`detail.html`)도 같이 고쳤다.** 행의 적립/사용 열을 부호가 아니라 거래유형으로 가른다 — 취소 행은 **자기 열에 음수로** 찍힌다(주문취소 복원 → 사용 열 -30,000). 이렇게 해야 열 합계가 상단 3칸 카드와 맞는다. 이전에는 복원 행이 적립 열에 양수로 들어가 표와 카드가 서로 다른 값을 보였다.

**DB 수정 없음.** 원장·잔액 데이터는 처음부터 정확했다(잔액 60,000 = 강남 30,000 + 제주 30,000). 순수하게 조회 집계만의 결함이다.

### 검증 (실측)
- 목록: 강남구 2026 `기부금액 100,000원 / 적립 30,000 / 사용 0 / 잔여 30,000`, 제주 2026 동일 — 취소 기부 10,000원과 취소 주문 30,000P가 양쪽에서 빠졌다.
- 카드: 총 적립 60,000 / 총 사용 0 / 총 잔여 60,000(실제 `PT_POINT_BALANCE`와 일치).
- 상세(제주): 카드 30,000/0/30,000, 표는 `적립 +30,000`, `사용 30,000`, `주문취소 복원 -30,000` → **열 합계 = 카드**.
- SPA `/api/my/points`: `totalEarned 60000 / totalUsed 0`. 원장 배열은 부호 그대로 노출(SPA는 적립/사용 열을 나누지 않고 ±로 한 열에 찍으므로 정상).

### 남은 판단거리 (미조치)
admin **"일별 포인트 현황"**(`point-history/daily.html`)은 `EARN_TYPES={EARN, RESTORE}` / `USE_TYPES={USE, REVERSE}`로 집계한다. 취소 복원이 그날의 "발생포인트"로, 기부취소 회수가 "사용포인트"로 잡힌다는 뜻이다. **일별 증감 리포트로 읽으면 맞고, "그날 발행된 포인트"로 읽으면 부풀려진 값**이다. 마이페이지와 달리 누적 잔액이 아니라 일자별 움직임을 보는 화면이라 성격이 달라 손대지 않았다 — admin 분석 때 어느 의미로 쓸지 정해야 한다.

---

## 10. 기부포인트 조회+상세 AS-IS 재현 (2026-09-21) → 조치 완료

§3-4(상세화면 누락)·§3-5(연도 차원 없음)·§3-6(기부금액 미표시)를 한 묶음으로 구현했다. AS-IS `mypage/cntrPoint.html`(목록)+`cntrPointDetail.html`(상세) 재현.

### 목록 (MyPointsView) — §3-5, §3-6
- `/api/my/points`에 **(기부연도+지자체) 그룹 요약**(`yearLocgovSummary`, 기부금액 포함)과 `years`를 추가. 목록을 지자체-only(`ledgerSummaryByLocgov`)에서 (연도,지자체)(`ledgerSummaryByYearAndLocgov`)로 전환 — AS-IS `getGivePointList`와 같은 단위(연도 DESC). 백엔드 메서드는 이미 있었고 API 노출·SPA 배선만 했다.
- 사용포인트 칸에 **상세 진입 아이콘**(AS-IS `short_icon`) 추가 → 상세화면으로.
- AS-IS처럼 연도 컬럼은 화면에 표시하지 않는다(행만 연도별로 분리).

### 상세 (MyPointDetailView, 신규) — §3-4
- 라우트 `/mypage/points/detail?locgovCode=`(연도 없음), 신규 API `/api/my/points/detail`.
- **거래 원장 구조**(AS-IS `getCntrPointDetail`의 UNION ALL 재현): 적립(기부) 행과 사용(구매) 행을 **각각** 시간순(DESC)으로 나열하고, 각 행에 **그 시점까지의 러닝 잔액**(누적적립−누적사용)을 매긴다.
  - **적립 행** = EARN 원장: 발생일자·기부액(CNTR_AMT)·적립 채움, 사용·주문번호 빈칸. 취소(REVERSE)된 기부 제외.
  - **사용 행** = USE 원장 + 소멸(EXPIRE): 발생일자·사용·**답례품 주문번호(REF_KEY)** 채움, 기부액·적립 빈칸. 취소(RESTORE)된 주문의 USE 제외.
- **소스는 실제 원장 `PT_POINT_LEDGER`**다. 목록(`ledgerSummary`)과 같은 원장을 itemize하므로 목록의 적립·사용·잔여 합계와 **정확히 일치**한다. 답례품 주문번호는 USE 원장행의 REF_KEY(주문차감=orderId)에 이미 들어 있다.
- 상단 적립/사용/잔여 총합 카드. 연도 필터 없이 지자체 단위(AS-IS 상세 링크도 locgovCode만).

- **정정 이력(2026-09-21)**:
  1. 최초 "기부건별 1행 집계"로 잘못 구현 → 사용자 지적으로 AS-IS 매퍼 재분석 후 거래원장 구조로 재작성.
  2. 그 재작성이 사용행을 신규 `g_cntr_use_point`에서만 읽어 **과거 사용분이 안 보이는 버그** + 과거 EARN의 기부액 NULL 문제 발생(사용자 재지적). **상세를 원장 직접 읽기로 다시 수정**(위)하고, 과거 EARN의 `cntr_amt`는 `donation.g_cntr`에서 백필(superuser cross-schema UPDATE 16행, e2e-* 시드행은 실제 기부건 없어 NULL 유지). 이로써 목록↔상세 합계 일치·과거분 표시 확인(user 1000/강남구: 적립665,000·사용326,500·잔여338,500).
  3. 결과적으로 **`g_cntr_use_point` 추적 인프라는 상세화면에 불필요**해졌다(USE 원장 refKey에 주문번호가 이미 있음). 쓰기 자체는 남아 있으나 보조 기록이다. [[point-use-tracking-g-cntr-use-point]]

### 마이페이지 기부 서브메뉴바(lnb-bar_3dep) — 신규
AS-IS `mypage-lnb`의 3뎁스 서브메뉴가 TO-BE에 없어서 신설. 공통 컴포넌트 `MypageDonationNav.vue`(기부내역현황=`/mypage/donations`, 기부포인트현황=`/mypage/points`, 기부확인증=`/mypage/receipts`, 기부혜택증(지자체별)=`/mypage/honor-certificates`, 활성 클래스 `currentP`)를 만들어 5개 화면(MyDonations/MyPoints/MyPointDetail/ReceiptList/HonorCertificates)에 얹었다. `.lnb-bar_3dep` CSS는 이미 default_ali.css에 있었다.

### 답례품 주문번호 링크 추적 (신규 인프라)
AS-IS는 `G_CNTR_USE_POINT`에 "어느 기부건 포인트를 어느 주문이 썼는지"를 남겨 상세의 주문번호를 채운다. MSA 활성 모델(`pt_point_ledger`)은 이 링크를 추적하지 않아 **사용자 결정으로 추적 인프라를 신설**했다.
- `consumeLots`가 FIFO로 소진하는 **적립 lot마다**(EARN 원장행, REF_KEY=기부건번호) `g_cntr_use_point`에 (cntr_sn, order_code, 사용액, 지자체) 한 행씩 기록. 3개 소진 경로(주문차감 `deductForOrder`, 수동사용 `usePoints`, 예약확정 `confirmReservation`) 전부에 orderCode를 전달.
- 주문취소 복원 `restoreForOrder`가 `deleteByOrderCode`로 그 주문의 사용이력을 삭제(AS-IS `deleteGiveUsePoint`) — 취소분은 상세의 기부건별 사용에서 사라진다.
- 신규: `GCntrUsePoint` 엔티티(USE_SN 시퀀스 발번, PK는 DB상 (CNTR_SN,USE_SN) 복합), `GCntrUsePointRepository`, `SEQ_G_CNTR_USE_POINT` 시퀀스(DDL 기록).
- **한계**: 소멸(EXPIRE)은 `g_cntr_use_point`에 안 남으므로 상세의 기부건별 '사용'은 소멸분을 포함하지 않는다(AS-IS도 동일 — 소멸은 G_CNTR_USE_POINT에 없다). 요약카드의 '사용'(원장 usedOf, 소멸 포함)과는 소멸 발생 시에만 차이가 나며, 정상/취소 경로에서는 일치한다.

### E2E 실측 (2026-09-21, user 1063 / 11230 강남구)
- 상세 baseline: 적립30000/사용0/잔여30000, 취소된 기부(D…4070)는 제외 확인.
- 5000P 사용(orderCode=TESTORDER-PD-1) → `g_cntr_use_point` 1행 기록, 상세 사용5000/잔여25000 + 주문번호 노출.
- `order.saga` ORDER_CANCELLED 발행 → `restoreForOrder` → `g_cntr_use_point` 삭제(0), 잔액 122000 복원, 상세 사용0 복귀.
- 검증 후 잔재 물리 정리(원본 lot 30000·잔액 122000 원상복구).
