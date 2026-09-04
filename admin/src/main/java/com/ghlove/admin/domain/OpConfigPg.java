package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** PG사(결제대행사) 연동 설정 (AS-IS opmanager/config - ConfigManagerController `pg` 서브화면).
 *  단일 행(ID=1)만 사용한다. Maps OP_CONFIG_PG. */
@Entity
@Table(name = "OP_CONFIG_PG")
@Getter
@Setter
@NoArgsConstructor
public class OpConfigPg {

    public static final long ID = 1L;

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PG_TYPE")
    private String pgType;

    @Column(name = "MID")
    private String mid;

    @Column(name = "KEY")
    private String key;

    @Column(name = "SIGN")
    private String sign;

    @Column(name = "MOBILE_MID")
    private String mobileMid;

    @Column(name = "MOBILE_KEY")
    private String mobileKey;

    @Column(name = "ESCROOW_MID")
    private String escroowMid;

    @Column(name = "ESCROOW_KEY")
    private String escroowKey;

    @Column(name = "MOBILE_ESCROOW_MID")
    private String mobileEscroowMid;

    @Column(name = "MOBILE_ESCROOW_KEY")
    private String mobileEscroowKey;

    @Column(name = "CARD_CODE")
    private String cardCode;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "CANCEL_PASSWORD")
    private String cancelPassword;

    @Column(name = "INSTALMENT")
    private String instalment;

    @Column(name = "USE_ESCROOW")
    private String useEscroow;

    @Column(name = "USE_AUTO_CASH_RECEIPT")
    private String useAutoCashReceipt;

    @Column(name = "USE_VBACK_REFUND_SERVICE")
    private String useVbackRefundService;

    @Column(name = "REALTIME_PARTCANCEL_FLAG")
    private String realtimePartcancelFlag;

    @Column(name = "CASHBILL_SERVICE_TYPE")
    private String cashbillServiceType;

    @Column(name = "USE_NPAY_ORDER")
    private String useNpayOrder;

    @Column(name = "USE_NPAY_PAYMENT")
    private String useNpayPayment;

    @Column(name = "NPAY_MID")
    private String npayMid;

    @Column(name = "NPAY_KEY")
    private String npayKey;

    @Column(name = "NPAY_LOG_KEY")
    private String npayLogKey;

    @Column(name = "NPAY_CLIENT_ID")
    private String npayClientId;

    @Column(name = "NPAY_CLIENT_SECRET")
    private String npayClientSecret;

    @Column(name = "NPAY_PARTNER_ID")
    private String npayPartnerId;

    @Column(name = "NPAY_BUTTON_KEY")
    private String npayButtonKey;

    /** 기본값이 채워진 신규(미저장) 설정을 만든다 - NOT NULL 컬럼은 빈값 대신 "N"/공백으로 채운다. */
    public static OpConfigPg defaults() {
        OpConfigPg c = new OpConfigPg();
        c.setId(ID);
        c.setPgType("");
        c.setMid("");
        c.setRealtimePartcancelFlag("N");
        c.setUseAutoCashReceipt("N");
        c.setUseEscroow("N");
        c.setUseNpayOrder("N");
        c.setUseNpayPayment("N");
        c.setUseVbackRefundService("N");
        c.setCashbillServiceType("N");
        return c;
    }
}
