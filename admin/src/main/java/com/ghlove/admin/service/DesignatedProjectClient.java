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

    /**
     * 관리자 목록(메뉴 17101) 검색 - AS-IS {@code selectDesignatedDonationList}를 donation이
     * 수행하고 결과(행 + 요약집계)를 받아 온다. 모금상태 재계산·달성율·부서명 기본값 등
     * AS-IS 규칙은 donation {@code DesignatedAdminService.search} 주석에 적어 두었다.
     */
    public SearchResult search(String upperLocgovCode, String locgovCode, String bsnsType,
                               String prjStDt, String prjEdDt, String query, String prjStatus,
                               String displayFlag) {
        try {
            SearchResult result = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/designated-projects/admin/search")
                            .queryParam("upperLocgovCode", nullToEmpty(upperLocgovCode))
                            .queryParam("locgovCode", nullToEmpty(locgovCode))
                            .queryParam("bsnsType", nullToEmpty(bsnsType))
                            .queryParam("prjStDt", nullToEmpty(prjStDt))
                            .queryParam("prjEdDt", nullToEmpty(prjEdDt))
                            .queryParam("query", nullToEmpty(query))
                            .queryParam("prjStatus", nullToEmpty(prjStatus))
                            .queryParam("displayFlag", nullToEmpty(displayFlag))
                            .build())
                    .retrieve().body(SearchResult.class);
            return result != null ? result : SearchResult.empty();
        } catch (RestClientException e) {
            return SearchResult.empty();
        }
    }

    /** 목록 하단 일괄처리 - 모금상태('2'/'9') 또는 공개여부('Y'/'N'). @return 바뀐 건수 */
    public int bulkUpdate(List<Long> ids, String value, Long managerId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        try {
            Map<String, Object> body = restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/designated-projects/admin/bulk-update")
                            .queryParam("ids", ids)
                            .queryParam("value", value)
                            .queryParam("managerId", managerId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });
            Object changed = body == null ? null : body.get("changed");
            return changed instanceof Number n ? n.intValue() : 0;
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "처리에 실패했습니다."));
        } catch (RestClientException e) {
            throw new ManagerException("처리에 실패했습니다.");
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
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

    /**
     * 사업 한 건의 모금 현황 - 수정화면의 읽기전용 세 칸(남은 일수/모금 된 금액/달성률)용.
     * 조회에 실패하면 0으로 채운 값을 돌려준다(화면이 비어 보이는 것이 500보다 낫다).
     */
    public ProjectStat statOf(Long id) {
        try {
            ProjectStat stat = restClient.get()
                    .uri("/api/designated-projects/admin/{id}/stat", id).retrieve().body(ProjectStat.class);
            return stat != null ? stat : ProjectStat.empty();
        } catch (RestClientException e) {
            return ProjectStat.empty();
        }
    }

    /** AS-IS 수정화면의 읽기전용 세 칸. */
    public record ProjectStat(long sumAmt, long cntrCnt, java.math.BigDecimal rateAmt, long leftDays) {
        static ProjectStat empty() {
            return new ProjectStat(0L, 0L, java.math.BigDecimal.ZERO, 0L);
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

    /**
     * 사업부서 검색 - AS-IS {@code part/list.jsp}의 조건 4종
     * (지자체·부서명·사용유무·등록일 범위)을 donation이 수행한다.
     */
    public List<Department> searchDepartments(String upperLocgovCode, String locgovCode, String deptNm,
                                              String useYn, String searchStDt, String searchEdDt) {
        try {
            List<Department> list = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/designated-projects/admin/departments/search")
                            .queryParam("upperLocgovCode", nullToEmpty(upperLocgovCode))
                            .queryParam("locgovCode", nullToEmpty(locgovCode))
                            .queryParam("deptNm", nullToEmpty(deptNm))
                            .queryParam("useYn", nullToEmpty(useYn))
                            .queryParam("searchStDt", nullToEmpty(searchStDt))
                            .queryParam("searchEdDt", nullToEmpty(searchEdDt))
                            .build())
                    .retrieve().body(new ParameterizedTypeReference<List<Department>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    /** 사업부서 단건 - 수정화면용. */
    public Department department(Long deptId) {
        try {
            return restClient.get()
                    .uri("/api/designated-projects/admin/departments/{deptId}", deptId)
                    .retrieve().body(Department.class);
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "사업부서를 찾을 수 없습니다."));
        } catch (RestClientException e) {
            throw new ManagerException("사업부서를 찾을 수 없습니다.");
        }
    }

    /** 사업부서 수정 - 부서명·지자체·사용유무. */
    public void updateDepartment(Long deptId, String deptNm, String locgovCode, String useYn, Long managerId) {
        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/api/designated-projects/admin/departments/{deptId}")
                            .queryParam("deptNm", deptNm)
                            .queryParam("locgovCode", locgovCode)
                            .queryParam("useYn", nullToEmpty(useYn))
                            .queryParam("managerId", managerId)
                            .build(deptId))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "저장에 실패했습니다."));
        } catch (RestClientException e) {
            throw new ManagerException("저장에 실패했습니다.");
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

    // ---- 월별통계(특정사업 월별통계) - AS-IS designated-donation/analysis/month 3종 ----

    public List<BsnsStat> monthCampaign(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        return monthBsns("campaign", shWdr, shLocgovCode, selYear, prjStatus);
    }

    public List<BsnsStat> monthAmountRaised(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        return monthBsns("amountraised", shWdr, shLocgovCode, selYear, prjStatus);
    }

    private List<BsnsStat> monthBsns(String kind, String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        try {
            List<BsnsStat> list = restClient.get()
                    .uri(uri -> uri.path("/api/designated-projects/admin/analysis/month/" + kind)
                            .queryParam("shWdr", nz(shWdr)).queryParam("shLocgovCode", nz(shLocgovCode))
                            .queryParam("selYear", nz(selYear)).queryParam("prjStatus", nz(prjStatus)).build())
                    .retrieve().body(new ParameterizedTypeReference<List<BsnsStat>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<MonthAmount> monthAmount(String shWdr, String shLocgovCode, String selYear, String prjStatus) {
        try {
            List<MonthAmount> list = restClient.get()
                    .uri(uri -> uri.path("/api/designated-projects/admin/analysis/month/amount")
                            .queryParam("shWdr", nz(shWdr)).queryParam("shLocgovCode", nz(shLocgovCode))
                            .queryParam("selYear", nz(selYear)).queryParam("prjStatus", nz(prjStatus)).build())
                    .retrieve().body(new ParameterizedTypeReference<List<MonthAmount>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    // ---- 지자체별 통계(AS-IS analysis/locgov) ----

    public LocgovStatSummary locgovStatSummary(String shWdr, String shLocgovCode, String bsnsType, String frDt, String toDt, String prjStatus) {
        try {
            LocgovStatSummary s = restClient.get()
                    .uri(uri -> uri.path("/api/designated-projects/admin/analysis/locgov/summary")
                            .queryParam("shWdr", nz(shWdr)).queryParam("shLocgovCode", nz(shLocgovCode))
                            .queryParam("bsnsType", nz(bsnsType)).queryParam("frDt", nz(frDt))
                            .queryParam("toDt", nz(toDt)).queryParam("prjStatus", nz(prjStatus)).build())
                    .retrieve().body(LocgovStatSummary.class);
            return s != null ? s : new LocgovStatSummary(0, 0, java.math.BigDecimal.ZERO, 0);
        } catch (RestClientException e) {
            return new LocgovStatSummary(0, 0, java.math.BigDecimal.ZERO, 0);
        }
    }

    public List<LocgovStatRow> locgovStatList(String shWdr, String shLocgovCode, String bsnsType, String frDt, String toDt, String prjStatus) {
        try {
            List<LocgovStatRow> list = restClient.get()
                    .uri(uri -> uri.path("/api/designated-projects/admin/analysis/locgov/list")
                            .queryParam("shWdr", nz(shWdr)).queryParam("shLocgovCode", nz(shLocgovCode))
                            .queryParam("bsnsType", nz(bsnsType)).queryParam("frDt", nz(frDt))
                            .queryParam("toDt", nz(toDt)).queryParam("prjStatus", nz(prjStatus)).build())
                    .retrieve().body(new ParameterizedTypeReference<List<LocgovStatRow>>() {
                    });
            return list != null ? list : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
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

    /**
     * 목록 한 행 - AS-IS list.jsp가 쓰는 칸 그대로(donation이 집계해 내려준다).
     * {@code prjStatus}는 저장값이 아니라 <b>다시 계산된</b> 상태다(목표 초과·기간 경과면 종료).
     */
    public record ProjectRow(Long dsgnDntnBizId, String locgovNm, String prjStatus,
                              String prjImage, String bsnsType, String bsnsTypeDesc, String bsnsSubType,
                              String prjSubject, long cntrCnt, String prjStDt,
                              String prjEdDt, long goalAmt, long sumAmt, java.math.BigDecimal rateAmt,
                              int inDate, String displayFlag, String deptNm) {
    }

    /** AS-IS list.jsp 상단 요약표 - 총 목표금액/총 모금액/총 달성율/총 기부건수. */
    public record ProjectSummary(long totTargetAmt, long totCntrAmt, java.math.BigDecimal totAchvRt,
                                  long totCntrCnt) {
    }

    public record SearchResult(List<ProjectRow> rows, ProjectSummary summary) {
        static SearchResult empty() {
            return new SearchResult(List.of(),
                    new ProjectSummary(0L, 0L, java.math.BigDecimal.ZERO, 0L));
        }
    }

    /** {@code createdDate}는 AS-IS 부서목록의 "등록일시" 칸이다. */
    public record Department(Long deptId, String deptNm, String locgovCode, String useYn,
                              String createdDate) {
    }

    public record ApprovalLog(Long logId, String beforeStatusCode, String afterStatusCode, Long frstRgtrId,
                               String frstRegistPnttm) {
    }

    public record LocgovStat(String locgovCode, java.math.BigDecimal amount, long donationCount, long donorCount) {
    }

    public record MonthStat(String month, java.math.BigDecimal amount, long donationCount) {
    }

    /** 월별통계 - 사업구분/모금액 비율 행(캠페인 2행, 모금액 3행). 필드명은 AS-IS month.jsp JS가 읽는 그대로. */
    public record BsnsStat(int tp, String columnTpDesc,
                           long prjBsns100, long prjBsns200, long prjBsns300, long prjBsns400, long prjBsnsTot) {
    }

    /** 월별통계 - 월별 추이 행(목표금액/모금액/참여자수/사업건수 4행). */
    public record MonthAmount(int tp, String columnTpDesc,
                              long m01, long m02, long m03, long m04, long m05, long m06,
                              long m07, long m08, long m09, long m10, long m11, long m12, long total) {
    }

    /** 지자체별통계 요약(총목표/총모금/달성율%/총기부건수). */
    public record LocgovStatSummary(long totTargetAmt, long totCntrAmt, java.math.BigDecimal totAchvRt, long totCntrCnt) {
    }

    /** 지자체별통계 1행. */
    public record LocgovStatRow(String locgovCode, String locgovNm, long prjCnt, long statu2Cnt, long statu9Cnt,
                                long totCntrCnt, long bsnsType100Cnt, long bsnsType200Cnt, long bsnsType300Cnt,
                                long bsnsType400Cnt, long targetAmt, long cntrAmt, java.math.BigDecimal achvRt) {
    }
}
