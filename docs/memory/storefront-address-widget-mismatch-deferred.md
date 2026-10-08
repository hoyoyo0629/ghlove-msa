---
name: storefront-address-widget-mismatch-deferred
description: "회원가입·마이페이지 개인정보수정 화면의 주소검색이 AS-IS(행안부 juso.go.kr)와 다른 위젯(Daum Postcode)을 쓰고 있음 - 외부연계 개발 가능해질 때까지 조치 보류(2026-10-08 사용자 지시)"
metadata:
  node_type: memory
  type: project
---

**발견 경위**: admin 일반회원관리(4101) 상세화면에서 주소 표시형식을 AS-IS와 맞추다가, 실데이터의
시/도명이 축약형("경기"/"서울")인 걸 발견해 회원가입 때 주소를 어떻게 입력받는지 역추적했다.

**AS-IS는 화면마다 주소검색 위젯이 다르다(통일돼 있지 않다)**:
- **회원가입**(`ghlove-frontend/users/join.html`) · **마이페이지 개인정보수정**(`users/modify.html`) ·
  원패스가입(`onepass-join.html`) → **행정안전부 juso.go.kr 팝업**(`users/jusoPopup.html`,
  `business.juso.go.kr/addrlink/addrLinkApiJsonp.do` JSONP 호출). 최종 저장값은
  `roadAddrPart1 + roadAddrPart2`(행안부 API가 항상 전체 공식 시/도명 "서울특별시 강남구 ..."을
  담아 돌려준다 - 지번·법정동 보조정보는 `roadAddrPart2`에 괄호로 붙는다).
- **마이페이지 배송지관리**(`mypage/deliveryInfo.html`) · **주문결제 1단계**(`order/step1.html`) →
  **Daum Postcode**(`components/ui/daum/address.vue`의 `openDaumAddress()`), 저장값은
  `response.roadAddress`(Daum도 보통 전체명을 준다).

**TO-BE 현재 상태(2026-10-08 실측)**:
- `storefront/src/views/SignupView.vue`, `storefront/src/views/mypage/ProfileView.vue` → 둘 다
  `daum.Postcode`를 쓴다(`loadDaumPostcode()` + `data.roadAddress || data.jibunAddress`). **AS-IS와
  다른 위젯**이다. 코드 주석에 "AS-IS 회원가입은 juso.go.kr 팝업이지만 이 프로젝트는 이미
  daum.Postcode로 통일해 다른 화면과 동작을 맞춘다"고 적혀 있었는데, **AS-IS를 확인하지 않고
  임의로 "통일"한 과거 결정**이었다([[no-invented-features-ask-first]] 위반 사례와 같은 유형).
- `storefront/src/views/mypage/DeliveryFormView.vue`, `storefront/src/views/order/CheckoutView.vue` →
  둘 다 `data.roadAddress || data.jibunAddress`로 이미 Daum을 쓴다. **이 둘은 AS-IS와 일치** -
  손댈 필요 없다.

**정리하면 회원가입·개인정보수정 두 화면만 위젯이 틀렸고, 배송지관리·주문결제는 이미 맞다.**

**"서울" vs "서울특별시" 표시차이의 진짜 원인일 가능성**: 행안부 juso.go.kr은 축약형을 주지 않는데
지금 두 화면은 그 API 자체를 안 쓰니, 이게 근본원인일 수 있다. 다만 현재 address input이
`readonly`라 위젯 없이는 입력 자체가 안 되므로, 실제 테스트데이터(`testuser01` 등)가 정말 위젯을
거쳐 들어간 값인지는 별도 확인 필요(과거 코드 상태에서 readonly 아니었을 때 수동입력했을 가능성도
있음 - 미확인).

**참고할 기존 자산**: admin 서비스에 이미 같은 행안부 API로 juso-popup을 포팅해 둔 선례가 있다
(`/admin/juso-popup`, `admin/src/main/resources/templates/juso/juso-popup.html` +
`content/modules/juso/addrlink.js` 등 정적자산, [[admin-member-area-port-progress]] 4401 지자체관리
작업 때 이식). storefront SPA에 같은 걸 포팅할 때 구조를 참고할 수 있다. 단, 행안부 API는
도메인별 승인키(confmKey)가 필요해서 storefront(회원 대상 운영 도메인)용 키 발급·외부연계 협의가
먼저 필요할 수 있다.

**사용자 결정(2026-10-08): 지금 당장 고치지 않는다.** "외부연계 개발이 가능해지면 그때 수정하자"
- 행안부 API 키/도메인 승인 등 외부연계가 먼저 정리돼야 착수 가능하다고 판단한 것으로 보임.
이 메모가 그 재개 시점의 체크리스트다: SignupView.vue·ProfileView.vue 두 파일만 juso.go.kr 팝업으로
교체, DeliveryFormView.vue·CheckoutView.vue는 손대지 않음.
