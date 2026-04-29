# TASK-001: Реализация тестов для Server.java с Mockito

## Описание

Требуется реализовать тесты для всех методов класса `Server.java`:
- Подключить библиотеку Mockito для тестов
- Тесты приватных методов выполнить через рефлексию
- Создать тесты для публичных и приватных методов
- Покрыть тестами вложенные классы JsonUtils и Gson

## Критерии приёмки (Acceptance Criteria)

- [x] Подключена зависимость Mockito в pom.xml
- [x] Создан тестовый класс JsonUtilsTest.java
- [x] Создан тестовый класс GsonTest.java
- [x] Создан тестовый класс ServerPrivateMethodsTest.java (рефлексия)
- [x] Создан тестовый класс ServerTest.java (интеграционные тесты)
- [x] Все тесты проходят: `mvn test`
- [x] Сборка успешна: `mvn clean package`

## TDD Цикл

### 🔴 RED — Тесты

Написаны следующие тесты:

**JsonUtilsTest.java:**
- `givenValidJsonWithNumericIdWhenParseThenReturnsCorrectMessage()`
- `givenValidJsonWithStringIdWhenParseThenReturnsCorrectMessage()`
- `givenJsonWithParamsWhenParseThenReturnsParamsMap()`
- `givenJsonWithoutParamsWhenParseThenParamsIsNull()`
- `givenEmptyJsonWhenParseThenReturnsMessageWithNulls()`
- `givenSimpleJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject()`
- `givenNestedJsonObjectWhenExtractJsonObjectThenReturnsCorrectObject()`
- `givenJsonObjectWithEscapedQuotesWhenExtractJsonObjectThenReturnsCorrectObject()`
- `givenSimpleJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray()`
- `givenJsonArrayWithObjectsWhenExtractJsonArrayThenReturnsCorrectArray()`
- `givenNestedJsonArrayWhenExtractJsonArrayThenReturnsCorrectArray()`
- `givenEmptyJsonObjectWhenParseJsonObjectThenReturnsEmptyMap()`
- `givenSimpleJsonObjectWhenParseJsonObjectThenReturnsCorrectMap()`
- `givenJsonObjectWithBooleanValuesWhenParseJsonObjectThenReturnsCorrectBooleans()`
- `givenJsonObjectWithNullValueWhenParseJsonObjectThenReturnsNull()`
- `givenJsonObjectWithNestedObjectWhenParseJsonObjectThenReturnsNestedMap()`
- `givenJsonObjectWithArrayWhenParseJsonObjectThenReturnsArray()`
- `givenJsonObjectWithDoubleWhenParseJsonObjectThenReturnsDouble()`
- `givenJsonObjectWithNegativeNumberWhenParseJsonObjectThenReturnsNegativeNumber()`
- `givenEmptyJsonArrayWhenParseJsonArrayThenReturnsEmptyList()`
- `givenJsonArrayOfNumbersWhenParseJsonArrayThenReturnsNumberList()`
- `givenJsonArrayOfStringsWhenParseJsonArrayThenReturnsStringList()`
- `givenJsonArrayWithMixedTypesWhenParseJsonArrayThenReturnsMixedList()`
- `givenJsonArrayWithNestedObjectsWhenParseJsonArrayThenReturnsNestedMaps()`
- `givenJsonArrayWithNestedArrayWhenParseJsonArrayThenReturnsNestedArray()`
- `givenNullWhenToJsonThenReturnsNullString()`
- `givenEmptyMapWhenToJsonThenReturnsEmptyObject()`
- `givenMapWithStringsWhenToJsonThenReturnsCorrectJson()`
- `givenListWhenToJsonThenReturnsCorrectJsonArray()`
- `givenMapWithNumbersWhenToJsonThenReturnsCorrectJson()`
- `givenMapWithBooleanWhenToJsonThenReturnsCorrectJson()`

**GsonTest.java:**
- `givenNullWhenToJsonThenReturnsNullString()`
- `givenStringWhenToJsonThenReturnsQuotedString()`
- `givenStringWithSpecialCharsWhenToJsonThenReturnsEscapedString()`
- `givenNumberWhenToJsonThenReturnsNumberString()`
- `givenDoubleWhenToJsonThenReturnsDoubleString()`
- `givenBooleanWhenToJsonThenReturnsBooleanString()`
- `givenMapWhenToJsonThenReturnsJsonObject()`
- `givenListWhenToJsonThenReturnsJsonArray()`
- `givenEmptyMapWhenToJsonThenReturnsEmptyObject()`
- `givenEmptyListWhenToJsonThenReturnsEmptyArray()`
- `givenNestedMapWhenToJsonThenReturnsNestedJson()`
- `givenListOfMapsWhenToJsonThenReturnsCorrectJson()`
- `givenStringWithoutSpecialCharsWhenEscapeJsonThenReturnsSameString()`
- `givenStringWithQuotesWhenEscapeJsonThenEscapesQuotes()`
- `givenStringWithBackslashWhenEscapeJsonThenEscapesBackslash()`
- `givenStringWithNewlineWhenEscapeJsonThenEscapesNewline()`
- `givenStringWithCarriageReturnWhenEscapeJsonThenEscapesCR()`
- `givenStringWithTabWhenEscapeJsonThenEscapesTab()`
- `givenStringWithMultipleSpecialCharsWhenEscapeJsonThenEscapesAll()`
- `givenSimpleMapWhenToJsonMapThenReturnsCorrectJson()`
- `givenMapWithMixedTypesWhenToJsonMapThenReturnsCorrectJson()`
- `givenSimpleListWhenToJsonListThenReturnsCorrectJson()`
- `givenListWithMixedTypesWhenToJsonListThenReturnsCorrectJson()`

**ServerPrivateMethodsTest.java:**
- `givenNullJarWhenJarContainsClassThenReturnsFalse()`
- `givenNonExistentJarWhenJarContainsClassThenReturnsFalse()`
- `givenDescriptionWhenCreateStringPropThenReturnsMapWithTypeAndDescription()`
- `givenEmptyDescriptionWhenCreateStringPropThenReturnsMapWithEmptyDescription()`
- `givenValidResponseWhenSendResponseThenWritesToJson()`
- `givenStringIdWhenSendResponseThenWritesStringId()`
- `givenErrorMessageWhenSendErrorThenWritesErrorJson()`
- `givenNumericIdWhenSendErrorThenWritesNumericId()`
- `givenStringAndCharWhenCountCharThenReturnsCorrectCount()`
- `givenStringWithoutCharWhenCountCharThenReturnsZero()`
- `givenEmptyStringWhenCountCharThenReturnsZero()`
- `givenStringWithAllSameCharsWhenCountCharThenReturnsLength()`
- `givenStringWithBracesWhenCountCharThenReturnsCorrectCount()`
- `givenMavenRepoWhenGetJarsThenReturnsListOfJars()`
- `givenCachedJarsWhenGetJarsThenReturnsSameList()`
- `givenValidJsonWhenParseJsonThenReturnsParsedMessage()`

**ServerTest.java:**
- `givenInitializeRequestWhenHandleInitializeThenReturnsCapabilities()`
- `givenToolsListRequestWhenHandleListToolsThenReturnsFourTools()`
- `givenToolsListWhenHandleListToolsThenToolsHaveDescriptions()`
- `givenUnknownToolWhenHandleCallToolThenReturnsError()`
- `givenFindClassInM2WhenHandleCallToolThenExecutesTool()`
- `givenNonExistentClassWhenFindClassInM2ThenReturnsNotFoundMessage()`
- `givenSimpleClassNameWhenFindClassInM2ThenSearchesBySimpleName()`
- `givenFQNClassNameWhenFindClassInM2ThenSearchesBySimpleName()`
- `givenInvalidJarPathWhenGetClassOutlineThenReturnsError()`
- `givenInvalidJarPathWhenGetMethodSourceThenReturnsError()`
- `givenInvalidJarPathWhenDecompileClassThenReturnsError()`

### 🟢 GREEN — Реализация

Все тесты проходят. В процессе реализации были исправлены следующие ошибки в коде:

1. **Бесконечный цикл в `parseJsonArray()`** - добавлена проверка на закрывающую скобку `]`
2. **Бесконечный цикл в `parseJsonObject()`** - добавлена проверка на закрывающую скобку `}`
3. **Неправильный порядок замен в `escapeJson()`** - сначала заменяется `\`, затем `"`

### 🔵 REFACTOR — Рефакторинг

- [x] Код рефакторен, все тесты проходят
- [x] Сборка успешна

## Работа с существующим кодом

- [x] Исправлен `parseJsonArray()` - добавлена проверка на `]`
- [x] Исправлен `parseJsonObject()` - добавлена проверка на `}`
- [x] Исправлен `escapeJson()` - правильный порядок замен

## Чек-лист завершения

- [x] Все тесты зелёные (81 тест)
- [x] Сборка успешна
- [x] Код соответствует стандартам проекта
- [x] Изменения закоммичены

## Статус

| Поле | Значение |
|------|----------|
| Дата создания: | 2026-03-26 |
| Дата начала: | 2026-03-26 |
| Дата завершения: | 2026-03-26 |
| Статус: | ✅ |

## Заметки

- Подключены зависимости: mockito-core 5.3.1, mockito-junit-jupiter 5.3.1
- Обновлён maven-surefire-plugin до версии 3.0.0 для поддержки JUnit 5
- Тесты приватных методов выполнены через рефлексию с использованием `setAccessible(true)`
- Использованы аннотации JUnit 5: `@Nested`, `@DisplayName`, `@Test`
- Применён AAA Pattern (Arrange-Act-Assert) в структуре тестов
