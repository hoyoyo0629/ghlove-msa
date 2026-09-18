package com.ghlove.order.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 반송지(반품 회수 주소) - AS-IS OP_SHIPMENT_RETURN / ShipmentReturnManagerController.
 * 판매자별 반품지 주소록으로, 반품/교환 회수 시 어디로 보낼지의 기준이 된다.
 * defaultAddressFlag='Y'가 판매자당 하나여야 한다(서비스에서 강제).
 */
@Entity
@Table(name = "OP_SHIPMENT_RETURN")
@Getter
@Setter
@NoArgsConstructor
public class ShipmentReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipmentReturnSeq")
    @SequenceGenerator(name = "shipmentReturnSeq",
            sequenceName = "op_shipment_return_shipment_return_id_seq", allocationSize = 1)
    @Column(name = "SHIPMENT_RETURN_ID")
    private Integer shipmentReturnId;

    @Column(name = "SELLER_ID")
    private Long sellerId;

    /** 주소 별칭 (예: "본사 반품센터"). */
    @Column(name = "ADDRESS_NAME")
    private String addressName;

    @Column(name = "NAME")
    private String name;

    @Column(name = "TELEPHONE_NUMBER")
    private String telephoneNumber;

    @Column(name = "ZIPCODE")
    private String zipcode;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "ADDRESS_DETAIL")
    private String addressDetail;

    /** 기본 반품지 여부 (Y/N). */
    @Column(name = "DEFAULT_ADDRESS_FLAG")
    private String defaultAddressFlag;

    @Column(name = "CREATED_DATE")
    private String createdDate;
}
