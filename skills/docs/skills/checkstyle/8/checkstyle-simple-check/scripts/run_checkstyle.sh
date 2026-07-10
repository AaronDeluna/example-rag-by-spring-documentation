#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHECKSTYLE_VERSION="${1:-10.18.1}"
CHECKSTYLE_JAR_NAME="checkstyle-${CHECKSTYLE_VERSION}-all.jar"
CONFIG_FILE="${2:-my_simple_checks.xml}"
SOURCE_DIR="${3:-src}"
MAVEN_URL="https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/${CHECKSTYLE_JAR_NAME}"

JAR_PATH="$SCRIPT_DIR/../assets/$CHECKSTYLE_JAR_NAME"
CONFIG_PATH="$SCRIPT_DIR/../embedded/$CONFIG_FILE"

if [ ! -f "$JAR_PATH" ]; then
  echo "Скачивание Checkstyle $CHECKSTYLE_VERSION..."
  mkdir -p "$SCRIPT_DIR/../assets"
  curl -L -o "$JAR_PATH" "$MAVEN_URL"
  if [ $? -ne 0 ]; then
    echo "Ошибка: не удалось скачать Checkstyle JAR."
    exit 1
  fi
fi

if [ ! -f "$CONFIG_PATH" ]; then
  echo "Ошибка: файл конфигурации $CONFIG_FILE не найден в $CONFIG_PATH."
  exit 1
fi

if [ ! -d "$SOURCE_DIR" ]; then
  echo "Ошибка: директория исходного кода '$SOURCE_DIR' не найдена."
  echo "Использование: $0 [checkstyle_version] [config_file] [source_directory]"
  exit 1
fi

echo "Запуск Checkstyle $CHECKSTYLE_VERSION с конфигом $CONFIG_FILE на $SOURCE_DIR..."
java -jar "$JAR_PATH" -c "$CONFIG_PATH" "$SOURCE_DIR"