package com.ghlove.gift.service;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.event.GiftLifecyclePublisher;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.OffRprsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 관리자 > 답례품관리 > 답례품관리(AS-IS opmanager/item/list, 메뉴 16301) - 답례품 목록·일괄처리(품절/공개)·삭제신청·변경사항 로그와
 * 지자체 대표 답례품 / 오프라인 대표 답례품 관리. AS-IS ItemManagerController + ItemServiceImpl + item-mapper.xml 그대로.
 *
 * <p>TO-BE 번역: 목록의 지자체는 판매자의 지자체(OP_SELLER.LOCGOV_CODE)를 본다(AS-IS 매퍼와 같다). 상품 코드(ITEM_USER_CODE)·대표이미지
 * (ITEM_IMAGE)는 TO-BE 답례품에 비어 있어서 AS-IS 채번 규칙({@code "G2" + lPad(itemId, 9, '0')})과 OP_ITEM_IMAGE 첫 장으로 보충한다.
 * 장바구니 삭제(AS-IS deleteCartBySoldOut)는 order 서비스 소관이라 건드리지 않는다 - order의 장바구니 조회가 품절 항목을 자동으로 지운다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ItemManageService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter MSG_TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** AS-IS ShopUtils.getItemStatusText. */
    private static String statusText(String code) {
        return switch (code) {
            case "1" -> "정상";
            case "20" -> "등록신청";
            case "21" -> "등록반려";
            case "30" -> "수정신청";
            case "31" -> "수정반려";
            case "40" -> "삭제신청";
            case "41" -> "삭제반려";
            case "90" -> "판매종료";
            case "99" -> "삭제";
            default -> "기타";
        };
    }

    /** AS-IS ShopUtils.getItemStatusMessage - "[상태 시각] 메시지\n". */
    private static String statusMessage(String code, String message) {
        return "[" + statusText(code) + " " + LocalDateTime.now().format(MSG_TS) + "] " + message + "\n";
    }

    /** 답례품승인관리 목록 구분(Search.listType). */
    public static final String PENDING_LIST = "pending";
    /** 상품검색 팝업(AS-IS item/find-item, conditionType FIND_ITEM_POPUP) 구분(Search.listType). */
    public static final String FIND_LIST = "find";

    /** 화면에 보이는 상품코드 - {@link #userCode}와 같은 식. 저장값이 비어 있는 TO-BE 답례품도 보이는 코드로 검색되게 한다. */
    private static final String USER_CODE_EXPR = "coalesce(nullif(i.item_user_code, ''), 'G2' || lpad(cast(i.item_id as varchar), 9, '0'))";

    /** 품절 판정 - AS-IS ITEM_SOLD_OUT_FLAG CASE 식(옵션 NULL = 옵션 없음 N). */
    private static final String SOLD_OUT_CASE = """
            (case when i.sold_out = '1' then 'Y'
                  when coalesce(i.item_option_flag, 'N') = 'N' and i.stock_flag = 'Y' and i.stock_quantity = 0 then 'Y'
                  when i.item_option_flag = 'Y' and i.item_option_type = 'T' and i.stock_flag = 'Y' and i.stock_quantity = 0 then 'Y'
                  when i.item_option_flag = 'Y' and iost.item_option_sold_out_flag = 'Y' then 'Y'
                  else 'N' end)""";

    private final EntityManager em;
    private final GiftRepository giftRepository;
    private final GiftService giftService;
    private final OffRprsRepository offRprsRepository;
    private final GiftLifecyclePublisher lifecyclePublisher;

    // ================= 목록 =================

    /** 목록 조회 조건 - AS-IS ItemParam 중 이 화면이 쓰는 것. locgov는 지자체 담당자의 소속 지자체(서버가 강제). */
    public record Search(String locgov, String shLocgovCode, String where, String query, String srchDateType,
                         String searchStartDate, String searchEndDate, Integer categoryGroupId, String categoryClass,
                         Integer categoryId, String saleStatus, String displayFlag, Long sellerId, String orderBy, String sort,
                         String listType, String dataStatusCode) {
    }

    public record ItemRow(Long itemId, String itemCode, String itemUserCode, String itemName, String itemSellerCode,
                          Integer salePrice, String itemPrice, String displayFlag, String dataStatusCode,
                          Integer orderMinQuantity, Integer orderMaxQuantity, String itemOptionFlag, String itemTextOptionFlag,
                          String stockFlag, Integer stockQuantity, String soldOut, String itemSoldOutFlag, String itemType,
                          String itemLabel, Long sellerId, String sellerName, String companyName, String sellerLocgovCode,
                          Integer hits, String brand, String createdDate, String updatedDate, String displayCommissionRate,
                          Integer minusSpotDiscount, Integer exceptSpotDiscount, Integer presentPrice, String thumbnail,
                          String dataStatusMessage) {
    }

    private record Built(String from, String where, Map<String, Object> params, boolean orderingJoin) {
    }

    /** 주문 상세 등 다른 화면이 품목 이미지를 그리려고 쓴다 - 답례품ID별 대표 이미지 파일명. */
    @Transactional(readOnly = true)
    public Map<Long, String> thumbnails(List<Long> itemIds) {
        return itemIds == null || itemIds.isEmpty() ? Map.of() : giftService.thumbnailsOf(itemIds);
    }

    @Transactional(readOnly = true)
    public int count(Search s) {
        Built b = build(s);
        Query q = em.createNativeQuery("select count(*) from (select i.item_id from " + b.from() + " where " + b.where()
                + " group by i.item_id) t");
        b.params().forEach(q::setParameter);
        return ((Number) q.getSingleResult()).intValue();
    }

    @Transactional(readOnly = true)
    public List<ItemRow> list(Search s, int offset, int limit) {
        Built b = build(s);
        String dir = "ASC".equalsIgnoreCase(s.sort()) ? "asc" : "desc";
        String order = switch (s.orderBy() == null ? "" : s.orderBy()) {
            case "ORDERING" -> b.orderingJoin() ? "min(io2.ordering) " + dir + ", i.item_id desc" : "i.item_id desc";
            case "SALE_PRICE" -> "(i.sale_price - coalesce(i.seller_discount_amount, 0) - coalesce(i.spot_discount_amount, 0)) " + dir;
            case "HITS" -> "i.hits " + dir;
            case "ITEM_ID" -> "i.item_id " + dir;
            default -> "i.item_id desc";
        };
        Query idq = em.createNativeQuery("select i.item_id from " + b.from() + " where " + b.where() + " group by i.item_id order by "
                + order + " limit :limit offset :offset");
        b.params().forEach(idq::setParameter);
        idq.setParameter("limit", limit > 0 ? limit : 10).setParameter("offset", Math.max(offset, 0));
        List<Long> ids = new ArrayList<>();
        for (Object o : idq.getResultList()) {
            ids.add(((Number) o).longValue());
        }
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, ItemRow> byId = new LinkedHashMap<>();
        for (ItemRow r : rows(ids)) {
            byId.put(r.itemId(), r);
        }
        List<ItemRow> ordered = new ArrayList<>();
        for (Long id : ids) {
            ItemRow r = byId.get(id);
            if (r != null) {
                ordered.add(r);
            }
        }
        return ordered;
    }

    /** 조회 조건 조립 - AS-IS item-mapper getItemCount/getItemList의 ITEM_LIST_MIG 분기 그대로. */
    private Built build(Search s) {
        Map<String, Object> p = new LinkedHashMap<>();
        StringBuilder from = new StringBuilder("public.op_item i inner join public.op_seller s on i.seller_id = s.seller_id and s.status_code = '2'");
        List<String> w = new ArrayList<>();

        String sale = nz(s.saleStatus());
        if (sale.equals("soldOut") || sale.equals("sale")) {
            from.append(" left join public.op_item_option_soldout iost on i.item_id = iost.item_id");
        }

        // 카테고리 - 선택한 가장 깊은 차수의 코드 접두 일치. 차수를 안 고르고 그룹만 고르면 그룹의 1차 카테고리들.
        List<String> classPrefixes = null;
        String categoryClass = nz(s.categoryClass());
        int groupId = s.categoryGroupId() == null ? 0 : s.categoryGroupId();
        if (!categoryClass.isEmpty()) {
            classPrefixes = List.of(categoryClass);
        } else if (groupId > 0) {
            classPrefixes = new ArrayList<>();
            Query cq = em.createNativeQuery("select substring(category_code, 1, 3) from public.op_category "
                    + "where category_group_id = :g and category_flag = 'Y' and category_level = '1'").setParameter("g", groupId);
            for (Object o : cq.getResultList()) {
                classPrefixes.add(String.valueOf(o));
            }
        }
        boolean orderingJoin = false;
        if (classPrefixes != null) {
            if (classPrefixes.isEmpty()) {
                w.add("1 = 0");
            } else {
                from.append(" inner join public.op_item_category ic on i.item_id = ic.item_id")
                        .append(" inner join public.op_category c on ic.category_id = c.category_id and c.category_flag = 'Y' and (");
                for (int k = 0; k < classPrefixes.size(); k++) {
                    from.append(k == 0 ? "" : " or ").append("c.category_code like :cc").append(k);
                    p.put("cc" + k, classPrefixes.get(k) + "%");
                }
                from.append(")");
            }
            if ("ORDERING".equals(s.orderBy())) {
                from.append(" left join public.op_item_ordering io2 on i.item_id = io2.item_id");
                if (s.categoryId() != null && s.categoryId() > 0) {
                    from.append(" and io2.category_id = :ioCategoryId");
                    p.put("ioCategoryId", s.categoryId());
                }
                orderingJoin = true;
            }
        }
        String where = nz(s.where());
        String query = nz(s.query());
        if (where.equals("ITEM_USER_CODE") && !query.isEmpty()) {
            from.append(" left join public.op_item_option io on i.item_id = io.item_id and io.option_display_flag = 'Y'");
        }

        if (nz(s.locgov()).isEmpty()) {
            w.add("s.locgov_code is not null");
        } else {
            w.add("s.locgov_code = :locgov");
            p.put("locgov", s.locgov());
        }
        // 답례품승인관리(AS-IS conditionType ITEM_LIST_PENDING_APPROVAL): 상태 라디오를 고르면 그 상태만, 아니면 승인 대기 계열 6종 전부.
        boolean pending = PENDING_LIST.equals(s.listType());
        // 상품검색 팝업(AS-IS FIND_ITEM_POPUP): 컨트롤러가 dataStatusCode 1·displayFlag Y를 넣는다. ITEM_DATA_TYPE 조건은 없다(추가구성도 나온다).
        boolean find = FIND_LIST.equals(s.listType());
        if (pending) {
            if (!nz(s.dataStatusCode()).isEmpty()) {
                w.add("i.data_status_code = :dataStatusCode");
                p.put("dataStatusCode", s.dataStatusCode());
            } else {
                w.add("i.data_status_code in ('20', '21', '30', '31', '40', '41')");
            }
        } else if (find) {
            w.add("i.data_status_code = :dataStatusCode");
            p.put("dataStatusCode", nz(s.dataStatusCode()));
        } else {
            w.add("i.data_status_code in ('0', '1', '90')");
        }
        if (!nz(s.displayFlag()).isEmpty()) {
            w.add("i.display_flag = :displayFlag");
            p.put("displayFlag", s.displayFlag());
        }
        if (!find) {
            w.add("i.item_data_type = '1'");
        }
        if (s.sellerId() != null && s.sellerId() > 0) {
            w.add("i.seller_id = :sellerId");
            p.put("sellerId", s.sellerId());
        }
        if (!nz(s.searchStartDate()).isEmpty()) {
            w.add((pending || "update".equals(s.srchDateType()) ? "i.updated_date" : "i.created_date")
                    + " >= concat(cast(:startDate as varchar), '000000')");
            p.put("startDate", s.searchStartDate());
        }
        if (!nz(s.searchEndDate()).isEmpty()) {
            w.add((pending || "update".equals(s.srchDateType()) ? "i.updated_date" : "i.created_date")
                    + " <= concat(cast(:endDate as varchar), '235959')");
            p.put("endDate", s.searchEndDate());
        }
        if (!query.isEmpty()) {
            switch (where) {
                case "ITEM_USER_CODE" -> {
                    w.add("(" + USER_CODE_EXPR + " like concat('%', cast(:query as varchar), '%') or io.option_stock_code like concat('%', cast(:query as varchar), '%'))");
                    p.put("query", query);
                }
                case "ITEM_NAME" -> {
                    w.add("(i.item_name like :qName or " + USER_CODE_EXPR + " like :qName or i.item_keyword like :qName or i.item_summary like :qName"
                            + " or i.item_id in (select item_id from public.op_item_option where option_stock_code like :qName))");
                    p.put("qName", "%" + query.replace(" ", "%") + "%");
                }
                case "COMPANY_NAME" -> {
                    w.add("s.company_name like concat('%', cast(:query as varchar), '%')");
                    p.put("query", query);
                }
                default -> {
                }
            }
        }
        switch (sale) {
            case "sale" -> w.add("i.data_status_code = '1' and " + SOLD_OUT_CASE + " = 'N'");
            case "soldOut" -> w.add("i.data_status_code = '1' and " + SOLD_OUT_CASE + " = 'Y'");
            case "saleEnd" -> w.add("i.data_status_code = '90'");
            case "mig" -> w.add("i.data_status_code = '0'");
            default -> {
            }
        }
        if (!nz(s.shLocgovCode()).isEmpty()) {
            w.add("s.locgov_code = :shLocgovCode");
            p.put("shLocgovCode", s.shLocgovCode());
        }
        return new Built(from.toString(), String.join(" and ", w), p, orderingJoin);
    }

    @SuppressWarnings("unchecked")
    private List<ItemRow> rows(List<Long> ids) {
        Query q = em.createNativeQuery("""
                select i.item_id, i.item_code, i.item_user_code, i.item_name, i.item_seller_code, i.sale_price, i.item_price,
                       i.display_flag, i.data_status_code, i.order_min_quantity, i.order_max_quantity,
                       coalesce(i.item_option_flag, 'N'), coalesce(i.item_text_option_flag, 'N'), i.stock_flag, i.stock_quantity,
                       i.sold_out, %s, i.item_type, i.item_label, i.seller_id, s.seller_name, s.company_name, s.locgov_code,
                       i.hits, i.brand, i.created_date, i.updated_date, i.commission_type, i.commission_rate, i.supply_price,
                       s.commission_rate, i.seller_discount_amount, i.spot_discount_amount, i.seller_discount_flag, i.spot_flag,
                       i.data_status_message
                  from public.op_item i
                       inner join public.op_seller s on s.seller_id = i.seller_id
                       left join public.op_item_option_soldout iost on iost.item_id = i.item_id
                 where i.item_id in (:ids)
                """.formatted(SOLD_OUT_CASE)).setParameter("ids", ids);
        Map<Long, String> thumbs = giftService.thumbnailsOf(ids);
        List<ItemRow> rows = new ArrayList<>();
        for (Object o : q.getResultList()) {
            Object[] r = (Object[]) o;
            long itemId = ((Number) r[0]).longValue();
            Integer salePrice = i(r[5]);
            // 판매자 즉시할인/스팟할인은 답례품에서 쓰지 않는다 - 플래그가 Y일 때만 AS-IS ItemBase 식대로 뺀다.
            int sellerDiscount = "Y".equals(s(r[33])) ? nzInt(r[31]) : 0;
            int except = (salePrice == null ? 0 : salePrice) - sellerDiscount;
            int minus = except - ("Y".equals(s(r[34])) ? nzInt(r[32]) : 0);
            rows.add(new ItemRow(itemId, s(r[1]), userCode(s(r[2]), itemId), s(r[3]), s(r[4]), salePrice, s(r[6]), s(r[7]), s(r[8]),
                    i(r[9]), i(r[10]), s(r[11]), s(r[12]), s(r[13]), i(r[14]), s(r[15]), s(r[16]), s(r[17]), s(r[18]),
                    r[19] == null ? null : ((Number) r[19]).longValue(), s(r[20]), s(r[21]), s(r[22]), i(r[23]), s(r[24]), s(r[25]),
                    s(r[26]), commissionRate(s(r[17]), s(r[27]), r[28], r[29], r[30], salePrice), minus, except, minus,
                    thumbs.get(itemId), s(r[35])));
        }
        return rows;
    }

    /** AS-IS Item.getDisplayCommissionRate - 상품형태 3은 0, 수수료구분 1은 판매자 수수료율, 3은 (판매가-공급가)/판매가. */
    private static String commissionRate(String itemType, String commissionType, Object rate, Object supplyPrice, Object sellerRate,
                                         Integer salePrice) {
        float commissionRate = rate == null ? 0f : ((Number) rate).floatValue();
        if ("3".equals(itemType)) {
            return Float.toString(0f);
        }
        if ("1".equals(commissionType) && sellerRate != null) {
            commissionRate = ((Number) sellerRate).floatValue();
        }
        if ("3".equals(commissionType) && salePrice != null && salePrice != 0) {
            int supply = supplyPrice == null ? 0 : ((Number) supplyPrice).intValue();
            commissionRate = ((float) (salePrice - supply) / salePrice) * 100f;
            commissionRate = Math.round(commissionRate * 10f) / 10f;
        }
        return Float.toString(commissionRate);
    }

    /** TO-BE 답례품에는 ITEM_USER_CODE가 비어 있다 - AS-IS 채번 규칙(G2 + 9자리)으로 보여준다. */
    private static String userCode(String stored, long itemId) {
        return stored != null && !stored.isBlank() ? stored : "G2" + String.format("%09d", itemId);
    }

    // ================= 일괄 처리 / 삭제 =================

    /** AS-IS updateListDataByDisplay - 선택한 답례품의 공개유무. */
    public int updateDisplay(List<Long> ids, String flag, long managerId) {
        if (!"Y".equals(flag) && !"N".equals(flag)) {
            throw new GiftException("공개유무는 Y 또는 N이어야 합니다.");
        }
        for (Long id : ids) {
            em.createNativeQuery("update public.op_item set display_flag = :f, updated_user_id = :u, updated_date = :d where item_id = :id")
                    .setParameter("f", flag).setParameter("u", managerId).setParameter("d", now()).setParameter("id", id).executeUpdate();
        }
        publish(ids);
        return ids.size();
    }

    /**
     * AS-IS updateListDataByLabel - 품절('1')/품절해제('0')/판매종료('90'). 품절 처리는 변경 전 값을 변경사항 로그에 먼저 남긴다
     * (AS-IS도 로그 insert가 update보다 앞이라 로그에는 처리 직전 상태가 기록된다).
     */
    public int updateLabel(List<Long> ids, String flag, long managerId) {
        if (!"1".equals(flag) && !"0".equals(flag) && !"90".equals(flag)) {
            throw new GiftException("처리 구분이 올바르지 않습니다.");
        }
        for (Long id : ids) {
            String message = null;
            if ("90".equals(flag)) {
                message = statusMessage("90", "관리자판매 종료 처리");
            } else if ("1".equals(flag)) {
                insertLog(id, managerId, "update");
            }
            StringBuilder sql = new StringBuilder("update public.op_item set ");
            if ("1".equals(flag) || "0".equals(flag)) {
                sql.append("sold_out = :flag, ");
            }
            if ("90".equals(flag)) {
                sql.append("data_status_code = '90', ");
            }
            if (message != null) {
                sql.append("data_status_message = :msg, ");
            }
            sql.append("updated_user_id = :u, updated_date = :d where item_id = :id");
            Query q = em.createNativeQuery(sql.toString()).setParameter("u", managerId).setParameter("d", now()).setParameter("id", id);
            if ("1".equals(flag) || "0".equals(flag)) {
                q.setParameter("flag", flag);
            }
            if (message != null) {
                q.setParameter("msg", message);
            }
            q.executeUpdate();
        }
        publish(ids);
        return ids.size();
    }

    /** AS-IS deleteListData - 삭제는 바로 지우지 않고 삭제신청(40)으로 바꾸고 변경사항 로그를 남긴다(관리자 승인 프로세스). */
    public int delete(List<Long> ids, long managerId) {
        for (Long id : ids) {
            em.createNativeQuery("update public.op_item set data_status_code = '40', "
                            + "data_status_message = coalesce(data_status_message, '') || :msg, updated_user_id = :u, updated_date = :d where item_id = :id")
                    .setParameter("msg", statusMessage("40", "판매자 답례품 삭제 신청. (승인대기)"))
                    .setParameter("u", managerId).setParameter("d", now()).setParameter("id", id).executeUpdate();
            insertLog(id, managerId, "delete");
        }
        publish(ids);
        return ids.size();
    }

    /** AS-IS insertItemLogForApproval - OP_ITEM 현재값을 스냅샷으로 OP_ITEM_LOG에 쌓는다. */
    private void insertLog(Long itemId, long managerId, String actionType) {
        insertLog(itemId, managerId, actionType, 0L);
    }

    /** createdSellerId - AS-IS는 승인처리 때만 상품의 SELLER_ID를 넣고(item이 조회된 Item), 그 밖에는 새 Item이라 0이다. */
    private void insertLog(Long itemId, long managerId, String actionType, long createdSellerId) {
        em.createNativeQuery("""
                insert into public.op_item_log (item_id, item_user_code, item_name, display_flag, sold_out, cost_price, item_price,
                       sale_price, commission_type, commission_rate, price_criteria, process_page, created_manager_id, created_seller_id,
                       created_date, action_type)
                select i.item_id, coalesce(nullif(i.item_user_code, ''), 'G2' || lpad(cast(i.item_id as varchar), 9, '0')), i.item_name,
                       i.display_flag, i.sold_out, i.cost_price, i.item_price, i.sale_price, i.commission_type, i.commission_rate,
                       i.price_criteria, 'manager', :managerId, :sellerId, :d, :action
                  from public.op_item i where i.item_id = :id
                """).setParameter("managerId", managerId).setParameter("sellerId", createdSellerId).setParameter("d", now())
                .setParameter("action", actionType).setParameter("id", itemId).executeUpdate();
    }

    /** 통계 ReadModel(admin)이 쓰는 답례품 이벤트 - 상태/공개 변경 뒤 다시 보낸다. */
    private void publish(List<Long> ids) {
        em.flush();
        em.clear();
        for (Gift g : giftRepository.findAllById(ids)) {
            lifecyclePublisher.publish(g);
        }
    }

    // ================= 답례품승인관리 - 승인처리 / 등록보류 =================

    /** 처리된 답례품 - admin이 국민비서 문자(승인/거절)를 만들 때 쓴다. deleted=삭제신청 승인(문자 없음). */
    public record ProcessedItem(Long itemId, String itemUserCode, Long sellerId, boolean deleted) {
    }

    /**
     * AS-IS ItemServiceImpl.updateListData(processType=approval) - 삭제신청(40)은 삭제 승인(deleteItemApproval: 상품코드에 _시각을 붙여
     * 코드를 비우고 99로), 그 밖에는 정상(1)으로 바꾸고 승인 로그를 남긴다(updateItemApproval). 서버는 현재 상태를 검사하지 않는다(AS-IS 그대로).
     */
    public List<ProcessedItem> approve(List<Long> ids, long managerId) {
        List<ProcessedItem> processed = new ArrayList<>();
        for (Long id : ids) {
            Object[] head = itemHead(id);
            String status = String.valueOf(head[0]);
            Long sellerId = head[1] == null ? null : ((Number) head[1]).longValue();
            String userCode = String.valueOf(head[2]);
            if ("40".equals(status)) {
                // AS-IS item-mapper deleteItem - 상품코드를 풀어 주려고 "코드_시각"으로 바꾸고 99(삭제)로. 로그·문자는 없다.
                em.createNativeQuery("update public.op_item i set item_user_code = " + USER_CODE_EXPR + " || '_' || :d, data_status_code = '99', "
                                + "data_status_message = coalesce(i.data_status_message, '') || :msg, updated_user_id = :u, updated_date = :d where i.item_id = :id")
                        .setParameter("msg", statusMessage("99", "운영자 승인처리."))
                        .setParameter("u", managerId).setParameter("d", now()).setParameter("id", id).executeUpdate();
                processed.add(new ProcessedItem(id, userCode, sellerId, true));
            } else {
                em.createNativeQuery("update public.op_item set data_status_code = '1', data_status_message = coalesce(data_status_message, '') || :msg, "
                                + "updated_user_id = :u, updated_date = :d where item_id = :id")
                        .setParameter("msg", statusMessage("1", "운영자 승인처리."))
                        .setParameter("u", managerId).setParameter("d", now()).setParameter("id", id).executeUpdate();
                insertLog(id, managerId, "approval", sellerId == null ? 0L : sellerId);
                processed.add(new ProcessedItem(id, userCode, sellerId, false));
            }
        }
        publish(ids);
        return processed;
    }

    /** AS-IS updateListData(processType=reject) - 등록보류(21), 사유를 상태 메시지에 쌓고 로그를 남긴다. 상태는 검사하지 않는다(AS-IS 그대로). */
    public List<ProcessedItem> reject(List<Long> ids, String message, long managerId) {
        List<ProcessedItem> processed = new ArrayList<>();
        for (Long id : ids) {
            Object[] head = itemHead(id);
            em.createNativeQuery("update public.op_item set data_status_code = '21', data_status_message = coalesce(data_status_message, '') || :msg, "
                            + "updated_user_id = :u, updated_date = :d where item_id = :id")
                    .setParameter("msg", statusMessage("21", message == null ? "" : message))
                    .setParameter("u", managerId).setParameter("d", now()).setParameter("id", id).executeUpdate();
            insertLog(id, managerId, "reject");
            processed.add(new ProcessedItem(id, String.valueOf(head[2]), head[1] == null ? null : ((Number) head[1]).longValue(), false));
        }
        publish(ids);
        return processed;
    }

    /** [상태, 판매자 번호, 화면 상품코드] - 없으면 AS-IS는 NPE(500)라서 업무 예외로 알린다. */
    private Object[] itemHead(Long id) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("select i.data_status_code, i.seller_id, " + USER_CODE_EXPR + " from public.op_item i where i.item_id = :id")
                .setParameter("id", id).getResultList();
        if (rows.isEmpty()) {
            throw new GiftException("답례품 정보가 없습니다.");
        }
        return rows.get(0);
    }

    // ================= 답례품승인관리 - 미리보기 =================

    public record PreviewImage(String imageName, Integer ordering) {
    }

    public record PreviewOption(Long itemOptionId, String optionName1, String optionName2, String optionName3, Integer optionPrice,
                                String optionHideFlag, String optionDisplayFlag, String optionStockFlag, Integer optionStockQuantity,
                                String optionSoldOutFlag) {
    }

    public record PreviewInfo(String title, String description) {
    }

    public record PreviewSeller(String companyName, String telephoneNumber, String businessNumber, String businessLocation, String addressDetail) {
    }

    public record Preview(Long itemId, String itemUserCode, String itemName, String itemSummary, String originCountry, Integer salePrice,
                          String locgovCode, String deliveryCompanyName, String itemOptionFlag, String itemOptionType,
                          String itemOptionTitle1, String itemOptionTitle2, String itemOptionTitle3,
                          String itemTextOptionFlag, String itemTextOptionTitle1, String itemTextOptionTitle2, String itemTextOptionTitle3,
                          Integer orderMinQuantity, Integer orderMaxQuantity, String itemReturnFlag, String detailContent,
                          String teamName, String groupName, String categoryName,
                          List<PreviewImage> images, List<PreviewOption> options, List<PreviewInfo> infos, PreviewSeller seller) {
    }

    /**
     * AS-IS getItemByItemUserCodePreview(getItemByParamPreview + bindItemAdditionInfo) + getSellerByIdPreview. 상품코드는 화면에 보이는 코드
     * (G2+9자리 포함)로 찾는다. 이미지는 큰 사이즈(L = thumbnail_large, 없으면 원본), 옵션은 OPTION_DISPLAY_FLAG='Y'만 ITEM_OPTION_ID 순.
     * 빵부스러기는 AS-IS getBreadcrumbListByCollection의 첫 상품 카테고리 - 1차 카테고리와 그 그룹·팀. 지자체명은 지자체 표가 admin/donation 소관이라
     * 코드(locgovCode)만 내려주고 admin이 AS-IS CASE(시도=시군구면 시도만)대로 만든다.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Preview preview(String itemUserCode) {
        List<Object[]> heads = em.createNativeQuery("""
                select i.item_id, %s, i.item_name, i.item_summary, i.origin_country, i.sale_price,
                       s.locgov_code,
                       i.delivery_company_name, i.item_option_flag, i.item_option_type, i.item_option_title1, i.item_option_title2,
                       i.item_option_title3, i.item_text_option_flag, i.item_text_option_title1, i.item_text_option_title2,
                       i.item_text_option_title3, i.order_min_quantity, i.order_max_quantity, i.item_return_flag, i.detail_content,
                       s.company_name, s.telephone_number, s.business_number, s.business_location, s.address_detail
                  from public.op_item i inner join public.op_seller s on s.seller_id = i.seller_id and s.status_code = '2'
                 where %s = :code
                """.formatted(USER_CODE_EXPR, USER_CODE_EXPR)).setParameter("code", itemUserCode).getResultList();
        if (heads.isEmpty()) {
            throw new GiftException("답례품정보가 없습니다.");
        }
        Object[] h = heads.get(0);
        long itemId = ((Number) h[0]).longValue();

        List<PreviewImage> images = new ArrayList<>();
        for (Object[] r : (List<Object[]>) em.createNativeQuery("select coalesce(nullif(thumbnail_large, ''), image_name), ordering "
                + "from public.op_item_image where item_id = :id order by item_image_id").setParameter("id", itemId).getResultList()) {
            images.add(new PreviewImage(s(r[0]), i(r[1])));
        }
        List<PreviewOption> options = new ArrayList<>();
        for (Object[] r : (List<Object[]>) em.createNativeQuery("select item_option_id, option_name1, option_name2, option_name3, option_price, "
                + "option_hide_flag, option_display_flag, option_stock_flag, option_stock_quantity, option_sold_out_flag "
                + "from public.op_item_option where item_id = :id and option_display_flag = 'Y' order by item_option_id").setParameter("id", itemId).getResultList()) {
            options.add(new PreviewOption(l(r[0]), s(r[1]), s(r[2]), s(r[3]), i(r[4]), s(r[5]), s(r[6]), s(r[7]), i(r[8]), s(r[9])));
        }
        List<PreviewInfo> infos = new ArrayList<>();
        for (Object[] r : (List<Object[]>) em.createNativeQuery("select title, description from public.op_item_info where item_id = :id order by item_info_id")
                .setParameter("id", itemId).getResultList()) {
            infos.add(new PreviewInfo(s(r[0]), s(r[1])));
        }

        // 빵부스러기 - 첫 상품 카테고리(OP_ITEM_CATEGORY 등록순)의 1차 카테고리와 그 그룹·팀
        String teamName = null;
        String groupName = null;
        String categoryName = null;
        List<Object[]> crumbs = em.createNativeQuery("""
                select ct.name, cg.name, c1.category_name
                  from public.op_item_category ic
                       inner join public.op_category c on c.category_id = ic.category_id and c.category_flag = 'Y'
                       left join public.op_category c1 on c1.category_code = concat(c.category_class1, '000000000')
                       left join public.op_category_group cg on cg.category_group_id = c1.category_group_id
                       left join public.op_category_team ct on ct.category_team_id = cg.category_team_id
                 where ic.item_id = :id
                 order by ic.item_category_id
                 limit 1
                """).setParameter("id", itemId).getResultList();
        if (!crumbs.isEmpty()) {
            teamName = s(crumbs.get(0)[0]);
            groupName = s(crumbs.get(0)[1]);
            categoryName = s(crumbs.get(0)[2]);
        }

        return new Preview(itemId, s(h[1]), s(h[2]), s(h[3]), s(h[4]), i(h[5]), s(h[6]), s(h[7]), s(h[8]), s(h[9]), s(h[10]), s(h[11]), s(h[12]),
                s(h[13]), s(h[14]), s(h[15]), s(h[16]), i(h[17]), i(h[18]), s(h[19]), s(h[20]), teamName, groupName, categoryName,
                images, options, infos, new PreviewSeller(s(h[21]), s(h[22]), s(h[23]), s(h[24]), s(h[25])));
    }

    // ================= 변경사항 로그 =================

    public record LogRow(Long itemLogId, Long itemId, String itemUserCode, String itemName, String displayFlag, String soldOut,
                         Integer salePrice, String processPage, Long createdManagerId, String createdDate, String actionType,
                         String sellerLoginId, String sellerName) {
    }

    @Transactional(readOnly = true)
    public int logCount(long itemId) {
        return ((Number) em.createNativeQuery("select count(*) from public.op_item_log where item_id = :id")
                .setParameter("id", itemId).getSingleResult()).intValue();
    }

    /** AS-IS getItemLogListById - 등록/수정자는 OP_ITEM_LOG.CREATED_MANAGER_ID로 OP_SELLER를 바로 잇는다(AS-IS 그대로). */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<LogRow> logList(long itemId, int offset, int limit) {
        List<Object[]> rows = em.createNativeQuery("""
                select il.item_log_id, il.item_id, il.item_user_code, il.item_name, il.display_flag, il.sold_out, il.sale_price,
                       il.process_page, il.created_manager_id, il.created_date, il.action_type, s.login_id, s.seller_name
                  from public.op_item_log il left outer join public.op_seller s on il.created_manager_id = s.seller_id
                 where il.item_id = :id
                 order by il.item_log_id desc
                 limit :limit offset :offset
                """).setParameter("id", itemId).setParameter("limit", limit > 0 ? limit : 10).setParameter("offset", Math.max(offset, 0))
                .getResultList();
        List<LogRow> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new LogRow(l(r[0]), l(r[1]), s(r[2]), s(r[3]), s(r[4]), s(r[5]), i(r[6]), s(r[7]), l(r[8]), s(r[9]), s(r[10]),
                    s(r[11]), s(r[12])));
        }
        return result;
    }

    // ================= 대표 답례품 (지자체당 최대 4개) =================

    public record RprsRow(Long itemId, String itemUserCode, String dataStatusCode, String displayFlag, String itemCode, String itemName,
                          Integer salePrice, String itemSoldOutFlag, String thumbnail) {
    }

    /** AS-IS getLclgvRprsGdsList(Manager) - 지자체의 대표 답례품을 SORT_SEQ 순으로. */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<RprsRow> rprsList(String locgov) {
        List<Object[]> rows = em.createNativeQuery("""
                select i.item_id, i.item_user_code, i.data_status_code, i.display_flag, i.item_code, i.item_name, i.sale_price, %s
                  from public.g_lclgv_rprs_gds_mng lrgm
                       join public.op_item i on lrgm.gds_id = i.item_id
                       left join public.op_item_option_soldout iost on i.item_id = iost.item_id
                 where lrgm.lclgv_cd = :locgov
                 order by lrgm.sort_seq
                """.formatted(SOLD_OUT_CASE)).setParameter("locgov", locgov).getResultList();
        List<Long> ids = rows.stream().map(r -> ((Number) r[0]).longValue()).toList();
        Map<Long, String> thumbs = ids.isEmpty() ? Map.of() : giftService.thumbnailsOf(ids);
        List<RprsRow> result = new ArrayList<>();
        for (Object[] r : rows) {
            long itemId = ((Number) r[0]).longValue();
            result.add(new RprsRow(itemId, userCode(s(r[1]), itemId), s(r[2]), s(r[3]), s(r[4]), s(r[5]), i(r[6]), s(r[7]), thumbs.get(itemId)));
        }
        return result;
    }

    /**
     * AS-IS insertLclgvRprsGds - 지자체의 대표 답례품을 통째로 지우고, 그 지자체 소속(판매자 지자체)의 정상(1) 답례품인 것만 다시 넣는다.
     * itemIds가 비어 있으면 전체 삭제만 한다.
     */
    public void saveRprs(String locgov, List<Long> itemIds, long userId) {
        em.createNativeQuery("delete from public.g_lclgv_rprs_gds_mng where lclgv_cd = :locgov").setParameter("locgov", locgov).executeUpdate();
        List<Long> valid = locgovItemIds(itemIds, locgov);
        for (int k = 0; k < valid.size(); k++) {
            em.createNativeQuery("insert into public.g_lclgv_rprs_gds_mng (lclgv_cd, gds_id, sort_seq, frst_rgtr_id, frst_reg_dt) "
                            + "values (:locgov, :id, :seq, :u, now())")
                    .setParameter("locgov", locgov).setParameter("id", valid.get(k)).setParameter("seq", k + 1).setParameter("u", userId)
                    .executeUpdate();
        }
    }

    /** AS-IS getLocgovItemIds - 정상(1) 상태이고 판매자(정상)의 지자체가 일치하는 답례품만. 요청 순서를 지킨다. */
    private List<Long> locgovItemIds(List<Long> itemIds, String locgov) {
        List<Long> valid = new ArrayList<>();
        if (itemIds == null) {
            return valid;
        }
        for (Long id : itemIds) {
            Number n = (Number) em.createNativeQuery("select count(*) from public.op_item i inner join public.op_seller s "
                            + "on s.seller_id = i.seller_id and s.status_code = '2' where i.item_id = :id and i.data_status_code = '1' and s.locgov_code = :locgov")
                    .setParameter("id", id).setParameter("locgov", locgov).getSingleResult();
            if (n.intValue() > 0) {
                valid.add(id);
            }
        }
        return valid;
    }

    // ================= 오프라인 대표 답례품 (지자체당 최대 2개) =================

    public record OffRprsRow(Long itemId, String itemUserCode, String dataStatusCode, String displayFlag, String itemCode, String itemName,
                             Integer salePrice, String itemOptionFlag, Integer stockQuantity, String options, String itemSoldOutFlag,
                             String itemOptionId, List<String> optionStockQuantity, String changedOption, String thumbnail) {
    }

    /**
     * AS-IS getLclgvOffRprsGdsListManager - 저장한 옵션 문자열(S: 4토막, S3: 8토막)로 OP_ITEM_OPTION 한 건을 찾아 붙이고,
     * 못 찾으면 옵션이 바뀐 것(changedOption=Y)으로 표시한다. 옵션이 없는 답례품은 N.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<OffRprsRow> offRprsList(String locgov) {
        List<Object[]> rows = em.createNativeQuery("""
                select i.item_id, i.item_user_code, i.data_status_code, i.display_flag, i.item_code, i.item_name, i.sale_price,
                       i.item_option_flag, i.stock_quantity, lorgm.options, %s
                  from public.g_lclgv_off_rprs_gds_mng lorgm
                       join public.op_item i on lorgm.gds_id = i.item_id
                       left join public.op_item_option_soldout iost on i.item_id = iost.item_id
                 where lorgm.lclgv_cd = :locgov
                 order by lorgm.sort_seq
                """.formatted(SOLD_OUT_CASE)).setParameter("locgov", locgov).getResultList();
        List<Long> ids = rows.stream().map(r -> ((Number) r[0]).longValue()).toList();
        Map<Long, String> thumbs = ids.isEmpty() ? Map.of() : giftService.thumbnailsOf(ids);
        List<OffRprsRow> result = new ArrayList<>();
        for (Object[] r : rows) {
            long itemId = ((Number) r[0]).longValue();
            String option = s(r[9]);
            String itemOptionId = null;
            List<String> optionStock = null;
            String changed = "N";
            if (option != null && !option.isEmpty()) {
                String[] sp = option.split("\\|\\|");
                Map<String, Object> cond = new LinkedHashMap<>();
                if (sp.length == 4) {
                    cond.put("gdsId", itemId);
                    cond.put("optionType", sp[0]);
                    cond.put("optionName2", sp[2]);
                    cond.put("optionPrice", sp[3]);
                }
                if (sp.length == 8) {
                    cond.put("gdsId", itemId);
                    cond.put("optionType", sp[0]);
                    cond.put("optionName1", sp[2]);
                    cond.put("optionName2", sp[4]);
                    cond.put("optionName3", sp[6]);
                    cond.put("optionPrice", sp[7]);
                }
                Map<String, Object> found = cond.isEmpty() ? null : offRprsRepository.selectOffRprsOption(cond);
                if (found != null) {
                    itemOptionId = String.valueOf(found.get("item_option_id"));
                    optionStock = List.of(String.valueOf(found.get("option_stock_quantity")));
                } else {
                    changed = "Y";
                }
            }
            result.add(new OffRprsRow(itemId, userCode(s(r[1]), itemId), s(r[2]), s(r[3]), s(r[4]), s(r[5]), i(r[6]), s(r[7]), i(r[8]), option,
                    s(r[10]), itemOptionId, optionStock, changed, thumbs.get(itemId)));
        }
        return result;
    }

    /** 오프라인 대표 답례품 저장 항목 - option은 S||제목||옵션명||가격 / S3||제목1||옵션1||제목2||옵션2||제목3||옵션3||가격 / 빈 값(옵션 없음). */
    public record OffRprsInput(Long itemId, String option) {
    }

    /** AS-IS insertLclgvOffRprsGds - 지자체의 오프라인 대표 답례품을 통째로 지우고 소속 답례품인 것만 옵션과 함께 다시 넣는다. */
    public void saveOffRprs(String locgov, List<OffRprsInput> inputs, long userId) {
        em.createNativeQuery("delete from public.g_lclgv_off_rprs_gds_mng where lclgv_cd = :locgov").setParameter("locgov", locgov).executeUpdate();
        if (inputs == null || inputs.isEmpty()) {
            return;
        }
        List<Long> requested = inputs.stream().map(OffRprsInput::itemId).toList();
        Set<Long> valid = new LinkedHashSet<>(locgovItemIds(requested, locgov));
        int seq = 0;
        for (Long id : valid) {
            for (OffRprsInput in : inputs) {
                if (id.equals(in.itemId())) {
                    em.createNativeQuery("insert into public.g_lclgv_off_rprs_gds_mng (lclgv_cd, gds_id, sort_seq, frst_rgtr_id, frst_reg_dt, options) "
                                    + "values (:locgov, :id, :seq, :u, now(), :opt)")
                            .setParameter("locgov", locgov).setParameter("id", id).setParameter("seq", ++seq).setParameter("u", userId)
                            .setParameter("opt", in.option() == null ? "" : in.option()).executeUpdate();
                }
            }
        }
    }

    // ---- 오프라인 대표 답례품 화면이 부르는 옵션 조회 3종 ----

    /**
     * AS-IS getOptionList - 답례품 옵션 목록. 옵션이 없는(N) 답례품은 null. 화면 JS가 itemOptionType(S/S3)·itemOptionTitle1~3·
     * itemOptionGroups[0].itemOptions[].optionName1~3/optionPrice를 읽는다.
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> optionList(long itemId) {
        List<Object[]> head = em.createNativeQuery("select i.item_id, i.item_user_code, i.item_option_type, i.item_option_flag, "
                + "i.item_option_title1, i.item_option_title2, i.item_option_title3 from public.op_item i where i.item_id = :id")
                .setParameter("id", itemId).getResultList();
        if (head.isEmpty() || "N".equals(s(head.get(0)[3])) || s(head.get(0)[3]) == null) {
            return null;
        }
        Object[] h = head.get(0);
        List<Object[]> opts = em.createNativeQuery("select option_name1, option_name2, option_name3, option_price "
                + "from public.op_item_option where item_id = :id order by item_option_id").setParameter("id", itemId).getResultList();
        List<Map<String, Object>> options = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (Object[] o : opts) {
            String key = s(o[0]) + "\u0001" + s(o[1]) + "\u0001" + s(o[2]) + "\u0001" + s(o[3]);
            if (!seen.add(key)) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("optionName1", s(o[0]));
            m.put("optionName2", s(o[1]));
            m.put("optionName3", s(o[2]));
            m.put("optionPrice", o[3] == null ? 0 : ((Number) o[3]).intValue());
            options.add(m);
        }
        Map<String, Object> group = new LinkedHashMap<>();
        group.put("itemOptions", options);
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("itemId", ((Number) h[0]).longValue());
        item.put("itemUserCode", userCode(s(h[1]), ((Number) h[0]).longValue()));
        item.put("itemOptionType", s(h[2]));
        item.put("itemOptionFlag", s(h[3]));
        item.put("itemOptionTitle1", s(h[4]));
        item.put("itemOptionTitle2", s(h[5]));
        item.put("itemOptionTitle3", s(h[6]));
        item.put("itemOptionGroups", List.of(group));
        return List.of(item);
    }

    /**
     * AS-IS getComboOptionList - S(선택형)는 옵션명2로 옵션명+가격, S3(조합형)는 지금까지 고른 옵션에 따라 옵션명1 → 옵션명2 → (옵션명3 + 가격)
     * 후보. 키는 AS-IS 매퍼 그대로 대문자 OPTION_NAME/PRICE(가격은 문자열, 후보 단계는 '').
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, String>> comboOptionList(Map<String, Object> optionMap) {
        long itemId = Long.parseLong(String.valueOf(optionMap.get("itemId")));
        String type = optionMap.get("optionType") == null ? "" : String.valueOf(optionMap.get("optionType"));
        String n1 = str(optionMap.get("optionName1"));
        String n2 = str(optionMap.get("optionName2"));
        String n3 = str(optionMap.get("optionName3"));
        StringBuilder sql = new StringBuilder("select ");
        if ("S".equals(type)) {
            sql.append("option_name2, cast(option_price as varchar)");
        } else if (n1 == null) {
            sql.append("distinct option_name1, ''");
        } else if (n2 == null) {
            sql.append("distinct option_name2, ''");
        } else {
            sql.append("distinct option_name3, cast(option_price as varchar)");
        }
        sql.append(" from public.op_item_option where item_id = :id");
        if ("S".equals(type)) {
            sql.append(" and option_name2 = :n2");
        } else {
            if (n1 != null) {
                sql.append(" and option_name1 = :n1");
            }
            if (n2 != null) {
                sql.append(" and option_name2 = :n2");
            }
            if (n3 != null) {
                sql.append(" and option_name3 = :n3");
            }
            sql.append(" order by 1");
        }
        Query q = em.createNativeQuery(sql.toString()).setParameter("id", itemId);
        if ("S".equals(type)) {
            q.setParameter("n2", n2);
        } else {
            if (n1 != null) {
                q.setParameter("n1", n1);
            }
            if (n2 != null) {
                q.setParameter("n2", n2);
            }
            if (n3 != null) {
                q.setParameter("n3", n3);
            }
        }
        List<Map<String, String>> result = new ArrayList<>();
        for (Object o : q.getResultList()) {
            Object[] r = (Object[]) o;
            Map<String, String> m = new LinkedHashMap<>();
            m.put("OPTION_NAME", s(r[0]));
            m.put("PRICE", s(r[1]));
            result.add(m);
        }
        return result;
    }

    /** AS-IS getSelectOption - 오프라인 대표 답례품으로 저장해 둔 옵션 문자열. */
    @Transactional(readOnly = true)
    public String selectedOption(long itemId) {
        List<?> rows = em.createNativeQuery("select options from public.g_lclgv_off_rprs_gds_mng where gds_id = :id")
                .setParameter("id", itemId).getResultList();
        return rows.isEmpty() ? null : s(rows.get(0));
    }

    // ================= 카테고리 select 옵션 (AS-IS /categories/options*) =================

    public record CategoryOption(String categoryCode, String categoryLevel, Integer categoryId, String categoryName) {
    }

    /** AS-IS getCategoriesListById - 그룹의 1차 카테고리(사용), 순서(ORDERING)순. */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<CategoryOption> categoriesByGroup(int groupId) {
        List<Object[]> rows = em.createNativeQuery("select category_code, category_level, category_id, category_name from public.op_category "
                + "where category_flag = 'Y' and category_level = '1' and category_group_id = :g and category_group_id > 0 order by ordering asc")
                .setParameter("g", groupId).getResultList();
        return categoryOptions(rows);
    }

    /** AS-IS getChildCategoriesFromParentCategoryClass - 부모 코드 바로 아래 차수의 사용 카테고리. */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<CategoryOption> categoryChildren(String parentClass) {
        List<Object[]> rows = em.createNativeQuery("select category_code, category_level, category_id, category_name from public.op_category "
                + "where category_level = cast(length(cast(:p as varchar)) / 3 + 1 as varchar) and category_code like concat(cast(:p as varchar), '%') "
                + "and category_flag = 'Y' order by ordering asc").setParameter("p", parentClass).getResultList();
        return categoryOptions(rows);
    }

    private static List<CategoryOption> categoryOptions(List<Object[]> rows) {
        List<CategoryOption> result = new ArrayList<>();
        for (Object[] r : rows) {
            result.add(new CategoryOption(s(r[0]), s(r[1]), i(r[2]), s(r[3])));
        }
        return result;
    }

    // ================= 공통 =================

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    private static String nz(String v) {
        return v == null ? "" : v.trim();
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static String s(Object o) {
        return o == null ? null : o.toString();
    }

    private static Integer i(Object o) {
        return o == null ? null : ((Number) o).intValue();
    }

    private static Long l(Object o) {
        return o == null ? null : ((Number) o).longValue();
    }

    private static int nzInt(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }
}
