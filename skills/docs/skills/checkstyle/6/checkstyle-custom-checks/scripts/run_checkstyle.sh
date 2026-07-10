#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG="$SCRIPT_DIR/../embedded/my_simple_checks.xml"
CHECKSTYLE_CACHE="$HOME/.cache/checkstyle"
JAR="$CHECKSTYLE_CACHE/checkstyle-10.18.0-all.jar"
URL="https://github.com/checkstyle/checkstyle/releases/download/checkstyle-10.18.0/checkstyle-10.18.0-all.jar"

if [ $# -lt 1 ]; then
    echo "Использование: $0 <путь к исходникам>"
    exit 1
fi

TARGET="$1"

if ! command -v java &> /dev/null; then
    echo "Ошибка: Java не установлена."
    exit 1
fi

if [ ! -f "$JAR" ]; then
    echo "JAR не найден. Скачиваю Checkstyle..."
    mkdir -p "$CHECKSTYLE_CACHE"
    if command -v curl &> /dev/null; then
        curl -L -o "$JAR" "$URL"
    elif command -v wget &> /dev/null; then
        wget -O "$JAR" "$URL"
    else
        echo "Ошибка: требуется curl или wget для загрузки JAR."
        exit 1
    fi
fi

java -jar "$JAR" -c "$CONFIG" "$TARGET"
