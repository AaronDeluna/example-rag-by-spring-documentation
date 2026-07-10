#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import requests
import time
import os
import sys
import re

# ------------------- НАСТРОЙКИ -------------------
# Получаем ключ из переменной окружения или вставляем прямо
DEEPSEEK_API_KEY = os.environ.get("DEEPSEEK_API_KEY", "ваш_ключ_здесь")
DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions"
MODEL_NAME = "deepseek-chat"
PROBLEMS_FILE = "problems.json"
OUTPUT_DIR = "examples"
DELAY_BETWEEN_REQUESTS = 1
MAX_RETRIES = 3
# -------------------------------------------------

def clean_api_key(key):
    """Удаляет пробелы, кавычки, переносы и проверяет, что ключ ASCII."""
    if not key or key == "ваш_ключ_здесь":
        print("❌ Ошибка: API-ключ не задан.")
        sys.exit(1)

    # Убираем лишние пробелы, кавычки, переносы строк
    cleaned = re.sub(r'[\s"\'`]', '', key)
    if cleaned != key:
        print("⚠️  Ключ был очищен от пробелов и кавычек.")
        key = cleaned

    # Проверяем, что ключ состоит только из ASCII-символов
    try:
        key.encode('ascii')
    except UnicodeEncodeError:
        print("❌ Ошибка: API-ключ содержит недопустимые символы (не ASCII).")
        print("   Убедитесь, что ключ скопирован правильно, без лишних пробелов и кавычек.")
        print("   Используйте переменную окружения DEEPSEEK_API_KEY.")
        sys.exit(1)

    return key

def build_prompt(problem):
    tools_str = ", ".join(problem.get("tools", []))
    return f"""
Мне нужно создать пример корректного и некорректного кода на Java для следующей проблемы, связанной с качеством кода или тестов.

**Название проблемы:** {problem["name"]}
**Краткое описание проблемы:** {problem["description"]}
**Категория (по вашему списку):** {problem["group"]}
**Инструменты, которые могут выявить проблему:** {tools_str}

**Требования к ответу:**
1. Приведи **корректный** фрагмент Java-кода (или теста), который соответствует лучшим практикам и не содержит данной проблемы.
2. Приведи **некорректный** фрагмент Java-кода, который демонстрирует описываемую проблему (со всеми её признаками).
3. Для некорректного примера **объясни**, почему он плох, и **укажи**, какой именно инструмент из перечисленных сможет его обнаружить, а также как именно (какое правило или проверка сработает).
4. Код должен быть самодостаточным (можно использовать упрощённые классы, но с сохранением сути проблемы).
5. Формат ответа: разделы «Корректный пример», «Некорректный пример», «Объяснение и выявление инструментом».
6. Форматирование в виде Markdown.
"""

def call_deepseek(prompt, api_key, retries=MAX_RETRIES):
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {api_key}"
    }

    payload = {
        "model": MODEL_NAME,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.7,
        "max_tokens": 4096,
        "stream": False
    }

    for attempt in range(1, retries + 1):
        try:
            response = requests.post(DEEPSEEK_URL, json=payload, headers=headers, timeout=120)
            response.raise_for_status()
            data = response.json()
            return data["choices"][0]["message"]["content"].strip()
        except requests.exceptions.RequestException as e:
            print(f"  Попытка {attempt}/{retries} не удалась: {e}")
            if attempt < retries:
                time.sleep(5)
            else:
                print(f"  Не удалось получить ответ после {retries} попыток.")
                return None

def safe_filename(text):
    return "".join(c for c in text if c.isalnum() or c in " _-").rstrip()

def process_problems():
    # Проверяем файл с проблемами
    if not os.path.isfile(PROBLEMS_FILE):
        print(f"❌ Ошибка: файл {PROBLEMS_FILE} не найден.")
        sys.exit(1)

    # Очищаем и проверяем ключ
    api_key = clean_api_key(DEEPSEEK_API_KEY)

    with open(PROBLEMS_FILE, "r", encoding="utf-8") as f:
        problems = json.load(f)

    os.makedirs(OUTPUT_DIR, exist_ok=True)

    total = len(problems)
    for idx, problem in enumerate(problems, 1):
        print(f"[{idx}/{total}] Обработка: {problem['name']}")
        prompt = build_prompt(problem)
        answer = call_deepseek(prompt, api_key)

        if answer is None:
            print(f"  Пропускаем проблему {problem['name']}.")
            continue

        safe_name = safe_filename(problem["name"])
        filename = f"{idx:02d}_{safe_name}.md"
        filepath = os.path.join(OUTPUT_DIR, filename)

        with open(filepath, "w", encoding="utf-8") as f:
            f.write(f"# {problem['name']}\n\n")
            f.write(f"**Категория:** {problem['group']}\n\n")
            f.write(f"**Описание:** {problem['description']}\n\n")
            f.write(f"**Инструменты:** {', '.join(problem.get('tools', []))}\n\n")
            f.write("---\n\n")
            f.write(answer)

        print(f"  Сохранено в {filepath}")
        time.sleep(DELAY_BETWEEN_REQUESTS)

    print("\n✅ Генерация завершена. Все примеры в папке", OUTPUT_DIR)

if __name__ == "__main__":
    process_problems()