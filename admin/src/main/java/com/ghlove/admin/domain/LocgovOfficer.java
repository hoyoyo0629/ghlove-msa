package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 매직라인4웹 PKI 인증서 로그인용 지자체 담당자 매핑 (인증서 Subject DN -> 담당자/지자체). */
@Entity
@Table(name = "LOCGOV_OFFICER")
@Getter
@Setter
@NoArgsConstructor
public class LocgovOfficer {

    @Id
    @Column(name = "CERT_SUBJECT_DN")
    private String certSubjectDn;

    @Column(name = "LOCGOV_CODE")
    private String locgovCode;

    @Column(name = "OFFICER_NAME")
    private String officerName;

    @Column(name = "USE_YN")
    private String useYn;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
