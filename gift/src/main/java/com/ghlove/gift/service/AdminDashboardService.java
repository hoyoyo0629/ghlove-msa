package com.ghlove.gift.service;

import com.ghlove.gift.domain.Seller;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 관리자 메인화면(AS-IS {@code /opmanager} main/index.jsp)의 "등록된 답례품 건 수"·답례품 미등록 지자체 -
 * AS-IS {@code main-mapper.xml}의 {@code opmanageMainAmountGiftA}(품절 아님) + {@code opmanageMainAmountGiftB}(품절)
 * 합계와 {@code opamanagerMainGift} 포팅.
 *
 * <p>판매자 사용 {@code STATUS_CODE '2'}는 AS-IS 값 그대로({@link Seller#STATUS_ACTIVE}). 코드 치환: 답례품 승인 AS-IS {@code DATA_STATUS_CODE '1'} → {@code 'APPROVED'}. AS-IS는 판매자의 지자체코드가 있어야 세는데
 * TO-BE 판매자에는 그 값이 없고 답례품이 직접 지자체코드를 가져서 답례품의 것을 본다(답례품 선호도와 같은 방식).
 * 등록일 조건은 AS-IS 그대로 문자열 비교 {@code created_date(yyyyMMddHHmmss) <= 기부일자(yyyyMMdd)} - 그래서 기부일자
 * 당일 등록분은 들어가지 않는다(AS-IS 동작).
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private static final String ITEM_APPROVED = "APPROVED";

    /** 품절 판정 - AS-IS GiftA의 CASE 식(품절이면 'Y'). */
    private static final String SOLD_OUT_CASE = """
            (case when i.sold_out = '1' then 'Y'
                  when i.item_option_flag = 'N' and i.stock_flag = 'Y' and i.stock_quantity = 0 then 'Y'
                  when i.item_option_flag = 'Y' and i.item_option_type = 'T' and i.stock_flag = 'Y' and i.stock_quantity = 0 then 'Y'
                  when i.item_option_flag = 'Y' and iost.item_option_sold_out_flag = 'Y' then 'Y'
                  else 'N' end)""";

    /** 품절 판정 - AS-IS GiftB의 OR 식. */
    private static final String SOLD_OUT_OR = """
            (i.sold_out = '1'
             or (i.item_option_flag = 'N' and i.stock_flag = 'Y' and i.stock_quantity = 0)
             or (i.item_option_flag = 'Y' and i.item_option_type = 'T' and i.stock_flag = 'Y' and i.stock_quantity = 0)
             or (i.item_option_flag = 'Y' and iost.item_option_sold_out_flag = 'Y'))""";

    private final EntityManager entityManager;

    /** 기부일자(yyyyMMdd) 기준 등록된 답례품 수 = 판매중(GiftA) + 품절(GiftB). */
    public long registeredItemCount(String date) {
        return count(SOLD_OUT_CASE + " = 'N'", date) + count(SOLD_OUT_OR, date);
    }

    /** 답례품을 하나라도 가진 지자체코드(AS-IS {@code distinct NVL(S.locgov_code, 0)}). */
    @SuppressWarnings("unchecked")
    public List<String> itemLocgovCodes() {
        return entityManager.createNativeQuery("select distinct coalesce(i.locgov_code, '0') from op_item i "
                + "inner join op_seller s on i.seller_id = s.seller_id").getResultList();
    }

    private long count(String soldOutCondition, String date) {
        Object o = entityManager.createNativeQuery("select count(*) from (select i.item_id from op_item i "
                        + "inner join op_seller s on i.seller_id = s.seller_id and s.status_code = :sellerActive "
                        + "left join op_item_option_soldout iost on i.item_id = iost.item_id "
                        + "where i.data_status_code = :itemApproved and i.item_data_type = '1' and " + soldOutCondition
                        + " and i.locgov_code is not null and i.created_date <= :date group by i.item_id) t")
                .setParameter("sellerActive", Seller.STATUS_ACTIVE).setParameter("itemApproved", ITEM_APPROVED)
                .setParameter("date", date).getSingleResult();
        return o == null ? 0L : ((Number) o).longValue();
    }
}
