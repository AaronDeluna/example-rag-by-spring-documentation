#!/usr/bin/env bash
# Скрипт: run-checkstyle-simple-custom.sh
# Использует конфиг с простыми регулярными проверками.

CHECKSTYLE_JAR="./lib/checkstyle-10.18.1-all.jar"
CONFIG="./config/my_simple_checks.xml"   # путь к вашему XML
SOURCE_PATH="./src/main/java"

if [ ! -f "$CHECKSTYLE_JAR" ]; then
    echo "Ошибка: JAR-файл Checkstyle не найден!"
    exit 1
fi

if [ ! -f "$CONFIG" ]; then
    echo "Ошибка: файл конфигурации $CONFIG не найден!"
    exit 1
fi

echo "Запуск Checkstyle с простыми кастомными проверками..."
java -jar "$CHECKSTYLE_JAR" -c "$CONFIG" "$SOURCE_PATH"