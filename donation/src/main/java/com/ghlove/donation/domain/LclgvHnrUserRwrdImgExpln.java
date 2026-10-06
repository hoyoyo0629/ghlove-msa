package com.ghlove.donation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 기부혜택증 메인 이미지의 대체텍스트(이미지 설명) - AS-IS
 * {@code G_LCLGV_HNR_USER_RWRD_IMG_EXPLN} 재현.
 *
 * <p>AS-IS 기부혜택증 설정 화면(메뉴 19102)에 "시각장애인을 위한 이미지 설명을 입력해주세요.
 * ※이미지 등록 수와 이미지 설명 항목 수가 반드시 일치해야 함"이라는 안내와 함께 순번·설명을
 * 행 단위로 추가/삭제하는 표가 있고, 저장할 때 지자체 단위로 <b>전부 지우고 다시 넣는다</b>
 * (AS-IS {@code deleteImgDescListByPrjId} → {@code insertImgDescList}).
 *
 * <p>AS-IS는 이 값을 특정사업 기부의 이미지 설명 DTO({@code PrjImageExplain})에 담아 재사용한다
 * ({@code IMG_EXPLN AS PRJSVALUES}, {@code IMG_SEQ AS PRJSINDEXES}) - 표가 다른데 DTO만 공유한
 * 것이라 TO-BE는 이 표 전용 모델로 둔다.
 */
@Entity
@Table(name = "g_lclgv_hnr_user_rwrd_img_expln")
@IdClass(LclgvHnrUserRwrdImgExpln.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class LclgvHnrUserRwrdImgExpln {

    @Id
    @Column(name = "LCLGV_CD", length = 10)
    private String lclgvCd;

    /** 1부터 시작하는 순번(화면의 '순번' 칸, 읽기전용). */
    @Id
    @Column(name = "IMG_SEQ")
    private Integer imgSeq;

    @Column(name = "IMG_EXPLN")
    private String imgExpln;

    @Column(name = "FRST_RGTR_ID")
    private Long frstRgtrId;

    @Column(name = "FRST_REG_DT")
    private LocalDateTime frstRegDt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private String lclgvCd;
        private Integer imgSeq;
    }
}
