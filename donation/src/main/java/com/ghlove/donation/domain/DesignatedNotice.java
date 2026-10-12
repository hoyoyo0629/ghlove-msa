package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 특정사업 기부 공지사항 (AS-IS {@code G_DSGN_DNTN_BIZ_NTC_MNG}).
 *
 * <p>TO-BE는 지금까지 {@code G_DSGN_PRJ_NOTICE} 라는 <b>AS-IS에 없는 이름</b>의 표를 쓰고 있었다.
 * 그 표에는 공개여부도 첨부도 없어서 AS-IS 화면의 "공개여부" 열·일괄 공개/비공개·파일첨부를
 * 아예 만들 수 없었다. AS-IS 실명 표 3종(본문/첨부/이미지설명)이 DB에 이미 비어 있는 채로
 * 존재해서, 2026-10-08 사용자 결정으로 이쪽으로 옮겼다(DDL 불필요, 기존 7건 이관).
 * 같은 유형의 선례: ADMIN_COMMON_CODE → OP_COMMON_CODE 전환.
 *
 * <p>AS-IS 매퍼({@code selectDesignatedDonationNoticeList})는 대민(FRONT) 조회 때
 * {@code RLS_YN = 'Y'} 를 건다 - 비공개 공지는 대민에 안 보인다.
 */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_NTC_MNG")
@Getter
@Setter
@NoArgsConstructor
public class DesignatedNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DSGN_DNTN_BIZ_NTC_ID")
    private Long noticeId;

    @Column(name = "DSGN_DNTN_BIZ_ID", nullable = false)
    private Long dsgnDntnBizId;

    @Column(name = "DSGN_DNTN_BIZ_NTC_TTL", nullable = false)
    private String subject;

    @Column(name = "DSGN_DNTN_BIZ_NTC_CN")
    private String content;

    /** 공개여부 Y/N - AS-IS 목록의 "공개여부" 열이자 대민 노출 조건. */
    @Column(name = "RLS_YN", nullable = false)
    private String rlsYn;

    @Column(name = "FRST_RGTR_ID", nullable = false)
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT", nullable = false)
    private LocalDateTime frstRegDt;

    @Column(name = "LAST_RGTR_ID", nullable = false)
    private Long lastRgtrId;

    @Column(name = "LAST_REG_DT", nullable = false)
    private LocalDateTime lastRegDt;
}
