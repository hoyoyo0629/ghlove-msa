package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 공지사항 첨부파일 (AS-IS {@code G_DSGN_DNTN_BIZ_NTC_FILE_MNG}).
 * 복합키(공지 id + 파일 순번)는 AS-IS 표 구조 그대로다.
 *
 * <p>AS-IS 등록화면 안내문구: "여러 개의 파일을 드래그하거나 선택한 후 한 번에 등록할 수 있습니다."
 * 허용 확장자도 화면에 적혀 있다(jpg~zip) - 그 목록은 서비스가 검증한다.
 */
@Entity
@Table(name = "G_DSGN_DNTN_BIZ_NTC_FILE_MNG")
@IdClass(DesignatedNoticeFile.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class DesignatedNoticeFile {

    @Id
    @Column(name = "DSGN_DNTN_BIZ_NTC_ID")
    private Long noticeId;

    @Id
    @Column(name = "FILE_SEQ")
    private Integer fileSeq;

    /** 저장된 파일명. */
    @Column(name = "FILE_NM", nullable = false)
    private String fileNm;

    /** 저장 경로(서버 상대경로). */
    @Column(name = "PATH_NM", nullable = false)
    private String pathNm;

    /** 사용자가 올린 원본 파일명 - 화면 링크에 이 이름이 보인다. */
    @Column(name = "ORGNFL_NM", nullable = false)
    private String orgnflNm;

    @Column(name = "FRST_RGTR_ID", nullable = false)
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT", nullable = false)
    private LocalDateTime frstRegDt;

    public DesignatedNoticeFile(Long noticeId, Integer fileSeq, String fileNm, String pathNm,
                                 String orgnflNm, Long frstRgtrId) {
        this.noticeId = noticeId;
        this.fileSeq = fileSeq;
        this.fileNm = fileNm;
        this.pathNm = pathNm;
        this.orgnflNm = orgnflNm;
        this.frstRgtrId = frstRgtrId;
        this.frstRegDt = LocalDateTime.now();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Key implements Serializable {
        private Long noticeId;
        private Integer fileSeq;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Key key)) {
                return false;
            }
            return Objects.equals(noticeId, key.noticeId) && Objects.equals(fileSeq, key.fileSeq);
        }

        @Override
        public int hashCode() {
            return Objects.hash(noticeId, fileSeq);
        }
    }
}
