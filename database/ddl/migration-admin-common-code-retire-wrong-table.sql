-- admin.ADMIN_COMMON_CODE 퇴장 → admin.OP_COMMON_CODE로 전환 (2026-10-07).
--
-- AS-IS(ghlove 모놀리스) 소스 전체(41개 매퍼 + code-mapper.xml)에 "ADMIN_COMMON_CODE"라는
-- 테이블은 존재하지 않는다 - AS-IS의 유일한 공통코드 테이블은 OP_COMMON_CODE다. 그런데
-- 지금까지 admin 서비스의 CommonCode 엔티티가 ADMIN_COMMON_CODE(TO-BE가 만든 이름)를 가리키고
-- 있었고, 실제로 화면이 읽는 "라이브" 테이블이 그쪽이었다. 반면 OP_COMMON_CODE는 2026-10-02
-- AS-IS 실운영 export로 정확히 적재해 뒀지만 아무도 읽지 않는 고아 테이블이었다
-- (migration-admin-common-code-asis-load.sql이 테이블명을 잘못 짚어 적재한 결과).
--
-- 그 결과 라이브 화면(ADMIN_COMMON_CODE)의 데이터가 OP_COMMON_CODE보다 부실했다 - 예:
-- MAINTEN_STATE_CODE의 DETAIL이 ADMIN_COMMON_CODE에는 전부 빈 값인데 OP_COMMON_CODE(실제
-- AS-IS export)에는 '접수'/'처리중'/'처리완료'/'제외'가 제대로 들어있다. "코드상세가 AS-IS와
-- 다르다"는 지적의 원인이다.
--
-- ADMIN_COMMON_CODE에만 있는 7개 code_type(29행)은 TO-BE가 의도적으로 추가한 의미코드라
-- (migration-admin-common-code-asis-gap.sql 주석 참고) 그대로 가져온다. 나머지 72개
-- code_type은 양쪽에 다 있으므로 OP_COMMON_CODE(AS-IS 원본)쪽이 우선이다 - 지운다/덮지
-- 않는다, OP_COMMON_CODE는 이미 정확하다.
--
-- 이후 CommonCode.java 엔티티를 OP_COMMON_CODE(컬럼명도 LANGUAGE, AS-IS와 동일)로 돌린다.
-- ADMIN_COMMON_CODE는 더 이상 아무도 읽지 않는다 - DROP은 사용자 승인 후 별도 진행(보류 원칙).

INSERT INTO admin.op_common_code
    (code_type, language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code)
SELECT code_type, code_language, id, label, detail, ordering, use_yn, up_id, code_value, extension_code, mapping_code
  FROM admin.admin_common_code
 WHERE code_type IN ('BSNS_PURPS_CODE', 'DELIVERY_CARRIER', 'MANAGER_STATUS', 'NOTICE_CATEGORY',
                      'POLICY_TYPE', 'POPUP_TYPE', 'SETTLEMENT_STATUS')
ON CONFLICT (code_type, language, id) DO NOTHING;
