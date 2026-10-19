package com.ghlove.admin.service;

import com.ghlove.admin.domain.OpEmail;
import com.ghlove.admin.web.support.EmailSendTarget;
import com.ghlove.admin.web.support.SendParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 배치 getNoBugaLocgovListBatch("3일 기부현황 내역 없는 지자체", AS-IS 월·목 09:00) -
 * AS-IS {@code NgDonationBatchServiceImpl.getNoBugaLocgovList}: 최근 3일 기부 없는 지자체 목록을 메일로 보낸다
 * (OP_EMAIL 등록 → 개별(E) 대상 즉시(D) 발송). 관리 화면 "농협배치테스트" 버튼도 이 메서드를 부른다.
 *
 * <p>수신자는 AS-IS 코드에 박힌 운영 담당자 8명 그대로다({@link #ASIS_RECIPIENTS}, 사용자 결정 2026-10-15).
 * 로컬(개발) 설정에서는 {@code ghlove.batch.no-buga-mail.test-mode=true}라 대신
 * {@code ghlove.batch.no-buga-mail.test-recipients}("이메일 이름" 목록)로 보내고, 비어 있으면 메일은 등록만 하고 보내지 않는다.
 */
@Service
@Slf4j
public class NoBugaLocgovMailService {

    private static final DateTimeFormatter DOT_DATE = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    /** AS-IS getNoBugaLocgovList의 수신자(주석처리된 사람은 제외, 주소 끝 공백까지 원문 그대로). */
    private static final List<EmailSendTarget> ASIS_RECIPIENTS = List.of(
            new EmailSendTarget("이현민", "rain2199@archivsoft.com"),
            new EmailSendTarget("임승빈", "scorplim@naver.com"),
            new EmailSendTarget("이신우", "leeshinwoo93@archivsoft.com"),
            new EmailSendTarget("조형원", "hyeong3381@archivsoft.com"),
            new EmailSendTarget("신승엽", "lpureall@naver.com"),
            new EmailSendTarget("유하영", "aayu0123@naver.com "),
            new EmailSendTarget("김애경", "akim2810@naver.com"),
            new EmailSendTarget("전표진", "sapiensjiny@naver.com"));

    public record NoBugaLocgov(String upperLocgovCode, String locgovCode, String upperLocgovNm, String locgovNm) {
    }

    private final OpEmailService opEmailService;
    private final RestClient donation;
    private final boolean testMode;
    private final List<String> testRecipients;

    public NoBugaLocgovMailService(OpEmailService opEmailService,
                                   @Value("${ghlove.donation-service.base-url}") String donationBaseUrl,
                                   @Value("${ghlove.internal.admin-secret}") String adminSecret,
                                   @Value("${ghlove.batch.no-buga-mail.test-mode:false}") boolean testMode,
                                   @Value("${ghlove.batch.no-buga-mail.test-recipients:}") List<String> testRecipients) {
        this.opEmailService = opEmailService;
        this.donation = RestClient.builder().baseUrl(donationBaseUrl).defaultHeader("X-Internal-Secret", adminSecret).build();
        this.testMode = testMode;
        this.testRecipients = testRecipients;
    }

    public String send() {
        LocalDate date = LocalDate.now();
        List<NoBugaLocgov> noBugaList = donation.get().uri("/api/admin/batch/no-buga-locgovs").retrieve()
                .body(new ParameterizedTypeReference<List<NoBugaLocgov>>() {
                });
        if (noBugaList == null) {
            noBugaList = List.of();
        }

        OpEmail email = new OpEmail();
        email.setAuthTarget("E");
        email.setFrstRegisterId(0L);
        email.setSubject(date.minusDays(3).format(DOT_DATE) + " ~ " + date.format(DOT_DATE) + " 기부내역이 없는 지자체 목록");
        if (noBugaList.isEmpty()) {
            email.setContent("전 지역 기부 내역 존재");
        } else {
            StringBuilder sb = new StringBuilder();
            StringJoiner codes = new StringJoiner(",");
            int i = 1;
            for (NoBugaLocgov l : noBugaList) {
                sb.append("<p>").append(i).append(". ").append(l.upperLocgovNm()).append(" ").append(l.locgovNm()).append("</p>");
                i++;
                codes.add(l.locgovCode());
            }
            email.setContent(sb + "<br>" + codes);
        }
        email.setSendType("D");
        OpEmail saved = opEmailService.insertEmail(email, null);

        List<EmailSendTarget> recipients = recipients();
        if (recipients.isEmpty()) {
            log.info("[배치] getNoBugaLocgovList - 테스트 모드인데 테스트 수신자가 없어 발송하지 않음(emailId={})", saved.getEmailId());
            return "등록 emailId=" + saved.getEmailId() + " / 대상 지자체 " + noBugaList.size() + "곳 / 발송 안 함(테스트 수신자 없음)";
        }
        SendParam param = new SendParam();
        param.setEmailId(saved.getEmailId());
        param.setSendUserList(new ArrayList<>(recipients));
        boolean ok = opEmailService.sendEmail(param);
        return "emailId=" + saved.getEmailId() + " / 대상 지자체 " + noBugaList.size() + "곳 / 수신 " + recipients.size()
                + "명" + (testMode ? "(테스트)" : "") + (ok ? "" : " / 발송 실패");
    }

    private List<EmailSendTarget> recipients() {
        if (!testMode) {
            return ASIS_RECIPIENTS;
        }
        List<EmailSendTarget> list = new ArrayList<>();
        for (String entry : testRecipients) {
            String s = entry.trim();
            if (s.isEmpty()) {
                continue;
            }
            int sp = s.indexOf(' ');
            list.add(sp < 0 ? new EmailSendTarget(s, s) : new EmailSendTarget(s.substring(sp + 1).trim(), s.substring(0, sp)));
        }
        return list;
    }
}
