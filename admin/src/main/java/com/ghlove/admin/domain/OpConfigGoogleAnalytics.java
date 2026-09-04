package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** GA(구글 애널리틱스) 추적 설정 (AS-IS opmanager/config - ConfigManagerController
 *  `google-analytics` 서브화면, #55 GoogleAnalyticsManagerController와 중복 엔드포인트).
 *  단일 행(ID=1)만 사용한다. Maps OP_CONFIG_GOOGLE_ANALYTICS. */
@Entity
@Table(name = "OP_CONFIG_GOOGLE_ANALYTICS")
@Getter
@Setter
@NoArgsConstructor
public class OpConfigGoogleAnalytics {

    public static final long ID = 1L;

    @Id
    @Column(name = "ID")
    private Long id;

    /** GA4 측정 ID(G-XXXXXXX). */
    @Column(name = "MEASUREMENT_ID")
    private String measurementId;

    @Column(name = "COMMON_TRACKING_FLAG")
    private String commonTrackingFlag;

    @Column(name = "ECOMMERCE_TRACKING_FLAG")
    private String ecommerceTrackingFlag;

    @Column(name = "STATISTICS_FLAG")
    private String statisticsFlag;

    @Column(name = "CURRENCY")
    private String currency;

    @Column(name = "PROFILE")
    private String profile;

    @Column(name = "AUTH_FILE")
    private String authFile;

    public static OpConfigGoogleAnalytics defaults() {
        OpConfigGoogleAnalytics c = new OpConfigGoogleAnalytics();
        c.setId(ID);
        c.setCommonTrackingFlag("N");
        c.setEcommerceTrackingFlag("N");
        c.setStatisticsFlag("N");
        return c;
    }
}
