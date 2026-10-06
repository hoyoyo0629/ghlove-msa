package com.ghlove.order.domain;

import jakarta.persistence.*;

/**
 * 도서산간/제주 배송비 판정표 - AS-IS {@code saleson.model.Island}(OP_ISLAND)의 포팅.
 * 수취인 우편번호로 {@code island_type}(JEJU/ISLAND)을 조회해 추가배송비 부과 여부를 정한다
 * (AS-IS OrderMapper.getIslandTypeByZipcode). 운영자가 관리하는 참조 데이터라 order가 소유한다.
 */
@Entity
@Table(name = "OP_ISLAND")
public class Island {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "islandSeq")
    @SequenceGenerator(name = "islandSeq", sequenceName = "op_island_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ZIPCODE", length = 7)
    private String zipcode;

    @Column(name = "ADDRESS", length = 255)
    private String address;

    /** JEJU / ISLAND */
    @Column(name = "ISLAND_TYPE", length = 20)
    private String islandType;

    public Long getId() {
        return id;
    }

    public String getZipcode() {
        return zipcode;
    }

    public String getAddress() {
        return address;
    }

    public String getIslandType() {
        return islandType;
    }
}
