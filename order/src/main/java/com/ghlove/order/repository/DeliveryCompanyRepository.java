package com.ghlove.order.repository;

import com.ghlove.order.domain.DeliveryCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeliveryCompanyRepository extends JpaRepository<DeliveryCompany, Integer> {
    List<DeliveryCompany> findAllByOrderByDeliveryCompanyIdDesc();

    /** 송장 등록 등 선택용 - 사용중인 택배사만. */
    /** AS-IS getActiveDeliveryCompanyListAll - 사용(Y) 업체를 ID 내림차순(송장 등록 배송업체 select 순서). */
    List<DeliveryCompany> findByUseFlagOrderByDeliveryCompanyIdDesc(String useFlag);
}
