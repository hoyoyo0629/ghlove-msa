package com.ghlove.admin.repository;

import com.ghlove.admin.domain.IpsSendingMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface IpsSendingMasterRepository extends JpaRepository<IpsSendingMaster, Long> {

    /**
     * AS-IS getSmsSendList - 문자 구분(SVC_ID)·검색내용(수신번호/발송내용)·생성일 범위로 걸러
     * 최신순(LIST_SN DESC). 검색내용은 AS-IS가 "이름, 전화번호 등"으로 안내하므로 전화번호와
     * 발송내용 양쪽을 본다(발송내용에 이름이 파이프 구분으로 들어간다).
     */
    @Query("""
            select m from IpsSendingMaster m
             where (:svcId is null or m.svcId = :svcId)
               and (cast(:query as String) is null
                     or m.prvcIdntfcInfo like concat('%', cast(:query as String), '%')
                     or m.sndngCntnts like concat('%', cast(:query as String), '%'))
               and (:startDate is null or m.infoCrtDt >= :startDate)
               and (:endDate is null or m.infoCrtDt < :endDate)
             order by m.listSn desc
            """)
    List<IpsSendingMaster> search(@Param("svcId") String svcId,
                                  @Param("query") String query,
                                  @Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * AS-IS {@code SmsMapper.getInsttCrtSn} - "SELECT TIF_IPS_SNDNG_M_LIST_SN.NEXT_VALUE"
     * (CUBRID serial). TO-BE는 같은 이름의 PostgreSQL 시퀀스를 쓴다
     * (database/ddl/migration-admin-tif-ips-sequence.sql).
     */
    @Query(value = "select nextval('admin.tif_ips_sndng_m_list_sn')", nativeQuery = true)
    long nextListSn();
}
