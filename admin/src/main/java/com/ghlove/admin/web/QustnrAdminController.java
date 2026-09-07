package com.ghlove.admin.web;

import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.domain.Qustnr;
import com.ghlove.admin.domain.QustnrQesitm;
import com.ghlove.admin.repository.QustnrQesitmRepository;
import com.ghlove.admin.repository.QustnrRepository;
import com.ghlove.admin.repository.QustnrRspnsRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/** 설문조사 관리 (AS-IS opmanager/qustnr - QustnrManagerController, 신규 테이블).
 * AS-IS는 객관식/주관식 등 복합 문항 구조지만 이 라운드 지시서 범위는 "문항 여러개 등록 +
 * 결과집계(총응답수만)"로 한정되어 문항을 자유서술형 텍스트 목록으로 단순화했다. */
@Controller
@RequestMapping("/admin/surveys")
@RequiredArgsConstructor
public class QustnrAdminController {

    private final QustnrRepository qustnrRepository;
    private final QustnrQesitmRepository qesitmRepository;
    private final QustnrRspnsRepository rspnsRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("surveys", qustnrRepository.findAllByOrderByQustnrSnDesc());
        return "survey-admin/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("survey", new Qustnr());
        model.addAttribute("questions", List.of());
        return "survey-admin/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("survey", qustnrRepository.findById(id).orElseThrow());
        model.addAttribute("questions", qesitmRepository.findByQustnrSnOrderByQestnSeq(id));
        return "survey-admin/form";
    }

    @GetMapping("/{id}/result")
    public String result(@PathVariable Long id, Model model) {
        model.addAttribute("survey", qustnrRepository.findById(id).orElseThrow());
        model.addAttribute("questions", qesitmRepository.findByQustnrSnOrderByQestnSeq(id));
        model.addAttribute("total", rspnsRepository.countByQustnrSn(id));
        return "survey-admin/result";
    }

    @PostMapping
    @Transactional
    public String create(@ModelAttribute Qustnr form, @RequestParam(required = false) List<String> questions,
                          HttpSession session) {
        Manager manager = manager(session);
        form.setQustnrSn(null);
        if (form.getIsShow() == null || form.getIsShow().isBlank()) {
            form.setIsShow("Y");
        }
        form.setFrstCrtId(manager.getUserId());
        form.setFrstCrtDt(LocalDateTime.now());
        Qustnr saved = qustnrRepository.save(form);
        saveQuestions(saved.getQustnrSn(), questions);
        return "redirect:/admin/surveys";
    }

    @PostMapping("/{id}")
    @Transactional
    public String update(@PathVariable Long id, @ModelAttribute Qustnr form,
                          @RequestParam(required = false) List<String> questions, HttpSession session) {
        Manager manager = manager(session);
        Qustnr survey = qustnrRepository.findById(id).orElseThrow();
        survey.setQustnrSj(form.getQustnrSj());
        survey.setQustnrBgnDe(form.getQustnrBgnDe());
        survey.setQustnrEndDe(form.getQustnrEndDe());
        survey.setIsShow(form.getIsShow() != null ? "Y" : "N");
        survey.setLastMdfcnId(manager.getUserId());
        survey.setLastMdfcnDt(LocalDateTime.now());
        qustnrRepository.save(survey);
        qesitmRepository.deleteByQustnrSn(id);
        saveQuestions(id, questions);
        return "redirect:/admin/surveys/" + id + "/edit";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id) {
        qesitmRepository.deleteByQustnrSn(id);
        qustnrRepository.deleteById(id);
        return "redirect:/admin/surveys";
    }

    private void saveQuestions(Long qustnrSn, List<String> questions) {
        if (questions == null) {
            return;
        }
        int seq = 1;
        for (String q : questions) {
            if (q == null || q.isBlank()) {
                continue;
            }
            QustnrQesitm qesitm = new QustnrQesitm();
            qesitm.setQustnrSn(qustnrSn);
            qesitm.setQestnCn(q);
            qesitm.setQestnSeq(seq++);
            qesitmRepository.save(qesitm);
        }
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
