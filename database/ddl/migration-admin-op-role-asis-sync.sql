-- admin.op_role 을 AS-IS(CUBRID ghlove2.OP_ROLE)와 동일하게 맞춘다.
-- 2026-10-06, AS-IS 개발DB(10.0.120.51:30000/ghlove2) 직접 조회로 확인한 10행 verbatim.
--
-- 왜 필요한가
--  1) TO-BE에 ROLE_ADMIN_9(답례품관리자) · ROLE_ADMIN_10(지정기부사업자) 두 행이 없었다.
--     1404 사용자 권한 관리 화면은 '지정기부사업자'만 목록에서 제외하므로(AS-IS
--     user-group/role/list.jsp 의 <c:if>), AS-IS 화면에는 9개가 보이고 TO-BE는 8개만 보였다.
--  2) created_date 가 10행 전부 비어 있었다. AS-IS 목록 쿼리가
--     ORDER BY opr.CREATED_DATE DESC 라서, 이 값이 없으면 목록 순서가 AS-IS와 달라지고
--     그 결과 "처음 접속했을 때 선택되는 첫 행"(답례품관리자)도 달라진다.
--     1405 사용자 권한그룹 관리의 '생성일자' 컬럼도 이 값을 쓴다.
--  3) role_desc 가 TO-BE에서 지어낸 설명문("시스템 전체 권한(정담당자) - 통계/정산/...",
--     "오프라인 기부접수 주담당자 (D8 신규 시드)")이었다. AS-IS는 role_name 과 같은 값이고
--     (ROLE_ADMIN_9만 "답례품관리자1") 이 값이 1405 화면 '설명' 컬럼에 그대로 노출된다.
--
-- updated_date / updated_user_id 는 옮기지 않는다 - AS-IS 운영 중 메뉴권한을 손볼 때마다
-- 바뀌는 값이고 두 화면 어디에도 표시되지 않는다(POP_ADMIN period 를 동기화 제외한 것과 같은 판단).
-- role_seq 는 AS-IS OP_ROLE 에 없는 TO-BE 전용 컬럼이다(정렬 동순위 안정화용) - 번호를 이어 붙인다.
--
-- 재실행 안전(ON CONFLICT / UPDATE).

INSERT INTO admin.op_role (authority, role_name, role_desc, created_date, created_user_id, role_seq)
VALUES
    ('ROLE_ADMIN_9',  '답례품관리자',   '답례품관리자1',  '20220915104904', '0',  9),
    ('ROLE_ADMIN_10', '지정기부사업자', '지정기부사업자', '20240325174435', '0', 10)
ON CONFLICT (authority) DO UPDATE
   SET role_name       = EXCLUDED.role_name,
       role_desc       = EXCLUDED.role_desc,
       created_date    = EXCLUDED.created_date,
       created_user_id = EXCLUDED.created_user_id,
       role_seq        = EXCLUDED.role_seq;

-- 기존 8행: role_desc 를 AS-IS verbatim 으로, created_date/created_user_id 를 AS-IS 값으로.
UPDATE admin.op_role r
   SET role_desc       = v.role_desc,
       created_date    = v.created_date,
       created_user_id = '0'
  FROM (VALUES
        ('ROLE_ADMIN_1', '시스템주담당자',   '20220915104002'),
        ('ROLE_ADMIN_2', '시스템부담당자',   '20220915104003'),
        ('ROLE_ADMIN_3', '행안부주담당자',   '20220915104609'),
        ('ROLE_ADMIN_4', '행안부부담당자',   '20220915104752'),
        ('ROLE_ADMIN_5', '지자체주담당자',   '20220915104808'),
        ('ROLE_ADMIN_6', '지자체부담당자',   '20220915104823'),
        ('ROLE_ADMIN_7', '오프라인주담당자', '20220915104841'),
        ('ROLE_ADMIN_8', '오프라인부담당자', '20220915104852')
       ) AS v(authority, role_desc, created_date)
 WHERE r.authority = v.authority;

-- ROLE_ADMIN_9(답례품관리자)는 AS-IS OP_MENU_RIGHT 에 행이 0건이다(메뉴권한 전부 해제 상태).
-- ROLE_ADMIN_10 의 6건(11401,11402,17101,17102,17201,17202)은 TO-BE에 이미 동일하게 있다.
-- 따라서 op_menu_right 는 손대지 않는다.
