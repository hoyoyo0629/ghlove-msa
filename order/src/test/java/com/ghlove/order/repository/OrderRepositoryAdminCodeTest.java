package com.ghlove.order.repository;

import com.ghlove.order.domain.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 관리자 수기 주문등록("AB"+yyMMdd+8자리) 채번 - AS-IS처럼 {@code coalesce(max+1)} 방식이라
 * (QestnarRepository 등 기존 패턴과 동일) 실제로 그 번호로 행이 생겨야 다음 호출이 이어진다.
 * 네이티브 substring/cast SQL이 실제로 맞는지는 목으로는 검증되지 않아 실제 DB로 확인한다.
 */
@SpringBootTest
class OrderRepositoryAdminCodeTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @Transactional
    void sequenceAdvancesOnceAnOrderWithThatCodeExists() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        long first = orderRepository.nextAdminOrderSeqForDate(today);

        Order order = new Order();
        order.setOrderId("AB" + today + String.format("%08d", first));
        order.setUserId(0L);
        order.setPointAmount(0L);
        order.setOrderStatus("CONFIRMED");
        order.setCreatedDate(LocalDateTime.now());
        order.setUpdatedDate(LocalDateTime.now());
        orderRepository.save(order);
        orderRepository.flush();

        long second = orderRepository.nextAdminOrderSeqForDate(today);
        assertThat(second).isEqualTo(first + 1);
    }
}
