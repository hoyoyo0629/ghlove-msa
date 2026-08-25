package com.ghlove.admin.web;

import com.ghlove.admin.domain.BatchJob;
import com.ghlove.admin.repository.BatchJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/** 배치 작업 스케줄러 등록 (AS-IS opmanager/batch-job) - 실행 메서드명+주기 등록/관리.
 *  실제 동적 스케줄러 엔진은 없다(각 서비스의 @Scheduled로 이미 개별 구현된 것과 별개). */
@Controller
@RequiredArgsConstructor
public class BatchJobController {

    private static final String STATUS_RUNNING = "1";
    private static final String STATUS_STOPPED = "2";

    private final BatchJobRepository batchJobRepository;

    @GetMapping("/batch-job")
    public String list(Model model) {
        model.addAttribute("jobs", batchJobRepository.findAllByOrderByOrderingAscBatchJobIdAsc());
        return "batch-job/list";
    }

    @GetMapping("/batch-job/new")
    public String createForm(Model model) {
        model.addAttribute("job", new BatchJob());
        return "batch-job/form";
    }

    @GetMapping("/batch-job/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("job", batchJobRepository.findById(id).orElseThrow());
        return "batch-job/form";
    }

    @PostMapping("/batch-job")
    public String create(BatchJob form) {
        form.setBatchJobId(null);
        form.setBatchStatus(STATUS_STOPPED);
        form.setBatchApplyFlag("0");
        batchJobRepository.save(form);
        return "redirect:/batch-job";
    }

    @PostMapping("/batch-job/{id}")
    public String update(@PathVariable Integer id, BatchJob form) {
        BatchJob job = batchJobRepository.findById(id).orElseThrow();
        job.setJobName(form.getJobName());
        job.setJobMethod(form.getJobMethod());
        job.setTriggerType(form.getTriggerType());
        job.setTriggerRepeatSeconds(form.getTriggerRepeatSeconds());
        job.setTriggerCronExpression(form.getTriggerCronExpression());
        job.setOrdering(form.getOrdering());
        batchJobRepository.save(job);
        return "redirect:/batch-job";
    }

    @PostMapping("/batch-job/{id}/toggle")
    public String toggle(@PathVariable Integer id) {
        BatchJob job = batchJobRepository.findById(id).orElseThrow();
        job.setBatchStatus(STATUS_RUNNING.equals(job.getBatchStatus()) ? STATUS_STOPPED : STATUS_RUNNING);
        batchJobRepository.save(job);
        return "redirect:/batch-job";
    }

    @PostMapping("/batch-job/{id}/delete")
    public String delete(@PathVariable Integer id) {
        batchJobRepository.deleteById(id);
        return "redirect:/batch-job";
    }
}
