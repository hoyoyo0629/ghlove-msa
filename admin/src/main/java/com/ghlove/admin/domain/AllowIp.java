package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 접속IP허용목록 (AS-IS opmanager/access, OP_ALLOW_IP). ACCESS_TYPE 1=관리자, 2=판매관리자
 *  (이 프로젝트엔 판매자 로그인 모델이 없어 사실상 1만 쓰이지만, AS-IS 값 그대로 유지). */
@Entity
@Table(name = "OP_ALLOW_IP")
@Getter
@Setter
@NoArgsConstructor
public class AllowIp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "allowIpIdSeq")
    @SequenceGenerator(name = "allowIpIdSeq", sequenceName = "op_allow_ip_allow_ip_id_seq", allocationSize = 1)
    @Column(name = "ALLOW_IP_ID")
    private Integer allowIpId;

    @Column(name = "ACCESS_TYPE")
    private String accessType;

    @Column(name = "REMOTE_ADDR")
    private String remoteAddr;

    @Column(name = "DISPLAY_FLAG")
    private String displayFlag;

    @Column(name = "CREATED_USER")
    private String createdUser;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "UPDATED_USER")
    private String updatedUser;

    @Column(name = "UPDATED_DATE")
    private String updatedDate;
}
