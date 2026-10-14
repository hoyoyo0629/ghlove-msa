package com.ghlove.donation.service;

import com.ghlove.donation.domain.Locgov;
import com.ghlove.donation.repository.LevyLinkLogWriteRepository;
import com.ghlove.donation.repository.LocgovRepository;
import com.ghlove.donation.repository.OffgiveFormRepository;
import com.ghlove.donation.service.integration.LocalTaxClient;
import com.ghlove.donation.service.integration.NextBugaRequest;
import com.ghlove.donation.service.integration.SeoulBugaRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 납부(세외수입) 부과요청 연계 - AS-IS
 * {@code NgDonationRelayServiceImpl.nextBugaRequest()}(지방세외 차세대)와
 * {@code OffgiveManagerController.getSdonationCharge()}(서울시 세외)를 이식한 것이다.
 *
 * <p><b>AS-IS 고정값·파생값을 그대로 만든다</b>:
 * <ul>
 *   <li>회계구분({@code fisSp})이 비어 있으면 지자체코드 3~5자리가 '000'이면 '31', 아니면 '41'
 *       (AS-IS {@code getLocgovFisSp}의 CASE 그대로 - 4401 지자체관리 화면의 회계구분 분기와 같은 규칙)</li>
 *   <li>특별회계(51·61)면 특별회계사업코드 '7092', 그 외 '0000'</li>
 *   <li>부서코드({@code dptCd})는 처리부서코드에서 <b>뒤 4자리를 뗀</b> 값</li>
 *   <li>연계관리키는 {@code yyyyMMddHHmmss + 시퀀스}, 서울 대장번호는 전용 시퀀스</li>
 *   <li>서울 시구코드는 지자체의 행정기관코드를 그대로 쓴다(AS-IS {@code getSiguCdSeoul})</li>
 * </ul>
 *
 * <p><b>local 프로파일(AS-IS {@code ServiceType.LOCAL})일 때만</b> AS-IS가 LOCAL에서 하던 것과 <b>같은
 * 모양의 가짜 응답</b>을 만든다 - 전자납부번호 {@code "99" + yyyyMMddHHmmssSSS}, {@code linkRstCd="000"},
 * {@code linkRstMsg="LOCAL FAKE : {전자납부번호}"}. 그 밖의 프로파일은 AS-IS 연계 주소로 실제 호출한다.
 * 가짜 응답일 때는 요청/응답을 연계로그 표에도 적재해 admin 1413·1415 화면에서 흐름을 확인할 수 있게 한다
 * (실연계에서는 상대 시스템이 적재하므로 쓰지 않는다 - {@link LevyLinkLogWriteRepository} 주석 참고).
 *
 * <p><b>AS-IS 결함 1건 - 안전하게 고쳤다</b>: AS-IS는 응답 전자납부번호를
 * {@code linkRstMsg.split(":")[1]}로 꺼내는데, 메시지에 ':'이 없으면
 * {@code ArrayIndexOutOfBoundsException}이 나고 바깥 catch가 그걸 그냥 삼켜 <b>요청이 조용히
 * 실패</b>한다. 여기서는 ':'이 없으면 전자납부번호를 빈 값으로 두고 결과코드만 그대로 돌려준다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LevyRelayService {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YEAR = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter HMS = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    /** AS-IS 고정값. */
    private static final String MNG_ITEM_CN1 = "고향사랑기부금";
    private static final String RPRS_TXM_CD = "224102";
    private static final String OPER_ITEM_CD = "000";
    private static final String PYR_SE_CD = "01";
    private static final String PYR_STT_CD = "10";
    private static final String LOTNO_ROAD_ADDR_SE_CD = "02";
    private static final String SPCL_FIS_BIZ_CD_DEFAULT = "0000";
    private static final String SPCL_FIS_BIZ_CD_SPECIAL = "7092";

    /** 성공 결과코드. */
    private static final String LINK_RST_SUCCESS = "000";

    private final LocgovRepository locgovRepository;
    private final LevyLinkLogWriteRepository levyLinkLogWriteRepository;
    private final LocalTaxClient localTaxClient;
    private final OffgiveFormRepository offgiveFormRepository;

    /** 부과요청 결과 - AS-IS는 Map으로 돌려주고 화면이 epayNo/linkRstCd를 본다. */
    public record BugaResult(String epayNo, String linkRstCd, String linkRstMsg) {

        public boolean success() {
            return LINK_RST_SUCCESS.equals(linkRstCd) && epayNo != null && !epayNo.isBlank();
        }
    }

    /**
     * 기탁서 등록 화면(form.jsp {@code nextBugaRequest()})이 보내는 값 그대로 - AS-IS {@code NextBugaRequestDto}에
     * 바인딩되는 것들(selectedRegionCd·userRegionCd는 DTO에 없어 AS-IS도 버린다).
     */
    public record NextBugaForm(String frstPctAmt, String pyrNo, String pyrNm, String roadNmCd, String bmno,
                               String bsno, String zip, String dongCd, String roadNmDaddr, String glNm,
                               String cntrLocgovCode, String presentType, String prjId, String cntrPathCode) {
    }

    /**
     * 지방세외수입 차세대 부과요청 (AS-IS {@code NgDonationRelayServiceImpl.nextBugaRequest}).
     *
     * <ol>
     *   <li>prjId가 0보다 크면 그 특정사업이 지금 기부 가능한지 본다 - 없으면(기간 밖 포함)
     *       "현재 기부 가능한 특정사업에 기부하기 사업이 아닙니다."(호출부가 errorMessage로 내림),
     *       SUCCESS가 아니면 {@code {resultCode}}만 돌려준다.</li>
     *   <li>서버 고정값·파생값을 채운 전문(DTO 전체 필드)을 보내고, 응답 키 전부 + {@code epayNo}를 돌려준다.</li>
     *   <li>그 밖의 오류(prjId 숫자 아님, 지자체 없음, 통신 실패 등)는 AS-IS처럼 로그만 남기고 빈 결과
     *       - 화면은 "지방세외 시스템 연결에 실패하였습니다."를 띄운다.</li>
     * </ol>
     * AS-IS는 <b>온라인(100)일 때만</b> 성공 후 G_CNTR을 적재한다 - 오프라인(200)은 적재하지 않는다(같다).
     */
    public Map<String, Object> requestNextBuga(NextBugaForm form) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            long prjId = Long.parseLong(form.prjId());
            if (prjId > 0) {
                String resultCode = offgiveFormRepository.designatedDonationResultCode(prjId);
                if (resultCode == null) {
                    throw new DonationException("현재 기부 가능한 특정사업에 기부하기 사업이 아닙니다.");
                }
                if (!"SUCCESS".equals(resultCode)) {
                    result.put("resultCode", resultCode);
                    return result;
                }
            }

            Locgov locgov = locgovRepository.findById(form.cntrLocgovCode()).orElse(null);
            if (locgov == null) {
                log.error("■■■API■■■ nextBugaRequest - 지자체 없음 {}", form.cntrLocgovCode());
                return result;
            }
            String fisSp = effectiveFisSp(locgov);
            String spclFisBizCd = ("51".equals(fisSp) || "61".equals(fisSp))
                    ? SPCL_FIS_BIZ_CD_SPECIAL : SPCL_FIS_BIZ_CD_DEFAULT;
            String today = LocalDate.now().format(YMD);

            NextBugaRequest request = new NextBugaRequest(
                    levyLinkLogWriteRepository.nextLinkMngKey(),
                    locgov.getAdministInsttCode(),
                    localTaxClient.linkTrgtCd(),
                    stripDeptCodeTail(locgov.getProcessDeptCode()),
                    spclFisBizCd,
                    LocalDate.now().format(YEAR),
                    fisSp,
                    RPRS_TXM_CD,
                    OPER_ITEM_CD,
                    today,
                    form.frstPctAmt(),
                    today,
                    PYR_SE_CD,
                    form.pyrNo(),
                    form.pyrNm(),
                    "",
                    "",
                    PYR_STT_CD,
                    LOTNO_ROAD_ADDR_SE_CD,
                    form.zip(),
                    form.roadNmCd(),
                    form.bmno(),
                    form.bsno(),
                    null,
                    form.dongCd(),
                    form.roadNmDaddr(),
                    form.glNm(),
                    MNG_ITEM_CN1,
                    form.prjId(),
                    form.cntrPathCode(),
                    0L,
                    form.cntrLocgovCode(),
                    form.presentType(),
                    form.prjId());

            Map<String, Object> response = localTaxClient.sendNextBuga(request);
            String linkRstCd = response.get("linkRstCd") == null ? "" : response.get("linkRstCd").toString();
            String linkRstMsg = response.get("linkRstMsg") == null ? "" : response.get("linkRstMsg").toString();
            String epayNo = extractEpayNo(linkRstMsg);

            if (localTaxClient.serviceTypeLocal()) {
                // local(가짜 응답)일 때만: 1415 부과연계 로그 화면에서 확인할 수 있게 남긴다
                levyLinkLogWriteRepository.insertNextBuga(request,
                        LINK_RST_SUCCESS.equals(linkRstCd) && !epayNo.isEmpty() ? "100" : "900", linkRstCd, linkRstMsg);
            }
            result.putAll(response);
            result.put("epayNo", epayNo);
        } catch (DonationException e) {
            log.error("■■■API■■■ nextBugaRequest UserException : {}", e.getMessage());
            throw e;
        } catch (IOException | RuntimeException e) {
            log.error("■■■API■■■ nextBugaRequest EXCEPTION", e);
        }
        return result;
    }

    /**
     * 서울시 세외수입 부과정보 등록 (AS-IS getSdonationCharge).
     * 응답의 전자납부번호({@code enapbuNo})를 돌려준다.
     */
    public BugaResult requestSeoulBuga(String locgovCode, String napId, String napNm, BigDecimal amount,
                                        String systemCd, String semokCd, String taxGubun, String sidoCd,
                                        String napGubun, String resideStatus, String mulGubun, String mulNm,
                                        String sysGubun) {
        Locgov locgov = locgovRepository.findById(locgovCode)
                .orElseThrow(() -> new DonationException("존재하지 않는 지자체입니다: " + locgovCode));

        SeoulBugaRequest request = new SeoulBugaRequest(
                LocalDate.now().format(YMD),
                LocalTime.now().format(HMS),
                systemCd,
                locgovCode,
                // AS-IS getSiguCdSeoul - 지자체의 행정기관코드를 시구코드로 쓴다
                locgov.getAdministInsttCode(),
                semokCd,
                LocalDate.now().format(YM),
                taxGubun,
                sidoCd,
                napId,
                napNm,
                napGubun,
                amount == null ? 0 : amount.intValue(),
                resideStatus,
                mulGubun,
                mulNm,
                levyLinkLogWriteRepository.nextSeoulBookNo(),
                sysGubun);

        LocalTaxClient.SeoulResponse response;
        try {
            response = localTaxClient.sendSeoulBuga(request);
        } catch (IOException e) {
            // AS-IS getSdonationCharge: IOException은 로그만 남기고 빈 결과(전자납부번호 없음)로 성공 응답
            log.error("■■■API■■■ getSdonationCharge IOException", e);
            return new BugaResult(null, null, null);
        }

        if (localTaxClient.serviceTypeLocal()) {
            // local(가짜 응답)일 때만: 1413 서울세외 부과연계 로그 화면에서 확인할 수 있게 남긴다
            levyLinkLogWriteRepository.insertSeoulBuga(request, response.enapbuNo(),
                    response.errorCd(), response.errorMsg());
        }
        return new BugaResult(response.enapbuNo(),
                "0".equals(response.errorCd()) ? LINK_RST_SUCCESS : response.errorCd(),
                response.errorMsg());
    }

    /**
     * AS-IS getLocgovFisSp - 회계구분이 비어 있으면 지자체코드 3~5자리가 '000'(광역)이면 '31',
     * 아니면 '41'이다.
     */
    private static String effectiveFisSp(Locgov locgov) {
        String fisSp = locgov.getFisSp();
        if (fisSp != null && !fisSp.isBlank()) {
            return fisSp;
        }
        String code = locgov.getLocgovCode();
        boolean wide = code != null && code.length() >= 5 && "000".equals(code.substring(2, 5));
        return wide ? "31" : "41";
    }

    /** AS-IS: 처리부서코드(11자리)에서 뒤 4자리를 뗀 값. */
    private static String stripDeptCodeTail(String processDeptCode) {
        if (processDeptCode == null || processDeptCode.length() <= 4) {
            return "";
        }
        return processDeptCode.substring(0, processDeptCode.length() - 4);
    }

    /** AS-IS는 linkRstMsg를 ':'로 쪼개 [1]을 전자납부번호로 쓴다 - ':'이 없으면 빈 값. */
    private static String extractEpayNo(String linkRstMsg) {
        if (linkRstMsg == null) {
            return "";
        }
        int idx = linkRstMsg.indexOf(':');
        return idx < 0 ? "" : linkRstMsg.substring(idx + 1).trim();
    }

    /** 호출부 편의 - AS-IS가 Map을 돌려주므로 같은 모양도 제공한다. */
    public static Map<String, Object> toMap(BugaResult result) {
        return Map.of("epayNo", result.epayNo() == null ? "" : result.epayNo(),
                "linkRstCd", result.linkRstCd() == null ? "" : result.linkRstCd(),
                "linkRstMsg", result.linkRstMsg() == null ? "" : result.linkRstMsg());
    }
}
