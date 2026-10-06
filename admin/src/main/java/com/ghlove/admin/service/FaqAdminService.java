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
 * FAQ 관리 (메뉴 5104) - AS-IS {@code FaqManagerController}({@code /opmanager/faq}) +
 * {@code FaqServiceImpl} + {@code FaqDto.getPredicate()} 이식.
 *
 * <p><b>AS-IS 그대로</b>:
 * <ul>
 *   <li>진입(GET)은 <b>조회하지 않고 빈 목록</b>을 내려준다
 *       ({@code new PageImpl<>(Collections.emptyList())}) - 검색(POST)해야 조회된다.
 *       이 프로젝트에서 반복되는 운영화면 패턴이다.</li>
 *   <li>정렬은 {@code id DESC} 고정이다({@code @PageableDefault(sort="id", DESC)}) -
 *       화면에 정렬 수단이 없다.</li>
 *   <li>검색은 화면이 {@code where=title} <b>hidden 고정</b>으로 보내 <b>제목만</b> 찾는다.
 *       AS-IS 조건식에는 {@code all}(제목+내용)·{@code content}(내용) 분기도 있지만
 *       화면의 검색구분 select가 <b>주석처리</b>되어 있어 쓰이지 않는다 - 분기는 옮기고
 *       화면은 주석 그대로 둔다.</li>
 *   <li>운영자 목록은 <b>미사용(use_yn='N') 건도 보인다</b> - 화면이 useYn을 보내지 않고
 *       AS-IS 조건식도 값이 없으면 걸지 않는다. 공개화면만 {@code useYn='Y'}로 거른다
 *       ({@link FaqService}).</li>
 *   <li>저장은 등록·수정 모두 <b>{@code use_yn='Y'}로 다시 써 넣는다</b>
 *       - 즉 이 화면에는 사용여부를 끄는 수단이 없다(AS-IS도 없다).</li>
 *   <li>삭제는 <b>행을 지운다</b>(소프트 삭제가 아니다, {@code faqRepository.deleteAll}).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class FaqAdminService {

    private final FaqRepository faqRepository;

    /** 목록 한 행 - 화면이 쓰는 모양 그대로(질문유형은 라벨까지 같이). */
    public record FaqRow(Long id, String faqType, String faqTypeLabel, String title, String content,
                         Integer hit, String useYn, LocalDateTime created) {
    }

    /**
     * AS-IS {@code FaqDto.getPredicate()} + {@code findAll(predicate, pageable)}.
     * 정렬은 {@code id DESC} 고정이고, 페이징은 호출부가 잘라 쓰도록 전체 목록을 돌려준다.
     */
    public List<FaqRow> search(String where, String query, String faqType) {
        List<Faq> all = faqRepository.findAll();

        String keyword = query == null ? null : query.trim();
        if (keyword != null && !keyword.isEmpty()) {
            String needle = keyword;
            all = all.stream().filter(f -> matches(f, where, needle)).toList();
        }
        if (faqType != null && !faqType.isBlank()) {
            all = all.stream().filter(f -> faqType.equals(f.getFaqType())).toList();
        }

        return all.stream()
                .sorted(Comparator.comparing(Faq::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(f -> new FaqRow(f.getId(), f.getFaqType(), FaqType.titleOf(f.getFaqType()),
                        f.getTitle(), f.getContent(), f.getHit(), f.getUseYn(), f.getCreated()))
                .toList();
    }

    /** AS-IS 조건식의 세 분기. 화면은 {@code title}만 보낸다. */
    private static boolean matches(Faq faq, String where, String keyword) {
        boolean inTitle = faq.getTitle() != null && faq.getTitle().contains(keyword);
        boolean inContent = faq.getContent() != null && faq.getContent().contains(keyword);
        if ("all".equalsIgnoreCase(where)) {
            return inTitle || inContent;
        }
        if ("content".equalsIgnoreCase(where)) {
            return inContent;
        }
        return inTitle;
    }

    public Faq find(Long id) {
        return faqRepository.findById(id)
                // AS-IS UserException("정보가 없습니다.", "/opmanager/faq/list")
                .orElseThrow(() -> new FaqException("정보가 없습니다."));
    }

    /**
     * AS-IS create POST - 검증은 {@code @Valid FaqDto}(질문유형·제목·내용 필수)와
     * {@code FaqValidator}(제목)로 두 번 한다. 문구는 AS-IS 것을 그대로 쓴다.
     */
    @Transactional
    public Faq create(String faqType, String title, String content, Long managerUserId) {
        validate(faqType, title, content);
        Faq faq = new Faq();
        faq.setFaqType(faqType);
        faq.setTitle(title);
        faq.setContent(content);
        faq.setHit(0);
        faq.setUseYn("Y");
        faq.setCreated(LocalDateTime.now());
        faq.setCreatedBy(managerUserId);
        faq.setUpdated(LocalDateTime.now());
        faq.setUpdatedBy(managerUserId);
        return faqRepository.save(faq);
    }

    /** AS-IS edit POST - {@code modelMapper.map(dto, faq)} 후 {@code useYn='Y'}로 다시 써 넣는다. */
    @Transactional
    public Faq update(Long id, String faqType, String title, String content, Long managerUserId) {
        Faq faq = find(id);
        validate(faqType, title, content);
        faq.setFaqType(faqType);
        faq.setTitle(title);
        faq.setContent(content);
        faq.setUseYn("Y");
        faq.setUpdated(LocalDateTime.now());
        faq.setUpdatedBy(managerUserId);
        return faqRepository.save(faq);
    }

    /**
     * AS-IS deleteListData - 체크한 건을 <b>행 삭제</b>한다.
     * 선택이 없으면 AS-IS 문구 그대로 "처리할 데이터가 없습니다."
     */
    @Transactional
    public void deleteList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new FaqException("처리할 데이터가 없습니다.");
        }
        faqRepository.deleteAll(faqRepository.findAllById(ids));
    }

    /**
     * AS-IS 검증 - {@code @Valid FaqDto}(@NotNull faqType, @NotEmpty title/content) 다음에
     * {@code FaqValidator}(제목)가 한 번 더 돈다.
     *
     * <p>제목 문구는 AS-IS {@code FaqValidator}에 적힌 것 그대로다. 질문유형·내용은 AS-IS에
     * <b>작성된 문구가 없고</b> Bean Validation 기본 문구를 {@code Message.set(errors)}가
     * "비어 있을 수 없습니다 (content)" 꼴로 보여준다 - 프레임워크 로케일 산출물이라 옮길 대상이
     * 아니라고 보고 같은 뜻의 짧은 문구를 썼다. 화면상 거의 닿지 않는 경로다:
     * 질문유형 select에는 빈 항목이 없어 항상 값이 있고, 제목은 클라이언트에서 required로 막힌다
     * (내용에는 AS-IS가 required를 걸지 않아 빈 내용만 서버에서 걸린다).
     */
    private static void validate(String faqType, String title, String content) {
        if (title == null || title.isBlank()) {
            throw new FaqException("제목을 반드시 입력해 주세요.");
        }
        if (faqType == null || faqType.isBlank()) {
            throw new FaqException("질문유형을 선택해 주세요.");
        }
        if (content == null || content.isBlank()) {
            throw new FaqException("내용을 입력해 주세요.");
        }
    }
}
