# TASK-029: Structurizr с PlantUML (structurizr-core)

## Информация о задаче

| Поле | Значение |
|------|----------|
| **Модуль:** | `common` |
| **ID:** | 029 |
| **Файл:** | `TASK-029_common_structurizr_plantuml_research.md` |
| **Приоритет:** | Средний |
| **Связь:** | [docs/global-plans.md](../../docs/global-plans.md), [TASK-026](../../.qwen/workplace/archive/TASK-026_common_structurizr_diagrams.md) |

---

## Описание

Требуется исследовать подключение Structurizr через зависимость `com.structurizr:structurizr-core` с генерацией PlantUML-диаграмм на основе примера из `StructurizrTest.java`.

**Контекст:**
- TASK-026 использовал Structurizr DSL и Docker для генерации
- Требуется альтернативный подход с использованием Java API
- Пример теста доступен: `webmvc-sync-mcp-server/src/test/java/ru/mirent/webmvc/StructurizrTest.java`
- Генерация через `StructurizrPlantUMLExporter`

**Цель:**
- Исследовать structurizr-core для программного создания диаграмм
- Реализовать генерацию PlantUML-диаграмм через Java API
- Создать рабочий тест с экспортом в .puml файлы
- Оценить применимость к проекту

## Критерии приёмки (Acceptance Criteria)

- [ ] Добавлена зависимость `com.structurizr:structurizr-core`
- [ ] Добавлена зависимость `net.sourceforge.plantuml:plantuml`
- [ ] Создан тест с генерацией PlantUML
- [ ] Диаграммы экспортированы в формате PlantUML (.puml)
- [ ] Реализована генерация PNG из PlantUML
- [ ] Созданы 3 уровня диаграмм (SystemContext, Container, Component)
- [ ] Подготовлена документация по использованию

## TDD Цикл

### 🔴 RED — Тесты

- [ ] Раскомментирован и доработан `StructurizrTest.java`
- [ ] Добавлен тест на экспорт PlantUML
- [ ] Тест падает (зависимость ещё не добавлена)

### 🟢 GREEN — Реализация

- [ ] Добавлена зависимость structurizr-core в pom.xml
- [ ] Добавлена зависимость plantuml в pom.xml
- [ ] Создана модель проекта (Person, SoftwareSystem, Container, Component)
- [ ] Созданы представления (SystemContextView, ContainerView, ComponentView)
- [ ] Реализован экспорт через StructurizrPlantUMLExporter
- [ ] Реализована генерация PNG через net.sourceforge.plantuml
- [ ] Все тесты проходят

### 🔵 REFACTOR — Рефакторинг

- [ ] Код рефакторен, устранено дублирование
- [ ] Выделен отдельный класс для создания модели
- [ ] Сборка успешна: `mvn clean package`

## Работа с существующим кодом

- [ ] Изучен `StructurizrTest.java` (закомментированный тест)
- [ ] Изучена документация structurizr-core
- [ ] Проверена совместимость с текущими зависимостями

## Чек-лист завершения

- [ ] Все тесты зелёные
- [ ] Сборка успешна
- [ ] .puml файлы сохранены в `docs/architecture/`
- [ ] .png файлы сохранены в `docs/architecture/`
- [ ] Код соответствует стандартам проекта
- [ ] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| **Модуль:** | `common` |
| Дата создания: | 2026-03-26 |
| Дата начала: | |
| Дата завершения: | |
| Статус: | 📋 |

## Заметки

### Зависимости

```xml
<!-- Structurizr Core API -->
<dependency>
    <groupId>com.structurizr</groupId>
    <artifactId>structurizr-core</artifactId>
    <version>1.30.4</version>
</dependency>

<!-- PlantUML для генерации PNG -->
<dependency>
    <groupId>net.sourceforge.plantuml</groupId>
    <artifactId>plantuml</artifactId>
    <version>1.2024.5</version>
</dependency>
```

### Пример экспорта в PlantUML

```java
import com.structurizr.Workspace;
import com.structurizr.export.Diagram;
import com.structurizr.export.plantuml.StructurizrPlantUMLExporter;

Workspace workspace = new Workspace("Test", "Description");
// ... создание модели ...

Diagram diagram = new StructurizrPlantUMLExporter().export(view);
System.out.println(diagram.getDefinition());
```

### Пример генерации PNG из PlantUML

```java
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

String plantUmlSource = diagram.getDefinition();

SourceStringReader reader = new SourceStringReader(plantUmlSource);
ByteArrayOutputStream os = new ByteArrayOutputStream();

// Генерация PNG
reader.outputImage(os, new FileFormatOption(FileFormat.PNG));

// Сохранение в файл
File pngFile = new File("docs/architecture/diagram.png");
try (FileOutputStream fos = new FileOutputStream(pngFile)) {
    fos.write(os.toByteArray());
}
```

### Полный процесс генерации

1. **Создание модели** через Structurizr Core API
2. **Создание представлений** (SystemContextView, ContainerView, ComponentView)
3. **Экспорт в PlantUML** через `StructurizrPlantUMLExporter`
4. **Генерация PNG** через `net.sourceforge.plantuml.SourceStringReader`
5. **Сохранение файлов** (.puml и .png)

### Ссылки

- [Пример теста](../../webmvc-sync-mcp-server/src/test/java/ru/mirent/webmvc/StructurizrTest.java)
- [TASK-026 (DSL подход)](../../.qwen/workplace/archive/TASK-026_common_structurizr_diagrams.md)
- [Structurizr Core API](https://github.com/structurizr/java/blob/master/docs/api.md)
- [PlantUML](https://plantuml.com/ru/)
- [PlantUML Maven](https://mvnrepository.com/artifact/net.sourceforge.plantuml/plantuml)
- [Глобальный план](../../docs/global-plans.md)

### Отличия от TASK-026

| Параметр | TASK-026 (DSL) | TASK-029 (Core API) |
|----------|----------------|---------------------|
| Формат | DSL файлы (.dsl) | Java код |
| Генерация | Docker (structurizr-site-generatr) | Java (StructurizrPlantUMLExporter + PlantUML) |
| Вывод | Статический сайт (HTML+PNG+SVG) | PlantUML (.puml) + PNG |
| Интеграция | Maven Exec Plugin | Maven тесты |
| Зависимости | Нет (Docker) | structurizr-core + plantuml |
