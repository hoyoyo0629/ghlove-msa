package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 정산(settlements) 상세화면의 입금계좌 표시용 - AS-IS remittance가 세일러 계좌를
 *  정산확정 시점에 스냅샷했던 것과 동일한 목적(누구에게 입금해야 하는지). 읽기 전용,
 *  실패 시 조용히 null로 대체(다른 client와 동일한 관행). */
@Component
public class GiftClient {

    private final RestClient restClient;

    public GiftClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public SellerInfo sellerOf(Long sellerId) {
        try {
            return restClient.get()
                    .uri("/api/sellers/{id}", sellerId)
                    .retrieve()
                    .body(SellerInfo.class);
        } catch (RestClientException e) {
            return null;
        }
    }

    public record SellerInfo(Long sellerId, String sellerName, String bankName, String bankInName,
                              String bankAccountNumber) {
    }

    /** StatsService ReadModel 재동기화용 - 답례품 전체 현재 상태 스냅샷. */
    public List<ItemSnapshot> allForResync() {
        try {
            List<ItemSnapshot> items = restClient.get()
                    .uri("/api/admin/items/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ItemSnapshot>>() {
                    });
            return items != null ? items : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record ItemSnapshot(Long itemId, Long sellerId, String categoryCode,
                                String dataStatusCode, Integer salePrice, String locgovCode,
                                String itemName, Integer brandId) {
    }

    /** 입점업체관리 (AS-IS opmanager/seller) - gift가 소유한 OP_SELLER의 cross-service CRUD. */
    public List<SellerDetail> sellers() {
        try {
            List<SellerDetail> list = restClient.get()
                    .uri("/api/admin/sellers")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<SellerDetail>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public SellerDetail sellerDetail(Long sellerId) {
        return restClient.get().uri("/api/admin/sellers/{id}", sellerId).retrieve().body(SellerDetail.class);
    }

    public SellerDetail createSeller(SellerDetail form) {
        return restClient.post().uri("/api/admin/sellers").body(form).retrieve().body(SellerDetail.class);
    }

    public SellerDetail updateSeller(Long sellerId, SellerDetail form) {
        return restClient.put().uri("/api/admin/sellers/{id}", sellerId).body(form).retrieve().body(SellerDetail.class);
    }

    public void updateSellerStatus(Long sellerId, String statusCode) {
        restClient.post().uri("/api/admin/sellers/{id}/status?statusCode={sc}", sellerId, statusCode)
                .retrieve().toBodilessEntity();
    }

    public record SellerDetail(Long sellerId, String communityBusinessYn, String sellerName, String bankName,
                                String bankInName, String bankAccountNumber, String loginId, String password,
                                String userName, String telephoneNumber, String phoneNumber, String email,
                                String address, String addressDetail, String companyName, String representativeName,
                                String businessNumber, String businessLocation, Double commissionRate,
                                String remittanceType, String statusCode, String createdDate, Long createdUserId,
                                String updatedDate, Long updatedUserId) {
    }

    /** 브랜드관리 (AS-IS opmanager/brand) - gift가 소유한 OP_BRAND의 cross-service CRUD. */
    public List<BrandDetail> brands() {
        try {
            List<BrandDetail> list = restClient.get()
                    .uri("/api/admin/brands")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<BrandDetail>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public BrandDetail brandDetail(Integer brandId) {
        return restClient.get().uri("/api/admin/brands/{id}", brandId).retrieve().body(BrandDetail.class);
    }

    public void createBrand(BrandDetail form) {
        restClient.post().uri("/api/admin/brands").body(form).retrieve().toBodilessEntity();
    }

    public void updateBrand(Integer brandId, BrandDetail form) {
        restClient.put().uri("/api/admin/brands/{id}", brandId).body(form).retrieve().toBodilessEntity();
    }

    public void deleteBrand(Integer brandId) {
        restClient.post().uri("/api/admin/brands/{id}/delete", brandId).retrieve().toBodilessEntity();
    }

    public record BrandDetail(Integer brandId, String brandName, String brandImage, String brandContent,
                               String displayFlag, String locgovCode, String createdDate, Long createdUserId,
                               String updatedDate, Long updatedUserId) {
    }

    /** 답례품 선호도 (AS-IS opmanager/shop-statistics/wish/list) - 찜 원본 목록, admin이 상품별 집계. */
    public List<WishlistEntry> wishlistAll() {
        try {
            List<WishlistEntry> list = restClient.get()
                    .uri("/api/admin/wishlist/all")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<WishlistEntry>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public record WishlistEntry(Integer wishlistId, Long itemId, Long userId, String createdDate) {
    }
}
