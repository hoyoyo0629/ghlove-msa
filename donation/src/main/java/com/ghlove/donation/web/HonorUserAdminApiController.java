package com.ghlove.donation.web;

import com.ghlove.donation.service.HonorUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 기부혜택증 관리 API (<b>admin 콘솔 전용</b>) - admin 메뉴 19101 기부혜택증 설정 관리 /
 * 19102 기부혜택증 설정 / 19103 기부혜택증 열람현황이 호출한다.
 *
 * <p>기부혜택증 설정·메인이미지 설명·열람이력이 모두 donation 소유라서 화면은 admin에 두고
 * 데이터는 여기서 다룬다(지자체관리 {@code /api/locgov-admin}과 같은 관행). 브라우저에 직접
 * 노출되지 않고 admin 콘솔의 OP_MANAGER 로그인 + 메뉴RBAC이 실제 게이트다.
 */
@RestController
@RequestMapping("/api/admin/honor-users")
@RequiredArgsConstructor
public class HonorUserAdminApiController {

    private final HonorUserAdminService honorUserAdminService;

    /** 19101 설정 목록. */
    @GetMapping
    public List<HonorUserAdminService.SettingRow> list(
            @RequestParam(required = false) String upperLocgovCode,
            @RequestParam(required = false) String lclgvCd) {
        return honorUserAdminService.settingList(upperLocgovCode, lclgvCd);
    }

    /** 19102 설정 상세 + 이미지 설명. 설정 행이 없으면 setting이 null이다. */
    @GetMapping("/{lclgvCd}")
    public Map<String, Object> detail(@PathVariable String lclgvCd) {
        return Map.of(
                "setting", java.util.Optional.ofNullable(honorUserAdminService.setting(lclgvCd)),
                "imageExplains", honorUserAdminService.imageExplains(lclgvCd));
    }

    /** 19102 설정 저장(멀티파트 - 메인이미지 여러 장 중 첫 장을 대표로 쓴다. AS-IS 동일). */
    @PostMapping(value = "/{lclgvCd}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> save(@PathVariable String lclgvCd,
                                  @RequestParam(required = false) Integer brnzGrdDntnAmt,
                                  @RequestParam(required = false) String hnrUserStngTtl,
                                  @RequestParam(required = false) String hnrUserRwrd,
                                  @RequestParam(required = false) String hnrUserSlctnSeCd,
                                  @RequestParam(required = false) String useYn,
                                  @RequestParam(required = false) List<String> imageExplains,
                                  @RequestParam(required = false) List<MultipartFile> images,
                                  @RequestParam(required = false) Long managerId) {
        honorUserAdminService.saveSetting(lclgvCd, brnzGrdDntnAmt, hnrUserStngTtl, hnrUserRwrd,
                hnrUserSlctnSeCd, useYn, imageExplains, images, managerId);
        return ResponseEntity.ok(Map.of("updated", 1));
    }

    /** 19102 대표이미지 삭제 - AS-IS deleteItemFile. */
    @PostMapping("/{lclgvCd}/main-image/delete")
    public Map<String, Integer> deleteMainImage(@PathVariable String lclgvCd) {
        return Map.of("deleted", honorUserAdminService.deleteMainImage(lclgvCd));
    }

    /** 대표이미지 바이트. */
    @GetMapping("/{lclgvCd}/main-image")
    public ResponseEntity<byte[]> mainImage(@PathVariable String lclgvCd) {
        byte[] bytes = honorUserAdminService.mainImageBytes(lclgvCd);
        return bytes != null ? ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(bytes)
                : ResponseEntity.notFound().build();
    }

    /** 19103 열람현황 - 사용자명은 admin이 member에서 채운다(AS-IS는 한 DB라 조인했다). */
    @GetMapping("/view-hist")
    public List<HonorUserAdminService.ViewHistRow> viewHist(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String upperLocgovCode,
            @RequestParam(required = false) String lclgvCd) {
        return honorUserAdminService.viewHist(startDate, endDate, upperLocgovCode, lclgvCd);
    }
}
