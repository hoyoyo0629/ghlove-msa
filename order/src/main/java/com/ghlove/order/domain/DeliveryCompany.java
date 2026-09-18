package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 택배사 마스터 (AS-IS OP_DELIVERY_COMPANY / DeliveryCompanyManagerController). 송장 등록 시
 * 택배사 선택과 배송조회 URL 조합에 쓰인다. 이관은 됐으나 애플리케이션 코드가 없던 것을
 * 이번에 admin 운영관리로 얹는다. 편집은 admin, 소유(스키마)는 order다.
 */
@Entity
@Table(name = "OP_DELIVERY_COMPANY")
@Getter
@Setter
@NoArgsConstructor
public class DeliveryCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deliveryCompanySeq")
    @SequenceGenerator(name = "deliveryCompanySeq",
            sequenceName = "op_delivery_company_delivery_company_id_seq", allocationSize = 1)
    @Column(name = "DELIVERY_COMPANY_ID")
    private Integer deliveryCompanyId;

    @Column(name = "DELIVERY_COMPANY_NAME")
    private String deliveryCompanyName;

    @Column(name = "TEL_NUMBER")
    private String telNumber;

    /** 배송조회 URL - 송장번호를 붙여 추적 링크를 만든다. */
    @Column(name = "DELIVERY_COMPANY_URL")
    private String deliveryCompanyUrl;

    /** 전송방법 (1:GET, 2:POST). */
    @Column(name = "SEND_FLAG")
    private String sendFlag;

    /** 배송조회 URL에 송장번호를 넣을 쿼리 파라미터명. */
    @Column(name = "DELIVERY_NUMBER_PARAMETER")
    private String deliveryNumberParameter;

    /** 사용유무 (Y:사용, N:사용안함). */
    @Column(name = "USE_FLAG")
    private String useFlag;
}
