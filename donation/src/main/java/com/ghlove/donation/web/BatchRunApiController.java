package com.ghlove.donation.web;

import com.ghlove.donation.service.BatchJobScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** 배치 수동 실행 - AS-IS ghlove-batch {@code ManualBatchController GET /batch/run/{jobName}}. 내부 전용(/api/admin/** 시크릿). */
@RestController
@RequiredArgsConstructor
public class BatchRunApiController {

    private final BatchJobScheduler batchJobScheduler;
    private final com.ghlove.donation.service.DonationBatchService donationBatchService;

    /** admin 농협 연계 배치(userCntrForNhBatch / userCntrLocForNhBatch) 원천 - byLocgov면 기부지자체까지 묶는다. */
    @org.springframework.web.bind.annotation.GetMapping("/api/admin/batch/nh/user-cntr")
    public java.util.List<com.ghlove.donation.service.DonationBatchService.NhUserCntr> nhUserCntr(
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "false") boolean byLocgov) {
        return donationBatchService.nhUserCntr(byLocgov);
    }

    /** admin 농협 연계 배치(locForNhBatch) 원천. */
    @org.springframework.web.bind.annotation.GetMapping("/api/admin/batch/nh/locgovs")
    public java.util.List<com.ghlove.donation.service.DonationBatchService.NhLocgov> nhLocgovs() {
        return donationBatchService.nhLocgovs();
    }

    /** admin 배치 getNoBugaLocgovListBatch가 메일 본문을 만들 때 부른다(최근 3일 기부 없는 지자체). */
    @org.springframework.web.bind.annotation.GetMapping("/api/admin/batch/no-buga-locgovs")
    public java.util.List<com.ghlove.donation.service.DonationBatchService.NoBugaLocgov> noBugaLocgovs() {
        return donationBatchService.noBugaLocgovs();
    }

    @PostMapping("/api/admin/batch/run/{jobMethod}")
    public String run(@PathVariable String jobMethod) {
        try {
            return "Job triggered manually : " + jobMethod + " - " + batchJobScheduler.runNow(jobMethod);
        } catch (RuntimeException e) {
            return "Error triggering job : " + e.getMessage();
        }
    }
}
