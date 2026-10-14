package com.ghlove.admin.service.integration;

import com.sci.v2.comm.secu.SciSecuManager;
import com.sci.v2.comm.secu.hmac.SciHmac;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 실명인증(AS-IS sci-call.jsp/sci-result.jsp 이식) - 벤더 모듈(sciSecu_v2.jar)이 이 런타임에서 그대로 돌고,
 * 결과 파싱(2단 복호화·위변조 검증·CI/DI 재복호화)이 AS-IS 순서대로 맞는지 확인한다. 사이렌24 응답은
 * 같은 모듈로 AS-IS 결과 모양("암호문/해시/")을 만들어 흉내낸다.
 */
class SciClientTest {

    private static final String REQ_NUM = "123456789";

    private final SciClient sciClient = new SciClient("XTEE001",
            "https://name.siren24.com/servlet/name_check_seed",
            "https://name.siren24.com/servlet/foreign_name_check_seed");

    @Test
    void requestPicksServiceNumberAndForeignUrlLikeAsIs() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/offgive/sci-call");
        request.setScheme("https");
        request.setServerName("www.ilovegohyang.go.kr");
        request.setServerPort(443);

        SciClient.CallForm domestic = sciClient.request(request, "900101", "1234567", "홍길동");
        assertThat(domestic.actionUrl()).isEqualTo("https://name.siren24.com/servlet/name_check_seed");
        assertThat(domestic.retUrl()).isEqualTo("https://www.ilovegohyang.go.kr:443/offgive/sci-result");
        assertThat(domestic.reqInfo()).isNotBlank();

        // 2차 암호문을 풀면 "1차암호문/HMAC/0000000000000000", 1차를 풀면 id/주민/이름/reqNum/srvNo
        SciSecuManager seed = new SciSecuManager();
        String outer = seed.getDec(domestic.reqInfo(), "");
        String encStr = outer.substring(0, outer.indexOf('/'));
        assertThat(outer).isEqualTo(encStr + "/" + SciHmac.HMacEncript(encStr) + "/0000000000000000");
        assertThat(seed.getDec(encStr, "")).startsWith("XTEE001/900101/1234567/").contains("/123456789/001001////");

        SciClient.CallForm foreign = sciClient.request(request, "900101", "5234567", "JOHN");
        assertThat(foreign.actionUrl()).isEqualTo("https://name.siren24.com/servlet/foreign_name_check_seed");
    }

    @Test
    void resultDecryptsAndVerifiesLikeAsIs() {
        SciSecuManager seed = new SciSecuManager();
        String di = seed.getEnc("DI-VALUE-0001", REQ_NUM);
        String ci = seed.getEnc("CI-VALUE-0001", REQ_NUM);
        String inner = REQ_NUM + "/900101/1234567/%ED%99%8D%EA%B8%B8%EB%8F%99/1/" + di + "/" + ci + "/1/XTEE001/KEY/0101";
        String encPara = seed.getEnc(inner, REQ_NUM);
        String retInfo = seed.getEnc(encPara + "/" + seed.getMsg(encPara) + "/", REQ_NUM);

        SciClient.SciResult ok = sciClient.result(retInfo);
        assertThat(ok).isNotNull();
        assertThat(ok.msgChk()).isEqualTo("Y");
        assertThat(ok.result()).isEqualTo("1");
        assertThat(ok.ci()).isEqualTo("CI-VALUE-0001");
        assertThat(ok.di()).isEqualTo("DI-VALUE-0001");

        // 해시가 안 맞으면 위변조(msgChk N) - AS-IS alert("비정상적인 접근입니다.!!N")
        String tampered = seed.getEnc(encPara + "/WRONGHASH/", REQ_NUM);
        assertThat(sciClient.result(tampered).msgChk()).isEqualTo("N");

        // 풀 수 없는 값은 AS-IS처럼 예외를 삼키고 아무것도 넘기지 않는다
        assertThat(sciClient.result("not-encrypted")).isNull();
    }
}
