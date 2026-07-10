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

# Функция для проверки успешности загрузки (размер > 0)
check_download() {
    if [ ! -f "$FILENAME" ]; then
        echo "Ошибка: файл не был скачан."
        exit 1
    fi
    if [ ! -s "$FILENAME" ]; then
        echo "Ошибка: скачанный файл пуст."
        exit 1
    fi
    echo "Файл успешно скачан (размер: $(stat -c %s "$FILENAME" 2>/dev/null || stat -f %z "$FILENAME" 2>/dev/null) байт)."
}

# Выбираем подходящий инструмент для загрузки с расширенными опциями
if command -v wget >/dev/null 2>&1; then
    echo "Используется wget..."
    wget --max-redirect=30 \
         --user-agent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" \
         --timeout=300 \
         --tries=3 \
         -O "$FILENAME" "$URL"
    check_download
elif command -v curl >/dev/null 2>&1; then
    echo "Используется curl..."
    curl -L --max-redirs 30 \
         --user-agent "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" \
         --connect-timeout 300 \
         --retry 3 \
         -o "$FILENAME" "$URL"
    check_download
else
    echo "Ошибка: не найден wget или curl. Установите один из них."
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