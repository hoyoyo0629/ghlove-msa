package com.ghlove.point.web;

import com.ghlove.point.domain.PointLedger;
import com.ghlove.point.domain.PointReservation;
import com.ghlove.point.service.JwtVerifier;
import com.ghlove.point.service.LocgovClient;
import com.ghlove.point.service.MemberClient;
import com.ghlove.point.service.PointException;
import com.ghlove.point.service.PointService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** storefront(Vue3 SPA)용 "기부포인트 조회" JSON API - {@link PointController}(Thymeleaf)의
 * `/` GET과 완전히 같은 조합. */
@RestController
@RequiredArgsConstructor
public class PointMyApiController {

    private final PointService pointService;
    private final LocgovClient locgovClient;
    private final MemberClient memberClient;
    private final JwtVerifier jwtVerifier;

    public record LedgerRowDto(String txnType, String txnTypeLabel, long pointAmount, String reason,
                                String refKey, String expirationDate, String createdDate) {
        static LedgerRowDto of(PointLedger l, Map<String, String> labels) {
            return new LedgerRowDto(l.getTxnType(), labels.get(l.getTxnType()), l.getPointAmount(), l.getReason(),
                    l.getRefKey(), l.getExpirationDate(), l.getCreatedDate() != null ? l.getCreatedDate().toString() : null);
        }
    }

    public record ExpiringLotDto(long remainingAmount, String expirationDate, String locgovCode) {
    }

    public record LocgovSummaryDto(String locgovCode, String upperLocgovNm, String locgovNm,
                                    long earned, long used, long remaining) {
    }

    public record MyPointResponse(String userName, long balance, long reservedAmount, long availableBalance,
                                   long totalEarned, long totalUsed, List<LedgerRowDto> ledger,
                                   List<ExpiringLotDto> upcomingExpirations, List<LocgovSummaryDto> locgovSummary) {
    }

    @GetMapping("/api/my/points")
    public ResponseEntity<MyPointResponse> myPoints(HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = authUserId.get();
        var member = memberClient.fetchOrNull(userId);
        Map<String, String> txnTypeLabels = pointService.codesOf("PT_TXN_TYPE");

        Map<String, LocgovClient.LocgovInfo> locgovsByCode = new LinkedHashMap<>();
        locgovClient.allLocgovs().forEach(l -> locgovsByCode.put(l.locgovCode(), l));
        List<LocgovSummaryDto> locgovSummary = pointService.ledgerSummaryByLocgov(userId).stream()
                .map(s -> {
                    LocgovClient.LocgovInfo l = locgovsByCode.get(s.locgovCode());
                    return new LocgovSummaryDto(s.locgovCode(), l != null ? l.upperLocgovNm() : null,
                            l != null ? l.locgovNm() : s.locgovCode(), s.earned(), s.used(), s.remaining());
                })
                .toList();

        return ResponseEntity.ok(new MyPointResponse(
                member != null ? member.userName() : null,
                pointService.balanceOf(userId), pointService.reservedAmountOf(userId),
                pointService.availableBalanceOf(userId), pointService.totalEarnedOf(userId),
                pointService.totalUsedOf(userId),
                pointService.ledgerOf(userId).stream().map(l -> LedgerRowDto.of(l, txnTypeLabels)).toList(),
                pointService.upcomingExpirations(userId).stream()
                        .map(l -> new ExpiringLotDto(l.getRemainingAmount(), l.getExpirationDate(), l.getLocgovCode()))
                        .toList(),
                locgovSummary));
    }

    public record ReservationDto(Long reservationId, long amount, String refKey, String reason,
                                  String status, String statusLabel, String createdDate) {
        static ReservationDto of(PointReservation r, Map<String, String> labels) {
            return new ReservationDto(r.getReservationId(), r.getAmount(), r.getRefKey(), r.getReason(),
                    r.getStatus(), labels.get(r.getStatus()), r.getCreatedDate() != null ? r.getCreatedDate().toString() : null);
        }
    }

    public record ReservationsResponse(long availableBalance, List<ReservationDto> reservations) {
    }

    /** {@link PointController}(Thymeleaf) `/reservations` GET과 동일 조합. */
    @GetMapping("/api/my/reservations")
    public ResponseEntity<ReservationsResponse> myReservations(HttpServletRequest request) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        Long userId = authUserId.get();
        Map<String, String> statusLabels = pointService.codesOf("PT_RESERVATION_STATUS");
        return ResponseEntity.ok(new ReservationsResponse(
                pointService.availableBalanceOf(userId),
                pointService.reservationsOf(userId).stream().map(r -> ReservationDto.of(r, statusLabels)).toList()));
    }

    public record ReserveRequest(long amount, String refKey, String reason) {
    }

    @PostMapping("/api/my/reservations")
    public ResponseEntity<?> reserve(HttpServletRequest request, @RequestBody ReserveRequest body) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        try {
            pointService.reserve(authUserId.get(), body.amount(), body.refKey(), body.reason());
        } catch (PointException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/my/reservations/{id}/confirm")
    public ResponseEntity<?> confirmReservation(HttpServletRequest request, @PathVariable Long id) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        if (!pointService.isReservationOwner(id, authUserId.get())) {
            return ResponseEntity.status(404).build();
        }
        try {
            pointService.confirmReservation(id);
        } catch (PointException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/my/reservations/{id}/release")
    public ResponseEntity<?> releaseReservation(HttpServletRequest request, @PathVariable Long id) {
        var authUserId = jwtVerifier.currentUserId(request);
        if (authUserId.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        if (!pointService.isReservationOwner(id, authUserId.get())) {
            return ResponseEntity.status(404).build();
        }
        try {
            pointService.releaseReservation(id);
        } catch (PointException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.noContent().build();
    }
}
