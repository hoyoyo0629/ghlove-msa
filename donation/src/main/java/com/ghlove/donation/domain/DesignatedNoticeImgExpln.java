package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 공지사항 본문 이미지 설명 (AS-IS {@code G_DSGN_DNTN_BIZ_NTC_CN_IMG_EXPLN}).
 *
 * <p>AS-IS 등록화면 에디터 아래의 표(순번 / 이미지 설명 / 삭제 + [+ 추가])가 이것이다.
 * 안내문구도 그대로다: "* 시각 장애인을 위한 이미지 설명을 입력하여 주시길 부탁드립니다. *"
 * 특정사업 등록폼의 이미지 설명표와 같은 목적이고 표만 다르다.
 */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_NTC_CN_IMG_EXPLN")
@IdClass(DesignatedNoticeImgExpln.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class DesignatedNoticeImgExpln {

    @Id
    @Column(name = "DSGN_DNTN_BIZ_NTC_ID")
    private Long noticeId;

    /** 순번(1부터) - 화면의 "순번" 칸이고 readonly 다. */
    @Id
    @Column(name = "IMG_SEQ")
    private Integer imgSeq;

    @Column(name = "IMG_EXPLN", nullable = false)
    private String imgExpln;

    @Column(name = "FRST_RGTR_ID", nullable = false)
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT", nullable = false)
    private LocalDateTime frstRegDt;

    public DesignatedNoticeImgExpln(Long noticeId, Integer imgSeq, String imgExpln, Long frstRgtrId) {
        this.noticeId = noticeId;
        this.imgSeq = imgSeq;
        this.imgExpln = imgExpln;
        this.frstRgtrId = frstRgtrId;
        this.frstRegDt = LocalDateTime.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private Long noticeId;
        private Integer imgSeq;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key key)) {
                return false;
            }
            return Objects.equals(noticeId, key.noticeId) && Objects.equals(imgSeq, key.imgSeq);
        }

        @Override
        public int hashCode() {
            return Objects.hash(noticeId, imgSeq);
        }
    }
}
