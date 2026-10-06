package com.ghlove.admin.web;

import com.ghlove.admin.domain.BatchJob;
import com.ghlove.admin.repository.BatchJobRepository;
import com.ghlove.admin.web.support.BatchJobParam;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch Job - AS-IS saleson.shop.batchjob.BatchJobManagerController(/opmanager/batch-job) 재현.
 * 작업 검색(메서드명/작업명)·트리거 종류·배치 상태로 걸러 11컬럼 목록을 보여주고, 등록·수정은
 * {@code Common.popup} 1250x310 팝업에서 ajax로 저장한다(응답은 {isSuccess} 모양).
 *
 * 표 컬럼은 AS-IS {@code OP_BATCH_JOB}과 동일하다. 예전 TO-BE는 검색·선택삭제·팝업이 없고
 * AS-IS에 없는 배치상태 토글 버튼을 갖고 있었다.
 */
@Controller
@RequiredArgsConstructor
public class BatchJobController {

    private final BatchJobRepository batchJobRepository;

    /** AS-IS batchJobList / searchBatchJobList - GET·POST 동일 동작. AS-IS는 페이저가 없고
     *  건수도 조회된 목록 크기를 그대로 보여준다(목록 전체를 뿌린다). */
    @RequestMapping(value = "/batch-job", method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("batchJobParam") BatchJobParam batchJobParam, Model model) {
        List<BatchJob> batchJobList = batchJobRepository.findAllByOrderByOrderingAscBatchJobIdAsc().stream()
                .filter(j -> batchJobParam.matches(j.getJobMethod(), j.getJobName(),
                        j.getTriggerType(), j.getBatchStatus()))
                .toList();
        model.addAttribute("batchJobList", batchJobList);
        model.addAttribute("totalCount", batchJobList.size());
        return "batch-job/list";
    }

    /** AS-IS getBatchJobCreate(GET create) - 팝업. */
    @GetMapping("/batch-job/create")
    public String createForm(Model model) {
        model.addAttribute("batchJob", new BatchJob());
        return "batch-job/form";
    }

    /** AS-IS sechedulDetailList(GET detail?batchJobId=) - 수정 팝업. */
    @GetMapping("/batch-job/detail")
    public String detailForm(@RequestParam("batchJobId") Integer batchJobId, Model model) {
        model.addAttribute("batchJob", batchJobRepository.findById(batchJobId).orElseThrow());
        return "batch-job/form";
    }

    /** AS-IS postBatchJobCreate(POST create) - ajax. 새 작업은 적용전(0)으로 넣는다. */
    @PostMapping("/batch-job/create")
    @ResponseBody
    public Map<String, Object> create(@ModelAttribute BatchJob form) {
        return run(() -> {
            form.setBatchJobId(null);
            if (form.getBatchApplyFlag() == null) {
                form.setBatchApplyFlag("0");
            }
            batchJobRepository.save(form);
        });
    }

    /** AS-IS update(POST update) - ajax. 팝업이 폼 전체를 serialize해서 보낸다. */
    @PostMapping("/batch-job/update")
    @ResponseBody
    public Map<String, Object> update(@ModelAttribute BatchJob form) {
        return run(() -> {
            BatchJob job = batchJobRepository.findById(form.getBatchJobId()).orElseThrow();
            job.setJobName(form.getJobName());
            job.setJobMethod(form.getJobMethod());
            job.setTriggerType(form.getTriggerType());
            job.setTriggerRepeatSeconds(form.getTriggerRepeatSeconds());
            job.setTriggerCronExpression(form.getTriggerCronExpression());
            job.setBatchStatus(form.getBatchStatus());
            batchJobRepository.save(job);
        });
    }

    /** AS-IS 목록 선택삭제(delete) - op.common.js Common.updateListData. */
    @PostMapping("/batch-job/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam(value = "id", required = false) List<Integer> ids) {
        return run(() -> {
            if (ids != null && !ids.isEmpty()) {
                batchJobRepository.deleteAllById(ids);
            }
        });
    }

    /**
     * AS-IS batchForNh - 목록의 "농협배치테스트" 버튼이 호출한다.
     * AS-IS는 {@code ngDonationBatchService.getNoBugaLocgovList()}(농협 미부과 지자체 조회 배치)를
     * 실행하는데 그 연계는 이 프로젝트에 아직 이식되지 않았다. 성공으로 꾸미지 않고 사유를 알린다
     * (AS-IS에 있는 버튼이므로 화면에서는 그대로 노출한다).
     */
    @PostMapping("/batch-job/batchForNh")
    @ResponseBody
    public Map<String, Object> batchForNh() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", false);
        result.put("errorMessage", "농협 배치 연계(미부과 지자체 조회)는 아직 이식되지 않았습니다.");
        return result;
    }

    private Map<String, Object> run(Runnable action) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            action.run();
            result.put("isSuccess", true);
        } catch (RuntimeException e) {
            result.put("isSuccess", false);
            result.put("errorMessage", e.getMessage());
        }
        return result;
    }
}
