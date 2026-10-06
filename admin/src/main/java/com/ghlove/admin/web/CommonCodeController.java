package com.ghlove.admin.web;

import com.ghlove.admin.domain.CommonCode;
import com.ghlove.admin.service.CommonCodeException;
import com.ghlove.admin.service.CommonCodeService;
import com.ghlove.admin.service.CommonMessageService;
import com.ghlove.admin.web.support.CodeParam;
import com.ghlove.admin.web.support.Pagination;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 공통코드 관리 - AS-IS saleson.shop.code.CodeManagerController(/opmanager/code) 재현.
 * AS-IS는 한 화면에서 왼쪽 코드구분 패널 + 오른쪽 코드목록을 함께 보여주고, 등록·수정은
 * {@code Common.popup}으로 띄우는 별도 팝업창(layout="base")이다. 예전 TO-BE는 코드유형 목차
 * 화면과 유형별 목록 화면을 둘로 나누고 검색·페이징·삭제가 없었으며, AS-IS에 없는 사용여부
 * 토글 버튼을 만들어 썼다 - AS-IS 구성으로 되돌렸다.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/codes")
public class CommonCodeController {

    private final CommonCodeService commonCodeService;
    private final CommonMessageService commonMessageService;

    /** AS-IS commonCodeList / searchCommonCodeList (GET·POST 동일 동작). */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
    public String list(@ModelAttribute("codeParam") CodeParam codeParam,
                       HttpServletRequest request, Model model) {
        Map<String, Long> codeTypeList = commonCodeService.codeTypeList();
        List<CommonCode> found = commonCodeService.search(codeParam.getCodeType(), codeParam.getWhere(), codeParam.getQuery());

        Pagination pagination = Pagination.of(found.size(), codeParam.getPage()).withLinkFrom(request);
        List<CommonCode> codeList = found.stream()
                .skip(pagination.getStartRow())
                .limit(pagination.getItemsPerPage())
                .toList();

        model.addAttribute("codeTypeCount", codeTypeList.size());
        model.addAttribute("codeCount", found.size());
        model.addAttribute("codeTypeList", codeTypeList);
        model.addAttribute("codeList", codeList);
        model.addAttribute("pagination", pagination);
        return "codes/list";
    }

    /** AS-IS codeInsert(GET create) - 팝업. 목록에서 고른 코드구분을 그대로 받아 읽기전용으로 채운다. */
    @GetMapping("/create")
    public String createForm(@RequestParam(value = "whereCodeType", required = false) String whereCodeType,
                             Model model) {
        CommonCode code = new CommonCode();
        code.setCodeType(whereCodeType);
        model.addAttribute("code", code);
        model.addAttribute("isNew", true);
        return "codes/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute CommonCode code, Model model) {
        try {
            commonCodeService.create(code);
            model.addAttribute("message", commonMessageService.get("M00632"));  // 등록되었습니다
            return "common/popup-result";
        } catch (CommonCodeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("code", code);
            model.addAttribute("isNew", true);
            return "codes/form";
        }
    }

    /** AS-IS codeUpdate(GET edit) - 팝업. 파라미터명(whereCodeType/whereId)까지 AS-IS와 같다. */
    @GetMapping("/edit")
    public String editForm(@RequestParam("whereCodeType") String whereCodeType,
                           @RequestParam("whereId") String whereId, Model model) {
        model.addAttribute("code", commonCodeService.get(whereCodeType, whereId));
        model.addAttribute("isNew", false);
        return "codes/form";
    }

    @PostMapping("/edit")
    public String edit(@ModelAttribute CommonCode code, Model model) {
        commonCodeService.update(code.getCodeType(), code.getId(), code);
        model.addAttribute("message", commonMessageService.get("M01673"));   // 수정되었습니다
        return "common/popup-result";
    }

    /** AS-IS deleteListData(POST delete) - 목록 삭제링크의 ajax. {isSuccess}만 보고 분기한다. */
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> delete(@RequestParam("codeType") String codeType,
                                      @RequestParam("id") String id) {
        commonCodeService.delete(codeType, id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }
}
