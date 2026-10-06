package com.ghlove.admin;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 2026-10-06 결함 재발 방지 - {@code th:inline="javascript"} 블록 안의 여는 대괄호 두 개를 막는다.
 *
 * <p>AS-IS 주석을 그대로 옮긴 중첩 배열 예시({@code [["MS UI Gothic", ...]]})가 Thymeleaf JS
 * 인라인 식으로 파싱돼 <b>"Could not parse as expression"으로 렌더링이 중단</b>됐다. 응답은 이미
 * 커밋된 상태라 브라우저는 {@code ERR_INCOMPLETE_CHUNKED_ENCODING 200}을 받고 페이지 아래쪽이
 * 통째로 사라진다 - 팝업 등록화면에서 에디터와 달력이 동시에 사라진 원인이고, 그 조각을 공유하는
 * <b>16개 화면</b>이 같이 깨져 있었다. <b>JS 주석 안도 예외가 아니다</b>(텍스트 노드로 처리된다).
 *
 * <p>컴파일·기동으로는 잡히지 않는 결함이라(요청이 와서 렌더링해야 터진다) 소스 규칙으로 막는다.
 * 규칙: 인라인 JS 블록의 여는 대괄호 두 개는 반드시 {@code ${...}}/{@code @{...}}/{@code #{...}}가
 * 바로 뒤따라야 한다. 중첩 배열을 쓰려면 한 칸 띄운다. HTML 주석은 처리 대상이 아니라 안전하다.
 */
class ThymeleafInlineHazardTest {

    private static final Path TEMPLATES = Paths.get("src", "main", "resources", "templates");

    @Test
    void 인라인JS블록에_식이_아닌_여는대괄호2개가_없다() throws IOException {
        List<String> violations = new ArrayList<>();
        try (Stream<Path> files = Files.walk(TEMPLATES)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".html")).toList()) {
                scan(file, violations);
            }
        }
        assertTrue(violations.isEmpty(),
                "인라인 JS 블록에서 식이 아닌 '[' + '['를 찾았다 - 렌더링이 중단된다:\n"
                        + String.join("\n", violations));
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

            // ★ Thymeleaf 3는 HTML 모드에서 인라인이 기본 활성이라 th:inline이 없는 <script>나
            //   일반 텍스트의 여는 대괄호 두 개도 식으로 처리된다 → 주석 밖 전부를 본다.
            int at = line.indexOf("[[");
            while (at >= 0) {
                String rest = line.substring(at + 2).stripLeading();
                boolean expression = rest.startsWith("${") || rest.startsWith("@{") || rest.startsWith("#{");
                if (!expression) {
                    violations.add("  %s:%d  %s".formatted(
                            TEMPLATES.relativize(file), i + 1, line.strip()));
                    break;
                }
                at = line.indexOf("[[", at + 2);
            }
        }
    }
}
