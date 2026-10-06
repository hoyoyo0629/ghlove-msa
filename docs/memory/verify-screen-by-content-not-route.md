---
name: verify-screen-by-content-not-route
description: AS-IS 화면의 MSA 대응물을 찾을 때 라우트·파일명이 아니라 화면 제목과 본문 마크업으로 대조할 것 - 경로명이 다른 화면이 있다 (2026-09-09 오판 사례)
metadata:
  type: feedback
---

AS-IS 화면이 MSA에 있는지 판단할 때 **라우트 목록이나 템플릿 파일명으로 결론내지 말고, 화면 제목과 본문 마크업으로 대조**한다. MSA에는 AS-IS와 경로명을 다르게 쓴 화면이 있다.

**실제 오판 사례 (2026-09-09, donation 분석):** AS-IS `donation/guide3.html`(연말정산 세액공제 안내)이 MSA에 없다고 보고했다. 근거는 `GuideController`의 라우트가 `/guide1 /guide2 /guide5 /guide6`뿐이고 `templates/guide/`에 guide3.html이 없다는 것이었다. **틀렸다** - `donation/honor.html` + `HonorBenefitController.guide()`가 guide3 재현본이었고, 화면 제목("안내사항 > 연말정산 세액공제 안내")과 AS-IS의 `give-table` PC/모바일 2벌 표까지 그대로 있었다. **컨트롤러 주석에 "AS-IS donation/guide3.html"이라고 명시까지 돼 있었는데 못 봤다.** storefront SPA도 같은 내용을 `TaxCreditGuideView.vue`(라우트 `/honor`)로 갖고 있었다.

그 결과 없는 화면을 새로 만들고 5개 모듈의 GNB 링크까지 바꿨다가 전부 되돌렸다(중복이었고 GNB 링크는 원래가 맞았다).

**Why:** 라우트·파일명은 MSA가 자체 판단으로 바꾼 부분이라 AS-IS와 1:1이 아니다. 이름만 보고 판단하면 (1) 있는 걸 없다고 오판해 중복 구현하고, (2) 그 오판을 근거로 멀쩡한 링크를 망가뜨린다.

**How to apply:** "AS-IS X가 MSA에 없다"를 보고하기 전에 최소 두 가지를 확인한다 - (1) AS-IS 화면의 **제목 문구**로 MSA 템플릿 전체를 grep, (2) 그 기능의 **특징적 클래스/표 구조**(여기서는 `give-table`)로 grep. 컨트롤러/템플릿 주석의 "AS-IS ...html" 표기도 같이 훑는다. 조치 규모를 추정할 때도 "기존 컴포넌트 재사용이라 작다"는 식의 낙관을 검증 없이 쓰지 않는다 - 같은 분석에서 지도 선택(map-select) 작업량도 과소평가했다. 분석 기준 자체는 [[check-as-is-source-when-analyzing]] 참고.

**두 번째 사례 (2026-09-10, donation 기부완료 모달):** 화면 대응표를 만들 때 축소 판정을 `popup-success.html`·`giro-success.html` 같은 **별도 파일 단위로만** 훑어서, AS-IS `donation-main.html` **안에 인라인된** 기부완료 모달(`#donation-c` — 기부 요약표 + "답례품 몰 바로가기")을 통째로 놓쳤다. 사용자가 기능 테스트 중 "기부하면 기부내역 화면으로만 가고 답례품 화면으로 안 넘어간다"고 지적해서 드러났다. 즉 **AS-IS 화면 하나가 파일 하나가 아니다** - 모달·팝업·탭 같은 인라인 영역이 별개 화면 역할을 한다. 대응표를 만들 때 파일 목록을 훑는 것만으로는 부족하고, AS-IS 화면 안의 모달/오버레이 블록(`black-bg`, `overlayer`, `popup` 계열)도 같이 세야 한다. 덧붙여 **링크되지 않은 CSS가 그 단서**였다 - AS-IS가 링크하는 `joind-agf.css`가 MSA 모듈에 파일로만 있고 아무 데서도 링크되지 않았는데, 그게 정확히 이 모달용 CSS였다.
