package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 팝업. Maps a subset of the AS-IS OP_POPUP columns (no image/positioning fields in this round). */
@Entity
@Table(name = "OP_POPUP")
@Getter
@Setter
@NoArgsConstructor
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "popupIdSeq")
    @SequenceGenerator(name = "popupIdSeq", sequenceName = "op_popup_popup_id_seq", allocationSize = 1)
    @Column(name = "POPUP_ID")
    private Integer popupId;

    @Column(name = "POPUP_TYPE")
    private String popupType;

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

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
