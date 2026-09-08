package com.ghlove.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

/** 지정기부(designated-donation) 관리 - 실제 데이터는 donation 서비스(G_DSGN_DNTN_BIZ_MNG
 *  등)에 있고, 이 클라이언트가 그 cross-service API를 호출한다. CtbnyOpratnClient와
 *  동일한 "쓰기 대행" 패턴(admin은 로그인/RBAC만, 실제 저장/검증은 donation). */
@Component
public class DesignatedProjectClient {

    private final RestClient restClient;

    public DesignatedProjectClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl,
            @Value("${ghlove.internal.admin-secret}") String adminSecret) {
        this.restClient = RestClient.builder().baseUrl(donationServiceBaseUrl)
                .defaultHeader("X-Internal-Secret", adminSecret).build();
    }

    public Map<String, String> codesOf(String codeType) {
        try {
            Map<String, String> map = restClient.get().uri("/api/codes/{codeType}", codeType).retrieve()
                    .body(new ParameterizedTypeReference<Map<String, String>>() {
                    });
            return map != null ? map : Map.of();
        } catch (RestClientException e) {
            return Map.of();
        }
    }

    public List<Project> listAll() {
        try {
            List<Project> list = restClient.get().uri("/api/designated-projects/admin").retrieve()
                    .body(new ParameterizedTypeReference<List<Project>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public Project get(Long id) {
        try {
            return restClient.get().uri("/api/designated-projects/admin/{id}", id).retrieve().body(Project.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지정기부사업을 찾을 수 없습니다."));
        }
    }

    public Project create(ProjectForm form, MultipartFile image, boolean canSelfApprove, Long managerId) {
        try {
            return restClient.post().uri("/api/designated-projects/admin")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(form, image, canSelfApprove, managerId))
                    .retrieve().body(Project.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지정기부사업 등록에 실패했습니다."));
        }
    }

    public Project update(Long id, ProjectForm form, MultipartFile image, boolean canSelfApprove, Long managerId) {
        try {
            return restClient.post().uri("/api/designated-projects/admin/{id}", id)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(form, image, canSelfApprove, managerId))
                    .retrieve().body(Project.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지정기부사업 수정에 실패했습니다."));
        }
    }

    public List<ApprovalLog> approvalLogOf(Long id) {
        try {
            List<ApprovalLog> list = restClient.get().uri("/api/designated-projects/admin/{id}/approval-log", id)
                    .retrieve().body(new ParameterizedTypeReference<List<ApprovalLog>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<Notice> noticesOf(Long id) {
        try {
            List<Notice> list = restClient.get().uri("/api/designated-projects/admin/{id}/notices", id)
                    .retrieve().body(new ParameterizedTypeReference<List<Notice>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void createNotice(Long id, String subject, String content) {
        try {
            restClient.post().uri("/api/designated-projects/admin/{id}/notices", id)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody(Map.of("subject", subject, "content", content)))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "공지사항 등록에 실패했습니다."));
        }
    }

    public void updateNotice(Long noticeId, String subject, String content) {
        try {
            restClient.post().uri("/api/designated-projects/admin/notices/{noticeId}", noticeId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody(Map.of("subject", subject, "content", content)))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "공지사항 수정에 실패했습니다."));
        }
    }

    public void deleteNotice(Long noticeId) {
        try {
            restClient.post().uri("/api/designated-projects/admin/notices/{noticeId}/delete", noticeId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("공지사항 삭제에 실패했습니다.");
        }
    }

    public List<Department> departments(String locgovCode) {
        try {
            String uri = locgovCode == null || locgovCode.isBlank()
                    ? "/api/designated-projects/admin/departments"
                    : "/api/designated-projects/admin/departments?locgovCode={locgovCode}";
            List<Department> list = restClient.get().uri(uri, locgovCode == null ? "" : locgovCode)
                    .retrieve().body(new ParameterizedTypeReference<List<Department>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void createDepartment(String deptNm, String locgovCode, Long managerId) {
        try {
            restClient.post().uri("/api/designated-projects/admin/departments")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody(Map.of("deptNm", deptNm, "locgovCode", locgovCode, "managerId", String.valueOf(managerId))))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "담당부서 등록에 실패했습니다."));
        }
    }

    public void toggleDepartment(Long deptId, Long managerId) {
        try {
            restClient.post().uri("/api/designated-projects/admin/departments/{deptId}/toggle?managerId={managerId}", deptId, managerId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "처리에 실패했습니다."));
        }
    }

    public List<String> analysisYears() {
        try {
            List<String> list = restClient.get().uri("/api/designated-projects/admin/analysis/years")
                    .retrieve().body(new ParameterizedTypeReference<List<String>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<LocgovStat> analysisByLocgov(String year) {
        try {
            String uri = year == null || year.isBlank()
                    ? "/api/designated-projects/admin/analysis/locgov"
                    : "/api/designated-projects/admin/analysis/locgov?year={year}";
            List<LocgovStat> list = restClient.get().uri(uri, year == null ? "" : year)
                    .retrieve().body(new ParameterizedTypeReference<List<LocgovStat>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<MonthStat> analysisByMonth(String year) {
        try {
            String uri = year == null || year.isBlank()
                    ? "/api/designated-projects/admin/analysis/month"
                    : "/api/designated-projects/admin/analysis/month?year={year}";
            List<MonthStat> list = restClient.get().uri(uri, year == null ? "" : year)
                    .retrieve().body(new ParameterizedTypeReference<List<MonthStat>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static MultiValueMap<String, String> formBody(Map<String, String> params) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        params.forEach(body::add);
        return body;
    }

    private static MultiValueMap<String, Object> toMultipart(ProjectForm form, MultipartFile image,
                                                               boolean canSelfApprove, Long managerId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("dsgnDntnBizTtl", form.dsgnDntnBizTtl());
        body.add("dsgnDntnBizCn", form.dsgnDntnBizCn());
        body.add("dsgnDntnBizBgngYmd", form.dsgnDntnBizBgngYmd());
        body.add("dsgnDntnBizEndYmd", form.dsgnDntnBizEndYmd());
        body.add("goalAmt", form.goalAmt());
        body.add("dsgnDntnBizSttsCd", form.dsgnDntnBizSttsCd());
        body.add("rlsYn", form.rlsYn());
        body.add("lclgvCd", form.lclgvCd());
        body.add("dsgnDntnBizSeCd", form.dsgnDntnBizSeCd());
        if (form.bsnsSubType() != null) {
            body.add("bsnsSubType", form.bsnsSubType());
        }
        if (form.contentEtc() != null) {
            body.add("contentEtc", form.contentEtc());
        }
        if (form.deptId() != null) {
            body.add("deptId", form.deptId());
        }
        body.add("canSelfApprove", canSelfApprove);
        body.add("managerId", managerId);
        if (image != null && !image.isEmpty()) {
            body.add("image", toResource(image));
        }
        return body;
    }

    private static org.springframework.core.io.Resource toResource(MultipartFile file) {
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

    public record ProjectForm(String dsgnDntnBizTtl, String dsgnDntnBizCn, String dsgnDntnBizBgngYmd,
                               String dsgnDntnBizEndYmd, Long goalAmt, String dsgnDntnBizSttsCd, String rlsYn,
                               String lclgvCd, String dsgnDntnBizSeCd, String bsnsSubType, String contentEtc,
                               Long deptId) {
    }

    public record Project(Long dsgnDntnBizId, String dsgnDntnBizTtl, String dsgnDntnBizCn,
                           String dsgnDntnBizBgngYmd, String dsgnDntnBizEndYmd, Long goalAmt,
                           String dsgnDntnBizSttsCd, String rlsYn, String lclgvCd, String dsgnDntnBizSeCd,
                           String bsnsSubType, String contentEtc, Long deptId, String imageUrl) {
    }

    public record Notice(Long prjNoticeId, Long dsgnDntnBizId, String prjNoticeSubject, String prjNoticeCn,
                          String frstRegistPnttm) {
    }

    public record Department(Long deptId, String deptNm, String locgovCode, String useYn) {
    }

    public record ApprovalLog(Long logId, String beforeStatusCode, String afterStatusCode, Long frstRgtrId,
                               String frstRegistPnttm) {
    }

    public record LocgovStat(String locgovCode, java.math.BigDecimal amount, long donationCount, long donorCount) {
    }

    public record MonthStat(String month, java.math.BigDecimal amount, long donationCount) {
    }
}
