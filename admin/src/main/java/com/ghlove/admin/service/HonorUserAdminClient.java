package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 기부혜택증 관리(메뉴 19101·19102·19103)가 쓰는 donation API 클라이언트.
 *
 * <p>기부혜택증 설정({@code G_LCLGV_HNR_USER_STNG_MNG})·메인이미지 설명
 * ({@code G_LCLGV_HNR_USER_RWRD_IMG_EXPLN})·열람이력({@code OP_HONOR_VIEW_HIST})이 모두
 * donation 소유다. admin 스키마에도 같은 이름의 빈 표가 있었지만(예전 TO-BE 구현) 실제
 * 열람이력이 쌓이는 곳은 donation이라 그쪽을 정본으로 쓴다 - 그 전까지 열람현황 화면은
 * 빈 표를 읽어 <b>항상 0건</b>이었다.
 */
@Service
public class HonorUserAdminClient {

    private final RestClient restClient;

    public HonorUserAdminClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl).build();
    }

    /** 19101 설정 목록. */
    public List<SettingRow> list(String upperLocgovCode, String lclgvCd) {
        try {
            List<SettingRow> rows = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/admin/honor-users");
                        if (notBlank(upperLocgovCode)) {
                            uriBuilder.queryParam("upperLocgovCode", upperLocgovCode);
                        }
                        if (notBlank(lclgvCd)) {
                            uriBuilder.queryParam("lclgvCd", lclgvCd);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve().body(new ParameterizedTypeReference<List<SettingRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 19102 설정 상세 + 이미지 설명. */
    public Detail detail(String lclgvCd) {
        try {
            Detail detail = restClient.get().uri("/api/admin/honor-users/{code}", lclgvCd)
                    .retrieve().body(Detail.class);
            return detail != null ? detail : new Detail(null, List.of());
        } catch (RestClientException e) {
            return new Detail(null, List.of());
        }
    }

    /** 19102 설정 저장. 메인이미지는 AS-IS처럼 여러 장을 받되 첫 장을 대표로 쓴다. */
    public void save(String lclgvCd, Integer brnzGrdDntnAmt, String hnrUserStngTtl, String hnrUserRwrd,
                      String hnrUserSlctnSeCd, String useYn, List<String> imageExplains,
                      List<MultipartFile> images, Long managerId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (brnzGrdDntnAmt != null) {
            body.add("brnzGrdDntnAmt", brnzGrdDntnAmt);
        }
        addIfPresent(body, "hnrUserStngTtl", hnrUserStngTtl);
        addIfPresent(body, "hnrUserRwrd", hnrUserRwrd);
        addIfPresent(body, "hnrUserSlctnSeCd", hnrUserSlctnSeCd);
        addIfPresent(body, "useYn", useYn);
        if (managerId != null) {
            body.add("managerId", managerId);
        }
        if (imageExplains != null) {
            for (String explain : imageExplains) {
                body.add("imageExplains", explain == null ? "" : explain);
            }
        }
        if (images != null) {
            for (MultipartFile image : images) {
                if (image != null && !image.isEmpty()) {
                    body.add("images", toResource(image));
                }
            }
        }

        try {
            restClient.post().uri("/api/admin/honor-users/{code}", lclgvCd)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("저장에 실패했습니다.");
        }
    }

    /** 19102 대표이미지 삭제 (AS-IS deleteItemFile - 삭제 건수를 그대로 돌려준다). */
    public int deleteMainImage(String lclgvCd) {
        try {
            Map<?, ?> result = restClient.post().uri("/api/admin/honor-users/{code}/main-image/delete", lclgvCd)
                    .retrieve().body(Map.class);
            Object deleted = result != null ? result.get("deleted") : null;
            return deleted instanceof Number n ? n.intValue() : 0;
        } catch (RestClientException e) {
            return 0;
        }
    }

    public byte[] mainImageBytes(String lclgvCd) {
        try {
            return restClient.get().uri("/api/admin/honor-users/{code}/main-image", lclgvCd)
                    .retrieve().body(byte[].class);
        } catch (RestClientException e) {
            return null;
        }
    }

    /** 19103 열람현황 - 사용자명은 여기 없다(admin이 member에서 채운다). */
    public List<ViewHistRow> viewHist(String startDate, String endDate, String upperLocgovCode, String lclgvCd) {
        try {
            List<ViewHistRow> rows = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/admin/honor-users/view-hist");
                        if (notBlank(startDate)) {
                            uriBuilder.queryParam("startDate", startDate);
                        }
                        if (notBlank(endDate)) {
                            uriBuilder.queryParam("endDate", endDate);
                        }
                        if (notBlank(upperLocgovCode)) {
                            uriBuilder.queryParam("upperLocgovCode", upperLocgovCode);
                        }
                        if (notBlank(lclgvCd)) {
                            uriBuilder.queryParam("lclgvCd", lclgvCd);
                        }
                        return uriBuilder.build();
                    })
                    .retrieve().body(new ParameterizedTypeReference<List<ViewHistRow>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static void addIfPresent(MultiValueMap<String, Object> body, String name, String value) {
        if (value != null) {
            body.add(name, value);
        }
    }

    private static ByteArrayResource toResource(MultipartFile file) {
        try {
            return new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new ManagerException("이미지 파일을 읽을 수 없습니다.");
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    /** 발급기준 구분명(코드라벨)은 admin의 OP_COMMON_CODE에서 붙인다. */
    public record SettingRow(String lclgvCd, String upperLocgovCode, String upperLocgovNm, String lclgvCdNm,
                              Integer gldGrdDntnAmt, Integer slvrGrdDntnAmt, Integer brnzGrdDntnAmt,
                              String hnrUserStngTtl, String hnrUserRwrd, String rprsImgNm,
                              String hnrUserSlctnSeCd, String useYn, LocalDateTime lastRegDt, Long lastRgtrId) {
    }

    public record Detail(SettingRow setting, List<String> imageExplains) {
    }

    public record ViewHistRow(String viewYm, Long userId, String lclgvCd, String upperLocgovCode,
                               String upperLocgovNm, String lclgvCdNm, long viewCnt) {
    }
}
