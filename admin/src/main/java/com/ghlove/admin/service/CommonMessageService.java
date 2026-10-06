package com.ghlove.admin.service;

import com.ghlove.admin.domain.CommonMessage;
import com.ghlove.admin.repository.CommonMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AS-IS JSP의 {@code ${op:message('M00730')}} / JS의 {@code Message.get("M00745")}에 대응하는
 * 문구 조회 (OP_COMMON_MESSAGE). 운영관리 화면 라벨·알럿·검증문구를 AS-IS와 글자 단위로 같게
 * 맞추려면 반드시 이걸 거쳐야 한다 - 템플릿에 한글을 직접 적으면 운영에서 문구를 바꿨을 때
 * AS-IS는 바뀌고 우리는 안 바뀌어 갈라진다.
 *
 * 표가 2천행대 정적 데이터라 최초 1회만 읽어 메모리에 들고 있는다. 운영 중 문구를 고쳤으면
 * {@link #reload()}로 비운다(문구 관리화면이 생기면 저장 후 호출할 자리다).
 *
 * 코드가 표에 없으면 코드 자체를 돌려준다 - AS-IS도 미등록 코드를 그대로 노출하므로 화면이
 * 깨지지 않고, 빠진 코드가 화면에 드러나 바로 눈에 띈다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommonMessageService {

    /** AS-IS inc_head의 OP_LANGUAGE 기본값과 동일. */
    public static final String DEFAULT_LANGUAGE = "ko";

    private final CommonMessageRepository commonMessageRepository;

    private final Map<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    /** 문구 조회 - 템플릿에서 {@code ${msg.get('M00730')}} 로 쓴다. */
    public String get(String code) {
        return get(code, DEFAULT_LANGUAGE);
    }

    public String get(String code, String language) {
        if (code == null || code.isBlank()) {
            return "";
        }
        String found = messagesOf(language).get(code);
        if (found != null) {
            return found;
        }
        if (!DEFAULT_LANGUAGE.equals(language)) {
            found = messagesOf(DEFAULT_LANGUAGE).get(code);
        }
        return found != null ? found : code;
    }

    /** 전체 문구 맵 - JS로 내려 {@code Message.get()}을 쓰는 화면에서 필요하다. */
    public Map<String, String> all() {
        return all(DEFAULT_LANGUAGE);
    }

    public Map<String, String> all(String language) {
        return messagesOf(language);
    }

    public void reload() {
        cache.clear();
    }

    private Map<String, String> messagesOf(String language) {
        return cache.computeIfAbsent(language, lang -> {
            Map<String, String> loaded = commonMessageRepository.findByLanguage(lang).stream()
                    .filter(m -> m.getId() != null && m.getMessage() != null)
                    .collect(Collectors.toMap(CommonMessage::getId, CommonMessage::getMessage, (a, b) -> a));
            log.info("운영관리 문구 사전 적재: language={}, {}건", lang, loaded.size());
            return loaded;
        });
    }
}
