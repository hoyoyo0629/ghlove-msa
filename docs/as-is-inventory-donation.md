# AS-IS 인벤토리 — donation 도메인

> 기준·절차: [[as-is-logic-is-the-spec]], [[as-is-inventory-procedure]], [[scope-migration-not-greenfield]]
> 판정: `대응있음` / `부분` / `재현누락` / `의도적축소` / **`죽은코드`**
> 작성 2026-09-10. 범위 배정은 [[as-is-coverage-map]] 참조.

## 1. 화면 생사 판정

### 1-1. 진입 래퍼 패턴
AS-IS 기부 화면은 **래퍼 → 실제화면** 2단 구조다. 이걸 모르면 실제 화면을 사장으로 오판한다.

| 래퍼 | 하는 일 | 실제 화면 |
|---|---|---|
| `donation/donation.html` | **넷퍼널(NetFunnel) 접속자 대기열** + `2024/02/08 18:00 ~ 02/12 09:00` 세외수입 전환 점검 차단(`:96~104`) | `donation-main.html` |
| `donation/donationNext.html` | 동일 패턴 | `donationNext-main.html` |
| `designated-donation/index.html` | 동일 패턴 | `index-main.html` |

### 1-2. live (18)
`donation.html`(래퍼), `donation-main.html`(실제 기부하기), `list-select.html`, `map-select.html`, `complete.html`(**기부하기 "처리중"** 로딩화면 — 완료화면이 아니다), `guide1/2/3/5/6.html`, `giro-success.html`·`giro-fail.html`(지로 결과 착지 — `NgDonationRelayServiceImpl:716~717,1361~1362`), `wetaxInfo_m.html`(`mypage/cntrList.html:643` window.open), `designated-donation/index.html`·`index-main.html`·`details.html`

mypage 기부분 live: `cntrList.html`(32), `receiptList.html`(10), `receiptPrint.html`(1), `receiptListPrint.html`(2), `honorList.html`(14, **기부혜택증**), `intrstLocGov.html`(15)

### 1-3. 죽은코드 (26)
- **inbound 0**: `donation-backup`, `donation_bak`, `donation-tax`, `donation-test`, `donation_buga_test`, `donation_girotest`, `donationNext`, `donationNext-main`(죽은 래퍼로만 도달), `donation-main_20240529`, `guide1/2/5_20240529`, `guide1/2/3/5/6_20240613`, `guide4_bak`, `quick-donation`, `quick-donation_bak`, `ngdonation`, `process`, `wetaxInfo`, `intro`(donation/ 하위 — 루트 `/intro.html`과 다른 파일), `popup-fail`, `popup-success`, `external-etax`, `external-etax-2`, `external-giro`, `external-giro-2`
- `mypage/honorList_new.html`(0), `mypage/qrSample.html`(0), `mypage/qrTest.html`(0), `mypage/honorList.html_20240927`
- **`mPop.html`**: 모든 참조가 `//donation.giroPopup = window.open(...)` 주석 → 도달불가
- **`donation-lnb_ali.vue`**: 이 LNB 컴포넌트는 `httpVueLoader` 등록이 **전부 주석 처리**되어 어느 화면에서도 렌더되지 않는다. 그 안의 메뉴는 `donation.html`/`list-select.html` 둘뿐.
- **`guide4.html`**: 파일 자체가 없고(`guide4_bak.html`만 존재) 참조도 전부 주석. 메뉴 문구는 "종합소득세 확정신고 서비스 안내" → **기능 비활성**

### 1-4. 살아있는 안내 메뉴 (`components/layouts/header_ali.vue:276~303`)
순서대로: 지자체 선택(`list-select`) → **고향사랑기부제 안내**(guide1) → **온라인 기부방법**(guide2) → **오프라인 기부방법**(guide5) → **연말정산 세액공제 안내**(guide3) → **고향사랑기부 주의사항**(guide6). guide4 자리는 비어 있다.

## 2. 컨트롤러 — 중복 구현 3세트 중 2세트가 사장

같은 기능이 세 벌 존재한다. **살아있는 것은 `/api/ngdonation` 하나뿐**이다.

| 컨트롤러 | 엔드포인트 | 판정 | 근거 |
|---|---|---|---|
| `NgDonationController` `/api/ngdonation` | 20 | **live** (2건 사장) | `donation-main.html` + `modules/op.donation.js` |
| `RegionTaxController` `/api/regiontax` | **12** | **죽은코드(전부)** | 12개 전부 프론트 호출처 0 |
| `SeoulTaxController` `/api/seoultax` | **4** | **죽은코드(전부)** | `modules/op.seoul.donation.js`가 유일 참조인데, 그 모듈을 로드하는 화면이 `donation-tax.html`(inbound 0)뿐 |
| `DonationTestController` `/api/donationTest` | **4** | **죽은코드(전부)** | 호출처 0 |

> `/api/regiontax`는 `/api/ngdonation`의 리팩터링본으로 보인다(`locGovLmtt //getLocGovLmtt` 같은 주석이 원본 메서드명을 달고 있다). **적용되지 않은 채 남았다.**
> `modules/op.donation.external.js`(→`external-etax-2`/`external-giro-2`), `modules/op.donation.test.js`(로드 화면 0)도 사장.

### 2-1. `/api/ngdonation` 상세
| 엔드포인트 | 역할 | live 호출처 | 판정 |
|---|---|---|---|
| `/userCntrInfo` | 기부자 정보 | `donation-main.html` | live |
| `/sidoList`·`/sigunguList` | 시·도/시·군·구 | `op.saleson.js` 래퍼 | live |
| `/locGovInfo` | 지자체 정보 | `donation-main`, `list-select`, `map-select` | live |
| `/rsgstadresinfo` | **주민등록 주소 조회(거주지 확인)** | `donation-main.html` | live |
| `/rsgstadresinfoForeigner` | 외국인 주소 조회 | `op.saleson.js` | live |
| `/getPublicKey` | 전송 암호화 공개키 | `op.saleson.js` | live |
| `/getLocGovLmtt` | **지자체 기부제한** | `donation-main`, `map-select`, `items/details-main` | live |
| `/getIntrstLocgov`·`/setIntrstLocgov` | 관심지자체 조회·설정 | `list-select`, `map-select`, `goods/searchGoods-main` | live |
| `/sntrBugaInsert` | 서울 부가정보 등록 | `op.donation.js` | live |
| `/etaxSunapInfo` | 서울 이택스 수납정보 | `op.donation.js` | live |
| `/sunapSuccess` | 수납 완료 처리 | `op.donation.js` | live |
| `/contryBugaInsert` | 지방(위택스) 부가정보 등록 | `op.donation.js` | live |
| `/contryNextSunapInfo` | 차세대 세외수입 수납정보 | `op.donation.js` | live |
| `/nextBugaRequest` | 차세대 부가정보 요청 | `op.donation.js` | live |
| `/local-sunap-confirm` | 지방 수납 확인 | `op.donation.js` | live |
| `/giroPay` | **지로 납부** | `op.donation.js` | live |
| `/contrySunapInfo` | 지방 수납정보(구) | **없음** | **죽은코드** |
| `/giroPayTest` | 지로 테스트 | **없음** | **죽은코드** |

### 2-2. `/api/designated-donation` (8)
`getList`, `getListBySearchEngine`, `getBsnsTypes`, `getDetail`, `getDesignatedCntrList`, `saveCheerMsg`(응원메시지), `getNoticeList` — 전부 live.
**`getFaqList`** — 호출처 0 → **죽은코드**.

### 2-3. `/api/mypage` 기부분 (14)
live: `getCntrList`, `getCntrInitInfo`, `getReceiptInitInfo`, `getReceiptPopInfo`, `honorList`, `honorList-new`, `saveHonorViewHist`, `intrstLocGovInfo`, `deleteIntrstLocGov`, `deleteIntrstLocGovAll`, `receipt-print`, `getLocGov`
**죽은코드**: `qr`, `qrTest` — 각각 `qrSample.html`/`qrTest.html`에서만 호출되는데 두 화면 모두 inbound 0.
> `honorList-new`는 API는 살아있다(`honorList.html`이 `getHonorListNew`를 호출). 사장인 것은 **화면** `honorList_new.html`이다.

## 3. 매퍼 쿼리 (핵심 7종 / 261 쿼리 중 20 사장)

| 매퍼 | namespace | 쿼리 | 사장 |
|---|---|---|---|
| `ngdonation-mapper.xml` | `saleson.shop.donation.NgDonationMapper` | 83 | **12** |
| `designated-donation-mapper.xml` | `...designateddonation.DesignatedDonationMapper` | 60 | 1 |
| `give-state-mapper.xml` | `...give.givestate.GiveStateMapper` | 47 | 4 |
| `locgov-mapper.xml` | `saleson.shop.user.LocgovMapper` | 34 | 3 |
| `offgive-mapper.xml` | `saleson.shop.offgive.OffgiveMapper` | 22 | 0 |
| `lclgvHnrUser-mapper.xml` | `...lclgvHnrUser.LclgvHnrUserMngMapper` | 11 | 0 |
| `locgov-image-mapper.xml` | `saleson.shop.user.LocgovImageMapper` | 4 | 0 |
| **합계** | | **261** | **20** |

(admin 배정 대상은 별도: `give-statistics` 25, `give-opertaion` 14, `locgov-databoard` 15, `statistics-locgov` 4 / point 배정: `give-point` 8, `give-point-expiration` 5, `order-give-point` 16)

### 3-1. 사장 쿼리 목록
- `ngdonation` **XML 고아**(인터페이스 미선언): `getCntrLocgovCode`, `getUserCiFromOpUserCi`
- `ngdonation` 호출 0: `deleteNotSunapStndOneBatch`, `getPresentTypeList`, `getProcessDeptCd`, `getSeoulNotSunapList`, `getStandardNotSunapList`, `getUserCI`, **`getUserCntrLimit`**, `insertRelayLog`, `selectHometaxCount`, `updateCntrBlcePoint`
- `locgov` XML 고아: `getLocgById` / 호출 0: `resetHonorCntrUser`, `resetStdrAmt`
- `designated-donation` XML 고아: `getMaxOrderingDesignatedDonationNoticeImgDesc`
- `give-state` 호출 0: `getCntrTaxTempLog`, `insertCntrTaxTempLog`, `giveDeleteAt`, `giveReqmngSmsUserInfoByReqId`

> **`getUserCntrLimit`**(회원 기부한도)와 **`updateCntrBlcePoint`**(기부잔액 포인트)가 호출 0이다. 즉 한도 검증이 이 쿼리로는 돌지 않는다 — **실제 경로는 `DonationVerification`(공통코드 `DONATION_LIMIT_AMT`)이며 §5-1에서 규명했다.**
> `insertRelayLog`(중계 로그)도 호출 0인데, MSA에는 `/api/admin/relay-log`가 있다. 근거 재확인 필요.

## 4. MSA 대응 요약

MSA donation은 컨트롤러 25개 / 엔드포인트 약 130개로 AS-IS보다 넓다(운영관리 API를 함께 갖고 있기 때문).

**대응있음(주요)**
- `list-select.html` → `/list-select` + `/api/list-select/{provinces,cities,fund}`
- `guide1/2/5/6` → `/guide1`,`/guide2`,`/guide5`,`/guide6`
- **`guide3`(연말정산 세액공제 안내) → `/honor`** — 라우트명은 다르나 `honor.html`의 `<title>`이 `안내사항 > 연말정산 세액공제 안내`로 동일하고, 헤더·푸터가 같은 문구로 링크한다. **경로만 보고 누락으로 판정하면 안 되는 사례**([[verify-screen-by-content-not-route]])
- `mypage/honorList.html`(기부혜택증) → `/honor/certificates` + `honor-certificates.html`
- `mypage/cntrList.html` → `/my`, `mypage/receiptList·receiptPrint` → `/receipts`, `/receipts/official/{cntrSn}`
- `mypage/intrstLocGov.html` → `/interest-locgovs`
- `designated-donation/*` → `/designated-donation`, `/designated-donation/{id}`, 응원메시지(`cheerMsg`) 포함
- `getLocGovLmtt` → `/api/locgov-lmtt`
- `rsgstadresinfo`(거주지 확인) → `/donate/verify-residence`

**의도적축소 (외부연계 미개방)**
- **넷퍼널 접속자 대기열** — `donate.html:28`에 "이번 스코프에서는 제외" 명시. CSS(`default_ali.css`)에 `#NetFunnel_Skin_Top` 잔재만 있다.
- 지로 납부(`giroPay`) 및 착지화면 `giro-success`/`giro-fail`, 서울 이택스/위택스 수납 연계(`sntrBugaInsert`·`etaxSunapInfo`·`contryBugaInsert`·`contryNextSunapInfo`·`nextBugaRequest`·`local-sunap-confirm`·`sunapSuccess`)
- `complete.html`(기부하기 **처리중** 화면) — 결제 대기 로딩이라 PG 미연계 시 해당 없음
- `getPublicKey`(전송 암호화), `rsgstadresinfoForeigner`(외국인 주소)
- `wetaxInfo_m.html`(위택스 안내 팝업)

**재현누락**
- **`map-select.html` 지도로 지자체 선택** — MSA에 지도 화면 없음. `LocgovMapProvince.ALL`은 시·도 **목록 데이터**로만 쓰인다(`DesignatedProjectController:64`, `FundProjectApiController:41`). [[donation-service-deferred-items]]의 "지도 선택"
- `/api/designated-donation/getFaqList`는 AS-IS에서도 죽은코드라 이식 대상 아님

**확인필요**
- AS-IS 연 500만원 기부한도의 실제 검증 경로(§3-1 `getUserCntrLimit` 호출 0)
- `receiptListPrint.html`(기부확인증 일괄 인쇄) 대응
- `goods/searchGoods-main.html`의 `setIntrstLocgov` 호출(gift 화면에서 관심지자체 설정) 대응

---

## 5. 조치 결과 (2026-09-10)

### 5-0. 초판 오판 정정 — 주석 블록 사각지대
| 항목 | 초판 | 실제 |
|---|---|---|
| 기부제한(`getLocGovLmtt`) 진입점 | "AS-IS 4곳 / MSA 2곳 → 부분" | **live 진입점은 2곳뿐, MSA와 일치 → 대응있음**. `list-select.html:830`은 블록 전체가 `/* */` 안이고 호출 경로도 존재하지 않는 `/api/donation/getLocGovLmtt`다. `map-select.html:1443`도 주석이며, 실제로는 `locGovInfo` 응답에 실려 온 `lmttBgnDe`/`violtResnCn`으로 판정한다(`:1430~1435`). |

> member의 "탭 3개/네이버 버튼" 오판과 같은 뿌리다. **핸들러·호출을 셀 때 주석 블록 안인지 반드시 본다.**

### 5-1. 연간 기부한도 — 규명과 조치
AS-IS 한도 로직은 **두 벌**이고 실제로 도는 것은 공통코드 쪽이다.
- `DonationService:93` `MAX_CNTR_AMT_LIMIT = 5000000` 하드코딩 → `userCntrLimitAmt` 표시용(`:246,:256`)
- **`DonationVerification.isDonationNormalAmount(userId, amount)`** ← 실제 검증. 호출처 3곳: `NgDonationController:358`(서울), `:692`(차세대), `OffgiveServiceImpl:339`(오프라인)

```
limitAmt = 공통코드 DONATION_LIMIT_AMT[올해].label      // 연도별. 하드코딩 아님
maxCntrAmt = limitAmt - getGCntrSumCntrAmt(userId)      // 본인 올해 기부합
                      - getGMberSecsnSumCntrAmt(mberCi) // 올해 탈퇴분 기부합 (CI 기준)
                      - getGcntrWegiveCntrAmt(mberCi)   // 위기부 합
return !(amount % 100 != 0 || amount < 0 || amount > maxCntrAmt)
```

§3-1에서 "호출 0"으로 나온 `getUserCntrLimit`는 **쓰이지 않는 다른 구현**이었다. 실제 경로는 위와 같다 — 규명 완료.

> **AS-IS 자체 결함**: 지방(위택스) 경로 `contryBugaInsert`(`NgDonationController:477~515`)에는 이 검증이 **없다**. 서울·차세대·오프라인에만 걸려 있다. MSA는 전 경로에서 검증하므로 이식 대상이 아니다.

### 5-2. 완료 (5건)

1. **100원 단위 검증** — `DonationService.requireAmount()`에 `amount % 100 != 0` 추가. 세외수입 고지서가 100원 단위로만 생성되기 때문에 AS-IS가 건 제약이다.
2. **탈퇴회원 기부액 CI 기준 합산** — 가장 큰 구멍이었다. `member.g_mber_secsn` **테이블은 있는데 어느 서비스도 참조하지 않아**, 탈퇴 후 재가입하면 연간 한도가 초기화됐다.
   - member: `MberSecsn` 엔티티/저장소 신설, **탈퇴 두 경로 모두**(`MemberService.withdraw` 본인탈퇴, `AdminMemberService.adminWithdraw` 강제탈퇴)에서 `DonationClient.mySummary().thisYearAmt()`로 그 해 기부액을 스냅샷.
   - member: `GET /api/users/{userId}/withdrawn-donation-carryover` — CI는 개인식별자라 응답에 담지 않고 **합계만** 반환.
   - donation: `MemberClient.withdrawnDonationCarryOver()` → `validateAnnualLimit`의 누적액에 가산. **조회 실패 시 0이 아니라 예외를 던진다** — 실패를 0으로 삼키면 그 순간이 한도 우회 창구가 된다. 표시용 `remainingAnnualLimit`만 실패를 0으로 보고 화면을 살린다(차단은 어차피 제출 시 걸린다).
3. **탈퇴회원 CI로 재가입 허용** — 2번을 검증하다 발견. member 작업에서 넣은 CI 1인1계정 검사가 **탈퇴 회원까지 막고 있었다**. AS-IS `getUserInfoByCi`는 `status_code = '9'`(정상)만 조회하며(상태코드 `1:가입대기 2:차단 3:탈퇴 4:휴면 9:정상`, `GeneralCustomer.java:30`), 애초에 `G_MBER_SECSN`의 존재 자체가 **재가입을 전제**한다. `findFirstByMberCiAndStatusCodeOrderByUserIdDesc(ci, ACTIVE)`로 교정.
   - 부수 효과: 한 CI에 여러 계정이 쌓일 수 있어 `Optional<User> findByMberCi`는 `NonUniqueResultException`을 낸다. `ExternalLoginService.findLinkedUser`도 같은 방식으로 상태를 좁히도록 함께 고쳤다.
4. **기부혜택증 열람이력** — `HonorViewHist` 엔티티/저장소 + `recordHonorCertificateViews()`. AS-IS는 캐러셀 `slideChange`마다 기록하지만(`honorList.html:381`) MSA는 목록으로 전부 보여주므로 화면을 열 때 표시된 지자체를 모두 기록한다.
   - AS-IS `saveHonorViewHist`는 저장 직후 **`throw new OpRuntimeException("test")`가 무조건 실행되어 항상 `SYSTEM_ERROR`를 응답**한다(`MypageController:1078`). 디버그 잔재이므로 옮기지 않았다.
5. 기부제한 진입점 판정 정정(§5-0).

### 5-3. 검증
donation·member 재기동 후 실측:

| 확인 | 결과 |
|---|---|
| 12,345원 기부 | `기부금액은 100원 단위로 입력해 주세요.` |
| 3,000,000원 기부 | 성공(COMPLETED) |
| 탈퇴 실행 → `g_mber_secsn` | `2026 / 1068 / CARRY-CI-0001 / 3000000` |
| 같은 CI로 재가입 | 성공 (교정 전에는 차단됐음) |
| 재가입 계정 이월액 조회 | `{"amount":3000000}` |
| 한도 2,000만 − 이월 300만 = 1,700만 / **1,800만 요청** | `연간 총 기부한도(20,000,000원)를 초과합니다. 올해 누적 기부액: 3,000,000원` |
| **1,700만 요청** | 성공 |

검증 데이터(회원 2, 기부 2, 포인트원장 2, 잔액 2, 스냅샷 1)는 전부 삭제 확인.

### 5-4. 미처리
- **`map-select.html` 지도 선택** — 규모가 커 별도 판단 필요. [[donation-service-deferred-items]]
- **위기부 합산**(`getGcntrWegiveCntrAmt` / `sumWegiveCntrAmtByWegiveAmtAndMberCi`) — "위기부"의 의미와 데이터 출처 확인 후 결정
- `receiptListPrint.html`(일괄 인쇄), `getListBySearchEngine`(검색엔진 연계), `getNoticeList` 공개 조회 경로
- `goods/searchGoods-main.html`의 `setIntrstLocgov` 호출 대응(gift 화면에서 관심지자체 설정)
- MSA 전용 운영관리 API 5종(`locgov-admin`/`cntr-reqmng`/`ctbny-opratn`/`welfare-centers-admin`/`nts-receipt-logs`)의 admin 도메인 배정 검토

---

## 6. 근거 확인 — 기부금영수증 화면보안(Fasoo Secure Web) 대체 검토 (2026-09-17)

기부금영수증 출력 화면(AS-IS `mypage/receiptPrint.html`)에 걸려있는 상용 화면보안 SW의 정체·용도·요구근거를 규명하고, 탈상용(제거) 가능성을 판정한다. (발단: 상용SW 지속사용 여부 검토)

### 6-1. AS-IS 실체 — 두 개의 다른 Fasoo 제품 구분

| 구분 | 코드 실재 | 정체 | 위치/근거 |
|---|---|---|---|
| **Fasoo Secure Web (FSW) 4.2.0.0** | ✅ 프론트 코드에 실재 | 브라우저 **화면보호 클라이언트**(네이티브 에이전트, IE=ActiveX `f_swv.dll` / 크롬·엣지=확장+네이티브호스트) | `ghlove-frontend/webDrm/`(fsw.js·ie.js·multi.js·Setup·헬프데스크 HTML 20여개), 운영도메인(`ilovegohyang.go.kr` 등)용 라이선스 시리얼 하드코딩 |
| **Fasoo Enterprise DRM 5** | ❌ 앱 코드에 없음 | 서버/파일 단위 **문서·DB 보안(DBMS문서보안)** | ISP SW 인벤토리(원본 p.325)에 "재활용" 전제로만 기재 |

→ **둘은 다른 제품.** 영수증 화면에서 실제로 동작하는 것은 FSW(화면보호)이고, ISP가 말하는 Fasoo는 Enterprise DRM(문서/DB보안)이다. 혼동 주의.

### 6-2. FSW 사용처·용도

- **적용 화면: 단 1곳** — `mypage/receiptPrint.html`(마이페이지→기부내역→"기부금영수증 출력" 팝업)에서만 `/webDrm/fsw.js` 로드. 전체 프론트/JSP 통틀어 유일. 관리자(opmanager) 화면엔 없음.
- 이 화면은 **OZReport(OZ Viewer 8.0)**로 `3.cntr_receipt_new.ozr` 리포트를 렌더 → 그 위에 FSW가 화면보호 권한 강제.
- FSW 권한(multi.js `arrRights`) = 캡처차단(SCREENCAPTURE/CAPTURE), 소스보기 차단(VIEWSOURCE), 저장차단(SAVEAS/SAVEIMAGE), 복사·추출 차단(COPY/EXTRACT), 워터마크 인쇄(WATERMARK_PRINT), 키보드보안(SECURE_KEY), 인쇄만 허용.
- 화면 코드도 OZ 저장버튼 disabled+hide, 인쇄버튼만 활성, `viewer.lockopt=true`. 인쇄 실행 시 `OZPrintCommand_OZViewer`→`/api/mypage/receipt-print/insert`로 인쇄이력 기록.
- **용도 결론:** 세액공제 증빙(개인정보+직인 포함)인 영수증의 캡처·저장·소스보기를 막아 위변조/무단복제를 억제하고 인쇄만 허용+추적하는 "출력화면 보안".

### 6-3. 순수 웹 대체 가능성

- **원리적 한계:** 캡처차단·키보드보안은 **OS 레벨 네이티브 에이전트**가 하는 일이라 브라우저 샌드박스 안의 웹 코드로는 **재현 불가**. "동일 동작"을 원하면 결국 타 상용 화면보안 제품(마크애니/소만사/잉카 등) 네이티브 에이전트로 **교체**하는 것뿐 → 탈상용 목적 미달성.
- **웹표준으로 가능한 실질보호(상용SW 불요):** ① **서버 렌더 워터마크**(기부자명·발급일시·문서번호를 문서에 직접 인쇄 — 캡처당해도 남아 추적 가능, 위변조억제엔 오히려 더 강함) ② 복사/드래그/우클릭 완화 ③ 인쇄이력 기록(MSA 재현완료) ④ 저장버튼 제거·인쇄전용(재현완료). = "차단(enforcement)"이 아닌 "억제(deterrent)" 수준.

### 6-4. RFP/ISP 요구근거 — **없음**

(저장소에 원본 전문 없음, 요약본 `requirements.md`·`isp-detailed-design-summary.md` 기준)

- **RFP**: 영수증 요구는 "기부영수증 및 기부확인증 발급/재발급(전자 방식)"(발급 기능)뿐. 화면캡처/출력물 유출방지/워터마크 요구 **없음**. 보안요구는 송수신 암호화·역할권한·감사로그(데이터 레벨)뿐.
- **ISP**: "화면보안/캡처/워터마크/출력물 유출방지" 매칭 0건. Fasoo는 Enterprise DRM(DBMS문서보안) "재활용" 항목으로만 등장 — FSW 화면보호를 지목한 기능/보안 요구가 아님. 요구 보안은 N2SF DB암호화·개인정보접속기록(WEDDS)·망분리 등 인프라·데이터 계층.
- **추정:** FSW는 신규 RFP/ISP 요구가 아니라 **AS-IS(SalesOn 원제품/기존 운영환경)에서 관성적으로 딸려온 것**.

### 6-5. 판정 및 잔여 확인사항

- **판정: 화면보안 필수 요구 근거 없음 → FSW(상용SW) 제거 가능.** 대체안 = 서버 워터마크 + 인쇄이력 + 국세청 전자기부금영수증 연계(정본은 홈택스 전자문서, 종이는 참고용). 이 조합이면 탈상용+위변조억제 동시 달성.
- **MSA 현황:** OZReport는 Thymeleaf 서버렌더+`window.print()`로 대체, 인쇄이력(`/receipts/official/{cntrSn}/print-log`) 재현완료. **FSW 화면보호 계층은 미재현**(의도적, 웹표준 등가 대체 불가). 워터마크는 미적용 상태 → 대체 결정 시 추가 필요.
- **최종 제거 전 안전판:** 공공사업 특성상 RFP 본문 외 **「보안요구사항 정의서」/보안성 심의 산출물** 별첨에 '출력물·화면 유출방지' 항목이 있는지 발주처에 실무 확인 권장(현재 저장소엔 해당 별첨 없음).
