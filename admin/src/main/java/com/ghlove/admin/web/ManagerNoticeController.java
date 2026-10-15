package com.ghlove.admin.web;

import com.ghlove.admin.domain.ManagerNotice;
import com.ghlove.admin.repository.ManagerNoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AS-IS {@code CommonController /common/manager/notice/list}(관리자 공지 팝업 목록) 이식. 복사해 온
 * {@code /content/popup/popup.js}의 {@code pop.openNotice(type)}가 이 주소를 부르고 {@code response.isSuccess}·
 * {@code response.data[]}(managerNoticeId·subject·content·width·height)를 읽는다. 로그인 화면에서도 부르므로 로그인 없이 열린다.
 */
@RestController
@RequiredArgsConstructor
public class ManagerNoticeController {

    /**
     * AS-IS {@code DATE_FORMAT(now(), '%Y%m%d%H%m%s')} 그대로 - 분 자리에 <b>월(%m)</b>이 들어간다(AS-IS 오타지만 게시기간
     * 비교 결과가 그 값에 따라 정해지므로 같게 둔다). 예: 2026-10-14 13:45:07 → "2026101413" + "10" + "07".
     */
    private static final DateTimeFormatter ASIS_NOW = DateTimeFormatter.ofPattern("yyyyMMddHHMMss");

    private final ManagerNoticeRepository managerNoticeRepository;

    @GetMapping("/common/manager/notice/list")
    public Map<String, Object> list(@RequestParam(required = false) String type) {
        // AS-IS: type이 있으면 NOTICE_TYPE IN('A', type), 없으면 'A'만.
        List<String> types = type != null && !type.isEmpty() ? List.of("A", type) : List.of("A");
        List<ManagerNotice> notices = managerNoticeRepository.findOpen(LocalDateTime.now().format(ASIS_NOW), types);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", true);
        body.put("data", notices);
        return body;
    }
}
