package com.ghlove.order.service;

import com.ghlove.order.domain.Claim;
import com.ghlove.order.domain.ClaimMemo;
import com.ghlove.order.domain.ExcelDownloadLog;
import com.ghlove.order.domain.Order;
import com.ghlove.order.repository.ClaimMemoRepository;
import com.ghlove.order.repository.ClaimRepository;
import com.ghlove.order.repository.ExcelDownloadLogRepository;
import com.ghlove.order.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * admin 주문관리 콘솔(AS-IS opmanager/order - OrderManagerController)의 검색/상세/상태변경/
 * 클레임처리큐/엑셀다운로드를 담당한다. 실제 상태전이(송장등록/배송상태갱신/클레임승인 등)는
 * 기존 OrderService/ClaimService 메서드를 그대로 재사용하고(SAGA 로직을 이 라운드에서
 * 새로 건드리지 않는다), 이 서비스는 그 위에 검색/조회범위(RBAC)/일괄처리/메모 이력을 얹는다.
 */
@Service
@RequiredArgsConstructor
public class OrderAdminService {

    private final OrderRepository orderRepository;
    private final ClaimRepository claimRepository;
    private final ClaimMemoRepository claimMemoRepository;
    private final ExcelDownloadLogRepository excelDownloadLogRepository;
    private final OrderService orderService;
    private final ClaimService claimService;

    /**
     * 통합 검색. searchType/keyword는 AS-IS "검색구분" 셀렉트+검색어 입력 한 쌍을 재현한다.
     * 주문번호/수취인명/답례품명은 OD_ORDER에 이미 있는 값을 그대로 검색하고, 판매자명/
     * 주문자명은 이 서비스가 이름을 갖고 있지 않아(각각 gift/member 서비스 소유 데이터라
     * cross-service 이름검색 API가 필요한데, 이 라운드는 동시에 진행 중인 다른 작업과의
     * 파일 충돌을 피하려 gift/member 서비스에는 손대지 않기로 했다) 대신 판매자ID/주문자ID
     * 정확매칭으로 대체한다 - admin 쪽에서 필요하면 이름->ID를 먼저 조회해 넘기면 된다.
     */
    public Page<Order> search(String locgovCode, String orderStatus, String searchType, String keyword,
                               LocalDate startDate, LocalDate endDate, int page, int size) {
        Specification<Order> spec = buildSpec(locgovCode, orderStatus, searchType, keyword, startDate, endDate);
        return orderRepository.findAll(spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate")));
    }

    private Specification<Order> buildSpec(String locgovCode, String orderStatus, String searchType, String keyword,
                                            LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (notBlank(locgovCode)) {
                predicates.add(cb.equal(root.get("locgovCode"), locgovCode));
            }
            if (notBlank(orderStatus)) {
                predicates.add(cb.equal(root.get("orderStatus"), orderStatus));
            }
            if (notBlank(searchType) && notBlank(keyword)) {
                switch (searchType) {
                    case "ORDER_ID" -> predicates.add(cb.like(root.get("orderId"), "%" + keyword + "%"));
                    case "RECEIVER_NAME" -> predicates.add(cb.like(root.get("receiverName"), "%" + keyword + "%"));
                    case "ITEM_NAME" -> predicates.add(cb.like(root.get("itemName"), "%" + keyword + "%"));
                    case "SELLER_ID" -> predicates.add(cb.equal(root.get("sellerId"), parseLongOrImpossible(keyword)));
                    case "USER_ID" -> predicates.add(cb.equal(root.get("userId"), parseLongOrImpossible(keyword)));
                    default -> {
                        // 알 수 없는 검색구분은 무시 - 신규 검색구분을 추가할 때 기존 화면을
                        // 막지 않기 위한 fail-open (MenuService의 동일 원칙).
                    }
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

    /** 파싱 실패 시 절대 매칭되지 않는 값(-1)을 써서 "조건은 있지만 결과 없음"으로 안전하게 처리한다. */
    private Long parseLongOrImpossible(String value) {
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public Order detail(String orderId) {
        return orderService.detail(orderId);
    }

    /** OrderService.registerInvoice/updateDeliveryStatus 그대로 위임 - 기존 SAGA 이벤트 발행
     *  로직(orderSagaPublisher.publishDeliveryUpdated)을 이 라운드에서 새로 건드리지 않기 위해
     *  래핑만 한다. 지자체 스코프 검증은 컨트롤러가 이 메서드 호출 전에 assertLocgovAllowed로 한다. */
    @Transactional
    public Order registerInvoiceDelegate(String orderId, String carrierCode, String invoiceNo) {
        return orderService.registerInvoice(orderId, carrierCode, invoiceNo);
    }

    @Transactional
    public Order updateDeliveryStatusDelegate(String orderId, String deliveryStatus) {
        return orderService.updateDeliveryStatus(orderId, deliveryStatus);
    }

    @Transactional
    public Order updateMemo(String orderId, String memo) {
        Order order = orderService.detail(orderId);
        order.setAdminMemo(memo);
        order.setUpdatedDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    /** 지자체담당자(ROLE_ADMIN_5/6)는 자기 지자체 답례품 주문만 처리할 수 있다 - admin이
     *  보낸 X-Manager-Locgov-Code 헤더가 있는데 그 주문의 지자체와 다르면 거부한다.
     *  시스템/행안부담당자(헤더값 없음)는 제한 없음. */
    public void assertLocgovAllowed(Order order, String managerLocgovCode) {
        if (managerLocgovCode != null && !managerLocgovCode.isBlank()
                && !managerLocgovCode.equals(order.getLocgovCode())) {
            throw new OrderException("소속 지자체의 주문만 처리할 수 있습니다.");
        }
    }

    /** 체크박스 일괄 배송상태변경 - 실패한 건은 건너뛰고 계속 진행, 결과를 orderId별로 모아 돌려준다. */
    @Transactional
    public Map<String, String> bulkUpdateDeliveryStatus(List<String> orderIds, String deliveryStatus,
                                                          String managerLocgovCode) {
        Map<String, String> results = new java.util.LinkedHashMap<>();
        for (String orderId : orderIds) {
            try {
                Order order = orderService.detail(orderId);
                assertLocgovAllowed(order, managerLocgovCode);
                orderService.updateDeliveryStatus(orderId, deliveryStatus);
                results.put(orderId, "OK");
            } catch (OrderException e) {
                results.put(orderId, e.getMessage());
            }
        }
        return results;
    }

    // ==================== 클레임 처리 큐 ====================

    public List<Claim> claimsByStatus(String status) {
        return status == null || status.isBlank()
                ? claimRepository.findAll()
                : claimRepository.findByStatusOrderByClaimIdDesc(status);
    }

    public List<Order> ordersOf(List<String> orderIds) {
        return orderRepository.findByOrderIdIn(orderIds);
    }

    @Transactional
    public Claim approveClaim(Long claimId, String managerLocgovCode, Long managerId, String managerName, String memo) {
        Claim claim = claimService.get(claimId);
        assertLocgovAllowed(orderService.detail(claim.getOrderId()), managerLocgovCode);
        Claim saved = claimService.approve(claimId);
        addMemoIfPresent(claimId, managerId, managerName, memo, "승인 처리");
        return saved;
    }

    @Transactional
    public Claim rejectClaim(Long claimId, String managerLocgovCode, Long managerId, String managerName, String memo) {
        Claim claim = claimService.get(claimId);
        assertLocgovAllowed(orderService.detail(claim.getOrderId()), managerLocgovCode);
        Claim saved = claimService.reject(claimId);
        addMemoIfPresent(claimId, managerId, managerName, memo, "거절 처리");
        return saved;
    }

    @Transactional
    public Claim completeClaim(Long claimId, String managerLocgovCode, Long managerId, String managerName, String memo) {
        Claim claim = claimService.get(claimId);
        assertLocgovAllowed(orderService.detail(claim.getOrderId()), managerLocgovCode);
        Claim saved = claimService.complete(claimId);
        addMemoIfPresent(claimId, managerId, managerName, memo, "처리완료");
        return saved;
    }

    @Transactional
    public ClaimMemo addClaimMemo(Long claimId, Long managerId, String managerName, String memo) {
        if (memo == null || memo.isBlank()) {
            throw new ClaimException("메모 내용을 입력해 주세요.");
        }
        claimService.get(claimId); // 존재 검증
        return saveMemo(claimId, managerId, managerName, memo);
    }

    private void addMemoIfPresent(Long claimId, Long managerId, String managerName, String memo, String fallbackLabel) {
        saveMemo(claimId, managerId, managerName, notBlank(memo) ? memo : fallbackLabel);
    }

    private ClaimMemo saveMemo(Long claimId, Long managerId, String managerName, String memo) {
        ClaimMemo entity = new ClaimMemo();
        entity.setClaimId(claimId);
        entity.setManagerId(managerId);
        entity.setManagerName(managerName);
        entity.setMemo(memo);
        entity.setCreatedDate(LocalDateTime.now());
        return claimMemoRepository.save(entity);
    }

    public List<ClaimMemo> memosOf(Long claimId) {
        return claimMemoRepository.findByClaimIdOrderByCreatedDateDesc(claimId);
    }

    public List<ClaimMemo> memosOf(List<Long> claimIds) {
        return claimIds.isEmpty() ? List.of() : claimMemoRepository.findByClaimIdInOrderByCreatedDateDesc(claimIds);
    }

    // ==================== 엑셀(CSV) 다운로드 ====================

    /** AS-IS opmanager는 개인정보가 포함된 다운로드마다 사유 입력을 강제하고 이력을 남긴다
     *  (개인정보보호법 접근·이용 로그 요건) - 이 프로젝트도 사유 없이는 실행하지 않는다.
     *  Apache POI 등 엑셀 라이브러리는 이 프로젝트에 아직 없어 CSV로 실행한다(엑셀에서
     *  그대로 열리는 실질적으로 동등한 결과물). */
    @Transactional
    public String exportCsv(List<Order> orders, Long managerId, String managerName, String reason, String searchCondition) {
        if (reason == null || reason.isBlank()) {
            throw new OrderException("다운로드 사유를 입력해 주세요.");
        }
        ExcelDownloadLog log = new ExcelDownloadLog();
        log.setManagerId(managerId);
        log.setManagerName(managerName);
        log.setDownloadReason(reason);
        log.setSearchCondition(searchCondition);
        log.setRowCount(orders.size());
        log.setCreatedDate(LocalDateTime.now());
        excelDownloadLogRepository.save(log);

        StringBuilder sb = new StringBuilder("﻿");
        sb.append("주문번호,주문자ID,답례품명,판매자ID,수량,결제포인트,주문상태,배송상태,수취인,연락처,주소,주문일시\n");
        for (Order o : orders) {
            sb.append(csv(o.getOrderId())).append(',')
                    .append(csv(String.valueOf(o.getUserId()))).append(',')
                    .append(csv(o.getItemName())).append(',')
                    .append(csv(String.valueOf(o.getSellerId()))).append(',')
                    .append(csv(String.valueOf(o.getQuantity()))).append(',')
                    .append(csv(String.valueOf(o.getPointAmount()))).append(',')
                    .append(csv(o.getOrderStatus())).append(',')
                    .append(csv(o.getDeliveryStatus())).append(',')
                    .append(csv(o.getReceiverName())).append(',')
                    .append(csv(o.getReceiverPhone())).append(',')
                    .append(csv((o.getDeliveryAddress() == null ? "" : o.getDeliveryAddress()) + " "
                            + (o.getDeliveryAddressDetail() == null ? "" : o.getDeliveryAddressDetail()))).append(',')
                    .append(csv(String.valueOf(o.getCreatedDate())))
                    .append('\n');
        }
        return sb.toString();
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
