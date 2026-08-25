package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * NH농협 데이터 연계 (AS-IS NhApiBatchServiceImpl - REST가 아니라 NH가 소유한 별도
 * CUBRID/DB에 배치가 직접 INSERT하는 DB-level 연계). 상시 연결 풀을 둘 만큼 트래픽이
 * 있는 경로가 아니라 배치 실행 시점에만 JDBC 연결을 열고 닫는다. enabled=false
 * (방화벽 미개방)면 실제 연결을 시도하지 않고 모크로 처리한다.
 */
@Component
@Slf4j
public class NhDataClient {

    private final boolean enabled;
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public NhDataClient(
            @Value("${ghlove.integrations.nh.enabled}") boolean enabled,
            @Value("${ghlove.integrations.nh.jdbc-url}") String jdbcUrl,
            @Value("${ghlove.integrations.nh.username}") String username,
            @Value("${ghlove.integrations.nh.password}") String password) {
        this.enabled = enabled;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    /** @return 실제 반출이면 "SENT", 모크면 "MOCK" */
    public String export(List<NhExportRow> rows) {
        if (!enabled) {
            log.info("[nh] disabled - mock 반출 {}건", rows.size());
            return "MOCK";
        }
        String sql = "INSERT INTO NH_USER_CNTR (CNTR_SN, USER_ID, LOCGOV_CODE, AMOUNT) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (NhExportRow row : rows) {
                stmt.setString(1, row.cntrSn());
                stmt.setLong(2, row.userId());
                stmt.setString(3, row.locgovCode());
                stmt.setBigDecimal(4, row.amount());
                stmt.addBatch();
            }
            stmt.executeBatch();
            return "SENT";
        } catch (SQLException e) {
            throw new IllegalStateException("NH 데이터 연계 반출에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
