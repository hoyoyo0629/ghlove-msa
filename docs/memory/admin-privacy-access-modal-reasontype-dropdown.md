---
name: admin-privacy-access-modal-reasontype-dropdown
description: "개인정보 엑셀 다운로드 사유 모달에 분류코드(#reasonType) select 신규추가 - AS-IS는 공통JS가 열 때마다 DOM에 끼워넣어서 마크업만 보고 '없다'고 오판했던 것, 2026-10-08"
metadata:
  node_type: memory
  type: project
---

**발견 경위**: 오프라인담당자 목록(4601) "전체 목록 엑셀 다운로드" 버튼으로 사용자가 지적.
AS-IS `include/modal/privacy-access.jsp`(공통 모달) 마크업 자체에는 실제로 분류코드 select가
없다 - **예전 조사(코드주석에 남아있던 "REASON_TYPE은 실제로 비어있다" 결론)가 틀렸던 건 아니고
불완전했다.** 진짜 출처는 `op.shop.js`의 `Shop.downloadExcelOrder(url, queryString,
isPrivacyAccess)` 공통 함수 - 모달을 열기 **직전에** `#reasonType`이 없으면 전역 `reasonType`
객체(1~5, `기부금 모금 실적 확인`/`답례품 주문내역 확인`/`택배 송장 출력 및 배송확인`/
`답례품 정산 및 지급 확인`/`기타`)로 select를 만들어 `.modal-body` 바로 앞에 DOM으로 끼워
넣는다. 오프라인담당자 목록의 "전체 목록 엑셀 다운로드"는 `Shop.downloadExcelOrder()`를 거쳐서
열리므로 분류코드가 보이고, 마크업만 보면 없는 것처� 보인다 - **이 함수는 AS-IS 전역에서
엑셀다운로드 버튼이 있는 화면 대부분이 쓴다**(70개 이상 JSP에서 참조).

**TO-BE는 이미 백엔드가 다 준비돼 있었다** - 1411(엑셀다운로드사유관리) 수정이력 팝업이
`PrivacyAccessLogService.reasonTypeOptions()`로 공통코드 `EXCELDOWNLOAD_REASON_TYPE`(1~5,
AS-IS 전역 reasonType 값과 정확히 동일)을 이미 읽고 있었고, `/common/opmanager/
privacy-access-log` 엔드포인트도 `reasonType` 파라미터를 이미 받아 저장하고 있었다. **빠진
건 다운로드를 처음 트리거하는 공통 모달(`fragments/privacy-access.html`)에 이 select를
보여주는 부분 하나뿐**이었다.

**조치**: AS-IS처럼 "매 화면마다 JS로 즉석 주입"하지 않고, TO-BE 관례대로 **서버에서 한 번에
렌더링**했다.
- `ManagerAuthAdvice`에 `@ModelAttribute("reasonTypeOptions")` 추가 - `PrivacyAccessLogService.
  reasonTypeOptions()`를 모든 화면에 전역 주입(이 모달을 쓰는 화면이 많아 컨트롤러마다
  일일이 넣을 필요 없게, `msg`/`navTree`와 같은 패턴).
- `fragments/privacy-access.html`에 `<select id="reasonType" name="reasonType" title="엑셀
  다운로드타입" class="wd-400 required">` 추가(`.modal-body` 바로 앞 - AS-IS 삽입 위치와 동일),
  `th:each="entry : ${reasonTypeOptions}"`로 1411과 같은 옵션을 뿌린다.
- 변경 핸들러(AS-IS verbatim) - 분류코드를 고르면 그 라벨 텍스트를 사유(`#reason`) textarea에
  그대로 채운다.
- `openPrivacyModal()`이 열 때마다 `#reasonType`도 빈 값으로 리셋(AS-IS의 "이미 있으면
  값만 초기화" 분기와 동일 효과).
- `name="reasonType"`이라 폼 제출(`$form.serialize()`) 시 자동으로 `/common/opmanager/
  privacy-access-log`에 실려가 저장된다 - 서버 쪽은 손대지 않았다(이미 받고 있었음).

**영향 범위**: 공통 모달 하나를 고쳤으므로 이 모달을 쓰는 화면 전부(오프라인담당자·메뉴관리·
주문관리·답례품리뷰·엑셀다운로드로그 등)에 동시에 적용된다 - AS-IS도 전역 공통함수라 전부
같은 동작이었다.

**상태**: admin `compileJava+test+bootJar` EXIT=0. **재기동 필요: admin.**
