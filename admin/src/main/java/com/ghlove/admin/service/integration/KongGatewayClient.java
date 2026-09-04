package com.ghlove.admin.service.integration;

import com.ghlove.admin.domain.OpenApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SFR-010 "민간개방 API 기능(민간 이용업체 데이터 통합관리)" - Kong Admin API로 컨슈머/API키를
 * 관리한다. Kong 3.x는 KONG_DATABASE=off(DB-less)일 때 컨슈머 등 개별 엔티티에 대한
 * POST/PUT/DELETE를 전부 거부한다("operation unsupported") - 실제로 curl로 확인한 동작이라
 * 처음엔 개별 CRUD로 시도했다가 이 제약을 발견하고 다시 설계했다. DB-less에서 설정을 바꾸는
 * 유일한 방법은 {@code POST /config}로 선언적 설정 전체를 통째로 교체하는 것뿐이라, 매번
 * 정적 라우팅(kong-local.yaml)을 다시 읽어 그 위에 "현재 ACTIVE한 오픈API 클라이언트"
 * 목록만 덧씌운 전체 설정을 다시 만들어 보낸다(부분 갱신이 아니라 항상 전체 스냅샷 재적용).
 */
@Component
@Slf4j
public class KongGatewayClient {

    private static final String DYNAMIC_USERNAME_PREFIX = "openapi-client-";

    private final RestClient restClient;
    private final Path declarativeConfigPath;

    public KongGatewayClient(@Value("${ghlove.kong.admin-url}") String adminUrl,
                              @Value("${ghlove.kong.declarative-config-path}") String declarativeConfigPath) {
        this.restClient = RestClient.create(adminUrl);
        this.declarativeConfigPath = Path.of(declarativeConfigPath);
    }

    /** ghlove-app(내부 JWT용) 등 정적 컨슈머는 그대로 두고, openapi-client-* 동적 컨슈머만
     *  전달받은 ACTIVE 목록으로 통째로 다시 채워서 Kong에 전체 설정을 재적용한다. */
    @SuppressWarnings("unchecked")
    public void syncConsumers(List<OpenApiClient> activeClients) {
        Map<String, Object> config;
        try (FileInputStream in = new FileInputStream(declarativeConfigPath.toFile())) {
            config = new Yaml().load(in);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Kong 설정 파일을 찾을 수 없습니다: " + declarativeConfigPath, e);
        } catch (Exception e) {
            throw new IllegalStateException("Kong 설정 파일을 읽는 데 실패했습니다: " + e.getMessage(), e);
        }

        List<Object> consumers = (List<Object>) config.computeIfAbsent("consumers", k -> new ArrayList<>());
        consumers.removeIf(c -> {
            Object username = ((Map<String, Object>) c).get("username");
            return username instanceof String s && s.startsWith(DYNAMIC_USERNAME_PREFIX);
        });
        for (OpenApiClient client : activeClients) {
            Map<String, Object> consumer = new LinkedHashMap<>();
            consumer.put("username", client.getKongUsername());
            consumer.put("keyauth_credentials", List.of(Map.of("key", client.getApiKey())));
            consumers.add(consumer);
        }

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String yaml = new Yaml(options).dump(config);
        try {
            restClient.post().uri("/config")
                    .contentType(MediaType.valueOf("text/yaml"))
                    .body(yaml)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            log.error("[kong-admin-api] 설정 재적용 실패", e);
            throw new IllegalStateException("API 게이트웨이(Kong) 설정 적용에 실패했습니다 - Kong 상태를 확인해 주세요: " + e.getMessage(), e);
        }
    }
}
