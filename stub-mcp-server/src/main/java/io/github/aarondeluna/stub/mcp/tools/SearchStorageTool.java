package io.github.aarondeluna.stub.mcp.tools;

import io.github.aarondeluna.stub.mcp.fixtures.StorageFixtures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Инструмент {@code search_storage}: ищет готовое решение в базе знаний.
 *
 * <p>Заглушка. Ответ — ТЕКСТ (не JSON). Поведение по умолчанию: если во входе
 * встречается сигнатура {@code NullPointerException at OrderService} или
 * {@code IllegalStateException at PaymentService} — ветка {@code found}, иначе —
 * {@code not_found}. Аргумент {@code scenario} ({@code found}|{@code not_found})
 * перекрывает поведение по умолчанию.
 */
@Component
public class SearchStorageTool {

    private static final Logger log = LoggerFactory.getLogger(SearchStorageTool.class);

    @Tool(name = "search_storage",
            description = "Ищет готовое решение по ошибкам в базе знаний; возвращает ТЕКСТ (FOUND ... | NOT_FOUND)")
    public String searchStorage(
            @ToolParam(description = "Строка с JSON ошибок/кластеров для поиска решения")
            String errors_json,
            @ToolParam(required = false,
                    description = "Форсировать ветку: found | not_found. Если не задано — определяется по содержимому")
            String scenario) {

        String branch = resolveBranch(errors_json, scenario);
        log.info("[search_storage] scenario={} inputLen={} -> ветка {}",
                scenario, errors_json == null ? 0 : errors_json.length(), branch);

        return "found".equals(branch) ? StorageFixtures.found() : StorageFixtures.notFound();
    }

    /** Выбирает ветку: явный scenario приоритетнее эвристики по сигнатурам. */
    private String resolveBranch(String errorsJson, String scenario) {
        if (StringUtils.hasText(scenario)) {
            return "found".equalsIgnoreCase(scenario.trim()) ? "found" : "not_found";
        }
        String input = errorsJson == null ? "" : errorsJson;
        boolean found = input.contains("NullPointerException at OrderService")
                || input.contains("IllegalStateException at PaymentService");
        return found ? "found" : "not_found";
    }
}
