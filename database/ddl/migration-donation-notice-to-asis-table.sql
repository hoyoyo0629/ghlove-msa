-- 특정사업 기부 공지사항을 AS-IS 실명 표로 이관 (2026-10-08).
--
-- TO-BE는 G_DSGN_PRJ_NOTICE 라는 **AS-IS에 없는 이름**의 표를 쓰고 있었다. AS-IS 매퍼
-- (selectDesignatedDonationNoticeList)가 쓰는 표는 G_DSGN_DNTN_BIZ_NTC_MNG 이고,
-- 거기에는 공개여부(RLS_YN)가 있으며 첨부(G_DSGN_DNTN_BIZ_NTC_FILE_MNG)와
-- 이미지설명(G_DSGN_DNTN_BIZ_NTC_CN_IMG_EXPLN) 표가 딸려 있다.
--
-- 그 결과 운영자 목록의 "공개여부" 열·일괄 [공개]/[비공개]·파일첨부·이미지설명을 아예 만들 수
-- 없었고, 대민 상세화면도 AS-IS와 달리 비공개 공지까지 전부 노출하고 있었다.
-- 사용자 결정(2026-10-08)으로 AS-IS 실명 표로 전환한다. 표·컬럼이 이미 존재하므로 DDL은 없다.
-- 같은 유형의 선례: ADMIN_COMMON_CODE -> OP_COMMON_CODE.
--
-- RLS_YN 은 옮기는 7건 모두 'Y'(공개)로 둔다 - 옛 표에 공개여부 개념이 없었고, 지금까지
-- 대민에 전부 노출돼 왔으므로 그 상태를 그대로 보존하는 쪽이 안전하다.
-- 등록자 id 는 옛 표에 없어 0 으로 채운다(화면은 등록자를 쓰지 않는다).
--
-- 적용: MSYS_NO_PATHCONV=1 docker run --rm -e PGPASSWORD=... postgres:16-alpine \
--         psql -h 10.0.110.37 -p 15432 -U admindb -d ghlove_core -f (이 파일)

INSERT INTO donation.g_dsgn_dntn_biz_ntc_mng
    (dsgn_dntn_biz_id, dsgn_dntn_biz_ntc_ttl, dsgn_dntn_biz_ntc_cn, rls_yn,
     frst_rgtr_id, frst_reg_dt, last_rgtr_id, last_reg_dt)
SELECT n.dsgn_dntn_biz_id,
       COALESCE(n.prj_notice_subject, '(제목 없음)'),
       n.prj_notice_cn,
       'Y',
       0,
       COALESCE(TO_TIMESTAMP(NULLIF(n.frst_regist_pnttm, ''), 'YYYYMMDDHH24MISS'), now()),
       0,
       COALESCE(TO_TIMESTAMP(NULLIF(n.frst_regist_pnttm, ''), 'YYYYMMDDHH24MISS'), now())
  FROM donation.g_dsgn_prj_notice n
 WHERE NOT EXISTS (
       SELECT 1 FROM donation.g_dsgn_dntn_biz_ntc_mng t
        WHERE t.dsgn_dntn_biz_id = n.dsgn_dntn_biz_id
          AND t.dsgn_dntn_biz_ntc_ttl = COALESCE(n.prj_notice_subject, '(제목 없음)'));

-- 옛 표(G_DSGN_PRJ_NOTICE)는 지우지 않고 남긴다 - 되돌릴 근거가 필요할 수 있고
-- DROP 은 파괴적이라 사용자 판단으로 둔다. 코드에서는 더 이상 읽지 않는다.
