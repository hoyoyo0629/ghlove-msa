package com.ghlove.donation.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@AllArgsConstructor
public class ReceiptListResult {
    private final List<ReceiptRow> rows;
    private final BigDecimal totalCntrAmt;
    private final int totalCnt;
}
