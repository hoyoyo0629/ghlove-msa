package com.ghlove.donation.service.integration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 서울시 세외수입시스템 부과정보 등록 전문 - AS-IS
 * {@code OffgiveManagerController.getSdonationCharge()}가 만드는 {@code reqParamMap}을
 * <b>키 순서·고정값·빈 값까지 그대로</b> 옮긴 것이다(약 90개 키).
 *
 * <p>AS-IS 주석을 그대로 남긴 것:
 * <ul>
 *   <li>{@code buseoCd}(부서코드 7자리)·{@code taxNo}(6자리)는 "세외수입시스템에서 자동 채번"이라
 *       빈 값으로 보낸다 - AS-IS 주석은 "개발테스트를 위해 하드코딩"이라고 적혀 있다.</li>
 *   <li>{@code sise}는 병기항목이 아닌 경우 본세와 같은 값이다({@code taxAmt}).</li>
 *   <li>{@code guse}·{@code gukse}·{@code gigum}과 각 이자·가산금은 모두 0이다.</li>
 *   <li>{@code bookNo}는 원천 시스템의 대장번호(유일 key)로 <b>채번이 필요하다</b>
 *       (AS-IS {@code gif_seoul_book_no_seq}).</li>
 *   <li>{@code sysGubun}은 "시스템 고유번호 LVHT (임시코드, 별도요청 없으면 수정없이 사용)".</li>
 *   <li>{@code siguCd}는 지자체의 행정기관코드를 그대로 쓴다(AS-IS {@code getSiguCdSeoul}).</li>
 * </ul>
 *
 * <p>AS-IS는 이 전문을 그대로 서울 세외 OPEN API에 POST하고 응답의 {@code enapbuNo}(전자납부번호)를
 * 받는다. 응답/오류는 {@code donation.gif_seoul}(1413 서울세외 부과연계 로그 화면)에 쌓인다.
 */
public record SeoulBugaRequest(
        String comReqDt,
        String comReqTm,
        String systemCd,
        String jijacheCd,
        String siguCd,
        String semokCd,
        String taxYm,
        String taxGubun,
        String sidoCd,
        String napId,
        String napNm,
        String napGubun,
        int taxAmt,
        String resideStatus,
        String mulGubun,
        String mulNm,
        String bookNo,
        String sysGubun) {

    /** AS-IS reqParamMap - 키 순서와 빈 값/0까지 그대로. */
    public Map<String, Object> toPayload() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("comReqDt", comReqDt);            // 요청일자
        p.put("comReqTm", comReqTm);            // 요청일시
        p.put("systemCd", systemCd);            // 인터페이스 구분코드
        p.put("jijacheCd", jijacheCd);          // 지자체코드

        p.put("siguCd", siguCd);                // 시구코드
        p.put("semokCd", semokCd);              // 세목코드
        p.put("taxYm", taxYm);                  // 과세년월
        p.put("taxGubun", taxGubun);            // 과세구분

        p.put("buseoCd", "");                   // 부서코드 7자리(세외수입시스템에서 자동 채번)
        p.put("taxNo", "");                     // 6자리(세외수입시스템에서 자동 채번)

        p.put("sidoCd", sidoCd);                // 시도코드
        p.put("napId", napId);                  // 납세자 ID
        p.put("napNm", napNm);                  // 납세자명
        p.put("napGubun", napGubun);            // 납세자구분
        p.put("taxAmt", taxAmt);                // 본세합계
        p.put("sise", taxAmt);                  // 병기항목아닌경우 본세

        p.put("guse", 0);                       // 병기항목아닌경우 구세
        p.put("gukse", 0);                      // 병기항목아닌경우 국세
        p.put("gigum", 0);                      // 기금
        p.put("siseIja", 0);                    // 시세이자
        p.put("guseIja", 0);                    // 구세이자
        p.put("gukseIja", 0);                   // 국세이자
        p.put("gigumIja", 0);                   // 기금이자
        p.put("siseGasanAmt", 0);               // 시세가산금
        p.put("guseGasamAmt", 0);               // 구세가산금(AS-IS 키 철자 그대로 - Gasam)
        p.put("gukseGasanAmt", 0);              // 국세가산금
        p.put("gigumGasanAmt", 0);              // 기금가산금

        p.put("napMobilNo", "");                // 납세자휴대폰
        p.put("napTelNo", "");                  // 납세자전화
        p.put("napEmail", "");                  // 납세자이메일

        p.put("resideStatus", resideStatus);    // 거주상태
        p.put("mulGubun", mulGubun);            // 물건구분
        p.put("mulNm", mulNm);                  // 물건명

        p.put("mulOcrSiguCd", "");
        p.put("mulBdongriCd", "");
        p.put("mulSpcCd", "");
        p.put("mulBon", "");
        p.put("mulBu", "");
        p.put("mulTong", "");
        p.put("mulBan", "");
        p.put("mulAptNm", "");
        p.put("mulDong", "");
        p.put("mulHosu", "");
        p.put("mulZipCd", "");
        p.put("mulZipAddr", "");
        p.put("mulDtlAddr", "");
        p.put("hdongCd", "");

        p.put("bookNo", bookNo);                // 원천 시스템의 대장번호(유일 key 값), 중복체크

        p.put("hangmok1", "");
        p.put("hangmok2", "");
        p.put("hangmok3", "");
        p.put("hangmok4", "");
        p.put("hangmok5", "");
        p.put("hangmok6", "");
        p.put("gasanRateGubun", "");
        p.put("specialRate", 0);
        p.put("specialRateApplySayu", "");
        p.put("bigo", "");
        p.put("ocrSiguCd", "");
        p.put("ocrBuseoCd", "");
        p.put("etc1", "");
        p.put("lastWorkId", "");
        p.put("lastWorkDate", "");
        p.put("vatAmt", 0);
        p.put("gasanAmtSkipGubun", "");

        p.put("sysGubun", sysGubun);            // 시스템 고유번호 LVHT (임시코드)

        p.put("napDzipCd", "");
        p.put("napDzipAddr", "");
        p.put("napDdtlAddr", "");
        p.put("napDrefAddr", "");
        p.put("etcCm1", "");
        p.put("etcCm2", "");
        p.put("etcCm3", "");
        p.put("etcCm4", "");
        p.put("etcCm5", "");
        p.put("napBldBon", "");
        p.put("napBldBu", "");
        p.put("napDoroCd", "");
        p.put("napUndYn", "");
        p.put("napbuYmd", "");
        return p;
    }
}
