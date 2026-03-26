package ru.mirent.services;

import ru.mirent.security.ClassNameValidator;
import ru.mirent.security.PathValidator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Сервис декомпиляции Java-классов через CFR
 */
public class DecompilationService {

    private static final Path OUTPUT_DIR = Paths.get("/tmp/cfr-decompiled");
    private static final int TIMEOUT_SECONDS = 60;

    /**
     * Получить схему класса (пакет, импорты, поля, сигнатуры методов без тел)
     */
    public String getClassOutline(String jarPath, String className) {
        // Валидация FQN класса
        ClassNameValidator.validateFQN(className);

        try {
            Path javaFile = ensureDecompiled(jarPath, className);
            List<String> lines = Files.readAllLines(javaFile);
            List<String> outline = new ArrayList<>();
            int depth = 0;

            for (String line : lines) {
                String stripped = line.strip();
                int opens = countChar(stripped, '{');
                int closes = countChar(stripped, '}');

                if (depth == 0) {
                    outline.add(line);
                    depth += opens - closes;
                } else if (depth == 1) {
                    if (opens > closes) {
                        outline.add(line);
                        outline.add("    // ...");
                        depth += opens - closes;
                    } else {
                        outline.add(line);
                        depth += opens - closes;
                    }
                } else {
                    depth += opens - closes;
                    if (depth == 1) {
                        outline.add("    }");
                    }
                }
            }

            return String.join("\n", outline);
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }

    /**
     * Получить исходный код метода по имени
     */
    public String getMethodSource(String jarPath, String className, String methodName) {
        // Валидация FQN класса и имени метода
        ClassNameValidator.validateFQN(className);
        ClassNameValidator.validateFQN(methodName);

        try {
            Path javaFile = ensureDecompiled(jarPath, className);
            List<String> lines = Files.readAllLines(javaFile);
            List<String> results = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(methodName) + "\\s*\\(");
            int i = 0;

            while (i < lines.size()) {
                String line = lines.get(i);
                if (pattern.matcher(line).find() && !line.strip().startsWith("/")) {
                    List<String> block = new ArrayList<>();
                    block.add(line);
                    int depth = countChar(line, '(') - countChar(line, ')');
                    i++;
                    while (i < lines.size() && depth > 0) {
                        block.add(lines.get(i));
                        depth += countChar(lines.get(i), '(') - countChar(lines.get(i), ')');
                        i++;
                    }
                    results.add(String.join("\n", block));
                } else {
                    i++;
                }
            }

            if (results.isEmpty()) {
                return String.format("Метод '%s' не найден в %s.", methodName, className);
            }

            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < results.size(); j++) {
                if (j > 0) {
                    sb.append("\n\n// --- перегрузка ---\n\n");
                }
                sb.append(results.get(j));
            }

            return sb.toString();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }

    /**
     * Полная декомпиляция класса
     */
    public String decompileClass(String jarPath, String classFqn) {
        // Валидация FQN класса
        ClassNameValidator.validateFQN(classFqn);

        try {
            Path javaFile = ensureDecompiled(jarPath, classFqn);
            String source = Files.readString(javaFile);
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("// %s\n// JAR: %s\n// %s\n",
                    classFqn, Paths.get(jarPath).getFileName(), "=".repeat(60)));
            sb.append(source);
            return sb.toString();
        } catch (Exception e) {
            return "ОШИБКА: " + e.getMessage();
        }
    }

    /**
     * Package-private метод для тестирования
     */
    int countChar(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    private Path ensureDecompiled(String jarPath, String classFqn) throws IOException {
        // Валидация пути к JAR
        Path validatedJarPath = PathValidator.validateJarPath(jarPath);

        Path javaFile = OUTPUT_DIR.resolve(
            classFqn.replace('.', File.separatorChar) + ".java"
        );
        if (Files.exists(javaFile)) {
            return javaFile;
        }

        Path jar = validatedJarPath;
        if (!Files.exists(jar)) {
            throw new IOException("JAR не найден: " + jarPath);
        }

        Path cfrJar = getCFRJar();
        if (!Files.exists(cfrJar)) {
            throw new IOException("CFR JAR не найден: " + cfrJar);
        }

        Files.createDirectories(OUTPUT_DIR);

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", cfrJar.toString(), jar.toString(), classFqn,
                "--outputdir", OUTPUT_DIR.toString(),
                "--silent", "false"
        );
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        int exitCode;
        try {
            if (!proc.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                proc.destroyForcibly();
                throw new IOException("Таймаут декомпиляции (" + TIMEOUT_SECONDS + " сек)");
            }
            exitCode = proc.exitValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            proc.destroyForcibly();
            throw new IOException("Декомпиляция прервана", e);
        }

        if (exitCode != 0 && !Files.exists(javaFile)) {
            throw new IOException(String.format(
                    "Декомпиляция не дала результата.\nstdout: %s", output));
        }

        if (!Files.exists(javaFile)) {
            throw new IOException("Декомпиляция не дала результата.");
        }

        return javaFile;
    }

    private Path getCFRJar() {
        try {
            Path currentJar = Paths.get(
                DecompilationService.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParent();
            if (currentJar != null && Files.exists(currentJar.resolve("cfr-0.152.jar"))) {
                return currentJar.resolve("cfr-0.152.jar");
            }
        } catch (Exception e) {
        }
        return Paths.get("cfr-0.152.jar");
    }
}
