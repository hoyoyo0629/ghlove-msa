package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonCodeException;
import com.ghlove.admin.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public String overview(Model model) {
        model.addAttribute("codeTypeCounts", commonCodeService.codeTypeCounts());
        return "codes/overview";
    }

    @GetMapping("/{codeType}")
    public String listByType(@PathVariable String codeType, Model model) {
        model.addAttribute("codeType", codeType);
        model.addAttribute("codes", commonCodeService.listByType(codeType));
        return "codes/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String codeType, Model model) {
        model.addAttribute("codeType", codeType);
        return "codes/form";
    }

    @PostMapping
    public String create(@RequestParam String codeType, @RequestParam String id, @RequestParam String label,
                          @RequestParam(required = false) String detail,
                          @RequestParam(required = false) Integer ordering, Model model) {
        try {
            var code = commonCodeService.create(codeType, id, label, detail, ordering);
            return "redirect:/codes/" + code.getCodeType();
        } catch (CommonCodeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("codeType", codeType);
            return "codes/form";
        }
    }

    @GetMapping("/{codeType}/{id}/edit")
    public String editForm(@PathVariable String codeType, @PathVariable String id, Model model) {
        model.addAttribute("code", commonCodeService.get(codeType, id));
        return "codes/edit";
    }

    @PostMapping("/{codeType}/{id}")
    public String update(@PathVariable String codeType, @PathVariable String id, @RequestParam String label,
                          @RequestParam(required = false) String detail,
                          @RequestParam(required = false) Integer ordering) {
        commonCodeService.update(codeType, id, label, detail, ordering);
        return "redirect:/codes/" + codeType;
    }

    @PostMapping("/{codeType}/{id}/toggle")
    public String toggle(@PathVariable String codeType, @PathVariable String id) {
        commonCodeService.toggleUse(codeType, id);
        return "redirect:/codes/" + codeType;
    }
}
