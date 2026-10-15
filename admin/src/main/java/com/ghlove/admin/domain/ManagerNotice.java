package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관리자 공지 팝업(AS-IS OP_MANAGER_NOTICE). 관리자 로그인 화면(NOTICE_TYPE 'O')·기탁서 등록(15102, 'N') 등이 화면을 열 때
 * {@code pop.openNotice(type)}로 띄운다. AS-IS에도 관리 화면은 없고 DB로 넣어 쓴다 - 여기서는 읽기만 한다.
 */
@Entity
@Table(name = "OP_MANAGER_NOTICE")
@Getter
@Setter
@NoArgsConstructor
public class ManagerNotice {

    @Id
    @Column(name = "MANAGER_NOTICE_ID")
    private Long managerNoticeId;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "NOTICE_TYPE")
    private String noticeType;

    @Column(name = "WIDTH")
    private Integer width;

    @Column(name = "HEIGHT")
    private Integer height;
}
