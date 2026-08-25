package com.ghlove.order.service;

/** Response shape of donation service's read-only GET /api/locgovs bulk lookup. */
public record LocgovInfo(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
}
