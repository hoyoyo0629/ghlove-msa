package com.ghlove.admin.service;

import com.ghlove.admin.domain.OpenApiClient;
import com.ghlove.admin.repository.OpenApiClientRepository;
import com.ghlove.admin.service.integration.KongGatewayClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * SFR-010 "민간개방 API 기능(민간 이용업체 데이터 통합관리)" - 외부 이용업체에게 API 키를
 * 발급/회수하고, Kong 게이트웨이(key-auth 컨슈머)와 동기화한다. Kong은 KONG_DATABASE=off라
 * 컨슈머가 메모리에만 있고 개별 CRUD도 막혀있어({@link KongGatewayClient} 참고), 매번
 * ACTIVE 클라이언트 전체 목록을 다시 밀어넣는 전체 재동기화 방식을 쓴다 - admin 기동
 * 시점(@PostConstruct)에도 한 번 실행해 Kong 컨테이너 재기동에도 견디게 한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpenApiClientService {

    private final OpenApiClientRepository openApiClientRepository;
    private final KongGatewayClient kongGatewayClient;

    @PostConstruct
    public void syncAllToKong() {
        try {
            kongGatewayClient.syncConsumers(openApiClientRepository.findAllByStatusCode(OpenApiClient.STATUS_ACTIVE));
        } catch (RuntimeException e) {
            log.warn("[open-api] 기동 시 Kong 재동기화 실패(Kong이 아직 안 떠 있을 수 있음): {}", e.getMessage());
        }
    }

    public List<OpenApiClient> list() {
        return openApiClientRepository.findAllByOrderByClientIdDesc();
    }

    @Transactional
    public OpenApiClient issue(String clientName) {
        OpenApiClient client = new OpenApiClient();
        client.setClientName(clientName);
        // clientId는 INSERT 이후에나 생기므로(KONG_USERNAME NOT NULL이라 그걸 기다렸다가
        // 채우는 2단계 저장은 첫 INSERT 자체가 실패한다), UUID 기반으로 저장 전에 미리 정한다.
        client.setKongUsername("openapi-client-" + UUID.randomUUID());
        client.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        client.setStatusCode(OpenApiClient.STATUS_ACTIVE);
        client.setCreatedDate(LocalDateTime.now());
        client = openApiClientRepository.save(client);

        syncActiveToKong();
        return client;
    }

    @Transactional
    public void suspend(Long clientId) {
        OpenApiClient client = openApiClientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("클라이언트를 찾을 수 없습니다."));
        client.setStatusCode(OpenApiClient.STATUS_SUSPENDED);
        openApiClientRepository.save(client);
        syncActiveToKong();
    }

    @Transactional
    public void reactivate(Long clientId) {
        OpenApiClient client = openApiClientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("클라이언트를 찾을 수 없습니다."));
        client.setStatusCode(OpenApiClient.STATUS_ACTIVE);
        openApiClientRepository.save(client);
        syncActiveToKong();
    }

    private void syncActiveToKong() {
        kongGatewayClient.syncConsumers(openApiClientRepository.findAllByStatusCode(OpenApiClient.STATUS_ACTIVE));
    }
}
