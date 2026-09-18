package com.ghlove.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 탈퇴회원의 연간 기부액 스냅샷 (AS-IS `G_MBER_SECSN`).
 *
 * <p>존재 이유는 하나다 — <b>탈퇴 후 재가입으로 연간 기부한도를 우회하는 것을 막는 것</b>.
 * AS-IS `DonationVerification.isDonationNormalAmount()`가 잔여한도를 계산할 때
 * `getGMberSecsnSumCntrAmt(mberCi)`로 이 테이블을 <b>본인확인 CI 기준</b>으로 합산해 뺀다
 * (`ngdonation-mapper.xml`: `where mber_ci = ? and secsn_year = 올해`).
 * 회원 탈퇴로 `USER_ID`가 끊겨도 CI는 같은 사람을 가리키므로 한도가 이어진다.
 */
@Entity
@Table(name = "g_mber_secsn")
@Getter
@Setter
@NoArgsConstructor
public class MberSecsn {

    @EmbeddedId
    private MberSecsnId id;

    /** 탈퇴 시점 기준 그 해에 이미 기부한 금액. donation 서비스에서 조회해 넣는다. */
    @Column(name = "CNTR_AMT")
    private Integer cntrAmt;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;

    public MberSecsn(String secsnYear, Long userId, String mberCi, Integer cntrAmt) {
        this.id = new MberSecsnId(secsnYear, userId, mberCi);
        this.cntrAmt = cntrAmt;
        this.frstRegisterId = userId;
        this.frstRegistPnttm = LocalDateTime.now();
    }
}
