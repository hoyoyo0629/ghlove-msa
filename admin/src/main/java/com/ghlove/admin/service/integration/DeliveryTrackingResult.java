package com.ghlove.admin.service.integration;

/** 스마트택배 배송조회 결과 (AS-IS SmartDeliveryServiceImpl 응답에 해당). */
public record DeliveryTrackingResult(String carrierCode, String invoiceNo, String status,
                                      String lastLocation, String lastUpdated) {
    public static DeliveryTrackingResult mock(String carrierCode, String invoiceNo) {
        return new DeliveryTrackingResult(carrierCode, invoiceNo, "IN_TRANSIT", "옥천 HUB (모크)", "방금 전 (모크)");
    }
}
