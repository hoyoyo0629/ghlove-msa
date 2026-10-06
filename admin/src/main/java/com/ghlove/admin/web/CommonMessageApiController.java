package com.ghlove.admin.web;

import com.ghlove.admin.service.CommonMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AS-IS op.common.js의 {@code Message.get(code)}가 때리는 엔드포인트 (verbatim).
 * <pre>
 *   $.post('/common/message', {'messageCode' : messageCode}, function(resp){ message = resp.data; })
 * </pre>
 * 복사해온 공통 JS를 그대로 쓰려면 경로·파라미터명·응답 모양({isSuccess, data})을 바꿀 수 없다.
 * 이 JS가 동기 ajax(async:false)로 호출하므로 조회는 메모리 캐시에서만 끝낸다
 * ({@link CommonMessageService}).
 */
@RestController
@RequiredArgsConstructor
public class CommonMessageApiController {

    private final CommonMessageService commonMessageService;

    @RequestMapping(value = "/common/message", method = { RequestMethod.GET, RequestMethod.POST })
    public Map<String, Object> message(@RequestParam("messageCode") String messageCode) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", true);
        m.put("data", commonMessageService.get(messageCode));
        return m;
    }

    /**
     * 한 화면에서 여러 코드를 쓰는 경우용 - AS-IS에는 없지만 {@code Message.get}이 코드마다
     * 동기 ajax를 한 번씩 때리는 구조라, 새로 만드는 화면은 필요한 코드를 한 번에 받아
     * 인라인으로 깔 수 있게 열어둔다(AS-IS 화면을 이식할 때는 쓰지 않는다).
     */
    @PostMapping("/common/messages")
    @ResponseBody
    public Map<String, Object> messages(@RequestParam("messageCodes") String messageCodes) {
        Map<String, String> found = new LinkedHashMap<>();
        for (String code : messageCodes.split(",")) {
            String trimmed = code.trim();
            if (!trimmed.isEmpty()) {
                found.put(trimmed, commonMessageService.get(trimmed));
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isSuccess", true);
        m.put("data", found);
        return m;
    }
}
