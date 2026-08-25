package com.ghlove.order.service;

import java.util.List;

/**
 * 장바구니 화면의 지자체별 그룹 (AS-IS displayBuyItems 한 항목). givePoint = 그 지자체
 * 기부로 적립된, 아직 소진되지 않은 포인트(point 서비스 balanceByLocgov). selectedSumPrice는
 * AS-IS처럼 "체크된 항목만" 클라이언트에서 실시간 재계산하므로 여기서는 그룹 전체 합계
 * (groupTotal)만 내려주고, 선택 여부에 따른 재계산/주문가능 표시는 화면 JS가 담당한다.
 */
public record CartGroup(String locgovCode, String locgovNm, long givePoint,
                         List<CartLine> lines, long groupTotal) {
}
