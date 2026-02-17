# Примеры работы с Spring AI

## Описание
Реализации **MCP-клиента** и **MCP-сервера**:
- взаимодействующие через транспорт **STDIO**:
  - `stdio-mcp-client`
  - `stdio-mcp-server`
- взаимодействующие через транспорт **WebMVC**:
    - `webmvc-mcp-client`
    - `webmvc-mcp-server` (с возможностью запуска через транспорт **STDIO**)

## Запуск клиента и сервера с транспортом STDIO
Для запуска можно использовать две реализации.

### Реализция: клиент-тест
В данной реализации тест в модуле `stdio-mcp-server` выполняет запуск скомпилированного `jar`-файла **MCP-сервера**.

Для этого:
1. Скомпилировать исполняемый файл **MCP-сервера** (с пропуском тестов):
> `mvn clean install -pl stdio-mcp-server -DskipTests`
2. Выполнить запуск теста, имитирующего **MCP-клиент**:
> `mvn test -pl stdio-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest`

### Реализция: клиент-стороннее приложение
В данной реализации клиент и сервер являются раздельными приложениями, расположенными в разных исполняемых файлах 
разных модулей.

Для запуска необходимо выполнить команду:

1. Скомпилировать исполняемый файл **MCP-сервера** (с пропуском тестов):
> `mvn clean install -pl stdio-mcp-server -DskipTests`
2. Выполнить запуск приложения **MCP-клиента**:
> `mvn spring-boot:run -pl stdio-mcp-client`

## Запуск клиента и сервера с транспортом WebMVC

1. Запуск MCP-сервера с конфигурацией по умолчанию - транспортом **WebMVC**:
> `mvn spring-boot:run -pl webmvc-mcp-server`
2. Выполнить запуск теста, имитирующего **MCP-клиент**:
> `mvn test -pl stdio-mcp-server -Dtest=ru.mirent.stdio.StdioClientTest`

(Опционально) Запуск с конфигурацией `stdio` с транспортом **STDIO**:
> `mvn spring-boot:run -pl webmvc-mcp-server -Dspring-boot.run.profiles=stdio`

TODO Добавить реализацию MCP-клиента и его запуск как для транспорта по умолчанию, так и для **STDIO**

## Дополнительные команды

> Отобразить дерево зависимостей проекта:

`mvn -Dverbose dependency:tree`

> Отобразить зависимости плагинов:

`mvn dependency:resolve-plugins`