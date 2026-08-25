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
import java.math.BigDecimal;
import java.util.List;

/**
 * 기부금 지출내역 (AS-IS give-operation) - 실제 데이터는 donation 서비스(G_CTBNY_OPRATN)에
 * 있고, 이 클라이언트가 그 cross-service API(/api/ctbny-opratn)를 호출한다. 다른
 * client(LocgovClient/MemberClient)와 달리 이건 "쓰기"까지 대행한다 - 로그인/RBAC은
 * admin이 갖고 실제 저장은 donation 자신의 DB에만 하기 위함(DB per Service).
 */
@Component
public class CtbnyOpratnClient {

    private final RestClient restClient;

    public CtbnyOpratnClient(@Value("${ghlove.donation-service.base-url}") String donationServiceBaseUrl) {
        this.restClient = RestClient.create(donationServiceBaseUrl);
    }

    public List<Row> listByLocgov(String locgovCode) {
        try {
            List<Row> rows = restClient.get()
                    .uri("/api/ctbny-opratn?locgovCode={locgovCode}", locgovCode)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Row>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public List<Row> listAll() {
        try {
            List<Row> rows = restClient.get()
                    .uri("/api/ctbny-opratn")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Row>>() {
                    });
            return rows != null ? rows : List.of();
        } catch (RestClientException e) {
            return List.of();
        }
    }

    public void create(String locgovCode, String bsnsPurpsCode, String bsnsNm, String bsnsCn,
                        String expndtrDe, BigDecimal expndtrAmt, String rm, Long managerId, List<MultipartFile> files) {
        try {
            restClient.post().uri("/api/ctbny-opratn")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(locgovCode, bsnsPurpsCode, bsnsNm, bsnsCn, expndtrDe, expndtrAmt, rm, managerId, files))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지출내역 등록에 실패했습니다."));
        }
    }

    public void update(Long registSn, String bsnsPurpsCode, String bsnsNm, String bsnsCn,
                        String expndtrDe, BigDecimal expndtrAmt, String rm, Long managerId, List<MultipartFile> files) {
        try {
            restClient.post().uri("/api/ctbny-opratn/{registSn}", registSn)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(toMultipart(null, bsnsPurpsCode, bsnsNm, bsnsCn, expndtrDe, expndtrAmt, rm, managerId, files))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            throw new ManagerException(extractMessage(e, "지출내역 수정에 실패했습니다."));
        }
    }

    public void delete(Long registSn) {
        try {
            restClient.post().uri("/api/ctbny-opratn/{registSn}/delete", registSn)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("지출내역 삭제에 실패했습니다.");
        }
    }

    public void deleteFile(Long fileId) {
        try {
            restClient.post().uri("/api/ctbny-opratn/files/{fileId}/delete", fileId)
                    .retrieve().toBodilessEntity();
        } catch (RestClientException e) {
            throw new ManagerException("파일 삭제에 실패했습니다.");
        }
    }

    private MultiValueMap<String, Object> toMultipart(String locgovCode, String bsnsPurpsCode, String bsnsNm,
                                                        String bsnsCn, String expndtrDe, BigDecimal expndtrAmt,
                                                        String rm, Long managerId, List<MultipartFile> files) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        if (locgovCode != null) {
            body.add("locgovCode", locgovCode);
        }
        if (bsnsPurpsCode != null) {
            body.add("bsnsPurpsCode", bsnsPurpsCode);
        }
        body.add("bsnsNm", bsnsNm);
        body.add("bsnsCn", bsnsCn);
        body.add("expndtrDe", expndtrDe);
        body.add("expndtrAmt", expndtrAmt);
        if (rm != null) {
            body.add("rm", rm);
        }
        body.add("managerId", managerId);
        if (files != null) {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                body.add("files", toResource(file));
            }
        }
        return body;
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
            var body = e.getResponseBodyAs(java.util.Map.class);
            Object msg = body != null ? body.get("message") : null;
            return msg != null ? msg.toString() : fallback;
        } catch (RuntimeException ex) {
            return fallback;
        }
    }

    public record Row(Long registSn, String locgovCode, String bsnsPurpsCode, String bsnsNm, String bsnsCn,
                       String expndtrDe, BigDecimal expndtrAmt, String rm, List<FileRow> files) {
    }

    public record FileRow(Long registFileId, String orginlFileNm) {
    }
}
