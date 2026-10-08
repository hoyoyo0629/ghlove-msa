package com.ghlove.order.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AS-IS {@code OrderServiceImpl.getNewOrderCode} 포맷("K" + 10자리 0-패딩 숫자)으로
 * 바꾼 뒤 실제 DB 시퀀스(ord.op_order_code_seq)가 원자적으로 증가하는지 확인한다 -
 * 레포지토리 메서드를 목으로 때우면 SQL 자체가 실행되는지는 확인되지 않는다
 * ([[admin-excel-download-log-500-fix]]에서 겪은 검증부족 교훈과 같은 이유로 실제
 * 스프링 컨텍스트 + 실제 DB로 확인한다).
 */
@SpringBootTest
class OrderCodeGenerationTest {

    @Autowired
    private OrderService orderService;

    @Test
    void generatesAsIsShapedSequentialCodes() {
        String first = orderService.generateOrderId();
        String second = orderService.generateOrderId();

        assertThat(first).matches("K\\d{10}");
        assertThat(second).matches("K\\d{10}");
        assertThat(Long.parseLong(second.substring(1))).isGreaterThan(Long.parseLong(first.substring(1)));
    }
}
