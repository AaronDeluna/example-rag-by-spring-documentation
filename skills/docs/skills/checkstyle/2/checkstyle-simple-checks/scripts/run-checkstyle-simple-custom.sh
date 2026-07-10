#!/usr/bin/env bash
# Скрипт: run-checkstyle-simple-custom.sh
# Загружает Checkstyle JAR из Maven Central во временную директорию и запускает проверку с простыми кастомными правилами.

CHECKSTYLE_VERSION="10.18.1"
CHECKSTYLE_JAR_NAME="checkstyle-${CHECKSTYLE_VERSION}-all.jar"
MAVEN_URL="https://repo1.maven.org/maven2/com/puppycrawl/tools/checkstyle/${CHECKSTYLE_VERSION}/${CHECKSTYLE_JAR_NAME}"

# Определяем директорию скрипта для доступа к embedded-файлам
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG="${SCRIPT_DIR}/../embedded/my_simple_checks.xml"

# Путь к исходникам: первый аргумент или значение по умолчанию
SOURCE_PATH="${1:-./src/main/java}"

if [ ! -f "$CONFIG" ]; then
    echo "Ошибка: файл конфигурации $CONFIG не найден!"
    exit 1
fi

if [ ! -d "$SOURCE_PATH" ]; then
    echo "Ошибка: директория с исходниками $SOURCE_PATH не существует!"
    exit 1
fi

# Создаём временную директорию
TEMP_DIR=$(mktemp -d)
CHECKSTYLE_JAR="${TEMP_DIR}/${CHECKSTYLE_JAR_NAME}"

# Функция очистки временной директории
cleanup() {
    rm -rf "$TEMP_DIR"
}
trap cleanup EXIT

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

echo "Запуск Checkstyle с простыми кастомными проверками для $SOURCE_PATH..."
java -jar "$CHECKSTYLE_JAR" -c "$CONFIG" "$SOURCE_PATH"
