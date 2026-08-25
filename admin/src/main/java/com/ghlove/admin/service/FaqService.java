package com.ghlove.admin.service;

import com.ghlove.admin.domain.LocgovFaq;
import com.ghlove.admin.repository.LocgovFaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * 고객센터 FAQ (AS-IS faq/list.html). 시딩 규모(수십~백여 건)에서는 검색/정렬을
 * 인메모리로 처리하는 편이 파생 쿼리를 늘리는 것보다 간단하다(이 프로젝트에서 이미
 * 여러 번 쓴 관례 - designated-donation 목록 필터링과 동일한 방식).
 */
@Service
@RequiredArgsConstructor
public class FaqService {

    private final LocgovFaqRepository locgovFaqRepository;

    public List<LocgovFaq> search(String faqType, String keyword, String sort) {
        List<LocgovFaq> all = locgovFaqRepository.findByUseYn("Y");

        List<LocgovFaq> filtered = (faqType == null || faqType.isBlank())
                ? all
                : all.stream().filter(f -> faqType.equals(f.getFaqType())).toList();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            filtered = filtered.stream()
                    .filter(f -> f.getSubject() != null && f.getSubject().contains(kw))
                    .toList();
        }

        Comparator<LocgovFaq> comparator = "updated,ASC".equals(sort)
                ? Comparator.comparing(LocgovFaq::getUpdatedDate, Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator.comparing(LocgovFaq::getUpdatedDate, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        return filtered.stream().sorted(comparator).toList();
    }

    /** AS-IS는 아코디언을 펼칠 때마다 조회수를 올린다(addHits). */
    @Transactional
    public void addHit(Integer id) {
        locgovFaqRepository.findById(id).ifPresent(f -> f.setHits((f.getHits() == null ? 0 : f.getHits()) + 1));
    }
}
