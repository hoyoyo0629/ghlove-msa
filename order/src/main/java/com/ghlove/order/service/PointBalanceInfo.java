package com.ghlove.order.service;

/** Response shape of point service's read-only GET /api/balance-by-locgov lookup. */
public record PointBalanceInfo(long balance) {
}
