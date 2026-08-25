package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** NH농협 데이터 연계 배치 반출 기록 (기부 1건당 1행, 중복 반출 방지용). */
@Entity
@Table(name = "NH_EXPORT_LOG")
@Getter
@Setter
@NoArgsConstructor
public class NhExportLog {

    @Id
    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    @Column(name = "EXPORT_STATUS")
    private String exportStatus;

    @Column(name = "EXPORTED_DATE")
    private LocalDateTime exportedDate;
}
