package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

/**
 * order 서비스의 택배사 마스터·반송지 관리 API 클라이언트 - 두 리소스 모두 ord 스키마
 * 소유라 데이터 주인은 order이고, admin은 운영 UI만 갖는다. 시크릿 헤더로 인증한다.
 */
@Service
public class DeliveryReturnClient {

    private static final String HEADER_SECRET = "X-Internal-Secret";

    private final RestClient restClient;
    private final String adminSecret;

    public DeliveryReturnClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl,
                                @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.create(orderServiceBaseUrl);
        this.adminSecret = adminSecret;
    }

    public record DeliveryCompanyDto(Integer deliveryCompanyId, String deliveryCompanyName, String telNumber,
                                     String deliveryCompanyUrl, String sendFlag, String deliveryNumberParameter,
                                     String useFlag) {
    }

    public record DeliveryCompanyForm(String deliveryCompanyName, String telNumber, String deliveryCompanyUrl,
                                      String sendFlag, String deliveryNumberParameter, String useFlag) {
    }

    public record ShipmentReturnDto(Integer shipmentReturnId, Long sellerId, String addressName, String name,
                                    String telephoneNumber, String zipcode, String address, String addressDetail,
                                    String defaultAddressFlag, String createdDate) {
    }

    public record ShipmentReturnForm(Long sellerId, String addressName, String name, String telephoneNumber,
                                     String zipcode, String address, String addressDetail, Boolean defaultAddress) {
    }

    // ---- 택배사 ----

    public List<DeliveryCompanyDto> deliveryCompanies() {
        return get("/api/admin/delivery-companies", DeliveryCompanyDto[].class);
    }

    public DeliveryCompanyDto deliveryCompany(Integer id) {
        try {
            return restClient.get().uri("/api/admin/delivery-companies/{id}", id)
                    .header(HEADER_SECRET, adminSecret).retrieve().body(DeliveryCompanyDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "택배사 조회에 실패했습니다."));
        }
    }

    public void createDeliveryCompany(DeliveryCompanyForm form) {
        try {
            restClient.post().uri("/api/admin/delivery-companies")
                    .header(HEADER_SECRET, adminSecret).contentType(MediaType.APPLICATION_JSON)
                    .body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "택배사 등록에 실패했습니다."));
        }
    }

    public void updateDeliveryCompany(Integer id, DeliveryCompanyForm form) {
        try {
            restClient.put().uri("/api/admin/delivery-companies/{id}", id)
                    .header(HEADER_SECRET, adminSecret).contentType(MediaType.APPLICATION_JSON)
                    .body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "택배사 수정에 실패했습니다."));
        }
    }

    public void deleteDeliveryCompany(Integer id) {
        try {
            restClient.delete().uri("/api/admin/delivery-companies/{id}", id)
                    .header(HEADER_SECRET, adminSecret).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "택배사 삭제에 실패했습니다."));
        }
    }

    // ---- 반송지 ----

    public List<ShipmentReturnDto> shipmentReturns(Long sellerId) {
        String path = sellerId != null ? "/api/admin/shipment-returns?sellerId=" + sellerId : "/api/admin/shipment-returns";
        return get(path, ShipmentReturnDto[].class);
    }

    public ShipmentReturnDto shipmentReturn(Integer id) {
        try {
            return restClient.get().uri("/api/admin/shipment-returns/{id}", id)
                    .header(HEADER_SECRET, adminSecret).retrieve().body(ShipmentReturnDto.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "반품지 조회에 실패했습니다."));
        }
    }

    public void createShipmentReturn(ShipmentReturnForm form) {
        try {
            restClient.post().uri("/api/admin/shipment-returns")
                    .header(HEADER_SECRET, adminSecret).contentType(MediaType.APPLICATION_JSON)
                    .body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "반품지 등록에 실패했습니다."));
        }
    }

    public void updateShipmentReturn(Integer id, ShipmentReturnForm form) {
        try {
            restClient.put().uri("/api/admin/shipment-returns/{id}", id)
                    .header(HEADER_SECRET, adminSecret).contentType(MediaType.APPLICATION_JSON)
                    .body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "반품지 수정에 실패했습니다."));
        }
    }

    public void deleteShipmentReturn(Integer id) {
        try {
            restClient.delete().uri("/api/admin/shipment-returns/{id}", id)
                    .header(HEADER_SECRET, adminSecret).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "반품지 삭제에 실패했습니다."));
        }
    }

    private <T> List<T> get(String path, Class<T[]> type) {
        try {
            T[] arr = restClient.get().uri(path).header(HEADER_SECRET, adminSecret).retrieve().body(type);
            return arr == null ? List.of() : List.of(arr);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extract(e, "목록 조회에 실패했습니다."));
        }
    }

    private String extract(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(java.util.Map.class);
            if (body != null && body.get("message") != null) {
                return String.valueOf(body.get("message"));
            }
        } catch (RuntimeException ignore) {
            // 본문이 JSON이 아니면 기본 메시지
        }
        return fallback;
    }
}
