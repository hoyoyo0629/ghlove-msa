package com.ghlove.admin.service;

import com.ghlove.admin.domain.Manager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 통계 > 답례품 구매현황 > 지자체별 (AS-IS {@code getDateStatsListGhlove}의 LOCGOV/ITEM/USER
 * 분기) - order가 돌려주는 원시 그룹 집계에 지자체명(donation)·상호명(gift)·회원명(member)을
 * 붙인다({@code 관심지자체} 화면과 같은 cross-service enrichment 방식).
 */
@Component
@RequiredArgsConstructor
public class ShopStatisticsLocgovService {

    private final OrderAdminClient orderAdminClient;
    private final LocgovClient locgovClient;
    private final GiftClient giftClient;
    private final MemberClient memberClient;

    public List<LocgovRow> locgovRows(Manager manager, String startYm, String endYm) {
        Map<String, LocgovClient.LocgovInfo> byCode = new LinkedHashMap<>();
        for (LocgovClient.LocgovInfo l : locgovClient.allLocgovs()) {
            byCode.put(l.locgovCode(), l);
        }
        List<LocgovRow> rows = new ArrayList<>();
        for (OrderAdminClient.GroupRow g : orderAdminClient.locgovGroupStats(
                manager, "LOCGOV", startYm, endYm, null, null)) {
            LocgovClient.LocgovInfo l = byCode.get(g.locgovCode());
            String name = l != null
                    ? ((l.upperLocgovNm() != null ? l.upperLocgovNm() + " " : "") + l.locgovNm())
                    : g.locgovCode();
            rows.add(new LocgovRow(g.locgovCode(), name,
                    g.saleCount(), g.saleAmount(), g.cancelCount(), g.cancelAmount(), g.sumCount(), g.sumAmount()));
        }
        return rows;
    }

    public List<ItemRow> itemRows(Manager manager, String startYm, String endYm, String locgovCode) {
        Map<Long, String> companyNames = new HashMap<>();
        for (GiftClient.SellerDetail s : giftClient.sellers()) {
            companyNames.put(s.sellerId(), s.companyName());
        }
        List<ItemRow> rows = new ArrayList<>();
        for (OrderAdminClient.GroupRow g : orderAdminClient.locgovGroupStats(
                manager, "ITEM", startYm, endYm, locgovCode, null)) {
            String company = companyNames.getOrDefault(g.sellerId(), "-");
            rows.add(new ItemRow(g.itemId(), g.itemName(), company,
                    g.saleCount(), g.saleAmount(), g.cancelCount(), g.cancelAmount(), g.sumCount(), g.sumAmount()));
        }
        return rows;
    }

    public List<UserRow> userRows(Manager manager, String startYm, String endYm, Long itemId) {
        List<UserRow> rows = new ArrayList<>();
        for (OrderAdminClient.GroupRow g : orderAdminClient.locgovGroupStats(
                manager, "USER", startYm, endYm, null, itemId)) {
            MemberClient.MemberInfo info = memberClient.fetchOrNull(g.userId());
            String name = (info != null && info.userName() != null)
                    ? info.userName() + "(" + info.loginId() + ")"
                    : ("회원#" + g.userId());
            rows.add(new UserRow(g.userId(), name, g.saleCount(), g.saleAmount(), g.cancelCount(), g.cancelAmount()));
        }
        return rows;
    }

    /** AS-IS detail.jsp 제목(답례품명) - USER 그룹도 item_name을 같이 돌려주므로 한 번에 읽는다
     *  (locgovCode 없이 품목만으로 좁히는 조회라 locgovRows/itemRows를 거칠 필요가 없다). */
    public String itemNameOf(Manager manager, String startYm, String endYm, Long itemId) {
        for (OrderAdminClient.GroupRow g : orderAdminClient.locgovGroupStats(
                manager, "USER", startYm, endYm, null, itemId)) {
            if (g.itemName() != null) {
                return g.itemName();
            }
        }
        return null;
    }

    /** AS-IS 목록(지자체별) 한 행 - 지자체명은 G_LOCGOV(donation 소유)에서 붙인다. */
    public record LocgovRow(String locgovCode, String displayName, long saleCount, long saleAmount,
                             long cancelCount, long cancelAmount, long sumCount, long sumAmount) {
    }

    /** AS-IS 목록(답례품별) 한 행 - 상호명은 OP_SELLER(gift 소유)에서 붙인다. */
    public record ItemRow(Long itemId, String itemName, String companyName, long saleCount, long saleAmount,
                           long cancelCount, long cancelAmount, long sumCount, long sumAmount) {
    }

    /** AS-IS 상세(회원별) 한 행 - 회원명(복호화)은 member 소유. 합계 칸이 없다(AS-IS detail.jsp와 같다). */
    public record UserRow(Long userId, String displayName, long saleCount, long saleAmount,
                           long cancelCount, long cancelAmount) {
    }
}
