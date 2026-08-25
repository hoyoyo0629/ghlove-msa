package com.ghlove.admin.web;

import com.ghlove.admin.service.NhExportBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class NhExportBatchController {

    private final NhExportBatchService nhExportBatchService;

    @GetMapping("/batch/nh-export")
    public String form(Model model) {
        model.addAttribute("history", nhExportBatchService.history());
        return "batch/nh-export";
    }

    @PostMapping("/batch/nh-export")
    public String run(Model model) {
        int count = nhExportBatchService.runExportBatch();
        model.addAttribute("resultCount", count);
        model.addAttribute("history", nhExportBatchService.history());
        return "batch/nh-export";
    }
}
