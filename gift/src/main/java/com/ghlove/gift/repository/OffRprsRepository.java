package com.ghlove.gift.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 지자체 오프라인 대표 답례품(G_LCLGV_OFF_RPRS_GDS_MNG) 조회 - AS-IS offgive-mapper의
 * {@code selectOffRprs}/{@code selectOffRprsOption} SQL 그대로(조회 전용).
 * 결과 키도 AS-IS 매퍼와 같은 소문자 컬럼명을 쓴다(화면 JS가 그 키로 읽는다).
 */
@Repository
@RequiredArgsConstructor
public class OffRprsRepository {

    private final EntityManager entityManager;

    /** AS-IS selectOffRprs - 정렬 없음(AS-IS SQL에 ORDER BY가 없다). */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> selectOffRprs(String lclgvCd) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT offr.lclgv_cd, offr.gds_id, offr.sort_seq, offr.frst_rgtr_id, offr.frst_reg_dt, offr.options,
                       oi.item_code, oi.item_name, oi.item_image, oi.item_option_flag, oi.stock_quantity,
                       oi.sale_price, oi.display_flag, oi.sold_out, oi.data_status_code
                  FROM g_lclgv_off_rprs_gds_mng offr
                  LEFT JOIN op_item oi ON oi.item_id = offr.gds_id
                 WHERE 1 = 1
                   AND offr.lclgv_cd = :lclgvCd
                   AND oi.display_flag = 'Y'
                   AND oi.sold_out = '0'
                """)
                .setParameter("lclgvCd", lclgvCd)
                .getResultList();
        String[] keys = {"lclgv_cd", "gds_id", "sort_seq", "frst_rgtr_id", "frst_reg_dt", "options",
                "item_code", "item_name", "item_image", "item_option_flag", "stock_quantity",
                "sale_price", "display_flag", "sold_out", "data_status_code"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> m = new LinkedHashMap<>();
            for (int i = 0; i < keys.length; i++) {
                m.put(keys[i], r[i]);
            }
            result.add(m);
        }
        return result;
    }

    /**
     * AS-IS selectOffRprsOption - 옵션 문자열로 OP_ITEM_OPTION 한 건을 찾는다. optionType이 S면
     * option_name2(+가격), 그 외(S3)면 option_name1~3(+가격)으로 맞춘다. 없으면 null.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> selectOffRprsOption(Map<String, Object> s) {
        boolean typeS = "S".equals(s.get("optionType"));
        String optionPrice = s.get("optionPrice") == null ? null : s.get("optionPrice").toString();
        boolean hasPrice = optionPrice != null && !optionPrice.isEmpty();
        StringBuilder sql = new StringBuilder("""
                SELECT item_option_id, option_price, option_stock_flag, option_stock_quantity
                  FROM op_item_option
                 WHERE item_id = :gdsId
                   AND option_type = :optionType
                """);
        if (typeS) {
            sql.append("   AND option_name2 = :optionName2\n");
        } else {
            sql.append("   AND option_name1 = :optionName1\n   AND option_name2 = :optionName2\n"
                    + "   AND option_name3 = :optionName3\n");
        }
        if (hasPrice) {
            sql.append("   AND option_price = :optionPrice\n");
        }
        var query = entityManager.createNativeQuery(sql.toString())
                .setParameter("gdsId", ((Number) s.get("gdsId")).intValue())
                .setParameter("optionType", s.get("optionType"))
                .setParameter("optionName2", s.get("optionName2"));
        if (!typeS) {
            query.setParameter("optionName1", s.get("optionName1"))
                    .setParameter("optionName3", s.get("optionName3"));
        }
        if (hasPrice) {
            query.setParameter("optionPrice", Integer.parseInt(optionPrice.trim()));
        }
        List<Object[]> rows = query.getResultList();
        if (rows.isEmpty()) {
            return null;
        }
        Object[] r = rows.get(0);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("item_option_id", r[0]);
        m.put("option_price", r[1]);
        m.put("option_stock_flag", r[2]);
        m.put("option_stock_quantity", r[3]);
        return m;
    }
}
