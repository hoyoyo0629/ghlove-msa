---
name: admin-excel-download-log-reason-edit-popup
description: "엑셀다운로드사유관리(1411) 사유 팝업이 늘 읽기전용이던 것을 AS-IS succChk 조건대로 수정가능하게 복원(2026-10-07)"
metadata:
  node_type: memory
  type: project
---

AS-IS `log/popup/exceldownload-log-detail.jsp`는 `succChk`(현재 로그인 관리자 == 그 로그를
남긴 MANAGER_ID)가 true일 때만 사유타입 select + textarea + "수정" 버튼을 보여주고, 아니면
읽기전용(사유타입 라벨 + 사유 텍스트 + "확인" 버튼만)이다. TO-BE
(`ExcelDownloadLogAdminController.reasonPopup`)는 이 분기 자체가 없어 항상 읽기전용이었다 -
수정 기능도, 그 결과로 만들어지는 수정이력도 실제로 동작할 수 없었다.

**수정**: 컨트롤러가 세션의 로그인 매니저와 `details.managerId`를 비교해 `succChk`를 계산하고,
`PrivacyAccessLogService.reasonTypeOptions()`(신규, `EXCELDOWNLOAD_REASON_TYPE` 공통코드를
등록순서대로)를 모델에 같이 넘긴다. 템플릿은 `succChk`로 편집/읽기전용 두 분기를 그려서
AS-IS와 동일하게 동작한다. 수정 POST는 이미 있던
`PrivacyAccessLogApiController.privacyAccessLogUpdate`(`/common/opmanager/privacy-access-log-update`)
를 그대로 쓴다(AS-IS 폼 필드명 `privacyAccessLogId`/`reason`/`reasonType`과 동일) - 새 엔드포인트
불필요했다. 수정 성공 후 갱신 방식은 AS-IS의 `opener.parent.search()`가 아니라 이 코드베이스가
이미 쓰는 `opener.location.reload()` 관례로 맞췄다(batch-job/form.html 등과 동일 패턴).

**이력보기 팝업도 같이 보강**: `excel-download-log-hist.html`에 AS-IS처럼 "총 N 건" 헤더와
No. 컬럼을 추가했다(컨트롤러가 `count` 모델 속성을 추가로 넘김). 전체 페이징은 추가하지 않았다
- AS-IS도 한 사람이 수정할 수 있는 이력 건수가 애초에 적어 실효성이 낮다.

**상태**: admin compileJava+test+bootJar 통과. **재기동 필요: admin.**
