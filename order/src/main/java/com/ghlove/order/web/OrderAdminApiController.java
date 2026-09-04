package com.ghlove.order.web;

import com.ghlove.order.config.AdminApiAuthInterceptor;
import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.ClaimMemo;
import com.ghlove.order.domain.Order;
import com.ghlove.order.service.ClaimException;
import com.ghlove.order.service.OrderAdminService;
import com.ghlove.order.service.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 전용 주문/클레임 관리 API - AdminApiAuthInterceptor(공유시크릿)로 게이트된다
 * (WebConfig 참고). 관리자 신원은 admin이 실어보내는 커스텀 헤더로 전달받는다: 이 서비스는
 * 세션이 없으므로 요청마다 그 헤더를 신뢰하되, 그 신뢰는 인터셉터가 이미 검증한 공유시크릿을
 * 전제로 한다 - 시크릿 검증을 통과하지 못하면 이 컨트롤러 메서드는 아예 실행되지 않는다.
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class OrderAdminApiController {

    private final OrderAdminService orderAdminService;

    @GetMapping
    public PageResponse<OrderAdminDto> search(@RequestParam(required = false) String locgovCode,
                                               @RequestParam(required = false) String orderStatus,
                                               @RequestParam(required = false) String searchType,
                                               @RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String startDate,
                                               @RequestParam(required = false) String endDate,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        Page<Order> result = orderAdminService.search(locgovCode, orderStatus, searchType, keyword,
                parseDate(startDate), parseDate(endDate), page, size);
        return new PageResponse<>(result.map(OrderAdminDto::from).getContent(),
                result.getTotalElements(), result.getTotalPages(), page, size);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> detail(@PathVariable String orderId) {
        try {
            Order order = orderAdminService.detail(orderId);
            return ResponseEntity.ok(OrderAdminDto.from(order));
        } catch (OrderException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }

    private List<Claim> claimsOf(String orderId) {
        return orderAdminService.claimsByStatus(null).stream()
                .filter(c -> c.getOrderId().equals(orderId)).toList();
    }

    @GetMapping("/{orderId}/claims")
    public List<ClaimAdminDto> claimsForOrder(@PathVariable String orderId) {
        return claimsOf(orderId).stream().map(ClaimAdminDto::from).toList();
    }

    @PostMapping("/{orderId}/memo")
    public ResponseEntity<?> updateMemo(@PathVariable String orderId, @RequestParam String memo) {
        try {
            orderAdminService.updateMemo(orderId, memo);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/invoice")
    public ResponseEntity<?> registerInvoice(@PathVariable String orderId,
                                              @RequestParam String carrierCode, @RequestParam String invoiceNo,
                                              @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode) {
        try {
            orderAdminService.assertLocgovAllowed(orderAdminService.detail(orderId), managerLocgovCode);
            orderAdminService.registerInvoiceDelegate(orderId, carrierCode, invoiceNo);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/delivery-status")
    public ResponseEntity<?> updateDeliveryStatus(@PathVariable String orderId, @RequestParam String deliveryStatus,
                                                   @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode) {
        try {
            orderAdminService.assertLocgovAllowed(orderAdminService.detail(orderId), managerLocgovCode);
            orderAdminService.updateDeliveryStatusDelegate(orderId, deliveryStatus);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/bulk-delivery-status")
    public Map<String, String> bulkDeliveryStatus(@RequestParam List<String> orderIds,
                                                    @RequestParam String deliveryStatus,
                                                    @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode) {
        return orderAdminService.bulkUpdateDeliveryStatus(orderIds, deliveryStatus, managerLocgovCode);
    }

    /** 엑셀(CSV) 다운로드 - 사유 필수. 검색조건은 그대로 재사용해 결과셋을 만든다. */
    @GetMapping("/export")
    public ResponseEntity<?> export(@RequestParam(required = false) String locgovCode,
                                     @RequestParam(required = false) String orderStatus,
                                     @RequestParam(required = false) String searchType,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam String reason,
                                     @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId,
                                     @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_NAME, required = false) String managerNameEncoded) {
        try {
            Page<Order> result = orderAdminService.search(locgovCode, orderStatus, searchType, keyword,
                    parseDate(startDate), parseDate(endDate), 0, 5000);
            String condition = "locgovCode=" + locgovCode + "&orderStatus=" + orderStatus
                    + "&searchType=" + searchType + "&keyword=" + keyword;
            String csv = orderAdminService.exportCsv(result.getContent(), managerId, decode(managerNameEncoded), reason, condition);
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv; charset=UTF-8"));
            headers.setContentDispositionFormData("attachment", "orders.csv");
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String decode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (RuntimeException e) {
            return value;
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            return null;
        }
    }

    public record PageResponse<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
    }

    public record OrderAdminDto(String orderId, Long userId, Long itemId, Long sellerId, String itemName,
                                 String locgovCode, Integer quantity, Integer unitPrice, Long pointAmount,
                                 String orderStatus, String cancelReason, String createdDate, String updatedDate,
                                 String carrierCode, String invoiceNo, String deliveryStatus, String receiverName,
                                 String receiverPhone, String deliveryAddress, String deliveryAddressDetail,
                                 String requestNote, String adminMemo) {
        static OrderAdminDto from(Order o) {
            return new OrderAdminDto(o.getOrderId(), o.getUserId(), o.getItemId(), o.getSellerId(), o.getItemName(),
                    o.getLocgovCode(), o.getQuantity(), o.getUnitPrice(), o.getPointAmount(), o.getOrderStatus(),
                    o.getCancelReason(), str(o.getCreatedDate()), str(o.getUpdatedDate()), o.getCarrierCode(),
                    o.getInvoiceNo(), o.getDeliveryStatus(), o.getReceiverName(), o.getReceiverPhone(),
                    o.getDeliveryAddress(), o.getDeliveryAddressDetail(), o.getRequestNote(), o.getAdminMemo());
        }

        private static String str(Object o) {
            return o == null ? null : o.toString();
        }
    }

    public record ClaimAdminDto(Long claimId, String orderId, String claimType, String reason, String status,
                                 String createdDate, String processedDate) {
        static ClaimAdminDto from(Claim c) {
            return new ClaimAdminDto(c.getClaimId(), c.getOrderId(), c.getClaimType(), c.getReason(), c.getStatus(),
                    c.getCreatedDate() == null ? null : c.getCreatedDate().toString(),
                    c.getProcessedDate() == null ? null : c.getProcessedDate().toString());
        }
    }
}
