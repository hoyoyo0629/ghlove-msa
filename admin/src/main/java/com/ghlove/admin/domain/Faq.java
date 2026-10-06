package com.ghlove.admin.domain;

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

import java.time.LocalDateTime;

/**
 * FAQ (AS-IS {@code OP_FAQ} / {@code saleson.model.Faq}) - <b>고객센터 FAQ의 정본 표</b>다.
 * 운영자 FAQ 관리(메뉴 5104)가 쓰고, 공개 FAQ 페이지와 공개 API가 같은 표를 읽는다
 * (AS-IS 통합검색 뷰 {@code view_search_faq}도 이 표를 보고 {@code /faq/list.html}로 링크한다).
 *
 * <p>AS-IS는 {@code BaseEntity}(Spring Data Auditing)로 {@code created}/{@code updated}/
 * {@code created_by}/{@code updated_by}를 채운다. TO-BE admin에는 Auditing을 켜 두지 않아
 * 서비스에서 직접 넣는다(동작은 같다).
 *
 * <p>AS-IS는 {@code faqType}을 {@code @Enumerated(EnumType.STRING)}으로 매핑하는데, 그러면
 * DB에 모르는 값이 하나 있을 때 <b>목록 전체가 예외</b>로 터진다. TO-BE는 문자열로 두고
 * 라벨을 찾을 때만 {@link com.ghlove.admin.service.FaqType}으로 바꾼다(정상 데이터의 결과는 같다).
 */
@Entity
@Table(name = "OP_FAQ")
@Getter
@Setter
@NoArgsConstructor
public class Faq {

    /** AS-IS {@code OP_FAQ_SEQ}(initialValue 630000) 그대로. */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opFaqSeq")
    @SequenceGenerator(name = "opFaqSeq", sequenceName = "admin.op_faq_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "FAQ_TYPE")
    private String faqType;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "HIT")
    private Integer hit;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @Column(name = "CREATED_BY")
    private Long createdBy;

    @Column(name = "UPDATED")
    private LocalDateTime updated;

    @Column(name = "UPDATED_BY")
    private Long updatedBy;
}
