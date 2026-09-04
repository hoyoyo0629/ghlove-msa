package com.ghlove.gift.web;

import com.ghlove.gift.domain.Gift;
import com.ghlove.gift.domain.StyleBook;
import com.ghlove.gift.domain.StyleBookItem;
import com.ghlove.gift.repository.GiftRepository;
import com.ghlove.gift.repository.StyleBookItemRepository;
import com.ghlove.gift.repository.StyleBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 스타일북(답례품 큐레이션) 관리자 CRUD (AS-IS opmanager/style-book - StyleBookManagerController).
 * admin 콘솔에서 이 API를 호출해 사용한다. */
@RestController
@RequestMapping("/api/admin/style-books")
@RequiredArgsConstructor
public class StyleBookAdminApiController {

    private final StyleBookRepository styleBookRepository;
    private final StyleBookItemRepository styleBookItemRepository;
    private final GiftRepository giftRepository;

    public record StyleBookRow(Long id, String title, String content, String image, Integer ordering) {
    }

    public record StyleBookItemRow(Long id, Long itemId, String itemName, Integer ordering) {
    }

    public record StyleBookForm(String title, String content, String image, Integer ordering) {
    }

    @GetMapping
    public List<StyleBookRow> list() {
        return styleBookRepository.findAllByOrderByOrderingAsc().stream()
                .map(s -> new StyleBookRow(s.getId(), s.getTitle(), s.getContent(), s.getImage(), s.getOrdering()))
                .toList();
    }

    @GetMapping("/{id}")
    public StyleBookRow get(@PathVariable Long id) {
        StyleBook s = styleBookRepository.findById(id).orElseThrow(() -> new StyleBookException("스타일북을 찾을 수 없습니다."));
        return new StyleBookRow(s.getId(), s.getTitle(), s.getContent(), s.getImage(), s.getOrdering());
    }

    @GetMapping("/{id}/items")
    public List<StyleBookItemRow> items(@PathVariable Long id) {
        return styleBookItemRepository.findByStyleBookIdOrderByOrderingAsc(id).stream()
                .map(i -> new StyleBookItemRow(i.getId(), i.getItemId(), itemName(i.getItemId()), i.getOrdering()))
                .toList();
    }

    @PostMapping
    @Transactional
    public StyleBookRow create(@RequestBody StyleBookForm form, @RequestHeader(value = "X-Manager-Id", required = false) Long managerId) {
        if (form.title() == null || form.title().isBlank()) {
            throw new StyleBookException("제목을 입력해 주세요.");
        }
        StyleBook s = new StyleBook();
        s.setTitle(form.title());
        s.setContent(form.content());
        s.setImage(form.image());
        s.setOrdering(form.ordering() != null ? form.ordering() : 0);
        s.setCreated(LocalDateTime.now());
        s.setCreatedBy(managerId);
        s.setUpdated(LocalDateTime.now());
        s.setUpdatedBy(managerId);
        StyleBook saved = styleBookRepository.save(s);
        return new StyleBookRow(saved.getId(), saved.getTitle(), saved.getContent(), saved.getImage(), saved.getOrdering());
    }

    @PutMapping("/{id}")
    @Transactional
    public StyleBookRow update(@PathVariable Long id, @RequestBody StyleBookForm form,
                                @RequestHeader(value = "X-Manager-Id", required = false) Long managerId) {
        StyleBook s = styleBookRepository.findById(id).orElseThrow(() -> new StyleBookException("스타일북을 찾을 수 없습니다."));
        s.setTitle(form.title());
        s.setContent(form.content());
        s.setImage(form.image());
        s.setOrdering(form.ordering() != null ? form.ordering() : s.getOrdering());
        s.setUpdated(LocalDateTime.now());
        s.setUpdatedBy(managerId);
        styleBookRepository.save(s);
        return new StyleBookRow(s.getId(), s.getTitle(), s.getContent(), s.getImage(), s.getOrdering());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        styleBookItemRepository.deleteByStyleBookId(id);
        styleBookRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/items")
    @Transactional
    public ResponseEntity<Void> addItem(@PathVariable Long id, @RequestBody Map<String, Long> body,
                                         @RequestHeader(value = "X-Manager-Id", required = false) Long managerId) {
        Long itemId = body.get("itemId");
        if (itemId == null || !giftRepository.existsById(itemId)) {
            throw new StyleBookException("존재하지 않는 답례품입니다.");
        }
        StyleBookItem item = new StyleBookItem();
        item.setStyleBookId(id);
        item.setItemId(itemId);
        int nextOrdering = styleBookItemRepository.findByStyleBookIdOrderByOrderingAsc(id).size() + 1;
        item.setOrdering(nextOrdering);
        item.setCreated(LocalDateTime.now());
        item.setCreatedBy(managerId);
        item.setUpdated(LocalDateTime.now());
        item.setUpdatedBy(managerId);
        styleBookItemRepository.save(item);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @Transactional
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        styleBookItemRepository.deleteByStyleBookIdAndItemId(id, itemId);
        return ResponseEntity.noContent().build();
    }

    private String itemName(Long itemId) {
        return giftRepository.findById(itemId).map(Gift::getItemName).orElse("(삭제된 답례품)");
    }

    @ExceptionHandler(StyleBookException.class)
    public ResponseEntity<Map<String, String>> handle(StyleBookException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    static class StyleBookException extends RuntimeException {
        StyleBookException(String message) {
            super(message);
        }
    }
}
