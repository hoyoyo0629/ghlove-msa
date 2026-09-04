package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** gift 서비스의 open-api.usage 이벤트를 그대로 쌓는 append-only 호출 로그
 *  (STAT_GIFT_LEDGER의 upsert 방식과 달리 STAT_POINT_LEDGER처럼 원본 그대로 보존,
 *  집계는 /open-api/stats 화면이 조회 시점에 계산한다). */
@Entity
@Table(name = "OPEN_API_USAGE_STAT")
@Getter
@Setter
@NoArgsConstructor
public class OpenApiUsageStat {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "openApiUsageStatIdSeq")
    @SequenceGenerator(name = "openApiUsageStatIdSeq", sequenceName = "open_api_usage_stat_usage_id_seq", allocationSize = 1)
    @Column(name = "USAGE_ID")
    private Long usageId;

    @Column(name = "CONSUMER_USERNAME")
    private String consumerUsername;

    @Column(name = "SERVICE_NAME")
    private String serviceName;

    @Column(name = "ENDPOINT")
    private String endpoint;

    /** yyyyMMddHHmmss, 발행 서비스(gift) 로컬 시각. */
    @Column(name = "CALLED_AT")
    private String calledAt;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
