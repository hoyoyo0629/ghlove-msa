package com.ghlove.admin.web;

import com.ghlove.admin.domain.ConfigIsms;
import com.ghlove.admin.repository.ConfigIsmsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** ISMS(정보보호관리체계) 설정 (AS-IS opmanager/isms/isms-config) - 광고성 메일/문자
 *  발송 가능 시간대 등. AS-IS는 그리드 전체를 JSON 배열로 한 번에 저장하는 AJAX
 *  방식이지만, 이 프로젝트는 행 단위 폼 저장으로 단순화했다(값 자체는 동일). */
@Controller
@RequiredArgsConstructor
public class ConfigIsmsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ConfigIsmsRepository configIsmsRepository;

    @GetMapping("/isms-config")
    public String list(Model model) {
        model.addAttribute("configs", configIsmsRepository.findAllByOrderByOrdering());
        return "isms-config/list";
    }

    @PostMapping("/isms-config/{key}")
    public String update(@org.springframework.web.bind.annotation.PathVariable String key,
                          @RequestParam String value, @RequestParam(required = false) String useYn) {
        ConfigIsms config = configIsmsRepository.findById(key).orElseThrow();
        config.setValue(value);
        config.setUseYn(useYn != null ? "Y" : "N");
        config.setUpdateDate(DATE_FORMAT.format(LocalDateTime.now()));
        configIsmsRepository.save(config);
        return "redirect:/isms-config?message=" +
                java.net.URLEncoder.encode("저장되었습니다.", java.nio.charset.StandardCharsets.UTF_8);
    }
}
