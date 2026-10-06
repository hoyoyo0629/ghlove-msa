package com.ghlove.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 개인정보 열람 이력 - AS-IS {@code G_INDVDLINFO_READNG_HIST} 재현.
 *
 * <p>일반회원관리(메뉴 4101) 상세의 "개인정보 열람"은 운영자가 자기 비밀번호를 한 번 더 확인한
 * 뒤에 마스킹하지 않은 회원정보를 보여주는데, AS-IS는 그 직전에 이 테이블에 <b>누가(USER_ID)
 * 누구를(TRGET_USER_ID) 언제(READNG_DT)</b> 열람했는지 남긴다
 * ({@code GeneralCustomerServiceImpl.getGeneralCustomerDetailsNoMasking}).
 *
 * <p>엑셀 다운로드 사유를 남기는 {@code OP_PRIVACY_ACCESS_LOG}(메뉴 1411)와는 다른 테이블이다 -
 * 이쪽은 화면에서 한 건을 열어본 이력이라 사유를 받지 않는다.
 */
@Entity
@Table(name = "G_INDVDLINFO_READNG_HIST")
@Getter
@Setter
@NoArgsConstructor
public class IndvdlinfoReadngHist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gIndvdlinfoReadngHistSeq")
    @SequenceGenerator(name = "gIndvdlinfoReadngHistSeq",
            sequenceName = "g_indvdlinfo_readng_hist_readng_sn_seq", allocationSize = 1)
    @Column(name = "READNG_SN")
    private Long readngSn;

    /** 열람한 운영자의 USER_ID. */
    @Column(name = "USER_ID")
    private Long userId;

    /** 열람 시각. 이 테이블은 (다른 AS-IS 날짜컬럼과 달리) 실제 TIMESTAMP다. */
    @Column(name = "READNG_DT")
    private java.time.LocalDateTime readngDt;

    /** 열람 대상 회원의 USER_ID. */
    @Column(name = "TRGET_USER_ID")
    private Long trgetUserId;
}
