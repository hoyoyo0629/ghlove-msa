package com.ghlove.admin.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 농협 통계용 임시데이터(AS-IS G_NH_STAT_DATA_TEMP) - 농협 소속 판매자를 가리는 사업자번호
 *  목록(외부에서 받은 참조데이터, DDL은 database/ddl/service-admin.sql). 통계 > 답례품
 *  구매현황 > 농협 사업자 답례품 현황이 {@code OP_SELLER.BUSINESS_NUMBER}와 매칭해 쓴다. */
@Entity
@Table(name = "G_NH_STAT_DATA_TEMP")
@Getter
@Setter
@NoArgsConstructor
public class NhStatDataTemp {

    @Id
    @Column(name = "BUSINESS_NO")
    private String businessNo;

    public NhStatDataTemp(String businessNo) {
        this.businessNo = businessNo;
    }
}
