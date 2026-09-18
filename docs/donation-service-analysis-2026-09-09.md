# 기부(donation) 서비스 상세분석 — 2026-09-09

분석 기준: **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본:
- AS-IS: `C:\workspace\ghlove`
  - 화면 `ghlove-frontend/donation/*.html`, `ghlove-frontend/designated-donation/*.html`
  - 매퍼 `ghlove-common/.../sqlmapper/cubrid/{give,give-state,ngdonation}-mapper.xml`
- RFP: `docs/requirements.md` SFR-003 (핵심 도메인)
- ISP: `docs/isp-detailed-design-summary.md` (p.415/427 이벤트스토밍, p.148 Core 서브도메인, p.325 SW목록)

---

## 1. 요구사항 대비 충족 현황

| RFP SFR-003 항목 | 상태 | 근거 |
|---|---|---|
| 기부사업 선택, 연간 한도·지자체별 규칙 검증 | 충족 | 전체 합산 한도(SYSTEM_CONFIG) + 지자체별 한도(`G_CTBNY_SETUP`) 이중 검증. **완료 시점에 재검증**까지 함(신청만 여러 건 만들어 순차 완료로 한도 우회하는 것을 막음) |
| 세외수입 연계(지방세외수입·위택스·서울ETAX) | 충족(축소) | `LocalTaxClient`가 부과등록(`registerLevy`)·수납확인(`confirmPayment`) 구현. `enabled:false` — 방화벽 미개방 |
| 기부 상태관리 | 충족 | §3-5 참고(AS-IS 대비 900만 없음) |
| 기부이력·통계 조회, 기부금 확인 | 충족 | `my.html`, `receipts.html`, `GiveState` |
| 기부영수증·기부확인증 발급/재발급 | 충족 | `certificate(-print).html`, `receipt-official-print.html`, `NtsClient`(국세청) |
| 기부혜택 관리, 지자체별 혜택 | 충족 | 명예기부자 혜택(`HonorBenefit`) + 연말정산 세액공제 안내(`/honor` = AS-IS guide3 재현본, §3-1) |
| 기탁서·오프라인 기부, 지자체별 운용현황 | 충족 | `offline.html`, `CtbnyOpratn`, `CtbnyOpratnFile` |
| **★ 기부완료 이벤트 → 포인트 적립 트리거** | 충족 | `DonationEventPublisher` / `DonationCompletedEvent`·`DonationCancelledEvent` |
| 주문·정산·통계와 **이벤트 기반으로만** 연계 | **부분** | §3-3 |
| **기부금 납부 시 답례품 선택 기능 추가** | **미충족** | §3-2 |

ISP 대조:
- Core 서브도메인 배치, 도메인 이벤트(기부확정/취소) 대응 ✅
- ISP 명령의 "결제세션생성 / 승인요청 / 정산캡처"와 이벤트 "결제세션생성됨 / 결제승인요청됨·실패됨 / 결제정산완료"는 **PG 연계가 비활성이라 미구현** — 연계 개방 시 착수 대상
- ISP 조회모델 "기부요약/영수증/한도, 결제상태/대사뷰" 없음(§3-4)

---

## 2. 화면 대응표 (주요 항목)

| AS-IS | MSA | 비고 |
|---|---|---|
| `donation/donation-main.html` | `donate.html` | 대응. 단 **지도 선택 진입 없음**(§3-6) |
| `designated-donation/index.html`·`details.html` | `designated-list.html`·`designated-detail.html` | 대응 |
| `donation/guide1,2,5,6.html` | `guide/guide1,2,5,6.html` | 대응 |
| `donation/guide3.html`(연말정산 세액공제 안내) | `honor.html`(`/honor`) | 대응 — 경로명만 다름(§3-1) |
| `donation/map-select.html`(지자체 지도선택) | **(없음)** | §3-6 |
| `donation/external-etax·giro`, `wetaxInfo`, `giro-success·fail`, `popup-success·fail` | (없음) | 결제/세외수입 연계 화면 — 연계 비활성이라 의도적 축소 |
| `donation/intro`, `process`, `quick-donation`, `ngdonation` | (없음) | 안내/빠른기부/논현금기부 — 미대응 |
| (없음) | `honor*.html`, `interest-locgovs.html`, `policy/*` | 명예기부자·관심지자체·약관 |

> AS-IS의 `guide4`는 `guide4_bak.html`만 남아 AS-IS에서도 폐기된 화면이다 — 누락이 아니다.

---

## 3. 기능 갭

### 3-1. ~~연말정산 세액공제 안내 화면 없음~~ → **오판, 철회** (2026-09-09 재검증)
최초 분석에서 "AS-IS guide3 미구현"으로 적었으나 **틀렸다**. `GuideController`의 라우트 목록(`/guide1 /guide2 /guide5 /guide6`)과 템플릿 파일명만 보고 판단한 결과다.

실제로는 **`donation/honor.html` + `HonorBenefitController.guide()`가 guide3의 재현본**이다 - 화면 제목이 "안내사항 > 연말정산 세액공제 안내"이고 AS-IS의 `give-table` PC/모바일 2벌 표까지 그대로 있으며, 컨트롤러 주석에도 "AS-IS donation/guide3.html"이라고 명시돼 있다. GNB의 "연말정산 세액공제 안내"가 `/honor`를 가리키는 것도 **정상**이다. storefront SPA도 같은 내용을 `TaxCreditGuideView.vue`(라우트 `/honor`)로 갖고 있다.

남는 것은 결함이 아니라 **경로 이름 문제**뿐이다 - AS-IS는 `guide3`, MSA는 `/honor`라서 "명예기부자 혜택"으로 오해하기 쉽다(실제로 이 분석이 그렇게 오판했다). 정리한다면 `/guide3`을 추가하고 `/honor`를 그쪽으로 리다이렉트하는 정도이며, 기능 갭은 아니다.

### 3-2. 기부 납부 시 답례품 선택 없음 (요구사항 미충족, 중)
RFP가 **"기부금 납부 시 답례품 선택 기능 추가 등 기부 프로세스 통합 개선"**을 명시적으로 요구한다(AS-IS에 없던 개선 요구). 현재는 기부 완료 후 마이페이지/포인트 화면에서 별도로 답례품몰로 이동하는 분리된 흐름이다. `donate.html`에 답례품 관련 UI가 전혀 없다.

### 3-3. point 연계에 동기 호출 경로가 남아 있음 (원칙 위반, 소)
정상 기부 완료는 이벤트(`DonationCompletedEvent`)로 처리된다 ✅. 다만 **기부금 변경신청(give-reqmng) 승인 경로**가 `PointClient.creditForDonation()`으로 point의 적립 로직을 **동기 REST로 직접 호출**한다. RFP는 point 연계를 ★이벤트로 명시하고 "동기 호출 금지"를 별도 항목으로 둔다. `earnedPointsByCntrSn`·`earnLotOf`는 조회성이라 다른 서비스의 조회 예외 패턴과 같지만, `creditForDonation`은 **쓰기 호출**이라 성격이 다르다.

### 3-4. ISP 조회모델 없음 (요구사항 미충족, 중)
ISP 조회모델 "기부요약/영수증/한도, 결제상태/대사뷰". 별도 조회 모델 없이 원본 테이블 직접 집계. point·member와 같은 유형의 갭.

### 3-5. 기부 상태코드 (사실상 충족 — 기록용)
AS-IS `G_CNTR.CNTR_STTUS_CODE`는 `100`(신청/결제대기) `200`(납부완료) `300`(취소, `giveCancelProcess`) `900`(수납배치 완료, 서울 세외수입 `updateSunapBatchCompleted`) 4종. MSA는 `REQUESTED`/`COMPLETED`/`CANCELLED` 3종으로 **900만 없다** — 900은 서울 세외수입 수납배치에 종속된 상태라 그 연계(비활성)와 함께 묶이는 의도적 축소로 본다. RFP 문구의 "결제대기/환불"은 AS-IS에도 별도 코드가 없어(100이 결제대기 겸용, 300이 취소·환불 겸용) 추가 대응 불필요.

### 3-6. 기부하기에서 지도로 지자체 선택 불가 (재현 누락, 소)
AS-IS `map-select.html`은 단순 팝업이 아니라 **지자체 선택 전용 화면**이다 — 지도에서 시·도를 고르고 지자체를 선택하면 그 지자체의 예산·인구·홈페이지 등 상세정보를 보여준 뒤 기부하기로 넘긴다(`donation-main.html`의 `goToBack`이 `flag=map`이면 이 화면으로 돌아간다). MSA `donate.html`은 시·도 → 시·군·구 셀렉트만 제공한다.

최초 분석에서 "기존 지도 컴포넌트를 재사용해 붙이는 수준"이라고 적었으나 **과소평가였다** — `designated-list.html`의 지도는 지정기부사업 목록을 거르는 필터용이라, 지자체 상세정보 패널과 기부 진입 흐름은 새로 만들어야 한다.

---

## 4. 레이아웃 (AS-IS 기준)

**CSS 링크 누락 문제 없음.** AS-IS가 링크하는 `layout.css`/`main.css`/`joind-agf.css`는 donation 모듈에 **파일은 있으나 링크되지 않았고**, `popup.css`/`event.css`/`header-footer_ali.css`는 파일 자체가 없다. 그러나 **각 파일의 클래스를 donation 템플릿이 실제로 쓰는지 클래스 단위로 대조한 결과 링크된 CSS로 전부 커버된다.**

- `.modal*` 계열이 한때 미정의로 잡혔으나, 실사용처는 `fragments/footer.html`이고 `modal-dialog` 등은 이미 링크된 `new.css`/`output.css`에 정의돼 있다 — 오탐.

→ order 모듈의 `mypage-order.css` 누락이 이 결함 유형의 유일한 실사례임이 다시 확인됐다(member·point·donation은 모두 정상).

---

## 5. 조치 내역 (2026-09-09)

**코드 변경 없음.** "바로 처리 가능"으로 꼽았던 두 건이 재검증에서 각각 다음과 같이 정리됐다.

| # | 최초 판단 | 재검증 결과 |
|---|---|---|
| 3-1 연말정산 세액공제 안내 | 미구현, 바로 재현 가능 | **오판** — `/honor`가 이미 guide3 재현본이었다. 화면을 새로 만들고 GNB 링크까지 바꿨다가 **전부 되돌렸다**(중복이었고 GNB는 원래가 맞았다) |
| 3-6 기부하기 지도 선택 | 기존 컴포넌트 재사용, 작음 | **과소평가** — 지자체 상세정보 패널 + 기부 진입 흐름을 새로 만들어야 하는 화면 단위 작업 |

### 오판에서 얻은 점검 절차
`GuideController`의 라우트 목록과 템플릿 파일명만 보고 "guide3 없음"으로 판단한 것이 원인이다. **AS-IS 화면의 대응물을 찾을 때는 라우트·파일명이 아니라 화면 제목과 본문 마크업으로 대조해야 한다** — MSA는 AS-IS와 경로명을 다르게 쓴 화면이 있다(여기서는 `guide3` → `/honor`). 이 건은 컨트롤러 주석에 "AS-IS donation/guide3.html"이라고 명시까지 돼 있었다.

## 6. 잔여 (미조치)

| # | 항목 | 규모 / 필요한 결정 |
|---|---|---|
| 3-2 | 납부 시 답례품 선택 | **RFP 신규 요구**라 AS-IS 참고본이 없다. 기부↔답례품 흐름 통합 설계 결정 필요 |
| 3-3 | `creditForDonation` 동기 호출 | 이벤트 전환 시 give-reqmng 승인 흐름의 보상 처리까지 같이 설계해야 함 |
| 3-4 | ISP 조회모델(기부요약/영수증/한도, 결제상태/대사뷰) | point ReadModel이 DB 설계 대기 중이라 같은 시점에 판단 |
| 3-6 | 기부하기 지도 선택 | 화면 단위 신규 작업. 지자체 상세정보(예산·인구·홈페이지) 출처 확인 선행 |
| — | 결제/PG 연계 화면·ISP 결제 이벤트(결제세션생성/승인요청/정산캡처) | 방화벽·PG 계약 개방 후 착수 |
| — | `/honor` 경로명 | 기능 갭 아님. 정리한다면 `/guide3` 추가 + `/honor` 리다이렉트 수준 |

---

## 7. 추가 발견 및 조치 (2026-09-10)

### 3-7. 기부완료 모달·답례품몰 진입 동선 없음 (재현 누락, 중) → **조치 완료**

기능 테스트 중 "기부금액 저장하면 기부내역 화면으로만 가고, 결제·포인트 충전·기부지역 답례품 화면 이동이 없다"는 지적에서 나온 건이다. 두 가지가 겹쳐 있었다.

**(1) 결제 단계 — 의도된 축소(기존 기록대로).** AS-IS `donation-main.html`은 `주소확인 → 지방세외수입 부과등록(전자납부번호) → 금결원 지로/이택스 결제창 → 수납확인 폴링 → G_CNTR 200 + 포인트 적립` 한 흐름이다. 이 MSA는 연계가 전부 `enabled:false`라 흐름을 둘로 쪼갰다 — `POST /donate`는 `REQUESTED`(= AS-IS `100` 결제대기)까지만 만들고, 기부내역 화면의 **"결제완료 처리" 버튼이 결제창 자리를 대신**해 `completeDonation()`을 트리거한다. 포인트는 그 시점의 `DonationCompletedEvent`로 적립되므로 **적립 시점 자체는 AS-IS(수납확인 후)와 동일**하다.

**(2) 기부완료 모달 — 재현 누락(진짜 갭).** AS-IS는 결제 성공 시 `#donation-c` 모달을 띄운다: 감사 인사 + 기부 요약표(기부자명/기부금액/기부 지자체/제공 예정 포인트/납부정보 확인/세액공제/전자납부번호) + **"답례품 몰 바로가기"**(`goToPresent()` → `/goods/searchGoods.html?type=L&locgov=<지자체코드>`). MSA는 `redirect:/my?donated=success` 한 줄뿐이라 **기부한 지자체의 답례품으로 이어지는 동선이 통째로 없었다.**

> 이 갭을 §2 화면 대응표가 놓친 이유: 축소 판정을 `popup-success.html`·`giro-success.html` 같은 **별도 파일 단위로만** 훑었는데, 이 모달은 `donation-main.html` **안에** 있어 파일 목록에 걸리지 않았다. guide3 오판과 같은 유형이다 — 화면 대응은 파일 목록이 아니라 마크업으로 봐야 한다.

#### 조치 내용
- `complete()`의 리다이렉트를 `/my?completed=<건번호>`로 바꿔 완료 건을 식별한다. AS-IS는 결제창이 닫히는 시점(기부하기 화면)에 모달을 띄우지만, **이 MSA에서 결제가 실제로 끝나는 지점은 기부내역 화면의 "결제완료 처리"**라 모달도 그쪽에 붙였다.
- `populateCompletionModal()`이 표시값을 채운다 — 기부자명(`MemberClient`), 금액·지자체명, 실적립 포인트(`PointClient.earnedPointsByCntrSn`)와 적립률, 전자납부번호(`DonationLevy.bugaNo`). 적립이 이미 끝난 시점이라 AS-IS의 "제공 예정 포인트" 칸에 실적립분을 쓴다.
- **소유권 가드**: 본인 소유 + `COMPLETED` 건에만 모델을 채운다. 남의 건번호를 URL에 밀어넣어도 모달 자체가 렌더되지 않는다(실측 0건).
- `my.html`에 AS-IS `#donation-c` 마크업을 재현하고 **`joind-agf.css`를 링크**했다 — 모듈에 파일은 있었으나 어디서도 링크되지 않던 CSS이고, AS-IS `donation-main.html`도 이 파일을 링크한다. 셀렉터가 전부 `.black-bg`/`.overlayer-body` 하위로 스코프돼 있어 기존 목록 레이아웃과 충돌하지 않는다(order의 `mypage-order.css` 사례와 달리 안전).
- 누락 이미지 3종 복사: `cli-donation-completed.png`, `icon/cli-icon_btn-illust-arrow1.png`, `arrow2.png`.
- "답례품 몰 바로가기"는 `http://localhost:8084/?locgovCode=<기부 지자체>`로 보낸다 — gift 목록 화면이 같은 필터를 받는다. Thymeleaf가 `th:onclick`에 문자열 변수 삽입을 막고 `button.illustBtn` CSS가 button 태그에만 걸려 있어 **`data-href` + JS 리스너** 방식으로 뺐다(`a`로 바꾸면 스타일이 죽는다).

#### 의도적 축소
- AS-IS의 **"답례품 상세로 되돌아가기"** 버튼은 답례품 상세에서 기부로 넘어온 경우에만 살아난다. 이 MSA에는 그 진입 경로가 없어 넣지 않았다.
- AS-IS의 "납부정보 확인 → 마이페이지 바로가기"는 기부하기 화면에서 띄우기 때문에 필요한 버튼이다. 이 모달은 이미 기부내역 화면 위에 떠 있어 **"기부내역 확인"(모달 닫기)**로 바꿨다.

#### 검증 (실측, test007/userId 1063)
`POST /donate` → 302 `/my?donated=success` → `POST /donations/{sn}/complete` → **302 `/my?completed=D202609100949204070`** → 모달 렌더 확인(기부자명 김진호 / 10,000 원 / 서울특별시 강남구 / 3,000 P·30% / MOCK-BUGA-… / 답례품몰 링크 `?locgovCode=11230`). 링크 대상 `http://localhost:8084/?locgovCode=11230` 200이고 해당 지자체 답례품 1건만 노출(DB상 11230 등록 건수와 일치). 남의 건번호·존재하지 않는 건번호로는 모달 미렌더. 스크린샷으로 레이아웃 확인.

> 검증용으로 만든 10,000원 기부 1건은 앱의 정상 취소 경로로 되돌렸다 — 상태 `CANCELLED`, 포인트 33,000 → 30,000으로 복원 확인. `donation.g_cntr`에 CANCELLED 행 1건이 남아 있다.

### 남은 것 (§6에 추가)

| # | 항목 | 규모 / 필요한 결정 |
|---|---|---|
| 3-7 잔여 | 결제창 자체(신청→결제→완료 한 흐름) | PG·세외수입 연계 개방 전에는 모크뿐. "가상 결제창" 화면을 두고 흐름을 이어붙일지 별도 판단 필요 |
