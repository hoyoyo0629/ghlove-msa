package com.ghlove.admin.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** SFR-010 "민간개방 API 기능(민간 이용업체 데이터 통합관리)" - 발급한 업체/API키의 admin측
 *  기록. 실제 접근제어의 원천은 Kong(KongGatewayClient가 컨슈머+key-auth로 등록)이고, 이
 *  테이블은 "누구에게 발급했는지" 조회/관리용 + Kong 컨테이너 재기동 시 재동기화 소스다. */
@Entity
@Table(name = "OPEN_API_CLIENT")
@Getter
@Setter
@NoArgsConstructor
public class OpenApiClient {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "openApiClientIdSeq")
    @SequenceGenerator(name = "openApiClientIdSeq", sequenceName = "open_api_client_client_id_seq", allocationSize = 1)
    @Column(name = "CLIENT_ID")
    private Long clientId;

    @Column(name = "CLIENT_NAME")
    private String clientName;

    @Column(name = "KONG_USERNAME")
    private String kongUsername;

    @Column(name = "API_KEY")
    private String apiKey;

    @Column(name = "STATUS_CODE")
    private String statusCode = STATUS_ACTIVE;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;
}
