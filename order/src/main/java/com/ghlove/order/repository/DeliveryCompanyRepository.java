package com.ghlove.order.repository;

import com.ghlove.order.domain.DeliveryCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryCompanyRepository extends JpaRepository<DeliveryCompany, Integer> {
    List<DeliveryCompany> findAllByOrderByDeliveryCompanyIdDesc();

    /** 송장 등록 등 선택용 - 사용중인 택배사만. */
    List<DeliveryCompany> findByUseFlagOrderByDeliveryCompanyNameAsc(String useFlag);
}
