package com.ghlove.donation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 기부혜택증 열람 이력 (AS-IS `OP_HONOR_VIEW_HIST`).
 *
 * <p>AS-IS는 혜택증을 스와이퍼(캐러셀)로 한 장씩 넘겨 보여주고, 슬라이드가 바뀔 때마다
 * 그 지자체 코드로 이력을 남긴다(`mypage/honorList.html:381`의 `slideChange` 핸들러 +
 * 최초 1건). 이 MSA는 같은 화면을 목록으로 그려 전부 한 번에 보여주므로, 화면을 열 때
 * <b>표시되는 혜택증 전부</b>를 열람으로 기록한다 - 표현이 달라도 "무엇을 봤는가"라는
 * 기록의 의미는 같다.
 *
 * <p>참고: AS-IS의 `/api/mypage/saveHonorViewHist`는 저장 직후 `throw new
 * OpRuntimeException("test")`가 무조건 실행되어 <b>항상 SYSTEM_ERROR를 응답</b>한다
 * (`MypageController:1078`). 디버그 잔재이므로 그 동작은 옮기지 않는다.
 */
@Entity
@Table(name = "op_honor_view_hist")
@Getter
@Setter
@NoArgsConstructor
public class HonorViewHist {

    @EmbeddedId
    private Id id;

    public HonorViewHist(Long userId, String lclgvCd) {
        this.id = new Id(LocalDateTime.now(), userId, lclgvCd);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Id implements Serializable {

        @Column(name = "VIEW_DT")
        private LocalDateTime viewDt;

        @Column(name = "USER_ID")
        private Long userId;

        @Column(name = "LCLGV_CD", length = 10)
        private String lclgvCd;
    }
}
