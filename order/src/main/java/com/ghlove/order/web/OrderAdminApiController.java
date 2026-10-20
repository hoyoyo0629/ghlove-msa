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
    private final com.ghlove.order.service.OrderDateStatsService orderDateStatsService;
    private final com.ghlove.order.service.OrderLocgovStatsService orderLocgovStatsService;
    private final com.ghlove.order.service.OrderLineListService orderLineListService;
    private final com.ghlove.order.service.OrderDetailService orderDetailService;

    /**
     * 일자/월/년 매출 통계 - AS-IS opmanager/shop-statistics/sales/{day,month,year}.
     * 페이징 없이 조건 전체를 돌려준다(AS-IS도 이 화면엔 페이저가 없다).
     */
    @GetMapping("/stats/date")
    public java.util.List<com.ghlove.order.service.OrderDateStatsService.DateStatRow> dateStats(
            @RequestParam(defaultValue = "day") String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return orderDateStatsService.dateStats(type, startDate, endDate);
    }

    /**
     * 답례품 구매현황 > 지자체별 - AS-IS getDateStatsListGhlove의 LOCGOV/ITEM/USER 분기.
     * 이름(지자체명·상호명·회원명)은 다른 서비스 소유라 admin이 키로 붙인다.
     */
    @GetMapping("/stats/locgov-group")
    public java.util.List<com.ghlove.order.service.OrderLocgovStatsService.GroupRow> locgovGroupStats(
            @RequestParam String extra,
            @RequestParam String startYearMonth,
            @RequestParam String endYearMonth,
            @RequestParam(required = false) String locgovCode,
            @RequestParam(required = false) Long itemId) {
        return switch (extra) {
            case "ITEM" -> orderLocgovStatsService.byItem(startYearMonth, endYearMonth, locgovCode);
            case "ITEM_ALL" -> orderLocgovStatsService.byItemAllLocgovs(startYearMonth, endYearMonth);
            case "USER" -> orderLocgovStatsService.byUser(startYearMonth, endYearMonth, itemId);
            default -> orderLocgovStatsService.byLocgov(startYearMonth, endYearMonth);
        };
    }

    /** 통계 > 답례품 구매현황 > 카테고리별 - AS-IS getCategoriesStatsList의 주문 통계 절반(지자체×품목). */
    @GetMapping("/stats/category-item")
    public java.util.List<com.ghlove.order.service.OrderLocgovStatsService.LocgovItemQtyRow> categoryItemStats(
            @RequestParam String startDate, @RequestParam String endDate) {
        return orderLocgovStatsService.locgovItemQuantityStats(startDate, endDate);
    }

    /** 통계 > 답례품 구매현황 > 농협 사업자 답례품 현황 - AS-IS getNhItemSalesStats의 주문 통계 절반. */
    @GetMapping("/stats/nh-item")
    public java.util.List<com.ghlove.order.service.OrderLocgovStatsService.NhItemSaleRow> nhItemSaleStats(
            @RequestParam String startYearMonth,
            @RequestParam String endYearMonth) {
        return orderLocgovStatsService.nhItemSaleStats(startYearMonth, endYearMonth);
    }

    public record LineSearchRequest(com.ghlove.order.service.OrderLineListService.Search search, int offset, int limit) {
    }

    /**
     * 전체주문관리(AS-IS opmanager/order/list) - 주문 품목 한 줄 단위 목록. 지자체 범위·이름 조건(주문자명·상호명)은 admin이 코드/번호로 풀어 보낸다.
     * 주문상태는 AS-IS 숫자코드로 도출해 돌려준다(OrderLineListService 참고).
     */
    @PostMapping("/lines/search")
    public com.ghlove.order.service.OrderLineListService.LinePage searchLines(@RequestBody LineSearchRequest request) {
        return orderLineListService.search(request.search(), request.offset(), request.limit());
    }

    /**
     * 주문 상세정보(AS-IS opmanager/order/all/order-detail/{출고번호}/{주문번호}) - 그 출고의 품목·배송지·클레임. 담당 지자체 주문이 아니거나 없으면 404.
     */
    @GetMapping("/{orderId}/detail-view")
    public ResponseEntity<?> detailView(@PathVariable String orderId, @RequestParam(defaultValue = "1") int orderSequence,
                                         @RequestParam(required = false) String locgovCode) {
        return orderDetailService.detail(orderId, orderSequence, locgovCode)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "주문 내역이 없습니다.")));
    }

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

    /**
     * 엑셀(CSV) 다운로드 - 검색조건은 그대로 재사용해 결과셋을 만든다.
     * 다운로드 사유는 AS-IS와 같이 관리자 콘솔(admin)이 이 호출 직전에 개인정보 접근로그
     * ({@code OP_PRIVACY_ACCESS_LOG})에 남긴다 - {@code reason}은 하위호환으로만 남겨 둔 값이고
     * 여기서는 쓰지 않는다(2026-10-03, 기록 지점 이전).
     */
    @GetMapping("/export")
    public ResponseEntity<?> export(@RequestParam(required = false) String locgovCode,
                                     @RequestParam(required = false) String orderStatus,
                                     @RequestParam(required = false) String searchType,
                                     @RequestParam(required = false) String keyword,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(required = false) String reason,
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

    // ==================== 주문대행 조회 (콜센터 - 지자체 스코프 우회 전용) ====================
    // AdminApiAuthInterceptor의 공유시크릿 게이트는 그대로 적용된다. locgovCode 스코프 강제는
    // admin 쪽 컨트롤러(시스템관리자만 접근 가능하도록 RBAC)가 담당하고, 여기는 search()와
    // 동일한 조회로직을 의미상 별도 엔드포인트로 노출한다(전체조회 전용이라는 것을 API
    // 경로 자체로 드러내 향후 실수로 지자체스코프를 강제로 붙이는 회귀를 막기 위함).

    @GetMapping("/agency-search")
    public PageResponse<OrderAdminDto> agencySearch(@RequestParam(required = false) String locgovCode,
                                                      @RequestParam(required = false) String orderStatus,
                                                      @RequestParam(required = false) String searchType,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String startDate,
                                                      @RequestParam(required = false) String endDate,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        return search(locgovCode, orderStatus, searchType, keyword, startDate, endDate, page, size);
    }

    // ==================== PG 결제현황 조회 ====================

    @GetMapping("/pay-info")
    public PageResponse<PayInfoDto> payInfo(@RequestParam(required = false) String locgovCode,
                                             @RequestParam(defaultValue = "false") boolean failedOnly,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Page<Order> result = orderAdminService.payInfoSearch(locgovCode, failedOnly, parseDate(startDate), parseDate(endDate), page, size);
        return new PageResponse<>(result.map(PayInfoDto::from).getContent(),
                result.getTotalElements(), result.getTotalPages(), page, size);
    }

    // ==================== 보류주문 처리 ====================

    @GetMapping("/held")
    public PageResponse<OrderAdminDto> held(@RequestParam(required = false) String locgovCode,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        Page<Order> result = orderAdminService.heldOrders(locgovCode, page, size);
        return new PageResponse<>(result.map(OrderAdminDto::from).getContent(),
                result.getTotalElements(), result.getTotalPages(), page, size);
    }

    @PostMapping("/{orderId}/hold")
    public ResponseEntity<?> hold(@PathVariable String orderId, @RequestParam String reason,
                                   @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode,
                                   @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId) {
        try {
            orderAdminService.hold(orderId, reason, managerLocgovCode, managerId);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{orderId}/release")
    public ResponseEntity<?> release(@PathVariable String orderId,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode) {
        try {
            orderAdminService.release(orderId, managerLocgovCode);
            return ResponseEntity.noContent().build();
        } catch (OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ==================== 관리자 주문 수기 등록 ====================

    @PostMapping("/manual")
    public ResponseEntity<?> createManual(@RequestBody OrderAdminService.ManualOrderRequest request) {
        try {
            String orderId = orderAdminService.createManualOrder(request);
            return ResponseEntity.ok(Map.of("orderId", orderId));
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
                                 String requestNote, String adminMemo, String holdYn, String holdReason,
                                 String holdDate) {
        static OrderAdminDto from(Order o) {
            return new OrderAdminDto(o.getOrderId(), o.getUserId(), o.getItemId(), o.getSellerId(), o.getItemName(),
                    o.getLocgovCode(), o.getQuantity(), o.getUnitPrice(), o.getPointAmount(), o.getOrderStatus(),
                    o.getCancelReason(), str(o.getCreatedDate()), str(o.getUpdatedDate()), o.getCarrierCode(),
                    o.getInvoiceNo(), o.getDeliveryStatus(), o.getReceiverName(), o.getReceiverPhone(),
                    o.getDeliveryAddress(), o.getDeliveryAddressDetail(), o.getRequestNote(), o.getAdminMemo(),
                    o.getHoldYn(), o.getHoldReason(), str(o.getHoldDate()));
        }

        private static String str(Object o) {
            return o == null ? null : o.toString();
        }
    }

    /** PG 결제현황 조회용 DTO - 이 플랫폼엔 실제 PG 연동이 없어(신용카드 등) 결제수단은 항상
     *  "포인트" 고정값이다. paymentStatus는 orderStatus를 사람이 읽는 결제 관점 라벨로 옮긴 것. */
    public record PayInfoDto(String orderId, Long userId, String itemName, Long sellerId, String paymentMethod,
                              Long paymentAmount, String paymentStatus, String orderStatus, String cancelReason,
                              String createdDate, String updatedDate) {
        static PayInfoDto from(Order o) {
            long amount = (o.getPointAmount() == null ? 0L : o.getPointAmount());
            String status = switch (o.getOrderStatus()) {
                case "PENDING" -> "결제대기";
                case "CONFIRMED" -> "결제완료";
                case "CANCELLED" -> (o.getCancelReason() != null && o.getCancelReason().startsWith("고객 요청"))
                        ? "결제취소(고객요청)" : "결제취소(처리실패)";
                default -> o.getOrderStatus();
            };
            return new PayInfoDto(o.getOrderId(), o.getUserId(), o.getItemName(), o.getSellerId(), "포인트",
                    amount, status, o.getOrderStatus(), o.getCancelReason(),
                    str(o.getCreatedDate()), str(o.getUpdatedDate()));
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
