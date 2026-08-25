package com.ghlove.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/** 답례품몰 GNB "전체 카테고리" 메가메뉴용 - gift 서비스의 카테고리 트리를 읽기 전용으로 조회한다. */
@Component
public class CategoryClient {

    private final RestClient restClient;

    public CategoryClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public List<CategoryGroupInfo> categoryGroups() {
        try {
            List<CategoryGroupInfo> groups = restClient.get()
                    .uri("/api/categories")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<CategoryGroupInfo>>() {
                    });
            return groups != null ? groups : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }
}
