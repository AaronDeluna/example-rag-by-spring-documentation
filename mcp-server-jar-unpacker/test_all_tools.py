#!/usr/bin/env python3
"""Тестовый скрипт для проверки всех инструментов MCP-сервера JAR Unpacker"""

import subprocess
import json
import sys
import time
import select

SERVER_JAR = "target/mcp-server-jar-unpacker-1.0-SNAPSHOT.jar"

class MCPClient:
    """Клиент для тестирования MCP-сервера"""
    
    def __init__(self, jar_path=SERVER_JAR):
        self.jar_path = jar_path
        self.process = None
        self.request_id = 0
    
    def start(self):
        """Запустить MCP-сервер"""
        self.process = subprocess.Popen(
            ["java", "-jar", self.jar_path],
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            bufsize=1
        )
        print(f"✓ MCP-сервер запущен: {self.jar_path}")
    
    def stop(self):
        """Остановить MCP-сервер"""
        if self.process:
            self.process.terminate()
            try:
                self.process.wait(timeout=2)
            except subprocess.TimeoutExpired:
                self.process.kill()
            print("✓ MCP-сервер остановлен")
    
    def send_request(self, method, params=None, timeout=30):
        """Отправить JSON-RPC запрос и получить ответ"""
        if not params:
            params = {}
        
        self.request_id += 1
        request = {
            "jsonrpc": "2.0",
            "id": self.request_id,
            "method": method,
            "params": params
        }
        
        # Отправляем запрос
        self.process.stdin.write(json.dumps(request) + "\n")
        self.process.stdin.flush()
        
        # Читаем ответ с таймаутом
        start_time = time.time()
        output_lines = []
        
        while time.time() - start_time < timeout:
            ready = select.select([self.process.stdout], [], [], 0.5)
            if ready[0]:
                line = self.process.stdout.readline()
                if line:
                    output_lines.append(line.strip())
                    try:
                        result = json.loads(line)
                        if "result" in result or "error" in result:
                            break
                    except:
                        pass
                else:
                    break
        
        if output_lines:
            try:
                return json.loads(output_lines[-1])
            except:
                return {"raw_output": output_lines}
        return None
    
    def initialize(self):
        """Инициализация MCP-протокола"""
        print("\n" + "=" * 60)
        print("1. Инициализация MCP-протокола")
        print("=" * 60)
        
        result = self.send_request("initialize", {
            "protocolVersion": "2024-11-05",
            "capabilities": {},
            "clientInfo": {
                "name": "test-client",
                "version": "1.0.0"
            }
        })
        
        if result and "result" in result:
            print(f"✓ Инициализация успешна")
            print(f"  Protocol: {result['result'].get('protocolVersion', 'N/A')}")
            print(f"  Server: {result['result'].get('serverInfo', {}).get('name', 'N/A')}")
        else:
            print(f"✗ Ошибка инициализации: {result}")
        
        return result
    
    def list_tools(self):
        """Получить список доступных инструментов"""
        print("\n" + "=" * 60)
        print("2. Получение списка инструментов")
        print("=" * 60)
        
        result = self.send_request("tools/list", {})
        
        if result and "result" in result:
            tools = result["result"].get("tools", [])
            print(f"✓ Найдено инструментов: {len(tools)}")
            for tool in tools:
                print(f"  - {tool['name']}: {tool['description']}")
        else:
            print(f"✗ Ошибка получения списка: {result}")
        
        return result
    
    def find_class(self, class_name):
        """Инструмент 1: find_class_in_m2"""
        print("\n" + "=" * 60)
        print(f"3. Инструмент: find_class_in_m2")
        print("=" * 60)
        print(f"  Поиск класса: {class_name}")
        
        result = self.send_request("tools/call", {
            "name": "find_class_in_m2",
            "arguments": {"class_name": class_name}
        })
        
        if result and "result" in result:
            result_data = result["result"]
            if isinstance(result_data, str):
                text = result_data
            elif isinstance(result_data, dict):
                content = result_data.get("content", [])
                if content:
                    text = content[0].get("text", "") if isinstance(content[0], dict) else str(content[0])
                else:
                    text = str(result_data)
            else:
                text = str(result_data)
            
            print(f"✓ Результат поиска:")
            print(f"  {text[:500]}{'...' if len(text) > 500 else ''}")
        else:
            print(f"✗ Ошибка: {result}")
        
        return result
    
    def get_class_outline(self, jar_path, class_fqn):
        """Инструмент 2: get_class_outline"""
        print("\n" + "=" * 60)
        print(f"4. Инструмент: get_class_outline")
        print("=" * 60)
        print(f"  JAR: {jar_path}")
        print(f"  Класс: {class_fqn}")
        
        result = self.send_request("tools/call", {
            "name": "get_class_outline",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn
            }
        })
        
        if result and "result" in result:
            result_data = result["result"]
            if isinstance(result_data, str):
                text = result_data
            elif isinstance(result_data, dict):
                content = result_data.get("content", [])
                if content:
                    text = content[0].get("text", "") if isinstance(content[0], dict) else str(content[0])
                else:
                    text = str(result_data)
            else:
                text = str(result_data)
            
            print(f"✓ Схема класса:")
            print(f"  {text[:800]}{'...' if len(text) > 800 else ''}")
        else:
            print(f"✗ Ошибка: {result}")
        
        return result
    
    def get_method_source(self, jar_path, class_fqn, method_name):
        """Инструмент 3: get_method_source"""
        print("\n" + "=" * 60)
        print(f"5. Инструмент: get_method_source")
        print("=" * 60)
        print(f"  JAR: {jar_path}")
        print(f"  Класс: {class_fqn}")
        print(f"  Метод: {method_name}")
        
        result = self.send_request("tools/call", {
            "name": "get_method_source",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn,
                "method_name": method_name
            }
        })
        
        if result and "result" in result:
            result_data = result["result"]
            if isinstance(result_data, str):
                text = result_data
            elif isinstance(result_data, dict):
                content = result_data.get("content", [])
                if content:
                    text = content[0].get("text", "") if isinstance(content[0], dict) else str(content[0])
                else:
                    text = str(result_data)
            else:
                text = str(result_data)
            
            print(f"✓ Исходный код метода:")
            print(f"  {text[:800]}{'...' if len(text) > 800 else ''}")
        else:
            print(f"✗ Ошибка: {result}")
        
        return result
    
    def decompile_class(self, jar_path, class_fqn):
        """Инструмент 4: decompile_class"""
        print("\n" + "=" * 60)
        print(f"6. Инструмент: decompile_class")
        print("=" * 60)
        print(f"  JAR: {jar_path}")
        print(f"  Класс: {class_fqn}")
        
        result = self.send_request("tools/call", {
            "name": "decompile_class",
            "arguments": {
                "jar_path": jar_path,
                "class_fqn": class_fqn
            }
        }, timeout=60)
        
        if result and "result" in result:
            result_data = result["result"]
            if isinstance(result_data, str):
                text = result_data
            elif isinstance(result_data, dict):
                content = result_data.get("content", [])
                if content:
                    text = content[0].get("text", "") if isinstance(content[0], dict) else str(content[0])
                else:
                    text = str(result_data)
            else:
                text = str(result_data)
            
            print(f"✓ Декомпилированный класс:")
            print(f"  {text[:800]}{'...' if len(text) > 800 else ''}")
        else:
            print(f"✗ Ошибка: {result}")
        
        return result


def main():
    print("=" * 60)
    print("MCP Server JAR Unpacker - Полное тестирование инструментов")
    print("=" * 60)
    
    client = MCPClient()
    
    try:
        # Запуск сервера
        client.start()
        time.sleep(1)  # Дать серверу время запуститься
        
        # 1. Инициализация
        client.initialize()
        time.sleep(0.5)
        
        # 2. Список инструментов
        tools_result = client.list_tools()
        time.sleep(0.5)
        
        # Тестовые данные (используем существующую версию Guava)
        test_class = "com.google.common.base.Preconditions"
        test_jar = "/home/vadim/.m2/repository/com/google/guava/guava/33.4.0-jre/guava-33.4.0-jre.jar"
        test_method = "checkNotNull"
        
        # 3. Поиск класса
        find_result = client.find_class(test_class)
        time.sleep(0.5)
        
        # 4. Схема класса
        client.get_class_outline(test_jar, test_class)
        time.sleep(0.5)
        
        # 5. Исходный код метода
        client.get_method_source(test_jar, test_class, test_method)
        time.sleep(0.5)
        
        # 6. Декомпиляция класса
        client.decompile_class(test_jar, test_class)
        
        print("\n" + "=" * 60)
        print("✓ Все инструменты успешно протестированы!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n✗ Ошибка тестирования: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
    
    finally:
        client.stop()


if __name__ == "__main__":
    main()
