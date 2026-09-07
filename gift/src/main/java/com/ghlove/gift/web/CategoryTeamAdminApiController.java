package com.ghlove.gift.web;

import com.ghlove.gift.domain.CategoryGroup;
import com.ghlove.gift.domain.CategoryGroupBanner;
import com.ghlove.gift.domain.CategoryTeam;
import com.ghlove.gift.domain.CategoryTeamItem;
import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.repository.CategoryGroupBannerRepository;
import com.ghlove.gift.repository.CategoryGroupRepository;
import com.ghlove.gift.repository.CategoryTeamItemRepository;
import com.ghlove.gift.repository.CategoryTeamRepository;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.service.FileStorageService;
import com.ghlove.gift.service.GiftException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 카테고리 "팀"(대분류 상위그룹)+"그룹"(팀 하위, 프로모션묶음) 관리 (AS-IS
 * saleson.shop.categoriesteamgroup, OP_CATEGORY_TEAM/OP_CATEGORY_TEAM_ITEM/OP_CATEGORY_GROUP/
 * OP_CATEGORY_GROUP_BANNER - 답례품 상품관리 2단계 #5). gift DB에 이미 이관돼 있던 테이블을
 * 처음으로 CRUD 대상으로 활성화한다. CategoryAdminApiController(GIFT_CATEGORY 대분류/중분류)
 * 와는 별개 트리 - "팀/그룹"은 GNB 카테고리와 무관하게 프로모션 기획전 성격의 상품 묶음이다.
 */
@RestController
@RequestMapping("/api/admin/category-teams")
@RequiredArgsConstructor
public class CategoryTeamAdminApiController {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String FLAG_ON = "Y";

    private final CategoryTeamRepository teamRepository;
    private final CategoryGroupRepository groupRepository;
    private final CategoryTeamItemRepository teamItemRepository;
    private final CategoryGroupBannerRepository bannerRepository;
    private final GiftRepository giftRepository;
    private final FileStorageService fileStorageService;

    // ---------------------------------------------------------------- 팀

    @GetMapping
    public List<TeamDto> list() {
        List<CategoryTeam> teams = teamRepository.findAllByOrderByOrderingAscCategoryTeamIdAsc();
        Map<Integer, List<CategoryGroup>> groupsByTeam = groupRepository.findAllByOrderByOrderingAscCategoryGroupIdAsc()
                .stream().collect(Collectors.groupingBy(CategoryGroup::getCategoryTeamId, LinkedHashMap::new, Collectors.toList()));
        Map<Integer, List<CategoryGroupBanner>> bannersByGroup = bannerRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(b -> b.getDisplayOrder() == null ? 0 : b.getDisplayOrder()))
                .collect(Collectors.groupingBy(CategoryGroupBanner::getCategoryGroupId, LinkedHashMap::new, Collectors.toList()));
        return teams.stream()
                .map(t -> new TeamDto(t.getCategoryTeamId(), t.getName(), t.getCode(), t.getCategoryTeamFlag(), t.getOrdering(),
                        groupsByTeam.getOrDefault(t.getCategoryTeamId(), List.of()).stream()
                                .map(g -> toGroupDto(g, bannersByGroup.getOrDefault(g.getCategoryGroupId(), List.of())))
                                .toList()))
                .toList();
    }

    @GetMapping("/{id}")
    public TeamDto get(@PathVariable Integer id) {
        CategoryTeam team = findTeam(id);
        return toTeamDto(team, groupRepository.findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(id));
    }

    @PostMapping
    public TeamDto create(@RequestBody TeamForm form) {
        if (form.name() == null || form.name().isBlank() || form.code() == null || form.code().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀명과 코드는 필수입니다.");
        }
        if (teamRepository.existsByCode(form.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 코드입니다: " + form.code());
        }
        int nextOrdering = teamRepository.findAllByOrderByOrderingAscCategoryTeamIdAsc().stream()
                .mapToInt(t -> t.getOrdering() == null ? 0 : t.getOrdering()).max().orElse(-1) + 1;
        CategoryTeam team = new CategoryTeam();
        team.setName(form.name());
        team.setCode(form.code());
        team.setCategoryTeamFlag(form.flag() != null ? form.flag() : FLAG_ON);
        team.setOrdering(form.ordering() != null ? form.ordering() : nextOrdering);
        team.setCreatedDate(TS.format(LocalDateTime.now()));
        team.setUpdatedDate(team.getCreatedDate());
        teamRepository.save(team);
        return toTeamDto(team, List.of());
    }

    @PutMapping("/{id}")
    public TeamDto update(@PathVariable Integer id, @RequestBody TeamForm form) {
        CategoryTeam team = findTeam(id);
        team.setName(form.name());
        if (form.ordering() != null) {
            team.setOrdering(form.ordering());
        }
        if (form.flag() != null) {
            team.setCategoryTeamFlag(form.flag());
        }
        team.setUpdatedDate(TS.format(LocalDateTime.now()));
        teamRepository.save(team);
        return toTeamDto(team, groupRepository.findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(id));
    }

    @Transactional
    @PostMapping("/{id}/delete")
    public void deleteTeam(@PathVariable Integer id) {
        if (!groupRepository.findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "하위 그룹이 남아있어 삭제할 수 없습니다. 먼저 하위 그룹을 삭제하세요.");
        }
        teamItemRepository.findByCategoryTeamIdOrderByCategoryTeamItemIdDesc(id).forEach(i -> teamItemRepository.deleteById(i.getCategoryTeamItemId()));
        teamRepository.deleteById(id);
    }

    @PostMapping("/{id}/move")
    public void moveTeam(@PathVariable Integer id, @RequestParam String direction) {
        List<CategoryTeam> ordered = teamRepository.findAllByOrderByOrderingAscCategoryTeamIdAsc();
        swapOrdering(ordered, t -> t.getCategoryTeamId().equals(id), CategoryTeam::getOrdering, CategoryTeam::setOrdering, direction);
        teamRepository.saveAll(ordered);
    }

    private CategoryTeam findTeam(Integer id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다: " + id));
    }

    // ---------------------------------------------------------------- 팀에 배정된 답례품

    @GetMapping("/{id}/items")
    public List<TeamItemDto> teamItems(@PathVariable Integer id) {
        List<CategoryTeamItem> links = teamItemRepository.findByCategoryTeamIdOrderByCategoryTeamItemIdDesc(id);
        Map<Long, String> names = giftRepository.findAllById(links.stream().map(CategoryTeamItem::getItemId).toList())
                .stream().collect(Collectors.toMap(Gift::getItemId, Gift::getItemName, (a, b) -> a));
        return links.stream().map(l -> new TeamItemDto(l.getCategoryTeamItemId(), l.getItemId(), names.getOrDefault(l.getItemId(), ""))).toList();
    }

    /** 답례품 일괄 배정 (CategoryAdminApiController#bulkAssignCategory와 동일한 관행). */
    @PostMapping("/{id}/items")
    public Map<String, Integer> addItems(@PathVariable Integer id, @RequestParam List<Long> itemIds) {
        findTeam(id);
        List<Gift> gifts = giftRepository.findAllById(itemIds);
        if (gifts.size() != itemIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 답례품이 포함되어 있습니다.");
        }
        int added = 0;
        for (Long itemId : itemIds) {
            CategoryTeamItem link = new CategoryTeamItem();
            link.setCategoryTeamId(id);
            link.setItemId(itemId);
            link.setCreatedDate(TS.format(LocalDateTime.now()));
            teamItemRepository.save(link);
            added++;
        }
        return Map.of("added", added);
    }

    @Transactional
    @PostMapping("/{id}/items/{itemId}/delete")
    public void removeItem(@PathVariable Integer id, @PathVariable Long itemId) {
        teamItemRepository.deleteByCategoryTeamIdAndItemId(id, itemId);
    }

    // ---------------------------------------------------------------- 그룹

    @GetMapping("/groups/{id}")
    public GroupDto group(@PathVariable Integer id) {
        return toGroupDto(findGroup(id), bannerRepository.findByCategoryGroupIdOrderByDisplayOrderAsc(id));
    }

    @PostMapping("/groups")
    public GroupDto createGroup(@RequestBody GroupForm form) {
        if (form.categoryTeamId() == null || form.name() == null || form.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "소속 팀과 그룹명은 필수입니다.");
        }
        findTeam(form.categoryTeamId());
        int nextOrdering = groupRepository.findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(form.categoryTeamId()).stream()
                .mapToInt(g -> g.getOrdering() == null ? 0 : g.getOrdering()).max().orElse(-1) + 1;
        CategoryGroup group = new CategoryGroup();
        group.setCategoryTeamId(form.categoryTeamId());
        group.setName(form.name());
        group.setCode(form.code());
        group.setCategoryGroupFlag(form.flag() != null ? form.flag() : FLAG_ON);
        group.setOrdering(nextOrdering);
        group.setCreatedDate(TS.format(LocalDateTime.now()));
        group.setUpdatedDate(group.getCreatedDate());
        groupRepository.save(group);
        return toGroupDto(group, List.of());
    }

    @PutMapping("/groups/{id}")
    public GroupDto updateGroup(@PathVariable Integer id, @RequestBody GroupForm form) {
        CategoryGroup group = findGroup(id);
        group.setName(form.name());
        group.setCode(form.code());
        if (form.flag() != null) {
            group.setCategoryGroupFlag(form.flag());
        }
        group.setUpdatedDate(TS.format(LocalDateTime.now()));
        groupRepository.save(group);
        return toGroupDto(group, bannerRepository.findByCategoryGroupIdOrderByDisplayOrderAsc(id));
    }

    @Transactional
    @PostMapping("/groups/{id}/delete")
    public void deleteGroup(@PathVariable Integer id) {
        bannerRepository.findByCategoryGroupIdOrderByDisplayOrderAsc(id).forEach(b -> bannerRepository.deleteById(b.getCategoryGroupBannerId()));
        groupRepository.deleteById(id);
    }

    @PostMapping("/groups/{id}/move")
    public void moveGroup(@PathVariable Integer id, @RequestParam String direction) {
        CategoryGroup target = findGroup(id);
        List<CategoryGroup> siblings = groupRepository.findByCategoryTeamIdOrderByOrderingAscCategoryGroupIdAsc(target.getCategoryTeamId());
        swapOrdering(siblings, g -> g.getCategoryGroupId().equals(id), CategoryGroup::getOrdering, CategoryGroup::setOrdering, direction);
        groupRepository.saveAll(siblings);
    }

    private CategoryGroup findGroup(Integer id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다: " + id));
    }

    // ---------------------------------------------------------------- 그룹 배너

    @PostMapping(value = "/groups/{id}/banners", consumes = "multipart/form-data")
    public BannerDto addBanner(@PathVariable Integer id, @RequestParam String title,
                                @RequestParam(required = false) String linkUrl,
                                @RequestParam MultipartFile image) {
        CategoryGroup group = findGroup(id);
        String storedName;
        try {
            storedName = fileStorageService.store(image);
        } catch (GiftException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        int nextOrder = bannerRepository.findByCategoryGroupIdOrderByDisplayOrderAsc(id).stream()
                .mapToInt(b -> b.getDisplayOrder() == null ? 0 : b.getDisplayOrder()).max().orElse(-1) + 1;
        CategoryGroupBanner banner = new CategoryGroupBanner();
        banner.setCategoryGroupId(group.getCategoryGroupId());
        banner.setTitle(title);
        banner.setLinkUrl(linkUrl != null ? linkUrl : "");
        banner.setFileName(storedName);
        banner.setDisplayOrder(nextOrder);
        banner.setCreatedDate(TS.format(LocalDateTime.now()));
        bannerRepository.save(banner);
        return toBannerDto(banner);
    }

    @PostMapping("/banners/{id}/delete")
    public void deleteBanner(@PathVariable Integer id) {
        bannerRepository.deleteById(id);
    }

    @PostMapping("/banners/{id}/move")
    public void moveBanner(@PathVariable Integer id, @RequestParam String direction) {
        CategoryGroupBanner target = bannerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "배너를 찾을 수 없습니다: " + id));
        List<CategoryGroupBanner> siblings = bannerRepository.findByCategoryGroupIdOrderByDisplayOrderAsc(target.getCategoryGroupId());
        swapOrdering(siblings, b -> b.getCategoryGroupBannerId().equals(id), CategoryGroupBanner::getDisplayOrder, CategoryGroupBanner::setDisplayOrder, direction);
        bannerRepository.saveAll(siblings);
    }

    // ---------------------------------------------------------------- 공통 유틸

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
            return;
        }
        T a = ordered.get(idx);
        T b = ordered.get(otherIdx);
        Integer aOrd = getter.apply(a);
        Integer bOrd = getter.apply(b);
        setter.accept(a, bOrd);
        setter.accept(b, aOrd);
    }

    private TeamDto toTeamDto(CategoryTeam t, List<CategoryGroup> groups) {
        return new TeamDto(t.getCategoryTeamId(), t.getName(), t.getCode(), t.getCategoryTeamFlag(), t.getOrdering(),
                groups.stream().map(g -> toGroupDto(g, List.of())).toList());
    }

    private GroupDto toGroupDto(CategoryGroup g, List<CategoryGroupBanner> banners) {
        return new GroupDto(g.getCategoryGroupId(), g.getCategoryTeamId(), g.getName(), g.getCode(),
                g.getCategoryGroupFlag(), g.getOrdering(), banners.stream().map(this::toBannerDto).toList());
    }

    private BannerDto toBannerDto(CategoryGroupBanner b) {
        return new BannerDto(b.getCategoryGroupBannerId(), b.getCategoryGroupId(), b.getTitle(), b.getLinkUrl(),
                b.getFileName(), b.getDisplayOrder());
    }

    // ---------------------------------------------------------------- DTO

    public record TeamDto(Integer id, String name, String code, String flag, Integer ordering, List<GroupDto> groups) {
    }

    public record TeamForm(String name, String code, Integer ordering, String flag) {
    }

    public record GroupDto(Integer id, Integer categoryTeamId, String name, String code, String flag,
                            Integer ordering, List<BannerDto> banners) {
    }

    public record GroupForm(Integer categoryTeamId, String name, String code, Integer ordering, String flag) {
    }

    public record BannerDto(Integer id, Integer categoryGroupId, String title, String linkUrl, String fileName,
                             Integer displayOrder) {
    }

    public record TeamItemDto(Integer categoryTeamItemId, Long itemId, String itemName) {
    }
}
