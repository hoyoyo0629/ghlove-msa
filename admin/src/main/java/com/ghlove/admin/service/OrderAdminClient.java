package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * admin 주문관리 콘솔(AS-IS opmanager/order) - order 서비스의 관리자 전용 API
 * (/api/admin/orders/**, /api/admin/claims/**)를 호출한다. order는 세션이 없으므로 매 요청마다
 * (1) 공유시크릿(X-Internal-Secret)과 (2) 로그인한 관리자 신원을 헤더로 함께 싣는다 -
 * order의 AdminApiAuthInterceptor가 (1)을 검증하지 못하면 요청을 아예 거부하고, (2)는
 * 검증을 통과한 뒤 지자체 스코프/메모 작성자 표시에 쓰인다. 한글이 들어갈 수 있는 관리자
 * 이름은 헤더 인코딩 문제(HTTP 헤더는 기본적으로 ISO-8859-1)를 피하려 URL-encode해서
 * 보낸다 - order 쪽 컨트롤러가 그대로 decode한다.
 */
@Component
public class OrderAdminClient {

    private static final String HEADER_SECRET = "X-Internal-Secret";
    private static final String HEADER_MANAGER_ID = "X-Manager-Id";
    private static final String HEADER_MANAGER_NAME = "X-Manager-Name";
    private static final String HEADER_MANAGER_AUTHORITY = "X-Manager-Authority";
    private static final String HEADER_MANAGER_LOCGOV = "X-Manager-Locgov-Code";

    private final RestClient restClient;
    private final String adminSecret;

    public OrderAdminClient(@Value("${ghlove.order-service.base-url}") String orderServiceBaseUrl,
                             @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.create(orderServiceBaseUrl);
        this.adminSecret = adminSecret;
    }

    private void applyHeaders(HttpHeaders h, Manager manager) {
        h.add(HEADER_SECRET, adminSecret);
        if (manager != null) {
            if (manager.getUserId() != null) {
                h.add(HEADER_MANAGER_ID, String.valueOf(manager.getUserId()));
            }
            if (manager.getUserName() != null) {
                h.add(HEADER_MANAGER_NAME, URLEncoder.encode(manager.getUserName(), StandardCharsets.UTF_8));
            }
            if (manager.getAuthority() != null) {
                h.add(HEADER_MANAGER_AUTHORITY, manager.getAuthority());
            }
            if (manager.getLocgovCode() != null && !manager.getLocgovCode().isBlank()) {
                h.add(HEADER_MANAGER_LOCGOV, manager.getLocgovCode());
            }
        }
    }

    public SearchResult search(Manager manager, String locgovCode, String orderStatus, String searchType,
                                String keyword, String startDate, String endDate, int page, int size) {
        try {
            PageResponse<OrderDetail> result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/orders")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("orderStatus", java.util.Optional.ofNullable(blankToNull(orderStatus)))
                            .queryParamIfPresent("searchType", java.util.Optional.ofNullable(blankToNull(searchType)))
                            .queryParamIfPresent("keyword", java.util.Optional.ofNullable(blankToNull(keyword)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .headers(h -> applyHeaders(h, manager))
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<OrderDetail>>() {
                    });
            return result != null
                    ? new SearchResult(result.content(), result.totalElements(), result.totalPages(), result.page(), result.size())
                    : new SearchResult(List.of(), 0, 0, page, size);
        } catch (RestClientResponseException e) {
            return new SearchResult(List.of(), 0, 0, page, size);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public OrderDetail detail(Manager manager, String orderId) {
        try {
            return restClient.get().uri("/api/admin/orders/{id}", orderId)
                    .headers(h -> applyHeaders(h, manager)).retrieve().body(OrderDetail.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "주문을 찾을 수 없습니다."));
        }
    }

    public List<ClaimInfo> claimsOf(Manager manager, String orderId) {
        try {
            List<ClaimInfo> list = restClient.get().uri("/api/admin/orders/{id}/claims", orderId)
                    .headers(h -> applyHeaders(h, manager)).retrieve()
                    .body(new ParameterizedTypeReference<List<ClaimInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    /** memo/reason처럼 한글이 들어갈 수 있는 값은 URI 템플릿 플레이스홀더({memo})에 직접
     *  넣지 않는다 - RestClient가 템플릿 확장 시 자동으로 URL-encode하는데, 여기서 값을
     *  미리 URLEncoder로 인코딩까지 해두면 이중 인코딩되어 order 쪽에서 깨진 문자열로
     *  디코드된다(실제로 재현해서 잡은 버그). UriBuilder.queryParam()은 값을 그대로 받아
     *  한 번만 인코딩하므로 이 메서드들은 전부 그 방식을 쓴다. */
    public void updateMemo(Manager manager, String orderId, String memo) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/orders/{id}/memo")
                            .queryParam("memo", memo == null ? "" : memo).build(orderId))
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "메모 저장에 실패했습니다."));
        }
    }

    public void registerInvoice(Manager manager, String orderId, String carrierCode, String invoiceNo) {
        try {
            restClient.post().uri("/api/admin/orders/{id}/invoice?carrierCode={cc}&invoiceNo={inv}",
                            orderId, carrierCode, invoiceNo)
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "송장 등록에 실패했습니다."));
        }
    }

    public void updateDeliveryStatus(Manager manager, String orderId, String deliveryStatus) {
        try {
            restClient.post().uri("/api/admin/orders/{id}/delivery-status?deliveryStatus={ds}", orderId, deliveryStatus)
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "배송상태 변경에 실패했습니다."));
        }
    }

    /** 체크박스 일괄 배송상태변경 - 성공/실패 결과를 orderId별로 돌려받는다(부분 실패 허용). */
    public Map<String, String> bulkUpdateDeliveryStatus(Manager manager, List<String> orderIds, String deliveryStatus) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            orderIds.forEach(id -> body.add("orderIds", id));
            body.add("deliveryStatus", deliveryStatus);
            Map<String, String> result = restClient.post().uri("/api/admin/orders/bulk-delivery-status")
                    .headers(h -> applyHeaders(h, manager))
                    .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body).retrieve().body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            return result != null ? result : Map.of();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "일괄 상태변경에 실패했습니다."));
        }
    }

    public byte[] export(Manager manager, String locgovCode, String orderStatus, String searchType,
                          String keyword, String startDate, String endDate, String reason) {
        try {
            ResponseEntity<byte[]> response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/orders/export")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("orderStatus", java.util.Optional.ofNullable(blankToNull(orderStatus)))
                            .queryParamIfPresent("searchType", java.util.Optional.ofNullable(blankToNull(searchType)))
                            .queryParamIfPresent("keyword", java.util.Optional.ofNullable(blankToNull(keyword)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .queryParam("reason", reason)
                            .build())
                    .headers(h -> applyHeaders(h, manager))
                    .retrieve().toEntity(byte[].class);
            return response.getBody();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "다운로드에 실패했습니다."));
        }
    }

    // ---- 클레임 처리 큐 ----

    public List<ClaimQueueRow> claimQueue(Manager manager, String status) {
        try {
            List<ClaimQueueRow> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/claims")
                            .queryParamIfPresent("status", java.util.Optional.ofNullable(blankToNull(status)))
                            .build())
                    .headers(h -> applyHeaders(h, manager)).retrieve()
                    .body(new ParameterizedTypeReference<List<ClaimQueueRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public List<ClaimMemoInfo> claimMemos(Manager manager, Long claimId) {
        try {
            List<ClaimMemoInfo> list = restClient.get().uri("/api/admin/claims/{id}/memos", claimId)
                    .headers(h -> applyHeaders(h, manager)).retrieve()
                    .body(new ParameterizedTypeReference<List<ClaimMemoInfo>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientResponseException e) {
            return List.of();
        }
    }

    public void approveClaim(Manager manager, Long claimId, String memo) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/claims/{id}/approve")
                            .queryParamIfPresent("memo", java.util.Optional.ofNullable(blankToNull(memo))).build(claimId))
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "승인 처리에 실패했습니다."));
        }
    }

    public void rejectClaim(Manager manager, Long claimId, String memo) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/claims/{id}/reject")
                            .queryParamIfPresent("memo", java.util.Optional.ofNullable(blankToNull(memo))).build(claimId))
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "거절 처리에 실패했습니다."));
        }
    }

    public void completeClaim(Manager manager, Long claimId, String memo) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/claims/{id}/complete")
                            .queryParamIfPresent("memo", java.util.Optional.ofNullable(blankToNull(memo))).build(claimId))
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "처리완료에 실패했습니다."));
        }
    }

    public void addClaimMemo(Manager manager, Long claimId, String memo) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/admin/claims/{id}/memo")
                            .queryParam("memo", memo == null ? "" : memo).build(claimId))
                    .headers(h -> applyHeaders(h, manager)).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "메모 등록에 실패했습니다."));
        }
    }

    private static String extractMessage(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record OrderDetail(String orderId, Long userId, Long itemId, Long sellerId, String itemName,
                               String locgovCode, Integer quantity, Integer unitPrice, Long pointAmount,
                               String orderStatus, String cancelReason, String createdDate, String updatedDate,
                               String carrierCode, String invoiceNo, String deliveryStatus, String receiverName,
                               String receiverPhone, String deliveryAddress, String deliveryAddressDetail,
                               String requestNote, String adminMemo) {
    }

    public record ClaimInfo(Long claimId, String orderId, String claimType, String reason, String status,
                             String createdDate, String processedDate) {
    }

    public record ClaimQueueRow(Long claimId, String orderId, String claimType, String reason, String status,
                                 String createdDate, String processedDate, String itemName, String locgovCode,
                                 Long userId, String receiverName) {
    }

    public record ClaimMemoInfo(Long claimMemoId, Long managerId, String managerName, String memo, String createdDate) {
    }

    public record SearchResult(List<OrderDetail> content, long totalElements, int totalPages, int page, int size) {
    }

    private record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
    }
}
