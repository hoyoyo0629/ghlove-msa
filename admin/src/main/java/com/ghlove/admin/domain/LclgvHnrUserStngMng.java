package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체 명예직원(명예시도민증) 설정 (AS-IS opmanager/lclgvHnrUser - LclgvHnrUserManagerController,
 * G_LCLGV_HNR_USER_STNG_MNG). AS-IS 매퍼(lclgvHnrUser-mapper.xml) 기준 PK는 지자체코드
 * 1건당 1행(지자체별 등급 기준금액/혜택 설정) - 신규 회원가입식 CRUD가 아니라 "지자체별
 * 명예사용자 선정기준 설정" 화면이다. AS-IS 원본 DB 스캔에서 서브테이블(RWRD_IMG_EXPLN)만
 * 발견되고 메인 테이블은 없어 이 프로젝트 DB에 신규로 만들었다(매퍼 컬럼 그대로 재현). */
@Entity
@Table(name = "G_LCLGV_HNR_USER_STNG_MNG")
@Getter
@Setter
@NoArgsConstructor
public class LclgvHnrUserStngMng {

    @Id
    @Column(name = "LCLGV_CD")
    private String lclgvCd;

    @Column(name = "GLD_GRD_DNTN_AMT")
    private Long gldGrdDntnAmt;

    @Column(name = "SLVR_GRD_DNTN_AMT")
    private Long slvrGrdDntnAmt;

    @Column(name = "BRNZ_GRD_DNTN_AMT")
    private Long brnzGrdDntnAmt;

    @Column(name = "HNR_USER_STNG_TTL")
    private String hnrUserStngTtl;

    @Column(name = "HNR_USER_RWRD")
    private String hnrUserRwrd;

    @Column(name = "RPRS_IMG_NM")
    private String rprsImgNm;

    @Column(name = "HNR_USER_SLCTN_SE_CD")
    private String hnrUserSlctnSeCd;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "LAST_REG_DT")
    private LocalDateTime lastRegDt;

    @Column(name = "LAST_RGTR_ID")
    private Long lastRgtrId;
}
