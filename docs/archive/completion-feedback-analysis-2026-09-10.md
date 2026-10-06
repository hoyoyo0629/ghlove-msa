# 완료 알림(피드백) 재현 상태 상세분석 — 2026-09-10

분석 기준: **기능은 AS-IS 소스 + 제안요청서(RFP) + ISP 요약 3축 종합**, **레이아웃은 AS-IS 기준**.

참조 원본: AS-IS `C:\workspace\ghlove\ghlove-frontend` (백업본 `*_bak` / `*_20xxxxxx` / `old/` 제외한 **라이브 화면만**)

> 화면 대응은 라우트·파일명이 아니라 **문구·마크업 기준**으로 대조했다.

---

## 1. 한 줄 요약

**MSA는 "물어보는 창"은 거의 다 재현했는데 "됐다고 알려주는 창"만 통째로 빠졌다.**

| | 건수 |
|---|---|
| MSA `confirm(...)` — 실행 전 확인 | **100** |
| MSA 완료 alert(`되었습니다`/`완료`) | **2** (둘 다 `수정되었습니다` — 2026-09-09에 추가한 회원정보 수정 하나) |

MSA 컨트롤러는 실패 경로에만 `?errorMessage=`를 실어 보내고, **성공 경로는 예외 없이 맨 `redirect:`**다. order·gift 모듈에는 성공을 알리는 리다이렉트 파라미터가 **0건**이다.

---

## 2. AS-IS의 완료 피드백 방식 3가지

1. **`$s.alert("...되었습니다.")`** — 대부분의 행위 단위 완료 통지
2. **완료 모달** — 회원가입(`users/join.html` `.overlayer.join-complete`), 기부완료(`donation/donation-main.html` `#donation-c`)
3. **완료 화면** — 주문완료(`order/step2.html`)

MSA는 3번(주문완료 `order-complete.html`)과 2번 중 기부완료 모달(2026-09-10 구현)만 갖고 있다.

---

## 3. 갭 목록 (AS-IS에 있고 MSA에 없음)

### 3-1. member

| 행위 | AS-IS | 근거 | MSA 현재 |
|---|---|---|---|
| **회원가입 완료** | **가입완료 축하 모달**(`.overlayer.join-complete` — "고향사랑e음 회원가입 완료" + 아이콘 + 수신동의 안내 + 확인) | `users/join.html:57-79` | `redirect:/` — **아무 알림 없이 메인으로**. 외부인증(SNS) 가입만 `signup.html:340`에 모달이 있다 |
| 비밀번호 변경 | alert "비밀번호가 변경되었습니다." | `users/modify.html:709`, `users/find-idpw.html:539` | 배너로 대체(§4 결함 참고) |
| 탈퇴 | alert "탈퇴처리가 정상처리 되었습니다." | `users/secede.html:322` | 배너 |
| 휴면 해제 | alert "고객님의 계정이 휴면해제 되었습니다.\n원활한 서비스 이용을 위하여 재 로그인 해주십시오." | `users/old/sleep-user.html:105` (라이브 대응 화면 없음) | 배너 |
| 개인정보(이름·생년월일) 수정 | alert "개인정보가 수정되었습니다. 저장하시려면 화면 아랫쪽 확인버튼을 누르세요." | `users/modify.html:1554` | `redirect:/profile` — 무알림 |

> 배송지 등록·수정·삭제는 **AS-IS도 성공 alert이 없다**(`mypage/deliveryInfo.html`은 실패 alert만). MSA의 조용한 리다이렉트가 맞다 — 갭 아님.

### 3-2. order

| 행위 | AS-IS | 근거 | MSA 현재 |
|---|---|---|---|
| 장바구니 담기 | alert "장바구니에 담았습니다." + 장바구니 개수 갱신, **상세화면에 머문다** | `items/details-main.html:1851` | `redirect:/cart` — 무알림 + **장바구니 화면으로 이동**(동선 자체가 다름) |
| 장바구니 삭제 | alert "해당 답례품이 장바구니에서 삭제되었습니다." | `cart/index.html:552` | 무알림 |
| 주문취소 신청 | alert "취소신청 되었습니다." | `components/ui/modal-order_cancle.vue:360` | 무알림 |
| 반품신청 | alert "반품신청 되었습니다." | `components/ui/modal-return.vue:301` | 무알림 |
| 교환신청 | alert "교환신청 되었습니다." | `components/ui/modal-exchange.vue:286` | 무알림 |
| **구매확정** | alert "구매확정이 완료되었습니다." | `mypage/orderDetail.html:976`, `orderList.html:1452` | 무알림 |
| 배송완료 처리 | alert "배송완료 처리되었습니다." | `mypage/orderDetail.html:946`, `orderList.html:1418` | 무알림 |

### 3-3. gift

| 행위 | AS-IS | 근거 | MSA 현재 |
|---|---|---|---|
| 관심답례품 추가 | alert "관심답례품에 추가 되었습니다." | `items/details-main.html:1949` | 하트 아이콘 상태만 바뀜 |
| 관심답례품 삭제 | alert "해당 답례품이 관심답례품에서 삭제되었습니다." | `items/details-main.html:1925` | 무알림 |
| 답례품 Q&A 등록 | alert "답례품Q&A가 등록되었습니다." | `items/details-main.html:2612` | `redirect:/gifts/{id}` — 무알림 |
| 답례품 후기 등록 | (라이브 화면은 모달 닫기로 처리) | — | 무알림 |
| 후기 삭제 | alert "삭제되었습니다." | `mypage/review.html:498` | SPA `GiftReviewsView.vue`가 **confirm 문구는 AS-IS 그대로 재현하고 완료 alert만 빠뜨렸다** |
| 답례품 문의 삭제 | alert "삭제되었습니다." | `mypage/inquiryItem.html:358` | 무알림 |
| 1:1 문의 등록 | alert "등록되었습니다." | `mypage/inquiry.html:385` | 무알림 |

### 3-4. donation

| 행위 | AS-IS | 근거 | MSA 현재 |
|---|---|---|---|
| 기부 완료 | 기부완료 모달 `#donation-c` | `donation/donation-main.html:105` | ✅ **2026-09-10 구현 완료** |
| 수납결과 확인 | alert "수납결과가 확인되었습니다." | `mypage/cntrList.html:461/513/561` | 위 모달로 대체 — 갭 아님 |
| 관심 지자체 등록 | alert "관심 지자체 등록 되었습니다." | `donation/list-select.html:803`, `map-select.html:1418`, `goods/searchGoods-main.html:642` | `redirect:/interest-locgovs` — 무알림 |

### 3-5. 고객센터 (admin 호스팅)

| 행위 | AS-IS | 근거 | MSA 현재 |
|---|---|---|---|
| Q&A 등록 | alert "등록되었습니다." | `qna/qna-form.html:255` | `redirect:/qna` — 무알림 |
| Q&A 수정 | alert "수정되었습니다." | `qna/qna-edit.html:309` | 무알림 |
| Q&A 삭제 | alert "삭제되었습니다." | `qna/qna-edit.html:344`, `qna-detail.html:311` | 무알림 |
| URL 복사 | alert "URL이 복사되었습니다." | `notice/detail.html:177`, `qna/qna-detail.html:289`, `data-board/detail.html:192` | **공유/URL복사 버튼 자체가 없음** |

---

## 4. 분석 중 발견한 결함 2건 (알림 로직 자체 버그)

| # | 위치 | 내용 |
|---|---|---|
| A | `member/login.html:41` | `?signup` 파라미터를 보내는 곳이 **한 군데도 없다**(`signup()`은 `redirect:/`). "회원가입이 완료되었습니다" 배너는 **절대 안 뜨는 죽은 코드**다 |
| B | `member/login.html:42` | 비밀번호 변경 성공 안내가 `class="error"`로 렌더된다 — 성공인데 **빨간 오류 스타일**로 나온다 |

---

## 5. RFP·ISP 대조

RFP의 "알림"은 **외부 발송 채널**(국민비서, SMS, 카카오 알림톡 추가 — p.94 멀티채널 알림) 이야기이지 화면 피드백이 아니다. 이 분석의 갭은 **AS-IS 재현 누락**이 근거이고 RFP·ISP에 직접 대응 조항은 없다. 발송 채널 쪽은 `admin`의 `Ums`/`UmsSendLog`/`NotificationClient`로 별도 존재하며 이 건과 성격이 다르다.

---

## 6. 우선순위 제안

| 순위 | 대상 | 이유 |
|---|---|---|
| 1 | **§4 결함 2건** | 코드 몇 줄. 죽은 배너 + 성공을 오류색으로 표시 |
| 2 | **회원가입 완료 모달** | AS-IS가 모달까지 갖춘 유일한 회원 플로우인데 지금은 알림이 0이다. 가입 직후 이탈에 직결 |
| 3 | **order 7종**(장바구니 담기·삭제, 주문취소, 반품, 교환, 구매확정, 배송완료) | 돈·물건이 오가는 행위인데 눌러도 아무 반응이 없다. 사용자가 가장 크게 체감 |
| 4 | gift 7종, donation 관심지자체, 고객센터 Q&A | 같은 유형, 건수만 많다 |
| 5 | URL 복사 | 버튼 자체를 새로 만들어야 해서 성격이 다름 |

### 착수 전 정할 것
- **표시 수단**: AS-IS는 브라우저 `alert`이 아니라 자체 모달(`$s.alert`)이다. MSA도 공용 알림 프래그먼트를 하나 만들어 6개 모듈이 공유할지, 당장은 `alert()`로 맞출지 결정이 필요하다.
- **전달 방식**: MSA는 POST→redirect 구조라 알림을 띄우려면 성공 파라미터(예: `?done=cancelled`)를 실어 보내고 화면에서 읽어야 한다. 기부완료 모달(`?completed=`)에서 쓴 방식을 그대로 확장하면 된다.
- **장바구니 담기 동선**: AS-IS는 상세화면에 머물고 MSA는 장바구니로 이동한다. 알림만 붙일지 동선까지 AS-IS로 맞출지 판단 필요.

---

## 7. 조치 내역 (2026-09-10) — §3·§4 전량 구현

### 7-1. 표시 수단: 공용 알림 프래그먼트 신설

AS-IS `$s.alert()`은 `components/layouts/alert.vue`의 `#op-alert`(부트스트랩 모달 + jQuery)에 얹혀 있다. 이 MSA에는 그 체인이 없고, AS-IS의 모달 CSS(bootstrap/`p_notitle`/`pop_txt` 계열)를 6개 모듈에 들여오면 order의 `mypage-order.css` 때처럼 기존 레이아웃과 충돌할 위험이 있다.

그래서 **자기 CSS를 안에 들고 다니는 독립 프래그먼트**로 만들었다 — `fragments/alert.html`의 `gh-alert`, 클래스 접두사 `gh-alert-`. 어느 모듈에 붙여도 다른 스타일시트와 겹치지 않는다. 6개 모듈(member/donation/point/gift/order/admin)에 동일 파일로 배포했다.

전달 방식은 **실패 경로의 `?errorMessage=`와 같은 관행**을 따랐다 — 성공 후 `?done=<문구>`로 리다이렉트하면 프래그먼트가 `th:text`로 띄운다. 문구는 서버가 정해 모듈마다 JS 사전을 복사하지 않는다. 비동기 처리(찜 토글, URL 복사)는 `window.ghAlert(문구)`를 부른다. 확인을 누르면 `done` 파라미터를 URL에서 지워 새로고침·뒤로가기에 다시 뜨지 않는다(기부완료 모달의 `completed` 처리와 동일).

리다이렉트 조립은 모듈마다 `Done.redirect(path, message)` 한 곳으로 모았다(member는 기존 관행대로 `AuthController.doneRedirect`).

### 7-2. 행위별 조치

| 서비스 | 행위 | 조치 |
|---|---|---|
| member | **회원가입 완료** | AS-IS `join.html` 축하 모달을 **일반 가입 경로에 연결**. 그동안 외부인증 가입에만 있었다. `signup()`이 `redirect:/signup?completed=1`로 돌아와 같은 모달을 띄운다(확인 → 메인). 제목은 AS-IS 문구 "고향사랑e음 회원가입 완료", 외부인증일 때만 인증수단을 덧붙인다 |
| member | 비밀번호 변경 / 탈퇴 / 휴면해제 | 배너 → AS-IS 문구 그대로 알림 모달 |
| member | 개인정보 수정 | 무알림 → "개인정보가 수정되었습니다." |
| member | 회원정보 수정 | 기존 `?updated=1` + 브라우저 `alert()` → 공용 모달로 통일 |
| member | 관심 지자체 등록(프로필 AJAX) | `ghAlert('관심 지자체 등록 되었습니다.')` |
| order | 장바구니 담기 / 삭제 | AS-IS 문구 2종. 담기는 동선까지 AS-IS와 일치(§7-5) |
| order | 주문취소 | "취소신청 되었습니다." |
| order | 반품 / 교환 / 취소 신청 | `claimType`별로 AS-IS 모달 3종 문구를 분기 |
| order | 구매확정 | "구매확정이 완료되었습니다." |
| order | 배송상태 변경 | `DELIVERED` 전이일 때만 AS-IS "배송완료 처리되었습니다.", 그 외는 일반 문구 |
| gift | 관심답례품 추가 / 해제 | 폼 경로는 `?done=`, 하트 토글(AJAX)은 `ghAlert` |
| gift | 답례품 Q&A 등록 | "답례품Q&A가 등록되었습니다." |
| gift | 판매자 문의답변 | 업무Web 화면이라 AS-IS 문구가 없어 "답변이 등록되었습니다." |
| gift(SPA) | 후기 삭제 / 답례품 Q&A 삭제 | AS-IS "삭제되었습니다." — confirm 문구는 이미 재현돼 있었고 완료 알림만 빠져 있었다 |
| donation | 관심 지자체 등록(list-select) | 자체 문구를 AS-IS 문구로 교체하고 공용 모달로 전환 |
| admin | 고객센터 Q&A 등록 | "등록되었습니다." |
| admin | **URL 복사** | 자료실·Q&A 상세에 AS-IS `share-con` 버튼 신설 + `ghCopyUrl()`(Clipboard API, 실패 시 AS-IS의 textarea+execCommand로 폴백) |

### 7-3. §4 결함 2건

- **죽은 배너 제거**: `login.html`의 "회원가입이 완료되었습니다" 배너는 `?signup`을 보내는 곳이 한 군데도 없어 한 번도 뜬 적이 없었다. 가입 완료는 AS-IS대로 축하 모달이 맡으므로 배너를 걷어냈다.
- **성공을 오류색으로 표시**: 비밀번호 변경 안내가 `class="error"`(빨강)였다. 알림 모달로 옮기면서 해소.

### 7-4. 갭이 아니라고 판정해 손대지 않은 것

- **배송지 등록·수정·삭제** — AS-IS `mypage/deliveryInfo.html`도 실패 alert만 있고 성공 알림이 없다
- **장바구니 수량 변경** — AS-IS도 목록만 다시 그린다
- **관심 지자체 삭제** — AS-IS는 등록만 알린다
- **송장 등록·주문 배송지 변경** — AS-IS 마이페이지에 없는 운영자용 동작
- **답례품 후기 등록** — AS-IS는 모달을 닫고 목록을 다시 그릴 뿐 완료 alert이 없다

### 7-5. 장바구니 담기 동선을 AS-IS와 일치시킴

AS-IS `items/details-main.html`의 "장바구니"는 **답례품 상세에 머문 채** 비동기로 담고 alert만 띄운다(`addToCart` → `$s.api.addToCart` 콜백에서 `$s.alert("장바구니에 담았습니다.")`). 반면 이 MSA는 gift(8084) 화면에서 order(8085)로 폼을 넘기는 구조라 장바구니 화면으로 넘어갔다.

처음에는 "돌아갈 URL을 폼에 실어 보내려면 오픈 리다이렉트 방지 검증을 order에 새로 만들어야 한다"는 이유로 동선을 그대로 뒀는데, **되돌아갈 필요 자체를 없애면 그 문제가 사라진다** - AS-IS처럼 비동기로 담고 화면을 떠나지 않으면 된다.

- **order**: SPA가 이미 쓰던 `POST /api/cart/items`를 재사용한다. 브라우저 입장에서 8084 → 8085 교차 오리진이라 **그 엔드포인트에만 `@CrossOrigin`으로 호출 오리진을 열었다**(donation의 관심지자체 API가 member 화면 8081에서 불릴 때 쓰는 방식과 동일). `GH_AUTH` 쿠키를 실어야 해서 `allowCredentials = "true"`가 필요한데, 쿠키 도메인이 `localhost`라 포트가 달라도 공유된다.
- **gift**: 기존 폼을 **지우지 않고 submit을 가로챈다**(`e.preventDefault()` + fetch). JS가 죽으면 예전처럼 폼이 전송돼 장바구니 화면으로 넘어가고, 그쪽에도 같은 알림이 붙어 있어 동작이 끊기지 않는다. 401이면 로그인 화면으로 보내고, 실패 메시지는 서버가 준 문구를 그대로 띄운다.
- 이 스크립트 블록이 폼보다 **위에** 있어 즉시 실행하면 폼을 못 찾는다 - `DOMContentLoaded`에서 바인딩한다.

**남은 차이 하나**: AS-IS는 담은 뒤 `getCartInfo()`로 헤더의 장바구니 개수 배지를 갱신한다. 이 MSA의 헤더에는 개수 배지가 없고 링크만 있어(`fragments/header.html`) 갱신할 대상이 없다 - 배지를 새로 만드는 건 이 건과 별개다.

검증: CORS 프리플라이트 `Access-Control-Allow-Origin: http://localhost:8084` / `Allow-Credentials: true` 확인, 교차 오리진 POST 204, **헤드리스 브라우저로 실제 버튼을 클릭해 화면 전환 없이 알림만 뜨는 것까지 스크린샷으로 확인**했다.

---

## 8. 작업 중 드러난 선재 결함 2건 (알림과 무관, 즉시 수정)

알림 프래그먼트를 붙이고 화면을 열어보다 **원래부터 깨져 있던 화면 두 부류**가 드러났다. 둘 다 HTTP 200을 반환하면서 본문이 중간에 잘려 나가고 있어(응답이 이미 커밋된 뒤 예외가 터진다) 눈에 띄지 않았다.

### 8-1. admin 고객센터 사용자 화면 10개가 푸터에서 렌더링 중단

`fragments/footer.html`이 `admin-footer` 프래그먼트만 정의하는데, **고객센터 사용자 화면 10개**(`content/notice-*`, `data-board/*`, `events/*`, `faq/list`, `qna/board*`, `qna/list`)는 `site-header`와 짝을 이루는 **`site-footer`를 참조**한다. 예전에 "운영자 콘솔에 소비자용 메가푸터가 뜨는 오류"를 고치면서 소비자 푸터를 걷어냈는데, 그때 이 10개 화면이 함께 검토되지 않았다(당시 주석은 두 화면만 영향받는다고 적고 있다).

→ `footer.html`에 다른 서비스(gift/order/donation)와 같은 내용의 **`site-footer` 프래그먼트를 되살렸다.** `admin-footer`는 그대로 둔다. 수정 후 `/qna/board` 33KB → 47KB, `/data-board` → 42KB로 늘고 `</body></html>`까지 정상 출력된다.

### 8-2. 후기가 달린 답례품 상세가 별점 계산에서 중단

`gift/detail.html`의 별점이 `${i <= #numbers.formatDecimal(averageScore, 1, 0)}`인데, `formatDecimal`은 **문자열**을 돌려주므로 정수 `i`와 비교하는 순간 `ClassCastException`이 난다. `th:if="${!#lists.isEmpty(reviews)}"` 안이라 **후기가 하나라도 달린 답례품에서만** 터져 오래 드러나지 않았다.

→ 반올림한 숫자끼리 비교하도록 고쳤다(`averageScore != null and i <= T(java.lang.Math).round(averageScore)`). 수정 후 상세 화면이 167KB로 끝까지 렌더된다. 같은 패턴을 gift·order 전체에서 재검색해 다른 사례가 없음을 확인했다.

---

## 9. 검증 (실측)

- **6개 모듈 빌드** 전부 exit 0, 5개 서비스(8081/8082/8084/8085/8086) 재기동 후 정상 응답
- **프래그먼트 렌더**: 로그인·회원정보수정·답례품몰·장바구니·고객센터 Q&A에서 `gh-alert` 1건씩 확인
- **회원가입 → 가입완료 모달**: `POST /signup` → `302 /signup?completed=1` → 모달에 "고향사랑e음 회원가입 완료" + 발급 ID 표시
- **탈퇴**: `POST /withdraw` → `302 /login?done=…` → 로그인 화면에 "탈퇴처리가 정상처리 되었습니다."
- **장바구니 담기·삭제**, **찜 추가**, **답례품 Q&A 등록**, **반품신청**, **고객센터 Q&A 등록** 모두 `?done=` 리다이렉트와 모달 렌더까지 확인
- **배송지 저장**은 알림 없이 리다이렉트(의도대로)
- **스크린샷**으로 모달 외형이 AS-IS `#op-alert`(가운데 흰 박스 + 문구 + 파란 확인 버튼)과 같은지 확인
- 선재 결함 2건은 수정 전후 응답 크기와 종료 태그로 확인

> 검증용 데이터는 전량 정리했다 — 장바구니·찜은 되돌렸고, 회원 `alerttest01`은 앱의 탈퇴 경로로 `WITHDRAWN` 처리, 답례품 Q&A 1건은 삭제, 반품신청 클레임은 **앱의 거절 경로**(`POST /claims/5/reject`)로 정리해 주문이 `CLAIM_REQUESTED` → `CONFIRMED`로 복원된 것까지 확인했다.

---

## 10. 후속 조치 (2026-09-10)

### 10-1. 답례품 상세 "기부하기" 버튼 405 (결함, 수정)

gift `detail.html`의 기부하기가 `http://localhost:8082/donate?locgovCode=...`를 가리켰는데, donation의 `/donate`는 **기부 제출용 POST 전용**이라 눌렀을 때 **405 Method Not Allowed**가 났다.

AS-IS 대조: `items/details-main.html`의 `donationInit()` → `goGivePage()` → `$s.donation.goDonationPage("?locgovCode=")` → **`/donation/donation.html?locgovCode=...`**(기부하기 화면). 그 화면의 이 MSA 대응은 donation의 **`GET /`(donate.html)**이다.

→ 링크를 `http://localhost:8082/?locgovCode=...`로 고쳤다. 로그인 필요 시 그쪽 컨트롤러가 target을 실어 로그인으로 보내므로 AS-IS의 `Saleson.init({loginPage:true})`와 동작이 같다.

**함께 드러난 미구현** → **§11에서 구현했다**: AS-IS는 기부하기로 넘어가기 전에 `POST /api/ngdonation/getLocGovLmtt`로 **지자체 기부제한**을 조회해, 제한 기간이면 "선택하신 지자체는 …으로 …부터 …까지 기부가 불가능합니다."를 띄우고 이동을 막는다. 이 MSA에는 `LocgovLmtt` 엔티티·리포지토리가 있었으나 admin 관리 화면에서만 쓰이고 사용자 기부 흐름에는 검사가 없었다.

### 10-2. 헤더 장바구니 개수 배지 — 만들었다가 **범위 밖으로 판정해 철회**

사용자 요청으로 배지를 만들었다가, **전환사업 범위 기준**(AS-IS 로직 + RFP/ISP 신규기능)에 비추어 **전량 되돌렸다.**

- **AS-IS에 없다**: 라이브 레이아웃(`header_ali.vue` / `header_g.vue` / `footer_ali.vue`)에 배지 마크업이 없다. `cartQuantity` / `displayCartQuantites` 계산식만 남아 있고, 그걸 그리는 `<span class="badge badge_cart">` / `<span class="num">`은 **더 이상 쓰이지 않는 구버전 `header.vue` / `footer.vue`에만** 있다. `items/details-main.html`의 `getCartInfo()`도 본문이 통째로 주석 처리된 죽은 호출이다.
- **RFP·ISP에도 없다**: `requirements.md`에서 "장바구니"는 SFR-006의 "주문 생성 및 상태관리(장바구니, 결제, 주문상태 관리)" 한 줄뿐이고 개수 표시 요구는 없다. ISP 요약에는 "장바구니"·"배지" 언급 자체가 없다.

되돌린 것: 5개 모듈 헤더의 배지 마크업·스타일·`ghRefreshCartCount()`, order의 `GET /api/cart/count`·`CartService.countOf`·`CartItemRepository.countByUserId`, gift 상세의 갱신 호출. 되돌린 뒤 헤더 diff에 남은 것은 이전 세션의 GNB 링크 수정뿐임을 확인했다.

> **발단은 오독이었다.** §7-5에 "AS-IS는 담은 뒤 `getCartInfo()`로 헤더 배지를 갱신한다"고 적었는데, 구버전 레이아웃의 잔재를 라이브 기능으로 읽은 것이다. 화면 대조는 파일이 존재하는지가 아니라 **라이브 화면이 실제로 그 컴포넌트를 쓰는지**까지 봐야 한다(`items/details-main.html`의 `httpVueLoader` 등록 목록이 그 근거였다).

---

## 11. 지자체 기부제한 조회 도입 (2026-09-10, AS-IS 재현)

§10-1에서 "미조치"로 남겼던 건을 구현했다.

**AS-IS 동작**: 기부 진입점마다 `POST /api/ngdonation/getLocGovLmtt`로 제한을 먼저 조회하고, 제한 기간이면 안내만 띄우고 진행을 막는다. 호출처는 `donation-main.html`(납부 제출 직전), `items/details-main.html`(기부하기 클릭), `list-select`·`map-select`·`ngdonation`·`quick-donation`이다. 조회 SQL(`ngdonation-mapper.getCntrLmtt`)은 `G_CNTR_LMTT`에서 `LMTT_BGN_DE <= CURRENT_DATE <= LMTT_END_DE`인 행을 찾아 `DATE_FORMAT(...,'%Y.%m.%d')`로 내려준다. **AS-IS도 서버 저장 시점에는 검사하지 않는다** — 진입점 게이트 방식이라 이 MSA도 같게 뒀다.

**구현**
- `LocgovLmttRepository`에 오늘이 기간 안에 드는 행을 찾는 파생 쿼리 추가. 날짜가 `yyyyMMdd` 문자열이라 사전식 비교가 곧 날짜 비교다.
- `LocgovAdminService.activeLmttOf(locgovCode)` — 그 리포지토리를 이미 갖고 있던 서비스에 붙였다.
- `GET /api/locgov-lmtt?locgovCode=` (donation) — 제한이 있으면 `{data:{lmttBgnDe, lmttEndDe, violtResnCn}}`, 없으면 `{}`(AS-IS도 data가 없으면 통과시킨다). 날짜는 AS-IS와 같은 `yyyy.MM.dd`로 포맷한다. 답례품 상세가 gift(8084)에 있어 그 오리진을 `@CrossOrigin`으로 열었다.
- **gift 답례품 상세**: 기부하기를 링크에서 클릭 핸들러로 바꿔, 조회 통과 시에만 `http://localhost:8082/?locgovCode=…`로 이동한다.
- **donation 기부하기 화면**: 납부 제출 직전에 조회한다. 비동기라 제출을 한 번 멈췄다가 통과했을 때만 다시 제출하고(`requestSubmit`), 지자체를 바꾸면 재검사하도록 플래그를 리셋한다.
- 안내 문구는 AS-IS 그대로이고, **인천 영종구(28155)·제물포구(28125)·검단구(28290)의 시각 단위 문구 분기까지 재현**했다.
- 조회 자체가 실패하면 AS-IS(`commError`)와 같이 이동시키지 않는다.

**검증(실측)**: 제한 없음 → `{}`. `g_cntr_lmtt`에 오늘 포함 기간을 넣고 → `{"data":{"lmttBgnDe":"2026.09.10","lmttEndDe":"2026.09.13",…}}`. 헤드리스 브라우저로 답례품 상세의 기부하기를 클릭하니 **URL이 그대로이고**(이동 차단) AS-IS 문구가 그대로 떴다. 제한 삭제 후 다시 클릭하니 `http://localhost:8082/?locgovCode=11230`으로 이동했다. 기부하기 화면의 납부 제출도 제한 중에는 막히고(문구 동일, URL 유지) 해제 후에는 `/my?donated=success`까지 정상 진행했다. 검증용 제한 행과 기부 1건(앱의 취소 경로로 `CANCELLED`)은 정리했고, 포인트 원장에 검증 흔적이 남지 않은 것까지 확인했다.
