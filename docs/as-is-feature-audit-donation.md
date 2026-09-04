# AS-IS 기능감사 - donation(기부) 서비스

조사일: 2026-09-03. 대상: `ghlove`(legacy 모놀리스) 고향사랑기부(도네이션) 도메인 전체 vs `ghlove-msa`의 `donation`/`storefront`/(참고)`admin` 서비스.

## 조사 방법 메모

AS-IS 기부 도메인의 실제 소스는 처음 추정한 것과 달리 `ghlove-web`(saleson opmanager 관리자콘솔)이 아니라 아래 3곳에 걸쳐 있다.

- `ghlove-frontend/donation`, `mypage/cntr*.html`, `modules/op.donation*.js` — 고객용 Vue2 화면 + 클라이언트 로직
- `ghlove-api/src/main/java/saleson/api/donation` — 고객용 REST 진입점(`NgDonationController`, `RegionTaxController`, `SeoulTaxController`)
- `ghlove-common/src/main/java/saleson/shop/donation` — 실제 비즈니스 로직(`DonationService`, `NgDonationServiceImpl`, `NgDonationRelayServiceImpl`, `DonationVerification`, `NgDonationBatchServiceImpl` 등, 총 60+ 파일)

`ghlove-web`의 `saleson/shop/give*`, `designateddonation`, `offgive` 패키지는 전부 **admin(운영자콘솔)** 쪽 컨트롤러이며, 이미 `designated_donation_admin_round`/`give_statistics_scope_and_levy_assumption`/`offgive_and_remittance_rounds` 메모에서 admin 서비스 감사로 완료 확인됨 — 이번 donation 감사에서는 참고만 하고 재검토하지 않았다.

## AS-IS 기능 전수 목록

| 기능 | AS-IS 위치 | TO-BE 구현여부 | 비고 |
|---|---|---|---|
| 지자체 목록/기부하기 폼(일반+지정) | `ghlove-frontend/donation/donation-main.html`, `ngDonationService.getUserCntrInfo/getSidoList/getLocGovInfo` | ✅ 완료 | `donation/donate.html`, `DonationService` |
| 지정기부(목록/상세/응원메시지) | `ghlove-frontend/designated-donation/*` | ✅ 완료 | storefront `/donate/designated*` (round6 메모) |
| 기부 상태전이(REQUESTED→COMPLETED/CANCELLED) | `DonationService.sunapProcess/paymentCancellation` | ✅ 완료 | `DonationService.completeDonation/cancelDonation` |
| 연간 개인 기부한도 검증(동적 금액) | `DonationVerification.isDonationNormalAmount` (연도별 CODE 테이블) | ✅ 완료 | `DonationService.validateAnnualLimit` — `SYSTEM_CONFIG` + `G_CTBNY_SETUP` 지자체별 한도까지 반영, 하드코딩 아님 |
| 세외수입 부과/수납 연계(서울 vs 그외 지자체) | `SeoulTaxService`/`RegionTaxService`, `NgDonationRelayServiceImpl.sntrBugaInsert/contryBugaInsert` | ✅ 완료(mock) | `LocalTaxClient` — `external_integrations_architecture` 메모의 기존 스코프 결정 |
| 국세청 홈택스 전자기부금영수증 연계 | `NgDonationRelayServiceImpl.sendNtsEreceipt` | ✅ 완료(mock) | `NtsClient`, `DONATION_LEVY` 테이블 |
| GIRO(지로) 계좌이체 결제 팝업 | `op.donation.js` giroPay/giroPolling/giroSunapConfirm 등(1300줄+) | ✅ 완료(mock, 팝업 UI 없이 백엔드 단순화) | 세외수입 연계와 동일한 mock 패턴의 일부로 흡수됨. 실제 지로 팝업창 UX는 재현 안 함 |
| 기부확인증(마이페이지, 카드뒤집기, 출력) | `mypage/receiptList.html`, `modal-receipt_view.vue` | ✅ 완료 | `donation_receipt_as_is_parity` 메모, `receipts.html`/`certificate.html`/`certificate-print.html` |
| 세액공제 계산(2026 개정 3단계 요율) | AS-IS 실제코드는 구법 유지(안내문구와 불일치, AS-IS 자체버그) | ✅ 완료(TO-BE가 개정법 기준으로 의도적 보정) | `donation_receipt_as_is_parity` 메모 |
| 기부혜택(지자체별 혜택 텍스트) | `G_HONOR_BENEFIT` | ✅ 완료 | `HonorBenefit`/`HonorBenefitController` |
| **명예기부자(누적기부액 기준 등급 등록/변경/삭제)** | `NgDonationServiceImpl.insertHonorCntrbtr`, `mypage/honorList.html` | ✅ 완료 | `HonorCntrbtr`/`HonorCntrbtrRepository`, `DonationService`(467~478행), `honor-certificates.html` — 처음엔 미확인 기능으로 의심했으나 실제로는 이미 구현되어 있음을 확인 |
| 오프라인 기부(기탁서) 등록 | AS-IS `procOffOrder` 등 | ✅ 완료 | `vue3_storefront_migration_round7_offline` 메모 |
| 관심 지자체 등록/조회 | `NgDonationServiceImpl.setIntrstLocgov/getIntrstLocgovInfo` | ✅ 완료 | `interest-locgovs.html`, `InterestLocgovController` |
| 기부 취소 사유 기록(admin 처리) | AS-IS opmanager give-reqmng | ✅ 완료 | `CntrReqmngService`(사유 10자 이상 검증), admin `give-reqmng-list/form.html` |
| 과오납 처리 / 수납취소 상태관리 | `DonationService.overPayment/paymentCancellation`(AS-IS) | ✅ 완료 | admin `CntrReqmng` 워크플로우로 흡수 |
| 일반기부(비지정) admin 관리 화면 | opmanager give-reqmng | ✅ 완료 | `CntrReqmngService`는 지정기부 여부와 무관하게 전체 `Donation` 대상 검색/승인/취소 지원 확인 |
| 안내사항(guide1/2/5/6) | `ghlove-frontend/donation/guide1,2,5,6.html` | ✅ 완료 | `vue3_storefront_migration_round9_guides` 메모 |
| 연말정산 세액공제 안내(guide3) | `ghlove-frontend/donation/guide3.html` | ✅ 완료 | storefront `TaxCreditGuideView.vue`(라우트 `/honor`) — Thymeleaf가 아닌 Vue3로 이전되어 있어 처음엔 누락으로 의심했으나 실제로는 존재 확인 |
| 고향사랑기부 주의사항(guide4, AS-IS 죽은코드) | `donation_info-lnb_ali.vue`에서 링크 자체가 주석처리 | — 스킵 정당 | AS-IS 자체가 죽은 코드(내비게이션에 노출 안 됨) — `feedback_scope_default_full_parity`의 유일한 스킵 근거 충족 |
| 정책(개인정보처리방침/저작권정책/이용약관) | AS-IS `policy/*` | ✅ 완료 | `vue3_storefront_migration_round13_policy` 메모 |
| 행공센 연계 주소조회(내국인, `rsgstadres`) | `DonationService.rsgstadres`, `NgDonationRelayServiceImpl.rsgstadresinfo` | ✅ 완료(mock) | `LocalTaxClient` 계열로 흡수. 마이페이지 등록주소 기준으로 지자체 판정(코드 주석에 명시) |
| **외국인/재외국민/외국국적동포 기부(거소신고 조회 연계)** | `DonationService.rsgstadresForeigner`, `donation-main.html`(외국인등록번호 입력 UI) | ❌ 미구현 | `donate.html` 29~32행 코드주석에 "외국인(거소신고) 별도 플로우... 이번 스코프에서는 제외한다"고 명시돼 있으나, 이 결정을 뒷받침하는 메모리/사용자 승인 기록이 없음. AS-IS는 죽은 코드가 아니라 실동작 기능 |
| **지자체별 기부제한 스케줄(CntrLmtt, 강원도 등 일시적 기부금지)** | `DonationService.locGovLmtt`, `G_CNTR_LMTT` | ❌ 미구현 | 위와 동일한 `donate.html` 코드주석에서 함께 제외 명시 |
| **기부 완료/명예기부자 등급변경 SMS·알림톡 통지** | `NgDonationServiceImpl.sunapSuccess`(기부 감사 인사 SMS), `insertHonorCntrbtr`(국민비서 SMS) | ❌ 미구현 | `DonationService.completeDonation()`/명예기부자 갱신 로직에 `NotificationClient` 연동 없음. member/admin 서비스엔 이미 자체 `NotificationClient` 사본이 있어 동일 패턴 이식 가능 |
| 오늘 같은 지자체 중복기부 시도 경고 다이얼로그 | `donation-main.html` 893행 confirm() | ❌ 미구현(경미) | 순수 UX 넛지, 데이터 무결성엔 영향 없음 |
| "빠른기부하기(예시)" 페이지 | `donation/quick-donation.html` (제목에 "(예시)" 명시) | — 검토불요 | 프로덕션 내비게이션에 연결 안 된 데모/샘플 페이지로 판단 |

## Gap 상세

### 우선순위 높음

없음 — 나머지 미구현 항목은 모두 외부기관 연계(mock 패턴으로 이미 흡수) 또는 알림/UX 수준이라 상위 우선순위로 분류할 항목이 없음.

### 우선순위 중간

**1. 외국인/재외국민/외국국적동포 기부 플로우 미구현**
- 위치: 신규 필요 — `donation/service/DonationService`(또는 신규 `ForeignDonorService`), `donation/templates/donate.html`
- AS-IS 근거: `ghlove-common/.../DonationService.rsgstadresForeigner()`(등록외국인/재외국민/외국국적동포 3분류, 체류만료일 검증, "자기 지자체 기부불가" 검증), `donation-main.html`의 외국인등록번호 입력 UI
- 착수 방법: 이미 확립된 mock-gated 외부연계 패턴(`LocalTaxClient` 참고)을 그대로 적용 — `ForeignResidentClient`(가칭)를 만들어 `enabled=false`일 때 그럴듯한 mock 응답(체류상태코드 1/2/3, 만료일, 주소)을 반환하게 하고, `donate.html`에 "외국인/재외국민" 체크박스+외국인등록번호 입력란을 추가해 국내 흐름과 동일하게 `validateAnnualLimit` 이전 단계에서 "자기 거주 지자체엔 기부 불가" 검증을 추가한다.
- 참고: 신규 DB 컬럼 `FOREIGN_STATUS_CODE`(OP_USER 등)가 필요할 수 있음 — AS-IS `ForeignStatusParam.updateForeignStatusCode` 참고.

**2. 지자체별 기부제한 스케줄(CntrLmtt) 미구현**
- 위치: 신규 필요 — `donation/domain/CntrLmtt`(엔티티), `LocgovRepository`, `DonationService.validateAnnualLimit()` 앞단
- AS-IS 근거: `DonationService.locGovLmtt()` — `G_CNTR_LMTT` 테이블에서 `LMTT_BGN_DE`~`LMTT_END_DE` 사이 오늘 날짜가 걸리면 해당 지자체 기부 자체를 차단(위반사유 `VIOLT_RESN_CN` 노출)
- 착수 방법: 이미 존재하는 `G_CTBNY_SETUP`(지자체별 설정) 테이블 옆에 `G_CNTR_LMTT` DDL 추가, 기부하기 폼 진입 시 지자체 선택 즉시 이 제한을 조회해 안내 문구로 노출 + 실제 기부 신청 시 서버단에서도 재검증. admin 쪽에 이 제한기간을 등록/해제하는 CRUD 화면도 함께 필요(현재 admin에도 없음 — admin 재감사 시 같이 반영 권장).

### 우선순위 낮음

**3. 기부 완료/명예기부자 등급변경 알림 미발송**
- 위치: `donation/src/main/java/com/ghlove/donation/service/DonationService.java` — `completeDonation()`(316행 부근)과 명예기부자 갱신 블록(467~478행)
- AS-IS 근거: `NgDonationServiceImpl.sunapSuccess()`가 수납 성공 시 `smsIpsService.giveSendSms(..., SmsType.DONATION)`으로 "기부 감사 인사" SMS 발송, `insertHonorCntrbtr()`가 등급 신규등록/변경 시 국민비서(Gov24) SMS 발송
- 착수 방법: member/admin 서비스에 이미 있는 `service/integration/NotificationClient.java`와 동일한 모양(서비스별 사본, `enabled=false` mock)을 donation에도 만들어 `completeDonation()`과 명예기부자 갱신 지점에 best-effort 호출만 추가하면 됨. 실패해도 기부 완료 자체를 막지 않도록 `NtsClient`처럼 예외를 삼키는 패턴을 그대로 따를 것.

**4. 동일 지자체 당일 중복기부 시도 경고**
- 위치: `donation/templates/donate.html`(폼 제출 전 confirm 단계)
- AS-IS 근거: `donation-main.html` 893행 — "선택하신 지자체에 오늘 기부를 시도하신 내역이 확인됩니다" confirm 다이얼로그
- 착수 방법: 폼 제출 직전 오늘 날짜+동일 사용자+동일 지자체 `REQUESTED/COMPLETED` 기부 존재 여부만 조회해 JS confirm 창 하나 추가하면 됨. 데이터 무결성엔 영향 없는 순수 UX 항목이라 후순위.

## 완전히 구현 확인됨 (요약)

일반기부·지정기부 신청/승인/취소 전체 플로우, 연간 기부한도(동적 금액+지자체별 한도), 세외수입 부과·수납 및 국세청 전자영수증 연계(mock), 기부확인증(카드뒤집기+인쇄), 세액공제 계산(2026 개정 요율로 AS-IS 버그까지 의도적으로 보정), 지자체별 기부혜택 텍스트, **명예기부자 등급 등록/변경/삭제**, 오프라인 기부(기탁서), 관심 지자체, 기부 취소 사유 기록 및 과오납/수납취소 상태관리(admin), 안내사항 guide1/2/3/5/6 전체(guide3는 Vue3로 이전되어 위치만 다름), 정책 3종, 행공센 주소조회 연계(mock) — AS-IS와 동등 수준으로 확인됨.
