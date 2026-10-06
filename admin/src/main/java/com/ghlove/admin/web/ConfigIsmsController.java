package com.ghlove.admin.web;

import com.ghlove.admin.domain.ConfigIsms;
import com.ghlove.admin.repository.ConfigIsmsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ISMS 시간설정 관리 - AS-IS saleson.shop.config.ConfigIsmsManagerController
 * (/opmanager/isms/isms-config) 재현.
 *
 * <p>화면은 구분(공통/관리자/회원)별로 rowspan 묶음 표 하나이고, 저장은 **전체 입력값을 JSON
 * 배열 하나로 한 번에** 보낸다({@code data=[{"key":..,"value":..},...]}). 예전 TO-BE는 이걸
 * 행 단위 폼 저장으로 단순화하고 화면에 없는 사용여부 체크박스를 받았는데, AS-IS 방식으로 되돌렸다.
 */
@Controller
@RequiredArgsConstructor
public class ConfigIsmsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /** AS-IS ISMS_TYPE - 0=공통 1=관리자 2=회원. */
    private static final Map<String, String> TYPE_NAMES = Map.of("0", "공통", "1", "관리자", "2", "회원");

    private final ConfigIsmsRepository configIsmsRepository;
    private final ObjectMapper objectMapper;

    /** 한 줄 - typeName/typeCount가 채워진 행이 그 구분의 첫 행이고 rowspan을 가진다. */
    public record IsmsRow(ConfigIsms config, String typeName, int typeCount) {
    }

    @GetMapping("/isms-config")
    public String list(Model model) {
        List<ConfigIsms> all = configIsmsRepository.findAllByOrderByOrdering();

        Map<String, Integer> counts = new LinkedHashMap<>();
        all.forEach(c -> counts.merge(c.getIsmsType(), 1, Integer::sum));

        List<IsmsRow> rows = new ArrayList<>();
        Map<String, Boolean> headUsed = new LinkedHashMap<>();
        for (ConfigIsms config : all) {
            String type = config.getIsmsType();
            boolean isHead = !Boolean.TRUE.equals(headUsed.get(type));
            if (isHead) {
                headUsed.put(type, true);
                rows.add(new IsmsRow(config, TYPE_NAMES.getOrDefault(type, ""), counts.getOrDefault(type, 0)));
            } else {
                rows.add(new IsmsRow(config, null, 0));
            }
        }
        model.addAttribute("getIsmsList", rows);
        return "isms-config/list";
    }

    /**
     * AS-IS updateIsmsConfig - 화면이 모든 입력칸을 모아 보낸 JSON 배열을 한 번에 반영한다.
     * 화면 JS는 응답 본문을 보지 않고 바로 "반영되었습니다."를 띄우므로 실패는 HTTP 오류로 알린다.
     */
    @PostMapping("/isms-config")
    @ResponseBody
    @Transactional
    public Map<String, Object> update(@RequestParam("data") String data) throws Exception {
        List<Map<String, String>> items = objectMapper.readValue(data, new com.fasterxml.jackson.core.type.TypeReference<>() { });
        String now = DATE_FORMAT.format(LocalDateTime.now());
        for (Map<String, String> item : items) {
            String key = item.get("key");
            if (key == null) {
                continue;
            }
            configIsmsRepository.findById(key).ifPresent(config -> {
                config.setValue(item.get("value"));
                config.setUpdateDate(now);
                configIsmsRepository.save(config);
            });
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("isSuccess", true);
        return result;
    }
}
