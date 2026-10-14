package com.ghlove.point.service;

import com.ghlove.point.domain.PointLedger;
import com.ghlove.point.event.PointLedgerPublisher;
import com.ghlove.point.event.ShipmentCreatedEvent;
import com.ghlove.point.repository.GCntrUsePointRepository;
import com.ghlove.point.repository.PointBalanceRepository;
import com.ghlove.point.repository.PointLedgerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 출고 단위 포인트 차감 - 온라인 주문은 잔액이 모자라면 실패하고, 기탁서 등록 답례품은 AS-IS procOffOrder처럼
 * 남은 잔액만큼만 차감하고 성공하는지 확인한다.
 */
@ExtendWith(MockitoExtension.class)
class PointServiceShipmentDeductTest {

    @Mock
    private PointLedgerRepository pointLedgerRepository;
    @Mock
    private PointBalanceRepository pointBalanceRepository;
    @Mock
    private PointLedgerPublisher pointLedgerPublisher;
    @Mock
    private GCntrUsePointRepository gCntrUsePointRepository;

    @InjectMocks
    private PointService pointService;

    private PointLedger lot(long remaining) {
        PointLedger lot = new PointLedger();
        lot.setUserId(7L);
        lot.setLocgovCode("41360");
        lot.setRefKey("2026101400001");
        lot.setRemainingAmount(remaining);
        return lot;
    }

    private final List<ShipmentCreatedEvent.Line> lines = List.of(
            new ShipmentCreatedEvent.Line(101L, 30_000L),
            new ShipmentCreatedEvent.Line(102L, 20_000L));

    @Test
    void onlineShipmentFailsWhenPointShort() {
        when(pointLedgerRepository.existsByRefKeyAndTxnType(anyString(), any())).thenReturn(false);
        when(pointLedgerRepository.findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(7L, 0L))
                .thenReturn(List.of(lot(40_000L)));

        boolean ok = pointService.deductForShipment(1L, "K0000000001", 7L, 50_000L, "41360", lines, false);

        assertThat(ok).isFalse();
        verify(pointLedgerPublisher, never()).publish(any());
    }

    @Test
    void offgiveShipmentDeductsOnlyWhatIsLeft() {
        PointLedger lot = lot(40_000L);
        when(pointLedgerRepository.existsByRefKeyAndTxnType(anyString(), any())).thenReturn(false);
        when(pointLedgerRepository.findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(7L, 0L))
                .thenReturn(List.of(lot));
        when(pointBalanceRepository.findById(7L)).thenReturn(Optional.empty());

        boolean ok = pointService.deductForShipment(1L, "K0000000001", 7L, 50_000L, "41360", lines, true);

        assertThat(ok).isTrue();
        ArgumentCaptor<PointLedger> saved = ArgumentCaptor.forClass(PointLedger.class);
        verify(pointLedgerPublisher, atLeastOnce()).publish(saved.capture());
        // 첫 품목 30,000 전부, 두 번째 품목은 남은 10,000만 - 합계 40,000(잔액 전부)
        assertThat(saved.getAllValues()).extracting(PointLedger::getPointAmount).containsExactly(-30_000L, -10_000L);
        assertThat(saved.getAllValues()).extracting(PointLedger::getRefKey)
                .containsExactly("K0000000001#101", "K0000000001#102");
        assertThat(lot.getRemainingAmount()).isZero();
    }

    @Test
    void offgiveShipmentSucceedsWithNoPointAtAll() {
        when(pointLedgerRepository.existsByRefKeyAndTxnType(anyString(), any())).thenReturn(false);
        when(pointLedgerRepository.findByUserIdAndRemainingAmountGreaterThanOrderByExpirationDateAsc(7L, 0L))
                .thenReturn(List.of());

        boolean ok = pointService.deductForShipment(1L, "K0000000001", 7L, 50_000L, "41360", lines, true);

        assertThat(ok).isTrue();
        verify(pointLedgerPublisher, never()).publish(any());
    }
}
