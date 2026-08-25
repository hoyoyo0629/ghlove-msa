package com.ghlove.donation.web;

import java.math.BigDecimal;

/** Read-only JSON shape for other services (member's 메인 화면) to show 지정기부사업 진행 현황. */
public record DesignatedProjectDto(Long dsgnDntnBizId, String title, String content, String locgovCode,
                                    String locgovName, Long goalAmt, BigDecimal raisedAmt, BigDecimal percent,
                                    String beginYmd, String endYmd, String imageUrl) {
}
