package com.ghlove.order.web;

import com.ghlove.order.config.AdminApiAuthInterceptor;
import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.ClaimMemo;
import com.ghlove.order.domain.Order;
import com.ghlove.order.service.ClaimException;
import com.ghlove.order.service.OrderAdminService;
import com.ghlove.order.service.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * admin 클레임(반품/교환) 처리 큐 - AS-IS opmanager/order의 클레임 승인/거절 화면 재현.
 * AdminApiAuthInterceptor로 게이트된다(WebConfig 참고). 지자체 스코프는 클레임이 걸린
 * 원 주문의 LOCGOV_CODE로 판정한다(OrderAdminService.assertLocgovAllowed).
 */
@RestController
@RequestMapping("/api/admin/claims")
@RequiredArgsConstructor
public class ClaimAdminApiController {

    private final OrderAdminService orderAdminService;

    @GetMapping
    public List<ClaimQueueDto> queue(@RequestParam(required = false) String status,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode) {
        List<Claim> claims = orderAdminService.claimsByStatus(status);
        List<String> orderIds = claims.stream().map(Claim::getOrderId).distinct().toList();
        Map<String, Order> ordersById = orderAdminService.ordersOf(orderIds).stream()
                .collect(Collectors.toMap(Order::getOrderId, Function.identity(), (a, b) -> a));

        List<Claim> scoped = (managerLocgovCode == null || managerLocgovCode.isBlank())
                ? claims
                : claims.stream()
                        .filter(c -> {
                            Order o = ordersById.get(c.getOrderId());
                            return o != null && managerLocgovCode.equals(o.getLocgovCode());
                        })
                        .toList();

        return scoped.stream()
                .sorted((a, b) -> b.getClaimId().compareTo(a.getClaimId()))
                .map(c -> ClaimQueueDto.of(c, ordersById.get(c.getOrderId())))
                .toList();
    }

    @GetMapping("/{claimId}/memos")
    public List<ClaimMemoDto> memos(@PathVariable Long claimId) {
        return orderAdminService.memosOf(claimId).stream().map(ClaimMemoDto::from).toList();
    }

    @PostMapping("/{claimId}/approve")
    public ResponseEntity<?> approve(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_NAME, required = false) String managerNameEncoded) {
        try {
            orderAdminService.approveClaim(claimId, managerLocgovCode, managerId, decode(managerNameEncoded), memo);
            return ResponseEntity.noContent().build();
        } catch (ClaimException | OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{claimId}/reject")
    public ResponseEntity<?> reject(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                                     @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode,
                                     @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId,
                                     @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_NAME, required = false) String managerNameEncoded) {
        try {
            orderAdminService.rejectClaim(claimId, managerLocgovCode, managerId, decode(managerNameEncoded), memo);
            return ResponseEntity.noContent().build();
        } catch (ClaimException | OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{claimId}/complete")
    public ResponseEntity<?> complete(@PathVariable Long claimId, @RequestParam(required = false) String memo,
                                       @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_LOCGOV, required = false) String managerLocgovCode,
                                       @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId,
                                       @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_NAME, required = false) String managerNameEncoded) {
        try {
            orderAdminService.completeClaim(claimId, managerLocgovCode, managerId, decode(managerNameEncoded), memo);
            return ResponseEntity.noContent().build();
        } catch (ClaimException | OrderException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{claimId}/memo")
    public ResponseEntity<?> addMemo(@PathVariable Long claimId, @RequestParam String memo,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_ID, required = false) Long managerId,
                                      @RequestHeader(value = AdminApiAuthInterceptor.HEADER_MANAGER_NAME, required = false) String managerNameEncoded) {
        try {
            orderAdminService.addClaimMemo(claimId, managerId, decode(managerNameEncoded), memo);
            return ResponseEntity.noContent().build();
        } catch (ClaimException e) {
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

    public record ClaimQueueDto(Long claimId, String orderId, String claimType, String reason, String status,
                                 String createdDate, String processedDate, String itemName, String locgovCode,
                                 Long userId, String receiverName) {
        static ClaimQueueDto of(Claim c, Order o) {
            return new ClaimQueueDto(c.getClaimId(), c.getOrderId(), c.getClaimType(), c.getReason(), c.getStatus(),
                    str(c.getCreatedDate()), str(c.getProcessedDate()),
                    o == null ? null : o.getItemName(), o == null ? null : o.getLocgovCode(),
                    o == null ? null : o.getUserId(), o == null ? null : o.getReceiverName());
        }

        private static String str(Object o) {
            return o == null ? null : o.toString();
        }
    }

    public record ClaimMemoDto(Long claimMemoId, Long managerId, String managerName, String memo, String createdDate) {
        static ClaimMemoDto from(ClaimMemo m) {
            return new ClaimMemoDto(m.getClaimMemoId(), m.getManagerId(), m.getManagerName(), m.getMemo(),
                    m.getCreatedDate() == null ? null : m.getCreatedDate().toString());
        }
    }
}
