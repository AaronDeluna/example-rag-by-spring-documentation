package org.mirent.skills.util.qwen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

import static org.mirent.skills.runner.qwen.QwenAgentRunner.resolveDefaultWorkingDirectory;

@Slf4j
public class QwenSettingsUpdater {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private String previousModelName = null;

    /**
     * Обновляет имя модели в settings.json и сохраняет файл.
     * Перед изменением сохраняет текущее значение как "предыдущее".
     *
     * @param newModelName новое имя модели
     */
    public void updateModelNameAndSave(String newModelName) throws Exception {
        log.debug("Начало обновления имени модели на: {}", newModelName);
        Path workDir = resolveDefaultWorkingDirectory();
        Path settingsPath = workDir.resolve(".qwen").resolve("settings.json");
        File settingsFile = settingsPath.toFile();
        if (!settingsFile.exists()) {
            log.error("Файл settings.json не найден по пути: {}", settingsPath);
            throw new IllegalStateException("Файл settings.json не найден по пути: " + settingsPath);
        }

        try {
            ObjectNode root = (ObjectNode) OBJECT_MAPPER.readTree(settingsFile);
            String currentName = root.get("model").get("name").asText();
            previousModelName = currentName;
            log.info("Текущее имя модели '{}' сохранено как предыдущее", currentName);

            ((ObjectNode) root.get("model")).put("name", newModelName);

            String updatedJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root);
            log.info("Обновленная конфигурация:\n{}", updatedJson);

            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(settingsFile, root);
            log.info("Имя модели успешно обновлено с '{}' на '{}'", currentName, newModelName);
        } catch (Exception e) {
            log.error("Ошибка при обновлении имени модели: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Восстанавливает исходное имя модели (которое было до последнего вызова updateModelNameAndSave).
     * Если предыдущее значение отсутствует (ни разу не вызывали update), то метод ничего не делает.
     *
     * @throws IllegalStateException если предыдущее имя не сохранено
     */
    public void restoreOriginalModelName() throws Exception {
        log.debug("Начало восстановления исходного имени модели");
        if (previousModelName == null) {
            log.warn("Попытка восстановления, но предыдущее имя модели не сохранено");
            throw new IllegalStateException("Нет сохранённого предыдущего имени модели. Сначала вызовите updateModelNameAndSave.");
        }

        Path workDir = resolveDefaultWorkingDirectory();
        Path settingsPath = workDir.resolve(".qwen").resolve("settings.json");
        File settingsFile = settingsPath.toFile();
        if (!settingsFile.exists()) {
            log.error("Файл settings.json не найден по пути: {}", settingsPath);
            throw new IllegalStateException("Файл settings.json не найден по пути: " + settingsPath);
        }

        try {
            ObjectNode root = (ObjectNode) OBJECT_MAPPER.readTree(settingsFile);
            ((ObjectNode) root.get("model")).put("name", previousModelName);
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(settingsFile, root);
            log.info("Имя модели восстановлено до '{}'", previousModelName);
        } catch (Exception e) {
            log.error("Ошибка при восстановлении имени модели: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Возвращает текущее имя модели из файла settings.json.
     */
    public String getCurrentModelName() throws Exception {
        log.debug("Получение текущего имени модели");
        Path workDir = resolveDefaultWorkingDirectory();
        Path settingsPath = workDir.resolve(".qwen").resolve("settings.json");
        try {
            ObjectNode root = (ObjectNode) OBJECT_MAPPER.readTree(settingsPath.toFile());
            String modelName = root.get("model").get("name").asText();
            log.debug("Текущее имя модели: {}", modelName);
            return modelName;
        } catch (Exception e) {
            log.error("Не удалось получить текущее имя модели: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Возвращает сохранённое предыдущее имя модели.
     */
    public String getPreviousModelName() {
        log.debug("Запрошено предыдущее имя модели: {}", previousModelName);
        return previousModelName;
    }
}
