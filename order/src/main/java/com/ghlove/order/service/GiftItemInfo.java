package com.ghlove.order.service;

/** Response shape of gift service's read-only GET /api/gifts/{id} lookup.
 * thumbnailPath is relative to gift service (e.g. "/uploads/xxx.jpg") - callers must
 * prefix it with gift's base URL before rendering. shipping* fields are SFR-005
 * "배송비·택배사 설정" (재검토 라운드) - used by OrderService to compute deliveryFee. */
public record GiftItemInfo(Long itemId, String itemName, Long sellerId, Integer salePrice,
                            Integer stockQuantity, String soldOut, String dataStatusCode,
                            String locgovCode, String thumbnailPath, String deliveryCompanyName,
                            String shippingType, Integer shipping, Integer shippingFreeAmount,
                            Integer shippingExtraCharge1, Integer shippingExtraCharge2,
                            String itemReturnFlag, String mobileItemYn, Integer orderMaxQuantity,
                            Integer shippingItemCount, String shippingGroupCode, String shipmentGroupCode,
                            Integer shippingReturn, String deliveryType) {

    /**
     * 교환·반품이 가능한 답례품인가 - AS-IS `mypage/orderList.html:318,321`의 버튼 노출 조건
     * (`itemReturnFlag == 'Y' && mobileItemYn == 'N'`)과 같다.
     * 모바일교환권은 실물 배송이 없어 반품 대상이 아니고, 반품 불가로 등록된 상품도 제외된다.
     * 값이 없으면 DDL 기본값(`ITEM_RETURN_FLAG='Y'`, `MOBILE_ITEM_YN='N'`)대로 가능으로 본다.
     */
    public boolean returnable() {
        boolean returnAllowed = itemReturnFlag == null || "Y".equalsIgnoreCase(itemReturnFlag);
        boolean physical = mobileItemYn == null || !"Y".equalsIgnoreCase(mobileItemYn);
        return returnAllowed && physical;
    }
}
