workspace {
    name: "example-rag-by-spring-documentation"
    description: "Многомодульный Maven-проект для демонстрации работы с Spring AI и MCP (Model Context Protocol)"
    
    model {
        # ==================== Пользователи ====================
        developer:Person = "Разработчик" {
            description "Разработчик MCP-серверов и клиентов"
        }
        
        tester:Person = "Тестировщик" {
            description "Тестировщик MCP-серверов"
        }
        
        # ==================== Основная система ====================
        system:SoftwareSystem = "example-rag-by-spring-documentation" {
            description "Многомодульный проект для демонстрации Spring AI и MCP"
            
            # ==================== MCP-серверы ====================
            stdioServer:Container = "stdio-sync-mcp-server" {
                description "MCP-сервер с STDIO транспортом (синхронный)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3, STDIO"
            }
            
            webmvcServer:Container = "webmvc-sync-mcp-server" {
                description "MCP-сервер на Spring WebMVC (синхронный, SSE)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3, SSE"
            }
            
            webfluxServer:Container = "webflux-async-mcp-server" {
                description "MCP-сервер на Spring WebFlux (асинхронный, SSE)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3, SSE"
            }
            
            jarUnpacker:Container = "mcp-server-jar-unpacker" {
                description "Утилита декомпиляции Java-классов из JAR"
                technology "Java 17, CFR 0.152, MCP Protocol"
                
                # Компоненты mcp-server-jar-unpacker
                server:Component = "Server" {
                    description "Основной класс MCP-сервера"
                    technology "Java"
                }
                
                jsonRpcHandler:Component = "JsonRpcHandler" {
                    description "Обработка JSON-RPC 2.0 запросов"
                    technology "Java"
                }
                
                toolRegistry:Component = "ToolRegistry" {
                    description "Реестр MCP-инструментов"
                    technology "Java"
                }
                
                findClassTool:Component = "FindClassTool" {
                    description "Поиск JAR по имени класса"
                    technology "Java"
                }
                
                decompileTool:Component = "DecompileClassTool" {
                    description "Полная декомпиляция класса через CFR"
                    technology "Java, CFR"
                }
                
                jarCacheService:Component = "JarCacheService" {
                    description "Кэширование JAR-файлов с TTL"
                    technology "Java"
                }
                
                jarSearchService:Component = "JarSearchService" {
                    description "Многопоточный поиск классов в JAR"
                    technology "Java"
                }
            }
            
            # ==================== RAG-приложение ====================
            ragApp:Container = "example-rag" {
                description "RAG-приложение (Retrieval Augmented Generation)"
                technology "Spring Boot 4.0.2, Spring AI 2.0.0-M2, PostgreSQL, PGVector"
            }
            
            # ==================== MCP-клиенты ====================
            stdioClient:Container = "stdio-sync-mcp-client" {
                description "MCP-клиент для STDIO-сервера (синхронный)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3"
            }
            
            webmvcClient:Container = "webmvc-sync-mcp-client" {
                description "MCP-клиент для WebMVC-сервера (синхронный)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3"
            }
            
            webfluxClient:Container = "webflux-async-mcp-client" {
                description "MCP-клиент для WebFlux-сервера (асинхронный)"
                technology "Spring Boot 3.5.5, Spring AI MCP 1.0.3"
            }
        }
        
        # ==================== Внешние системы ====================
        ollama:SoftwareSystem = "Ollama" {
            description "Сервер для запуска локальных LLM"
            url "https://ollama.ai"
        }
        
        postgresql:SoftwareSystem = "PostgreSQL + PGVector" {
            description "Векторная база данных для RAG"
            url "https://github.com/pgvector/pgvector"
        }
        
        mavenRepo:SoftwareSystem = "Maven Repository (локальный)" {
            description "Локальный Maven репозиторий (~/.m2/repository)"
        }
        
        claudeDesktop:SoftwareSystem = "Claude Desktop" {
            description "MCP-клиент от Anthropic"
            url "https://claude.ai"
        }
        
        mcpInspector:SoftwareSystem = "MCP Inspector" {
            description "Инструмент для отладки MCP-серверов"
            url "https://github.com/modelcontextprotocol/inspector"
        }
        
        # ==================== Отношения (пользователи) ====================
        developer -> stdioServer "Разрабатывает и тестирует"
        developer -> webmvcServer "Разрабатывает и тестирует"
        developer -> webfluxServer "Разрабатывает и тестирует"
        developer -> jarUnpacker "Использует для декомпиляции"
        developer -> mcpInspector "Использует для отладки"
        
        tester -> stdioServer "Тестирует"
        tester -> webmvcServer "Тестирует"
        tester -> webfluxServer "Тестирует"
        tester -> jarUnpacker "Использует для анализа"
        
        claudeDesktop -> stdioServer "Вызывает инструменты через STDIO"
        claudeDesktop -> webmvcServer "Вызывает инструменты через SSE"
        claudeDesktop -> webfluxServer "Вызывает инструменты через SSE"
        claudeDesktop -> jarUnpacker "Вызывает инструменты через STDIO"
        
        mcpInspector -> stdioServer "Отлаживает"
        mcpInspector -> webmvcServer "Отлаживает"
        mcpInspector -> webfluxServer "Отлаживает"
        mcpInspector -> jarUnpacker "Отлаживает"
        
        # ==================== Отношения (внутренние) ====================
        stdioClient -> stdioServer "Вызов инструментов"
        webmvcClient -> webmvcServer "Вызов инструментов"
        webfluxClient -> webfluxServer "Вызов инструментов"
        
        # ==================== Отношения (внешние) ====================
        ragApp -> ollama "Запросы к LLM для генерации ответов"
        ragApp -> postgresql "Векторный поиск и хранение эмбеддингов"
        jarUnpacker -> mavenRepo "Поиск и чтение JAR-файлов"
        
        # ==================== Внутренние связи jarUnpacker ====================
        server -> jsonRpcHandler "Делегирует обработку"
        jsonRpcHandler -> toolRegistry "Получает инструмент"
        toolRegistry -> findClassTool "Вызывает"
        toolRegistry -> decompileTool "Вызывает"
        findClassTool -> jarCacheService "Использует кэш"
        findClassTool -> jarSearchService "Использует поиск"
        decompileTool -> jarCacheService "Использует кэш"
    }
    
    views {
        # ==================== System Context Diagram ====================
        systemContext system "SystemContext" {
            title "System Context Diagram — example-rag-by-spring-documentation"
            description "Взаимодействие системы с внешними пользователями и сервисами"
            
            include developer
            include tester
            include system
            include ollama
            include postgresql
            include mavenRepo
            include claudeDesktop
            include mcpInspector
            
            autolayout lr
        }
        
        # ==================== Container Diagram ====================
        container system "Containers" {
            title "Container Diagram — example-rag-by-spring-documentation"
            description "Контейнеры системы и их взаимодействие"
            
            include stdioServer
            include webmvcServer
            include webfluxServer
            include jarUnpacker
            include ragApp
            include stdioClient
            include webmvcClient
            include webfluxClient
            
            include ollama
            include postgresql
            include mavenRepo
            
            autolayout lr
        }
        
        # ==================== Component Diagram (jarUnpacker) ====================
        component jarUnpacker "Components" {
            title "Component Diagram — mcp-server-jar-unpacker"
            description "Компоненты утилиты декомпиляции JAR"
            
            include server
            include jsonRpcHandler
            include toolRegistry
            include findClassTool
            include decompileTool
            include jarCacheService
            include jarSearchService
            
            autolayout tb
        }
    }
    
    configuration {
        theme: default
        description "Диаграммы архитектуры проекта example-rag-by-spring-documentation"
        
        # Настройки для structurizr-site-generatr
        !generatr.site.exporter c4
        !generatr.theme default
        !generatr.site.branding.description "Многомодульный MCP-проект"
    }
}
