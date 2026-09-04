package com.ghlove.member.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 회원 등급변경 이력 (AS-IS OP_USER_LEVEL_LOG) - 회원이 어느 시점에 어떤 등급이었는지
 *  스냅샷을 남긴다. 이 프로젝트는 회원가입/구매에 등급을 자동 산정하는 배치가 아직 없어
 *  (등급관리 화면 자체가 이번에 신설) 현재는 관리자가 UserLevelAdminService#usageCount()로
 *  "이 등급을 쓰는 회원 수"를 셀 때(삭제 가드) 참조하는 용도로만 쓰인다. USER_ID는
 *  복합 자연키가 아니라 로그 행 식별에만 쓰이는 컬럼이라 별도 PK 시퀀스를 새로 붙였다
 *  (원본 DDL은 PK 지정이 없는 로그성 테이블). */
@Entity
@Table(name = "OP_USER_LEVEL_LOG")
@Getter
@Setter
@NoArgsConstructor
public class UserLevelLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "opUserLevelLogSeq")
    @SequenceGenerator(name = "opUserLevelLogSeq", sequenceName = "op_user_level_log_log_id_seq", allocationSize = 1)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(name = "GROUP_CODE")
    private String groupCode;

    @Column(name = "LEVEL_ID")
    private Integer levelId;

    @Column(name = "LEVEL_NAME")
    private String levelName;

    @Column(name = "ADMIN_USER_NAME")
    private String adminUserName;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
