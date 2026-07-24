package org.mirent.skills.tests.exemple;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.SkillsFileUtils;
import org.mirent.skills.util.WutPreparer;

import java.nio.file.Path;
import java.util.Map;

/**
 * Демонстрационный тест: без запуска агента печатает в консоль, что вернёт
 * {@link SkillsFileUtils#readSkillsFromLog(String)} — карту «имя скилла → полный скилл».
 * <p>
 * На вход методу даётся ЛОГ прогона (JSON-объект {@code { "skillName":..., "events":[...] }},
 * как пишет {@code RunnerLogWriter}). Реальный workspace готовит {@link WutPreparer},
 * а {@code cwd} в логе подменяется на него, чтобы скилл читался с диска.
 */
public class SkillReadDemoTest {

    @Test
    @DisplayName("Демо: readSkillsFromLog(log) -> Map<имя, полный скилл> (вывод в консоль)")
    void demo() throws Exception {
        // 1. Реальный workspace со скиллами (шаблон default → .qwen/skills/arithmetic, ...)
        Path workspace = WutPreparer.builder()
                .wutSourceName("default")
                .wutSourcePath(Path.of("src/test/resources/wut-templates"))
                .build()
                .prepare();
        String cwd = workspace.toAbsolutePath().toString();

        // 2. Лог прогона как объект (skillName — явный вызов; tool_use в events — неявный). cwd = свежий workspace.
        String logJson = ("""
                {
                  "runId" : "1ffe9a1c-1548-4e4e-af9a-fc42796b31b8",
                  "skillName" : "arithmetic",
                  "finalResult" : "4",
                  "events" : [
                    { "type" : "system", "subtype" : "init", "cwd" : "CWD_PLACEHOLDER" },
                    { "type" : "assistant",
                      "message" : { "content" : [
                        { "type" : "tool_use", "name" : "skill", "input" : { "skill" : "arithmetic" } }
                      ] } },
                    { "type" : "result", "subtype" : "success", "result" : "4" }
                  ]
                }
                """).replace("CWD_PLACEHOLDER", cwd.replace("\\", "\\\\"));

        // 3. Один вызов: даём лог -> получаем Map<имя скилла, полный скилл>
        Map<String, String> skills = SkillsFileUtils.readSkillsFromLog(logJson);

        // 4. Печать в консоль
        System.out.println("\n########## readSkillsFromLog(log) ##########");
        System.out.println("вызвано скиллов: " + skills.size() + " -> " + skills.keySet());
        skills.forEach((name, body) -> {
            System.out.println("\n----- скилл: " + name + " -----");
            System.out.println(body);
        });
        System.out.println("\n########## конец демо ##########");
    }
}
