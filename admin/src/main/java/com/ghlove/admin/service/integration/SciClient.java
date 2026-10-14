package com.ghlove.admin.service.integration;

import com.sci.v2.comm.secu.SciSecuManager;
import com.sci.v2.comm.secu.hmac.SciHmac;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * SCI(사이렌24) 실명인증 - AS-IS {@code opmanager/offgive/sci-call.jsp}(요청)와 {@code sci-result.jsp}(결과 수신)의
 * 스크립틀릿을 그대로 옮긴 것이다. 암복호화는 AS-IS와 같은 벤더 모듈({@code sciSecu_v2.jar}의
 * {@link SciSecuManager}·{@link SciHmac})을 쓴다.
 *
 * <p>흐름: 화면이 숨김 iframe으로 {@code POST /offgive/sci-call} → {@link #request}가 만든 암호문을
 * {@code checkForm}으로 사이렌24에 자동 제출 → 사이렌24가 {@code ok_url}(/offgive/sci-result)로 결과를
 * 돌려줌 → {@link #result}가 복호화·위변조 검증 → {@code parent.sciResponse(sciUser)}.
 * 로컬 우회(요청 URL에 localhost)는 호출부가 AS-IS와 같은 조건·같은 값으로 처리한다.
 */
@Component
@Slf4j
public class SciClient {

    /** AS-IS reqNum - "sample 페이지와 result 페이지가 동일하지 않으면 결과페이지 복호화 시 에러". */
    private static final String REQ_NUM = "123456789";
    /** AS-IS exVar - 복호화용 임시필드. */
    private static final String EX_VAR = "0000000000000000";

    private final String id;
    private final String nameCheckUrl;
    private final String foreignNameCheckUrl;

    public SciClient(@Value("${ghlove.integrations.sci.site-id}") String id,
                      @Value("${ghlove.integrations.sci.name-check-url}") String nameCheckUrl,
                      @Value("${ghlove.integrations.sci.foreign-name-check-url}") String foreignNameCheckUrl) {
        this.id = id;
        this.nameCheckUrl = nameCheckUrl;
        this.foreignNameCheckUrl = foreignNameCheckUrl;
    }

    /** sci-call.jsp가 사이렌24로 자동 제출하는 {@code checkForm}의 값. */
    public record CallForm(String actionUrl, String reqInfo, String retUrl) {
    }

    /**
     * sci-result.jsp가 {@code parent.sciResponse(sciUser)}로 넘기는 값 - msgChk가 N이면 위변조.
     * 결과값 메시지(2 불일치/3 없음/...)는 AS-IS처럼 화면 스크립트가 result로 고른다.
     */
    public record SciResult(String msgChk, String result, String ci, String di) {
    }

    /**
     * AS-IS sci-call.jsp - 서비스번호는 접속 주소로 고르고(localhost 002003 / 192.168.100.248 004001 /
     * ilovegohyang.go.kr 001001 / 그 외 002003), 1차 암호화 → HMAC → 2차 암호화. 주민번호 뒷자리 첫
     * 숫자가 5~8이면 외국인 URL로 보낸다.
     */
    public CallForm request(HttpServletRequest request, String jumin1, String jumin2, String name)
            throws UnsupportedEncodingException {
        String srvNo;    // 실명확인 서비스번호
        if (request.getRequestURL().indexOf("localhost") > -1) {
            srvNo = "002003";
        } else if (request.getRequestURL().indexOf("192.168.100.248") > -1) {
            srvNo = "004001";
        } else if (request.getRequestURL().indexOf("ilovegohyang.go.kr") > -1) {
            srvNo = "001001";
        } else {
            srvNo = "002003";
        }

        String encName = URLEncoder.encode(name, "UTF-8");    //utf-8환경인 경우에는 해당방식으로 요청해야함.(개발문서 참고!)

        // 실명확인 결과수신 URL
        String retUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
                + "/offgive/sci-result";
        String actionUrl = nameCheckUrl;    //submit 할 URL(내국인)

        //01. 암호화 모듈 선언
        SciSecuManager seed = new SciSecuManager();

        //02. 1차 암호화
        String reqInfo = id + "/" + jumin1 + "/" + jumin2 + "/" + encName + "/" + REQ_NUM + "/" + srvNo + "////" + EX_VAR;
        String encStr = seed.getEnc(reqInfo, "");

        //03. 위변조 검증 값 생성
        String hmacMsg = SciHmac.HMacEncript(encStr);

        //03. 2차 암호화
        reqInfo = seed.getEnc(encStr + "/" + hmacMsg + "/" + "0000000000000000", "");

        if (jumin2 == null) {
            throw new NullPointerException("주민번호2의 정보가 없습니다.");
        }
        //주민번호2의 1번째자리로 외국인인 경우 체크 submit 할 URL 변경
        if (isForeign(jumin2)) {
            actionUrl = foreignNameCheckUrl;
        }
        return new CallForm(actionUrl, reqInfo, retUrl);
    }

    /**
     * AS-IS sci-result.jsp - retInfo를 reqNum 키로 복호화 → 위변조 검증(getMsg) → 다시 복호화해 10개 필드로
     * 나누고, 중복가입확인정보(DI)·CI연계값을 한 번 더 복호화한다. 결과값 메시지는 AS-IS 그대로
     * (2 불일치 / 3 없음 / 4 오류 / 5 에러 / 7 명의도용방지 / 9 인증시도초과). 예외면 null(AS-IS도 화면에
     * 아무것도 쓰지 않고 로그만 남긴다).
     */
    public SciResult result(String retInfoParam) {
        try {
            String retInfo = retInfoParam != null ? retInfoParam.trim() : "";

            // 1. 암호화 모듈 (jar) Loading
            SciSecuManager sciSecuMg = new SciSecuManager();
            //쿠키에서 생성한 값을 Key로 생성 한다.
            retInfo = sciSecuMg.getDec(retInfo, REQ_NUM);

            // 2.1차 파싱
            int inf1 = retInfo.indexOf("/", 0);
            int inf2 = retInfo.indexOf("/", inf1 + 1);

            String encPara = retInfo.substring(0, inf1);         //암호화된 통합 파라미터
            String encMsg = retInfo.substring(inf1 + 1, inf2);   //암호화된 통합 파라미터의 Hash값

            String encMsg2 = sciSecuMg.getMsg(encPara);
            // 3.위/변조 검증
            String msgChk = encMsg2.equals(encMsg) ? "Y" : "N";
            if ("N".equals(msgChk)) {
                return new SciResult(msgChk, null, null, null);
            }

            // 복호화 및 위/변조 검증
            retInfo = sciSecuMg.getDec(encPara, REQ_NUM);

            int info1 = retInfo.indexOf("/", 0);
            int info2 = retInfo.indexOf("/", info1 + 1);
            int info3 = retInfo.indexOf("/", info2 + 1);
            int info4 = retInfo.indexOf("/", info3 + 1);
            int info5 = retInfo.indexOf("/", info4 + 1);
            int info6 = retInfo.indexOf("/", info5 + 1);
            int info7 = retInfo.indexOf("/", info6 + 1);
            int info8 = retInfo.indexOf("/", info7 + 1);
            int info9 = retInfo.indexOf("/", info8 + 1);
            int info10 = retInfo.indexOf("/", info9 + 1);

            String name = URLDecoder.decode(retInfo.substring(info3 + 1, info4), StandardCharsets.UTF_8);
            String result = retInfo.substring(info4 + 1, info5);
            String discrHash = retInfo.substring(info5 + 1, info6);
            String ciscrHash = retInfo.substring(info6 + 1, info7);

            discrHash = sciSecuMg.getDec(discrHash, REQ_NUM);   //암호화된 중복가입확인정보 한번더 복호화
            ciscrHash = sciSecuMg.getDec(ciscrHash, REQ_NUM);   //암호화된 CI연계값 한번더 복호화
            log.debug("sci-result decoded (name={}, result={}, info10={})", name, result, info10);

            return new SciResult(msgChk, result, ciscrHash, discrHash);
        } catch (Exception ex) {
            log.error("sci-result.jsp :: 위변조 검증 에러", ex);
            return null;
        }
    }

    /** AS-IS isForeign() - 주민번호 뒷자리 첫 숫자가 5~8이면 외국인. */
    public static boolean isForeign(String jumin2) {
        if (jumin2 == null || jumin2.isEmpty()) {
            return false;
        }
        char c = jumin2.charAt(0);
        return c == '5' || c == '6' || c == '7' || c == '8';
    }
}
