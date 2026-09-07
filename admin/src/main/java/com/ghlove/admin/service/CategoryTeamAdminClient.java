package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/** 카테고리 "팀"(대분류 상위그룹)+"그룹"(팀 하위, 프로모션묶음) 관리 (AS-IS
 *  saleson.shop.categoriesteamgroup - 답례품 상품관리 2단계 #5) - gift 서비스의
 *  OP_CATEGORY_TEAM/OP_CATEGORY_GROUP cross-service API 래퍼. */
@Component
public class CategoryTeamAdminClient {

    private final RestClient restClient;

    public CategoryTeamAdminClient(@Value("${ghlove.gift-service.base-url}") String giftServiceBaseUrl) {
        this.restClient = RestClient.create(giftServiceBaseUrl);
    }

    public List<TeamDto> list() {
        try {
            List<TeamDto> list = restClient.get().uri("/api/admin/category-teams")
                    .retrieve().body(new ParameterizedTypeReference<List<TeamDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public TeamDto team(Integer id) {
        return restClient.get().uri("/api/admin/category-teams/{id}", id).retrieve().body(TeamDto.class);
    }

    public void createTeam(TeamForm form) {
        try {
            restClient.post().uri("/api/admin/category-teams").body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "팀 등록에 실패했습니다."));
        }
    }

    public void updateTeam(Integer id, TeamForm form) {
        restClient.put().uri("/api/admin/category-teams/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteTeam(Integer id) {
        try {
            restClient.post().uri("/api/admin/category-teams/{id}/delete", id).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "팀 삭제에 실패했습니다."));
        }
    }

    public void moveTeam(Integer id, String direction) {
        restClient.post().uri("/api/admin/category-teams/{id}/move?direction={d}", id, direction).retrieve().toBodilessEntity();
    }

    public List<TeamItemDto> teamItems(Integer id) {
        try {
            List<TeamItemDto> list = restClient.get().uri("/api/admin/category-teams/{id}/items", id)
                    .retrieve().body(new ParameterizedTypeReference<List<TeamItemDto>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void addTeamItems(Integer id, List<Long> itemIds) {
        var body = new org.springframework.util.LinkedMultiValueMap<String, String>();
        itemIds.forEach(i -> body.add("itemIds", String.valueOf(i)));
        try {
            restClient.post().uri("/api/admin/category-teams/{id}/items", id)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(body).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "답례품 배정에 실패했습니다."));
        }
    }

    public void removeTeamItem(Integer teamId, Long itemId) {
        restClient.post().uri("/api/admin/category-teams/{id}/items/{itemId}/delete", teamId, itemId).retrieve().toBodilessEntity();
    }

    public GroupDto group(Integer id) {
        return restClient.get().uri("/api/admin/category-teams/groups/{id}", id).retrieve().body(GroupDto.class);
    }

    public void createGroup(GroupForm form) {
        try {
            restClient.post().uri("/api/admin/category-teams/groups").body(form).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "그룹 등록에 실패했습니다."));
        }
    }

    public void updateGroup(Integer id, GroupForm form) {
        restClient.put().uri("/api/admin/category-teams/groups/{id}", id).body(form).retrieve().toBodilessEntity();
    }

    public void deleteGroup(Integer id) {
        restClient.post().uri("/api/admin/category-teams/groups/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void moveGroup(Integer id, String direction) {
        restClient.post().uri("/api/admin/category-teams/groups/{id}/move?direction={d}", id, direction).retrieve().toBodilessEntity();
    }

    public void addBanner(Integer groupId, String title, String linkUrl, MultipartFile image) {
        var multipart = new LinkedMultiValueMap<String, Object>();
        multipart.add("title", title);
        if (linkUrl != null) {
            multipart.add("linkUrl", linkUrl);
        }
        multipart.add("image", image.getResource());
        try {
            restClient.post().uri("/api/admin/category-teams/groups/{id}/banners", groupId)
                    .contentType(MediaType.MULTIPART_FORM_DATA).body(multipart).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "배너 등록에 실패했습니다."));
        }
    }

    public void deleteBanner(Integer id) {
        restClient.post().uri("/api/admin/category-teams/banners/{id}/delete", id).retrieve().toBodilessEntity();
    }

    public void moveBanner(Integer id, String direction) {
        restClient.post().uri("/api/admin/category-teams/banners/{id}/move?direction={d}", id, direction).retrieve().toBodilessEntity();
    }

    private static String extractMessage(RestClientResponseException e, String fallback) {
        try {
            var body = e.getResponseBodyAs(Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record TeamDto(Integer id, String name, String code, String flag, Integer ordering, List<GroupDto> groups) {
    }

    public record TeamForm(String name, String code, Integer ordering, String flag) {
    }

    public record GroupDto(Integer id, Integer categoryTeamId, String name, String code, String flag,
                            Integer ordering, List<BannerDto> banners) {
    }

    public record GroupForm(Integer categoryTeamId, String name, String code, Integer ordering, String flag) {
    }

    public record BannerDto(Integer id, Integer categoryGroupId, String title, String linkUrl, String fileName,
                             Integer displayOrder) {
    }

    public record TeamItemDto(Integer categoryTeamItemId, Long itemId, String itemName) {
    }
}
