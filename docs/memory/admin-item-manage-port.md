---
name: admin-item-manage-port
description: 답례품관리>답례품관리(16301) 목록 AS-IS 이식 - 목록/일괄처리/삭제신청/변경사항팝업/지자체 대표·오프라인 대표 답례품. 등록·수정 폼과 엑셀은 미이식, 상태코드 숫자 전환 포함
metadata:
  type: project
---

2026-10-16 이식·2026-10-19 실화면 검증. AS-IS `opmanager/item/list`(메뉴 16301). 구성: admin `ItemManageController`(`/item`) + `ItemManageClient` +
`templates/item/list|log-popup.html`, gift `ItemManageApiController`(`/api/admin/item-manage/*`) + `ItemManageService`. 메뉴 URL `/item`, 16351(답례품승인관리)은 옛 `/admin/gift-items` 그대로.

**검증한 것(2026-10-19, 스페어 포트 + Playwright)**: 시스템 관리자 목록 23건·지자체 컬럼("시도 시군구")·페이징, 지자체 담당자(pwtest_loc 11230)는 자기 지자체 8건 + 대표 답례품(≤4)/오프라인 대표(≤2) 영역,
일괄 공개/비공개/품절/품절해제(품절만 처리 직전 상태를 `op_item_log`에 남김 - AS-IS 동일), 삭제=삭제신청(상태 40+로그), 변경사항 팝업, 대표 답례품 저장·전체삭제,
오프라인 대표 저장 시 AS-IS 검증 문구("30,000P와 15,000P인 상품 1건씩만"). 날짜 범위 검사(`Common.searchDateMonth`)가 AS-IS 그대로라 "전체"로 넓게 잡으면 alert로 막힌다 - 버그 아님.

**이번에 고친 것**: 상품코드 검색이 0건 - TO-BE 답례품은 `item_user_code`가 26건 전부 비어 있고(엔티티에 매핑조차 없음) 화면은 `G2`+9자리로 계산해 보여주는데 검색은 저장값만 봤다.
`USER_CODE_EXPR`(coalesce(nullif(item_user_code,''),'G2'||lpad(item_id,9,'0')))로 화면 표시와 같은 식으로 검색(상품코드·상품명 두 분기).

**상태코드 숫자 전환(같은 날, 마이그레이션 적용 완료)**: `op_item.data_status_code`를 AS-IS 숫자(1 정상/20 등록신청/21 반려/90 판매종료/99 삭제…)로 통일, GIFT_STATUS 공통코드도 재키잉.
gift/order/storefront/admin 코드가 같이 바뀌었으므로 이 서비스들은 함께 재기동해야 한다. 개발 판매자 3/18은 지자체코드 비어 있음(상품 없는 판매자 - 정상).

**★미이식(남은 것)**: ①엑셀 다운로드 팝업 `/item/download-excel`(목록 "엑셀" 버튼이 여는데 컨트롤러 매핑 없음 = 죽은 버튼) ②업로드 `/item/upload-excel` ③등록 `item/create`·수정 `item/edit/{코드}` 폼 -
지금은 옛 TO-BE 폼(`/admin/gift-items/new`, `/{id}/edit`)으로 리다이렉트만 한다(코드 `G2`+번호에서 번호 추출). 컨트롤러 javadoc에도 같은 내용.

**함정**: 검증 중 `item_id=1028`은 목록에 안 나온다 - `item_data_type=2`(추가구성 자식)라서 `item_data_type='1'` 조건에 걸리는 게 맞다. 테스트 변경은 SQL로 원복하되 Kafka 이벤트가 안 나가므로 `update-display`를 한 번 더 쳐서 현재 상태를 재발행한 뒤 `updated_*`만 되돌렸다.
관련 [[admin-categories-port]], [[admin-seller-manage-port]], [[admin-representative-banner-port]], [[gift-seller-status-asis-codes]], [[spare-stack-needs-kafka-member-donation]].
