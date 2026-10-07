package com.ghlove.admin.web;

import com.ghlove.admin.domain.LocgovFaq;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.repository.LocgovFaqRepository;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MenuService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 고객센터 FAQ 관리 ({@code OP_COMMUNITY_LOCGOVFAQ}) - 공개 화면
 * {@code /faqs}·{@code /api/faqs}({@link com.ghlove.admin.service.FaqService})가 읽는 그 데이터를
 * 관리하는 화면이다. 쓰기는 ROLE_ADMIN_1~4(시스템/행안부)만, ROLE_ADMIN_5~6(지자체)은 조회만
 * 가능하다 - {@link #requireWriteAccess} 참고.
 *
 * <p><b>★ AS-IS 메뉴 11403 '지자체FAQ'를 이 화면으로 되살리지 않기로 했다(2026-10-04 사용자 결정).</b>
 * AS-IS에 같은 URL({@code /opmanager/community/locv-faq})의 {@code LocgFaqManagerController}가 있고
 * 같은 표를 다루지만, 그 <b>메뉴는 AS-IS에서 중지</b>({@code display_flag='N'},
 * {@code status_code='2'})다. 더구나 AS-IS 화면의 질문유형 목록은 Java enum {@code FaqType}
 * ({@code F_LOGIN}·{@code F_CNTR_SYSTEM}…)인데 이 표의 실데이터 코드는
 * {@code JOIN}·{@code DONATE}… 라서 <b>애초에 서로 맞지 않는다</b>(메뉴가 꺼진 배경으로 보인다).
 *
 * <p>그래서 <b>op_menu에 11403을 중지 상태로만 등록</b>하고
 * ({@code migration-admin-menu-11403-locgov-faq-stopped.sql}) 이 화면은 손대지 않았다 -
 * AS-IS가 꺼 둔 화면을 살아서 쓰이는 공개 FAQ 관리 화면 위에 덮는 것이 오히려 AS-IS와
 * 멀어지기 때문이다. [[as-is-parity-includes-disabled-state]] 원칙("빼지도 켜지도 말 것")에 맞는다.
 *
 * <p><b>[2026-10-07 정정] 이전 판의 "{@code ADMIN_COMMON_CODE}는 없는 표" 라는 단정은 틀렸다.</b>
 * admin 서비스의 공통코드는 <b>읽는 경로가 두 갈래</b>이고 표도 둘 다 실재한다:
 * <ul>
 *   <li>JPA 엔티티 {@link com.ghlove.admin.domain.CommonCode} → {@code admin.ADMIN_COMMON_CODE}
 *       ({@link com.ghlove.admin.service.CommonCodeService#labelsOf}가 쓰는 경로)</li>
 *   <li>네이티브 SQL 10여 곳 → {@code admin.op_common_code}</li>
 * </ul>
 * 코드유형 71개가 양쪽에 겹쳐 있다. {@code service-admin.sql}이 넣은 {@code FAQ_TYPE} 11건
 * ({@code JOIN}·{@code DONATE}…)은 {@code ADMIN_COMMON_CODE}에 <b>정상 적재돼 있고</b>, 이 화면은
 * {@code labelsOf}를 쓰므로 그 표를 본다 - 즉 질문유형 라벨이 빈 값이라던 결함은 없다.
 *
 * <p>대신 같은 두 갈래 때문에 <b>담당자용 FAQ(11405)가 실제로 터졌다</b>: {@code CMNTY_FAQ_TYPE}
 * 11건이 {@code op_common_code}에만 들어가 질문유형 탭이 '전체' 하나만 보였다
 * ({@code migration-admin-cmnty-faq-type-codes-admin-table.sql}로 해결).
 * <b>공통코드를 다룰 때는 읽는 쪽이 JPA인지 네이티브인지부터 확인할 것.</b> 표 일원화는 별도 과제다.
 */
@Controller
@RequestMapping("/community/locv-faq")
@RequiredArgsConstructor
public class LocgFaqAdminController {

    private final LocgovFaqRepository locgovFaqRepository;
    private final CommonCodeService commonCodeService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("faqs", locgovFaqRepository.findAllByOrderByIdDesc());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/list";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("faq", new LocgovFaq());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, HttpSession session, Model model) {
        requireWriteAccess(session);
        model.addAttribute("faq", locgovFaqRepository.findById(id).orElseThrow());
        model.addAttribute("faqTypes", commonCodeService.labelsOf("FAQ_TYPE"));
        return "community/locv-faq/form";
    }

    @PostMapping
    public String create(LocgovFaq form, HttpSession session) {
        Manager manager = requireWriteAccess(session);
        form.setId(null);
        form.setAdminId(manager.getUserId());
        form.setUseYn("Y");
        form.setHits(0);
        form.setCreatedDate(LocalDateTime.now());
        form.setUpdatedDate(LocalDateTime.now());
        locgovFaqRepository.save(form);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, LocgovFaq form, HttpSession session) {
        Manager manager = requireWriteAccess(session);
        LocgovFaq faq = locgovFaqRepository.findById(id).orElseThrow();
        faq.setFaqType(form.getFaqType());
        faq.setSubject(form.getSubject());
        faq.setContent(form.getContent());
        faq.setUpdatedBy(manager.getUserId());
        faq.setUpdatedDate(LocalDateTime.now());
        locgovFaqRepository.save(faq);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id, HttpSession session) {
        requireWriteAccess(session);
        LocgovFaq faq = locgovFaqRepository.findById(id).orElseThrow();
        faq.setUseYn("Y".equals(faq.getUseYn()) ? "N" : "Y");
        locgovFaqRepository.save(faq);
        return "redirect:/community/locv-faq";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, HttpSession session) {
        requireWriteAccess(session);
        locgovFaqRepository.deleteById(id);
        return "redirect:/community/locv-faq";
    }

    /** AS-IS 실제 권한: 지자체담당자(ROLE_ADMIN_5/6)는 이 화면을 조회는 해도 쓰기는 못 한다. */
    private static Manager requireWriteAccess(HttpSession session) {
        Manager manager = (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
        if (MenuService.isLocgovScoped(manager)) {
            throw new ManagerException("지자체 담당자는 조회만 가능합니다.");
        }
        return manager;
    }
}
