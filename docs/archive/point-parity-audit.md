# point 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적/독자**: AS-IS "서비스로직도 화면단도 똑같이" 재현 전수 갭 목록. 방식 `[[as-is-parity-exhaustive-audit-method]]`. 상태 **O**/**X**/**부분**/**확인**. 작성 2026-09-21.
>
> point는 이번 세션(2026-09-21)에 기부포인트 조회/상세를 깊이 재작업했다. 상세 분석·구현 원문은 `docs/point-service-analysis-2026-09-09.md`(§10 최신). 이 문서는 그 결과를 전수 관점으로 통합한다.

## 1. 기부포인트 조회 (cntrPoint.html) — **O (이번 세션 재작업)**

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 1-1 | 총 적립/사용/잔여 카드 | `MyPointsView` 카드 | **O** |
| 1-2 | 목록: (기부연도+지자체) 그룹, 적립/사용/잔여 | `ledgerSummaryByYearAndLocgov` | **O**(이번 세션) |
| 1-3 | 사용포인트 칸 상세 진입 아이콘(short_icon) | 상세 아이콘 → MyPointDetailView | **O**(이번 세션) |
| 1-4 | 답례품몰 이동 버튼(goToLocgovMall) | 지자체 답례품 목록 링크 | **O** |
| 1-5 | 마이페이지 LNB(2뎁스+3뎁스) | `MypageLnb` | **O**(이번 세션) |

## 2. 기부포인트 현황 상세 (cntrPointDetail.html) — **O (이번 세션 재작업)**

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 2-1 | UNION ALL 거래원장(적립 행 + 사용 행) 시간순 | `pointDetail` 원장 기반 거래원장 | **O**(이번 세션 정정) |
| 2-2 | 적립 행: 발생일자·기부액·적립 / 사용 행: 사용·답례품 주문번호 | 동일 | **O** |
| 2-3 | 러닝 잔액(그 시점 누적적립-누적사용) | 동일 | **O** |
| 2-4 | 답례품 주문번호 → 주문상세 이동 | order-detail 링크 | **O** |

> 과거 기부액 NULL 시드데이터는 donation.g_cntr에서 백필 완료. 소멸(EXPIRE)은 상세에 미포함(AS-IS도 동일).

## 3. 포인트 적립/차감/소멸 (서비스 로직) — **O**

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 3-1 | 기부완료 → 30%(지자체별) 적립 | `creditForDonation`(이벤트) | **O** |
| 3-2 | 주문결제 포인트 차감/복원 | `deductForOrder`/`restoreForOrder`(SAGA) | **O** |
| 3-3 | 취소분 적립·사용 이중가산 방지 | §9 집계 수정 완료 | **O** |
| 3-4 | 지자체별 가용잔액으로만 사용(다른 지자체 포인트 차단) | `balanceByLocgov`/`availableBalanceOf(locgov)` | **O** |
| 3-5 | 포인트 유효기간(FIFO lot) 소멸 | `openLot`/만료 배치 | **O** |
| 3-6 | 소멸예정 안내 메일 발송 | 화면 안내만(UMS 미개방) | **부분(축소)** |

## 4. 포인트 예약/예약해제 — **MSA 신규(AS-IS 없음)**

| # | | 상태 |
|---|---|---|
| 4-1 | AS-IS에 예약 개념 자체가 없음(MSA 신규기능, ISP 설계엔 있음) | `[[point-reservation-unused-decision-deferred]]` |
| 4-2 | 실사용 경로 0곳(주문은 즉시차감) — 화면 숨김 vs 체크아웃 재배선 제품결정 대기 | **결정대기** |

## 5. CQRS ReadModel — **중지(DB설계 대기)**

`[[point-readmodel-paused-pending-db-design]]` — 테이블·엔티티만 생성, 미배포. DB 설계 후 재개.

---

## 6. ★ point 최종 갭 목록 (확정)

**기부포인트 조회/상세·적립/차감/소멸 로직은 재현 완료(O, 상당수 이번 세션).** 남은 것:

| 갭 | 상태 | 성격 |
|---|---|---|
| 소멸예정 안내 메일 발송 | 부분 | UMS 미개방 축소 — 개방 시 |
| 포인트 예약 화면 노출 정리 | 결정대기 | 제품결정(숨김/재배선) |
| CQRS ReadModel | 중지 | DB설계 대기 |
| 답례품 주문번호 링크 추적(g_cntr_use_point) | 완료(보조) | `[[point-use-tracking-g-cntr-use-point]]` |

> **point audit 완료.** 순수 재현 갭 없음(조회/상세/원장 O). 남은 3건은 외부연계 축소·제품결정·DB대기. 다음: gift 전수조사.