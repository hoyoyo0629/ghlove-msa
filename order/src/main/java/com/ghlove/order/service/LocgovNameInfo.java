package com.ghlove.order.service;

/** Response shape of donation service's read-only GET /api/locgovs/{code} lookup. */
public record LocgovNameInfo(String locgovCode, String displayName) {
}
