package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** AS-IS /featured/eventList.html(지역 이벤트) - OP_EVENT는 raw SQL로만 채워지는
 * 테이블이라 DesignatedProject/LocgovFaq/DataBoard와 동일하게 @GeneratedValue를 쓰지 않는다. */
@Entity
@Table(name = "OP_EVENT")
@Getter
@Setter
public class Event {

    @Id
    @Column(name = "EVENT_ID")
    private Integer eventId;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "LIST_IMAGE")
    private String listImage;

    @Column(name = "LINK_URL")
    private String linkUrl;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "UPPER_LOCGOV_CODE")
    private String upperLocgovCode;

    @Column(name = "LOCGOV_NM")
    private String locgovNm;

    @Column(name = "UPPER_LOCGOV_NM")
    private String upperLocgovNm;

    @Column(name = "HOST_NAME")
    private String hostName;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "USE_YN")
    private String useYn;
}
