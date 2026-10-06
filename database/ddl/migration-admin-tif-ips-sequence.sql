-- 국민비서(IPS) 문자발송 적재용 채번 시퀀스
--
-- AS-IS는 CUBRID serial을 쓴다: SmsMapper.getInsttCrtSn = "SELECT TIF_IPS_SNDNG_M_LIST_SN.NEXT_VALUE",
-- 그 값을 LIST_SN과 INSTT_CRT_SN(PK)에 같이 넣는다. TO-BE 표(admin.tif_ips_sndng_m)는 이미
-- AS-IS 컬럼 그대로 있으나 채번 수단이 없어 같은 이름의 시퀀스를 만든다.
--
-- 이미 들어 있는 행이 있으면 그 다음 번호부터 시작한다(지금은 0행).

create sequence if not exists admin.tif_ips_sndng_m_list_sn as bigint start with 1 increment by 1;

-- 3번째 인자 is_called=false면 "다음 nextval이 이 값"이라는 뜻이다 - 빈 표에서는 1부터 쓰게 된다.
select setval('admin.tif_ips_sndng_m_list_sn',
              coalesce((select max(instt_crt_sn) + 1 from admin.tif_ips_sndng_m), 1), false);
