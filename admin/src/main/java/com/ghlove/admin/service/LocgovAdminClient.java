package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 지자체 마스터관리 (AS-IS opmanager/user/locgov) - 실제 데이터는 donation 서비스에 있고
 * (LocgovAdminApiController, /api/locgov-admin), 이 클라이언트가 그 cross-service API를
 * 호출한다. 읽기전용인 기존 {@link LocgovClient}(권한요청 폼의 광역지자체 선택용)와 달리
 * 이건 쓰기까지 대행한다 - CtbnyOpratnClient와 동일한 패턴(DB per Service: 실제 저장은
 * donation 자신의 DB에만 한다).
 *
 * 폐기된 donation의 LocgovSealAdminController(무인증 내부용 임시 직인 업로드 화면)가 하던
 * 일을 이 화면의 직인 등록 섹션이 완전히 대체한다.
 */
@Component
public class LocgovAdminClient {

    private final RestClient restClient;

    public LocgovAdminClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public PageResult search(String locgovCode, String locgovNm, String chargerNm, String chargerCttpc,
                              String startDate, String endDate, int page, int size) {
        try {
            PageResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/locgov-admin")
                            .queryParamIfPresent("locgovCode", java.util.Optional.ofNullable(blankToNull(locgovCode)))
                            .queryParamIfPresent("locgovNm", java.util.Optional.ofNullable(blankToNull(locgovNm)))
                            .queryParamIfPresent("chargerNm", java.util.Optional.ofNullable(blankToNull(chargerNm)))
                            .queryParamIfPresent("chargerCttpc", java.util.Optional.ofNullable(blankToNull(chargerCttpc)))
                            .queryParamIfPresent("startDate", java.util.Optional.ofNullable(blankToNull(startDate)))
                            .queryParamIfPresent("endDate", java.util.Optional.ofNullable(blankToNull(endDate)))
                            .queryParam("page", page)
                            .queryParam("size", size)
                            .build())
                    .retrieve()
                    .body(PageResult.class);
            return result != null ? result : new PageResult(List.of(), 0, 0, 0);
        } catch (RestClientException e) {
            return new PageResult(List.of(), 0, 0, 0);
        }
    }

    public List<Locgov> registrable() {
        try {
            List<Locgov> list = restClient.get().uri("/api/locgov-admin/registrable").retrieve()
                    .body(new ParameterizedTypeReference<List<Locgov>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public LocgovDetail get(String locgovCode) {
        try {
            return restClient.get().uri("/api/locgov-admin/{code}", locgovCode).retrieve().body(LocgovDetail.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지자체 정보를 찾을 수 없습니다."));
        }
    }

    public void register(String locgovCode, Map<String, Object> fields, MultipartFile offcsFile,
                          MultipartFile addPcFile, MultipartFile addMbFile) {
        try {
            restClient.post().uri("/api/locgov-admin/{code}/register", locgovCode)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(fields, offcsFile, addPcFile, addMbFile))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            String message = extractMessage(e, "지자체 등록에 실패했습니다.");
            throw new ManagerException("DUP".equals(message) ? "이미 등록된 지자체 정보가 존재합니다." : message);
        }
    }

    public void update(String locgovCode, Map<String, Object> fields, MultipartFile offcsFile,
                        MultipartFile addPcFile, MultipartFile addMbFile) {
        try {
            restClient.post().uri("/api/locgov-admin/{code}", locgovCode)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(fields, offcsFile, addPcFile, addMbFile))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지자체 정보 수정에 실패했습니다."));
        }
    }

    public void deactivate(String locgovCode, Long managerId) {
        try {
            restClient.post().uri("/api/locgov-admin/{code}/delete?managerId={managerId}", locgovCode, managerId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("지자체 비활성화에 실패했습니다.");
        }
    }

    public List<DeptHist> deptHist(String locgovCode) {
        try {
            List<DeptHist> list = restClient.get().uri("/api/locgov-admin/{code}/dept-hist", locgovCode).retrieve()
                    .body(new ParameterizedTypeReference<List<DeptHist>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<Lmtt> lmttList(String locgovCode) {
        try {
            List<Lmtt> list = restClient.get().uri("/api/locgov-admin/{code}/lmtt", locgovCode).retrieve()
                    .body(new ParameterizedTypeReference<List<Lmtt>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void createLmtt(String locgovCode, String lmttBgnDe, String lmttEndDe, String violtResnCode,
                            String violtResnCn, String registerNm, Long managerId) {
        try {
            restClient.post().uri("/api/locgov-admin/{code}/lmtt", locgovCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("lmttBgnDe", lmttBgnDe, "lmttEndDe", lmttEndDe, "violtResnCode", violtResnCode,
                            "violtResnCn", violtResnCn == null ? "" : violtResnCn, "registerNm", registerNm,
                            "managerId", managerId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "기부금모금제한 등록에 실패했습니다."));
        }
    }

    public void deleteLmtt(String locgovCode, String lmttBgnDe, String lmttEndDe) {
        try {
            restClient.post()
                    .uri("/api/locgov-admin/{code}/lmtt/delete?lmttBgnDe={bgn}&lmttEndDe={end}", locgovCode,
                            lmttBgnDe, lmttEndDe)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("기부금모금제한 삭제에 실패했습니다.");
        }
    }

    public byte[] sealBytes(String locgovCode) {
        return fetchBytes("/api/locgov-admin/{code}/seal", locgovCode);
    }

    public byte[] imageBytes(String locgovCode, String type) {
        return fetchBytes("/api/locgov-admin/{code}/image/{type}", locgovCode, type);
    }

    public void deleteImage(String locgovCode, String type, Long managerId) {
        try {
            restClient.post()
                    .uri("/api/locgov-admin/{code}/image/{type}/delete?managerId={managerId}", locgovCode, type,
                            managerId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("이미지 삭제에 실패했습니다.");
        }
    }

    private byte[] fetchBytes(String uri, Object... vars) {
        try {
            return restClient.get().uri(uri, vars).retrieve().body(byte[].class);
        } catch (RestClientException e) {
            return null;
        }
    }

    private MultiValueMap<String, Object> toMultipart(Map<String, Object> fields, MultipartFile offcsFile,
                                                        MultipartFile addPcFile, MultipartFile addMbFile) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        fields.forEach((k, v) -> {
            if (v != null) {
                body.add(k, String.valueOf(v));
            }
        });
        addFile(body, "offcsFile", offcsFile);
        addFile(body, "addPcFile", addPcFile);
        addFile(body, "addMbFile", addMbFile);
        return body;
    }

    private void addFile(MultiValueMap<String, Object> body, String key, MultipartFile file) {
        if (file != null && !file.isEmpty()) {
            body.add(key, toResource(file));
        }
    }

    private org.springframework.core.io.Resource toResource(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return new org.springframework.core.io.ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    public record PageResult(List<Locgov> content, long totalElements, int totalPages, int page) {
    }

    public record Locgov(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm,
                          String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                          Long locgovBudgetAmt, String locgovPopltnCo, String useAt, LocalDateTime frstRegistPnttm) {
    }

    public record LocgovDetail(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm,
                                String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                                String locgovHmpg, String locgovIntrcnCn, String locgovZip, String bassAdres,
                                String dtlAdres, Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                                String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                                String chargerPsitnDept, String processDeptCode, String administInsttCode,
                                String fisSp, String offcsNm, boolean hasSeal, boolean hasPcImage,
                                boolean hasMobileImage, String useAt, LocalDateTime frstRegistPnttm) {
    }

    public record DeptHist(Integer deptHistNo, String processDeptCode, LocalDateTime changedAt) {
    }

    public record Lmtt(String lmttBgnDe, String lmttEndDe, String violtResnCode, String violtResnCn,
                        String registerNm) {
    }
}
