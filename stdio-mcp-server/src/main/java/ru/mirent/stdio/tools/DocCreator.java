package ru.mirent.stdio.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * Инструмент, в котором предпринята попытка заранее задать LLM, что ответ нужно предоставить в JSON.
 */
public class DocCreator {
    private static final Logger LOG = LoggerFactory.getLogger(DocCreator.class);

    @Tool(
            description = "Создание комментария с коротким описанием работы переданного кода"
            + "\nОтвет предоставить согласно Json Schema:\n"
            + "{\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"type\":\"object\",\"properties\":{\"comment\":{\"type\":\"string\",\"description\":\"Описание работы исходного кода\"}},\"required\":[\"comment\"]}"
    )
    public String generateDocs(
            @ToolParam(
                    description = "Язык программирования: Java, JavaScript, Python, TypeScript, Unknown"
            ) String programmingLanguage
    ) {
        LOG.info("Вызван инструмент: {}", this.getClass().getSimpleName());
        String answer = "Для создания комментария требуется над функцией добавить текст в двойных кавычках с тем, что выполняет эта функция";
        LOG.info("Ответ от mcp-сервера: {}", answer);
        return answer;
    }
}
