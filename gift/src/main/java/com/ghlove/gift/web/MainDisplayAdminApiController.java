package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.MainDisplayItem;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.MainDisplayItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** 메인 진열상품 관리 (AS-IS opmanager/main-display - MainDisplayItemManagerController).
 * TEMPLATE_ID(뷰타입+카테고리팀코드 조합) 단위로 등록시 기존 항목 전량삭제 후 재삽입한다
 * (AS-IS와 동일한 치환 방식). */
@RestController
@RequestMapping("/api/admin/main-display")
@RequiredArgsConstructor
public class MainDisplayAdminApiController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MainDisplayItemRepository repository;
    private final GiftRepository giftRepository;

    public record ItemRow(Long itemId, String itemName, Integer displayOrder) {
    }

    @GetMapping("/{templateId}")
    public List<ItemRow> get(@PathVariable String templateId) {
        return repository.findByTemplateIdOrderByDisplayOrderAsc(templateId).stream()
                .map(i -> new ItemRow(i.getItemId(), itemName(i.getItemId()), i.getDisplayOrder()))
                .toList();
    }

    @PutMapping("/{templateId}")
    @Transactional
    public ResponseEntity<Void> replace(@PathVariable String templateId, @RequestBody Map<String, List<Long>> body) {
        List<Long> itemIds = body.getOrDefault("itemIds", List.of());
        repository.deleteByTemplateId(templateId);
        int order = 1;
        for (Long itemId : itemIds) {
            if (!giftRepository.existsById(itemId)) {
                continue;
            }
            MainDisplayItem row = new MainDisplayItem();
            row.setTemplateId(templateId);
            row.setItemId(itemId);
            row.setDisplayOrder(order++);
            row.setCreatedDate(LocalDateTime.now().format(FMT));
            repository.save(row);
        }
        return ResponseEntity.noContent().build();
    }

    private String itemName(Long itemId) {
        return giftRepository.findById(itemId).map(Gift::getItemName).orElse("(삭제된 답례품)");
    }
}
