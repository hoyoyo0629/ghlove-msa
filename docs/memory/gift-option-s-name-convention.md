---
name: gift-option-s-name-convention
description: "gift 선택형(S) 옵션은 AS-IS처럼 option_name1=옵션 제목·option_name2=옵션 내용(2026-10-13 전환+5행 이관). 스냅샷 라벨은 '제목 : 내용'. S2·S3 제목(item_option_title1~3)은 TO-BE 미매핑"
metadata:
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T05:50:13.213Z
---

AS-IS OP_ITEM_OPTION 선택형(S) 실데이터 31,583행 전부 name1=제목('토마토')·name2=내용('완숙 토마토 4kg')
(opmanager/item/form.jsp 옵션적용: optionName1=제목, optionName2=내용, itemOptionTitle1/2='옵션 제목'/'옵션 내용').
TO-BE는 값을 name1에 넣고 name2를 비워 AS-IS 조회(selectOffRprsOption 등 name2 기준)와 어긋났었다.

**2026-10-13 전환**: GiftOptionService(saveOptions/register/edit, 검증문구 "옵션명을/옵션값을 입력해 주세요."),
admin 단건 옵션 API·화면(optionTitle 추가), 판매자 edit.html(S도 2칸 '옵션 제목/옵션 내용'), storefront 상세
(드롭다운 제목=첫 옵션 name1, 항목=name2 - AS-IS details-main.html), 스냅샷 라벨 "제목 : 내용"(AS-IS viewOptionTextNoUl).
기존 S행 5건 이관(`migration-gift-option-s-name-asis.sql`: 1025 '맛', 1026 '색상' - TO-BE 시드라 제목을 붙임).

**남은 갭**: S2·S3의 단계별 제목(OP_ITEM.ITEM_OPTION_TITLE1~3)을 TO-BE Gift 엔티티가 매핑하지 않아 저장·표시가 안 된다
(AS-IS는 조합형 드롭다운에 제목을 띄우고 옵션명 텍스트에 "제목: 값 | ..."로 쓴다). 판매자 옵션 에디터도 AS-IS의
"입력 도우미 + 적용" 구조가 아니다.

관련: [[gift-option-multiform-redesign]], [[offgive-15102-verbatim-port-data-gaps]]
