package com.ghlove.admin.service;

import com.ghlove.admin.domain.Faq;
import com.ghlove.admin.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * 고객센터 FAQ 공개 조회 - AS-IS 공개 API {@code /api/faq}({@code saleson.api.faq.FaqController})와
 * 공개 페이지 {@code /faq/list.html}이 쓰는 조합을 옮긴 것이다.
 *
 * <p><b>AS-IS 그대로</b>: {@code useYn='Y'}만, 정렬 {@code updated DESC}, 기본 10건,
 * 질문유형 목록은 공통코드가 아니라 {@link FaqType} enum이다.
 * 아코디언을 펼칠 때마다 조회수를 올린다({@code /api/faq/hits}).
 *
 * <p><b>★ 2026-10-05 표를 바로잡았다</b>: 이 화면은 원래 {@code op_community_locgovfaq}를 읽고 있었다.
 * 그 표는 AS-IS에서 <b>중지된 메뉴 11403 '지자체FAQ'</b>의 것이고, 공개 FAQ의 정본은
 * {@code OP_FAQ}다(AS-IS 통합검색 뷰 {@code view_search_faq}도 {@code op_faq}를 보고
 * {@code /faq/list.html}로 링크한다). TO-BE 초기 시드가 FAQ 63건을 엉뚱한 표에 자체 코드
 * ({@code JOIN}·{@code DONATE}…)로 넣어 둔 것이라, 라벨이 같은 AS-IS enum 코드로 1:1 바꿔
 * {@code op_faq}로 옮기고 읽는 쪽을 여기로 맞췄다
 * (database/ddl/migration-admin-faq-5104.sql). 관리 화면은 메뉴 5104
 * ({@link com.ghlove.admin.web.FaqAdminController})다.
 */
@Service
@RequiredArgsConstructor
public class FaqService {

    private final FaqRepository faqRepository;

    /** 공개화면이 쓰는 모양 - AS-IS 화면이 참조하는 필드명(subject/updatedDate/hits)에 맞춘 것이다. */
    public record FaqRow(Long id, String faqType, String subject, String content,
                         LocalDateTime updatedDate, Integer hits) {
    }

    public List<FaqRow> search(String faqType, String keyword, String sort) {
        List<Faq> all = faqRepository.findByUseYn("Y");

        List<Faq> filtered = (faqType == null || faqType.isBlank())
                ? all
                : all.stream().filter(f -> faqType.equals(f.getFaqType())).toList();
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            filtered = filtered.stream()
                    .filter(f -> f.getTitle() != null && f.getTitle().contains(kw))
                    .toList();
        }

        Comparator<Faq> comparator = "updated,ASC".equals(sort)
                ? Comparator.comparing(Faq::getUpdated, Comparator.nullsLast(Comparator.naturalOrder()))
                : Comparator.comparing(Faq::getUpdated, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        return filtered.stream().sorted(comparator)
                .map(f -> new FaqRow(f.getId(), f.getFaqType(), f.getTitle(), f.getContent(),
                        f.getUpdated(), f.getHit()))
                .toList();
    }

    /** AS-IS는 아코디언을 펼칠 때마다 조회수를 올린다(/api/faq/hits). */
    @Transactional
    public void addHit(Long id) {
        faqRepository.findById(id).ifPresent(f -> f.setHit((f.getHit() == null ? 0 : f.getHit()) + 1));
    }

    /** 질문유형 목록 - AS-IS {@code enumMapper.get("FaqType")} 자리. */
    public java.util.Map<String, String> faqTypes() {
        return FaqType.options();
    }
}
