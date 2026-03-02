# Примеры работы с Spring AI

## Описание
Реализации **MCP-клиента** и **MCP-сервера**:
- синхронное взаимодействующие через транспорт **STDIO**:
  - `stdio-sync-mcp-client`
  - `stdio-sync-mcp-server`
- взаимодействующие через транспорт **SSE**:
  - синхронное:
    - `webmvc-sync-mcp-client`
    - `webmvc-sync-mcp-server` (с возможностью запуска через транспорт **STDIO**)
  - асинхронное:
      - `webflux-async-mcp-client`
      - `webflux-async-mcp-server` (с возможностью запуска через транспорт **STDIO**)

## Запуск клиента и сервера с транспортом STDIO
Для запуска можно использовать две реализации.

### Реализция: клиент-тест
В данной реализации тест в модуле `stdio-sync-mcp-server` выполняет запуск скомпилированного `jar`-файла **MCP-сервера**.

Для этого:
1. Скомпилировать исполняемый файл **MCP-сервера** (с пропуском тестов):
> `mvn clean install -pl stdio-sync-mcp-server -DskipTests`
2. Выполнить запуск теста, имитирующего **MCP-клиент**:
> `mvn test -pl stdio-sync-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest`

### Реализция: клиент-стороннее приложение
В данной реализации клиент и сервер являются раздельными приложениями, расположенными в разных исполняемых файлах 
разных модулей.

Для запуска необходимо выполнить команду:

1. Скомпилировать исполняемый файл **MCP-сервера** (с пропуском тестов):
> `mvn clean install -pl stdio-sync-mcp-server -DskipTests`
2. Выполнить запуск приложения **MCP-клиента**:
> `mvn spring-boot:run -pl stdio-sync-mcp-client`

## Запуск клиента и сервера с транспортом синхронный SSE

Выполнить запуск теста, имитирующего **MCP-клиент**:
> `mvn test -pl webmvc-sync-mcp-server -Dtest=ru.mirent.webmvc.WebMvcClientTest`

(Опционально) Запуск с конфигурацией `stdio` с транспортом **STDIO**:
> `mvn spring-boot:run -pl webmvc-sync-mcp-server -Dspring-boot.run.profiles=stdio`

## Запуск клиента и сервера с транспортом асинхронный SSE

Выполнить запуск теста, имитирующего **MCP-клиент**:
> `mvn test -pl webflux-async-mcp-server -Dtest=ru.mirent.webflux.WebFluxClientTest`

(Опционально) Запуск с конфигурацией `stdio` с транспортом **STDIO**:
> `mvn spring-boot:run -pl webflux-async-mcp-server -Dspring-boot.run.profiles=stdio`

TODO Добавить реализацию MCP-клиента и его запуск как для транспорта по умолчанию, так и для **STDIO**

## Дополнительные команды

> Отобразить дерево зависимостей проекта:

`mvn -Dverbose dependency:tree`

> Отобразить зависимости плагинов:

`mvn dependency:resolve-plugins`

## Сравнительная таблицы для разных реализаций Spring WebFlux и Spring WebMVC
| Критерий       | WebMVC                               | WebFlux                                       |
|----------------|--------------------------------------|-----------------------------------------------|
| Контейнер      | Работает на любом Servlet-контейнере | Реактивный сервер                             |
| Основана на    | Servlet API и блокирующих потоках    | Reactive Streams и неблокирующей модели       |
| Тип выполнения | Синхронная (блокирующая)             | Асинхронная (неблокирующая)                   |
| Подход         | Один поток — один запрос             | Немного потоков — много запросов (event-loop) |
