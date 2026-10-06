---
name: asis-message-catalog-loaded
description: "AS-IS 화면 문구 사전(OP_COMMON_MESSAGE) 운영DB export 받아 admin에 적재 완료 - 템플릿은 ${msg.get('M00730')}, JS는 /common/message. AS-IS 덤프 550개는 전부 DDL만이라 데이터는 사용자 export로만 확보"
metadata: 
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-02T09:01:58.091Z
---

**AS-IS 화면 문구는 전부 코드다** - JSP는 `${op:message('M00730')}`(고유 741코드 / 5,653곳), JS는 `Message.get("M00745")`. 2026-10-02 사용자가 운영DB에서 export(`Desktop\고향사랑e음\1.AS-IS\1. DB\OP_COMMON_MESSAGE_202610021746.sql`, **UTF-8**, ko 2034 / ja 98, 고유 M코드 1692)해줘서 `admin.op_common_message`에 적재 완료(`database/ddl/migration-admin-common-message-asis-load.sql`, 재실행 가능).

**How to apply:**
- 템플릿: `th:text="${msg.get('M00730')}"` - `ManagerAuthAdvice`의 `@ModelAttribute("msg")`가 모든 화면에 주입한다. 한글 직접 입력 금지([[copy-as-is-verbatim-never-invent]])
- JS: 복사해온 `op.common.js`의 `Message.get`이 `$.post('/common/message', {messageCode:...})`를 **동기**로 때린다. `CommonMessageApiController`가 경로·파라미터명·응답모양(`{isSuccess, data}`)까지 verbatim으로 받는다 - 바꾸면 공통 JS가 깨진다
- 조회는 `CommonMessageService` 메모리 캐시(동기 ajax라 DB 왕복 금지). 문구 수정 후엔 `reload()`
- 미등록 코드는 코드 문자열을 그대로 반환 - AS-IS와 동일, 빠진 게 화면에 드러난다

**중요: AS-IS 테이블 덤프(`db-dump\테이블dump\*.sql` 550개)는 전부 CREATE TABLE만, INSERT 0건이다.** [[asis-table-dump-path]]에 "코드성데이터 verbatim 확인용"으로 적어둔 건 **틀렸다**. 데이터가 필요하면 사용자에게 `SELECT ... FROM <table>` export를 요청해야 한다(사용자가 해주겠다고 했음). 이미 받은 것: `op_menu`, `op_menu_right`, `op_common_message`. **다음에 필요한 것: `op_common_code`**(AS-IS 매퍼 60곳이 `FROM OP_COMMON_CODE` 참조 - 화면 셀렉트박스 옵션의 원본), 그 외 `op_business_code`(5곳). 관련: [[asis-screen-port-procedure]]
