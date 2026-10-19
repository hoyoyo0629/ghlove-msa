package com.ghlove.admin.service.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * NH농협 데이터 연계 - AS-IS {@code NhApiBatchServiceImpl}이 analysis 데이터소스(AS-IS 운영 CUBRID {@code ghlove_analysis})의
 * analysis-mapper로 쓰던 세 표({@code stat_cntr_mber}·{@code stat_cntr_mber_loc}·{@code stat_locgov})를 같은 방식으로 채운다:
 * 표 전체를 지우고({@code DELETE FROM ... WHERE 1=1}) 한 행씩 넣는다(행 하나가 실패해도 로그만 남기고 다음 행으로 - AS-IS 그대로).
 * 배치 실행 시점에만 JDBC 연결을 열고 닫는다. enabled=false(방화벽 미개방)면 연결하지 않고 건수만 남긴다.
 * ★운영에서 켜려면 CUBRID JDBC 드라이버가 admin 실행 경로에 있어야 한다(AS-IS libs/JDBC-*-cubrid.jar).
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

    /** stat_cntr_mber 행(AS-IS insertUserCntrForNh). */
    public record UserCntrRow(Long userId, String mberCi, String sttemntPayDe, long cntrAmt) {
    }

    /** stat_cntr_mber_loc 행(AS-IS insertUserCntrLocgovForNh). */
    public record UserCntrLocgovRow(Long userId, String mberCi, String sttemntPayDe, String cntrLocgovCode, long cntrAmt) {
    }

    /** stat_locgov 행(AS-IS insertLocgovForNh). */
    public record LocgovRow(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm) {
    }

    public String replaceUserCntr(List<UserCntrRow> rows) {
        return replace("stat_cntr_mber",
                "INSERT INTO stat_cntr_mber (user_id, mber_ci, sttemnt_pay_de, cntr_amt) VALUES (?, ?, ?, ?)",
                rows, (ps, r) -> {
                    ps.setObject(1, BigInteger.valueOf(r.userId()));
                    ps.setString(2, r.mberCi());
                    ps.setString(3, r.sttemntPayDe());
                    ps.setObject(4, BigInteger.valueOf(r.cntrAmt()));
                });
    }

    public String replaceUserCntrLocgov(List<UserCntrLocgovRow> rows) {
        return replace("stat_cntr_mber_loc",
                "INSERT INTO stat_cntr_mber_loc (user_id, mber_ci, sttemnt_pay_de, cntr_locgov_code, cntr_amt) VALUES (?, ?, ?, ?, ?)",
                rows, (ps, r) -> {
                    ps.setObject(1, BigInteger.valueOf(r.userId()));
                    ps.setString(2, r.mberCi());
                    ps.setString(3, r.sttemntPayDe());
                    ps.setString(4, r.cntrLocgovCode());
                    ps.setObject(5, BigInteger.valueOf(r.cntrAmt()));
                });
    }

    public String replaceLocgov(List<LocgovRow> rows) {
        return replace("stat_locgov",
                "INSERT INTO stat_locgov (locgov_code, locgov_nm, upper_locgov_code, upper_locgov_nm) VALUES (?, ?, ?, ?)",
                rows, (ps, r) -> {
                    ps.setString(1, r.locgovCode());
                    ps.setString(2, r.locgovNm());
                    ps.setString(3, r.upperLocgovCode());
                    ps.setString(4, r.upperLocgovNm());
                });
    }

    private interface Binder<T> {
        void bind(PreparedStatement ps, T row) throws SQLException;
    }

    private <T> String replace(String table, String insertSql, List<T> rows, Binder<T> binder) {
        if (!enabled) {
            log.info("[nh] disabled - {} 반출 생략 {}건", table, rows.size());
            return "MOCK " + rows.size() + "건";
        }
        int inserted = 0;
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM " + table + " WHERE 1=1")) {
                del.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (T row : rows) {
                    try {
                        binder.bind(ps, row);
                        ps.executeUpdate();
                        inserted++;
                    } catch (SQLException | RuntimeException e) {
                        log.error("[nh] {} insert 실패 {} - {}", table, row, e.getMessage());
                    }
                }
            }
            return "SENT " + inserted + "/" + rows.size() + "건";
        } catch (SQLException e) {
            throw new IllegalStateException("NH 데이터 연계(" + table + ")에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
