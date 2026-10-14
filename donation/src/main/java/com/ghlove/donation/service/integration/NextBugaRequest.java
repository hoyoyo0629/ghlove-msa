package com.ghlove.donation.service.integration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 지방세외수입 <b>차세대</b> 부과요청 전문 - AS-IS {@code NextBugaRequestDto} +
 * {@code NgDonationRelayServiceImpl.nextBugaRequest()}가 채워 넣는 고정값을 그대로 옮긴 것이다.
 *
 * <p>AS-IS가 서버에서 직접 채우는 값(화면 입력이 아니다):
 * <ul>
 *   <li>{@code sgbCd} = 지자체의 행정기관코드(G_LOCGOV.ADMINIST_INSTT_CODE)</li>
 *   <li>{@code dptCd} = 처리부서코드에서 <b>뒤 4자리를 뗀</b> 값(11자리 → 7자리)</li>
 *   <li>{@code spclFisBizCd} = 회계구분이 51·61(특별회계)이면 <b>7092</b>, 그 외 <b>0000</b>
 *       (AS-IS 주석: "2024.03.04 개발원 요청")</li>
 *   <li>{@code fyr} = 올해(yyyy), {@code actSeCd} = 회계구분</li>
 *   <li>{@code rprsTxmCd} = "224102", {@code operItemCd} = "000"</li>
 *   <li>{@code lvyYmd}/{@code frstPidYmd} = 오늘(yyyyMMdd)</li>
 *   <li>{@code pyrSeCd} = "01", {@code rprsPyrNo}/{@code rprsPyrNm} = 빈 값, {@code pyrSttCd} = "10"</li>
 *   <li>{@code lotnoRoadAddrSeCd} = "02", {@code mngItemCn1} = "고향사랑기부금"</li>
 *   <li>{@code dsgnDntnBizId} = 특정사업 ID(prjId)</li>
 * </ul>
 * 화면(form.jsp {@code nextBugaRequest()})이 보내는 값: frstPctAmt·pyrNo·pyrNm·roadNmCd·bmno·bsno·zip·
 * dongCd·roadNmDaddr·glNm("고향사랑기부금")·cntrLocgovCode·presentType·prjId.
 *
 * <p>필드 이름은 {@code donation.g_next_buga_request}(1415 부과연계 로그 화면이 읽는 표)의
 * 컬럼과 1:1로 맞춘다 - AS-IS도 연계서버가 이 전문을 그 표에 그대로 적재한다.
 */
public record NextBugaRequest(
        String linkMngKey,
        String sgbCd,
        String linkTrgtCd,
        String dptCd,
        String spclFisBizCd,
        String fyr,
        String actSeCd,
        String rprsTxmCd,
        String operItemCd,
        String lvyYmd,
        String frstPctAmt,
        String frstPidYmd,
        String pyrSeCd,
        String pyrNo,
        String pyrNm,
        String rprsPyrNo,
        String rprsPyrNm,
        String pyrSttCd,
        String lotnoRoadAddrSeCd,
        String zip,
        String roadNmCd,
        String bmno,
        String bsno,
        String stdgCd,
        String dongCd,
        String roadNmDaddr,
        String glNm,
        String mngItemCn1,
        String dsgnDntnBizId,
        /** 100 온라인 / 200 오프라인 - AS-IS는 <b>온라인일 때만</b> 성공 후 G_CNTR을 적재한다. */
        String cntrPathCode,
        Long userId,
        String cntrLocgovCode,
        String presentType,
        String prjId) {

    /**
     * 연계서버로 보내는 JSON 본문 - AS-IS는 {@code objectMapper.convertValue(nextBugaRequestDto, Map.class)}로
     * DTO <b>전체 필드</b>를 Map으로 바꿔 보낸다(채우지 않은 필드는 null, long은 0). 같은 키 집합을 만든다.
     */
    public Map<String, Object> toPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sgbCd", sgbCd);
        payload.put("linkTrgtCd", linkTrgtCd);
        payload.put("linkMngKey", linkMngKey);
        payload.put("dptCd", dptCd);
        payload.put("spclFisBizCd", spclFisBizCd);
        payload.put("fyr", fyr);
        payload.put("actSeCd", actSeCd);
        payload.put("rprsTxmCd", rprsTxmCd);
        payload.put("operItemCd", operItemCd);
        payload.put("lvyYmd", lvyYmd);
        payload.put("frstPctAmt", frstPctAmt);
        payload.put("frstPidYmd", frstPidYmd);
        payload.put("lvySeCd", null);
        payload.put("untySeCd", null);
        payload.put("ntntaxAmt", null);
        payload.put("prvtxAmt", null);
        payload.put("curprcAmt", null);
        payload.put("vatAmt", null);
        payload.put("pyrSeCd", pyrSeCd);
        payload.put("pyrNo", pyrNo);
        payload.put("pyrNm", pyrNm);
        payload.put("rprsPyrNo", rprsPyrNo);
        payload.put("rprsPyrNm", rprsPyrNm);
        payload.put("cnpc", null);
        payload.put("cnpcSeCd", null);
        payload.put("pyrEmlAddr", null);
        payload.put("pyrSttCd", pyrSttCd);
        payload.put("lotnoRoadAddrSeCd", lotnoRoadAddrSeCd);
        payload.put("zip", zip);
        payload.put("roadNmCd", roadNmCd);
        payload.put("addrUdgdYn", null);
        payload.put("bmno", bmno);
        payload.put("bsno", bsno);
        payload.put("stdgCd", stdgCd);
        payload.put("dongCd", dongCd);
        payload.put("addrMtnYn", null);
        payload.put("mno", null);
        payload.put("sno", null);
        payload.put("spclDg", null);
        payload.put("spclHo", null);
        payload.put("spclAddr", null);
        payload.put("roadNmAddr", null);
        payload.put("roadNmDaddr", roadNmDaddr);
        payload.put("roadNmAlAddr", null);
        payload.put("lotnoAddr", null);
        payload.put("lotnoDaddr", null);
        payload.put("lotnoAlAddr", null);
        payload.put("lvyTrgtSeCd", null);
        payload.put("glNm", glNm);
        payload.put("glLotnoRoadAddrSeCd", null);
        payload.put("glZip", null);
        payload.put("glRoadNmCd", null);
        payload.put("glAddrUdgdYn", null);
        payload.put("glBmno", null);
        payload.put("glBsno", null);
        payload.put("glStdgCd", null);
        payload.put("glDongCd", null);
        payload.put("glMtnYn", null);
        payload.put("glMno", null);
        payload.put("glSno", null);
        payload.put("glSpclDG", null);
        payload.put("glSpclHo", null);
        payload.put("glSpclAddr", null);
        payload.put("glRoadNmAddr", null);
        payload.put("glRoadNmDaddr", null);
        payload.put("glRoadNmAlAddr", null);
        payload.put("glLotnoAddr", null);
        payload.put("glLotnoDaddr", null);
        payload.put("glLotnoAlAddr", null);
        payload.put("mngItemCn1", mngItemCn1);
        payload.put("mngItemCn2", null);
        payload.put("mngItemCn3", null);
        payload.put("mngItemCn4", null);
        payload.put("mngItemCn5", null);
        payload.put("mngItemCn6", null);
        payload.put("rmCn", null);
        payload.put("rsveItem1", null);
        payload.put("rsveItem2", null);
        payload.put("rsveItem3", null);
        payload.put("rsveItem4", null);
        payload.put("rsveItem5", null);
        payload.put("cntrPathCode", cntrPathCode);
        payload.put("locgovCode", null);
        payload.put("cntrLocgovCode", cntrLocgovCode);
        payload.put("psitnLocgovCode", null);
        payload.put("userId", userId == null ? 0L : userId);
        payload.put("epayNo", null);
        payload.put("presentType", presentType);
        payload.put("foreignStatusCode", null);
        payload.put("prjId", prjId);
        payload.put("dsgnDntnBizId", dsgnDntnBizId == null || dsgnDntnBizId.isBlank() ? 0L : Long.valueOf(dsgnDntnBizId));
        return payload;
    }
}
