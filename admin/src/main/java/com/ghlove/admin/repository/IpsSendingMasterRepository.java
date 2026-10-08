package com.ghlove.admin.repository;

import com.ghlove.admin.domain.IpsSendingMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface IpsSendingMasterRepository extends JpaRepository<IpsSendingMaster, Long> {

    /**
     * AS-IS {@code SmsMapper.getInsttCrtSn} - "SELECT TIF_IPS_SNDNG_M_LIST_SN.NEXT_VALUE"
     * (CUBRID serial). TO-BE는 같은 이름의 PostgreSQL 시퀀스를 쓴다
     * (database/ddl/migration-admin-tif-ips-sequence.sql).
     */
    @Query(value = "select nextval('admin.tif_ips_sndng_m_list_sn')", nativeQuery = true)
    long nextListSn();
}
