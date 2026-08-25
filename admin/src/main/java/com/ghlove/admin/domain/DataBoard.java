package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** AS-IS data-board/list.html(자료실) - OP_DATA_BOARD는 raw SQL로만 채워지는 테이블이라
 * DesignatedProject/LocgovFaq와 동일하게 @GeneratedValue를 쓰지 않는다. */
@Entity
@Table(name = "OP_DATA_BOARD")
@Getter
@Setter
public class DataBoard {

    @Id
    @Column(name = "DATA_ID")
    private Integer dataId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "HITS")
    private Integer hits;

    @Column(name = "CREATED_DATE")
    private String createdDate;

    @Column(name = "NOTICE_FLAG")
    private String noticeFlag;

    @Column(name = "USE_YN")
    private String useYn;
}
