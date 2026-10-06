package com.ghlove.member.service.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.security.SecureRandom;

/**
 * 카카오 인증서비스(카카오톡 지갑 본인인증) 연계 - AS-IS KakaoLinkServiceImpl의
 * {@code getToolkitAuth()} / {@code restApiWithToolkit()}에 해당한다.
 *
 * <p>일반 카카오 로그인(OAuth 프로필 조회)이 아니라는 점이 핵심이다. AS-IS는 화면에서
 * {@code Kakao.Auth.authorizeForCert({settleId, signData, identifyItems:'ci,name,birthday,phone_number,gender'})}로
 * 인증을 시작하고, 돌아온 인가코드를 서버가 <b>SSOL 툴킷</b>(중계 API,
 * {@code Authorization: SsolAuth <accessToken>})의 {@code /api/s1/login/request/SAK201}에 넘겨
 * CI·이름·생년월일·휴대폰번호를 받아온다. 카카오 서버를 직접 부르지 않으므로 여기서도 같은
 * 중계 규격을 그대로 쓴다.
 *
 * <p>인가 단계는 화면(JS SDK)이 담당한다 - authorizeForCert가 붙이는 인증서비스 전용
 * 쿼리파라미터 조합은 SDK가 소유하고 있어서 서버가 authorize URL을 직접 조립하지 않는다.
 * 서버는 화면이 SDK를 부를 때 필요한 값(JS 앱키/settleId/redirectUri/identifyItems/signData)을
 * 내려주고({@link CertConfig}), 돌아온 code를 검증한다({@link #verify}).
 */
@Slf4j
public class KakaoCertClient {

    /** 인증 결과에 반드시 있어야 하는 항목 - AS-IS identifyItems와 동일. */
    private static final String DEFAULT_IDENTIFY_ITEMS = "ci,name,birthday,phone_number,gender";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String SIGN_DATA_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final boolean enabled;
    private final String jsKey;
    private final String settleId;
    private final String identifyItems;
    private final String loginRedirectUri;
    private final String joinRedirectUri;
    private final String verifyPath;
    private final String toolkitAccessToken;
    private final RestClient toolkitClient;

    public KakaoCertClient(boolean enabled, String jsKey, String settleId, String identifyItems,
                            String loginRedirectUri, String joinRedirectUri,
                            String toolkitBaseUrl, String toolkitAccessToken, String verifyPath) {
        this.enabled = enabled;
        this.jsKey = jsKey;
        this.settleId = settleId;
        this.identifyItems = (identifyItems == null || identifyItems.isBlank()) ? DEFAULT_IDENTIFY_ITEMS : identifyItems;
        this.loginRedirectUri = loginRedirectUri;
        this.joinRedirectUri = joinRedirectUri;
        this.verifyPath = verifyPath;
        this.toolkitAccessToken = toolkitAccessToken;
        this.toolkitClient = RestClient.create(toolkitBaseUrl == null ? "" : toolkitBaseUrl);
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 카카오 계정 연동해제 (AS-IS kakaoLinkToolkitClear - 카카오싱크 톡키트 unlink).
     * 실연계가 열리면 톡키트 unlink를 호출한다 - 지금은 꺼져 있어(enabled=false) 통과시킨다.
     * @return 연동해제 성공 여부
     */
    public boolean unlink(String kakaoUserKey) {
        if (!enabled) {
            return true;
        }
        // TODO 실연계 개방 시 카카오싱크 톡키트 unlink 호출로 교체
        return true;
    }

    /** 화면이 카카오 JS SDK를 호출하는 데 필요한 값 묶음. signData는 요청마다 새로 만든다. */
    public record CertConfig(String jsKey, String settleId, String redirectUri,
                             String identifyItems, String signData) {}

    /**
     * AS-IS는 회원가입 경유와 로그인 경유에 서로 다른 redirect_uri를 썼다(redirectUri2 / redirectUri).
     * 카카오 콘솔에 등록된 URI와 토큰 교환 시 보내는 URI가 반드시 같아야 하므로 여기서도 두 개를
     * 설정으로 나눠 갖고, 같은 규칙으로 골라 쓴다.
     */
    public CertConfig configFor(boolean joinFlow, String signData) {
        return new CertConfig(jsKey, settleId, redirectUriFor(joinFlow), identifyItems, signData);
    }

    public String newSignData() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(SIGN_DATA_CHARS.charAt(RANDOM.nextInt(SIGN_DATA_CHARS.length())));
        }
        return sb.toString();
    }

    private String redirectUriFor(boolean joinFlow) {
        return joinFlow ? joinRedirectUri : loginRedirectUri;
    }

    /**
     * 인가코드를 SSOL 툴킷에 넘겨 본인확인 결과(CI/이름/생년월일/휴대폰/성별)를 받는다.
     * AS-IS getToolkitAuth()와 같은 파라미터(redirect_uri, code) / 같은 성공조건
     * (status=COMPLETED && result=Y)을 쓴다.
     */
    public ExternalIdentity verify(String code, String signData, boolean joinFlow) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("redirect_uri", redirectUriFor(joinFlow));
        form.add("code", code);

        ToolkitResponse response;
        try {
            response = toolkitClient.post()
                    .uri(verifyPath)
                    .header("Authorization", "SsolAuth " + toolkitAccessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(ToolkitResponse.class);
        } catch (RestClientException e) {
            log.error("카카오 인증서비스 툴킷 호출 실패", e);
            throw new ExternalAuthException("카카오톡 인증 로그인 연결에 실패했습니다.", e);
        }

        // 툴킷은 {status, data:{...}} 로 감싸 주기도 하고 data 내용만 바로 주기도 한다(AS-IS도
        // 두 모양을 모두 받아 처리한다).
        ToolkitData data = response == null ? null : response.effectiveData();
        String status = response == null ? null : response.effectiveStatus();
        if (data == null || !"COMPLETED".equalsIgnoreCase(status) || !"Y".equalsIgnoreCase(data.result())) {
            log.warn("카카오 인증서비스 미완료 응답 - status={}, result={}", status, data == null ? null : data.result());
            throw new ExternalAuthException("카카오톡 인증 로그인에 실패했습니다.");
        }

        // AS-IS는 화면이 만든 signData와 툴킷이 돌려준 request_token 비교를 주석처리해 두었다
        // (툴킷이 access_token을 대신 채워주는 경우가 있어서 그대로 켜면 정상 인증도 막힌다).
        // 여기서도 실패로 처리하지 않고 어긋난 사실만 남긴다 - 실연계 확인 후 판단할 지점.
        String requestToken = data.effectiveRequestToken();
        if (signData != null && requestToken != null && !signData.equals(requestToken)) {
            log.warn("카카오 인증서비스 request_token이 요청한 signData와 다르다(AS-IS도 검증 미적용 상태)");
        }

        String ci = normalizeCi(data.ci());
        if (ci == null || ci.isBlank()) {
            // AS-IS ApiError.KAKAO_LINK_NO_CI
            throw new ExternalAuthException("카카오 계정 정보에 개인식별 정보가 없어 로그인/회원가입이 불가능합니다.");
        }

        ToolkitProfile profile = data.profile() != null
                ? data.profile()
                : new ToolkitProfile(null, null, null, null, null, null, null, null);
        return new ExternalIdentity("KAKAO",
                profile.id() != null ? profile.id() : ci,
                profile.name(),
                profile.email(),
                birthdayOf(profile),
                normalizePhone(profile.effectivePhoneNumber()),
                profile.gender(),
                ci);
    }

    /** 카카오 JS SDK 2.7.2부터 CI 값 앞뒤에 따옴표가 붙어 오는 것을 벗긴다(AS-IS와 동일). */
    private String normalizeCi(String ci) {
        return ci == null ? null : ci.replace("\"", "");
    }

    /** 카카오는 국제표기("+82 10-1234-5678")로 주기도 한다 - AS-IS와 같이 국내표기로 바꾼다. */
    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.replace("+82 ", "0").replace("-", "").replace(" ", "");
    }

    /**
     * 툴킷 경유는 birthday에 yyyyMMdd 8자리가 그대로 오고, 카카오 프로필 직접 조회는
     * birthyear(yyyy) + birthday(MMdd)로 나뉘어 온다 - AS-IS도 두 경우를 나눠 조립했다.
     */
    private String birthdayOf(ToolkitProfile profile) {
        String birthday = profile.birthday();
        if (birthday == null || birthday.isBlank()) {
            return null;
        }
        if (birthday.length() == 8) {
            return birthday;
        }
        if (profile.birthyear() != null && !profile.birthyear().isBlank()) {
            return profile.birthyear() + birthday;
        }
        return birthday;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ToolkitResponse(String status, ToolkitData data, String result,
                           @JsonProperty("request_token") String requestToken,
                           @JsonProperty("access_token") String accessToken,
                           String ci, ToolkitProfile profile) {

        ToolkitData effectiveData() {
            return data != null ? data : new ToolkitData(result, requestToken, accessToken, ci, profile, status);
        }

        String effectiveStatus() {
            if (data != null) {
                return status != null ? status : data.status();
            }
            return status;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ToolkitData(String result,
                       @JsonProperty("request_token") String requestToken,
                       @JsonProperty("access_token") String accessToken,
                       String ci, ToolkitProfile profile, String status) {

        String effectiveRequestToken() {
            return requestToken != null ? requestToken : accessToken;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ToolkitProfile(String id, String name, String email,
                          @JsonProperty("phone_number") String phoneNumber,
                          String phone, String birthyear, String birthday, String gender) {

        /** 툴킷은 phone, 카카오 프로필은 phone_number로 준다(AS-IS KakaoAccount와 동일). */
        String effectivePhoneNumber() {
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                return phoneNumber;
            }
            return phone;
        }
    }
}
