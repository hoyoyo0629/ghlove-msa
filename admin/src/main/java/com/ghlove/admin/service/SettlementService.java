package com.ghlove.admin.service;

import com.ghlove.admin.domain.OrderLedger;
import com.ghlove.admin.domain.Settlement;
import com.ghlove.admin.repository.OrderLedgerRepository;
import com.ghlove.admin.repository.SettlementRepository;
import com.ghlove.admin.service.integration.NotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 정산 프로세스 관리 (SFR-007, 포인트 기준). 확정 주문(STAT_ORDER_LEDGER, feature 3
 * 통계에서 이미 구축한 ReadModel)을 제공자별로 묶어 정산내역을 생성하고, 생성 →
 * 세금계산서발행 → 입금확인 → 마감 4단계를 순서대로만 진행할 수 있게 한다.
 */
@Service
@RequiredArgsConstructor
public class SettlementService {

    private static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_GENERATED = "GENERATED";
    private static final String STATUS_INVOICED = "INVOICED";
    private static final String STATUS_DEPOSITED = "DEPOSITED";
    private static final String STATUS_CLOSED = "CLOSED";

    private final SettlementRepository settlementRepository;
    private final OrderLedgerRepository orderLedgerRepository;
    private final NotificationClient notificationClient;

    public List<Settlement> list() {
        return settlementRepository.findAllByOrderBySettlementIdDesc();
    }

    public Settlement get(Long id) {
        return settlementRepository.findById(id).orElseThrow(() -> new SettlementException("정산 내역을 찾을 수 없습니다."));
    }

    public List<OrderLedger> ordersOf(Long settlementId) {
        return orderLedgerRepository.findBySettlementId(settlementId);
    }

    /** 아직 정산되지 않은 확정 주문을 제공자별로 묶어 새 정산내역을 생성한다. */
    @Transactional
    public List<Settlement> generate() {
        Map<Long, List<OrderLedger>> bySeller = new LinkedHashMap<>();
        for (OrderLedger order : orderLedgerRepository.findByStatusAndSettlementIdIsNull(ORDER_STATUS_CONFIRMED)) {
            Long seller = order.getSellerId() != null ? order.getSellerId() : -1L;
            bySeller.computeIfAbsent(seller, k -> new java.util.ArrayList<>()).add(order);
        }
        if (bySeller.isEmpty()) {
            throw new SettlementException("정산할 확정 주문이 없습니다.");
        }

        List<Settlement> created = new java.util.ArrayList<>();
        for (Map.Entry<Long, List<OrderLedger>> entry : bySeller.entrySet()) {
            long totalPoints = entry.getValue().stream()
                    .mapToLong(o -> o.getPointAmount() != null ? o.getPointAmount() : 0L)
                    .sum();

            Settlement settlement = new Settlement();
            settlement.setSellerId(entry.getKey());
            settlement.setOrderCount(entry.getValue().size());
            settlement.setTotalPointAmount(totalPoints);
            settlement.setStatus(STATUS_GENERATED);
            settlement.setCreatedDate(LocalDateTime.now());
            Settlement saved = settlementRepository.save(settlement);

            for (OrderLedger order : entry.getValue()) {
                order.setSettlementId(saved.getSettlementId());
                orderLedgerRepository.save(order);
            }
            created.add(saved);
        }
        return created;
    }

    @Transactional
    public Settlement issueInvoice(Long id, String invoiceNo) {
        Settlement settlement = requireStatus(id, STATUS_GENERATED, "정산내역 생성 상태에서만 세금계산서를 발행할 수 있습니다.");
        if (invoiceNo == null || invoiceNo.isBlank()) {
            throw new SettlementException("세금계산서 번호를 입력해 주세요.");
        }
        settlement.setInvoiceNo(invoiceNo);
        settlement.setStatus(STATUS_INVOICED);
        settlement.setUpdatedDate(LocalDateTime.now());
        Settlement saved = settlementRepository.save(settlement);
        notifySeller(saved, "SETTLEMENT_INVOICED");
        return saved;
    }

    @Transactional
    public Settlement confirmDeposit(Long id) {
        Settlement settlement = requireStatus(id, STATUS_INVOICED, "세금계산서 발행 상태에서만 입금확인 처리할 수 있습니다.");
        settlement.setStatus(STATUS_DEPOSITED);
        settlement.setUpdatedDate(LocalDateTime.now());
        Settlement saved = settlementRepository.save(settlement);
        notifySeller(saved, "SETTLEMENT_DEPOSITED");
        return saved;
    }

    /** 정산 상태 변경을 제공자에게 카카오 알림톡으로 알린다 (best-effort). */
    private void notifySeller(Settlement settlement, String templateCode) {
        notificationClient.sendAlimtalk("seller:" + settlement.getSellerId(), templateCode, Map.of(
                "settlementId", String.valueOf(settlement.getSettlementId()),
                "totalPointAmount", String.valueOf(settlement.getTotalPointAmount())));
    }

    @Transactional
    public Settlement close(Long id) {
        Settlement settlement = requireStatus(id, STATUS_DEPOSITED, "입금확인 상태에서만 정산을 마감할 수 있습니다.");
        settlement.setStatus(STATUS_CLOSED);
        settlement.setUpdatedDate(LocalDateTime.now());
        return settlementRepository.save(settlement);
    }

    private Settlement requireStatus(Long id, String requiredStatus, String errorMessage) {
        Settlement settlement = get(id);
        if (!requiredStatus.equals(settlement.getStatus())) {
            throw new SettlementException(errorMessage);
        }
        return settlement;
    }
}
