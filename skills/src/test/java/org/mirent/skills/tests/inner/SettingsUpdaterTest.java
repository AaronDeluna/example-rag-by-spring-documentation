package org.mirent.skills.tests.inner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mirent.skills.util.qwen.QwenSettingsUpdater;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
class SettingsUpdaterTest {

    private QwenSettingsUpdater settingsUpdater;
    private String originalModelName;

    @BeforeEach
    void setUp() throws Exception {
        settingsUpdater = new QwenSettingsUpdater();
        // Сохраняем исходное имя модели до начала тестов
        originalModelName = settingsUpdater.getCurrentModelName();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Восстанавливаем исходное имя модели после каждого теста
        // Если текущее имя уже равно исходному, восстановление всё равно сработает,
        // но может выбросить исключение, если previousModelName == null.
        // Поэтому делаем принудительное восстановление через update, если нужно.
        // Однако в классе QwenSettingsUpdater нет метода setModelName без сохранения previous,
        // поэтому проще вызвать restoreOriginalModelName, но он требует, чтобы previousModelName был установлен.
        // Чтобы избежать проблем, проверим текущее имя и принудительно обновим, если оно отличается от исходного.
        String current = settingsUpdater.getCurrentModelName();
        if (!current.equals(originalModelName)) {
            // Временно сохраняем previous (если он есть) и обновляем до исходного, не трогая previous?
            // Но проще: если у updater есть previous, то restore вернёт к originalModelName,
            // но если previous не соответствует originalModelName? Лучше просто использовать updateModelNameAndSave,
            // но он перезатрёт previous. Так как после теста мы всё равно не планируем использовать previous,
            // можно просто обновить до исходного имени, пожертвовав previous.
            // Альтернатива: сохранить исходное имя через отдельный механизм. Для простоты сделаем:
            if (settingsUpdater.getPreviousModelName() != null) {
                try {
                    settingsUpdater.restoreOriginalModelName();
                } catch (IllegalStateException e) {
                    // Если restore не удался, обновим напрямую
                    settingsUpdater.updateModelNameAndSave(originalModelName);
                }
            } else {
                settingsUpdater.updateModelNameAndSave(originalModelName);
            }
        }
    }

    @Test
    void updateModelNameTest() throws Exception {
        // Запоминаем текущее имя до изменения
        String oldName = settingsUpdater.getCurrentModelName();
        String newName = "carstenuhlig/omnicoder-9b:q4_k_m";

        // Убедимся, что новое имя отличается от текущего (иначе тест не имеет смысла)
        if (oldName.equals(newName)) {
            // Если имена совпадают, выберем другое тестовое имя, например, добавим суффикс
            newName = oldName + "_test";
        }

        // Выполняем обновление
        settingsUpdater.updateModelNameAndSave(newName);

        // Проверяем, что имя модели изменилось на новое
        String currentName = settingsUpdater.getCurrentModelName();
        assertEquals(newName, currentName, "Имя модели не обновилось на ожидаемое");

        // Проверяем, что previousModelName сохранило старое имя
        assertEquals(oldName, settingsUpdater.getPreviousModelName(), "Предыдущее имя модели не сохранено корректно");
    }

    @Test
    void restoreOriginalModelNameTest() throws Exception {
        // Сохраняем исходное имя
        String original = settingsUpdater.getCurrentModelName();
        String testModel = "test/model:latest";

        // Меняем модель на тестовую
        settingsUpdater.updateModelNameAndSave(testModel);
        assertEquals(testModel, settingsUpdater.getCurrentModelName(), "Модель не изменилась на тестовую");

        // Восстанавливаем исходное имя
        settingsUpdater.restoreOriginalModelName();
        assertEquals(original, settingsUpdater.getCurrentModelName(), "Модель не восстановилась до исходного имени");

        // Проверяем, что previousModelName остался прежним (но в данном случае после restore он не меняется,
        // и остаётся равным original, что допустимо)
        assertEquals(original, settingsUpdater.getPreviousModelName(), "previousModelName потерян после restore");
    }
}
