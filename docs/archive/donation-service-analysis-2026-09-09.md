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

### 3-3. ~~point 연계에 동기 호출 경로가 남아 있음~~ → **조치 완료 (2026-09-21)**
정상 기부 완료는 이벤트(`DonationCompletedEvent`)로 처리된다 ✅. 남아 있던 유일한 예외인 **기부금 변경신청(give-reqmng) 승인의 "포인트생성"**이 `PointClient.creditForDonation()` 동기 REST 쓰기를 쓰고 있었는데, 정상 완료 경로와 똑같이 `donationEventPublisher.publishCompleted()`로 `donation.lifecycle` COMPLETED 이벤트를 재발행하도록 전환했다(`CntrReqmngService.approve`, TYPE_POINT_CREATE 분기). donation의 `PointClient.creditForDonation` 쓰기 메서드는 제거하고 조회 전용으로 축소했다. 이로써 **donation→point 동기 쓰기호출이 완전히 사라져** RFP SFR-003 "동기 호출 금지"/SFR-004 ★를 충족한다.

- **안전 근거**: `donation.lifecycle` COMPLETED 소비자가 point(`DonationEventListener`→`creditForDonation`, `existsByRefKeyAndTxnType(cntrSn,EARN)` 멱등)와 admin 통계(`DonationStatsListener`→`onDonationCompleted`, `stat_donation_ledger` cntrSn PK upsert) 둘뿐이고 **둘 다 멱등**이라 재발행해도 중복 적립·중복 집계가 없다.
- **유지**: point 엔드포인트 `/api/admin/credit-for-donation` 자체는 admin `ResyncService`(운영 백필)가 여전히 사용하므로 남겨 둔다. 제거한 것은 donation 쪽 클라이언트 호출뿐이다.
- **트레이드오프**: 동기는 실패 시 승인 자체가 실패(재시도 유도)했으나, 이벤트는 승인 즉시 성공하고 적립은 결과적 정합성으로 수렴한다 — 이는 mainline `completeDonation()`과 **동일한 성격**이라 오히려 일관성이 맞다.
- **E2E 실측(2026-09-21)**: Kafka·point(8083)·admin(8085) 기동 상태에서 시드 기부건(`DTESTRQMNG00001`, user 1063, 11230, 50000) 대상으로 give-reqmng 포인트생성 신청→승인. `Published COMPLETED event ... offset=36` → point EARN **15000**(30%) 생성 + admin `stat_donation_ledger` COMPLETED 반영 확인. 2차 신청·승인으로 재발행해도 point EARN 1건·15000, admin 1건 유지(멱등 확인). 검증 후 시드·원장·통계·잔액을 물리 정리해 원상복구(1063 잔액 137000→122000).

### 3-4. ISP 조회모델 없음 (요구사항 미충족, 중)
ISP 조회모델 "기부요약/영수증/한도, 결제상태/대사뷰". 별도 조회 모델 없이 원본 테이블 직접 집계. point·member와 같은 유형의 갭.

### 3-5. 기부 상태코드 (사실상 충족 — 기록용)
AS-IS `G_CNTR.CNTR_STTUS_CODE`는 `100`(신청/결제대기) `200`(납부완료) `300`(취소, `giveCancelProcess`) `900`(수납배치 완료, 서울 세외수입 `updateSunapBatchCompleted`) 4종. MSA는 `REQUESTED`/`COMPLETED`/`CANCELLED` 3종으로 **900만 없다** — 900은 서울 세외수입 수납배치에 종속된 상태라 그 연계(비활성)와 함께 묶이는 의도적 축소로 본다. RFP 문구의 "결제대기/환불"은 AS-IS에도 별도 코드가 없어(100이 결제대기 겸용, 300이 취소·환불 겸용) 추가 대응 불필요.

### 3-6. ~~기부하기에서 지도로 지자체 선택 불가~~ → **오판, 철회 (2026-09-21 재검증)**
최초 분석은 AS-IS `map-select.html` **파일이 존재**하는 것을 보고 "재현 누락"으로 적었으나 **틀렸다**. 운영 도달 가능성을 검증하지 않은 것이 원인이다(guide3·기부완료모달과 같은 유형의 오판).

실제로는 **AS-IS 프론트엔드 어디에도 `map-select.html`로 진입하는 순방향 버튼·링크·메뉴가 없다**:
- 모든 `location.href="/donation/map-select.html"` 참조가 `goToBack`(뒤로가기, `flag=='map'`일 때만) 안에 있다 — 순방향 진입 0곳. `flag`은 URL 파라미터(`getParameter("flag")`)로만 설정된다.
- 콘텐츠관리 메뉴에서 명시적으로 제외(`opmanager/.../cntnts-stsfdg/list.jsp`의 `c:if`가 `map-select.html`·`guide4.html`을 거른다).
- `list-select.html`의 지도 안내 이미지(`1.지도에서 지역 선택하기...`)는 **"고객 요청: 해당 이미지 삭제"**로 주석처리됐다.
- `donation-main.html`의 LNB 지도탭 하이라이트 로직(line 1034)도 주석처리.
- header의 `@show-map-select`(`components/layouts/header_g.vue` 등)는 답례품몰(goods)·장바구니·주문의 **지역필터용 별개 팝업**(`map.vue`)이라 기부 플로우와 무관하다.

즉 **지도선택은 파일만 남은 死화면(retired)이고, AS-IS 운영도 목록선택(`list-select.html`)만 노출**한다. MSA는 이미 목록선택(`ListSelectView.vue`)을 구현했으므로 **운영 AS-IS와 동등하다** — 착수할 갭이 아니다. (사용자가 라이브 AS-IS 화면 `localhost:3000/donation/donation-main.html`에서 "지도에서 보기" 버튼이 없음을 발견해 정정)

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
| ~~3-3~~ | ~~`creditForDonation` 동기 호출~~ | **완료(2026-09-21)** — give-reqmng 승인을 `publishCompleted` 이벤트 재발행으로 전환, 동기 쓰기 제거. E2E 실측 완료. 위 §3-3 참고 |
| 3-4 | ISP 조회모델(기부요약/영수증/한도, 결제상태/대사뷰) | point ReadModel이 DB 설계 대기 중이라 같은 시점에 판단 |
| ~~3-6~~ | ~~기부하기 지도 선택~~ | **철회(2026-09-21)** — AS-IS 운영도 미노출인 死화면. MSA 목록선택으로 이미 동등. 위 §3-6 참고 |
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
