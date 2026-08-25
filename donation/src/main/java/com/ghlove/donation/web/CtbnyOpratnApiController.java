package com.ghlove.donation.web;

import com.ghlove.donation.domain.CtbnyOpratn;
import com.ghlove.donation.domain.CtbnyOpratnFile;
import com.ghlove.donation.service.CtbnyOpratnService;
import com.ghlove.donation.service.DonationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 기부금 지출내역 - admin 서비스(운영관리 콘솔)가 호출하는 cross-service API. 실제 쓰기는
 * 여기(donation)에서만 일어난다(DB per Service) - admin은 로그인/RBAC만 담당하고, 승인된
 * 매니저의 요청을 그대로 이 API에 위임한다. 다른 cross-service API(예: NoticeClient가
 * 부르는 /api/notices)와 동일하게 별도 인증 없이 열려있다 - 브라우저에 직접 노출되지 않고
 * admin 콘솔에서만 호출되는 내부용.
 */
@RestController
@RequiredArgsConstructor
public class CtbnyOpratnApiController {

    private final CtbnyOpratnService ctbnyOpratnService;

    @GetMapping("/api/ctbny-opratn")
    public List<CtbnyOpratnDto> list(@RequestParam(required = false) String locgovCode) {
        List<CtbnyOpratn> list = (locgovCode == null || locgovCode.isBlank())
                ? ctbnyOpratnService.listAll()
                : ctbnyOpratnService.listByLocgov(locgovCode);
        return list.stream()
                .map(e -> CtbnyOpratnDto.of(e, ctbnyOpratnService.filesOf(e.getRegistSn()).stream()
                        .map(CtbnyOpratnFileDto::of).toList()))
                .toList();
    }

    @PostMapping(value = "/api/ctbny-opratn", consumes = "multipart/form-data")
    public ResponseEntity<?> create(@RequestParam String locgovCode, @RequestParam(required = false) String bsnsPurpsCode,
                                     @RequestParam String bsnsNm, @RequestParam String bsnsCn,
                                     @RequestParam String expndtrDe, @RequestParam BigDecimal expndtrAmt,
                                     @RequestParam(required = false) String rm, @RequestParam Long managerId,
                                     @RequestParam(required = false) List<MultipartFile> files) {
        try {
            CtbnyOpratn saved = ctbnyOpratnService.create(locgovCode, bsnsPurpsCode, bsnsNm, bsnsCn,
                    parseDate(expndtrDe), expndtrAmt, rm, managerId, files);
            return ResponseEntity.ok(CtbnyOpratnDto.of(saved, List.of()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/api/ctbny-opratn/{registSn}", consumes = "multipart/form-data")
    public ResponseEntity<?> update(@PathVariable Long registSn, @RequestParam(required = false) String bsnsPurpsCode,
                                     @RequestParam String bsnsNm, @RequestParam String bsnsCn,
                                     @RequestParam String expndtrDe, @RequestParam BigDecimal expndtrAmt,
                                     @RequestParam(required = false) String rm, @RequestParam Long managerId,
                                     @RequestParam(required = false) List<MultipartFile> files) {
        try {
            CtbnyOpratn saved = ctbnyOpratnService.update(registSn, bsnsPurpsCode, bsnsNm, bsnsCn,
                    parseDate(expndtrDe), expndtrAmt, rm, managerId, files);
            return ResponseEntity.ok(CtbnyOpratnDto.of(saved, List.of()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/api/ctbny-opratn/{registSn}/delete")
    public void delete(@PathVariable Long registSn) {
        ctbnyOpratnService.delete(registSn);
    }

    @PostMapping("/api/ctbny-opratn/files/{fileId}/delete")
    public void deleteFile(@PathVariable Long fileId) {
        ctbnyOpratnService.deleteFile(fileId);
    }

    private static LocalDateTime parseDate(String isoDate) {
        return LocalDate.parse(isoDate).atStartOfDay();
    }

    public record CtbnyOpratnDto(Long registSn, String locgovCode, String bsnsPurpsCode, String bsnsNm,
                                  String bsnsCn, String expndtrDe, BigDecimal expndtrAmt, String rm,
                                  List<CtbnyOpratnFileDto> files) {
        static CtbnyOpratnDto of(CtbnyOpratn e, List<CtbnyOpratnFileDto> files) {
            return new CtbnyOpratnDto(e.getRegistSn(), e.getLocgovCode(), e.getBsnsPurpsCode(), e.getBsnsNm(),
                    e.getBsnsCn(),
                    e.getExpndtrDe() != null ? e.getExpndtrDe().toLocalDate().toString() : null,
                    e.getExpndtrAmt(), e.getRm(), files);
        }
    }

    public record CtbnyOpratnFileDto(Long registFileId, String orginlFileNm) {
        static CtbnyOpratnFileDto of(CtbnyOpratnFile f) {
            return new CtbnyOpratnFileDto(f.getRegistFileId(), f.getOrginlFileNm());
        }
    }
}
