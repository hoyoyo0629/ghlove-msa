package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** SFR-002 "파기 이력 기록 및 보고" - 탈퇴회원 개인정보 익명화/로그인로그 익명화 실행 이력. */
@Entity
@Table(name = "USER_DATA_DESTRUCTION_LOG")
@Getter
@Setter
@NoArgsConstructor
public class UserDataDestructionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userDataDestructionLogIdSeq")
    @SequenceGenerator(name = "userDataDestructionLogIdSeq",
            sequenceName = "user_data_destruction_log_destruction_id_seq", allocationSize = 1)
    @Column(name = "DESTRUCTION_ID")
    private Long destructionId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "DESTROYED_FIELDS")
    private String destroyedFields;

    @Column(name = "REASON")
    private String reason;

    /** yyyyMMddHHmmss. */
    @Column(name = "DESTROYED_DATE")
    private String destroyedDate;
}
