---
name: offgive-15102-verbatim-port-data-gaps
description: "기탁서 등록(15102) 2026-10-13 verbatim 재이식 + 테스트 데이터 적재. 결함 2개(답례품 주문 미생성, S형 옵션 저장규칙 반대)는 같은 날 해결, 연계 우회도 로컬 전용으로 정리"
metadata:
  node_type: memory
  type: project
  originSessionId: 224e315d-88ae-4d9f-90e1-af3100460c29
  modified: 2026-10-13T05:50:27.283Z
---

15102 기탁서 등록을 form.jsp 1,995줄 전체 + OffgiveManagerController/OffgiveServiceImpl/offgive·give-state·ngdonation·
designated-donation 매퍼 기준으로 재이식(2026-10-13): 마크업·CSS·JS verbatim, 엔드포인트 AS-IS 이름,
실명인증 iframe(sci-call→sci-result), 지정기부목록 엑셀(`AsisSxssfExcelView`, 암호화 없음), PDF 버튼 같은 href.

**적재한 데이터(2026-10-13, 사용자 요청 "기능테스트에 필요한 일부만")**:
- `donation.g_locgov.ordering` AS-IS값 243건 (`migration-donation-g-locgov-ordering-asis-load.sql`) - 시군구 순서 AS-IS와 동일.
  남은 NULL 3건(46150·46530·36110)은 AS-IS에 대응값 없는 구코드.
- `gift.g_lclgv_off_rprs_gds_mng` **테스트 매핑 5건**(`seed-gift-offgive-representative-goods-test.sql`, frst_rgtr_id=0):
  강남구 11230(1000·1020, 서울 경로) / 해운대 26350(1001, 비서울 경로) / 제주 50000(1003·1014, 지정기부 1100 같이).
  AS-IS 실데이터는 AS-IS 상품ID라 TO-BE 시드상품(1000~1028)과 안 맞아 못 넣는다.
- **안 넣은 것**: g_cntr_lmtt(AS-IS 4건 전부 기간 지남), g_instt_code·g_adm_locgov(로컬 우회 중엔 화면 영향 없음, 실경로엔 필요).

**같은 날 해결한 결함**:
1. 대표답례품 주문 미생성(장바구니 체크아웃 재사용 → 포인트부족) → AS-IS saveOffGiveOrder/procOffOrder 이식,
   [[offgive-gift-order-asis-flow]].
2. S형 옵션 저장규칙 반대 → AS-IS(name1 제목/name2 내용)로 전환+이관, [[gift-option-s-name-convention]].
3. 연계 우회가 단일설정이라 운영에서도 우회되던 것 → 실경로 AS-IS 그대로 + 로컬 전용 우회, [[integration-bypass-local-only]].

**2026-10-14 등록 후 상세화면(detail.jsp "기탁서") 재이식**: 경로 `/offgive/detail/{접수번호}`(예전 `/offgive/{sn}`은 302),
getOffgive 한 줄을 donation(`/api/offgive/{sn}/detail`)·member(post·addressDetail 추가)·order(`/api/admin/offgive-orders/summary`,
품목명+옵션표시문자열 `<br/>`, `od_order.delivery_zipcode` 신설)·WDR로 조립. 기부취소(`/offgive/offgiveCancel` → donation
`/api/cntr-reqmng/offgive-cancel`, 입력검증 없음)·납부신청서 OZ 팝업(`ghlove.integrations.oz.url`) 이식. AS-IS `cancelTemPointSet`
(포인트 임시칸 이동)은 포인트가 point 원장이라 없음(변경신청 화면과 동일). TO-BE 전용 "수납처리" 버튼은 form 밖에 남김(사용자 결정 대기).

**How to apply:** 대표답례품 관리화면(AS-IS 답례품 목록의 "오프라인 대표 답례품 관리", ROLE_ADMIN_5/6,
`saveLclgvOffRprsGds`)은 미이식 - 매핑은 SQL로만 넣을 수 있다.
