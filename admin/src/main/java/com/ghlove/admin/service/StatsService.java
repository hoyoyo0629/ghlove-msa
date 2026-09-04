package com.ghlove.admin.service;

import com.ghlove.admin.domain.DonationLedger;
import com.ghlove.admin.domain.GiftLedgerStat;
import com.ghlove.admin.domain.OrderLedger;
import com.ghlove.admin.domain.PointLedgerStat;
import com.ghlove.admin.event.DonationCancelledEvent;
import com.ghlove.admin.event.DonationCompletedEvent;
import com.ghlove.admin.event.GiftLifecycleEvent;
import com.ghlove.admin.event.OrderCancelledEvent;
import com.ghlove.admin.event.OrderConfirmedEvent;
import com.ghlove.admin.event.OrderCreatedEvent;
import com.ghlove.admin.event.OrderDeliveryUpdatedEvent;
import com.ghlove.admin.event.PointLedgerEvent;
import com.ghlove.admin.repository.DonationLedgerRepository;
import com.ghlove.admin.repository.GiftLedgerStatRepository;
import com.ghlove.admin.repository.OrderLedgerRepository;
import com.ghlove.admin.repository.PointLedgerStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 운영 통계 (SFR-007). admin은 donation/order 서비스의 DB를 직접 읽을 수 없으므로
 * (DB per Service) 각 서비스의 Kafka 이벤트를 구독해 자체 ReadModel(원장 사본)을
 * 쌓고, 화면에서는 그 사본을 라이브로 집계해 보여준다 (CQRS).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private static final String STATUS_COMPLETED = "COMPLETED";
    private static final String STATUS_CANCELLED = "CANCELLED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_CONFIRMED = "CONFIRMED";

    private final DonationLedgerRepository donationLedgerRepository;
    private final OrderLedgerRepository orderLedgerRepository;
    private final PointLedgerStatRepository pointLedgerStatRepository;
    private final GiftLedgerStatRepository giftLedgerStatRepository;

    // ---- donation.lifecycle 반영 ----

    @Transactional
    public void onDonationCompleted(DonationCompletedEvent event) {
        DonationLedger ledger = donationLedgerRepository.findById(event.cntrSn()).orElseGet(DonationLedger::new);
        ledger.setCntrSn(event.cntrSn());
        ledger.setUserId(event.userId());
        ledger.setLocgovCode(event.cntrLocgovCode());
        ledger.setAmount(event.cntrAmt());
        ledger.setStatus(STATUS_COMPLETED);
        ledger.setEventDate(event.cntrDe());
        ledger.setCntrPathCode(event.cntrPathCode());
        ledger.setPsitnLocgovCode(event.psitnLocgovCode());
        ledger.setCreatedDate(LocalDateTime.now());
        donationLedgerRepository.save(ledger);
    }

    @Transactional
    public void onDonationCancelled(DonationCancelledEvent event) {
        DonationLedger ledger = donationLedgerRepository.findById(event.cntrSn()).orElse(null);
        if (ledger == null) {
            log.info("Donation {} not in stats ledger yet - ignoring cancel (never completed)", event.cntrSn());
            return;
        }
        ledger.setStatus(STATUS_CANCELLED);
        donationLedgerRepository.save(ledger);
    }

    // ---- order.saga 반영 ----

    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        if (orderLedgerRepository.existsById(event.orderId())) {
            return;
        }
        OrderLedger ledger = new OrderLedger();
        ledger.setOrderId(event.orderId());
        ledger.setUserId(event.userId());
        ledger.setItemId(event.itemId());
        ledger.setSellerId(event.sellerId());
        ledger.setQuantity(event.quantity());
        ledger.setPointAmount(event.pointAmount());
        ledger.setLocgovCode(event.locgovCode());
        ledger.setStatus(STATUS_PENDING);
        ledger.setCreatedDate(LocalDateTime.now());
        orderLedgerRepository.save(ledger);
    }

    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        OrderLedger ledger = orderLedgerRepository.findById(event.orderId()).orElse(null);
        if (ledger == null) {
            log.info("Order {} not in stats ledger yet - ignoring ORDER_CONFIRMED (ORDER_CREATED not seen)", event.orderId());
            return;
        }
        ledger.setStatus(STATUS_CONFIRMED);
        ledger.setOrderConfirmedAt(event.occurredAt());
        orderLedgerRepository.save(ledger);
    }

    @Transactional
    public void onOrderCancelled(OrderCancelledEvent event) {
        updateOrderStatus(event.orderId(), STATUS_CANCELLED);
    }

    /** SFR-006 "제공자·지자체별 SLA 지표" - 송장등록/배송중·완료/구매확정마다 도착하는
     *  배송 리드타임 원본을 그대로 사본에 반영한다(집계는 조회 시점에 {@link #slaStats()}가). */
    @Transactional
    public void onOrderDeliveryUpdated(OrderDeliveryUpdatedEvent event) {
        OrderLedger ledger = orderLedgerRepository.findById(event.orderId()).orElse(null);
        if (ledger == null) {
            log.info("Order {} not in stats ledger yet - ignoring ORDER_DELIVERY_UPDATED", event.orderId());
            return;
        }
        ledger.setDeliveryStatus(event.deliveryStatus());
        ledger.setShippedDate(event.shippedDate());
        ledger.setDeliveredDate(event.deliveredDate());
        ledger.setDeliveryConfirmedDate(event.confirmedDate());
        orderLedgerRepository.save(ledger);
    }

    private void updateOrderStatus(String orderId, String status) {
        OrderLedger ledger = orderLedgerRepository.findById(orderId).orElse(null);
        if (ledger == null) {
            log.info("Order {} not in stats ledger yet - ignoring {} (ORDER_CREATED not seen)", orderId, status);
            return;
        }
        ledger.setStatus(status);
        orderLedgerRepository.save(ledger);
    }

    // ---- point.ledger 반영 (SFR-007/009 gap fill) ----

    @Transactional
    public void onPointLedgerEvent(PointLedgerEvent event) {
        if (pointLedgerStatRepository.existsById(event.ledgerId())) {
            log.info("Point ledger {} already recorded in stats - skipping duplicate event", event.ledgerId());
            return;
        }
        PointLedgerStat stat = new PointLedgerStat();
        stat.setLedgerId(event.ledgerId());
        stat.setUserId(event.userId());
        stat.setLocgovCode(event.locgovCode());
        stat.setTxnType(event.txnType());
        stat.setPointAmount(event.pointAmount());
        stat.setCreatedDate(event.createdDate());
        pointLedgerStatRepository.save(stat);
    }

    // ---- gift.lifecycle 반영 ----

    @Transactional
    public void onGiftLifecycleEvent(GiftLifecycleEvent event) {
        GiftLedgerStat stat = giftLedgerStatRepository.findById(event.itemId()).orElseGet(GiftLedgerStat::new);
        stat.setItemId(event.itemId());
        stat.setSellerId(event.sellerId());
        stat.setCategoryCode(event.categoryCode());
        stat.setDataStatusCode(event.dataStatusCode());
        stat.setSalePrice(event.salePrice());
        stat.setLocgovCode(event.locgovCode());
        stat.setUpdatedDate(LocalDateTime.now());
        giftLedgerStatRepository.save(stat);
    }

    // ---- ReadModel 재동기화 (SFR-009 "원장 복구·재동기화 절차") ----
    // Kafka 컨슈머 오프셋은 admin DB와 독립적이라(오늘 admin DB만 재프로비저닝했을 때 실제로
    // 겪음 - STAT_DONATION_LEDGER가 0건인데 donation엔 완료 기부가 8건 있던 사고), 각
    // 원장서비스의 "현재 상태 전체 스냅샷"을 직접 불러와 upsert한다. 이벤트 리스너와 똑같은
    // find-or-create 패턴을 그대로 재사용해 - OrderLedger.settlementId처럼 admin에서만
    // 만들어지고 원본 서비스엔 없는 필드를 덮어쓰지 않도록.

    @Transactional
    public void resyncDonations(List<com.ghlove.admin.service.DonationClient.DonationSnapshot> snapshots) {
        for (var s : snapshots) {
            DonationLedger ledger = donationLedgerRepository.findById(s.cntrSn()).orElseGet(DonationLedger::new);
            ledger.setCntrSn(s.cntrSn());
            ledger.setUserId(s.userId());
            ledger.setLocgovCode(s.cntrLocgovCode());
            ledger.setAmount(s.cntrAmt());
            ledger.setStatus(s.cntrSttusCode());
            ledger.setEventDate(s.cntrDe());
            ledger.setCntrPathCode(s.cntrPathCode());
            ledger.setPsitnLocgovCode(s.psitnLocgovCode());
            if (ledger.getCreatedDate() == null) {
                ledger.setCreatedDate(LocalDateTime.now());
            }
            donationLedgerRepository.save(ledger);
        }
    }

    @Transactional
    public void resyncOrders(List<com.ghlove.admin.service.OrderClient.OrderSnapshot> snapshots) {
        for (var s : snapshots) {
            OrderLedger ledger = orderLedgerRepository.findById(s.orderId()).orElseGet(OrderLedger::new);
            ledger.setOrderId(s.orderId());
            ledger.setUserId(s.userId());
            ledger.setItemId(s.itemId());
            ledger.setSellerId(s.sellerId());
            ledger.setQuantity(s.quantity());
            ledger.setPointAmount(s.pointAmount());
            ledger.setStatus(s.orderStatus());
            // settlementId는 손대지 않는다 - 원본 order 서비스엔 없는 admin 고유 정산연결 상태.
            if (ledger.getCreatedDate() == null) {
                ledger.setCreatedDate(LocalDateTime.now());
            }
            orderLedgerRepository.save(ledger);
        }
    }

    @Transactional
    public void resyncPointLedger(List<com.ghlove.admin.service.PointClient.LedgerSnapshot> snapshots) {
        for (var s : snapshots) {
            PointLedgerStat stat = pointLedgerStatRepository.findById(s.ledgerId()).orElseGet(PointLedgerStat::new);
            stat.setLedgerId(s.ledgerId());
            stat.setUserId(s.userId());
            stat.setLocgovCode(s.locgovCode());
            stat.setTxnType(s.txnType());
            stat.setPointAmount(s.pointAmount());
            stat.setCreatedDate(s.createdDate());
            pointLedgerStatRepository.save(stat);
        }
    }

    @Transactional
    public void resyncGifts(List<com.ghlove.admin.service.GiftClient.ItemSnapshot> snapshots) {
        for (var s : snapshots) {
            GiftLedgerStat stat = giftLedgerStatRepository.findById(s.itemId()).orElseGet(GiftLedgerStat::new);
            stat.setItemId(s.itemId());
            stat.setSellerId(s.sellerId());
            stat.setCategoryCode(s.categoryCode());
            stat.setDataStatusCode(s.dataStatusCode());
            stat.setSalePrice(s.salePrice());
            stat.setLocgovCode(s.locgovCode());
            stat.setUpdatedDate(LocalDateTime.now());
            giftLedgerStatRepository.save(stat);
        }
    }

    // ---- 집계 조회 ----

    /** 거래유형(적립/사용/회수/복원/소멸)별 포인트 합계. */
    public Map<String, Long> pointStatsByTxnType() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (PointLedgerStat s : pointLedgerStatRepository.findAll()) {
            result.merge(s.getTxnType() != null ? s.getTxnType() : "-",
                    s.getPointAmount() != null ? s.getPointAmount() : 0L, Long::sum);
        }
        return result;
    }

    /** 상태(승인대기/승인/반려/판매중지/폐지)별 답례품 건수. */
    public Map<String, Long> giftCountByStatus() {
        Map<String, Long> result = new LinkedHashMap<>();
        for (GiftLedgerStat s : giftLedgerStatRepository.findAll()) {
            result.merge(s.getDataStatusCode() != null ? s.getDataStatusCode() : "-", 1L, Long::sum);
        }
        return result;
    }

    /** 지자체별 완료 기부 건수/총액. */
    public Map<String, DonationStat> donationStatsByLocgov() {
        Map<String, DonationStat> result = new LinkedHashMap<>();
        for (DonationLedger d : donationLedgerRepository.findByStatus(STATUS_COMPLETED)) {
            String locgov = d.getLocgovCode() != null ? d.getLocgovCode() : "-";
            result.computeIfAbsent(locgov, k -> new DonationStat()).add(d.getAmount());
        }
        return sortByTotalDesc(result, DonationStat::total);
    }

    /** 제공자별 확정 주문 건수/포인트 합계. */
    public Map<Long, OrderStat> orderStatsBySeller() {
        Map<Long, OrderStat> result = new LinkedHashMap<>();
        for (OrderLedger o : orderLedgerRepository.findByStatus(STATUS_CONFIRMED)) {
            Long seller = o.getSellerId() != null ? o.getSellerId() : -1L;
            result.computeIfAbsent(seller, k -> new OrderStat()).add(o.getPointAmount());
        }
        return sortByTotalDesc(result, OrderStat::total);
    }

    public long totalCompletedDonationCount() {
        return donationLedgerRepository.findByStatus(STATUS_COMPLETED).size();
    }

    public BigDecimal totalCompletedDonationAmount() {
        return donationLedgerRepository.findByStatus(STATUS_COMPLETED).stream()
                .map(DonationLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long totalConfirmedOrderCount() {
        return orderLedgerRepository.findByStatus(STATUS_CONFIRMED).size();
    }

    public long totalConfirmedOrderPoints() {
        return orderLedgerRepository.findByStatus(STATUS_CONFIRMED).stream()
                .mapToLong(o -> o.getPointAmount() != null ? o.getPointAmount() : 0L)
                .sum();
    }

    private <K, V> Map<K, V> sortByTotalDesc(Map<K, V> map, java.util.function.Function<V, BigDecimal> totalFn) {
        List<Map.Entry<K, V>> entries = new java.util.ArrayList<>(map.entrySet());
        entries.sort(Comparator.comparing((Map.Entry<K, V> e) -> totalFn.apply(e.getValue())).reversed());
        Map<K, V> sorted = new LinkedHashMap<>();
        entries.forEach(e -> sorted.put(e.getKey(), e.getValue()));
        return sorted;
    }

    public static class DonationStat {
        private long count;
        private BigDecimal amount = BigDecimal.ZERO;

        void add(BigDecimal value) {
            count++;
            amount = amount.add(value != null ? value : BigDecimal.ZERO);
        }

        public long getCount() {
            return count;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        BigDecimal total() {
            return amount;
        }
    }

    public static class OrderStat {
        private long count;
        private long points;

        void add(Long value) {
            count++;
            points += value != null ? value : 0L;
        }

        public long getCount() {
            return count;
        }

        public long getPoints() {
            return points;
        }

        BigDecimal total() {
            return BigDecimal.valueOf(points);
        }
    }

    /**
     * SFR-006 "제공자·지자체별 SLA 지표" - 다른 통계와 동일하게 저장 시점이 아니라 조회
     * 시점에 라이브 집계한다. 세 구간(확정→발송/발송→배송완료/배송완료→구매확정)의
     * 평균 소요시간(시간 단위)과 표본 수를 그룹(제공자 또는 지자체)별로 계산한다 - 구간의
     * 시작/끝 타임스탬프가 둘 다 있는 주문만 그 구간의 표본에 포함된다(아직 발송 전인
     * 주문이 "발송→배송완료" 평균을 0으로 왜곡하지 않도록).
     */
    public record SlaMetric(String groupKey, long orderCount, Double avgConfirmToShipHours,
                             Double avgShipToDeliverHours, Double avgDeliverToConfirmHours) {
    }

    public List<SlaMetric> slaStatsBySeller() {
        return slaStats(l -> l.getSellerId() != null ? String.valueOf(l.getSellerId()) : "(미지정)");
    }

    public List<SlaMetric> slaStatsByLocgov() {
        return slaStats(l -> l.getLocgovCode() != null ? l.getLocgovCode() : "(미지정)");
    }

    private List<SlaMetric> slaStats(java.util.function.Function<OrderLedger, String> groupKeyFn) {
        Map<String, List<OrderLedger>> grouped = orderLedgerRepository.findAll().stream()
                .filter(l -> !STATUS_CANCELLED.equals(l.getStatus()))
                .collect(java.util.stream.Collectors.groupingBy(groupKeyFn, LinkedHashMap::new, java.util.stream.Collectors.toList()));

        List<SlaMetric> result = new java.util.ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<OrderLedger> rows = entry.getValue();
            result.add(new SlaMetric(entry.getKey(), rows.size(),
                    avgHoursBetween(rows, OrderLedger::getOrderConfirmedAt, OrderLedger::getShippedDate),
                    avgHoursBetween(rows, OrderLedger::getShippedDate, OrderLedger::getDeliveredDate),
                    avgHoursBetween(rows, OrderLedger::getDeliveredDate, OrderLedger::getDeliveryConfirmedDate)));
        }
        result.sort(Comparator.comparing(SlaMetric::groupKey));
        return result;
    }

    private Double avgHoursBetween(List<OrderLedger> rows, java.util.function.Function<OrderLedger, LocalDateTime> start,
                                    java.util.function.Function<OrderLedger, LocalDateTime> end) {
        List<Double> hours = new java.util.ArrayList<>();
        for (OrderLedger row : rows) {
            LocalDateTime s = start.apply(row);
            LocalDateTime e = end.apply(row);
            if (s != null && e != null) {
                hours.add(java.time.Duration.between(s, e).toMinutes() / 60.0);
            }
        }
        if (hours.isEmpty()) {
            return null;
        }
        return Math.round(hours.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 10) / 10.0;
    }
}
