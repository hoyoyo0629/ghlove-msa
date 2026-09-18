# 대민 Thymeleaf → storefront SPA 커버리지 매핑 (폐기 대상 판정)

- 작성: 2026-09-17
- 목적: 5개 서비스(member·donation·point·gift·order)의 Thymeleaf 페이지가 **이미 storefront Vue3 SPA로 대체되었는지** 대조해, 안전하게 삭제할 목록을 확정한다.
- 전제: 대민 프론트의 Vue3+SPA(정본)는 **이미 storefront에 존재**한다. 따라서 이 작업은 "신규 전환"이 아니라 **중복 Thymeleaf 폐기(decommission)**다.
- 범위 제외: 판매자/제공자·운영관리는 PL 그룹 결정 후 별도. 이 5개 서비스 안에 섞여 있는 그 성격의 템플릿은 **유지**로 둔다.

## 판정 범례

| 판정 | 의미 | 조치 |
|---|---|---|
| **삭제** | SPA 라우트가 이미 대체 | 커버 확인 후 Thymeleaf 페이지+뷰@Controller 삭제 |
| **확인필요** | SPA에 유사 라우트는 있으나 완전 대체 여부 미확정 | 삭제 전 화면 대조 필요 |
| **유지-프린트** | 서버렌더 출력 페이지(SPA 화면 아님) | 유지(별도 결정) |
| **유지-범위밖** | 판매자/운영/배치 = 제외군 | 유지 |
| **유지-미커버** | 대민이지만 SPA에 아직 없음 | SPA 구현 전까지 유지 |

## member (22개: 페이지 17 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| home.html | `/`(home) | `/` | 삭제 |
| login.html | `/login` | `/login` | 삭제 |
| signup.html | `/signup` | `/signup` | 삭제 |
| find-idpw.html | `/find-idpw` | `/find-idpw` | 삭제 |
| mypage.html | `/mypage` | `/mypage` | 삭제 |
| profile.html | `/profile` | `/mypage/profile` | 삭제 |
| password.html | `/mypage/password` | `/mypage/password` | 삭제 |
| withdraw.html | `/mypage/withdraw` | `/mypage/withdraw` | 삭제 |
| delivery/list.html | `/delivery/list` | `/mypage/delivery` | 삭제 |
| delivery/write.html | `/delivery/edit/{id}` | `/mypage/delivery/new`,`/:id/edit` | 삭제 |
| roles/request.html | `/roles/request` | `/mypage/role-request` | 삭제 |
| roles/queue.html | `/roles/queue` | `/mypage/role-queue` | 삭제 |
| personal-info.html | `/profile/personal-info` | `/mypage/profile`? | **확인필요** |
| reactivate.html | `/reactivate` | (없음) | **유지-미커버** (휴면 재활성화) |
| coming-soon.html | `/coming-soon` | (없음) | **유지-미커버** (준비중 placeholder) |
| external-login-disabled.html | `/signup/finance-cert`,`/signup/mobile-auth` | (login 탭 일부) | **유지-미커버** (외부인증 연계, 보류군) |
| external-login-mock.html | (개발 mock) | — | **유지-미커버** (dev) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

## donation (26개: 페이지 21 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| donate.html | `/donate` | `/donate` | 삭제 |
| my.html | `/my` | `/mypage/donations` | 삭제 |
| receipts.html | `/receipts` | `/mypage/receipts` | 삭제 |
| designated-list.html | `/designated` | `/designated-donation` | 삭제 |
| designated-detail.html | `/designated/{id}` | `/designated-donation/:id` | 삭제 |
| honor.html | `/honor` | `/honor` | 삭제 |
| honor-certificates.html | `/honor/certificates` | `/mypage/honor-certificates` | 삭제 |
| interest-locgovs.html | `/interest-locgovs` | `/mypage/interest-locgovs` | 삭제 |
| guide/guide1,2,5,6.html | `/guide/*` | `/guide1,2,5,6` | 삭제 |
| guide/list-select.html | `/guide/list-select` | `/list-select` | 삭제 |
| policy/auth,copyright,privacy.html | `/policy/*` | `/policy/:slug` | 삭제 |
| certificate.html | `/certificate` | `/mypage/receipts`? | **확인필요** (기부확인증 화면) |
| honor-estimate.html | `/honor/estimate` | `/mypage/tax-credit-estimate`? | **확인필요** |
| offline.html | `/donations/offline` | `/mypage/donations/offline`? | **확인필요** (대민 조회 vs 담당자 접수) |
| receipt-official-print.html | `/receipts/official/{sn}` | (프린트) | **유지-프린트** (공식영수증 PDF) |
| certificate-print.html | `/certificate/print` | `/print/receipt-certificate`? | **확인필요-프린트** (SPA 라우트 존재) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

## point (8개: 페이지 4 + fragment 4)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| my.html | `/my` | `/mypage/points` | 삭제 |
| reservations.html | `/reservations` | `/mypage/points/reservations` | 삭제 |
| detail.html | `/detail` | `/mypage/points`? | **확인필요** (상세 vs 목록) |
| expire-batch.html | `/batch/expire` | (배치) | **유지-범위밖** (배치) |
| fragments/×4 | — | — | 페이지와 함께 삭제 |

## gift (17개: 페이지 12 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| list.html | `/gifts`(list) | `/gifts` | 삭제 |
| detail.html | `/gifts/{itemId}` | `/gifts/:itemId` | 삭제 |
| events.html | `/events` | `/events` | 삭제 |
| event-detail.html | `/events/{id}` | `/events/:id` | 삭제 |
| wishlist.html | `/wishlist` | `/mypage/wishlist` | 삭제 |
| my-reviews.html | `/my/reviews` | `/mypage/gift-reviews` | 삭제 |
| my-qna.html | `/my/qna` | `/mypage/gift-qna` | 삭제 |
| inquiries.html | `/my/inquiries` | (없음?) | **확인필요** (문의 vs Q&A) |
| my.html | `/my` | (gift 마이 허브) | **확인필요** |
| register.html | `/register` | — | **유지-범위밖** (판매자 등록) |
| edit.html | `/gifts/{itemId}/edit` | — | **유지-범위밖** (판매자 수정) |
| seller-dashboard.html | `/seller/dashboard` | — | **유지-범위밖** (판매자 대시보드) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

## order (16개: 페이지 11 + fragment 5)

| Thymeleaf | 컨트롤러 라우트 | SPA 라우트 | 판정 |
|---|---|---|---|
| cart.html | `/cart` | `/cart` | 삭제 |
| checkout.html | `/checkout` | `/checkout` | 삭제 |
| order-complete.html | `/order-complete` | `/checkout/done` | 삭제 |
| my.html | `/my` | `/orders` | 삭제 |
| detail.html | `/detail/{id}` | `/orders/:orderId` | 삭제 |
| claims/my.html | `/claims/my` | `/claims/my` | 삭제 |
| coupon/my.html | `/my/coupons` | `/my/coupons` | 삭제 |
| coupon/claimable.html | `/coupons` | `/coupons` | 삭제 |
| coupon/offline-claim.html | `/coupons/offline` | `/coupons/offline` | **확인필요** (대민 수령 vs 담당자) |
| create.html | `/`,`/orders` | `/checkout`? | **확인필요** (주문생성 흐름) |
| claims/queue.html | `/claims`(queue) | — | **유지-범위밖** (클레임 처리) |
| fragments/×5 | — | — | 페이지와 함께 삭제 |

## 확인필요 9건 판정 확정 (2026-09-17, 화면 대조 완료)

| # | 페이지 | 대조 근거 | 확정 |
|---|---|---|---|
| 1 | member/personal-info | "개인정보 수정" = ProfileView "회원 정보 수정" | **삭제** |
| 2 | donation/certificate | "기부확인증" = ReceiptListView | **삭제** |
| 3 | donation/honor-estimate | "세액공제 예상액" = TaxCreditEstimateView (지자체 추가혜택 섹션은 SPA 미보유·현재 비어있음) | **삭제** |
| 4 | donation/offline | OfflineDonationView(`/mypage/donations/offline`) 존재 | **삭제** |
| 5 | point/detail | "기부포인트 현황" = MyPointsView "기부포인트 조회" | **삭제** |
| 6 | gift/inquiries | "상품문의 관리" = GiftQnaView "답례품 Q&A" | **삭제** |
| 7 | **gift/my** | "내 답례품 관리" + 컨트롤러가 `currentSellerId`/`OP_SELLER` 사용 → **판매자 페이지** | **유지-범위밖** |
| 8 | order/create | "포인트로 주문하기" = CheckoutView "주문/결제" | **삭제** |
| 9 | order/coupon-offline-claim | 쿠폰은 SPA에서 숨김(redirect)+컴포넌트 존재, 일관 | **삭제** |
| + | donation/certificate-print | ReceiptPrintView(`/print/receipt-certificate`) 존재 | **삭제** |

## 확정 삭제 목록 (대민 중복, 54개 페이지)

- **member (13)**: home, login, signup, find-idpw, mypage, profile, personal-info, password, withdraw, delivery/list, delivery/write, roles/request, roles/queue
- **donation (20)**: donate, my, receipts, certificate, certificate-print, designated-list, designated-detail, honor, honor-estimate, honor-certificates, interest-locgovs, guide/guide1·2·5·6, guide/list-select, policy/auth·copyright·privacy
- **point (3)**: my, detail, reservations
- **gift (8)**: list, detail, events, event-detail, wishlist, my-reviews, my-qna, inquiries
- **order (10)**: cart, checkout, order-complete, my, detail, claims/my, coupon/my, coupon/claimable, coupon/offline-claim, create

## 유지 목록 (삭제 금지)

- **유지-프린트**: donation/receipt-official-print (공식영수증 PDF)
- **유지-범위밖(판매자/운영/배치)**: gift/register·edit·seller-dashboard·**my**, point/expire-batch, order/claims/queue
- **유지-미커버(대민이나 SPA 미구현)**: member/reactivate·coming-soon·external-login-disabled·external-login-mock
- **fragments (24)**: **유지** — 위 "유지" 페이지들이 header/footer/floating/alert 프래그먼트를 계속 쓰므로 삭제 불가

## 중요: Thymeleaf 의존성은 5개 서비스 모두 유지

각 서비스에 유지 페이지가 남는다(member 4·donation 1·point 1·gift 4·order 1 + fragments). 따라서 **이번 삭제로 `spring-boot-starter-thymeleaf`를 제거할 수 있는 서비스는 없다.** 목표는 "대민 중복 54개 폐기 + SPA 정본화"까지다.

## 집계 (구버전 추정 — 위 확정본으로 대체됨)

| 판정 | 수 |
|---|---|
| **삭제(커버 확정)** | ~43 페이지 (+ 딸린 fragment 24) |
| **확인필요** | 9 (personal-info, certificate, honor-estimate, donation offline, point detail, gift inquiries·my, order create·offline-claim) |
| **유지-프린트** | receipt-official-print (+ certificate-print 확인필요) |
| **유지-범위밖** | gift register·edit·seller-dashboard, point expire-batch, order claims/queue |
| **유지-미커버** | member reactivate·coming-soon·external-login×2 |

## 실행 결과 (2026-09-17, 1차 완료 — 5개 서비스 전부 컴파일 통과)

**대민 중복 Thymeleaf 폐기 + 뷰@Controller 수술을 5개 서비스 전부 완료했다.** 각 서비스 컴파일 검증(`gradlew compileJava` exit 0).

| 서비스 | 삭제 템플릿 | 삭제/수술 컨트롤러 | 남은 Thymeleaf(=2차 대상) |
|---|---|---|---|
| member | 13 | 4 삭제(Delivery/MyPage/Profile/RoleRequest) + AuthController 수술(find-idpw AJAX만) | login, external-login-disabled/mock, reactivate + fragments×5 |
| donation | 21 (offline 포함) | 8 삭제(Donation/Receipt/HonorBenefit/Guide/Policy/Designated/Offline/FundProject) + InterestLocgov 수술(add/remove AJAX만) | receipt-official-print + fragments×5 |
| point | 3 | PointController 수술(배치만) | expire-batch + fragments×4 |
| gift | 5 (list·detail·wishlist·my-reviews·my-qna) | GiftController 수술(판매자+토글/좋아요/재입고 AJAX만) | events·event-detail·inquiries·my·register·edit·seller-dashboard + fragments×5 |
| order | 10 | 4 삭제(Cart/Checkout/Order/CouponMy) + Claim 수술(운영자 큐만) | claims/queue + fragments×5 |

**SPA가 직접 호출하는 비-`/api` AJAX(유지 필수):** donation `POST /interest-locgovs`·`/interest-locgovs/{code}/delete`(ProfileView/ListSelectView/InterestLocgovsView), gift `POST /wishlist/{itemId}/toggle`(GiftList/Detail/Wishlist). 나머지는 전부 `/api/*` 트윈이 대체.

### 매핑표 오판 정정 (내용 대조로 발견 — [[verify-screen-by-content-not-route]])

- **gift/inquiries** → 삭제(#6)로 찍혔으나 실제 `GET /my/inquiries`는 `currentSellerId`/`inquiriesForSeller`를 쓰는 **판매자 문의답변** 화면. GiftQnaView(구매자 `/api/my/qna`)와 별개 → **유지-범위밖**.
- **gift/events·event-detail** → 삭제로 찍혔으나 FeaturedController의 **지역이벤트**(featured). SPA `/events`는 customer/EventListView가 **admin `/api/events`**(운영 이벤트)를 부르는 다른 기능 → **유지-미커버**.
- **donation/offline** → 확정삭제목록(20)엔 누락됐으나 OfflineDonationView(`/api/my/donations/offline`)가 대체 → offline.html·OfflineDonationController **삭제 완료**(실제 21개 삭제).

## 2차 진행 현황 (2026-09-17)

**최소범위·완주 우선으로 donation부터.** "지금 의존성을 0으로 만들 수 있는 유일한 서비스는 donation"(남은 라이브 1개가 대민/범위내)이라 gift(판매자 5개가 PL 대기라 막힘)보다 앞세웠다.

- ✅ **donation — Thymeleaf 의존성 완전 제거 완료.** `receipt-official-print`(공식 기부금영수증 조회)를 storefront `OfficialReceiptPrintView.vue`(`/print/official-receipt/:cntrSn`, bare)로 이관. 백엔드는 `GET /api/my/receipts/official/{cntrSn}`(DonationMyApiController) 신설 + `OfficialReceiptController`의 뷰 메서드 제거(직인 합성 PDF `/pdf`·인쇄이력 `/print-log`는 Thymeleaf가 아니라 유지). `MyDonationsView`의 "영수증출력" 링크를 새 프린트 라우트로 교체. 템플릿(fragments 포함)·`spring-boot-starter-thymeleaf` 의존성 제거. compileJava·vite build 모두 통과. **donation 뷰 렌더링 0건 확인.**
- ✅ **member — 휴면해제를 AS-IS와 동일하게 재구현(의존성은 유지).** 1차 시도(`/reactivate` 페이지+아이디/비번 재입력 폼+로그인 링크)는 AS-IS와 달라 사용자 지적으로 폐기하고, AS-IS(`op.saleson.js:1169`) 흐름 그대로 재구현: 로그인 시 휴면회원이면 비밀번호 본인확인 후 서버가 `SLEEP_USER` 반환 → LoginView가 그 자리에서 `confirm("휴면해제 하시겠습니까?")` → `POST /api/auth/recovery`(세션 대기 userId, 자격증명 재입력 없음) → 재로그인 유도. `MemberService.checkCredentials`가 휴면을 하드차단하던 것을 제거(통과시킨 뒤 로그인단에서 분기), `reactivate(loginId,pw)`→`reactivateById(userId)`. `DormancyController`·`reactivate.html`·`ReactivateView`·`/reactivate` 라우트·링크 전부 제거. **login·external-login×3이 남아 member의 `thymeleaf` 의존성은 그대로**(외부인증 보류군).
- ⏸ **나머지 전부 대기**(위 분류대로): point expire-batch·order claims/queue(운영자), gift 판매자 포털 5 + events×2(PL/지역이벤트), member login·external-login(외부연계 보류).

## 2차(살아있는 기능 Vue3 전환) 대상 — Thymeleaf 의존성 제거의 전제

1차로 `thymeleaf` 의존성을 제거할 수 있는 서비스는 **없다**(전부 살아있는 기능이 남음). 2차는 두 부류:
- **대민 미커버(SPA 만들면 즉시 제거 가능):** member `reactivate`(SFR-002 휴면해제)·`external-login`×3(원패스/카카오/금융/간편, 외부연계 보류군), gift `events`/`event-detail`(지역이벤트), donation `receipt-official-print`(공식영수증 프린트·서버PDF).
- **판매자/운영(PL 그룹 결정 대기, 범위밖):** gift `my`/`register`/`edit`/`seller-dashboard`/`inquiries`, order `claims/queue`, point `expire-batch`(운영자 소멸배치).

## 결론 / 다음 단계

- **"5개 서비스 Thymeleaf 완전 소거"는 이 단계로 달성되지 않는다** — 프린트·판매자·운영·미커버 페이지가 남으므로, 대부분 서비스는 `thymeleaf` 의존성을 유지한다. 이 단계의 목표는 **대민 중복 폐기 + SPA 정본화**.
- **삭제 순서**: ① 확인필요 9건을 화면 대조로 확정 → ② 삭제 확정분(≈43+α) Thymeleaf 페이지·뷰@Controller·fragment 제거 → ③ storefront에서 각 화면 정상 확인 → ④ 서비스별 잔여 Thymeleaf 0인 곳만 의존성 제거.
- **확인필요 9건은 삭제 금지** — SPA가 완전 대체하는지 페이지별 대조 후에만.
