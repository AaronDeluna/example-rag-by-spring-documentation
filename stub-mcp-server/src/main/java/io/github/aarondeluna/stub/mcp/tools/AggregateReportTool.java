package io.github.aarondeluna.stub.mcp.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.aarondeluna.stub.mcp.fixtures.ReportFixtures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * Инструмент {@code aggregate_report}: собирает итоговый отчёт по устранённой ошибке.
 *
 * <p>Заглушка. Классификация: если во входном JSON задано поле
 * {@code classification} — используется оно; иначе определяется по содержимому
 * (NPE/ISE → {@code bugfix}, timeout/unknown → {@code incident}).
 */
@Component
public class AggregateReportTool {

    private static final Logger log = LoggerFactory.getLogger(AggregateReportTool.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Tool(name = "aggregate_report",
            description = "Собирает итоговый отчёт по ошибке; возвращает JSON с classification, рекомендациями и fix_plan")
    public String aggregateReport(
            @ToolParam(description = "Строка с JSON: агрегированные данные по ошибке и её решению")
            String input_json) {

        String classification = resolveClassification(input_json);
        log.info("[aggregate_report] inputLen={} -> classification {}",
                input_json == null ? 0 : input_json.length(), classification);

        return "incident".equals(classification) ? ReportFixtures.incident() : ReportFixtures.bugfix();
    }

    /** Определяет классификацию: явное поле classification приоритетнее эвристики. */
    private String resolveClassification(String inputJson) {
        String input = inputJson == null ? "" : inputJson;

        String explicit = readClassificationField(input);
        if (explicit != null) {
            return "incident".equalsIgnoreCase(explicit) ? "incident" : "bugfix";
        }

        boolean bugfix = input.contains("NullPointerException") || input.contains("IllegalStateException");
        return bugfix ? "bugfix" : "incident";
    }

    /** Пытается прочитать поле {@code classification} из входного JSON; null — если поля нет. */
    private String readClassificationField(String input) {
        if (input.isBlank()) {
            return null;
        }
        try {
            JsonNode root = MAPPER.readTree(input);
            JsonNode node = root.get("classification");
            if (node != null && node.isTextual() && !node.asText().isBlank()) {
                return node.asText().trim();
            }
        } catch (Exception e) {
            // Вход — не валидный JSON: падаем на эвристику по содержимому
            log.debug("[aggregate_report] вход не распознан как JSON, используем эвристику: {}", e.getMessage());
        }
        return null;
    }
}
