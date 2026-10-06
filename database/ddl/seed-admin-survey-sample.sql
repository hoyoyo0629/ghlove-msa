-- 설문관리(7207) / 이용자 설문참여 화면 검증용 시드데이터
--
-- 설문 표 4개(G_QESTNAR / G_QUSTNR_QESITM / G_QUSTNR_IEM / G_QUSTNR_RSPNS_RESULT)가 모두
-- 비어 있어 관리자 화면도, storefront 설문참여 화면도 눈으로 확인할 수가 없었다. 개발DB에만
-- 넣는 샘플이다 - **운영 데이터가 아니다**.
--
-- 구성은 세 가지 문항 형태를 모두 덮는다(화면 분기를 다 확인할 수 있게):
--   1 객관식(rtype) 선택지 4개
--   2 주관식(stype)
--   3 객관식(rtype) + 그 아래 연계질문(parent_sn) 1개
--
-- 노출기간은 오늘을 포함하도록 잡는다(어제 ~ 한 달 뒤) - 이용자 화면은 기간 안인 설문만
-- 보여주므로 고정 날짜로 넣으면 안 보인다. SRVY_TRGT='U'(대민)라야 /api/surveys/active에 걸린다.
--
-- 재실행 안전: QUSTNR_SN = 9001 한 건만 지우고 다시 넣는다.
--
-- 적용: MSYS_NO_PATHCONV=1 docker exec -i ghlove-postgres \
--         psql -U postgres -d ghlove_core -f /dev/stdin < seed-admin-survey-sample.sql

BEGIN;

DELETE FROM admin.g_qustnr_rspns_result WHERE qustnr_sn = 9001;
DELETE FROM admin.g_qustnr_iem          WHERE qustnr_sn = 9001;
DELETE FROM admin.g_qustnr_qesitm       WHERE qustnr_sn = 9001;
DELETE FROM admin.g_qestnar             WHERE qustnr_sn = 9001;

INSERT INTO admin.g_qestnar
    (qustnr_sn, srvy_trgt, qustnr_sj, qustnr_purps, qustnr_bgn_de, qustnr_end_de,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
VALUES
    (9001, 'U', '[샘플] 고향사랑기부제 이용 만족도 조사',
     '답례품 선택과 기부 절차에 대한 이용자 의견을 수집합니다.',
     to_char(now() - interval '1 day', 'YYYYMMDD'),
     to_char(now() + interval '1 month', 'YYYYMMDD'),
     1, now(), 1, now());

-- 문항 1: 객관식
INSERT INTO admin.g_qustnr_qesitm
    (qustnr_sn, qustnr_qesitm_sn, qestn_sn, qestn_ty_code, qestn_cn, answer_choise_co,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, parent_sn)
VALUES (9001, 1, 1, 'rtype', '고향사랑e음을 어떻게 알게 되셨습니까?', 1, 1, now(), 1, now(), NULL);

INSERT INTO admin.g_qustnr_iem
    (qustnr_sn, qustnr_qesitm_sn, qustnr_iem_sn, iem_sn, iem_cn, etc_answer_at,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
VALUES
    (9001, 1, 1, 1, 'TV·라디오 광고',     'N', 1, now(), 1, now()),
    (9001, 1, 2, 2, '인터넷 검색·블로그', 'N', 1, now(), 1, now()),
    (9001, 1, 3, 3, '지자체 안내',        'N', 1, now(), 1, now()),
    (9001, 1, 4, 4, '지인 추천',          'N', 1, now(), 1, now());

-- 문항 2: 주관식
INSERT INTO admin.g_qustnr_qesitm
    (qustnr_sn, qustnr_qesitm_sn, qestn_sn, qestn_ty_code, qestn_cn, answer_choise_co,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, parent_sn)
VALUES (9001, 2, 2, 'stype', '개선이 필요하다고 느낀 점을 자유롭게 적어 주세요.', 1, 1, now(), 1, now(), NULL);

-- 주관식도 AS-IS는 선택지 1건을 자리표시로 둔다(화면 입력칸 대응)
INSERT INTO admin.g_qustnr_iem
    (qustnr_sn, qustnr_qesitm_sn, qustnr_iem_sn, iem_sn, iem_cn, etc_answer_at,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
VALUES (9001, 2, 1, 1, '', 'N', 1, now(), 1, now());

-- 문항 3: 객관식 + 연계질문
INSERT INTO admin.g_qustnr_qesitm
    (qustnr_sn, qustnr_qesitm_sn, qestn_sn, qestn_ty_code, qestn_cn, answer_choise_co,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, parent_sn)
VALUES (9001, 3, 3, 'rtype', '답례품 선택 과정이 편리했습니까?', 1, 1, now(), 1, now(), NULL);

INSERT INTO admin.g_qustnr_iem
    (qustnr_sn, qustnr_qesitm_sn, qustnr_iem_sn, iem_sn, iem_cn, etc_answer_at,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
VALUES
    (9001, 3, 1, 1, '편리했다',   'N', 1, now(), 1, now()),
    (9001, 3, 2, 2, '보통이다',   'N', 1, now(), 1, now()),
    (9001, 3, 3, 3, '불편했다',   'N', 1, now(), 1, now());

INSERT INTO admin.g_qustnr_qesitm
    (qustnr_sn, qustnr_qesitm_sn, qestn_sn, qestn_ty_code, qestn_cn, answer_choise_co,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm, parent_sn)
VALUES (9001, 4, 4, 'stype', '(위에서 불편했다고 답하신 경우) 어떤 점이 불편했습니까?', 1, 1, now(), 1, now(), 3);

INSERT INTO admin.g_qustnr_iem
    (qustnr_sn, qustnr_qesitm_sn, qustnr_iem_sn, iem_sn, iem_cn, etc_answer_at,
     frst_register_id, frst_regist_pnttm, last_updusr_id, last_updt_pnttm)
VALUES (9001, 4, 1, 1, '', 'N', 1, now(), 1, now());

COMMIT;

SELECT q.qustnr_qesitm_sn AS 문항, q.qestn_ty_code AS 형태, q.parent_sn AS 부모,
       q.qestn_cn AS 문항내용, count(i.qustnr_iem_sn) AS 선택지수
  FROM admin.g_qustnr_qesitm q
  LEFT JOIN admin.g_qustnr_iem i
         ON i.qustnr_sn = q.qustnr_sn AND i.qustnr_qesitm_sn = q.qustnr_qesitm_sn
 WHERE q.qustnr_sn = 9001
 GROUP BY q.qustnr_qesitm_sn, q.qestn_ty_code, q.parent_sn, q.qestn_cn
 ORDER BY q.qustnr_qesitm_sn;
