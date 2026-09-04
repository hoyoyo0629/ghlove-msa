package com.ghlove.order.web;

import com.ghlove.order.domain.Order;
import com.ghlove.order.service.ClaimService;
import com.ghlove.order.service.JwtVerifier;
import com.ghlove.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.function.Function;

/** storefront(Vue3 SPA)용 마이페이지 "취소반품교환" JSON API - {@link ClaimController}(Thymeleaf)와
 *  완전히 같은 로직을 감싼다. */
@RestController
@RequiredArgsConstructor
public class ClaimMyApiController {

    private final ClaimService claimService;
    private final OrderService orderService;
    private final JwtVerifier jwtVerifier;

    public record ClaimRowDto(Long claimId, String orderId, String itemName, String claimType, String claimTypeLabel,
                               String status, String statusLabel, java.time.LocalDateTime createdDate) {
    }

    @GetMapping("/api/claims/my")
    public ResponseEntity<?> myClaims(@RequestParam(required = false) String searchStartDate,
                                       @RequestParam(required = false) String searchEndDate,
                                       @RequestParam(required = false) String itemName,
                                       HttpServletRequest request) {
        var userId = jwtVerifier.currentUserId(request);
        if (userId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        var claims = claimService.myClaims(userId.get());
        Map<String, Order> ordersById = orderService.ordersOf(claims.stream().map(c -> c.getOrderId()).distinct().toList())
                .stream().collect(java.util.stream.Collectors.toMap(Order::getOrderId, Function.identity()));

        Map<String, String> claimTypeLabels = orderService.codesOf("CLAIM_TYPE");
        Map<String, String> claimStatusLabels = orderService.codesOf("CLAIM_STATUS");

        LocalDate start = parseDate(searchStartDate);
        LocalDate end = parseDate(searchEndDate);
        var rows = claims.stream()
                .filter(c -> start == null || !c.getCreatedDate().toLocalDate().isBefore(start))
                .filter(c -> end == null || !c.getCreatedDate().toLocalDate().isAfter(end))
                .filter(c -> {
                    if (itemName == null || itemName.isBlank()) {
                        return true;
                    }
                    Order o = ordersById.get(c.getOrderId());
                    return o != null && o.getItemName() != null && o.getItemName().contains(itemName);
                })
                .map(c -> new ClaimRowDto(c.getClaimId(), c.getOrderId(),
                        ordersById.containsKey(c.getOrderId()) ? ordersById.get(c.getOrderId()).getItemName() : "-",
                        c.getClaimType(), claimTypeLabels.getOrDefault(c.getClaimType(), c.getClaimType()),
                        c.getStatus(), claimStatusLabels.getOrDefault(c.getStatus(), c.getStatus()), c.getCreatedDate()))
                .toList();
        return ResponseEntity.ok(rows);
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
