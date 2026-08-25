package com.ghlove.donation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 연계(부과/수납/국세청 등록) 시도 이력 - 기부 1건당 여러 행(호출마다 append).
 * DONATION_LEVY(현재 상태, upsert)와 달리 실패/재시도 이력까지 그대로 남긴다.
 */
@Entity
@Table(name = "G_RELAY_LOG")
@Getter
@Setter
@NoArgsConstructor
public class RelayLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relayLogIdSeq")
    @SequenceGenerator(name = "relayLogIdSeq", sequenceName = "g_relay_log_relay_log_id_seq", allocationSize = 1)
    @Column(name = "RELAY_LOG_ID")
    private Integer relayLogId;

    @Column(name = "USER_ID", nullable = false)
    private Long userId;

    @Column(name = "RELAY_TYPE", nullable = false)
    private String relayType;

    @Column(name = "CNTR_LOCGOV_CODE", nullable = false)
    private String cntrLocgovCode;

    @Column(name = "CNTR_SN")
    private String cntrSn;

    @Column(name = "RELAY_RESULT_CODE", nullable = false)
    private String relayResultCode;

    @Column(name = "FRST_REGIST_PNTTM")
    private LocalDateTime frstRegistPnttm;
}
