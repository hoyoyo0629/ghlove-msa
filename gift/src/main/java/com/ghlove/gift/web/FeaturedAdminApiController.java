package com.ghlove.gift.web;

import com.ghlove.gift.domain.Featured;
import com.ghlove.gift.domain.FeaturedItem;
import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.repository.FeaturedItemRepository;
import com.ghlove.gift.repository.FeaturedRepository;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 기획전/이벤트 관리 (AS-IS opmanager/featured, featured-mobile - FeaturedManagerController)
 *  cross-service API - admin 콘솔(/admin/featured)이 사용한다. AS-IS는 PC/모바일을 별도
 *  URL(featured, featured-mobile)로 나누지만 이 프로젝트는 FEATURED_TYPE 파라미터 하나로
 *  통합한다(list와 select box 필드로 구분, memory give-statistics-scope 등과 동일한
 *  "여러 화면을 하나로" 관행). MainDisplayAdminClient와 같은 패턴으로 기획전에 딸린
 *  답례품 목록은 항상 통째로 교체(replace)한다. */
@RestController
@RequestMapping("/api/admin/featured")
@RequiredArgsConstructor
public class FeaturedAdminApiController {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final FeaturedRepository featuredRepository;
    private final FeaturedItemRepository featuredItemRepository;
    private final GiftRepository giftRepository;
    private final FileStorageService fileStorageService;

    @GetMapping
    public List<Featured> list(@RequestParam(defaultValue = "1") String featuredType,
                                @RequestParam(required = false) String featuredName) {
        if (featuredName != null && !featuredName.isBlank()) {
            return featuredRepository.findByFeaturedTypeAndFeaturedNameContainingOrderByFeaturedIdDesc(featuredType, featuredName);
        }
        return featuredRepository.findByFeaturedTypeOrderByFeaturedIdDesc(featuredType);
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(@PathVariable Integer id) {
        Featured featured = featuredRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기획전을 찾을 수 없습니다."));
        List<FeaturedItem> items = featuredItemRepository.findByFeaturedIdOrderByDisplayOrder(id);
        Map<Integer, String> itemNames = giftRepository.findAllById(
                        items.stream().map(i -> i.getItemId().longValue()).toList()).stream()
                .collect(Collectors.toMap(g -> g.getItemId().intValue(), Gift::getItemName));
        List<Map<String, Object>> itemRows = items.stream()
                .map(i -> Map.<String, Object>of(
                        "itemId", i.getItemId(),
                        "itemName", itemNames.getOrDefault(i.getItemId(), "(삭제된 상품)"),
                        "displayOrder", i.getDisplayOrder()))
                .toList();
        return Map.of("featured", featured, "items", itemRows);
    }

    @PostMapping
    public Featured create(@RequestParam Integer featuredClass, @RequestParam String featuredType,
                            @RequestParam String featuredUrl, @RequestParam String featuredCode,
                            @RequestParam String featuredName, @RequestParam String featuredSimpleContent,
                            @RequestParam String featuredContent, @RequestParam(required = false) String link,
                            @RequestParam(required = false) String featuredFlag,
                            @RequestParam(required = false) String displayListFlag,
                            @RequestParam(required = false) Integer ordering,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            @RequestParam(required = false) String locgovCode,
                            @RequestParam(required = false) MultipartFile image,
                            @RequestParam(required = false) MultipartFile thumbnailImage) {
        Featured featured = new Featured();
        applyForm(featured, featuredClass, featuredType, featuredUrl, featuredCode, featuredName,
                featuredSimpleContent, featuredContent, link, featuredFlag, displayListFlag, ordering,
                startDate, endDate, locgovCode);
        featured.setCreatedDate(LocalDateTime.now().format(TS));
        if (image != null && !image.isEmpty()) {
            featured.setFeaturedImage(fileStorageService.store(image));
        }
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            featured.setThumbnailImage(fileStorageService.store(thumbnailImage));
        }
        return featuredRepository.save(featured);
    }

    @PutMapping("/{id}")
    public Featured update(@PathVariable Integer id, @RequestParam Integer featuredClass, @RequestParam String featuredType,
                            @RequestParam String featuredUrl, @RequestParam String featuredCode,
                            @RequestParam String featuredName, @RequestParam String featuredSimpleContent,
                            @RequestParam String featuredContent, @RequestParam(required = false) String link,
                            @RequestParam(required = false) String featuredFlag,
                            @RequestParam(required = false) String displayListFlag,
                            @RequestParam(required = false) Integer ordering,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            @RequestParam(required = false) String locgovCode,
                            @RequestParam(required = false) MultipartFile image,
                            @RequestParam(required = false) MultipartFile thumbnailImage) {
        Featured featured = featuredRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "기획전을 찾을 수 없습니다."));
        applyForm(featured, featuredClass, featuredType, featuredUrl, featuredCode, featuredName,
                featuredSimpleContent, featuredContent, link, featuredFlag, displayListFlag, ordering,
                startDate, endDate, locgovCode);
        if (image != null && !image.isEmpty()) {
            featured.setFeaturedImage(fileStorageService.store(image));
        }
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            featured.setThumbnailImage(fileStorageService.store(thumbnailImage));
        }
        return featuredRepository.save(featured);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Integer id) {
        featuredItemRepository.deleteByFeaturedId(id);
        featuredRepository.deleteById(id);
    }

    @PutMapping("/{id}/items")
    @Transactional
    public void replaceItems(@PathVariable Integer id, @RequestParam(required = false) List<Integer> itemIds) {
        if (!featuredRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "기획전을 찾을 수 없습니다.");
        }
        featuredItemRepository.deleteByFeaturedId(id);
        if (itemIds == null) {
            return;
        }
        String now = LocalDateTime.now().format(TS);
        int order = 1;
        for (Integer itemId : itemIds) {
            if (!giftRepository.existsById(itemId.longValue())) {
                continue;
            }
            FeaturedItem row = new FeaturedItem();
            row.setFeaturedId(id);
            row.setItemId(itemId);
            row.setDisplayOrder(order++);
            row.setCreatedDate(now);
            featuredItemRepository.save(row);
        }
    }

    private void applyForm(Featured featured, Integer featuredClass, String featuredType, String featuredUrl,
                            String featuredCode, String featuredName, String featuredSimpleContent,
                            String featuredContent, String link, String featuredFlag, String displayListFlag,
                            Integer ordering, String startDate, String endDate, String locgovCode) {
        featured.setFeaturedClass(featuredClass);
        featured.setFeaturedType(featuredType);
        featured.setFeaturedUrl(featuredUrl);
        featured.setFeaturedCode(featuredCode);
        featured.setFeaturedName(featuredName);
        featured.setFeaturedSimpleContent(featuredSimpleContent);
        featured.setFeaturedContent(featuredContent);
        featured.setLink(link != null ? link : "");
        featured.setFeaturedFlag("Y".equals(featuredFlag) ? "Y" : "N");
        featured.setDisplayListFlag("Y".equals(displayListFlag) ? "Y" : "N");
        featured.setOrdering(ordering != null ? ordering : 0);
        featured.setStartDate(startDate);
        featured.setEndDate(endDate);
        featured.setLocgovCode(locgovCode);
    }
}
