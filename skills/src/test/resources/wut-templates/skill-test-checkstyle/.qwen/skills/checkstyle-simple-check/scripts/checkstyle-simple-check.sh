#!/usr/bin/env bash
set -euo pipefail

CHECKSTYLE_VERSION="${1:-10.18.1}"
CHECKSTYLE_JAR_NAME="checkstyle-${CHECKSTYLE_VERSION}-all.jar"
MAVEN_URL="https://github.com/checkstyle/checkstyle/releases/download/checkstyle-${CHECKSTYLE_VERSION}/${CHECKSTYLE_JAR_NAME}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CACHE_DIR="$SCRIPT_DIR/../.checkstyle-cache"
EMBEDDED_DIR="$SCRIPT_DIR/../embedded"
CONFIG_FILE="$EMBEDDED_DIR/my_simple_checks.xml"

TARGET_DIR="${2:-.}"

if [ ! -d "$CACHE_DIR" ]; then
  mkdir -p "$CACHE_DIR"
fi

JAR_PATH="$CACHE_DIR/$CHECKSTYLE_JAR_NAME"

if [ ! -f "$JAR_PATH" ]; then
  echo "Downloading Checkstyle $CHECKSTYLE_VERSION..."
  if command -v curl &> /dev/null; then
    curl -L -o "$JAR_PATH" "$MAVEN_URL"
  elif command -v wget &> /dev/null; then
    wget -O "$JAR_PATH" "$MAVEN_URL"
  else
    echo "Error: curl or wget is required to download the jar." >&2
    exit 1
  fi
fi

if [ ! -f "$CONFIG_FILE" ]; then
  echo "Error: Configuration file $CONFIG_FILE not found." >&2
  exit 1
fi

echo "Running Checkstyle on $TARGET_DIR ..."
java -jar "$JAR_PATH" -c "$CONFIG_FILE" "$TARGET_DIR"
