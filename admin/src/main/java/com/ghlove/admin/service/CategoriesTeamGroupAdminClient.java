package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** 카테고리 그룹 관리(AS-IS opmanager/categories-team-group) - gift 서비스의 OP_CATEGORY_TEAM/GROUP cross-service API 래퍼. */
@Component
public class CategoriesTeamGroupAdminClient {

    private static final String BASE = "/api/admin/categories-team-group";

    private final RestClient restClient;

    public CategoriesTeamGroupAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(giftServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    /** AS-IS getCategoriesTeamGroupList - 대그룹 + 하위 그룹. */
    public List<TeamRow> teamGroupList() {
        return orEmpty(restClient.get().uri(BASE + "/team-groups").retrieve().body(new ParameterizedTypeReference<List<TeamRow>>() {
        }));
    }

    /** AS-IS getCategoriesTeamList. */
    public List<TeamRow> teamList() {
        return orEmpty(restClient.get().uri(BASE + "/teams").retrieve().body(new ParameterizedTypeReference<List<TeamRow>>() {
        }));
    }

    /** AS-IS getCategoriesGroupList. */
    public List<GroupRow> groupList() {
        return orEmpty(restClient.get().uri(BASE + "/groups").retrieve().body(new ParameterizedTypeReference<List<GroupRow>>() {
        }));
    }

    /** 없으면 null. */
    public GroupRow group(int id) {
        try {
            return restClient.get().uri(BASE + "/groups/{id}", id).retrieve().body(GroupRow.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return null;
            }
            throw e;
        }
    }

    public List<CategoryRef> categoriesOf(int id) {
        return orEmpty(restClient.get().uri(BASE + "/groups/{id}/categories", id).retrieve()
                .body(new ParameterizedTypeReference<List<CategoryRef>>() {
                }));
    }

    public List<CategoriesInGroup> categoriesInGroups() {
        return orEmpty(restClient.get().uri(BASE + "/categories-in-groups").retrieve()
                .body(new ParameterizedTypeReference<List<CategoriesInGroup>>() {
                }));
    }

    public int createGroup(GroupForm form) {
        Map<String, Integer> body = restClient.post().uri(BASE + "/groups").body(form).retrieve()
                .body(new ParameterizedTypeReference<Map<String, Integer>>() {
                });
        return body != null && body.get("categoryGroupId") != null ? body.get("categoryGroupId") : 0;
    }

    public void updateGroup(GroupForm form) {
        restClient.put().uri(BASE + "/groups").body(form).retrieve().toBodilessEntity();
    }

    public void deleteGroup(int id) {
        restClient.post().uri(BASE + "/groups/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void groupOrdering(List<Integer> ids, List<Integer> orderings) {
        restClient.post().uri(BASE + "/ordering/groups").body(new OrderingForm(ids, orderings)).retrieve().toBodilessEntity();
    }

    public void teamOrdering(List<Integer> ids, List<Integer> orderings) {
        restClient.post().uri(BASE + "/ordering/teams").body(new OrderingForm(ids, orderings)).retrieve().toBodilessEntity();
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return list != null ? list : List.of();
    }

    public record GroupRow(Integer categoryGroupId, Integer categoryTeamId, String name, String code, String flag,
                           String accessType, Integer ordering) {
    }

    public record TeamRow(Integer categoryTeamId, String name, String code, String flag, Integer ordering,
                          List<GroupRow> groups) {
    }

    public record CategoryRef(String categoryCode, String categoryName) {
    }

    public record CategoriesInGroup(Integer categoryGroupId, String name, List<CategoryRef> categoryList) {
    }

    public record GroupForm(Integer categoryGroupId, Integer categoryTeamId, String name, String code,
                            String categoryGroupFlag, String accessType, List<String> categoryCodes) {
    }

    public record OrderingForm(List<Integer> ids, List<Integer> orderings) {
    }
}
