package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.List;

/** "주문금액-포인트사용 대사"(AS-IS view_rc_order_amt_vs_point_use_check 재구현)용 - order
 *  서비스의 포인트차감 성공 주문 목록을 읽기 전용으로 조회한다. */
@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl) {
        this.restClient = RestClient.create(orderServiceBaseUrl);
    }

    public List<PointDeductedOrder> pointDeductedOrders() {
        try {
            List<PointDeductedOrder> orders = restClient.get()
                    .uri("/api/orders/point-deducted")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<PointDeductedOrder>>() {
                    });
            return orders != null ? orders : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record PointDeductedOrder(String orderId, Long userId, String itemName, Long pointAmount,
                                      String orderStatus, LocalDateTime createdDate) {
    }

    /** StatsService ReadModel 재동기화용 - 주문 전체 현재 상태 스냅샷. */
    public List<OrderSnapshot> allForResync() {
        try {
            List<OrderSnapshot> orders = restClient.get()
                    .uri("/api/admin/orders/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<OrderSnapshot>>() {
                    });
            return orders != null ? orders : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record OrderSnapshot(String orderId, Long userId, Long itemId, Long sellerId,
                                 Integer quantity, Long pointAmount, String orderStatus) {
    }

    // ==================== 쿠폰관리 (AS-IS opmanager/coupon+coupon-regular+coupon-use) ====================

    public List<CouponDetail> coupons() {
        try {
            List<CouponDetail> list = restClient.get().uri("/api/admin/coupons").retrieve()
                    .body(new ParameterizedTypeReference<List<CouponDetail>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public CouponDetail coupon(Integer id) {
        return restClient.get().uri("/api/admin/coupons/{id}", id).retrieve().body(CouponDetail.class);
    }

    public CouponDetail createCoupon(CouponDetail form) {
        return restClient.post().uri("/api/admin/coupons").body(form).retrieve().body(CouponDetail.class);
    }

    public void updateCoupon(Integer id, CouponDetail form) {
        restClient.put().uri("/api/admin/coupons/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteCoupon(Integer id) {
        restClient.post().uri("/api/admin/coupons/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void togglePublishCoupon(Integer id) {
        restClient.post().uri("/api/admin/coupons/{id}/publish", id).retrieve().toBodilessEntity();
    }

    public record CouponDetail(Integer couponId, String couponType, String couponName, String couponComment,
                                String issueType, String issueStartDate, String issueEndDate,
                                String applyType, Integer applyDay, String applyStartDate, String applyEndDate,
                                String targetTimeType, String targetUserType, String targetUserLevel,
                                Integer payRestriction, String concurrently, String payType, Integer pay,
                                Integer discountLimitPrice, String targetItemType, String couponFlag,
                                String offlineFlag, String birthday, String dataStatusCode,
                                Integer downloadLimit, Integer downloadUserLimit, String multipleDownloadFlag,
                                String createdDate, String updateUserName, String updatedDate,
                                String directInputFlag, String directInputValue) {
    }

    public List<CouponTargetItemInfo> couponTargetItems(Integer couponId) {
        try {
            List<CouponTargetItemInfo> list = restClient.get().uri("/api/admin/coupons/{id}/target-items", couponId)
                    .retrieve().body(new ParameterizedTypeReference<List<CouponTargetItemInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void addCouponTargetItem(Integer couponId, Long itemId) {
        restClient.post().uri("/api/admin/coupons/{id}/target-items?itemId={itemId}", couponId, itemId)
                .retrieve().toBodilessEntity();
    }

    public void removeCouponTargetItem(Integer couponId, Long itemId) {
        restClient.post().uri("/api/admin/coupons/{id}/target-items/{itemId}/delete", couponId, itemId)
                .retrieve().toBodilessEntity();
    }

    public record CouponTargetItemInfo(Long itemId, Integer couponId, String createdDate) {
    }

    public List<CouponTargetUserInfo> couponTargetUsers(Integer couponId) {
        try {
            List<CouponTargetUserInfo> list = restClient.get().uri("/api/admin/coupons/{id}/target-users", couponId)
                    .retrieve().body(new ParameterizedTypeReference<List<CouponTargetUserInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void addCouponTargetUser(Integer couponId, Long userId) {
        restClient.post().uri("/api/admin/coupons/{id}/target-users?userId={userId}", couponId, userId)
                .retrieve().toBodilessEntity();
    }

    public void removeCouponTargetUser(Integer couponId, Long userId) {
        restClient.post().uri("/api/admin/coupons/{id}/target-users/{userId}/delete", couponId, userId)
                .retrieve().toBodilessEntity();
    }

    public record CouponTargetUserInfo(Long userId, Integer couponId, String createdDate) {
    }

    public List<CouponOfflineInfo> couponOfflineCodes(Integer couponId) {
        try {
            List<CouponOfflineInfo> list = restClient.get().uri("/api/admin/coupons/{id}/offline-codes", couponId)
                    .retrieve().body(new ParameterizedTypeReference<List<CouponOfflineInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void generateCouponOfflineCodes(Integer couponId, int count) {
        restClient.post().uri("/api/admin/coupons/{id}/offline-codes?count={count}", couponId, count)
                .retrieve().toBodilessEntity();
    }

    public record CouponOfflineInfo(Integer couponOfflineId, Integer couponId, Long userId, String offlineCode,
                                     String usedFlag, String usedDate, String publishedDate) {
    }

    public List<CouponIssueInfo> couponUsage(Integer couponId) {
        try {
            List<CouponIssueInfo> list = restClient.get().uri("/api/admin/coupons/{id}/usage", couponId)
                    .retrieve().body(new ParameterizedTypeReference<List<CouponIssueInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public CouponCountsInfo couponCounts(Integer couponId) {
        try {
            return restClient.get().uri("/api/admin/coupons/{id}/counts", couponId).retrieve().body(CouponCountsInfo.class);
        } catch (RestClientException e) {
            return new CouponCountsInfo(0, 0);
        }
    }

    public record CouponIssueInfo(Integer couponUserId, Integer couponId, Long userId, String couponType,
                                   String couponName, String couponComment, String applyType,
                                   String applyStartDate, String applyEndDate, Integer payRestriction,
                                   String concurrently, String payType, Integer pay, Integer discountLimitPrice,
                                   String targetItemType, String dataStatusCode, String downloadDate,
                                   String usedDate, String orderCode, Integer orderSequence, Integer itemSequence,
                                   Integer discountAmount, String createdDate) {
    }

    public record CouponCountsInfo(long downloadCount, long usedCount) {
    }

    // ---- 정기발행쿠폰 ----

    public List<CouponRegularDetail> couponRegulars() {
        try {
            List<CouponRegularDetail> list = restClient.get().uri("/api/admin/coupon-regular").retrieve()
                    .body(new ParameterizedTypeReference<List<CouponRegularDetail>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public CouponRegularDetail couponRegular(Integer id) {
        return restClient.get().uri("/api/admin/coupon-regular/{id}", id).retrieve().body(CouponRegularDetail.class);
    }

    public CouponRegularDetail createCouponRegular(CouponRegularDetail form) {
        return restClient.post().uri("/api/admin/coupon-regular").body(form).retrieve().body(CouponRegularDetail.class);
    }

    public void updateCouponRegular(Integer id, CouponRegularDetail form) {
        restClient.put().uri("/api/admin/coupon-regular/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteCouponRegular(Integer id) {
        restClient.post().uri("/api/admin/coupon-regular/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public record CouponRegularDetail(Integer couponId, String couponType, String couponName, String couponComment,
                                       String issueType, String issueStartDate, String issueEndDate,
                                       String targetTimeType, String targetUserType, String targetUserLevel,
                                       Integer payRestriction, String concurrently, String payType, Integer pay,
                                       Integer discountLimitPrice, String targetItemType, String couponFlag,
                                       String birthday, String dataStatusCode, Integer downloadLimit,
                                       Integer downloadUserLimit, String multipleDownloadFlag,
                                       String createdDate, String updateUserName, String updatedDate) {
    }
}
