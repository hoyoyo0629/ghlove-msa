package com.ghlove.order.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 배송비 정책 전체(G1~G7) 계산 - AS-IS {@code saleson.shop.order.domain.Shipping#getShippingGroups()}
 * (Shipping.java:195~419)를 그대로 옮긴 것이다. 답례품 주문은 지자체(locgov) 단위로 쪼개져
 * 포인트가 차감되므로, AS-IS가 주문 전체에 대해 하던 묶음배송 그룹핑을 여기서는 지자체 그룹
 * 안에서 수행한다(실데이터상 shippingGroupCode가 전부 비어 있어 결과는 동일하다).
 *
 * <p>계산 결과는 "그룹당 1건"인 AS-IS realShipping을 그대로 유지하되, 화면/합계가 라인 단위인
 * MSA 구조에 맞춰 <b>그룹 배송비를 그 그룹의 첫 라인 하나에 몰아주고 나머지 라인은 0</b>으로
 * 배분한다 - 라인별 합·그룹 합 모두 AS-IS 총액과 정확히 같아진다.
 *
 * <p>착불(shippingPaymentType=2 → payShipping 0) 분기도 AS-IS대로 두었으나, 답례품은 포인트
 * 선결제라 착불 데이터 원천이 없어 항상 선불로 계산된다(코드 parity, 데이터로 비활성).
 */
public final class DeliveryFeeCalculator {

    private DeliveryFeeCalculator() {
    }

    /** 배송비 계산 입력 한 줄. {@code baseAmountForShipping}는 조건부무료 기준 판정에 쓰는 금액(=lineTotal),
     *  {@code sequence}는 개별배송 그룹코드 유일화를 위한 값(AS-IS itemSequence 대응). */
    public record Line(Long key, String shippingType, int shipping, Integer shippingFreeAmount,
                        Integer shippingItemCount, Integer shippingExtraCharge1, Integer shippingExtraCharge2,
                        String shippingGroupCode, String shipmentGroupCode, String shippingPaymentType,
                        int quantity, long baseAmountForShipping, long sequence) {
    }

    private static final class Group {
        String shippingType;
        String shippingGroupCode;
        String shipmentGroupCode;
        int shipping;
        int shippingItemCount;
        int shippingExtraCharge1;
        int shippingExtraCharge2;
        int shippingFreeAmount;
        String shippingPaymentType;
        final List<Line> lines = new ArrayList<>();
    }

    /** null과 ""를 동일하게 본다 - AS-IS CommonUtils.dataNvl 대응(무료배송 저장시 group code가 ''/null로 갈리는 문제 회피). */
    private static String dataNvl(String v) {
        return v == null ? "" : v;
    }

    /**
     * @param lines     같은 지자체 그룹의 배송 계산 입력들
     * @param islandType "JEJU" / "ISLAND" / "" (수취인 우편번호로 판정)
     * @return key(장바구니행) → 배분된 배송비. 합계는 AS-IS realShipping 총액과 같다.
     */
    public static Map<Long, Long> compute(List<Line> lines, String islandType) {
        Map<Long, Long> result = new LinkedHashMap<>();
        for (Line line : lines) {
            result.put(line.key(), 0L);
        }

        // 1) 묶음배송 그룹핑 (Shipping.java:202~288)
        List<Group> groups = new ArrayList<>();
        for (Line line : lines) {
            // 1: 무료배송, 6: 고정배송비가 아닌경우에는 묶음배송 상품임
            boolean isSingleShipping = "1".equals(line.shippingType()) || "6".equals(line.shippingType());

            boolean isNew = true;
            if (!isSingleShipping && !groups.isEmpty()) {
                for (Group g : groups) {
                    if (dataNvl(g.shippingGroupCode).equals(dataNvl(line.shippingGroupCode()))) {
                        isNew = false;
                        break;
                    }
                }
            }

            if (isNew) {
                Group g = new Group();
                g.shippingType = line.shippingType();
                g.shippingGroupCode = line.shippingGroupCode();
                g.shipping = line.shipping();
                g.shippingExtraCharge1 = nz(line.shippingExtraCharge1());
                g.shippingExtraCharge2 = nz(line.shippingExtraCharge2());
                g.shippingFreeAmount = nz(line.shippingFreeAmount());
                g.shippingPaymentType = line.shippingPaymentType();
                g.shipmentGroupCode = line.shipmentGroupCode();
                // 0으로 나누는 상황 방지 (Shipping.java:258)
                int itemCount = nz(line.shippingItemCount());
                g.shippingItemCount = itemCount == 0 ? 1 : itemCount;

                if (isSingleShipping) {
                    // 개별배송은 그룹코드를 임의로 유일화 (Shipping.java:267)
                    g.shippingGroupCode = "single-" + line.sequence();
                }
                g.lines.add(line);
                groups.add(g);
            } else {
                for (Group g : groups) {
                    if (dataNvl(g.shippingGroupCode).equals(dataNvl(line.shippingGroupCode()))) {
                        g.lines.add(line);
                        break;
                    }
                }
            }
        }

        // 2) 그룹별 배송비 산정 (Shipping.java:290~417)
        for (Group g : groups) {
            int addDeliveryCharge = 0;
            if ("JEJU".equals(islandType)) {
                addDeliveryCharge = g.shippingExtraCharge1;
            } else if ("ISLAND".equals(islandType)) {
                addDeliveryCharge = g.shippingExtraCharge2;
            }

            long realShipping = 0;
            if ("1".equals(g.shippingType)) { // 무료 배송
                realShipping = addDeliveryCharge;
            } else if ("2".equals(g.shippingType) || "3".equals(g.shippingType)) { // 판매자/출고지 조건부
                long totalItemAmount = 0;
                if ("3".equals(g.shippingType) && g.shipmentGroupCode != null && !g.shipmentGroupCode.isEmpty()) {
                    // 통합 물류 배송 - 조건부 금액을 물류 그룹 전체 상품으로 합산 (Shipping.java:314~328)
                    for (Line line : lines) {
                        if (line.shipmentGroupCode() == null || line.shipmentGroupCode().isEmpty()) {
                            continue;
                        }
                        if (g.shipmentGroupCode.equals(line.shipmentGroupCode())) {
                            totalItemAmount += line.baseAmountForShipping();
                        }
                    }
                } else {
                    for (Line line : g.lines) {
                        totalItemAmount += line.baseAmountForShipping();
                    }
                }
                realShipping = (g.shippingFreeAmount <= totalItemAmount)
                        ? addDeliveryCharge : g.shipping + addDeliveryCharge;
            } else if ("4".equals(g.shippingType)) { // 상품 조건부
                long totalItemAmount = 0;
                for (Line line : g.lines) {
                    totalItemAmount += line.baseAmountForShipping();
                }
                realShipping = (g.shippingFreeAmount <= totalItemAmount)
                        ? addDeliveryCharge : g.shipping + addDeliveryCharge;
            } else if ("5".equals(g.shippingType)) { // 개당배송비 - BOX 당 배송비
                int totalItemQuantity = 0;
                for (Line line : g.lines) {
                    totalItemQuantity += line.quantity();
                }
                int boxCount = (int) Math.ceil((float) totalItemQuantity / g.shippingItemCount);
                realShipping = (long) (g.shipping + addDeliveryCharge) * boxCount;
            } else { // 고정 배송비
                realShipping = g.shipping + addDeliveryCharge;
            }

            // 착불이면 사용자 배송비를 0으로 (Shipping.java:411)
            long payShipping = "2".equals(g.shippingPaymentType) ? 0 : realShipping;

            // 그룹 배송비를 첫 라인에 몰아준다(나머지는 0 유지)
            if (!g.lines.isEmpty()) {
                result.put(g.lines.get(0).key(), payShipping);
            }
        }
        return result;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }
}
