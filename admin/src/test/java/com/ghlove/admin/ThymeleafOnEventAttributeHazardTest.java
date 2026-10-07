package com.ghlove.admin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2026-10-07 결함 재발 방지 - {@code th:onclick}(그 외 {@code th:onXXX} DOM 이벤트 속성)을 아예 막는다.
 *
 * <p>Thymeleaf 3.1은 {@code th:onclick} 같은 DOM 이벤트 속성({@code StandardDOMEventAttributeTagProcessor})을
 * "신뢰할 수 없는 컨텍스트"로 취급해, 그 안의 {@code ${...}} 변수식이 <b>숫자·불린이 아니면</b>
 * {@code TemplateProcessingException("Only variable expressions returning numbers or booleans are
 * allowed...")}을 던진다. 1405 권한그룹 목록의 {@code th:onclick="|fnGrpUpdate('${row.role.authority}')|"}
 * (문자열 authority)가 이걸로 렌더링 중 터졌는데, 그 시점에 헤더·상단 마크업이 이미 응답으로
 * 나간 뒤라 에러페이지도 못 띄우고 <b>응답이 끊겨 화면 전체가 깨졌다</b>(브라우저는
 * {@code ERR_INCOMPLETE_CHUNKED_ENCODING}/빈 view-source로 본다) - 컴파일·기동 성공과 무관하게
 * 그 화면을 실제로 렌더링해봐야만 터지는 결함이라 소스 규칙으로 막는다. 같은 패턴이 당시 7개
 * 화면·10곳에 더 있었다(숫자 파라미터뿐이라 지금은 안 터지지만 똑같이 위험하다).
 *
 * <p>규칙: 템플릿에 {@code th:on}으로 시작하는 속성을 아예 쓰지 않는다. 대신 이미 15곳 이상에서
 * 쓰는 {@code th:attr="onclick=|...${var}...|"}로 쓴다 - th:attr은 이 "신뢰 컨텍스트" 제한이 없어
 * 문자열·숫자 가리지 않고 안전하다.
 */
class ThymeleafOnEventAttributeHazardTest {

    private static final Path TEMPLATES = Paths.get("src", "main", "resources", "templates");
    private static final Pattern ON_EVENT_ATTR = Pattern.compile("th:on[a-zA-Z]+\\s*=");

    @Test
    void 템플릿에_th_on이벤트속성을_쓰지않는다() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(TEMPLATES)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".html")).toList()) {
                scan(file, violations);
            }
        }
        assertTrue(violations.isEmpty(),
                "th:onXXX(DOM 이벤트 속성)을 찾았다 - 문자열 변수식이 들어가면 렌더링이 중단된다. "
                        + "th:attr=\"onclick=|...|\"로 바꿔라:\n" + String.join("\n", violations));
    }

    private void scan(Path file, List<String> violations) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        boolean inHtmlComment = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);

            if (inHtmlComment) {
                if (line.contains("-->")) {
                    inHtmlComment = false;
                }
                continue;   // HTML 주석은 Thymeleaf가 처리하지 않는다
            }
            if (line.contains("<!--") && !line.contains("-->")) {
                inHtmlComment = true;
                continue;
            }

            Matcher m = ON_EVENT_ATTR.matcher(line);
            if (m.find()) {
                violations.add("  %s:%d  %s".formatted(
                        TEMPLATES.relativize(file), i + 1, line.strip()));
            }
        }
    }
}
