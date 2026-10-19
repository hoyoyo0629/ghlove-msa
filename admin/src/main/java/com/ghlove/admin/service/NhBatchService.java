package com.ghlove.admin.service;

import com.ghlove.admin.service.integration.NhDataClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * 농협 연계 배치 3종 - AS-IS {@code NhApiBatchServiceImpl}(setUserCntrForNh·setUserCntrLocForNh·setLocForNh).
 * 원천은 기부(donation: 수납완료 기부의 회원·수납일(·지자체)별 합계, 사용 중 지자체)와 회원 CI(member)이고,
 * 결과는 {@link NhDataClient}가 농협이 읽는 분석 DB 세 표를 지우고 다시 채운다. AS-IS가 회원과 INNER JOIN(CI 있음)하므로
 * CI 없는 회원의 행은 넣지 않는다. 예전 TO-BE의 NH 반출 화면(/batch/nh-export, 기부 건별 NH_USER_CNTR)은 AS-IS에 없어 제거했다.
 */
@Service
public class NhBatchService {

    private final NhDataClient nhDataClient;
    private final RestClient donation;
    private final RestClient member;

    public record NhUserCntr(Long userId, String sttemntPayDe, String cntrLocgovCode, long cntrAmt) {
    }

    public NhBatchService(NhDataClient nhDataClient,
                          @Value("${ghlove.donation-service.base-url}") String donationBaseUrl,
                          @Value("${ghlove.member-service.base-url}") String memberBaseUrl,
                          @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.nhDataClient = nhDataClient;
        this.donation = RestClient.builder().baseUrl(donationBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
        this.member = RestClient.builder().baseUrl(memberBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** userCntrForNhBatch - stat_cntr_mber. */
    public String userCntr() {
        Map<Long, String> ci = ciMap();
        List<NhDataClient.UserCntrRow> rows = userCntrRows(false).stream()
                .filter(r -> r.userId() != null && ci.containsKey(r.userId()))
                .map(r -> new NhDataClient.UserCntrRow(r.userId(), ci.get(r.userId()), r.sttemntPayDe(), r.cntrAmt()))
                .toList();
        return nhDataClient.replaceUserCntr(rows);
    }

    /** userCntrLocForNhBatch - stat_cntr_mber_loc. */
    public String userCntrLocgov() {
        Map<Long, String> ci = ciMap();
        List<NhDataClient.UserCntrLocgovRow> rows = userCntrRows(true).stream()
                .filter(r -> r.userId() != null && ci.containsKey(r.userId()))
                .map(r -> new NhDataClient.UserCntrLocgovRow(r.userId(), ci.get(r.userId()), r.sttemntPayDe(),
                        r.cntrLocgovCode(), r.cntrAmt()))
                .toList();
        return nhDataClient.replaceUserCntrLocgov(rows);
    }

    /** locForNhBatch - stat_locgov. */
    public String locgov() {
        List<NhDataClient.LocgovRow> rows = donation.get().uri("/api/admin/batch/nh/locgovs").retrieve()
                .body(new ParameterizedTypeReference<List<NhDataClient.LocgovRow>>() {
                });
        return nhDataClient.replaceLocgov(rows == null ? List.of() : rows);
    }

    private List<NhUserCntr> userCntrRows(boolean byLocgov) {
        List<NhUserCntr> rows = donation.get()
                .uri(u -> u.path("/api/admin/batch/nh/user-cntr").queryParam("byLocgov", byLocgov).build())
                .retrieve().body(new ParameterizedTypeReference<List<NhUserCntr>>() {
                });
        return rows == null ? List.of() : rows;
    }

    private Map<Long, String> ciMap() {
        Map<Long, String> map = member.get().uri("/api/admin/members/batch/ci-map").retrieve()
                .body(new ParameterizedTypeReference<Map<Long, String>>() {
                });
        return map == null ? Map.of() : map;
    }
}
