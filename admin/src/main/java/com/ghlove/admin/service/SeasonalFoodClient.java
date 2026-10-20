package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

/** 제철식품관 관리(AS-IS opmanager/seasonal-food) - gift 서비스의 월별 제철 키워드(G_SEASON_FOOD) cross-service API 래퍼. */
@Component
public class SeasonalFoodClient {

    private static final String BASE = "/api/admin/seasonal-food";

    private final RestClient restClient;

    public SeasonalFoodClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
                              @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** 한 달의 키워드(쉼표로 이은 글자) - AS-IS SeasonalFood의 월·키워드 두 칸. */
    public record MonthKeywords(int seasonalFoodMonth, String seasonalFoodKeyword) {
    }

    private record SaveRequest(List<MonthKeywords> seasonalFoods, long userId) {
    }

    /** 1~12월 전부(데이터 없는 달은 빈 키워드). */
    public List<MonthKeywords> list() {
        List<MonthKeywords> rows = restClient.get().uri(BASE).retrieve()
                .body(new ParameterizedTypeReference<List<MonthKeywords>>() {
                });
        return rows != null ? rows : List.of();
    }

    /** 달마다 지우고 다시 넣은 뒤 목록을 돌려준다. */
    public List<MonthKeywords> save(List<MonthKeywords> months, long userId) {
        List<MonthKeywords> rows = restClient.post().uri(BASE + "/save").body(new SaveRequest(months, userId)).retrieve()
                .body(new ParameterizedTypeReference<List<MonthKeywords>>() {
                });
        return rows != null ? rows : List.of();
    }
}
