package com.ghlove.admin.web.support;

import lombok.Getter;
import lombok.Setter;

/**
 * AS-IS saleson.shop.deliverycompany.support.DeliveryCompanyParam 재현 - 배송업체 목록 검색조건.
 * 검색구분(where)은 배송업체명(DELIVERY_COMPANY_NAME)·연락처(TEL_NUMBER) 둘이고,
 * 화면출력은 10/20/30/50/100/500/1000이다(AS-IS 셀렉트 그대로).
 */
@Getter
@Setter
public class DeliveryCompanyParam {

    private String where = "DELIVERY_COMPANY_NAME";
    private String query = "";
    private int itemsPerPage = 10;
    private int page = 1;

    public boolean matches(String deliveryCompanyName, String telNumber) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String keyword = query.trim();
        if ("TEL_NUMBER".equals(where)) {
            return telNumber != null && telNumber.contains(keyword);
        }
        return deliveryCompanyName != null && deliveryCompanyName.contains(keyword);
    }
}
