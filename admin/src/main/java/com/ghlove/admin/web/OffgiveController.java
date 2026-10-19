package com.ghlove.admin.web;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.domain.Manager;
import com.ghlove.admin.service.CntrReqmngClient;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.GiftClient;
import com.ghlove.admin.service.ManagerException;
import com.ghlove.admin.service.MemberClient;
import com.ghlove.admin.service.MenuService;
import com.ghlove.admin.service.OffgiveClient;
import com.ghlove.admin.service.OrderClient;
import com.ghlove.admin.service.integration.AdministrativeAddressClient;
import com.ghlove.admin.service.integration.LevyRelayClient;
import com.ghlove.admin.service.integration.SciClient;
import com.ghlove.admin.web.support.AsisSxssfExcelView;
import com.ghlove.admin.web.support.RsaCryptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 오프라인기부 접수 - 기탁서 등록(메뉴 15102, AS-IS {@code OffgiveManagerController} +
 * {@code opmanager/offgive/form.jsp}). 화면이 부르는 엔드포인트는 AS-IS 경로에서
 * {@code /opmanager}만 뗀 이름 그대로 두고(15101 {@code /offgive/list}와 같은 규칙),
 * 응답도 AS-IS JsonView 모양({@code isSuccess/data/errorMessage})이라 화면 JS를 AS-IS 원문에
 * 가깝게 옮길 수 있다. 실제 데이터/도메인효과는 donation·member·gift에 있다.
 *
 * <p><b>권한</b>: AS-IS {@code GET /create}는 권한을 보지 않고 폼을 그린다. 등록은 화면
 * {@code save()}가 접수은행/지점명이 비면 막고, 서버 {@code POST /create}가
 * {@code getLoginUserAdminRoleCheck()}로 OFF(ROLE_ADMIN_7/8)·WCM(ROLE_ADMIN_11)만 통과시킨다.
 * ROLE_ADMIN_11은 AS-IS에서도 OP_ROLE에 없어 할당 불가능한 죽은 분기라
 * ({@link OffgiveReceiptAdminController} 주석) 7/8만 본다.
 *
 * <p><b>외부연계</b>: SCI 실명인증({@link SciClient})·행정정보공동이용센터({@link AdministrativeAddressClient})는
 * AS-IS 통신 그대로 실제로 호출하고, <b>로컬 테스트에서만</b> AS-IS와 같은 조건(요청 URL에 localhost)·같은
 * 값으로 우회한다. 세외수입 부과요청은 donation이 local 프로파일(AS-IS ServiceType.LOCAL)일 때만 가짜 응답을
 * 만든다. juso.go.kr는 공개 API라 화면이 직접 호출한다(우회 없음).
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class OffgiveController {

    private final OffgiveClient offgiveClient;
    private final MemberClient memberClient;
    private final GiftClient giftClient;
    private final CommonCodeService commonCodeService;
    private final SciClient sciClient;
    private final AdministrativeAddressClient administrativeAddressClient;
    private final LevyRelayClient levyRelayClient;
    private final OrderClient orderClient;
    private final CntrReqmngClient cntrReqmngClient;

    @org.springframework.beans.factory.annotation.Value("${ghlove.gift-service.base-url}")
    private String giftServiceBaseUrl;

    /** AS-IS saleson.url.oz - 납부신청서(OZ) 팝업. */
    @org.springframework.beans.factory.annotation.Value("${ghlove.integrations.oz.url}")
    private String ozUrl;

    private static final String CODE_TYPE_OFF_BANK = "OFF_BANK_LIST";
    private static final String SESSION_FOREIGN_PRIVATE_KEY = "foreignPrivateKey";

    /**
     * 기부금 접수관리 목록은 AS-IS 형태로 분리 이식했다
     * ({@link OffgiveReceiptAdminController} - {@code /offgive/list}, 메뉴 15101).
     * 예전 링크가 깨지지 않게 여기로 들어오면 그 화면으로 보낸다.
     */
    @GetMapping("/offgive")
    public String redirectToList() {
        return "redirect:/offgive/list";
    }

    /** AS-IS {@code GET /create}(createPrj) - 권한 확인 없이 폼을 그린다. */
    @GetMapping("/offgive/new")
    public String newForm(HttpSession session, Model model) {
        Manager viewer = manager(session);
        String bankCode = viewer != null ? viewer.getBankCode() : null;
        String offBankLabel = bankCode != null && !bankCode.isBlank()
                ? commonCodeService.labelsOf(CODE_TYPE_OFF_BANK).getOrDefault(bankCode, "") : "";
        // AS-IS donationVerification.donationLimitAmt().getDetail() - 올해 DONATION_LIMIT_AMT의 detail("2천만")
        String limitAmtString;
        try {
            CommonCode limitAmt = commonCodeService.get("DONATION_LIMIT_AMT", String.valueOf(LocalDate.now().getYear()));
            limitAmtString = limitAmt.getDetail() == null ? "" : limitAmt.getDetail();
        } catch (RuntimeException e) {
            limitAmtString = "";
        }

        model.addAttribute("offBankLabel", offBankLabel);
        model.addAttribute("psitnNm", viewer != null && viewer.getPsitnNm() != null ? viewer.getPsitnNm() : "");
        model.addAttribute("wdr", commonCodeService.labelsOf("WDR"));
        model.addAttribute("phone", commonCodeService.labelsOf("PHONE"));
        model.addAttribute("limitAmtString", limitAmtString);
        model.addAttribute("loginUserId", viewer != null ? viewer.getUserId() : null);
        return "offgive/form";
    }

    /** 예전 상세 경로 - AS-IS 경로({@code /detail/{id}})로 옮겼다. */
    @GetMapping("/offgive/{cntrSn:[0-9A-Za-z]+}")
    public String redirectToDetail(@PathVariable String cntrSn) {
        return "redirect:/offgive/detail/" + cntrSn;
    }

    /**
     * AS-IS {@code GET /detail/{id}}(viewPrj, detail.jsp "기탁서") - {@code offgiveService.getOffgive(id)}와
     * {@code today}(yyyyMMdd). getOffgive 한 줄을 TO-BE에서는 donation(g_cntr·지자체·지정기부·변경신청·포인트)·
     * member(아이디·이름·생년월일·연락처·주소)·order(선택 답례품·배송지)·공통코드 WDR(시도명)에서 모아 같은 이름의
     * 맵으로 만든다. AS-IS 매퍼가 RCEPT_BANK_CODE_NM을 고르지 않아 화면의 접수은행 앞칸은 늘 비는데, 그것도 그대로다.
     */
    @GetMapping("/offgive/detail/{cntrSn}")
    public String detail(@PathVariable String cntrSn, HttpSession session, Model model) {
        Manager viewer = manager(session);
        try {
            OffgiveClient.DetailResult result = offgiveClient.detail(cntrSn);
            OffgiveClient.Detail d = result.offgive();
            if (MenuService.isLocgovScoped(viewer) && !viewer.getLocgovCode().equals(d.locgovCode())) {
                return "redirect:/offgive";
            }
            MemberClient.MemberInfo member = d.userId() != null ? memberClient.fetchOrNull(d.userId()) : null;
            OrderClient.OffgiveOrderSummary order = orderClient.offgiveOrderSummary(cntrSn);

            Map<String, Object> offgive = new HashMap<>();
            offgive.put("cntrSn", d.cntrSn());
            offgive.put("frstRegistPnttm", d.frstRegistPnttm());
            offgive.put("infoAgreAt", d.infoAgreAt());
            offgive.put("upperLocgovCode", d.upperLocgovCode());
            offgive.put("upperLocgovNm", commonCodeService.labelsOf("WDR").get(d.upperLocgovCode()));
            offgive.put("locgovCode", d.locgovCode());
            offgive.put("locgovNm", d.locgovNm());
            offgive.put("cntrAmt", d.cntrAmt());
            offgive.put("cntrAmtYearTotal", d.cntrAmtYearTotal());
            // AS-IS getOffgive는 RCEPT_BANK_CODE_NM을 고르지 않아 화면 앞칸이 늘 비어 있다. 키가 없으면 SpEL이
            // 렌더링을 중단하므로(맵 키 누락) 빈 값으로 둔다.
            offgive.put("rceptBankCodeNm", null);
            offgive.put("rceptBankNm", d.rceptBankNm());
            offgive.put("elctrnPayNo", d.elctrnPayNo());
            offgive.put("cntrPoint", result.cntrPoint());
            offgive.put("cntrUsePoint", result.cntrUsePoint());
            offgive.put("cntrBlcePoint", result.cntrBlcePoint());
            // 화면 스크립트는 AS-IS 숫자코드(100 신고 / 200 수납)로 버튼을 가른다.
            offgive.put("cntrSttusCode", asIsSttusCode(d.cntrSttusCode()));
            offgive.put("reqId", d.reqId());
            offgive.put("loginId", member != null ? member.loginId() : null);
            offgive.put("userName", member != null ? member.userName() : null);
            offgive.put("birthday", member != null ? member.birthday() : null);
            offgive.put("phoneNumber", member != null ? member.phoneNumber() : null);
            offgive.put("post", member != null ? member.post() : null);
            offgive.put("address", member != null ? member.address() : null);
            offgive.put("addressDetail", member != null ? member.addressDetail() : null);
            offgive.put("prjSubject", d.prjSubject());
            offgive.put("options", order != null ? order.options() : null);
            offgive.put("rcvPost", order != null ? order.rcvPost() : null);
            offgive.put("rcvAddress", order != null ? order.rcvAddress() : null);
            offgive.put("rcvAddressDetail", order != null ? order.rcvAddressDetail() : null);
            model.addAttribute("offgive", offgive);
            model.addAttribute("today", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        } catch (ManagerException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "offgive/detail";
    }

    /** TO-BE 기부상태 낱말 → AS-IS CNTR_STTUS_CODE(100 신고 / 200 수납 / 300 과오납). */
    private static String asIsSttusCode(String code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case "REQUESTED" -> "100";
            case "COMPLETED" -> "200";
            case "CANCELLED" -> "300";
            default -> code;
        };
    }

    /**
     * AS-IS {@code POST /offgiveCancel} - 상세의 "기부취소" 버튼. 변경신청(G_CNTR_REQMNG)에 요청분류 100(기부취소)을
     * 넣는다(AS-IS giveReqmngInsert). 화면은 성공이면 "요청되었습니다.", 아니면 "오류가 발생했습니다."만 띄운다.
     */
    @PostMapping("/offgive/offgiveCancel")
    @ResponseBody
    public Map<String, Object> offgiveCancel(@RequestParam Map<String, String> param, HttpSession session) {
        Manager viewer = manager(session);
        try {
            cntrReqmngClient.submitOffgiveCancel(param.get("cntrSn"), param.get("sttemntPayDe"),
                    param.get("discription"), viewer != null ? viewer.getUserId() : null);
            return success(null);
        } catch (RuntimeException e) {
            log.error("기부취소 요청 실패", e);
            return failure("에러가 발생하여 등록처리가 되지 않았습니다. 고객센터에 문의 하세요.");
        }
    }

    /** AS-IS {@code POST /offgiveCancelReq} - 요청된 기부취소 신청을 취소(giveReqmngCancel). 화면 버튼은 AS-IS에서 주석이다. */
    @PostMapping("/offgive/offgiveCancelReq")
    @ResponseBody
    public Map<String, Object> offgiveCancelReq(@RequestParam Map<String, String> param, HttpSession session) {
        Manager viewer = manager(session);
        try {
            cntrReqmngClient.cancel(Long.valueOf(param.get("reqId")), viewer != null ? viewer.getUserId() : null);
            return success(null);
        } catch (RuntimeException e) {
            log.error("기부취소 신청취소 실패", e);
            return failure("에러가 발생하여 등록처리가 되지 않았습니다. 고객센터에 문의 하세요.");
        }
    }

    /** AS-IS {@code GET /popup/form-apply/{cntrSn}}(납부신청서, layout base) - OZ 뷰어로 6.cntr_application_form_prj.ozr. */
    @GetMapping("/offgive/popup/form-apply/{cntrSn}")
    public String formApply(@PathVariable String cntrSn, Model model) {
        model.addAttribute("ozUrl", ozUrl);
        model.addAttribute("cntrSn", cntrSn);
        return "offgive/form-apply";
    }

    // ==================== 실명인증 (AS-IS sci-call → iframe → parent.sciResponse) ====================

    /**
     * AS-IS {@code POST sci-call}(sci-call.jsp) - 숨김 iframe이 받는 페이지. 암호화한 요청을 사이렌24로 자동
     * 제출하고, 결과는 {@code /offgive/sci-result}로 돌아온다.
     *
     * <p>로컬 우회는 AS-IS와 같은 조건·같은 값: localhost 접근이면 입력한 주민번호를 그대로 인정하는 성공
     * 결과(CI/DI = "MOCKCI"/"MOCKDI"+주민번호)로 parent.sciResponse()를 바로 부른다 - name.siren24.com은
     * 사전 등록된 서버 IP만 허용해 로컬에서는 항상 실패하기 때문이다.
     */
    @PostMapping("/offgive/sci-call")
    public String sciCall(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam(name = "sci_jumin1", required = false) String jumin1,
                          @RequestParam(name = "sci_jumin2", required = false) String jumin2,
                          @RequestParam(name = "sci_name", required = false) String name, Model model)
            throws IOException {
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Pragma", "no-store");
        response.setHeader("Cache-Control", "no-cache");

        // ===== 로컬 테스트 전용 실명인증 우회 =====
        if (isLocalhost(request)) {
            model.addAttribute("mockCi", "MOCKCI" + jumin1 + jumin2);
            model.addAttribute("mockDi", "MOCKDI" + jumin1 + jumin2);
            return "offgive/sci-call";
        }

        SciClient.CallForm form = sciClient.request(request, jumin1, jumin2, name);
        model.addAttribute("actionUrl", form.actionUrl());
        model.addAttribute("reqInfo", form.reqInfo());
        model.addAttribute("retUrl", form.retUrl());
        return "offgive/sci-call";
    }

    /**
     * AS-IS {@code GET|POST sci-result}(sci-result.jsp) - 사이렌24가 돌려준 retInfo를 복호화·위변조 검증해
     * parent.sciResponse(sciUser)로 넘긴다. 사이렌24에서 넘어오는 요청이라 AS-IS처럼 관리자 로그인
     * 확인에서 뺐다({@code OpmanagerHandlerInterceptor.isManagerPage}의 /offgive/sci-result 예외 → WebConfig).
     */
    @RequestMapping(value = "/offgive/sci-result", method = {RequestMethod.GET, RequestMethod.POST})
    public String sciResult(@RequestParam(required = false) String retInfo, Model model) {
        SciClient.SciResult result = sciClient.result(retInfo);
        if (result != null) {
            model.addAttribute("msgChk", result.msgChk());
            model.addAttribute("sciResult", result.result() == null ? "" : result.result());
            model.addAttribute("sciCi", result.ci() == null ? "" : result.ci());
            model.addAttribute("sciDi", result.di() == null ? "" : result.di());
        }
        return "offgive/sci-result";
    }

    /**
     * AS-IS {@code GET pdbtest}(pdbtest.jsp) - 개인정보 암호화 솔루션(OWL PrivacyDB, {@code com.privacy.pCrypto})
     * 설치 확인 페이지. 화면 링크는 없고 직접 주소로 연다 - AS-IS isManagerPage도 로그인 검사에서 뺐다(→ WebConfig).
     * JSP 스크립틀릿이 하던 암·복호화 시험을 그대로 하되, TO-BE에는 그 벤더 라이브러리가 없어 클래스를
     * 이름으로 찾는다 - 없으면 AS-IS가 네이티브 라이브러리를 못 읽었을 때처럼 ERROR 칸에 그 오류가 찍힌다.
     */
    @GetMapping("/offgive/pdbtest")
    public String pdbtest(Model model) {
        String sString = "test1234#$%^&";
        // AS-IS 스크립틀릿처럼 전부 ""로 시작한다(템플릿의 v.xxx는 없는 키면 SpEL 예외라 키를 미리 둔다).
        Map<String, String> v = new HashMap<>();
        for (String key : List.of("sOutput", "sOutput7", "sOutputh", "sOutputh2", "sDecoded", "sDecoded7",
                "libError", "error", "javaTest", "libTest")) {
            v.put(key, "");
        }
        String loadErr = "";
        Class<?> pCrypto = null;
        try {
            pCrypto = Class.forName("com.privacy.pCrypto");
            pCrypto.getDeclaredConstructor().newInstance();
            v.put("javaTest", String.valueOf(pCrypto.getMethod("JavaTest").invoke(null)));
            v.put("libTest", String.valueOf(pCrypto.getMethod("LibTest").invoke(null)));
            java.lang.reflect.Method encrypt = pCrypto.getMethod("Encrypt", String.class, String.class, String.class);
            java.lang.reflect.Method decrypt = pCrypto.getMethod("Decrypt", String.class, String.class, String.class,
                    int.class);
            v.put("sOutput", String.valueOf(encrypt.invoke(null, "normal", sString, "")));
            v.put("sOutput7", String.valueOf(encrypt.invoke(null, "pattern7", sString, "")));
            v.put("sDecoded", String.valueOf(decrypt.invoke(null, "normal", v.get("sOutput"), "", 0)));
            v.put("sDecoded7", String.valueOf(decrypt.invoke(null, "pattern7", v.get("sOutput7"), "", 0)));
            v.put("sOutputh", String.valueOf(encrypt.invoke(null, "hash", sString, "")));
            v.put("sOutputh2", String.valueOf(encrypt.invoke(null, "hash.5", sString, "")));
        } catch (java.lang.reflect.InvocationTargetException e) {
            loadErr = e.getCause() != null ? e.getCause().toString() : e.toString();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            loadErr = e.toString();
        } finally {
            if (pCrypto != null) {
                try {
                    v.put("libError", String.valueOf(pCrypto.getMethod("LibError").invoke(null)));
                    v.put("error", String.valueOf(pCrypto.getMethod("Error").invoke(null)));
                } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
                    log.error("pdbtest.jsp :: pCrypto Encrypt/Decrypt RuntimeException");
                }
            }
        }
        model.addAttribute("sString", sString);
        model.addAttribute("v", v);
        model.addAttribute("sLoadErr", loadErr);
        return "offgive/pdbtest";
    }

    /** AS-IS {@code POST user-check} - CI로 기존회원 조회. 없으면 data 없이 성공. */
    @PostMapping("/offgive/user-check")
    @ResponseBody
    public Map<String, Object> userCheck(@RequestParam String mberCi) {
        MemberClient.ByCiInfo found = memberClient.findByCi(mberCi);
        if (found == null || found.userId() == null) {
            return success(null);
        }
        Map<String, Object> userDetail = new LinkedHashMap<>();
        userDetail.put("post", found.post());
        userDetail.put("address", found.address());
        userDetail.put("addressDetail", found.addressDetail());
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("userId", found.userId());
        user.put("phoneNumber", found.phoneNumber());
        user.put("userDetail", userDetail);
        return success(Map.of("user", user));
    }

    /** AS-IS {@code POST maxCheck} - 연간 잔여 기부한도. */
    @PostMapping("/offgive/maxCheck")
    @ResponseBody
    public Map<String, Object> maxCheck(@RequestParam(required = false) String userId,
                                        @RequestParam(required = false) String mberCi) {
        Long id = userId != null && !userId.isBlank() ? Long.valueOf(userId) : null;
        BigDecimal limit = offgiveClient.limit(id);
        return success(limit == null ? 0 : limit.intValue());
    }

    /** AS-IS {@code POST getLocgovMapngCode} - 행정기관코드(5자리) → 고향사랑 지자체코드. */
    @PostMapping("/offgive/getLocgovMapngCode")
    @ResponseBody
    public Map<String, Object> getLocgovMapngCode(@RequestParam String locgovCode) {
        return success(offgiveClient.locgovMapngCode(locgovCode));
    }

    /** AS-IS {@code GET locgov/{userId}} - 로그인 담당자의 지자체코드. */
    @GetMapping("/offgive/locgov/{userId}")
    @ResponseBody
    public Map<String, Object> getLocgovCodeByUserId(@PathVariable String userId, HttpSession session) {
        Manager viewer = manager(session);
        return success(viewer != null ? viewer.getLocgovCode() : null);
    }

    /** AS-IS {@code GET view/{locgovCode}} - 지자체 상세(화면은 bassAdres만 쓴다). */
    @GetMapping("/offgive/view/{locgovCode}")
    @ResponseBody
    public Map<String, Object> getLocgovDetails(@PathVariable String locgovCode) {
        return success(offgiveClient.locgovView(locgovCode));
    }

    /** AS-IS {@code POST getLocGovLmtt} - 기부제한 기간이면 사유/기간, 아니면 data 없음. */
    @PostMapping("/offgive/getLocGovLmtt")
    @ResponseBody
    public Map<String, Object> getLocGovLmtt(@RequestParam(required = false) String cityCode) {
        return success(cityCode == null ? null : offgiveClient.locgovLmtt(cityCode));
    }

    /** AS-IS {@code POST getDesignatedDonationList} - 기부 지자체의 모금중 지정기부 사업(prjId/prjSubject). */
    @PostMapping("/offgive/getDesignatedDonationList")
    @ResponseBody
    public Map<String, Object> getDesignatedDonationList(@RequestParam(required = false) String locgovCode) {
        try {
            List<Map<String, Object>> list = new ArrayList<>();
            for (OffgiveClient.DesignatedRow row : designatedRows(locgovCode, null)) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("prjId", row.prjId());
                m.put("prjSubject", row.prjSubject());
                list.add(m);
            }
            return success(list);
        } catch (RuntimeException e) {
            return failure(e.getMessage());
        }
    }

    /**
     * AS-IS {@code POST offRprs} - 기부 지자체의 오프라인 대표 답례품. 이미지(load_image)는 gift
     * 서비스가 서빙하는 경로라 gift 주소를 앞에 붙여 내려준다(AS-IS는 같은 앱이 서빙했다).
     */
    @PostMapping("/offgive/offRprs")
    @ResponseBody
    public Map<String, Object> selectOffRprs(@RequestParam(name = "lclgv_cd") String lclgvCd) {
        try {
            List<Map<String, Object>> rows = giftClient.offRprs(lclgvCd);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    Object image = row.get("load_image");
                    if (image instanceof String path && path.startsWith("/uploads/")) {
                        row.put("load_image", giftServiceBaseUrl + path);
                    }
                }
            }
            return success(rows);
        } catch (RuntimeException e) {
            log.error("offRprs 조회 실패", e);
            return failure(e.getMessage());
        }
    }

    // ==================== 행정주소검색 (AS-IS mopas) ====================

    /**
     * AS-IS {@code POST getUserAddressInfo} - 행정정보공동이용센터 주소간단조회(내국인).
     * 로컬 우회는 AS-IS와 같은 조건(요청 URL에 localhost)·같은 값이고, 그 밖에는 실제로 조회한다.
     */
    @PostMapping("/offgive/getUserAddressInfo")
    @ResponseBody
    public Map<String, Object> getUserAddressInfo(HttpServletRequest request,
                                                  @RequestParam(required = false) String juminNo,
                                                  @RequestParam(required = false) String userName) {
        Map<String, Object> map = new HashMap<>();

        // ===== 로컬 테스트 전용 행정주소검색 우회 =====
        // 행정정보공동이용센터(adminAddressinfoUrl)는 행정망 내부에서만 접근 가능해 로컬
        // 개발환경에서는 항상 실패한다. localhost 접근일 때만, 고정된 성공 결과(경기도
        // 시흥시)로 즉시 응답해 이후 흐름(기부 지자체~등록)을 테스트할 수 있게 한다.
        // 운영 도메인에서는 isLocalhost가 false라 이 분기를 타지 않는다.
        if (isLocalhost(request)) {
            map.put("serviceResult", "1");
            map.put("upper_locgov_mapng_code", "41000");
            map.put("locgov_mapng_code", "41390");
            map.put("locgov_nm", "시흥시");
            map.put("juso", "경기도 시흥시 서해안로 1645");
            return success(map);
        }

        try {
            Map<String, Object> jsonObject = administrativeAddressClient.lookup(juminNo, userName);

            //            1:성공
            //            2:주민등록번호오류
            //            3:성명오류
            //            99:등록된이용기관이아님
            String serviceResult = jsonObject.get("serviceResult").toString();
            map.put("serviceResult", serviceResult);
            if ("1".equals(serviceResult)) {
                Map<String, Object> locgovMapngInfo =
                        offgiveClient.locgovMapngCode(jsonObject.get("hangkikcd").toString().substring(0, 5));
                map.put("upper_locgov_mapng_code", locgovMapngInfo.get("upper_locgov_mapng_code"));
                map.put("locgov_mapng_code", locgovMapngInfo.get("locgov_mapng_code"));
                map.put("locgov_nm", locgovMapngInfo.get("locgov_nm"));
                map.put("juso", jsonObject.get("juso").toString());
            } else if ("2".equals(serviceResult)) {
                map.put("serviceResultMsg", "주민등록번호가 잘못 되었습니다.");
                return success(map);
            } else if ("3".equals(serviceResult)) {
                map.put("serviceResultMsg", "주민등록상 성함과 다릅니다. 실명인증과 별개로 주민등록상 성함을 입력해 주세요.");
                return success(map);
            } else if ("99".equals(serviceResult)) {
                map.put("serviceResultMsg", "등록된 이용기관이 아닙니다. ");
                return success(map);
            } else {
                return failure("serviceResult 코드값 오류");
            }
        } catch (IOException e) {
            log.error("{} getUserAddressInfo IOException1 =========================", getClass().getName(), e);
            return failure("행정망공동이용센터 주소 검색에 실패했습니다.");
        } catch (RuntimeException e) {
            log.error("{} getUserAddressInfo Exception =========================", getClass().getName(), e);
            return failure("행정망공동이용센터 주소 검색에 실패했습니다.");
        }
        return success(map);
    }

    /** AS-IS 로컬 우회 조건 - {@code request.getRequestURL().indexOf("localhost") > -1}. */
    private static boolean isLocalhost(HttpServletRequest request) {
        return request.getRequestURL().indexOf("localhost") > -1;
    }

    /** AS-IS {@code POST getPublicKey} - 외국인등록번호 암호화용 RSA 키쌍, 개인키는 세션에. */
    @PostMapping("/offgive/getPublicKey")
    @ResponseBody
    public Map<String, Object> getPublicKey(HttpServletRequest request) {
        Map<String, String> keyPair = RsaCryptor.createKeypairAsString();
        if (keyPair == null) {
            return failure("암호화키 생성에 실패했습니다.");
        }
        request.getSession().setAttribute(SESSION_FOREIGN_PRIVATE_KEY, keyPair.get("privateKeyStr"));
        return success(Map.of("publicKey", keyPair.get("publicKeyStr")));
    }

    /**
     * AS-IS {@code POST getUserAddressInfoForeign} - 외국인 거소정보 조회(암호문을 세션 개인키로 푼다).
     * 로컬 우회는 AS-IS와 같은 조건(localhost)·같은 값 - 복호화는 우회 전에 그대로 실행한다.
     */
    @PostMapping("/offgive/getUserAddressInfoForeign")
    @ResponseBody
    public Map<String, Object> getUserAddressInfoForeign(HttpServletRequest request, HttpSession session,
                                                         @RequestParam(required = false) String juminNo,
                                                         @RequestParam(required = false) String userName) {
        String foreignPrivateKey;
        try {
            foreignPrivateKey = request.getSession().getAttribute(SESSION_FOREIGN_PRIVATE_KEY).toString();

            request.getSession().removeAttribute(SESSION_FOREIGN_PRIVATE_KEY);

            if (foreignPrivateKey.isEmpty()) {
                throw new NullPointerException("외국인등록번호 정보가 없습니다.");
            }
        } catch (NullPointerException e) {
            log.error("/offgive/getUserAddressInfoForeign NullPointerException", e);
            return failure("데이터 복호화에 실패했습니다.");
        }

        Map<String, Object> resultMap = new HashMap<>();

        try {
            String decryptedNo = RsaCryptor.decrypt(juminNo, foreignPrivateKey);

            // ===== 로컬 테스트 전용 행정주소검색 우회 =====
            // 행정정보공동이용시스템 외국인 조회(getForeignApiData)는 행정망 내부에서만 접근 가능해
            // 로컬 개발환경에서는 항상 실패한다. localhost 접근일 때만, 고정된 성공 결과(경기도 시흥시)로
            // 즉시 응답한다. 복호화(juminNo)는 그대로 실행해 로컬 암복호화 라운드트립 자체는 계속 검증된다.
            if (isLocalhost(request)) {
                resultMap.put("rst", "SUCCESS");
                resultMap.put("upper_locgov_mapng_code", "41000");
                resultMap.put("locgov_mapng_code", "41390");
                resultMap.put("locgov_nm", "시흥시");
                return success(resultMap);
            }

            Map<String, Object> requestParam = new HashMap<>();
            requestParam.put("foreignNo", decryptedNo);    //주민번호
            requestParam.put("foreignName", userName);     //이름

            // AS-IS locgovService.getLocgovCodeByOpId(로그인 관리자, MANAGER)
            Manager viewer = manager(session);
            String locgovCode = viewer != null ? viewer.getLocgovCode() : null;

            Map<String, Object> apiData = administrativeAddressClient.getForeignApiData(requestParam, locgovCode);

            String rst = apiData.get("rst").toString();
            if ("SUCCESS".equalsIgnoreCase(rst) || "IN_LOCGOV".equalsIgnoreCase(rst)) {
                Map<String, Object> locgovMapngInfo = offgiveClient.locgovMapngCode(apiData.get("locgovCode").toString());
                resultMap.put("upper_locgov_mapng_code", locgovMapngInfo.get("upper_locgov_mapng_code"));
                resultMap.put("locgov_mapng_code", locgovMapngInfo.get("locgov_mapng_code"));
                resultMap.put("locgov_nm", locgovMapngInfo.get("locgov_nm"));
                resultMap.put("rst", rst);
            } else {
                resultMap = apiData;
            }
        } catch (IOException | NullPointerException e) {
            log.error("/offgive/getUserAddressInfoForeign IOException 행공센 조회 실패", e);
            resultMap.put("errCode", "BAD_REQUEST");
            resultMap.put("errMsg", "행정정보공동이용시스템 조회 통신 실패");
        } catch (RuntimeException e) {
            log.error("/offgive/getUserAddressInfoForeign Exception 행공센 조회 실패", e);
            resultMap.put("errCode", "Exception");
            resultMap.put("errMsg", "행정정보공동이용시스템 조회 오류");
        }

        return success(resultMap);
    }

    // ==================== 세외수입 부과요청 (등록 전 전자납부번호 선취득) ====================

    /**
     * AS-IS {@code POST nextBugaRequest}(goContry2) - 지방세외 차세대 부과요청(서울 외). 화면 값이
     * {@code NextBugaRequestDto}에 바인딩되는 그대로 넘긴다(selectedRegionCd·userRegionCd는 DTO에 없어
     * AS-IS도 버린다). 결과는 AS-IS처럼 항상 success로 감싸고, 특정사업 검증 실패는 data.errorMessage,
     * 연계 응답은 data에 키 전부 + epayNo.
     */
    @PostMapping("/offgive/nextBugaRequest")
    @ResponseBody
    public Map<String, Object> nextBugaRequest(@RequestParam(required = false) String frstPctAmt,
                                               @RequestParam(required = false) String pyrNo,
                                               @RequestParam(required = false) String pyrNm,
                                               @RequestParam(required = false) String roadNmCd,
                                               @RequestParam(required = false) String bmno,
                                               @RequestParam(required = false) String bsno,
                                               @RequestParam(required = false) String zip,
                                               @RequestParam(required = false) String dongCd,
                                               @RequestParam(required = false) String roadNmDaddr,
                                               @RequestParam(required = false) String glNm,
                                               @RequestParam(required = false) String cntrLocgovCode,
                                               @RequestParam(required = false) String presentType,
                                               @RequestParam(required = false) String prjId) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("frstPctAmt", frstPctAmt);
        form.put("pyrNo", pyrNo);
        form.put("pyrNm", pyrNm);
        form.put("roadNmCd", roadNmCd);
        form.put("bmno", bmno);
        form.put("bsno", bsno);
        form.put("zip", zip);
        form.put("dongCd", dongCd);
        form.put("roadNmDaddr", roadNmDaddr);
        form.put("glNm", glNm);
        form.put("cntrLocgovCode", cntrLocgovCode);
        form.put("presentType", presentType);
        form.put("prjId", prjId);
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap.putAll(levyRelayClient.nextBuga(form));
        } catch (RuntimeException e) {
            log.error("■■■API■■■ nextBugaRequest Exception", e);
        }
        return success(resultMap);
    }

    /** AS-IS {@code POST getSdonationCharge}(goSeoul) - 서울시 세외수입 부과정보 등록. */
    @PostMapping("/offgive/getSdonationCharge")
    @ResponseBody
    public Map<String, Object> getSdonationCharge(@RequestParam(required = false) String systemCd,
                                                  @RequestParam(required = false) String jijacheCd,
                                                  @RequestParam(required = false) String semokCd,
                                                  @RequestParam(required = false) String taxGubun,
                                                  @RequestParam(required = false) String sidoCd,
                                                  @RequestParam(required = false) String napId,
                                                  @RequestParam(required = false) String napNm,
                                                  @RequestParam(required = false) String napGubun,
                                                  @RequestParam(required = false) String taxAmt,
                                                  @RequestParam(required = false) String resideStatus,
                                                  @RequestParam(required = false) String mulGubun,
                                                  @RequestParam(required = false) String mulNm,
                                                  @RequestParam(required = false) String sysGubun) {
        Map<String, Object> map = new HashMap<>();
        try {
            LevyRelayClient.BugaResponse r = levyRelayClient.seoulBuga(jijacheCd, napId, napNm, new BigDecimal(taxAmt),
                    systemCd, semokCd, taxGubun, sidoCd, napGubun, resideStatus, mulGubun, mulNm, sysGubun);
            if (r.epayNo() != null) {
                map.put("elctPayNo", r.epayNo());
            }
        } catch (RuntimeException e) {
            // AS-IS도 IOException 등은 로그만 남기고 빈 data로 성공을 돌려준다.
            log.error("■■■API■■■ getSdonationCharge Exception", e);
        }
        return success(map);
    }

    /** AS-IS {@code POST updatePhoneNumber} - 기존회원 휴대폰번호 수정. */
    @PostMapping("/offgive/updatePhoneNumber")
    @ResponseBody
    public Map<String, Object> updatePhoneNumber(@RequestParam Long userId, @RequestParam String phoneNumber) {
        try {
            return memberClient.updatePhoneNumber(userId, phoneNumber) > 0
                    ? success(null) : failure("연락처 수정을 실패하였습니다");
        } catch (RuntimeException e) {
            return failure(e.getMessage());
        }
    }

    // ==================== 등록 (AS-IS POST create, form serialize) ====================

    /**
     * AS-IS {@code POST create} - 화면이 {@code $('#offgive').serialize()}로 보낸 값으로 등록한다.
     * 담당자 롤이 OFF가 아니면 "오프라인 담당자만 등록이 가능합니다.", 그 밖의 실패는 AS-IS처럼
     * 사유와 무관하게 "기탁서 등록에 실패하였습니다. 고객센터에 문의 하세요."로 돌려준다.
     */
    @PostMapping("/offgive/create")
    @ResponseBody
    public Map<String, Object> createAction(@RequestParam Map<String, String> form, HttpSession session) {
        Manager viewer = manager(session);
        if (!canRegister(viewer)) {
            return failure("오프라인 담당자만 등록이 가능합니다.");
        }
        try {
            String prjId = form.get("prjId");
            OffgiveClient.RegisterRequest request = new OffgiveClient.RegisterRequest(
                    blankToNull(form.get("userId")) != null ? Long.valueOf(form.get("userId")) : null,
                    form.get("userName"), null, null, form.get("mberCi"), form.get("mberDi"),
                    form.get("phoneNumber"), form.get("address"), form.get("addressDetail"),
                    form.get("psitnLocgovCode"), form.get("locgovCode"), new BigDecimal(form.get("cntrAmt")),
                    prjId == null || prjId.isBlank() || "0".equals(prjId) ? null : Long.valueOf(prjId),
                    form.get("rtnpsntReqstCode"), form.get("rcvUserName"),
                    joinPhone(form.get("rcvPhoneNumber1"), form.get("rcvPhoneNumber2"), form.get("rcvPhoneNumber3")),
                    form.get("rcvAddress"), form.get("rcvAddressDetail"), parseOrderItems(form.get("orderItems")),
                    viewer.getBankCode(), viewer.getPsitnNm(), viewer.getUserName(),
                    form.get("elctrnPayNo"), null, form.get("birthday"), form.get("rcvPost"),
                    // AS-IS insertOffgive: 서울(11000)은 연계관리키 새 채번, 그 외는 부과요청 응답의 #linkMngKey가 기부일련번호
                    form.get("locgovUpperCode"), form.get("linkMngKey"),
                    form.get("post"), form.get("gender"));
            OffgiveClient.RegisterResult result = offgiveClient.register(request);
            return success(result.donation().cntrSn());
        } catch (RuntimeException e) {
            log.error("기탁서 등록 실패", e);
            return failure("기탁서 등록에 실패하였습니다. 고객센터에 문의 하세요.");
        }
    }

    /**
     * AS-IS orderItems 문자열 - {@code itemId||수량||optionId```} 를 {@code @}로 이어 붙인 것
     * (form.jsp goSave). optionId가 비면 옵션 없는 답례품.
     */
    private static List<OffgiveClient.GiftLine> parseOrderItems(String orderItems) {
        List<OffgiveClient.GiftLine> lines = new ArrayList<>();
        if (orderItems == null || orderItems.isEmpty()) {
            return lines;
        }
        for (String entry : orderItems.split("@")) {
            String item = entry.replace("```", "");
            String[] parts = item.split("\\|\\|", -1);
            if (parts.length < 2 || parts[0].isBlank()) {
                continue;
            }
            Long optionId = parts.length > 2 && !parts[2].isBlank() && !"undefined".equals(parts[2])
                    ? Long.valueOf(parts[2]) : null;
            lines.add(new OffgiveClient.GiftLine(Long.valueOf(parts[0]), optionId, Integer.valueOf(parts[1])));
        }
        return lines;
    }

    // ==================== 지정기부목록 다운로드 (AS-IS list/prj/download-excel) ====================

    /**
     * AS-IS {@code GET list/prj/download-excel}({@code OffgiveExcelViewPrjList.buildItemSheet}) -
     * 전 지자체 모금중 지정기부 사업, 사업부문순. 시트 "특정사업 기부", 제목 "특정사업 기부목록",
     * 9컬럼. 사업구분/사업부문은 공통코드 BUSINESS_TYPE/BUSINESS_SUB_TYPE의 <b>detail</b>.
     * 개인정보가 없어 AS-IS도 사유 팝업·암호화 없이 바로 내려준다.
     */
    @GetMapping("/offgive/list/prj/download-excel")
    public void downloadExcelProcessByListPrj(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Map<String, String> bsnsDetail = detailsOf("BUSINESS_TYPE");
        Map<String, String> subBsnsDetail = detailsOf("BUSINESS_SUB_TYPE");

        AsisSxssfExcelView view = new AsisSxssfExcelView();
        Sheet sheet = view.workbook().createSheet("특정사업 기부");
        Row row = sheet.createRow((short) 0);
        view.createSheetHeader(sheet, row,
                new String[]{"No.", "사업구분", "사업부문", "지자체명", "사업명", "시작일", "종료일", "목표금액", "모금액"},
                new int[]{500, 7500, 3500, 3500, 3500, 2000, 2000, 3000, 3000}, "특정사업 기부목록");

        int rowIndex = 2;
        int index = 0;
        for (OffgiveClient.DesignatedRow obj : designatedRows(null, "BSNS")) {
            row = sheet.createRow(rowIndex);
            row.setHeight((short) 400);
            AsisSxssfExcelView.CellIndex cellIndex = new AsisSxssfExcelView.CellIndex(-1);
            view.setText(sheet, rowIndex, cellIndex, String.valueOf(++index));
            view.setText(sheet, rowIndex, cellIndex, bsnsDetail.getOrDefault(obj.bsnsType(), ""));
            view.setText(sheet, rowIndex, cellIndex, subBsnsDetail.getOrDefault(obj.bsnsSubType(), ""));
            view.setText(sheet, rowIndex, cellIndex, obj.locgovNm());
            view.setTextLeft(sheet, rowIndex, cellIndex, obj.prjSubject());
            view.setText(sheet, rowIndex, cellIndex, ymd(obj.prjStDt()));
            view.setText(sheet, rowIndex, cellIndex, ymd(obj.prjEdDt()));
            view.setTextRight(sheet, rowIndex, cellIndex, comma(obj.targetAmt()));
            view.setTextRight(sheet, rowIndex, cellIndex, comma(obj.sumAmt()));
            rowIndex++;
        }
        view.write(request, response,
                "지정기부목록_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx");
    }

    /**
     * donation의 selectDesignatedDonationList 결과에서 AS-IS SQL의
     * {@code JOIN OP_COMMON_CODE C1 ON CODE_TYPE='BUSINESS_TYPE'}(내부조인)을 마저 적용한다 -
     * 사업구분 코드가 공통코드에 없는 사업은 AS-IS 목록에 안 나온다(공통코드는 admin 소유).
     */
    private List<OffgiveClient.DesignatedRow> designatedRows(String locgovCode, String sort) {
        Set<String> bsnsTypes = commonCodeService.listByType("BUSINESS_TYPE").stream()
                .map(CommonCode::getId).collect(Collectors.toSet());
        List<OffgiveClient.DesignatedRow> rows = offgiveClient.designatedDonationList(locgovCode, sort);
        return rows == null ? List.of() : rows.stream().filter(r -> bsnsTypes.contains(r.bsnsType())).toList();
    }

    private Map<String, String> detailsOf(String codeType) {
        Map<String, String> map = new LinkedHashMap<>();
        for (CommonCode code : commonCodeService.listByType(codeType)) {
            map.put(code.getId(), code.getDetail() == null ? "" : code.getDetail());
        }
        return map;
    }

    /** AS-IS SimpleDateFormat yyyyMMdd → yyyy-MM-dd. */
    private static String ymd(String yyyyMMdd) {
        if (yyyyMMdd == null || yyyyMMdd.length() < 8) {
            return yyyyMMdd == null ? "" : yyyyMMdd;
        }
        return yyyyMMdd.substring(0, 4) + "-" + yyyyMMdd.substring(4, 6) + "-" + yyyyMMdd.substring(6, 8);
    }

    /** AS-IS {@code String.valueOf(n).replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",")}. */
    private static String comma(Long amount) {
        return String.valueOf(amount).replaceAll("\\B(?=(\\d{3})+(?!\\d))", ",");
    }

    // ==================== 공통 ====================

    /** AS-IS insertOffgive/createAction - OFF(ROLE_ADMIN_7/8)만 등록 가능. */
    private static boolean canRegister(Manager viewer) {
        return viewer != null
                && ("ROLE_ADMIN_7".equals(viewer.getAuthority()) || "ROLE_ADMIN_8".equals(viewer.getAuthority()));
    }

    private static String joinPhone(String p1, String p2, String p3) {
        return nvl(p1) + "-" + nvl(p2) + "-" + nvl(p3);
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    /** AS-IS JsonViewUtils.success(data). */
    private static Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("isSuccess", true);
        map.put("data", data);
        return map;
    }

    /** AS-IS JsonViewUtils.failure(message). */
    private static Map<String, Object> failure(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("isSuccess", false);
        map.put("errorMessage", message == null ? "" : message);
        return map;
    }

    private static Manager manager(HttpSession session) {
        return (Manager) session.getAttribute(ManagerAuthController.SESSION_MANAGER_KEY);
    }
}
