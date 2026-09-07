package com.ghlove.admin.service;

import com.ghlove.admin.domain.ClaimLedger;
import com.ghlove.admin.domain.OrderLedger;
import com.ghlove.admin.repository.ClaimLedgerRepository;
import com.ghlove.admin.repository.OrderLedgerRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SFR-006 재검토 라운드 - ISP p.161 "주문상세/이력뷰"·"클레임사건/상태뷰" ReadModel gap
 * fill. admin 주문목록/클레임 큐 화면의 검색·조회는 이제 매 요청 order 서비스에 REST로
 * 묻는 대신(live pass-through) 이 서비스가 로컬 사본(OrderLedger/ClaimLedger, order.saga
 * 이벤트로 동기화)을 조회한다 - ISP가 명시한 "Query는 ReadModel로, Command(상세보기 이후의
 * 메모작성·상태변경 등 쓰기)는 원장으로"라는 CQRS 원칙을 그대로 따른 것. 상세 단건 조회·
 * 쓰기는 여전히 {@link OrderAdminClient}(원장 REST)를 그대로 쓴다 - 단건 조회는애초에
 * ReadModel이 해결하려는 "검색 성능" 문제와 무관하다.
 *
 * 검색 조건(searchType/keyword/orderStatus/날짜범위)은 order 서비스
 * {@code OrderAdminService.buildSpec}과 동일한 의미로 맞췄다 - 화면/API 계약을 바꾸지 않기
 * 위해서다.
 */
@Service
@RequiredArgsConstructor
public class OrderReadModelService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final OrderLedgerRepository orderLedgerRepository;
    private final ClaimLedgerRepository claimLedgerRepository;

    public record OrderListRow(String orderId, Long userId, String itemName, Long sellerId, Integer quantity,
                                Long pointAmount, String orderStatus, String deliveryStatus, String createdDate) {
        static OrderListRow of(OrderLedger l) {
            return new OrderListRow(l.getOrderId(), l.getUserId(), l.getItemName(), l.getSellerId(), l.getQuantity(),
                    l.getPointAmount(), l.getStatus(), l.getDeliveryStatus(), format(l.getCreatedDate()));
        }
    }

    public record SearchResult(List<OrderListRow> content, long totalElements, int totalPages, int page, int size) {
    }

    public SearchResult search(String locgovCode, String orderStatus, String searchType, String keyword,
                                String startDate, String endDate, int page, int size) {
        Specification<OrderLedger> spec = buildOrderSpec(locgovCode, orderStatus, searchType, keyword,
                parseDate(startDate), parseDate(endDate));
        Page<OrderLedger> result = orderLedgerRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate")));
        return new SearchResult(result.getContent().stream().map(OrderListRow::of).toList(),
                result.getTotalElements(), result.getTotalPages(), page, size);
    }

    private Specification<OrderLedger> buildOrderSpec(String locgovCode, String orderStatus, String searchType,
                                                        String keyword, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (notBlank(locgovCode)) {
                predicates.add(cb.equal(root.get("locgovCode"), locgovCode));
            }
            if (notBlank(orderStatus)) {
                predicates.add(cb.equal(root.get("status"), orderStatus));
            }
            if (notBlank(searchType) && notBlank(keyword)) {
                switch (searchType) {
                    case "ORDER_ID" -> predicates.add(cb.like(root.get("orderId"), "%" + keyword + "%"));
                    case "RECEIVER_NAME" -> predicates.add(cb.like(root.get("receiverName"), "%" + keyword + "%"));
                    case "ITEM_NAME" -> predicates.add(cb.like(root.get("itemName"), "%" + keyword + "%"));
                    case "SELLER_ID" -> predicates.add(cb.equal(root.get("sellerId"), parseLongOrImpossible(keyword)));
                    case "USER_ID" -> predicates.add(cb.equal(root.get("userId"), parseLongOrImpossible(keyword)));
                    default -> { /* fail-open, 신규 검색구분은 order 서비스의 agency-search 등 별도 경로로 유지 */ }
                }
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), startDate.atStartOfDay()));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("createdDate"), endDate.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public List<OrderAdminClient.ClaimQueueRow> claimQueue(String locgovCode, String status) {
        Specification<ClaimLedger> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (notBlank(locgovCode)) {
                predicates.add(cb.equal(root.get("locgovCode"), locgovCode));
            }
            if (notBlank(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return claimLedgerRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(ClaimLedger::getClaimId).reversed())
                .map(c -> new OrderAdminClient.ClaimQueueRow(c.getClaimId(), c.getOrderId(), c.getClaimType(),
                        c.getReason(), c.getStatus(), format(c.getCreatedDate()), format(c.getProcessedDate()),
                        c.getItemName(), c.getLocgovCode(), c.getUserId(), c.getReceiverName()))
                .toList();
    }

    private static String format(java.time.LocalDateTime dt) {
        return dt == null ? null : dt.format(DATE_FORMAT);
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Long parseLongOrImpossible(String value) {
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
