package ru.mirent.security;

import java.util.regex.Pattern;

/**
 * Валидатор имён Java-классов (FQN — Fully Qualified Name)
 * <p>
 * Обеспечивает проверку полных имён классов на соответствие правилам Java:
 * - Только буквы, цифры, точка, $ (вложенные классы), _ (допустимо в Java)
 * - Запрещены: /, \, пробелы, специальные символы
 */
public class ClassNameValidator {

    // Разрешены: буквы, цифры, точка, $ (вложенные классы), _ (допустимо в Java)
    // Запрещены: /, \, пробелы, специальные символы
    private static final Pattern FQN_PATTERN = Pattern.compile(
        "^[a-zA-Z_][a-zA-Z0-9_$]*(\\.[a-zA-Z_][a-zA-Z0-9_$]*)*$"
    );

    private ClassNameValidator() {
        // Утилитный класс, не должен инстанцироваться
    }

    /**
     * Проверить полное имя класса (FQN)
     *
     * @param fqn полное имя класса, например "com.example.MyClass"
     * @return true если имя корректно
     */
    public static boolean isValidFQN(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            return false;
        }

        // Проверка на наличие недопустимых последовательностей
        if (fqn.contains("..")) {
            return false;
        }

        if (fqn.contains("/") || fqn.contains("\\")) {
            return false;
        }

        if (fqn.contains(" ")) {
            return false;
        }

        // Проверка по regex
        return FQN_PATTERN.matcher(fqn).matches();
    }

    /**
     * Проверить и выбросить исключение при ошибке
     *
     * @param fqn полное имя класса
     * @throws IllegalArgumentException если имя некорректно
     */
    public static void validateFQN(String fqn) {
        if (fqn == null || fqn.isEmpty()) {
            throw new IllegalArgumentException(
                "Имя класса не может быть пустым"
            );
        }

        if (fqn.contains("..")) {
            throw new IllegalArgumentException(
                "Недопустимая последовательность '..' в имени класса: " + fqn
            );
        }

        if (fqn.contains("/") || fqn.contains("\\")) {
            throw new IllegalArgumentException(
                "Имя класса должно использовать точку (.) вместо разделителей пути: " + fqn
            );
        }

        if (fqn.contains(" ")) {
            throw new IllegalArgumentException(
                "Имя класса не должно содержать пробелы: " + fqn
            );
        }

        if (!FQN_PATTERN.matcher(fqn).matches()) {
            throw new IllegalArgumentException(
                "Недопустимые символы в имени класса. Разрешены только буквы, цифры, " +
                    "точка (.), подчёркивание (_) и доллар ($): " + fqn
            );
        }
    }

    /**
     * Проверить простое имя класса (без пакета)
     *
     * @param simpleName простое имя, например "MyClass"
     * @return true если имя корректно
     */
    public static boolean isValidSimpleClassName(String simpleName) {
        if (simpleName == null || simpleName.isEmpty()) {
            return false;
        }

        // Простое имя не должно содержать точку
        if (simpleName.contains(".")) {
            return false;
        }

        return FQN_PATTERN.matcher(simpleName).matches();
    }
}
