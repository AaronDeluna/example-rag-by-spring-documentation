#!/bin/bash
# Скрипт для загрузки и запуска frap-mcp-http-local.jar в подпапке frap-temp

set -e  # Прерывать выполнение при любой ошибке

URL="https://github.com/kotler-dev/frap/releases/download/java-v1.1.1/frap-mcp-http-local.jar"
FILENAME="frap-mcp-http-local.jar"
TEMP_DIR="frap-temp"

echo "Создание папки $TEMP_DIR ..."
mkdir -p "$TEMP_DIR"

echo "Переход в $TEMP_DIR ..."
cd "$TEMP_DIR"

echo "Скачивание $FILENAME из $URL ..."

# Выбираем подходящий инструмент для загрузки
if command -v wget >/dev/null 2>&1; then
    wget -O "$FILENAME" "$URL"
elif command -v curl >/dev/null 2>&1; then
    curl -L -o "$FILENAME" "$URL"
else
    echo "Ошибка: не найден wget или curl. Установите один из них."
    exit 1
fi

# Проверяем, что файл действительно скачан
if [ ! -f "$FILENAME" ]; then
    echo "Ошибка: файл не был скачан."
    exit 1
fi

# Проверяем, что файл не пустой
if [ ! -s "$FILENAME" ]; then
    echo "Ошибка: скачанный файл пуст."
    exit 1
fi

# Проверяем наличие Java
if ! command -v java >/dev/null 2>&1; then
    echo "Ошибка: Java не установлена. Установите JRE (например, openjdk-17-jre)."
    exit 1
fi

echo "Запуск $FILENAME из папки $TEMP_DIR ..."
java -jar "$FILENAME"

# Если java завершится с ошибкой, set -e прервёт скрипт