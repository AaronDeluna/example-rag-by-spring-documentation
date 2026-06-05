#!/usr/bin/env python3
"""Тестовый скрипт для проверки всех инструментов MCP-сервера JAR Unpacker"""

import subprocess
import json
import sys
import time
import select

SERVER_JAR = "target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar"

def run_mcp_request(request, timeout=10):
    """Отправить JSON-RPC запрос серверу"""
    process = subprocess.Popen(
        ["java", "-jar", SERVER_JAR],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        bufsize=1
    )

    # Отправляем запрос
    process.stdin.write(json.dumps(request) + "\n")
    process.stdin.flush()

    # Читаем ответ с таймаутом
    start_time = time.time()
    output_lines = []

    while time.time() - start_time < timeout:
        ready = select.select([process.stdout], [], [], 0.5)
        if ready[0]:
            line = process.stdout.readline()
            if line:
                output_lines.append(line.strip())
                # Если получили ответ, прекращаем
                try:
                    result = json.loads(line)
                    if "result" in result or "error" in result:
                        break
                except:
                    pass
            else:
                break

    process.terminate()
    process.wait(timeout=2)

    if output_lines:
        try:
            return json.loads(output_lines[-1])
        except:
            return {"raw_output": output_lines}
    return None

def test_initialize():
    """Тест: инициализация MCP"""
    request = {
        "jsonrpc": "2.0",
        "id": 0,
        "method": "initialize",
        "params": {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {
                "name": "test-client",
                "version": "1.0.0"
            }
        }
    }

    print("1. Тест: initialize")
    result = run_mcp_request(request, timeout=15)
    print(f"   Результат: {json.dumps(result, indent=2, ensure_ascii=False)[:200]}...")
    print()
    return result

def test_list_tools():
    """Тест: получение списка инструментов"""
    request = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "tools/list",
        "params": {}
    }

    print("2. Тест: tools/list")
    result = run_mcp_request(request, timeout=15)
    if result and "result" in result:
        tools = result["result"].get("tools", [])
        print(f"   Найдено инструментов: {len(tools)}")
        for tool in tools:
            print(f"   - {tool['name']}: {tool['description'][:60]}...")
    print()
    return result

def test_find_class():
    """Тест: поиск класса"""
    request = {
        "jsonrpc": "2.0",
        "id": 2,
        "method": "tools/call",
        "params": {
            "name": "find_class_in_m2",
            "arguments": {
                "class_name": "org.springframework.web.bind.annotation.RestController"
            }
        }
    }

    print("3. Тест: find_class_in_m2 (RestController)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str) and "Найдено JAR-файлов" in res:
            lines = res.split('\n')
            print(f"   {lines[0]}")
            print(f"   Первые 5 результатов:")
            for line in lines[1:6]:
                if line.strip():
                    print(f"   {line.strip()}")
    print()
    return result

def test_get_class_outline():
    """Тест: получение схемы класса"""
    jar_path = "/home/vadim/.m2/repository/org/springframework/spring-web/6.2.15/spring-web-6.2.15.jar"
    class_fqn = "org.springframework.web.bind.annotation.RestController"
    
    request = {
        "jsonrpc": "2.0",
        "id": 3,
        "method": "tools/call",
        "params": {
            "name": "get_class_outline",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn
            }
        }
    }

    print("4. Тест: get_class_outline (RestController)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str):
            lines = res.split('\n')
            print(f"   Первые 15 строк схемы:")
            for line in lines[:15]:
                print(f"   {line}")
    print()
    return result

def test_decompile_class():
    """Тест: полная декомпиляция класса"""
    jar_path = "/home/vadim/.m2/repository/org/springframework/spring-web/6.2.15/spring-web-6.2.15.jar"
    class_fqn = "org.springframework.web.bind.annotation.RestController"
    
    request = {
        "jsonrpc": "2.0",
        "id": 4,
        "method": "tools/call",
        "params": {
            "name": "decompile_class",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn
            }
        }
    }

    print("5. Тест: decompile_class (RestController)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str):
            lines = res.split('\n')
            print(f"   Первые 20 строк декомпиляции:")
            for line in lines[:20]:
                print(f"   {line}")
    print()
    return result

def test_get_method_source():
    """Тест: получение исходника метода"""
    jar_path = "/home/vadim/.m2/repository/org/springframework/spring-web/6.2.15/spring-web-6.2.15.jar"
    class_fqn = "org.springframework.web.bind.annotation.RestController"
    method_name = "annotationType"
    
    request = {
        "jsonrpc": "2.0",
        "id": 5,
        "method": "tools/call",
        "params": {
            "name": "get_method_source",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn,
                "method_name": method_name
            }
        }
    }

    print("6. Тест: get_method_source (annotationType)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str):
            lines = res.split('\n')
            print(f"   Первые 20 строк метода:")
            for line in lines[:20]:
                print(f"   {line}")
    print()
    return result

def test_list_classes_in_jar():
    """Тест: список классов в JAR"""
    jar_path = "/home/vadim/.m2/repository/org/springframework/spring-web/6.2.15/spring-web-6.2.15.jar"
    
    request = {
        "jsonrpc": "2.0",
        "id": 6,
        "method": "tools/call",
        "params": {
            "name": "list_classes_in_jar",
            "arguments": {
                "jar_path": jar_path,
                "filter": ".*Controller.*"
            }
        }
    }

    print("7. Тест: list_classes_in_jar (.*Controller.*)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str):
            lines = res.split('\n')
            print(f"   {lines[0]}")
            for line in lines[1:11]:
                if line.strip():
                    print(f"   {line}")
    print()
    return result

def test_search_classes_by_pattern():
    """Тест: поиск классов по паттерну"""
    request = {
        "jsonrpc": "2.0",
        "id": 7,
        "method": "tools/call",
        "params": {
            "name": "search_classes_by_pattern",
            "arguments": {
                "pattern": ".*Guava.*",
                "limit": "10"
            }
        }
    }

    print("8. Тест: search_classes_by_pattern (.*Guava.*)")
    result = run_mcp_request(request, timeout=30)
    if result and "result" in result:
        res = result["result"]
        if isinstance(res, str):
            lines = res.split('\n')
            print(f"   Первые 10 строк:")
            for line in lines[:10]:
                print(f"   {line}")
    print()
    return result

if __name__ == "__main__":
    print("=" * 80)
    print("MCP Server JAR Unpacker - Полное тестирование всех инструментов")
    print("=" * 80)
    print()

    test_initialize()
    test_list_tools()
    test_find_class()
    test_get_class_outline()
    test_decompile_class()
    test_get_method_source()
    test_list_classes_in_jar()
    test_search_classes_by_pattern()

    print("=" * 80)
    print("Тестирование завершено!")
    print("=" * 80)
