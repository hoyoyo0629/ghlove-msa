package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 팝업. AS-IS OP_POPUP - 이전 라운드에서 등록/목록/노출토글만 구현했던 걸 이번에
 * 수정/삭제/이미지업로드/스타일(POPUP_STYLE)까지 채워 완성도를 보완한다. */
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

    /** "1"=텍스트, "2"=텍스트(테두리없음), "3"=이미지 (AS-IS 컨벤션). */
    @Column(name = "POPUP_STYLE")
    private String popupStyle;

    /** "Y"/"N" - 닫기버튼(오늘 하루 보지않기 등) 노출 여부. */
    @Column(name = "POPUP_CLOSE")
    private String popupClose;

    @Column(name = "SUBJECT")
    private String subject;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "START_DATE")
    private String startDate;

    @Column(name = "END_DATE")
    private String endDate;

    @Column(name = "POPUP_IMAGE")
    private String popupImage;

    @Column(name = "IMAGE_LINK")
    private String imageLink;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
