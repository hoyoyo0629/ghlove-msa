package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 공지사항. Maps a subset of the AS-IS OP_NOTICE columns. */
@Entity
@Table(name = "OP_NOTICE")
@Getter
@Setter
@NoArgsConstructor
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noticeIdSeq")
    @SequenceGenerator(name = "noticeIdSeq", sequenceName = "op_notice_notice_id_seq", allocationSize = 1)
    @Column(name = "NOTICE_ID")
    private Integer noticeId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "NOTICE_CATEGORY_CODE")
    private String categoryCode;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "HITS")
    private Integer hits;

    @Column(name = "NOTICE_FLAG")
    private String noticeFlag;

    /** 지자체공지사항(기금사업소개 4번째 탭)용 - 전역 공지는 NULL, 지자체 공지는 그
     * 지자체 코드가 채워진다. */
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;
}
