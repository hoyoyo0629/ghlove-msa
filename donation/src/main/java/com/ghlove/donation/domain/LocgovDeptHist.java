package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 지자체 부서변경이력 (G_LOCGOV_DEPT_HIST) - admin 지자체관리 상세화면의 "부서코드 이력"
 * 팝업(AS-IS user/locgov/popup)이 읽는다. Locgov#processDeptCode가 바뀔 때마다
 * LocgovAdminService가 한 행씩 추가한다(append-only, 수정/삭제 없음). */
@Entity
@Table(name = "G_LOCGOV_DEPT_HIST")
@IdClass(LocgovDeptHistId.class)
@Getter
@Setter
@NoArgsConstructor
public class LocgovDeptHist {

    @Id
    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Id
    @Column(name = "DEPT_HIST_NO")
    private Integer deptHistNo;

    @Column(name = "PROCESS_DEPT_CODE")
    private String processDeptCode;

    @Column(name = "FRST_REGISTER_ID")
    private Long frstRegisterId;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;

    @Column(name = "LAST_UPDUSR_ID")
    private Long lastUpdusrId;

    @Column(name = "LAST_UPDT_PNTTM")
    private LocalDateTime lastUpdtPnttm;
}
