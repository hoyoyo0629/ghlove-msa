package com.ghlove.admin.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 대량 메일발송(EMS) 연계 (AS-IS {@code EmailServiceImpl.sendEmailData}).
 *
 * AS-IS는 {@code ems.host}(운영: http://…:8380/api/sendMail.do)로 아래 모양의 JSON을 POST하고
 * 응답 {@code {"result":{"result":"…"}}}의 result 문자열을 받는다. 여기서는 그 요청 본문과
 * 필드명을 그대로 재현한다 - 수신자는 {@code "메일주소 이름,메일주소 이름,…"} 한 줄이고, 30,000명
 * 단위로 끊어 여러 번 호출하는 것도 AS-IS와 같다(끊는 쪽은 OpEmailService).
 *
 * 다른 외부연계와 동일한 {@code enabled=false} 모크 패턴이다 - EMS는 내부망 전용이라
 * 개발환경에서는 호출하지 않고 로그만 남긴다(성공으로 꾸미지 않고, 발송요청 상태 그대로 둔다).
 */
@Component
@Slf4j
public class EmsMailClient {

    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    private final boolean enabled;
    private final String host;
    private final String sendUserName;
    private final String sendEmail;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmsMailClient(@Value("${ghlove.integrations.ems.enabled}") boolean enabled,
                         @Value("${ghlove.integrations.ems.host}") String host,
                         @Value("${ghlove.integrations.ems.send-username}") String sendUserName,
                         @Value("${ghlove.integrations.ems.send-email}") String sendEmail) {
        this.enabled = enabled;
        this.host = host;
        this.sendUserName = sendUserName;
        this.sendEmail = sendEmail;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * AS-IS sendEmailData와 동일한 본문으로 EMS에 발송 요청을 넘긴다.
     *
     * @param sendType  D:즉시 R:지정 - 즉시면 AS-IS도 sendDate를 빈 문자열로 넘긴다
     * @param sendDate  지정발송 시각 yyyyMMddHHmmss
     * @param recipients "메일주소 이름,메일주소 이름,…"
     * @param memo      AS-IS는 EMAIL_ID를 memo로 넘겨서 EMS 리포트(EMS_REPORT.MEMO)와 연결한다
     * @return EMS 응답의 result 문자열
     */
    public String sendMail(String subject, String content, String sendType, String sendDate,
                           String recipients, String categoryName, String linkName, Object memo)
            throws Exception {
        Map<String, Object> mailDataMap = new LinkedHashMap<>();
        mailDataMap.put("title", subject);
        mailDataMap.put("content", content);
        mailDataMap.put("sendInfo", sendEmail + " " + sendUserName);
        mailDataMap.put("rcvInfo", recipients);
        mailDataMap.put("sendDate", "D".equals(sendType) ? "" : sendDate);
        mailDataMap.put("sendType", sendType);
        mailDataMap.put("categoryNm", categoryName);
        mailDataMap.put("linkNm", linkName);
        mailDataMap.put("memo", memo);

        Map<String, Object> mailMap = new LinkedHashMap<>();
        mailMap.put("data", mailDataMap);
        String jsonValue = objectMapper.writeValueAsString(mailMap);

        if (!enabled) {
            log.info("[ems] disabled - mock 메일발송 요청 subject={} sendType={} recipients={}",
                    subject, sendType, abbreviate(recipients));
            return null;
        }

        URL url = URI.create(host).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        try {
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            http.setRequestProperty("Accept-Charset", "UTF-8");
            http.setConnectTimeout(CONNECT_TIMEOUT_MS);
            http.setReadTimeout(READ_TIMEOUT_MS);

            try (OutputStreamWriter osw = new OutputStreamWriter(http.getOutputStream(), StandardCharsets.UTF_8)) {
                PrintWriter writer = new PrintWriter(osw);
                writer.write(jsonValue);
                writer.flush();
            }

            StringBuilder response = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader br = new BufferedReader(isr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line).append('\n');
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Map<String, String>> result = objectMapper.readValue(response.toString(), Map.class);
            return result.get("result").get("result");
        } finally {
            http.disconnect();
        }
    }

    private static String abbreviate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= 200 ? value : value.substring(0, 200) + "…";
    }
}
