package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 고객센터 FAQ (AS-IS faq/list.html). Maps OP_COMMUNITY_LOCGOVFAQ - 배치스캔 구간에
 *  이미 있었지만 지금까지 어떤 서비스도 채우지 않았던 테이블. */
@Entity
@Table(name = "OP_COMMUNITY_LOCGOVFAQ")
@Getter
@Setter
@NoArgsConstructor
public class LocgovFaq {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "locgovFaqIdSeq")
    @SequenceGenerator(name = "locgovFaqIdSeq", sequenceName = "op_community_locgovfaq_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "FAQ_TYPE")
    private String faqType;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "CREATED_DATE")
    private java.time.LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    private java.time.LocalDateTime updatedDate;

    @Column(name = "HITS")
    private Integer hits;

    @Column(name = "ADMIN_ID")
    private Long adminId;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
