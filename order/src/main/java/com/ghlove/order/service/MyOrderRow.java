package com.ghlove.order.service;

/**
 * 마이페이지 "주문조회"/"취소반품교환" 목록 한 줄 (AS-IS mypage/orderList.html·orderCancel.html의
 * order.items 한 항목). 화면이 필요로 하는 값을 order 자신의 데이터(Order)와 다른 서비스에서
 * 끌어온 값(답례품 썸네일 = gift, 지자체명 = donation)으로 합쳐 만든다 - 템플릿에서
 * 서비스 호출을 하지 않도록 컨트롤러가 한 번에 조립한다.
 */
public record MyOrderRow(String orderId, String createdDate, String itemName, String thumbnailUrl,
                          String locgovCode, String locgovNm, int quantity, long pointAmount,
                          String orderStatus, String orderStatusLabel,
                          String carrierCode, String invoiceNo, String cancelReason,
                          String claimType, String claimTypeLabel, String claimStatus, String claimStatusLabel) {

    /** 배송조회 링크를 걸 수 있는 상태인지 (송장번호가 실제로 있는 경우만). */
    public boolean hasDelivery() {
        return invoiceNo != null && !invoiceNo.isBlank();
    }
}
