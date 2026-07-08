#!/usr/bin/env bash
# compress-context.sh
# Сжимает контекст qwen-сессии через встроенную команду /compress
# (AI-суммаризация истории). По умолчанию сжимает ПОСЛЕДНЮЮ сессию
# текущего проекта (--continue). Команда /compress поддерживает
# non_interactive-режим, поэтому её можно прогнать через -p.
#
# Использование:
#   compress-context.sh                     # сжать последнюю сессию текущего проекта
#   compress-context.sh <session-id>        # сжать конкретную сессию по ID
#   compress-context.sh -i "<инструкции>"   # доп. инструкции для суммаризации
#   compress-context.sh -h                  # помощь
#
# Переменные окружения:
#   QWEN_BIN   имя/путь исполняемого файла qwen (по умолчанию: qwen)

set -euo pipefail

QWEN_BIN="${QWEN_BIN:-qwen}"
SESSION_ID=""
INSTRUCTIONS=""

usage() {
    cat <<'EOF'
Сжатие контекста qwen-сессии через /compress (AI-суммаризация).

Использование:
  compress-context.sh                     сжать последнюю сессию текущего проекта
  compress-context.sh <session-id>        сжать конкретную сессию по ID
  compress-context.sh -i "<инструкции>"   доп. инструкции для суммаризации
  compress-context.sh -h                  показать эту справку

Список сессий: qwen sessions list
EOF
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        -i|--instructions)
            INSTRUCTIONS="${2:-}"
            shift 2
            ;;
        -h|--help)
            usage
            exit 0
            ;;
        -*)
            echo "Ошибка: неизвестный флаг '$1'" >&2
            usage
            exit 1
            ;;
        *)
            SESSION_ID="$1"
            shift
            ;;
    esac
done

if ! command -v "$QWEN_BIN" >/dev/null 2>&1; then
    echo "Ошибка: qwen CLI не найден (QWEN_BIN=$QWEN_BIN)." >&2
    exit 1
fi

# Команда /compress с опциональными инструкциями по суммаризации.
COMPRESS_CMD="/compress"
if [[ -n "$INSTRUCTIONS" ]]; then
    COMPRESS_CMD="/compress ${INSTRUCTIONS}"
fi

# Выбор сессии: конкретная по ID (--resume) или последняя в проекте (--continue).
if [[ -n "$SESSION_ID" ]]; then
    RESUME_ARGS=(--resume "$SESSION_ID")
    echo "Сжимаю контекст сессии: $SESSION_ID"
else
    RESUME_ARGS=(--continue)
    echo "Сжимаю контекст последней сессии текущего проекта"
fi

# --chat-recording гарантирует, что сжатая история сохранится на диск,
# иначе --continue/--resume не смогут её подхватить в следующий раз.
"$QWEN_BIN" "${RESUME_ARGS[@]}" --chat-recording --prompt "$COMPRESS_CMD"
