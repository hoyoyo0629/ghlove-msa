package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** OP_HONOR_VIEW_HIST - 기부혜택증(명예시도민증) 열람이력 (AS-IS OP_HONOR_VIEW_HIST 재현,
 * lclgvHnrUser-mapper.xml lclgvHnrUserViewHist 참고). 열람 이벤트를 실제로 남기는 화면
 * (기부혜택증 조회)이 이 라운드 범위 밖이라 지금은 조회 전용 테이블로만 존재한다. */
@Entity
@Table(name = "OP_HONOR_VIEW_HIST")
@Getter
@Setter
@NoArgsConstructor
public class OpHonorViewHist {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opHonorViewHistIdSeq")
    @SequenceGenerator(name = "opHonorViewHistIdSeq", sequenceName = "op_honor_view_hist_view_hist_id_seq", allocationSize = 1)
    @Column(name = "VIEW_HIST_ID")
    private Long viewHistId;

    @Column(name = "VIEW_DT")
    private LocalDateTime viewDt;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LCLGV_CD")
    private String lclgvCd;
}
