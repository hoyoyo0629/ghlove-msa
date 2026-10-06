-- 사업부서(특정사업 담당부서) 검증용 시드
--
-- G_DSGN_DNTN_BIZ_DEPT_MNG이 0행이라 사업부서 관리 화면과 사업 등록폼의 "사업부서" select가
-- "-부서 미지정-"만 보였다(화면 기능은 정상이지만 확인이 불가). 실제 사업이 있는 지자체에
-- 부서를 넣어 둔다 - 지자체코드는 donation.g_locgov 기준이다.
--
-- 11680 강남구 / 44770 서천군 : 특정사업 기부 실적이 있는 지자체
-- 26350 해운대구 / 12150 순천시 : 사업만 있는 지자체(사용/미사용 섞어 검색 확인용)

insert into donation.g_dsgn_dntn_biz_dept_mng
       (dsgn_dntn_biz_dept_id, dsgn_dntn_biz_dept_nm, lclgv_cd, use_yn, frst_rgtr_id, last_rgtr_id,
        frst_reg_dt, last_reg_dt)
values
(1, '복지정책과',   '11680', 'Y', 1001, 1001, now() - interval '40 days', now() - interval '40 days'),
(2, '청소년과',     '11680', 'Y', 1001, 1001, now() - interval '30 days', now() - interval '30 days'),
(3, '지역경제과',   '44770', 'Y', 1001, 1001, now() - interval '20 days', now() - interval '20 days'),
(4, '해양관광과',   '26350', 'Y', 1001, 1001, now() - interval '10 days', now() - interval '10 days'),
(5, '문화예술과',   '12150', 'N', 1001, 1001, now() - interval '5 days',  now() - interval '5 days')
on conflict (dsgn_dntn_biz_dept_id) do nothing;

-- 채번 시퀀스가 있으면 시드 뒤로 옮긴다(없으면 조용히 넘어간다).
do $$
begin
    if exists (select 1 from information_schema.sequences
                where sequence_schema = 'donation'
                  and sequence_name = 'g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq') then
        perform setval('donation.g_dsgn_dntn_biz_dept_mng_dsgn_dntn_biz_dept_id_seq',
                       coalesce((select max(dsgn_dntn_biz_dept_id) + 1
                                   from donation.g_dsgn_dntn_biz_dept_mng), 1), false);
    end if;
end $$;
