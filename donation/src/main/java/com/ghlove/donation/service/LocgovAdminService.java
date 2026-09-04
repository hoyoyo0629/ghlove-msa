package com.ghlove.donation.service;

import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.domain.LocgovDeptHist;
import com.ghlove.donation.domain.LocgovImage;
import com.ghlove.donation.domain.LocgovLmtt;
import com.ghlove.donation.repository.LocgovDeptHistRepository;
import com.ghlove.donation.repository.LocgovImageRepository;
import com.ghlove.donation.repository.LocgovLmttRepository;
import com.ghlove.donation.repository.LocgovRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * admin 콘솔의 "지자체 마스터관리" 화면(AS-IS opmanager/user/locgov) - 실제 데이터/도메인
 * 로직은 여기(donation)에 있고, admin은 로그인/RBAC과 화면만 담당한다(다른 cross-service
 * client와 동일한 패턴). G_LOCGOV는 전국 행정구역 246건이 이미 시드되어 있어 "등록"은 새
 * PK를 만드는 게 아니라 그중 아직 담당자정보가 채워지지 않은 행을 골라 나머지 항목을
 * 채우는 것이다(AS-IS validator의 "이미 등록된 지자체 정보가 존재합니다" DUP 체크가 바로
 * 이 상태를 가리킨다 - 사업자등록번호가 이미 있으면 등록완료로 간주).
 *
 * "삭제"는 G_LOCGOV를 물리 삭제하지 않는다 - G_CNTR/G_CNTR_LMTT/G_HONOR_CNTRBTR 등 FK가
 * 이 코드를 참조하므로 하드 삭제는 위험하고, 애초에 이 표의 PK는 "행정구역 코드"라는 고정
 * 마스터 데이터라 지워선 안 된다. 대신 이미 있던 USE_AT 컬럼으로 비활성화("미등록으로
 * 되돌리기")한다.
 */
@Service
@RequiredArgsConstructor
public class LocgovAdminService {

    private final LocgovRepository locgovRepository;
    private final LocgovDeptHistRepository locgovDeptHistRepository;
    private final LocgovImageRepository locgovImageRepository;
    private final LocgovLmttRepository locgovLmttRepository;
    private final LocgovSealService locgovSealService;
    private final FileStorageService fileStorageService;

    private static final String IMAGE_SUBDIR = "locgov-image";

    public Page<Locgov> search(String locgovCode, String locgovNm, String chargerNm, String chargerCttpc,
                                LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        String code = blankToNull(locgovCode);
        String nm = blankToNull(locgovNm);
        String charger = blankToNull(chargerNm);
        String cttpc = blankToNull(chargerCttpc);
        Specification<Locgov> spec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (code != null) {
                predicates.add(cb.equal(root.get("locgovCode"), code));
            }
            if (nm != null) {
                predicates.add(cb.like(root.get("locgovNm"), "%" + nm + "%"));
            }
            if (charger != null) {
                predicates.add(cb.like(root.get("chargerNm"), "%" + charger + "%"));
            }
            if (cttpc != null) {
                predicates.add(cb.like(root.get("chargerCttpc"), "%" + cttpc + "%"));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("frstRegistPnttm"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThan(root.get("frstRegistPnttm"), endDate));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return locgovRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), size, org.springframework.data.domain.Sort.by("locgovCode")));
    }

    public Locgov findOrThrow(String locgovCode) {
        return locgovRepository.findById(locgovCode)
                .orElseThrow(() -> new DonationException("존재하지 않는 지자체입니다: " + locgovCode));
    }

    /** 아직 담당자정보가 채워지지 않아 "등록" 대상으로 고를 수 있는 행 (등록 폼의 지자체
     *  선택 드롭다운용). */
    public List<Locgov> registrableLocgovs() {
        return locgovRepository.findAll().stream()
                .filter(l -> l.getBizrno() == null || l.getBizrno().isBlank())
                .sorted((a, b) -> {
                    int byUpper = safe(a.getUpperLocgovCode()).compareTo(safe(b.getUpperLocgovCode()));
                    return byUpper != 0 ? byUpper : safe(a.getLocgovCode()).compareTo(safe(b.getLocgovCode()));
                })
                .toList();
    }

    public LocgovImage imageOf(String locgovCode) {
        return locgovImageRepository.findById(locgovCode).orElse(null);
    }

    public List<LocgovDeptHist> deptHistOf(String locgovCode) {
        return locgovDeptHistRepository.findByLocgovCodeOrderByDeptHistNoDesc(locgovCode);
    }

    public List<LocgovLmtt> lmttOf(String locgovCode) {
        return locgovLmttRepository.findByLocgovCodeOrderByLmttBgnDeDesc(locgovCode);
    }

    public record LocgovForm(String bizrno, String chargerNm, String chargerCttpc, String chargerEmail,
                              String locgovHmpg, String locgovIntrcnCn, String locgovZip, String bassAdres,
                              String dtlAdres, Long locgovBudgetAmt, String locgovPopltnCo, String locgovAr,
                              String locgovSpcprd, String gcctUseAt, String etrcshUseAt, String achlqrSleAt,
                              String chargerPsitnDept, String processDeptCode, String administInsttCode,
                              String fisSp, String offcsNm) {
    }

    @Transactional
    public Locgov register(String locgovCode, LocgovForm form, MultipartFile offcsFile, MultipartFile addPcFile,
                            MultipartFile addMbFile, Long managerId) {
        Locgov locgov = findOrThrow(locgovCode);
        if (locgov.getBizrno() != null && !locgov.getBizrno().isBlank()) {
            throw new DonationException("DUP");
        }
        validate(form);
        applyForm(locgov, form);
        locgov.setFrstRegisterId(managerId);
        locgov.setFrstRegistPnttm(LocalDateTime.now());
        locgov.setLastUpdusrId(managerId);
        locgov.setLastUpdtPnttm(LocalDateTime.now());
        locgov.setUseAt("Y");
        locgov = locgovRepository.save(locgov);
        applySeal(locgovCode, form.offcsNm(), offcsFile);
        applyImages(locgovCode, addPcFile, addMbFile, managerId);
        return locgov;
    }

    @Transactional
    public Locgov update(String locgovCode, LocgovForm form, MultipartFile offcsFile, MultipartFile addPcFile,
                          MultipartFile addMbFile, Long managerId) {
        Locgov locgov = findOrThrow(locgovCode);
        validate(form);
        String prevDeptCode = locgov.getProcessDeptCode();
        applyForm(locgov, form);
        locgov.setLastUpdusrId(managerId);
        locgov.setLastUpdtPnttm(LocalDateTime.now());
        // 비활성화(USE_AT='N')된 지자체라도 담당자가 정보를 저장하면 다시 활성화된 것으로
        // 본다 - "비활성화" 버튼 외에 별도 "재활성화" 버튼을 두지 않은 의도적 단순화.
        locgov.setUseAt("Y");
        locgov = locgovRepository.save(locgov);

        if (form.processDeptCode() != null && !form.processDeptCode().equals(prevDeptCode)) {
            recordDeptHist(locgovCode, prevDeptCode, managerId);
        }

        applySeal(locgovCode, form.offcsNm(), offcsFile);
        applyImages(locgovCode, addPcFile, addMbFile, managerId);
        return locgov;
    }

    /** AS-IS는 물리 삭제 버튼을 이미 주석처리해뒀다(list.jsp) - USE_AT 비활성화로 대체. */
    @Transactional
    public void deactivate(String locgovCode, Long managerId) {
        Locgov locgov = findOrThrow(locgovCode);
        locgov.setUseAt("N");
        locgov.setLastUpdusrId(managerId);
        locgov.setLastUpdtPnttm(LocalDateTime.now());
        locgovRepository.save(locgov);
    }

    @Transactional
    public LocgovLmtt registerLmtt(String locgovCode, String lmttBgnDe, String lmttEndDe, String violtResnCode,
                                    String violtResnCn, String registerNm, Long managerId) {
        findOrThrow(locgovCode);
        LocgovLmtt lmtt = new LocgovLmtt();
        lmtt.setLocgovCode(locgovCode);
        lmtt.setLmttBgnDe(lmttBgnDe);
        lmtt.setLmttEndDe(lmttEndDe);
        lmtt.setVioltResnCode(violtResnCode);
        lmtt.setVioltResnCn(violtResnCn);
        lmtt.setRegisterNm(registerNm);
        lmtt.setFrstRegisterId(managerId);
        lmtt.setFrstRegistPnttm(LocalDateTime.now());
        lmtt.setLastUpdusrId(managerId);
        lmtt.setLastUpdtPnttm(LocalDateTime.now());
        return locgovLmttRepository.save(lmtt);
    }

    @Transactional
    public void deleteLmtt(String locgovCode, String lmttBgnDe, String lmttEndDe) {
        locgovLmttRepository.deleteById(new com.ghlove.donation.domain.LocgovLmttId(lmttBgnDe, lmttEndDe, locgovCode));
    }

    /** 직인/PC·모바일 배경이미지 관리자 미리보기용 - LocgovSealAdminController(폐기 대상)가
     *  하던 복호화 프리뷰를 여기로 흡수. */
    public byte[] sealPreview(String locgovCode) {
        return locgovSealService.decryptedSealBytes(locgovCode).orElse(null);
    }

    public byte[] imageBytes(String locgovCode, boolean pc) {
        LocgovImage image = imageOf(locgovCode);
        if (image == null) {
            return null;
        }
        String fileName = pc ? image.getPcFileName() : image.getMobileFileName();
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        try {
            return java.nio.file.Files.readAllBytes(fileStorageService.resolve(fileName, IMAGE_SUBDIR));
        } catch (java.io.IOException e) {
            return null;
        }
    }

    private void applySeal(String locgovCode, String offcsNm, MultipartFile offcsFile) {
        if (offcsFile != null && !offcsFile.isEmpty()) {
            locgovSealService.uploadSeal(locgovCode, offcsNm, offcsFile);
        } else if (offcsNm != null && !offcsNm.isBlank()) {
            // 파일 재업로드 없이 직인명만 바뀐 경우
            Locgov locgov = findOrThrow(locgovCode);
            locgov.setOffcsNm(offcsNm);
            locgovRepository.save(locgov);
        }
    }

    private void applyImages(String locgovCode, MultipartFile addPcFile, MultipartFile addMbFile, Long managerId) {
        if ((addPcFile == null || addPcFile.isEmpty()) && (addMbFile == null || addMbFile.isEmpty())) {
            return;
        }
        LocgovImage image = locgovImageRepository.findById(locgovCode).orElseGet(() -> {
            LocgovImage i = new LocgovImage();
            i.setLocgovCode(locgovCode);
            i.setFrstRegisterId(managerId);
            i.setFrstRegistPnttm(LocalDateTime.now());
            return i;
        });
        if (addPcFile != null && !addPcFile.isEmpty()) {
            deleteImageFileQuietly(image.getPcFileName());
            image.setPcFileName(fileStorageService.store(addPcFile, IMAGE_SUBDIR));
        }
        if (addMbFile != null && !addMbFile.isEmpty()) {
            deleteImageFileQuietly(image.getMobileFileName());
            image.setMobileFileName(fileStorageService.store(addMbFile, IMAGE_SUBDIR));
        }
        image.setLastUpdusrId(managerId);
        image.setLastUpdtPnttm(LocalDateTime.now());
        locgovImageRepository.save(image);
    }

    /** AS-IS "답례품 배경 이미지 삭제"(delete/itemFile) - PC/MOBILE 개별 삭제. */
    @Transactional
    public void deleteImage(String locgovCode, boolean pc, Long managerId) {
        LocgovImage image = locgovImageRepository.findById(locgovCode).orElse(null);
        if (image == null) {
            return;
        }
        if (pc) {
            deleteImageFileQuietly(image.getPcFileName());
            image.setPcFileName(null);
        } else {
            deleteImageFileQuietly(image.getMobileFileName());
            image.setMobileFileName(null);
        }
        image.setLastUpdusrId(managerId);
        image.setLastUpdtPnttm(LocalDateTime.now());
        locgovImageRepository.save(image);
    }

    private void deleteImageFileQuietly(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        try {
            java.nio.file.Files.deleteIfExists(fileStorageService.resolve(fileName, IMAGE_SUBDIR));
        } catch (java.io.IOException ignored) {
            // best-effort cleanup
        }
    }

    private void recordDeptHist(String locgovCode, String prevDeptCode, Long managerId) {
        int nextNo = locgovDeptHistRepository.countByLocgovCode(locgovCode) + 1;
        LocgovDeptHist hist = new LocgovDeptHist();
        hist.setLocgovCode(locgovCode);
        hist.setDeptHistNo(nextNo);
        hist.setProcessDeptCode(prevDeptCode);
        hist.setFrstRegisterId(managerId);
        hist.setFrstRegistPnttm(LocalDateTime.now());
        hist.setLastUpdusrId(managerId);
        hist.setLastUpdtPnttm(LocalDateTime.now());
        locgovDeptHistRepository.save(hist);
    }

    private void applyForm(Locgov locgov, LocgovForm form) {
        locgov.setBizrno(form.bizrno());
        locgov.setChargerNm(form.chargerNm());
        locgov.setChargerCttpc(form.chargerCttpc());
        locgov.setChargerEmail(form.chargerEmail());
        locgov.setLocgovHmpg(form.locgovHmpg());
        locgov.setLocgovIntrcnCn(form.locgovIntrcnCn());
        locgov.setLocgovZip(form.locgovZip());
        locgov.setBassAdres(form.bassAdres());
        locgov.setDtlAdres(form.dtlAdres());
        locgov.setLocgovBudgetAmt(form.locgovBudgetAmt());
        locgov.setLocgovPopltnCo(form.locgovPopltnCo());
        locgov.setLocgovAr(form.locgovAr());
        locgov.setLocgovSpcprd(form.locgovSpcprd());
        locgov.setGcctUseAt(form.gcctUseAt());
        locgov.setEtrcshUseAt(form.etrcshUseAt());
        locgov.setAchlqrSleAt(form.achlqrSleAt());
        locgov.setChargerPsitnDept(form.chargerPsitnDept());
        locgov.setProcessDeptCode(form.processDeptCode());
        locgov.setAdministInsttCode(form.administInsttCode());
        locgov.setFisSp(form.fisSp());
        if (form.offcsNm() != null && !form.offcsNm().isBlank()) {
            locgov.setOffcsNm(form.offcsNm());
        }
    }

    private void validate(LocgovForm form) {
        requireNotBlank(form.bizrno(), "사업자 등록번호를 입력해 주세요.");
        requireNotBlank(form.chargerNm(), "담당자명을 입력해 주세요.");
        requireNotBlank(form.chargerEmail(), "담당자 이메일을 입력해 주세요.");
        requireNotBlank(form.chargerCttpc(), "담당자 연락처를 입력해 주세요.");
        requireNotBlank(form.locgovHmpg(), "지자체 홈페이지를 입력해 주세요.");
        requireNotBlank(form.locgovIntrcnCn(), "지자체 소개를 입력해 주세요.");
        requireNotBlank(form.locgovZip(), "지자체 주소(우편번호)를 입력해 주세요.");
        requireNotBlank(form.bassAdres(), "지자체 주소를 입력해 주세요.");
        if (form.locgovBudgetAmt() == null) {
            throw new DonationException("예산을 입력해 주세요.");
        }
        requireNotBlank(form.locgovPopltnCo(), "인구수를 입력해 주세요.");
        requireNotBlank(form.chargerPsitnDept(), "부서명을 입력해 주세요.");
        requireNotBlank(form.processDeptCode(), "부서코드를 입력해 주세요.");
        if (form.processDeptCode() != null && form.processDeptCode().length() != 11) {
            throw new DonationException("부서코드는 11자리로 입력해 주세요.");
        }
        requireNotBlank(form.administInsttCode(), "행정표준기관코드를 입력해 주세요.");
        requireNotBlank(form.fisSp(), "회계구분을 입력해 주세요.");
    }

    private static void requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new DonationException(message);
        }
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String safe(String s) {
        return Objects.requireNonNullElse(s, "");
    }
}
