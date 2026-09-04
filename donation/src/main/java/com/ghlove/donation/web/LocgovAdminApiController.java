package com.ghlove.donation.web;

import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.domain.LocgovImage;
import com.ghlove.donation.domain.LocgovLmtt;
import com.ghlove.donation.service.DonationException;
import com.ghlove.donation.service.LocgovAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * admin 콘솔 "지자체 마스터관리" 화면(AS-IS opmanager/user/locgov)이 부르는 cross-service
 * API - 실제 쓰기는 여기(donation)에서만 일어난다(DB per Service). 다른 cross-service
 * API(CtbnyOpratnApiController 등)와 동일하게 별도 인증 없이 열려있다 - 브라우저에 직접
 * 노출되지 않고 admin 콘솔에서만 호출되는 내부용.
 *
 * 폐기된 LocgovSealAdminController(무인증 내부용 임시 화면)의 직인 업로드/미리보기 기능을
 * 여기로 완전히 흡수했다.
 */
@RestController
@RequestMapping("/api/locgov-admin")
@RequiredArgsConstructor
public class LocgovAdminApiController {

    private final LocgovAdminService locgovAdminService;

    @GetMapping
    public PageDto<LocgovDto> search(@RequestParam(required = false) String locgovCode,
                                      @RequestParam(required = false) String locgovNm,
                                      @RequestParam(required = false) String chargerNm,
                                      @RequestParam(required = false) String chargerCttpc,
                                      @RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        Page<Locgov> result = locgovAdminService.search(locgovCode, locgovNm, chargerNm, chargerCttpc,
                parseStart(startDate), parseEnd(endDate), page, size);
        return new PageDto<>(result.getContent().stream().map(LocgovDto::of).toList(),
                result.getTotalElements(), result.getTotalPages(), result.getNumber());
    }

    /** 등록 폼의 "지자체(시도/시군구)" 선택 드롭다운용 - 아직 담당자정보가 없는 행만. */
    @GetMapping("/registrable")
    public List<LocgovDto> registrable() {
        return locgovAdminService.registrableLocgovs().stream().map(LocgovDto::of).toList();
    }

    @GetMapping("/{locgovCode}")
    public LocgovDetailDto detail(@PathVariable String locgovCode) {
        Locgov locgov = locgovAdminService.findOrThrow(locgovCode);
        LocgovImage image = locgovAdminService.imageOf(locgovCode);
        return LocgovDetailDto.of(locgov, image);
    }

    @PostMapping(value = "/{locgovCode}/register", consumes = "multipart/form-data")
    public ResponseEntity<?> register(@PathVariable String locgovCode, LocgovFormRequest req,
                                       @RequestParam(required = false) MultipartFile offcsFile,
                                       @RequestParam(required = false) MultipartFile addPcFile,
                                       @RequestParam(required = false) MultipartFile addMbFile) {
        try {
            Locgov saved = locgovAdminService.register(locgovCode, req.toForm(), offcsFile, addPcFile, addMbFile,
                    req.managerId());
            return ResponseEntity.ok(LocgovDto.of(saved));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/{locgovCode}", consumes = "multipart/form-data")
    public ResponseEntity<?> update(@PathVariable String locgovCode, LocgovFormRequest req,
                                     @RequestParam(required = false) MultipartFile offcsFile,
                                     @RequestParam(required = false) MultipartFile addPcFile,
                                     @RequestParam(required = false) MultipartFile addMbFile) {
        try {
            Locgov saved = locgovAdminService.update(locgovCode, req.toForm(), offcsFile, addPcFile, addMbFile,
                    req.managerId());
            return ResponseEntity.ok(LocgovDto.of(saved));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{locgovCode}/delete")
    public void deactivate(@PathVariable String locgovCode, @RequestParam Long managerId) {
        locgovAdminService.deactivate(locgovCode, managerId);
    }

    @GetMapping("/{locgovCode}/dept-hist")
    public List<DeptHistDto> deptHist(@PathVariable String locgovCode) {
        return locgovAdminService.deptHistOf(locgovCode).stream()
                .map(h -> new DeptHistDto(h.getDeptHistNo(), h.getProcessDeptCode(), h.getLastUpdtPnttm()))
                .toList();
    }

    @GetMapping("/{locgovCode}/lmtt")
    public List<LmttDto> lmttList(@PathVariable String locgovCode) {
        return locgovAdminService.lmttOf(locgovCode).stream()
                .map(l -> new LmttDto(l.getLmttBgnDe(), l.getLmttEndDe(), l.getVioltResnCode(), l.getVioltResnCn(),
                        l.getRegisterNm()))
                .toList();
    }

    @PostMapping("/{locgovCode}/lmtt")
    public ResponseEntity<?> createLmtt(@PathVariable String locgovCode, @RequestBody LmttCreateRequest req) {
        try {
            LocgovLmtt saved = locgovAdminService.registerLmtt(locgovCode, req.lmttBgnDe(), req.lmttEndDe(),
                    req.violtResnCode(), req.violtResnCn(), req.registerNm(), req.managerId());
            return ResponseEntity.ok(new LmttDto(saved.getLmttBgnDe(), saved.getLmttEndDe(), saved.getVioltResnCode(),
                    saved.getVioltResnCn(), saved.getRegisterNm()));
        } catch (DonationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{locgovCode}/lmtt/delete")
    public void deleteLmtt(@PathVariable String locgovCode, @RequestParam String lmttBgnDe,
                            @RequestParam String lmttEndDe) {
        locgovAdminService.deleteLmtt(locgovCode, lmttBgnDe, lmttEndDe);
    }

    /** 직인 미리보기 - 복호화된 원본 바이트. AS-IS sealView와 동일. */
    @GetMapping("/{locgovCode}/seal")
    public ResponseEntity<byte[]> seal(@PathVariable String locgovCode) {
        byte[] bytes = locgovAdminService.sealPreview(locgovCode);
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    @GetMapping("/{locgovCode}/image/{type}")
    public ResponseEntity<byte[]> image(@PathVariable String locgovCode, @PathVariable String type) {
        byte[] bytes = locgovAdminService.imageBytes(locgovCode, "pc".equalsIgnoreCase(type));
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    @PostMapping("/{locgovCode}/image/{type}/delete")
    public void deleteImage(@PathVariable String locgovCode, @PathVariable String type, @RequestParam Long managerId) {
        locgovAdminService.deleteImage(locgovCode, "pc".equalsIgnoreCase(type), managerId);
    }

    private static LocalDateTime parseStart(String isoDate) {
        return (isoDate == null || isoDate.isBlank()) ? null : LocalDate.parse(isoDate).atStartOfDay();
    }

    private static LocalDateTime parseEnd(String isoDate) {
        return (isoDate == null || isoDate.isBlank()) ? null : LocalDate.parse(isoDate).plusDays(1).atStartOfDay();
    }

    public record PageDto<T>(List<T> content, long totalElements, int totalPages, int page) {
    }

    public record LocgovFormRequest(String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                                     String locgovHmpg, String locgovIntrcnCn, String locgovZip, String bassAdres,
                                     String dtlAdres, Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                                     String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                                     String chargerPsitnDept, String processDeptCode, String administInsttCode,
                                     String fisSp, String offcsNm, Long managerId) {
        LocgovAdminService.LocgovForm toForm() {
            return new LocgovAdminService.LocgovForm(bizrno, chargerNm, chargerCttpc, chargerEmail, locgovHmpg,
                    locgovIntrcnCn, locgovZip, bassAdres, dtlAdres, locgovBudgetAmt, locgovPopltnCo, locgovAr,
                    locgovSpcprd, gcctUseAt, etrcshUseAt, achlqrSleAt, chargerPsitnDept, processDeptCode,
                    administInsttCode, fisSp, offcsNm);
        }
    }

    public record LmttCreateRequest(String lmttBgnDe, String lmttEndDe, String violtResnCode, String violtResnCn,
                                     String registerNm, Long managerId) {
    }

    public record DeptHistDto(Integer deptHistNo, String processDeptCode, LocalDateTime changedAt) {
    }

    public record LmttDto(String lmttBgnDe, String lmttEndDe, String violtResnCode, String violtResnCn,
                           String registerNm) {
    }

    public record LocgovDto(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm,
                             String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                             Long locgovBudgetAmt, String locgovPopltnCo, String useAt, LocalDateTime frstRegistPnttm) {
        static LocgovDto of(Locgov l) {
            return new LocgovDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(), l.getUpperLocgovNm(),
                    l.getBizrno(), l.getChargerNm(), l.getChargerCttpc(), l.getChargerEmail(),
                    l.getLocgovBudgetAmt(), l.getLocgovPopltnCo(), l.getUseAt(), l.getFrstRegistPnttm());
        }
    }

    public record LocgovDetailDto(String locgovCode, String locgovNm, String upperLocgovCode, String upperLocgovNm,
                                   String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                                   String locgovHmpg, String locgovIntrcnCn, String locgovZip, String bassAdres,
                                   String dtlAdres, Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                                   String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                                   String chargerPsitnDept, String processDeptCode, String administInsttCode,
                                   String fisSp, String offcsNm, boolean hasSeal, boolean hasPcImage,
                                   boolean hasMobileImage, String useAt, LocalDateTime frstRegistPnttm) {
        static LocgovDetailDto of(Locgov l, LocgovImage image) {
            return new LocgovDetailDto(l.getLocgovCode(), l.getLocgovNm(), l.getUpperLocgovCode(),
                    l.getUpperLocgovNm(), l.getBizrno(), l.getChargerNm(), l.getChargerCttpc(), l.getChargerEmail(),
                    l.getLocgovHmpg(), l.getLocgovIntrcnCn(), l.getLocgovZip(), l.getBassAdres(), l.getDtlAdres(),
                    l.getLocgovBudgetAmt(), l.getLocgovPopltnCo(), l.getLocgovAr(), l.getLocgovSpcprd(),
                    l.getGcctUseAt(), l.getEtrcshUseAt(), l.getAchlqrSleAt(), l.getChargerPsitnDept(),
                    l.getProcessDeptCode(), l.getAdministInsttCode(), l.getFisSp(), l.getOffcsNm(),
                    l.getOffcsFileNm() != null && !l.getOffcsFileNm().isBlank(),
                    image != null && image.getPcFileName() != null && !image.getPcFileName().isBlank(),
                    image != null && image.getMobileFileName() != null && !image.getMobileFileName().isBlank(),
                    l.getUseAt(), l.getFrstRegistPnttm());
        }
    }
}
