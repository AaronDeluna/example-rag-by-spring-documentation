package org.mirent.skills.util.qwen;

import org.mirent.skills.exeptions.QwenCommandFactoryException;

import java.nio.file.Path;
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
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac")) {
            return List.of("qwen", prompt, "--output-format", "stream-json", "--approval-mode", "yolo");
        }
        if (osName.contains("linux")) {
            // На Linux qwen-cli ставится через npm-global,
            // запускаем напрямую js-файл (у него есть shebang).
            String userHome = System.getProperty("user.home");
            Path cli = Path.of(userHome, ".npm-global", "lib", "node_modules", "@qwen-code", "qwen-code", "cli.js");
            return List.of(cli.toString(), "--output-format", "stream-json", "--approval-mode", "yolo", prompt);
        }
        if (osName.contains("win")) {
            return List.of("cmd.exe", "/c", "qwen", prompt, "--output-format", "stream-json", "--approval-mode", "yolo");
        }
        throw new QwenCommandFactoryException("Неизвестная операционная система: " + osName);
    }
}
