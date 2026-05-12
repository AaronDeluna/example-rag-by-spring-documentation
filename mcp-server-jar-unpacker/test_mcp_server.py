#!/usr/bin/env python3
"""Тестовый скрипт для проверки MCP-сервера JAR Unpacker"""

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

def test_list_tools():
    """Тест: получение списка инструментов"""
    request = {
        "jsonrpc": "2.0",
        "id": 1,
        "method": "tools/list",
        "params": {}
    }
    
    print("Тест: tools/list")
    result = run_mcp_request(request, timeout=15)
    print(f"Результат: {json.dumps(result, indent=2, ensure_ascii=False)}")
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
                "class_name": "com.google.common.base.Preconditions"
            }
        }
    }
    
    print("Тест: find_class_in_m2 (com.google.common.base.Preconditions)")
    result = run_mcp_request(request, timeout=30)
    print(f"Результат: {json.dumps(result, indent=2, ensure_ascii=False)}")
    print()
    return result

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
    
    print("Тест: initialize")
    result = run_mcp_request(request, timeout=15)
    print(f"Результат: {json.dumps(result, indent=2, ensure_ascii=False)}")
    print()
    return result

if __name__ == "__main__":
    print("=" * 60)
    print("MCP Server JAR Unpacker - Тестирование")
    print("=" * 60)
    print()
    
    test_initialize()
    test_list_tools()
    test_find_class()
    
    print("Тестирование завершено!")
