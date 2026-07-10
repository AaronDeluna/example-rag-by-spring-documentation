#!/usr/bin/env bash
# Скрипт: run-checkstyle-simple-custom.sh
# Загружает Checkstyle JAR из Maven Central во временную директорию и запускает проверку.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

CHECKSTYLE_VERSION="${1:-10.18.1}"
CHECKSTYLE_JAR_NAME="checkstyle-${CHECKSTYLE_VERSION}.jar"
MAVEN_URL="https://repo1.maven.org/maven2/com/puppycrawl/tools/checkstyle/${CHECKSTYLE_VERSION}/${CHECKSTYLE_JAR_NAME}"

# Создаём временную директорию
TEMP_DIR=$(mktemp -d)
CHECKSTYLE_JAR="${TEMP_DIR}/${CHECKSTYLE_JAR_NAME}"

# Функция очистки временной директории
cleanup() {
    rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

CONFIG="$SCRIPT_DIR/../embedded/my_simple_checks.xml"
SOURCE_PATH="${2:-./src/main/java}"

if [ ! -f "$CONFIG" ]; then
    echo "Ошибка: файл конфигурации $CONFIG не найден!"
    exit 1
fi

# Загрузка JAR
echo "Загрузка Checkstyle ${CHECKSTYLE_VERSION} из Maven Central..."
if command -v curl >/dev/null 2>&1; then
    curl -fSL -o "$CHECKSTYLE_JAR" "$MAVEN_URL"
elif command -v wget >/dev/null 2>&1; then
    wget -O "$CHECKSTYLE_JAR" "$MAVEN_URL"
else
    echo "Ошибка: необходима утилита curl или wget для загрузки JAR."
    exit 1
fi

if [ ! -f "$CHECKSTYLE_JAR" ]; then
    echo "Ошибка: не удалось загрузить Checkstyle JAR."
    exit 1
fi

echo "Запуск Checkstyle с простыми кастомными проверками..."
java -jar "$CHECKSTYLE_JAR" -c "$CONFIG" "$SOURCE_PATH"
