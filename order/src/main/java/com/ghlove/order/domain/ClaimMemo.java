package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** 클레임(반품/교환) 처리 메모 이력 - admin 콘솔에서 승인/거절/완료 처리 시 또는 단독으로
 * 남기는 운영자 메모. 승인/거절/완료 자체의 상태전이 이력은 OD_CLAIM.STATUS/PROCESSED_DATE에
 * 이미 있으므로, 이 테이블은 "왜 그렇게 처리했는지"에 대한 자유서식 메모만 누적한다. */
@Entity
@Table(name = "OD_CLAIM_MEMO")
@Getter
@Setter
@NoArgsConstructor
public class ClaimMemo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CLAIM_MEMO_ID")
    private Long claimMemoId;

    @Column(name = "CLAIM_ID")
    private Long claimId;

    @Column(name = "MANAGER_ID")
    private Long managerId;

    @Column(name = "MANAGER_NAME")
    private String managerName;

    @Column(name = "MEMO")
    private String memo;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
