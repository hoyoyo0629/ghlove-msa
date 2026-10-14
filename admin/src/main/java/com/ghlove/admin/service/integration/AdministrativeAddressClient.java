package com.ghlove.admin.service.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ghlove.admin.service.OffgiveClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 행정정보공동이용센터 주소조회 - AS-IS {@code OffgiveManagerController.getUserAddressInfo}
 * ({@code outconn.admin-addressinfo-url}, 내국인)와 {@code NgDonationRelayServiceImpl.getForeignApiData}
 * ({@code outconn.in-link-url}/api/mopas/rsgstadresinfoForeigner, 외국인 거소) 통신 부분.
 *
 * <p>로컬 우회는 이 클래스가 아니라 호출부(컨트롤러)에 있다 - AS-IS와 같은 조건(요청 URL에 localhost)·
 * 같은 값으로 이 클래스를 부르지 않고 바로 응답한다. 그 밖에는 여기서 실제로 호출한다.
 */
@Component
@Slf4j
public class AdministrativeAddressClient {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HHmmss");
    /** AS-IS TIMEOUT_VALUE(10초) / setReadTimeout(30000). */
    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 30000;
    /** AS-IS SUC_RST_CD - 법무부 조회 성공 코드. */
    private static final String SUC_RST_CD = "SUC.WS.000";

    private static final ObjectMapper JSON = new ObjectMapper();

    private final String url;
    private final String inLinkUrl;
    private final OffgiveClient offgiveClient;

    public AdministrativeAddressClient(@Value("${ghlove.integrations.administrative-addressinfo.url}") String url,
                                        @Value("${ghlove.integrations.administrative-addressinfo.in-link-url}") String inLinkUrl,
                                        OffgiveClient offgiveClient) {
        this.url = url;
        this.inLinkUrl = inLinkUrl;
        this.offgiveClient = offgiveClient;
    }

    /**
     * 내국인 주소간단조회 - 요청 {@code {comReqDt, comReqTm, id(주민번호), name}}, 응답을 그대로 돌려준다
     * ({@code serviceResult} 1성공/2주민번호오류/3성명오류/99미등록기관, {@code hangkikcd}, {@code juso}).
     */
    public Map<String, Object> lookup(String juminNo, String userName) throws IOException {
        Map<String, Object> reqParamMap = new HashMap<>();
        reqParamMap.put("comReqDt", LocalDateTime.now().format(YMD));
        reqParamMap.put("comReqTm", LocalDateTime.now().format(HMS));
        reqParamMap.put("id", juminNo);
        reqParamMap.put("name", userName);
        return toMap(JSON.readTree(postJson(url, reqParamMap, false)));
    }

    /**
     * AS-IS {@code getForeignApiData(requestParam, checkLocgovCode)} 그대로 - 결과 Map 키:
     * {@code rst}(SUCCESS / IN_LOCGOV(기부 지자체가 거소지이거나 그 상위) / EXPIRED(체류만료)),
     * {@code locgovCode}, {@code bassAdres}, {@code adressData}({"juso":주소}), {@code foreignStatusCode},
     * 또는 {@code errCode}/{@code errMsg}.
     */
    public Map<String, Object> getForeignApiData(Map<String, Object> requestParam, String checkLocgovCode)
            throws IOException {
        String response = postJson(inLinkUrl + "/api/mopas/rsgstadresinfoForeigner", requestParam, true);
        Map<String, Object> result = new HashMap<>();

        String bassAdres;

        if (response != null && !response.isEmpty()) {
            JsonNode responseData = JSON.readTree(response);

            int responseCode = responseData.get("responseCode").asInt();

            if (HttpURLConnection.HTTP_OK == responseCode) {

                JsonNode data = getDataJsonObject(responseData.get("body").asText());

                if (data == null) {
                    result.put("errCode", "NONE_DATA");
                    result.put("errMsg", "조회된 정보가 없습니다.");
                    return result;
                }

                String rstCd = data.get("mojResultCode").asText();
                if (SUC_RST_CD.equals(rstCd)) {

                    String frgnrSttusSe = responseData.get("frgnrSttusSe").asText();

                    JsonNode addressInfo;
                    String locgovCheckCode;
                    String locgovCode;
                    String upperLocgovCode;
                    String address;
                    ObjectNode jusoInfo;

                    LocalDate now = LocalDate.now();

                    switch (frgnrSttusSe) {
                        case "1" -> {        // 등록외국인
                            String stayQualfEndDate = data.get("stayQualfEndDate").asText();        // 만료일?
                            if (stayQualfEndDate.isEmpty()) {
                                stayQualfEndDate = "99991231";
                            }
                            LocalDate checkDate = LocalDate.parse(stayQualfEndDate, YMD);

                            if (now.isAfter(checkDate)) {        // 현재 날짜가 만료일 이후 기부 불가
                                result.put("rst", "EXPIRED");
                            } else {
                                addressInfo = data.get("stayAreaInfoResponse").get("stayAreaInfoList").get(0);
                                locgovCheckCode = addressInfo.get("admnstmachCddr").asText();

                                locgovCode = convertAdmCdToLocgovCode(locgovCheckCode);

                                upperLocgovCode = locgovCode.substring(0, 2) + "000";
                                address = addressInfo.get("adres").asText();

                                result.put("locgovCode", locgovCode);

                                // 기부 지자체 코드가 같거나 기부 지자체가 기부자 지자체의 상위 지자체이면 기부 불가
                                if (locgovCode.equals(checkLocgovCode) || upperLocgovCode.equals(checkLocgovCode)) {
                                    result.put("rst", "IN_LOCGOV");
                                } else {
                                    result.put("rst", "SUCCESS");
                                }

                                bassAdres = bassAdresOf(locgovCode);
                                result.put("bassAdres", bassAdres);

                                // 이름, 주민번호 등 값은 화면에서 쓰지 않아서 주소만 추가전송
                                jusoInfo = JSON.createObjectNode();
                                jusoInfo.put("juso", address);
                                result.put("adressData", jusoInfo.toString());
                            }
                        }
                        case "2" -> {        // 재외국민
                            addressInfo = data.get("ovrsekrnOndssResponse").get("ovrsekrnOndssInfoList").get(0);
                            locgovCheckCode = addressInfo.get("admnstmachCddr").asText();
                            locgovCode = convertAdmCdToLocgovCode(locgovCheckCode);
                            upperLocgovCode = locgovCode.substring(0, 2) + "000";
                            address = addressInfo.get("adres").asText();

                            if (locgovCode.equals(checkLocgovCode) || upperLocgovCode.equals(checkLocgovCode)) {
                                result.put("rst", "IN_LOCGOV");
                            } else {
                                result.put("rst", "SUCCESS");
                            }

                            result.put("locgovCode", locgovCode);

                            bassAdres = bassAdresOf(locgovCode);
                            result.put("bassAdres", bassAdres);

                            jusoInfo = JSON.createObjectNode();
                            jusoInfo.put("juso", address);
                            result.put("adressData", jusoInfo.toString());
                        }
                        case "3" -> {        // 외국국적동포
                            addressInfo = data.get("frntnKoreanOndssResponse").get("frntnKoreanOndssInfoList").get(0);

                            String stayEndDateStr = null;        // 만료일 체크 추가, 확인 필요

                            if (data.get("stayEndDe") != null) {
                                stayEndDateStr = data.get("stayEndDe").asText();
                            } else if (addressInfo.get("stayEndDe") != null) {
                                stayEndDateStr = addressInfo.get("stayEndDe").asText();
                            }

                            if (stayEndDateStr != null && stayEndDateStr.trim().isEmpty()) {
                                stayEndDateStr = "99991231";
                            }

                            LocalDate stayEndDate = LocalDate.parse(stayEndDateStr, YMD);

                            if (now.isAfter(stayEndDate)) {        // 현재 날짜가 만료일 이후 기부 불가
                                result.put("rst", "EXPIRED");
                            } else {
                                locgovCheckCode = addressInfo.get("admnstmachCddr").asText();

                                locgovCode = convertAdmCdToLocgovCode(locgovCheckCode);
                                upperLocgovCode = locgovCode.substring(0, 2) + "000";
                                address = addressInfo.get("adres").asText();

                                if (locgovCode.equals(checkLocgovCode) || upperLocgovCode.equals(checkLocgovCode)) {
                                    result.put("rst", "IN_LOCGOV");
                                } else {
                                    result.put("rst", "SUCCESS");
                                }

                                result.put("locgovCode", locgovCode);

                                bassAdres = bassAdresOf(locgovCode);
                                result.put("bassAdres", bassAdres);

                                jusoInfo = JSON.createObjectNode();
                                jusoInfo.put("juso", address);
                                result.put("adressData", jusoInfo.toString());
                            }
                        }
                        default -> {
                            log.error("{} rsgstadresinfoForeigner frgnrSttusSe is invalid data :: {}",
                                    getClass().getName(), frgnrSttusSe);
                            throw new IOException("거소 정보 조회 중 문제가 발생했습니다.");
                        }
                    }
                    result.put("foreignStatusCode", frgnrSttusSe);
                } else {
                    putError(result, responseData);
                }
            } else {
                putError(result, responseData);
            }
        } else {
            result.put("errCode", "EMPTY_RESPONSE");
            result.put("errMsg", "조회된 정보가 없습니다.");
        }

        return result;
    }

    /** AS-IS: body가 객체일 때 message.errorCode/errorMessage (Gson getAsJsonObject - 객체가 아니면 예외). */
    private static void putError(Map<String, Object> result, JsonNode responseData) {
        JsonNode body = responseData.get("body");
        if (body == null || !body.isObject()) {
            throw new IllegalStateException("Not a JSON Object: " + body);
        }
        JsonNode errObj = body.get("message");
        result.put("errCode", errObj.get("errorCode").asText());
        result.put("errMsg", errObj.get("errorMessage").asText());
    }

    /** AS-IS getDataJsonObject - body 문자열 JSON의 data 배열 첫 원소, 비었으면 null. */
    private static JsonNode getDataJsonObject(String jsonString) throws IOException {
        JsonNode array = JSON.readTree(jsonString).get("data");
        return array != null && array.size() > 0 ? array.get(0) : null;
    }

    /** AS-IS convertAdmCdToLocgovCode - 행정기관코드 앞 5자리로 G_ADM_LOCGOV 조회. */
    private String convertAdmCdToLocgovCode(String locgovCheckCode) {
        return offgiveClient.locgovCodeByAdmCd(locgovCheckCode.substring(0, 5));
    }

    /** AS-IS ngDonationMapper.getLocGovInfo(locgovCode).getBassAdres(). */
    private String bassAdresOf(String locgovCode) {
        Map<String, Object> view = offgiveClient.locgovView(locgovCode);
        return view.get("bassAdres") == null ? null : view.get("bassAdres").toString();
    }

    private static Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        node.fields().forEachRemaining(e -> map.put(e.getKey(), e.getValue().isValueNode()
                ? (e.getValue().isNull() ? null : e.getValue().asText()) : e.getValue().toString()));
        return map;
    }

    /**
     * AS-IS restFulToRelayServer(Https)/getUserAddressInfo의 HttpURLConnection 호출 - JSON POST
     * (연결 10초/읽기 30초). trustAll이면 AS-IS SSLSocketFactoryMaker처럼 인증서·호스트명 검증 없이 붙는다.
     */
    private static String postJson(String url, Map<String, Object> body, boolean trustAll) throws IOException {
        HttpURLConnection http = (HttpURLConnection) new URL(url).openConnection();
        try {
            if (trustAll && http instanceof HttpsURLConnection https) {
                https.setSSLSocketFactory(trustAllContext().getSocketFactory());
                https.setHostnameVerifier((hostname, session) -> true);
            }
            http.setConnectTimeout(CONNECT_TIMEOUT);
            http.setReadTimeout(READ_TIMEOUT);
            http.setDefaultUseCaches(false);
            http.setDoInput(true);
            http.setDoOutput(true);
            http.setRequestMethod("POST");
            http.setRequestProperty("Content-Type", "application/json");
            try (OutputStreamWriter osw = new OutputStreamWriter(http.getOutputStream(), StandardCharsets.UTF_8)) {
                osw.write(JSON.writeValueAsString(body));
                osw.flush();
            }
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        } finally {
            http.disconnect();
        }
    }

    private static SSLContext trustAllContext() throws IOException {
        try {
            TrustManager[] trustAll = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, trustAll, new SecureRandom());
            return sslContext;
        } catch (GeneralSecurityException e) {
            throw new IOException("SSL 초기화 실패", e);
        }
    }
}
