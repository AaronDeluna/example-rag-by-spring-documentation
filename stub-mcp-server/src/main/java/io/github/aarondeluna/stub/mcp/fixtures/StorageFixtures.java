package io.github.aarondeluna.stub.mcp.fixtures;

/**
 * Хардкоженные фикстуры для инструмента {@code search_storage}.
 *
 * <p>Ответ — ТЕКСТ (не JSON). Ветка {@code found} — найдено решение в базе знаний,
 * ветка {@code not_found} — совпадений нет.
 *
 * <p>Чтобы заменить заглушку на реальную логику — правьте методы этого класса.
 */
public final class StorageFixtures {

    private StorageFixtures() {
    }

    /** Найдено готовое решение в базе знаний. */
    public static String found() {
        return """
                FOUND
                match_id: kb-042
                signature: java.lang.NullPointerException at OrderService.calcTotal
                solution: Добавить null-check на discount перед calcTotal. Исправлено в PR #1234.
                confidence: 0.92""";
    }

    /** Совпадений в базе знаний нет. */
    public static String notFound() {
        return "NOT_FOUND";
    }
}
