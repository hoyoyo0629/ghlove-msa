package com.ghlove.gift.web;

import com.ghlove.gift.domain.CommonCode;
import com.ghlove.gift.domain.GiftSubcategory;
import com.ghlove.gift.domain.GiftSubcategoryItem;
import com.ghlove.gift.repository.CommonCodeRepository;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.GiftSubcategoryItemRepository;
import com.ghlove.gift.repository.GiftSubcategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 답례품 카테고리 관리 (AS-IS opmanager/categories - CategoriesManagerController) cross-service
 * API - admin 콘솔(/admin/gift-categories)이 사용한다. 답례품몰 GNB "전체 카테고리" 메가메뉴가
 * 실제로 읽는 3단계 구조(대분류=GIFT_CATEGORY 공통코드 → 중분류=GIFT_SUBCATEGORY →
 * 개별품목=GIFT_SUBCATEGORY_ITEM, GiftService#categoryTree() 참고)를 그대로 CRUD한다.
 *
 * 원본 schema.sql의 OP_CATEGORY/OP_ITEM_CATEGORY도 gift DB에 테이블 자체는 이미 존재하지만
 * (이전 라운드에서 이관됨) 실제 스토어프론트 카테고리 필터링/GNB 어디에서도 참조되지 않는
 * 완전히 분리된 죽은 테이블이라 - 이 라운드에서는 "실제로 스토어프론트에 반영되는" GIFT_CATEGORY
 * 계열을 관리 대상으로 택했다(OP_CATEGORY까지 활성화하려면 GiftService 필터링 파이프라인
 * 자체를 다시 짜야 해서 이번 라운드 범위를 벗어남 + 병렬 작업 중인 공용 파일 변경 최소화 원칙).
 */
@RestController
@RequestMapping("/api/admin/gift-categories")
@RequiredArgsConstructor
public class CategoryAdminApiController {

    private static final String CODE_TYPE = "GIFT_CATEGORY";
    private static final String LANG = "ko";

    private final CommonCodeRepository commonCodeRepository;
    private final GiftSubcategoryRepository subcategoryRepository;
    private final GiftSubcategoryItemRepository subcategoryItemRepository;
    private final GiftRepository giftRepository;

    // ---------------------------------------------------------------- 트리 조회

    @GetMapping("/tree")
    public List<MajorDto> tree() {
        List<GiftSubcategoryItem> allItems = subcategoryItemRepository.findAllByOrderBySubcategoryIdAscOrderingAsc();
        Map<Long, List<ItemDto>> itemsBySub = allItems.stream()
                .collect(Collectors.groupingBy(GiftSubcategoryItem::getSubcategoryId, LinkedHashMap::new,
                        Collectors.mapping(this::toItemDto, Collectors.toList())));

        List<GiftSubcategory> allSubs = subcategoryRepository.findAllByOrderByCategoryCodeAscOrderingAsc();
        Map<String, List<SubcategoryDto>> subsByCode = allSubs.stream()
                .collect(Collectors.groupingBy(GiftSubcategory::getCategoryCode, LinkedHashMap::new,
                        Collectors.mapping(s -> toSubcategoryDto(s, itemsBySub.getOrDefault(s.getSubcategoryId(), List.of())),
                                Collectors.toList())));

        return commonCodeRepository.findByCodeTypeAndLanguageOrderByOrdering(CODE_TYPE, LANG).stream()
                .map(c -> new MajorDto(c.getId(), c.getLabel(), c.getOrdering(), c.getUseYn(),
                        subsByCode.getOrDefault(c.getId(), List.of())))
                .toList();
    }

    // ---------------------------------------------------------------- 대분류(GIFT_CATEGORY)

    @GetMapping("/majors")
    public List<MajorDto> majors() {
        return commonCodeRepository.findByCodeTypeAndLanguageOrderByOrdering(CODE_TYPE, LANG).stream()
                .map(c -> new MajorDto(c.getId(), c.getLabel(), c.getOrdering(), c.getUseYn(), List.of()))
                .toList();
    }

    @GetMapping("/majors/{id}")
    public MajorDto major(@PathVariable String id) {
        CommonCode c = findCommonCode(id);
        return new MajorDto(c.getId(), c.getLabel(), c.getOrdering(), c.getUseYn(), List.of());
    }

    @GetMapping("/majors/exists")
    public Map<String, Boolean> majorExists(@RequestParam String id) {
        return Map.of("exists", commonCodeRepository.existsById(new com.ghlove.gift.domain.CommonCodeId(CODE_TYPE, LANG, id)));
    }

    @PostMapping("/majors")
    public MajorDto createMajor(@RequestBody MajorForm form) {
        if (form.id() == null || form.id().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "카테고리코드는 필수입니다.");
        }
        var idKey = new com.ghlove.gift.domain.CommonCodeId(CODE_TYPE, LANG, form.id());
        if (commonCodeRepository.existsById(idKey)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 카테고리코드입니다.");
        }
        int nextOrdering = commonCodeRepository.findByCodeTypeAndLanguageOrderByOrdering(CODE_TYPE, LANG).stream()
                .mapToInt(c -> c.getOrdering() == null ? 0 : c.getOrdering()).max().orElse(-1) + 1;
        CommonCode c = new CommonCode();
        c.setCodeType(CODE_TYPE);
        c.setLanguage(LANG);
        c.setId(form.id());
        c.setLabel(form.label());
        c.setOrdering(form.ordering() != null ? form.ordering() : nextOrdering);
        c.setUseYn("Y");
        commonCodeRepository.save(c);
        return new MajorDto(c.getId(), c.getLabel(), c.getOrdering(), c.getUseYn(), List.of());
    }

    @PutMapping("/majors/{id}")
    public MajorDto updateMajor(@PathVariable String id, @RequestBody MajorForm form) {
        CommonCode c = findCommonCode(id);
        c.setLabel(form.label());
        if (form.ordering() != null) {
            c.setOrdering(form.ordering());
        }
        if (form.useYn() != null) {
            c.setUseYn(form.useYn());
        }
        commonCodeRepository.save(c);
        return new MajorDto(c.getId(), c.getLabel(), c.getOrdering(), c.getUseYn(), List.of());
    }

    @PostMapping("/majors/{id}/delete")
    public void deleteMajor(@PathVariable String id) {
        if (giftRepository.existsByCategoryCode(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이 카테고리를 사용 중인 답례품이 있어 삭제할 수 없습니다.");
        }
        if (!subcategoryRepository.findByCategoryCodeOrderByOrderingAsc(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "하위 중분류가 남아있어 삭제할 수 없습니다. 먼저 하위 중분류를 삭제하세요.");
        }
        commonCodeRepository.deleteById(new com.ghlove.gift.domain.CommonCodeId(CODE_TYPE, LANG, id));
    }

    @PostMapping("/majors/{id}/move")
    public void moveMajor(@PathVariable String id, @RequestParam String direction) {
        List<CommonCode> ordered = commonCodeRepository.findByCodeTypeAndLanguageOrderByOrdering(CODE_TYPE, LANG);
        swapOrdering(ordered, c -> c.getId().equals(id), CommonCode::getOrdering, CommonCode::setOrdering, direction);
        commonCodeRepository.saveAll(ordered);
    }

    private CommonCode findCommonCode(String id) {
        return commonCodeRepository.findById(new com.ghlove.gift.domain.CommonCodeId(CODE_TYPE, LANG, id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리를 찾을 수 없습니다: " + id));
    }

    // ---------------------------------------------------------------- 중분류(GIFT_SUBCATEGORY)

    @GetMapping("/subcategories/{id}")
    public SubcategoryDto subcategory(@PathVariable Long id) {
        return toSubcategoryDto(findSubcategory(id),
                subcategoryItemRepository.findBySubcategoryIdOrderByOrderingAsc(id).stream()
                        .map(this::toItemDto).toList());
    }

    @PostMapping("/subcategories")
    public SubcategoryDto createSubcategory(@RequestBody SubcategoryForm form) {
        if (form.categoryCode() == null || form.name() == null || form.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대분류와 중분류명은 필수입니다.");
        }
        findCommonCode(form.categoryCode());
        int nextOrdering = subcategoryRepository.findByCategoryCodeOrderByOrderingAsc(form.categoryCode()).stream()
                .mapToInt(s -> s.getOrdering() == null ? 0 : s.getOrdering()).max().orElse(0) + 1;
        GiftSubcategory s = new GiftSubcategory();
        s.setCategoryCode(form.categoryCode());
        s.setName(form.name());
        s.setOrdering(nextOrdering);
        s.setMetaTitle(form.metaTitle());
        s.setMetaKeywords(form.metaKeywords());
        s.setMetaDescription(form.metaDescription());
        subcategoryRepository.save(s);
        return toSubcategoryDto(s, List.of());
    }

    @PutMapping("/subcategories/{id}")
    public SubcategoryDto updateSubcategory(@PathVariable Long id, @RequestBody SubcategoryForm form) {
        GiftSubcategory s = findSubcategory(id);
        if (form.categoryCode() != null && !form.categoryCode().equals(s.getCategoryCode())) {
            findCommonCode(form.categoryCode());
            int nextOrdering = subcategoryRepository.findByCategoryCodeOrderByOrderingAsc(form.categoryCode()).stream()
                    .mapToInt(x -> x.getOrdering() == null ? 0 : x.getOrdering()).max().orElse(0) + 1;
            s.setCategoryCode(form.categoryCode());
            s.setOrdering(nextOrdering);
        }
        s.setName(form.name());
        s.setMetaTitle(form.metaTitle());
        s.setMetaKeywords(form.metaKeywords());
        s.setMetaDescription(form.metaDescription());
        subcategoryRepository.save(s);
        return toSubcategoryDto(s, subcategoryItemRepository.findBySubcategoryIdOrderByOrderingAsc(id).stream()
                .map(this::toItemDto).toList());
    }

    @PostMapping("/subcategories/{id}/delete")
    public void deleteSubcategory(@PathVariable Long id) {
        List<GiftSubcategoryItem> items = subcategoryItemRepository.findBySubcategoryIdOrderByOrderingAsc(id);
        subcategoryItemRepository.deleteAll(items);
        subcategoryRepository.deleteById(id);
    }

    @PostMapping("/subcategories/{id}/move")
    public void moveSubcategory(@PathVariable Long id, @RequestParam String direction) {
        GiftSubcategory target = findSubcategory(id);
        List<GiftSubcategory> siblings = subcategoryRepository.findByCategoryCodeOrderByOrderingAsc(target.getCategoryCode());
        swapOrdering(siblings, s -> s.getSubcategoryId().equals(id), GiftSubcategory::getOrdering, GiftSubcategory::setOrdering, direction);
        subcategoryRepository.saveAll(siblings);
    }

    private GiftSubcategory findSubcategory(Long id) {
        return subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "중분류를 찾을 수 없습니다: " + id));
    }

    // ---------------------------------------------------------------- 개별품목(GIFT_SUBCATEGORY_ITEM)

    @GetMapping("/items/{id}")
    public ItemDto item(@PathVariable Long id) {
        return toItemDto(findItem(id));
    }

    @PostMapping("/items")
    public ItemDto createItem(@RequestBody ItemForm form) {
        if (form.subcategoryId() == null || form.name() == null || form.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "중분류와 품목명은 필수입니다.");
        }
        findSubcategory(form.subcategoryId());
        int nextOrdering = subcategoryItemRepository.findBySubcategoryIdOrderByOrderingAsc(form.subcategoryId()).stream()
                .mapToInt(i -> i.getOrdering() == null ? 0 : i.getOrdering()).max().orElse(0) + 1;
        GiftSubcategoryItem item = new GiftSubcategoryItem();
        item.setSubcategoryId(form.subcategoryId());
        item.setName(form.name());
        item.setOrdering(nextOrdering);
        subcategoryItemRepository.save(item);
        return toItemDto(item);
    }

    @PutMapping("/items/{id}")
    public ItemDto updateItem(@PathVariable Long id, @RequestBody ItemForm form) {
        GiftSubcategoryItem item = findItem(id);
        item.setName(form.name());
        subcategoryItemRepository.save(item);
        return toItemDto(item);
    }

    @PostMapping("/items/{id}/delete")
    public void deleteItem(@PathVariable Long id) {
        subcategoryItemRepository.deleteById(id);
    }

    @PostMapping("/items/{id}/move")
    public void moveItem(@PathVariable Long id, @RequestParam String direction) {
        GiftSubcategoryItem target = findItem(id);
        List<GiftSubcategoryItem> siblings = subcategoryItemRepository.findBySubcategoryIdOrderByOrderingAsc(target.getSubcategoryId());
        swapOrdering(siblings, i -> i.getSubcategoryItemId().equals(id), GiftSubcategoryItem::getOrdering, GiftSubcategoryItem::setOrdering, direction);
        subcategoryItemRepository.saveAll(siblings);
    }

    private GiftSubcategoryItem findItem(Long id) {
        return subcategoryItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "개별품목을 찾을 수 없습니다: " + id));
    }

    // ---------------------------------------------------------------- 공통 유틸

    /** ordering 값을 인접 형제와 맞바꿔 순서를 1칸 이동한다 - 목록은 이미 ordering 오름차순 정렬돼 있다고 가정. */
    private <T> void swapOrdering(List<T> ordered, java.util.function.Predicate<T> isTarget,
                                   java.util.function.Function<T, Integer> getter,
                                   java.util.function.BiConsumer<T, Integer> setter, String direction) {
        int idx = -1;
        for (int i = 0; i < ordered.size(); i++) {
            if (isTarget.test(ordered.get(i))) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다.");
        }
        int otherIdx = "up".equalsIgnoreCase(direction) ? idx - 1 : idx + 1;
        if (otherIdx < 0 || otherIdx >= ordered.size()) {
            return; // 이미 맨 위/맨 아래 - 조용히 무시
        }
        T a = ordered.get(idx);
        T b = ordered.get(otherIdx);
        Integer aOrd = getter.apply(a);
        Integer bOrd = getter.apply(b);
        setter.accept(a, bOrd);
        setter.accept(b, aOrd);
    }

    private SubcategoryDto toSubcategoryDto(GiftSubcategory s, List<ItemDto> items) {
        return new SubcategoryDto(s.getSubcategoryId(), s.getCategoryCode(), s.getName(), s.getOrdering(),
                s.getMetaTitle(), s.getMetaKeywords(), s.getMetaDescription(), items);
    }

    private ItemDto toItemDto(GiftSubcategoryItem i) {
        return new ItemDto(i.getSubcategoryItemId(), i.getSubcategoryId(), i.getName(), i.getOrdering());
    }

    // ---------------------------------------------------------------- DTO

    public record MajorDto(String id, String label, Integer ordering, String useYn, List<SubcategoryDto> subcategories) {
    }

    public record MajorForm(String id, String label, Integer ordering, String useYn) {
    }

    public record SubcategoryDto(Long subcategoryId, String categoryCode, String name, Integer ordering,
                                  String metaTitle, String metaKeywords, String metaDescription, List<ItemDto> items) {
    }

    public record SubcategoryForm(String categoryCode, String name, String metaTitle, String metaKeywords,
                                   String metaDescription) {
    }

    public record ItemDto(Long subcategoryItemId, Long subcategoryId, String name, Integer ordering) {
    }

    public record ItemForm(Long subcategoryId, String name) {
    }
}
