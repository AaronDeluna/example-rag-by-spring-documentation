# Structurizr — диаграммы архитектуры проекта

## 📋 Обзор

Этот документ описывает процесс создания и генерации диаграмм архитектуры C4 для проекта example-rag-by-spring-documentation с использованием Structurizr DSL.

## 🎯 Концептуальные цели

- **Для фреймворка:** Добавление примеров тестирования AI-агентов и MCP-серверов
- **Для MCP-сервера:** Разработка автотестов, улучшение продукта, сокращение ручного тестирования
- **Для документации:** Актуальные диаграммы архитектуры, генерируемые из кода

## 🏗️ Архитектура проекта

### Модули

```
example-rag-by-spring-documentation/
├── example-rag                    # RAG-приложение (Spring AI + PostgreSQL + Ollama)
├── stdio-sync-mcp-server          # MCP-сервер (STDIO, синхронный)
├── stdio-sync-mcp-client          # MCP-клиент (STDIO, синхронный)
├── webmvc-sync-mcp-server         # MCP-сервер (WebMVC, SSE, синхронный)
├── webmvc-sync-mcp-client         # MCP-клиент (WebMVC, SSE, синхронный)
├── webflux-async-mcp-server       # MCP-сервер (WebFlux, SSE, асинхронный)
├── webflux-async-mcp-client       # MCP-клиент (WebFlux, SSE, асинхронный)
└── mcp-server-jar-unpacker        # Утилита декомпиляции JAR
```

## 📊 Уровни диаграмм C4

### Level 1: System Context

Диаграмма показывает систему и внешние зависимости:

- **Пользователи** (разработчики, тестировщики)
- **MCP-клиенты** (Claude Desktop, MCP Inspector)
- **Внешние сервисы** (Ollama, PostgreSQL, Maven Repository)

### Level 2: Container

Диаграмма показывает контейнеры внутри системы:

- **Spring Boot приложения** (MCP-серверы)
- **Базы данных** (PostgreSQL для RAG)
- **Векторные хранилища** (PGVector)

### Level 3: Component

Диаграмма показывает компоненты внутри контейнеров:

- **JsonRpcHandler** — обработка JSON-RPC запросов
- **ToolRegistry** — реестр инструментов
- **Tools** — реализация инструментов
- **Services** — сервисы бизнес-логики

### Level 4: Code

Диаграмма показывает классы и методы (опционально, для сложных компонентов).

## 🛠️ Инструменты

### Structurizr DSL

**Зависимость Maven:**
```xml
<dependency>
    <groupId>com.structurizr</groupId>
    <artifactId>structurizr-dsl</artifactId>
    <version>2.1.1</version>
</dependency>
```

### structurizr-site-generatr

**CLI установка (Homebrew):**
```bash
brew install avisi-cloud/tap/structurizr-site-generatr
```

**Docker образ:**
```bash
docker pull ghcr.io/avisi-cloud/structurizr-site-generatr
```

## 📁 Структура каталога

```
docs/architecture/
├── workspace.dsl              # Основная модель Structurizr
├── views/                     # Представления (диаграммы)
│   ├── system-context.dsl     # Level 1: System Context
│   ├── containers.dsl         # Level 2: Container
│   └── components.dsl         # Level 3: Component
├── assets/                    # Дополнительные ресурсы
│   └── logo.png
└── output/                    # Сгенерированный сайт (не коммитится)
    ├── index.html
    ├── diagrams/              # PNG/SVG диаграммы
    └── documentation/         # Документация
```

## 🚀 Генерация диаграмм

### Через Docker (рекомендуется)

```bash
# Генерация статического сайта
docker run -it --rm \
  -v $(pwd)/docs/architecture:/var/model \
  ghcr.io/avisi-cloud/structurizr-site-generatr \
  generate-site \
  --workspace-file /var/model/workspace.dsl \
  --assets-dir /var/model/assets \
  --output-dir /var/model/output

# Запуск сервера разработки (автообновление)
docker run -it --rm \
  -v $(pwd)/docs/architecture:/var/model \
  -p 8080:8080 \
  ghcr.io/avisi-cloud/structurizr-site-generatr \
  serve \
  --workspace-file /var/model/workspace.dsl
```

### Через Maven Exec Plugin

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>3.1.0</version>
    <executions>
        <execution>
            <id>generate-architecture-diagrams</id>
            <phase>site</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>docker</executable>
                <arguments>
                    <argument>run</argument>
                    <argument>-it</argument>
                    <argument>--rm</argument>
                    <argument>-v</argument>
                    <argument>${project.basedir}/docs/architecture:/var/model</argument>
                    <argument>ghcr.io/avisi-cloud/structurizr-site-generatr</argument>
                    <argument>generate-site</argument>
                    <argument>--workspace-file</argument>
                    <argument>/var/model/workspace.dsl</argument>
                    <argument>--output-dir</argument>
                    <argument>/var/model/output</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## 📝 Пример DSL

### workspace.dsl

```dsl
workspace {
    name: "example-rag-by-spring-documentation"
    description: "Многомодульный проект для демонстрации Spring AI и MCP"
    
    model {
        # Пользователи
        user:Person = "Пользователь" {
            description "Разработчик или тестировщик"
        }
        
        # Система
        system:SoftwareSystem = "example-rag-by-spring-documentation" {
            description "Многомодульный проект MCP-серверов"
            
            # MCP-серверы
            stdioServer:Container = "stdio-sync-mcp-server" {
                description "MCP-сервер с STDIO транспортом"
                technology "Spring Boot, Spring AI MCP"
            }
            
            webmvcServer:Container = "webmvc-sync-mcp-server" {
                description "MCP-сервер на Spring WebMVC"
                technology "Spring Boot, Spring AI MCP, SSE"
            }
            
            webfluxServer:Container = "webflux-async-mcp-server" {
                description "MCP-сервер на Spring WebFlux"
                technology "Spring Boot, Spring AI MCP, SSE (async)"
            }
            
            jarUnpacker:Container = "mcp-server-jar-unpacker" {
                description "Утилита декомпиляции JAR"
                technology "Java 17, CFR"
            }
            
            # RAG
            ragApp:Container = "example-rag" {
                description "RAG-приложение"
                technology "Spring Boot, Spring AI, PostgreSQL, PGVector"
            }
        }
        
        # Внешние зависимости
        ollama:SoftwareSystem = "Ollama" {
            description "Сервер для запуска LLM"
        }
        
        postgresql:SoftwareSystem = "PostgreSQL" {
            description "База данных с расширением PGVector"
        }
        
        mavenRepo:SoftwareSystem = "Maven Repository" {
            description "Локальный Maven репозиторий (~/.m2/repository)"
        }
        
        # Отношения
        user -> stdioServer "Использует"
        user -> webmvcServer "Использует"
        user -> webfluxServer "Использует"
        user -> jarUnpacker "Использует"
        
        ragApp -> ollama "Запросы к LLM"
        ragApp -> postgresql "Векторный поиск"
        jarUnpacker -> mavenRepo "Поиск JAR"
    }
    
    views {
        # System Context Diagram
        systemContext system "SystemContext" {
            title "System Context Diagram"
            include *
            autolayout lr
        }
        
        # Container Diagram
        container system "Containers" {
            title "Container Diagram"
            include *
            autolayout lr
        }
    }
    
    configuration {
        theme: default
        branding {
            logo: assets/logo.png
        }
    }
}
```

## 🎨 Стилизация

### Цветовая схема

```dsl
configuration {
    theme: default
    branding {
        colors {
            primary: #3b82f6
            secondary: #10b981
            tertiary: #f59e0b
        }
    }
}
```

### Настройки экспорта

```properties
# В модели workspace.dsl
!generatr.site.exporter c4
!generatr.theme dark
```

## 📊 Форматы экспорта

- **SVG** — векторный формат, подходит для документации
- **PNG** — растровый формат, подходит для презентаций
- **PlantUML** — для редактирования в PlantUML-совместимых редакторах
- **HTML** — статический сайт с навигацией

## 🔗 Интеграция с CI/CD

### GitHub Actions

```yaml
name: Generate Architecture Diagrams

on:
  push:
    paths:
      - 'docs/architecture/**'

jobs:
  generate-diagrams:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Generate diagrams
        run: |
          docker run -it --rm \
            -v $(pwd)/docs/architecture:/var/model \
            ghcr.io/avisi-cloud/structurizr-site-generatr \
            generate-site \
            --workspace-file /var/model/workspace.dsl \
            --output-dir /var/model/output
      
      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v3
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: ./docs/architecture/output
```

## 📚 Рекомендации

### Для новых проектов

1. **Начните с System Context** — опишите внешние зависимости
2. **Добавьте Container** — детализируйте архитектуру
3. **Используйте Component** — для сложных контейнеров
4. **Избегайте Level 4** — только для критически важных компонентов

### Для существующих проектов

1. **Начните с одного модуля** — например, mcp-server-jar-unpacker
2. **Документируйте по мере изменений** — обновляйте DSL при изменении архитектуры
3. **Автоматизируйте генерацию** — добавьте в CI/CD

## 🔗 Ссылки

- [Structurizr DSL Documentation](https://docs.structurizr.com/dsl/)
- [C4 Model](https://c4model.com/)
- [structurizr-site-generatr](https://github.com/avisi-cloud/structurizr-site-generatr)
- [Глобальный план проекта](../../docs/global-plans.md)

## 📝 Статус задачи

| Поле | Значение |
|------|----------|
| **Задача:** | TASK-026 |
| **Статус:** | В выполнении |
| **Модуль:** | common |
| **Дата начала:** | 2026-03-26 |

---

*Документ создан в рамках задачи TASK-026: Structurizr — диаграммы архитектуры*
