package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 이메일 발송의 대상 권한 (AS-IS opmanager/email - OP_EMAIL_DETAIL).
 * 발송대상이 "권한별(A)"일 때 고른 권한그룹을 메일 한 건당 여러 행으로 저장한다.
 */
@Entity
@Table(name = "OP_EMAIL_DETAIL")
@IdClass(OpEmailDetail.Key.class)
@Getter
@Setter
@NoArgsConstructor
public class OpEmailDetail {

    @Id
    @Column(name = "EMAIL_ID")
    private Long emailId;

    @Id
    @Column(name = "AUTHORITY")
    private String authority;

    /** 상세화면 표시용 - AS-IS는 OP_ROLE을 조인해 권한그룹명을 가져온다. */
    @Transient
    private String roleName;

    public OpEmailDetail(Long emailId, String authority) {
        this.emailId = emailId;
        this.authority = authority;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class Key implements Serializable {
        private Long emailId;
        private String authority;
    }
}
