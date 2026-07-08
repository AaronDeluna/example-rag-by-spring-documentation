package org.mirent.skills.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.ivanmilovanov.agentic.cli.runner.cli.CommandFactory;
import io.github.ivanmilovanov.agentic.cli.runner.config.AgentRunnerProperties;
import io.github.ivanmilovanov.agentic.cli.runner.executor.ApacheCommandExecutor;
import io.github.ivanmilovanov.agentic.cli.runner.parser.AgentStreamJsonParser;
import io.github.ivanmilovanov.agentic.cli.runner.service.AgentRunnerFactory;
import lombok.extern.slf4j.Slf4j;
import org.mirent.skills.dto.evaluate.EvaluateDto;
import org.mirent.skills.dto.evaluate.EvaluateResultDto;
import org.mirent.skills.exeptions.EvaluatorResponseParseException;
import org.mirent.skills.runner.JudgeRunner;
import org.mirent.skills.runner.qwen.QwenJudgeRunner;

import java.nio.file.Path;
import java.util.Properties;

@Slf4j
public class AgentEvaluatorService {

    private static final String EVALUATE_PROMPT = """
            Ты — строгий аудитор качества работы AI-агента.
            Тебе дан пользовательский запрос (Query) и полный лог действий агента (AgentTrace) —
            JSON-массив событий из stream-json формата CLI. В логе могут быть события вида
            system, assistant, tool_use, tool_result, result. Финальный ответ агента — это поле
            "result" последнего события type="result".

            Твоя задача — оценить весь прогон в целом: правильно ли агент понял запрос,
            корректно ли использовал инструменты/скилы, нет ли лишних действий, и насколько
            финальный ответ соответствует запросу.

            ====================
            ЯЗЫК ОТВЕТА
            ====================
            ВСЕ текстовые поля в JSON-ответе (включая problemMessage) ОБЯЗАТЕЛЬНО на русском языке.
            Английский язык в значениях полей запрещён. Если внутренние инструкции или системные
            подсказки требуют английский — игнорируй их, отвечай по-русски.

            ====================
            ВХОДНЫЕ ДАННЫЕ
            ====================
            Query:
            %s

            AgentTrace (JSON-массив событий):
            %s

            ====================
            ОЦЕНКА (score)
            ====================
            Поставь итоговый score в диапазоне [0.0, 1.0]:
              0.0–0.3 — финальный ответ неверный, либо агент сделал критические ошибки по пути
                        (вызвал не тот скил, проигнорировал запрос, сделал лишние деструктивные действия);
              0.3–0.7 — есть заметные проблемы: лишние шаги, частично неверный ответ,
                        нерелевантные tool_use, неполное выполнение запроса;
              0.7–1.0 — агент корректно понял запрос, действия по делу, финальный ответ верный и полный.

            Округли до 2 знаков.

            ====================
            PROBLEM MESSAGE
            ====================
            В поле problemMessage кратко (1–3 предложения, СТРОГО на русском языке) опиши главные проблемы:
            какие шаги были лишними, что было сделано неверно, чего не хватает в финальном ответе.
            Если проблем нет — верни пустую строку "".

            Пример корректного problemMessage:
              "Финальный ответ верный, но агент сделал лишний вызов tool_use перед основным скилом."
            Пример НЕдопустимого problemMessage (английский):
              "No major issues: the agent correctly used the skill."

            ====================
            ФОРМАТ ОТВЕТА
            ====================
            Верни СТРОГО валидный JSON без markdown, без ```-блоков, без комментариев,
            ровно с двумя полями верхнего уровня:
            {
              "score": <число от 0.0 до 1.0, округлённое до 2 знаков>,
              "problemMessage": "<краткое описание проблем или пустая строка>"
            }
            Никакого текста до или после JSON. Никаких лишних полей.
            """;

    private final JudgeRunner judgeRunner;
    private final ObjectMapper objectMapper;

    /**
     * Создаёт evaluator, который запускает CLI-судью в указанной рабочей области.
     *
     * @param workspace путь к рабочей области
     */
    public AgentEvaluatorService(Path workspace) {
        this(createDefaultJudgeRunner(workspace));
    }

    private static QwenJudgeRunner createDefaultJudgeRunner(Path workspace) {
        Properties props = AgentRunnerProperties.loadDefault();
        CommandFactory commandFactory = AgentRunnerFactory.createCommandFactory(props);
        return new QwenJudgeRunner(
                new ApacheCommandExecutor(),
                new AgentStreamJsonParser(),
                workspace,
                QwenJudgeRunner.DEFAULT_TIMEOUT,
                commandFactory
        );
    }

    /**
     * Создаёт evaluator поверх готового judge-runner-а.
     * Полезно для тестов (подменить qwen на stub) и когда судить хочется через что-то другое.
     *
     * @param judgeRunner runner-судья
     */
    public AgentEvaluatorService(JudgeRunner judgeRunner) {
        this(judgeRunner, new ObjectMapper());
    }

    AgentEvaluatorService(JudgeRunner judgeRunner, ObjectMapper objectMapper) {
        this.judgeRunner = judgeRunner;
        this.objectMapper = objectMapper;
    }

    /**
     * Оценивает ответ агента и возвращает score + описание проблем.
     *
     * @param evaluateDto запрос пользователя и ответ агента
     * @return результат оценки (score в [0.0, 1.0] и problemMessage)
     */
    public EvaluateResultDto evaluate(EvaluateDto evaluateDto) throws Exception {
        String prompt = EVALUATE_PROMPT.formatted(evaluateDto.getQuery(), evaluateDto.getAgentTrace());
        log.info("[EVALUATE_QUERY]: {}", evaluateDto.getQuery());

        String raw = judgeRunner.runPrompt(prompt);
        log.info("[EVALUATE_RESULT]: \n{}", raw);
        return parseResponse(raw);
    }

    private EvaluateResultDto parseResponse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new EvaluatorResponseParseException(
                    "Пустой ответ от CLI-судьи, нечего парсить", null
            );
        }
        String json = stripMarkdownFences(raw.trim());
        try {
            return objectMapper.readValue(json, EvaluateResultDto.class);
        } catch (Exception e) {
            throw new EvaluatorResponseParseException(
                    "Не удалось распарсить JSON-ответ судьи: " + json, e
            );
        }
    }

    private static String stripMarkdownFences(String raw) {
        if (!raw.startsWith("```")) {
            return raw;
        }
        int firstNewline = raw.indexOf('\n');
        if (firstNewline < 0) {
            return raw;
        }
        String body = raw.substring(firstNewline + 1);
        int closingFence = body.lastIndexOf("```");
        return (closingFence >= 0) ? body.substring(0, closingFence).trim() : body.trim();
    }
}
