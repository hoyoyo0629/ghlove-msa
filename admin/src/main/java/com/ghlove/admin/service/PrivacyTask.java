package com.ghlove.admin.service;

/**
 * 개인정보 접근 수행업무 (AS-IS saleson.common.enumeration.PrivacyTask 그대로).
 * {@code OP_PRIVACY_ACCESS_LOG.TASK}에 {@link #getTitle()}이 저장되고, 엑셀다운로드 사유
 * 관리(1411) 화면은 {@code TASK = '엑셀 다운로드'}인 행만 보여준다.
 */
public enum PrivacyTask {

    LIST("list", "목록 열람", "목록 데이터 조회"),
    VIEW("view", "열람", "상세 데이터 조회"),
    UPDATE("update", "수정", "데이터 수정"),
    DELETE("delete", "삭제", "데이터 삭제"),
    EXCEL_DOWNLOAD("excel-download", "엑셀 다운로드", "데이터 엑셀 다운로드");

    private final String code;
    private final String title;
    private final String description;

    PrivacyTask(String code, String title, String description) {
        this.code = code;
        this.title = title;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
