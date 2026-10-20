package com.ghlove.admin.web;

import com.ghlove.admin.service.OffgiveClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AS-IS CommonController {@code POST /common/getLocgovCode} - 시도 코드로 시군구 목록({LOCGOV_CODE, LOCGOV_NM}).
 * 응답은 AS-IS JsonViewUtils 모양({isSuccess, data})이라 화면 JS가 {@code Common.responseHandler}로 받는다.
 * ({@code /give-state/options-by-locgovCode}는 같은 목록을 봉투 없이 돌려주는 다른 AS-IS 경로다.)
 */
@Controller
@RequiredArgsConstructor
public class CommonLocgovController {

    private final OffgiveClient offgiveClient;

    @PostMapping("/common/getLocgovCode")
    @ResponseBody
    public Map<String, Object> getLocgovCode(@RequestParam(name = "code", defaultValue = "0") String code) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        result.put("data", offgiveClient.locgovOptions(code));
        return result;
    }
}
