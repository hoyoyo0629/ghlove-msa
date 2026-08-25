package com.ghlove.admin.web;

import com.ghlove.admin.domain.*;
import com.ghlove.admin.repository.*;
import com.ghlove.admin.service.CommonCodeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** 커뮤니티 게시판 4종 (AS-IS opmanager/community/{bbs,sr-bbs,off-sr-bbs,faq-bbs}) -
 *  지자체 담당자 ↔ 행안부 내부 소통용. 4개 테이블이 완전히 동일한 구조라(faq-bbs만
 *  FAQ_TYPE 하나 추가) 컨트롤러 하나로 boardType 경로변수를 받아 처리한다 - 실제로
 *  같은 화면 4개를 반복하는 것보다 이쪽이 진짜 중복 제거다. */
@Controller
@RequestMapping("/community/{boardType}")
@RequiredArgsConstructor
public class CmntyBoardController {

    private static final Map<String, String> TITLES = Map.of(
            "bbs", "자유게시판",
            "sr-bbs", "SR게시판(기능개선/오류신고)",
            "off-sr-bbs", "오프라인SR게시판(오프라인기부 관련)",
            "faq-bbs", "FAQ게시판(시스템 운영 FAQ)"
    );

    private final CmntyBbsRepository bbsRepository;
    private final CmntySrBbsRepository srBbsRepository;
    private final CmntyOffSrBbsRepository offSrBbsRepository;
    private final CmntyFaqBbsRepository faqBbsRepository;
    private final CommonCodeService commonCodeService;

    @GetMapping
    public String list(@PathVariable String boardType, Model model) {
        requireKnownType(boardType);
        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", TITLES.get(boardType));
        model.addAttribute("isFaq", "faq-bbs".equals(boardType));
        if ("faq-bbs".equals(boardType)) {
            model.addAttribute("posts", faqBbsRepository.findAllByOrderByBbsIdDesc());
            model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        } else {
            model.addAttribute("posts", listOf(boardType));
        }
        return "community/board/list";
    }

    private List<? extends CmntyBoard> listOf(String boardType) {
        return switch (boardType) {
            case "bbs" -> bbsRepository.findAllByOrderByBbsIdDesc();
            case "sr-bbs" -> srBbsRepository.findAllByOrderByBbsIdDesc();
            case "off-sr-bbs" -> offSrBbsRepository.findAllByOrderByBbsIdDesc();
            default -> throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        };
    }

    @GetMapping("/new")
    public String createForm(@PathVariable String boardType, Model model) {
        requireKnownType(boardType);
        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", TITLES.get(boardType));
        model.addAttribute("isFaq", "faq-bbs".equals(boardType));
        model.addAttribute("post", newInstance(boardType));
        if ("faq-bbs".equals(boardType)) {
            model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        }
        return "community/board/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String boardType, @PathVariable Long id, Model model) {
        requireKnownType(boardType);
        model.addAttribute("boardType", boardType);
        model.addAttribute("boardTitle", TITLES.get(boardType));
        model.addAttribute("isFaq", "faq-bbs".equals(boardType));
        model.addAttribute("post", findOrThrow(boardType, id));
        if ("faq-bbs".equals(boardType)) {
            model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        }
        return "community/board/form";
    }

    @PostMapping
    public String create(@PathVariable String boardType, @RequestParam String bbsTtl, @RequestParam String bbsCn,
                          @RequestParam(required = false) String faqType,
                          @RequestParam(required = false) String noticeYn,
                          @RequestParam(required = false) String isSecret,
                          HttpSession session) {
        requireKnownType(boardType);
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        CmntyBoard entity = newInstance(boardType);
        entity.setBbsTtl(bbsTtl);
        entity.setBbsCn(bbsCn);
        entity.setUseYn("Y");
        entity.setNoticeYn(noticeYn != null ? "Y" : "N");
        entity.setIsSecret(isSecret != null ? "Y" : "N");
        entity.setInqCnt(0L);
        entity.setFrstCrtId(manager.getUserId());
        entity.setFrstCrtDt(LocalDateTime.now());
        if (entity instanceof CmntyFaqBbs faq) {
            faq.setFaqType(faqType);
        }
        save(boardType, entity);
        return "redirect:/community/" + boardType;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String boardType, @PathVariable Long id, @RequestParam String bbsTtl,
                          @RequestParam String bbsCn, @RequestParam(required = false) String faqType,
                          @RequestParam(required = false) String noticeYn,
                          @RequestParam(required = false) String isSecret,
                          HttpSession session) {
        requireKnownType(boardType);
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        CmntyBoard entity = findOrThrow(boardType, id);
        entity.setBbsTtl(bbsTtl);
        entity.setBbsCn(bbsCn);
        entity.setNoticeYn(noticeYn != null ? "Y" : "N");
        entity.setIsSecret(isSecret != null ? "Y" : "N");
        entity.setLastMdfcnId(manager.getUserId());
        entity.setLastMdfcnDt(LocalDateTime.now());
        if (entity instanceof CmntyFaqBbs faq) {
            faq.setFaqType(faqType);
        }
        save(boardType, entity);
        return "redirect:/community/" + boardType;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String boardType, @PathVariable Long id) {
        requireKnownType(boardType);
        switch (boardType) {
            case "bbs" -> bbsRepository.deleteById(id);
            case "sr-bbs" -> srBbsRepository.deleteById(id);
            case "off-sr-bbs" -> offSrBbsRepository.deleteById(id);
            case "faq-bbs" -> faqBbsRepository.deleteById(id);
            default -> throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        }
        return "redirect:/community/" + boardType;
    }

    private CmntyBoard newInstance(String boardType) {
        return switch (boardType) {
            case "bbs" -> new CmntyBbs();
            case "sr-bbs" -> new CmntySrBbs();
            case "off-sr-bbs" -> new CmntyOffSrBbs();
            case "faq-bbs" -> new CmntyFaqBbs();
            default -> throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        };
    }

    private CmntyBoard findOrThrow(String boardType, Long id) {
        return switch (boardType) {
            case "bbs" -> bbsRepository.findById(id).orElseThrow();
            case "sr-bbs" -> srBbsRepository.findById(id).orElseThrow();
            case "off-sr-bbs" -> offSrBbsRepository.findById(id).orElseThrow();
            case "faq-bbs" -> faqBbsRepository.findById(id).orElseThrow();
            default -> throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        };
    }

    private void save(String boardType, CmntyBoard entity) {
        switch (boardType) {
            case "bbs" -> bbsRepository.save((CmntyBbs) entity);
            case "sr-bbs" -> srBbsRepository.save((CmntySrBbs) entity);
            case "off-sr-bbs" -> offSrBbsRepository.save((CmntyOffSrBbs) entity);
            case "faq-bbs" -> faqBbsRepository.save((CmntyFaqBbs) entity);
            default -> throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        }
    }

    private void requireKnownType(String boardType) {
        if (!TITLES.containsKey(boardType)) {
            throw new IllegalArgumentException("알 수 없는 게시판입니다: " + boardType);
        }
    }
}
