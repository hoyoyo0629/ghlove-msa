# donation 서비스 AS-IS 전수 대조 (parity audit)

> **문서 목적 / 독자**: AS-IS를 "서비스로직도 화면단도 똑같이" 재현하기 위한 **전수 갭 목록**. `[[as-is-parity-exhaustive-audit-method]]` 방식대로 프론트 이벤트 + 서비스 검증 분기 + 매퍼를 대조한다. "발견되면 그때"가 아니라 **이 목록 기준으로** 구현한다. NEW/CHANGED(ISP·RFP) 요건은 표에 표시하고 **착수 전 사용자 확인**.
>
> 상태 범례: **O** 재현됨 / **X** 누락 / **부분** 일부만 / **확인** 추가 대조 필요. 작성 2026-09-21 (1차: 기부하기 플로우).

---

## 1. 기부하기 플로우 — 지자체 선택(@change) 시 검증

AS-IS `donation-main.html` `getLocgovInfo(locgovCode)` (지자체 드롭다운 선택 시 호출) + `/api/ngdonation/locGovInfo`.

| # | AS-IS 동작 | 트리거 | MSA 재현 | 상태 | 비고 |
|---|---|---|---|---|---|
| 1-1 | **본인 주민등록주소지 지자체 차단** — alert "자신의 주민등록주소지의 지자체에는 기부를 하실 수 없습니다" | 지자체 선택 즉시(프론트) | `DonationService:290`에서 기부 신청 시 차단. 단 **선택 즉시 프론트 경고는 없음**(신청 눌러야 막힘) | **부분** | AS-IS는 선택 순간 막고 리셋. MSA는 제출 시점 차단 → UX 다름 |
| 1-2 | **하루 중복기부 확인** — `resultStatus=="dupl"`이면 confirm "선택하신 지자체에 오늘 기부를 시도하신 내역이 확인됩니다. 중복 기부를 방지하기 위해… 납부확인은 최대 3일…" (취소=뒤로가기) | 지자체 선택 | **없음** | **X** | **정본 조건**: `getTodayCntrListInfo(userId, locgovCode)` = 오늘 + 같은 지자체 + **부가정보(전자납부번호) 생성된 기부건** 존재. (25.08.01 KLID: 금일 생성 부가정보 리스트로만 판단) → MSA는 `donation.g_cntr` 중 `cntr_de=오늘·cntr_locgov_code=선택·user_id=본인`이고 `DonationLevy(전자납부번호)` 있는 건 존재 여부로 판정 |
| 1-3 | **기부 불가 기간(violtResn) 차단** — `lmttBgnDe~lmttEndDe` 사이면 alert "선택하신 지자체는 {violtResnCn}으로 {시작}부터 {종료}일까지 기부가 불가능합니다" + 리셋 | 지자체 선택 | **없음** | **X** | g_locgov의 violtResnCn/lmttBgnDe/lmttEndDe. 인천 영종(28155)·제물포(28125)·검단(28290)은 "18시~10시" 특수 문구 |
| 1-4 | 수납결과 확인 오류 시 alert "수납결과 확인중 오류가 발생했습니다" | locGovInfo ERROR | 없음 | **X** | 1-2 구현에 딸림 |
| 1-5 | 선택 지자체 포인트 지급률 표시 | 지자체 선택 | `point-rate` 조회 | **O** | |

## 2. 기부하기 플로우 — 주소확인/본인인증/금액

| # | AS-IS 동작 | MSA 재현 | 상태 | 비고 |
|---|---|---|---|---|
| 2-1 | **주소확인하기**(rsgstadresinfo) → 거주지 확인, "귀하는 {지역}에 납부가 가능합니다" | DonateView `verifyResidence` → `/api/donate/verify-residence`, "귀하는 …에 납부가 가능합니다" | **O** | |
| 2-2 | **본인인증 미완료 차단** — `userCntrInfo`에서 미인증 시 alert "본인인증 미완료상태입니다. 회원정보수정에서 본인인증을…" | 확인 필요(로컬은 `identity-verification-bypass:true`) | **확인** | MSA 본인인증 우회 설정과의 관계 정리 필요 |
| 2-3 | **거소지(거소지 지자체) 차단** — "자신의 거소지 지자체에는 기부를 하실 수 없습니다" (거소지 코드 36 시작 등) | 주소지만 차단, **거소지 차단 확인 필요** | **확인** | |
| 2-4 | 주민번호 입력·검증 (이름/주민번호 앞·뒤) | MSA는 CI 미수집 구조라 주민번호 입력 자체가 없음 | **확인** | 본인인증 방식 차이 — 외부연계 축소인지 확인 |
| 2-5 | 금액: 최소 100원, 100원 단위, 한도 초과 | DonateView:170-174 최소 100·100원단위, 서비스 연간한도 | **O** | 한도는 신청+완료 이중검증(`DonationService`) |
| 2-6 | 답례품 제공 여부(presentType) 선택 | DonateView 재현 | **O** | |

## 3. 연간 한도 / 신청·완료

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 3-1 | 연간 기부한도 검증(개인 연 2천만원 등) | 신청 시 + **완료 시점 재검증**(한도우회 방지) | **O** |
| 3-2 | 기부 신청 → 부가정보(전자납부번호) 생성 → 결제 | MSA는 REQUESTED 생성(PG 미개방으로 "결제완료 처리" 대체) | **부분(외부차단)** |

---

## 4. 이번 대조로 확정된 착수 대상 (기부하기 플로우)

전부 **순수 AS-IS 재현**(신규·변경 요건 아님 → 사용자 확인 불요, 제1원칙대로 동일 구현):

1. **[X→구현] 1-2 하루 중복기부 확인** — 지자체 선택 시 오늘·같은지자체·전자납부번호생성 건 존재하면 confirm 경고. MSA에 `GET /api/donate/today-duplicate?locgovCode=` 류 추가 + `DonateView.onLocgovChanged`에서 confirm.
2. **[X→구현] 1-3 기부불가기간(violtResn) 차단** — g_locgov의 violt/lmtt 필드 기반 차단 alert.
3. **[부분→보완] 1-1 주소지 차단을 선택 즉시 프론트 경고**로 (AS-IS와 동일 UX).
4. **[확인] 2-2 본인인증 / 2-3 거소지 / 2-4 주민번호** — 백엔드·설정 대조 후 재현 여부 판정.

> 데이터 의존(1-3 violtResn, 1-2 dupl)은 g_locgov·g_cntr 실데이터가 있어야 화면 검증 가능.

---

## 5. 지정기부(designated) 신청 검증

AS-IS `designated-donation/details.html` + MSA `DesignatedDetailView.vue` / `DonationService.donateToDesignatedProject`.

| # | AS-IS 동작 | MSA 재현 | 상태 |
|---|---|---|---|
| 5-1 | "기부가능한 상태가 아닙니다" / "진행 중 상태만 기부 가능합니다" | `DonationService:310` "현재 기부를 받고 있지 않은 사업입니다" | **O(문구차)** |
| 5-2 | "시작일 이전에 기부가 불가능합니다" / "종료일 이후에 기부가 불가능합니다" | `DonationService:313-315` 사업기간(bgngYmd~endYmd) 검증 | **O(문구차)** |
| 5-3 | **"목표 금액이 달성되어 기부가 불가능합니다"** — 목표액 도달 시 차단 | **확인** (MSA에 목표금액 달성 차단 있는지 대조 필요) | **확인** |
| 5-4 | 응원메시지 30자 제한 "30자 까지 입력가능합니다" | 확인 | **확인** |
| 5-5 | **지정기부 완료 시 취소 불가** | 확인 (MSA cancelDonation이 지정기부 구분하는지) | **확인** |

## 6. 기탁/오프라인 기부(offgive) 등록 검증

AS-IS `OffgiveServiceImpl` + MSA `OfflineDonationView.vue` / `DonationService.registerOfflineDonation`.

| # | AS-IS 동작 | MSA 재현 | 상태 |
|---|---|---|---|
| 6-1 | **"기 신고한 기부정보(기부한도 사용)가 있어 추가 등록할 수 없습니다. 신고일 익일 자정에 초기화"** — 기탁 중복 신고 차단 | **확인** (MSA registerOfflineDonation에 기신고 차단 있는지) | **확인** |
| 6-2 | "기부 금액 단위는 100원 단위입니다" | 확인 | **확인** |
| 6-3 | 사용자 기부 한도 체크 | 연간한도 검증 재사용(주석상 O) | **O** |

## 7. 아직 전수 안 한 donation 흐름 (계속 목록화 예정)

- 영수증/기부확인증 발급·재발급, 국세청(NtsClient) 연계 검증
- 명예기부자/등급 산정, 기부혜택증
- 마이페이지 기부내역/기부포인트/기부확인증(→ 별도 `point`/마이페이지 대조와 겹침)
- 빠른기부/논현금기부(quick/ng) — AS-IS 존재하나 MSA 미대응(의도적 축소 여부 확인)

## 8. 영수증/기부확인증 · 명예기부자

| # | AS-IS | MSA | 상태 |
|---|---|---|---|
| 8-1 | 기부확인증: "선택한 기부내역이 없습니다" (선택 후 출력) | ReceiptListView 선택검증 | **확인(경미)** |
| 8-2 | 영수증출력: 로그인 필요 / "영수증 정보가 없습니다" | requiresAuth + 404 | **O(부분)** |
| 8-3 | 명예기부자/혜택증: 서버 errMsg 표시 | HonorCertificatesView | **확인(경미)** |

---

## 9. ★ donation 최종 갭 목록 (확정 — 구현 단계에서 이 목록대로)

MSA 백엔드 대조로 확정. **전부 순수 AS-IS 재현**(신규·변경 요건 아님 → 사용자 확인 불요).

| 우선 | 갭 | 상태 | 구현 요지 |
|---|---|---|---|
| **높음** | ~~하루 중복기부 확인(1-2)~~ | **✅완료(2026-09-21)** | `GET /api/donate/today-duplicate` + `DonationService.hasTodayDonation`(오늘 cntrDe·같은지자체·비취소건) + `DonateView.onLocgovChanged` confirm(취소=선택리셋). AS-IS는 차단 아닌 경고(확인 시 진행) |
| **높음** | ~~기부불가기간(violtResn) 차단(1-3)~~ | **✅완료(2026-09-21)** | g_locgov에 `violt_resn_cn/lmtt_bgn_de/lmtt_end_de` 컬럼+엔티티 추가, `donationRestrictionMessage`(오늘이 제한기간이면 안내, 인천3구 특수문구), 엔드포인트+프론트 alert+리셋, createGeneralDonation 서버가드. **로컬 데이터 없어 미발동(재현 배치 완료)** |
| 중 | ~~주소지 차단 선택 즉시 경고(1-1)~~ | **✅완료(2026-09-21)** | `knownResidenceLocgov` 유지 → 확인된 거주지 (재)선택 시 즉시 alert+리셋. 제출·주소확인 시점 차단은 기존 유지 |
| 중 | ~~지정기부 목표금액 달성 차단(5-3)~~ | **✅완료(2026-09-21)** | `createDesignatedDonation`에 `raisedAmount≥goalAmt` 차단 "목표 금액이 달성되어 기부가 불가능합니다" |
| 중 | **지정기부 목표금액 달성 차단**(5-3) | **X(순수재현)** | AS-IS: prjStatus=2여도 달성률 rateAmt(누적기부액/목표액×100)≥100%면 "목표 금액이 달성되어 기부가 불가능합니다". MSA `createDesignatedDonation`은 상태·기간만 검증 → 달성률 계산+차단 추가 |
| ~~중~~ | ~~거소지 지자체 차단(2-3)~~ | **재분류→축소** | **주소지 차단의 외국인 버전.** 외국인 거소신고 주소를 `rsgstadresinfoForeigner`(외국인 거소정보 **외부조회**)로 받아 같은 지자체면 차단(체류만료도). **외부연계 축소** — 개방 시 재검토(순수재현 아님) |
| ~~중~~ | ~~기탁 기신고 중복 차단(6-1)~~ | **✅완료(2026-09-21) — 한도검증 대기포함** | AS-IS `getGCntrSumCntrAmt`는 상태무관(삭제제외)으로 **올해 대기+완료 전체**를 한도사용액으로 합산(신청만 해도 즉시 잡힘). MSA는 완료건만 셌음 → **`validateAnnualLimit`을 취소아닌(REQUESTED+COMPLETED) 합산으로 수정**, 완료 시점 재검증은 `excludeCntrSn`으로 자기건 제외(이중집계 방지). 일반/지정/기탁 모든 신청 경로에 적용. "익일 자정 초기화" 문구는 실제 연간계산과 무관한 레거시 안내 |
| 낮음 | ~~지정기부 응원메시지 30자 제한(5-4)~~ | **✅완료(2026-09-22)** | MSA가 100자로 지어놨던 것(verbatim 위반)을 AS-IS 30자로 정합: DonateView maxlength 100→30·placeholder "30자"·제출검증 alert "30자 까지 입력가능합니다."(details.html:736), 서버 truncate 100→30. (AS-IS는 기부내역 탭 인라인편집 saveCheerMsg, MSA는 제출시점 수집 — flow는 문서화된 적응) |
| — | ~~지정기부 완료취소 불가(5-5)~~ | **갭 아님(2026-09-22)** | AS-IS 원문이 `"...취소 안되용~ 테스트 문구"`(details.html:206) — 명백한 테스트 잔재. MSA가 완료취소 허용이 맞음 |
| — | quick 빠른기부(donation/quick-donation.html) | **미사용 시안 → 보류+기록** | AS-IS 소스 주석(2026-09-14): "빠른기부하기(예시) 시안, 메뉴·헤더·푸터·배너·팝업 어디에도 미연결, 동작 안 함, 기능수정·신규구현 대상 아님. 실제 기부는 donation-main.html". MSA 미구현이 맞음 |
| — | 본인인증 미완료 차단(2-2)/주민번호(2-4) | **축소** | MSA는 CI 미수집·`identity-verification-bypass`(외부연계 축소). **외부연계 개방 시** 재검토 — 물리차단이라 사용자 확인 대상 |

**재현 확인된 것(O)**: 주소지 차단(제출시점), 사업상태·기간, 100원단위·금액검증, 연간한도(신청+완료 이중), 포인트율, 영수증출력, 답례품제공여부.

> **donation audit 완료(1차).** 구현은 "전 서비스 목록화 완료 후" 시작(방식 [[as-is-parity-exhaustive-audit-method]]). 다음: member 전수조사.
