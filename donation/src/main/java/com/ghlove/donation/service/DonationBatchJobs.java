package com.ghlove.donation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.UnpaidDonationBatchRepository;
import com.ghlove.donation.service.integration.LocalTaxClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * donation 소유 배치 - AS-IS ghlove-batch {@code JobServiceImpl}의 기부 관련 job. 키는 AS-IS OP_BATCH_JOB.JOB_METHOD,
 * 실행 시각·사용여부는 관리 화면 설정({@link BatchJobScheduler})이 정한다. 본문은 프록시를 타는 다른 빈을 불러
 * 트랜잭션을 보존한다(self-invocation 금지).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DonationBatchJobs implements BatchJobSource {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final UnpaidDonationBatchRepository unpaidDonationBatchRepository;
    private final DesignatedAdminService designatedAdminService;
    private final DonationBatchService donationBatchService;
    private final LocalTaxClient localTaxClient;
    private final LocgovRepository locgovRepository;
    private final MemberClient memberClient;
    private final com.ghlove.donation.service.integration.NtsClient ntsClient;

    @Override
    public Map<String, Supplier<String>> batchJobs() {
        Map<String, Supplier<String>> jobs = new LinkedHashMap<>();
        // AS-IS ngDonationBatchService.deleteCntrData - "기부 데이터 미결재분(서울시) 삭제"
        jobs.put("deleteCntrDataBatch", () -> {
            log.info("========= deleteCntrData {}", " 서울 미결제분 삭제");
            return "삭제 " + unpaidDonationBatchRepository.deleteSeoulUnpaid() + "건";
        });
        // AS-IS ngDonationBatchService.deleteNotSunapStndBatch - "지방세외(현세대) 미납건 데이터 삭제 처리"
        jobs.put("deleteNotSunapStndBatch", () -> {
            int deleted = unpaidDonationBatchRepository.deleteNotSunapStnd();
            log.info("========= deleteNotSunapStndBatch 지방세외(현세대) 미납건 삭제 {}건", deleted);
            return "삭제 " + deleted + "건";
        });
        // AS-IS designatedDonationService.updateDesinatedDonationStatus - 모금완료·기간만료 특정기부사업 종료
        jobs.put("dsgnBizStatusUpdateBatch", () -> "종료 " + designatedAdminService.updateDesignatedDonationStatus() + "건");
        // AS-IS mainMapper.getGiveTotalAmt + insertDyGramtInfo - 매일 총 기부금(G_DY_GRAMT)
        jobs.put("dailyGramtInsertBatch", donationBatchService::insertDailyGramt);
        // AS-IS cntrNonSunapFiveMinSunapCheck - 5분마다 세외 실시간 수납확인 후 수납처리
        jobs.put("fiveMinuteSunapCheckBatch", () -> sunapCheck(true));
        // AS-IS cntrNonSunapdayCompleteSunapCheck - 23:40 일정산(7일치 재확인)
        jobs.put("dayCompleteSunapCheckBatch", () -> sunapCheck(false));
        // AS-IS id 31/32/33 국세청 전자기부영수증 데이터 처리(online / offline / 이전데이터) - AS-IS에서 중지 상태
        jobs.put("sendNtsEreceiptOnBatch", () -> sendNtsEreceipt("100", false));
        jobs.put("sendNtsEreceiptOffBatch", () -> sendNtsEreceipt("200", false));
        jobs.put("sendNtsEreceiptBatchOld", () -> sendNtsEreceipt(null, true));
        // AS-IS id 2000108 "2023기부금영수증처리" 4분마다 - AS-IS에서 중지 상태
        jobs.put("cntrTaxTempBatch", this::cntrTaxTemp);
        // AS-IS id 2000002 "전날 미수납건 재수납 확인" 1~7시 15분마다 - AS-IS에서 중지 상태
        jobs.put("confirmSunapForDeletedListBatch", this::confirmSunapForDeletedList);
        return jobs;
    }

    /**
     * AS-IS {@code NgDonationBatchServiceImpl.confirmSunapForDeletedList}: 대상마다(전부 서울) 이택스 수납조회 →
     * 응답이 있고 RST_CD 100·SUNAP_YN Y면 수납처리(AS-IS sunapSuccess: 수납일=기부일자·상태 200·삭제여부 N, 포인트는 TO-BE 완료
     * 이벤트), 응답은 있는데 미수납이면 상태 '900'(updateSunapBatchCompleted). 응답이 비면 아무것도 안 한다. 건 사이 1초.
     */
    private String confirmSunapForDeletedList() {
        List<DonationBatchService.DeletedSunapTarget> targets = donationBatchService.deletedSunapTargets();
        int paid = 0;
        int unpaid = 0;
        for (DonationBatchService.DeletedSunapTarget t : targets) {
            try {
                String response = localTaxClient.sendEtaxSunapInfo(t.enapbuNo(), t.jijacheCd(), "02");
                if (!response.isEmpty()) {
                    if (seoulPaid(response)) {
                        donationBatchService.completeSunapForDeleted(t.cntrSn());
                        paid++;
                    } else {
                        donationBatchService.markSunapRecheckedUnpaid(t.enapbuNo());
                        unpaid++;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (RuntimeException e) {
                log.error("confirmSunapForDeletedList {}", e.toString());
            }
        }
        return "대상 " + targets.size() + "건 / 수납 " + paid + "건 / 미수납(900) " + unpaid + "건";
    }

    /**
     * AS-IS {@code NgDonationBatchServiceImpl.cntrTaxTempBatch}: 토큰을 못 받으면 아무것도 안 한다. 받으면 미처리 100건마다
     * CI(표 값, 없으면 회원 CI)·단체 사업자번호를 RSA 암호화 → 암호화 실패면 이력 301(CI)/302(사업자번호) + 처리완료 →
     * 아니면 이력 100(신고) 후 전송(기부금코드·신청구분·관리번호 포함) → 결과 200이면 200, 아니면 300 + 처리완료.
     * 통신 예외는 이력 500만 남기고 처리완료로 바꾸지 않는다(다음 회차 재시도) - 전부 AS-IS 그대로.
     */
    private String cntrTaxTemp() {
        if (!ntsClient.enabled()) {
            log.info("[nts] disabled - 기부금영수증 처리 생략");
            return "연계 꺼짐 - 처리 안 함";
        }
        String token = ntsClient.relayAccessToken();
        if (token == null || token.isEmpty()) {
            return "토큰 없음 - 처리 안 함";
        }
        List<DonationBatchService.TaxTempTarget> targets = donationBatchService.taxTempTargets();
        List<Long> needCi = targets.stream().filter(t -> t.mberCi() == null && t.userId() != null)
                .map(DonationBatchService.TaxTempTarget::userId).distinct().toList();
        Map<Long, String> memberCi = needCi.isEmpty() ? Map.of() : memberClient.ciMap(needCi);
        int ok = 0;
        for (DonationBatchService.TaxTempTarget t : targets) {
            String ci = t.mberCi() != null ? t.mberCi() : memberCi.getOrDefault(t.userId(), "");
            String encryptedCi = ntsClient.rsa(ci);
            String encryptedBizNo = ntsClient.rsa(t.bizNo());
            if (encryptedCi.isEmpty() || encryptedBizNo.isEmpty()) {
                // AS-IS: CI가 비면 301, 사업자번호가 비면 302(둘 다면 302가 이긴다)
                donationBatchService.insertTaxLog(t, encryptedBizNo.isEmpty() ? "302" : "301");
                donationBatchService.markTaxTempSent(t);
                continue;
            }
            donationBatchService.insertTaxLog(t, "100");
            try {
                Map<?, ?> res = ntsClient.sendEreceipt(token, t.sttemntPayDe(), t.conbCd(), t.taxAmt(), encryptedCi,
                        encryptedBizNo, t.elcrAplCd(), t.enapbuNo());
                String resCode = String.valueOf(res.get("res-code"));
                String resMsg = String.valueOf(res.get("res-msg"));
                donationBatchService.updateTaxLog(t, "200".equals(resCode) ? "200" : "300", resCode, resMsg, true);
                ok++;
            } catch (Exception e) {
                donationBatchService.updateTaxLog(t, "500", null, null, false);
                log.error("■■■BATCH ERROR■■■cntrTaxTempBatch API_RESONSE Exception : {}", e.toString());
            }
        }
        return "대상 " + targets.size() + "건 / 전송 " + ok + "건";
    }

    /**
     * AS-IS {@code sendNtsEreceiptOnBatch} / {@code sendNtsEreceiptOffBatch} / {@code sendNtsEreceiptBatchOld}: 대상(CI 있는 100건)
     * → 토큰 → 건마다 CI·단체 사업자번호 RSA 암호화 후 전송 → 이력(GIF_HOMETAX)·기부 결과 메시지 기록, 건 사이 1초 쉼.
     * Old만 암호화 결과가 비면 건너뛴다(AS-IS 그대로). 연계가 꺼져 있으면(enabled=false) 호출·기록하지 않는다
     * (AS-IS에는 로컬 분기가 없어 실패하면 아무것도 남지 않는다).
     */
    private String sendNtsEreceipt(String cntrPathCode, boolean old) {
        List<DonationBatchService.NtsTarget> candidates = donationBatchService.ntsTargets(cntrPathCode);
        Map<Long, String> ci = candidates.isEmpty() ? Map.of()
                : memberClient.ciMap(candidates.stream().map(DonationBatchService.NtsTarget::userId)
                        .filter(java.util.Objects::nonNull).distinct().toList());
        List<DonationBatchService.NtsTarget> targets = candidates.stream()
                .filter(t -> t.userId() != null && ci.containsKey(t.userId())).limit(100).toList();
        if (targets.isEmpty()) {
            return "대상 0건";
        }
        if (!ntsClient.enabled()) {
            log.info("[nts] disabled - 전자기부영수증 전송 생략 {}건", targets.size());
            return "연계 꺼짐 - 대상 " + targets.size() + "건 전송 안 함";
        }
        String accessToken = ntsClient.relayAccessToken();
        int sent = 0;
        for (DonationBatchService.NtsTarget t : targets) {
            String encryptedCi = ntsClient.rsa(ci.get(t.userId()));
            String encryptedBizNo = ntsClient.rsa(t.bizNo());
            if (old && (encryptedCi.isEmpty() || encryptedBizNo.isEmpty())) {
                continue;
            }
            try {
                Map<?, ?> res = ntsClient.sendEreceipt(accessToken, t.sttemntPayDe(), t.taxAmt(), encryptedCi, encryptedBizNo);
                donationBatchService.recordNts(t, encryptedCi, encryptedBizNo, String.valueOf(res.get("res-code")),
                        String.valueOf(res.get("res-msg")));
                sent++;
            } catch (Exception e) {
                log.error("sendNtsEreceipt API_RESONSE Exception eno : {} {}", t.enapbuNo(), e.toString());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return "대상 " + targets.size() + "건 / 전송 " + sent + "건";
    }

    /**
     * AS-IS {@code NgDonationBatchServiceImpl.cntrNonSunapFiveMinSunapCheck} / {@code cntrNonSunapdayCompleteSunapCheck}.
     * 대상마다 서울(SEOUL_TRGET_AT='Y')은 이택스 수납조회(RST_CD 100 + SUNAP_YN Y), 그 외는 차세대 수납이력확인
     * (status가 FAIL·ERROR가 아니면 납부)을 하고, 납부면 수납처리(변경신청 300 등록·승인)한다. 결과와 무관하게
     * 확인한 대상은 SUNAP_PROCSS_YN='Y'. AS-IS처럼 도중 예외가 나면 남은 대상은 다음 회차로 넘긴다.
     * AS-IS가 OP_USER와 INNER JOIN하므로 회원을 찾지 못한 기부는 대상에서 빠진다.
     */
    private String sunapCheck(boolean fiveMinute) {
        List<DonationBatchService.SunapTarget> targets = donationBatchService.sunapTargets(fiveMinute);
        int checked = 0;
        int paid = 0;
        try {
            for (DonationBatchService.SunapTarget t : targets) {
                MemberInfo member = memberClient.fetchOrNull(t.userId());
                if (member == null) {
                    continue;
                }
                boolean isPaid;
                if ("Y".equals(t.seoulTrgetAt())) {
                    isPaid = seoulPaid(localTaxClient.sendEtaxSunapInfo(t.enapbuNo(), t.cntrLocgovCode(), "02"));
                } else {
                    String sgbCd = locgovRepository.findById(t.cntrLocgovCode()).map(Locgov::getAdministInsttCode).orElse(null);
                    Map<String, Object> result = localTaxClient.sendLocalSunapConfirm(t.enapbuNo(), sgbCd, t.cntrSn());
                    String status = result.get("status") == null ? "" : String.valueOf(result.get("status"));
                    if ("FAIL".equals(status)) {
                        log.error("■■■debug■■■ 세외수입이 없는 수납자료 입니다. ::  {}", t.cntrSn());
                        isPaid = false;
                    } else if ("ERROR".equals(status)) {
                        log.error("■■■debug■■■ 수납결과 확인 중 오류가 발생했습니다.. ::  {} , {}", t.cntrSn(), result.get("errorMsg"));
                        isPaid = false;
                    } else {
                        isPaid = true;
                    }
                }
                if (isPaid) {
                    donationBatchService.processSunapSuccess(t, member.loginId(), member.userName());
                    paid++;
                }
                donationBatchService.markSunapChecked(t);
                checked++;
            }
        } catch (RuntimeException e) {
            log.error("■■■ERROR■■■ {} RuntimeException {}", fiveMinute ? "cntrNonSunapFiveMinSunapCheck" : "cntrNonSunapdayCompleteSunapCheck", e.toString());
        }
        return "대상 " + targets.size() + "건 / 확인 " + checked + "건 / 수납처리 " + paid + "건";
    }

    /** AS-IS: 응답이 비면 "수납확인에 실패", RST_CD=100 이고 ARR_RESULT[0].SUNAP_YN=Y면 납부. */
    private static boolean seoulPaid(String apiResponse) {
        if (apiResponse == null || apiResponse.isEmpty()) {
            log.error("■■■debug■■■ 수납확인에 실패하였습니다. ");
            return false;
        }
        try {
            JsonNode root = JSON.readTree(apiResponse);
            JsonNode first = root.path("ARR_RESULT").path(0);
            return "100".equals(root.path("RST_CD").asText("")) && "Y".equals(first.path("SUNAP_YN").asText(""));
        } catch (Exception e) {
            log.error("■■■debug■■■ 수납확인 응답 해석 실패 : {}", e.toString());
            return false;
        }
    }
}
