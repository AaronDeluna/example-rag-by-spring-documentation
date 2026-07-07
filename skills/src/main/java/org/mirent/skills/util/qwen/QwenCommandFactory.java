package org.mirent.skills.util.qwen;

import org.mirent.skills.exeptions.QwenCommandFactoryException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class QwenCommandFactory {

    private QwenCommandFactory() {
    }

    /**
     * Собирает командную строку для запуска qwen one-shot в stream-json формате.
     * Подбирает путь к CLI под текущую ОС.
     *
     * @param prompt пользовательский prompt (последний или предпоследний аргумент в зависимости от ОС)
     * @return список аргументов для {@link ProcessBuilder}-подобного запуска
     */
    public static List<String> buildCommand(String prompt) {
        return buildCommand(prompt, null);
    }

    /**
     * Собирает командную строку для запуска qwen one-shot в stream-json формате.
     * Подбирает путь к CLI под текущую ОС.
     * При наличии {@code openaiLogDir} добавляет --openai-logging и --openai-logging-dir.
     *
     * @param prompt       пользовательский prompt
     * @param openaiLogDir директория для OpenAI-логов (может быть {@code null})
     * @return список аргументов для {@link ProcessBuilder}-подобного запуска
     */
    public static List<String> buildCommand(String prompt, Path openaiLogDir) {
        String osName = System.getProperty("os.name").toLowerCase();
        List<String> command = new ArrayList<>();

        if (osName.contains("mac")) {
            command.add("qwen");
            command.add(prompt);
            command.add("--output-format");
            command.add("stream-json");
            command.add("--approval-mode");
            command.add("yolo");
        } else if (osName.contains("linux")) {
            String userHome = System.getProperty("user.home");
//            qwenPath = Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli-entry.js");
            Path cli = Path.of(userHome, ".npm-global", "bin", "qwen");
            command.add(cli.toString());
            command.add("--output-format");
            command.add("stream-json");
            command.add("--approval-mode");
            command.add("yolo");
        } else if (osName.contains("win")) {
            command.add("cmd.exe");
            command.add("/c");
            command.add("qwen");
            command.add(prompt);
            command.add("--output-format");
            command.add("stream-json");
            command.add("--approval-mode");
            command.add("yolo");
        } else {
            throw new QwenCommandFactoryException("Неизвестная операционная система: " + osName);
        }

        if (openaiLogDir != null) {
            command.add("--openai-logging");
            command.add("true");
            command.add("--openai-logging-dir");
            command.add(openaiLogDir.toAbsolutePath().toString());
        }

        // На Linux prompt — последний аргумент (после всех флагов)
        if (osName.contains("linux")) {
            command.add(prompt);
        }

        return Collections.unmodifiableList(command);
    }
}
