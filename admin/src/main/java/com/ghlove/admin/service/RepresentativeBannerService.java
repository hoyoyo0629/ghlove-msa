package com.ghlove.admin.service;

import com.ghlove.admin.domain.RepresentativeBanner;
import com.ghlove.admin.repository.RepresentativeBannerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 지정기부 대표배너 관리 (AS-IS opmanager/designated-donation/banner). PC(1280x400)/
 * 모바일(768x280) 이미지가 분리되어 있고, 최대 15슬롯까지 노출순서로 관리한다
 * (AS-IS는 순서 중복도 검증했다). PROCESS_TYPE은 이 프로젝트에서 지정기부 전용으로만
 * 고정한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentativeBannerService {

    private static final String PROCESS_TYPE = "DESIGNATED_DONATION";
    /** 답례품관리 > 대표배너관리(AS-IS opmanager/representative-banner)의 PROCESS_TYPE. */
    public static final String PROCESS_TYPE_ITEM = "ITEM";
    /** AS-IS RepresentativeBanner 권장사이즈 - 업로드한 이미지는 이 크기로 다시 맞춰 저장된다(ThumbUtils.create). */
    private static final int PC_WIDTH = 1280;
    private static final int PC_HEIGHT = 400;
    private static final int MOBILE_WIDTH = 768;
    private static final int MOBILE_HEIGHT = 280;
    /** AS-IS 화면 validator와 같은 한도 - 20MB. */
    private static final long MAX_SIZE = 20L * 1024 * 1024;
    /** AS-IS 화면 validator의 허용 확장자 목록 그대로(pdf가 섞여 있는 것까지 동일). */
    private static final java.util.Set<String> ALLOWED_EXT =
            java.util.Set.of("jpg", "gif", "png", "pdf", "jpeg");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepresentativeBannerRepository repository;

    @Value("${admin.upload.dir}")
    private String uploadDir;

    public List<RepresentativeBanner> list() {
        return list(PROCESS_TYPE);
    }

    /** 같은 G_REPRST_BANNER를 PROCESS_TYPE별로 나눠 본다 - DESIGNATED_DONATION(지정기부) / ITEM(답례품 대표배너). */
    public List<RepresentativeBanner> list(String processType) {
        return repository.findByProcessTypeOrderByDisplayOrderAsc(processType);
    }

    public RepresentativeBanner get(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RepresentativeBannerException("배너를 찾을 수 없습니다."));
    }

    /**
     * AS-IS {@code editRepresentativeBanner}(id==0 분기) + {@code insertRepresentativeBanner}.
     *
     * <p>AS-IS는 <b>노출순서를 폼에서 받지 않는다</b> - INSERT문이
     * {@code (SELECT NVL(MAX(DISPLAY_ORDER),0)+1 FROM G_REPRST_BANNER)}로 직접 채운다.
     * 즉 맨 뒤에 붙고, 순서는 목록 화면의 select로 바꾼다.
     * (AS-IS SQL이 PROCESS_TYPE을 안 걸고 테이블 전체의 MAX를 쓰는 것까지 그대로 옮긴다.)
     */
    @Transactional
    public RepresentativeBanner create(String title, String linkUrl, String bannerContent, String useYn,
                                        MultipartFile pcImage, MultipartFile mobileImage, Long managerId) {
        return create(PROCESS_TYPE, title, linkUrl, bannerContent, useYn, pcImage, mobileImage, managerId);
    }

    @Transactional
    public RepresentativeBanner create(String processType, String title, String linkUrl, String bannerContent, String useYn,
                                        MultipartFile pcImage, MultipartFile mobileImage, Long managerId) {
        validateTitle(title);
        RepresentativeBanner banner = new RepresentativeBanner();
        banner.setTitle(title);
        banner.setLinkUrl(linkUrl);
        banner.setBannerContent(bannerContent);
        banner.setDisplayOrder(repository.findMaxDisplayOrder() + 1);
        banner.setUseYn(normalizeUseYn(useYn));
        banner.setProcessType(processType);
        // AS-IS는 파일이 없으면 빈 문자열을 넣는다(검증은 화면 validator가 한다).
        banner.setFileNamePc(hasFile(pcImage) ? store(pcImage, PC_WIDTH, PC_HEIGHT) : "");
        banner.setFileNameMobile(hasFile(mobileImage) ? store(mobileImage, MOBILE_WIDTH, MOBILE_HEIGHT) : "");
        banner.setFrstRegisterId(managerId);
        banner.setFrstRegistPnttm(LocalDateTime.now());
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    /**
     * AS-IS {@code editRepresentativeBanner}(id>0 분기) + {@code updateRepresentativeBanner}.
     *
     * <p>AS-IS UPDATE문은 TITLE·LINK_URL·USE_YN은 <b>항상</b> 쓰고,
     * FILE_NAME_PC·FILE_NAME_MOBILE·BANNER_CONTENT는 {@code <if test="... != ''">}로
     * <b>값이 있을 때만</b> 쓴다. 즉 파일을 다시 첨부하지 않거나 배너 설명을 비워 보내면
     * 기존 값이 그대로 남는다. 노출순서는 건드리지 않는다.
     */
    @Transactional
    public RepresentativeBanner update(Integer id, String title, String linkUrl, String bannerContent,
                                        String useYn, MultipartFile pcImage, MultipartFile mobileImage,
                                        Long managerId) {
        validateTitle(title);
        RepresentativeBanner banner = get(id);
        banner.setTitle(title);
        banner.setLinkUrl(linkUrl);
        banner.setUseYn(normalizeUseYn(useYn));
        if (bannerContent != null && !bannerContent.isEmpty()) {
            banner.setBannerContent(bannerContent);
        }
        if (hasFile(pcImage)) {
            banner.setFileNamePc(store(pcImage, PC_WIDTH, PC_HEIGHT));
        }
        if (hasFile(mobileImage)) {
            banner.setFileNameMobile(store(mobileImage, MOBILE_WIDTH, MOBILE_HEIGHT));
        }
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    private static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /** AS-IS 폼은 라디오라 Y/N만 온다 - 그 외 값은 'Y'로 본다(라디오 기본 checked가 사용). */
    private static String normalizeUseYn(String useYn) {
        return "N".equals(useYn) ? "N" : "Y";
    }

    /**
     * AS-IS {@code /banner/change-ordering} + {@code updateRepresentativeBanneOrdering} -
     * 목록에서 노출순서를 한꺼번에 바꾼다.
     *
     * <p>AS-IS는 {@code id[]}와 {@code ordering[]} <b>두 배열을 같은 인덱스로 짝지어</b> 돌며
     * 배너별 {@code DISPLAY_ORDER}를 UPDATE한다. 길이가 다르면 AS-IS는
     * {@code ArrayIndexOutOfBoundsException}으로 터지므로 짝이 맞는 만큼만 처리한다.
     */
    @Transactional
    public int changeOrdering(List<Integer> ids, List<Integer> orderings, Long managerId) {
        if (ids == null || orderings == null || ids.isEmpty()) {
            return 0;
        }
        int changed = 0;
        int size = Math.min(ids.size(), orderings.size());
        for (int i = 0; i < size; i++) {
            Integer id = ids.get(i);
            Integer ordering = orderings.get(i);
            if (id == null || ordering == null) {
                continue;
            }
            RepresentativeBanner banner = repository.findById(id).orElse(null);
            if (banner == null) {
                continue;
            }
            banner.setDisplayOrder(ordering);
            banner.setLastUpdusrId(managerId);
            banner.setLastUpdtPnttm(LocalDateTime.now());
            repository.save(banner);
            changed++;
        }
        return changed;
    }

    /**
     * AS-IS {@code /banner/delete} + {@code deleteRepresentativeBanner} -
     * <b>이름은 "삭제"지만 배너 행을 지우지 않는다</b>. {@code deleteFlag}가 가리키는
     * <b>이미지 파일명만 비운다</b>({@code pc} → {@code FILE_NAME_PC=''},
     * {@code mobile} → {@code FILE_NAME_MOBILE=''}). 배너 자체를 내리는 수단은 사용여부 전환이다.
     *
     * <p>AS-IS는 둘 중 어느 값도 아니면 수정자·수정일시만 바꾸고 끝난다 - 그 동작도 그대로 둔다.
     * 디스크 파일은 AS-IS도 남긴다(컬럼만 비운다).
     */
    @Transactional
    public RepresentativeBanner clearImage(Integer id, String deleteFlag, Long managerId) {
        RepresentativeBanner banner = get(id);
        if ("pc".equals(deleteFlag)) {
            banner.setFileNamePc("");
        } else if ("mobile".equals(deleteFlag)) {
            banner.setFileNameMobile("");
        }
        banner.setLastUpdusrId(managerId);
        banner.setLastUpdtPnttm(LocalDateTime.now());
        return repository.save(banner);
    }

    /**
     * AS-IS 화면 validator가 보는 것 중 서버에서도 지켜야 하는 것만 남긴다 - 배너명 필수.
     * 노출순서는 폼에 없으므로 검증 대상이 아니고(순서 중복은 목록 화면 저장에서 막는다),
     * PC/모바일 이미지 2개 필수는 AS-IS가 화면 validator에서만 본다.
     */
    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new RepresentativeBannerException("배너명을 입력해 주세요.");
        }
    }

    /**
     * AS-IS {@code uploadFile} + {@code ThumbUtils.create(file, dest, width, height)} - 저장 전에 이미지를 권장사이즈(PC 1280x400 /
     * 모바일 768x280)로 <b>비율과 상관없이 맞춘다</b>(스케일 후 RGB로 저장, 투명 PNG는 검게 나온다). 읽을 수 없는 파일(pdf 등)은 원본 그대로 둔다.
     */
    private static byte[] fitToSize(MultipartFile file, String ext, int width, int height) throws IOException {
        java.awt.image.BufferedImage src = javax.imageio.ImageIO.read(file.getInputStream());
        if (src == null) {
            return file.getBytes();
        }
        java.awt.Image scaled = src.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
        java.awt.image.BufferedImage dest = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics2D g = dest.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        if (!javax.imageio.ImageIO.write(dest, ext, out)) {
            return file.getBytes();
        }
        return out.toByteArray();
    }

    private String store(MultipartFile file, int width, int height) {
        if (file.getSize() > MAX_SIZE) {
            throw new RepresentativeBannerException("첨부파일 사이즈는 20MB 이내로 등록 가능합니다. ");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        if (!ALLOWED_EXT.contains(ext.toLowerCase().replace(".", ""))) {
            throw new RepresentativeBannerException("등록할 수 없는 파일확장자입니다.");
        }
        String storedName = UUID.randomUUID() + ext;
        try {
            Path dir = Paths.get(uploadDir, "rep-banner");
            Files.createDirectories(dir);
            Path target = dir.resolve(storedName);
            Files.write(target, fitToSize(file, ext.toLowerCase().replace(".", ""), width, height));
        } catch (IOException e) {
            log.error("Failed to store banner image", e);
            throw new RepresentativeBannerException("이미지 저장에 실패했습니다.");
        }
        return "/uploads/rep-banner/" + storedName;
    }
}
