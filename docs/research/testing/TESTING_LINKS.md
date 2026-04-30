## 📊 Агрегированный отчет по инструментам тестирования AI-агентов, навыков, MCP-серверов и моделей

---

### 🧩 Комплексное нагрузочное тестирование агентов

| Инструмент         | Краткое описание                                                                          | GitHub                                                                                                                      |
|--------------------|-------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------|
| **Terminal-Bench** | Индустриальный бенчмарк для оценки агентов в Docker-контейнерах с метриками точности.     | [laude-institute/terminal-bench](https://github.com/laude-institute/terminal-bench)                                         |
| **CLI Arena**      | Фреймворк для оценки AI-агентов в итеративных сценариях на реальных кодовых базах.        | Доступен через PyPI ([pypi.org/project/cli-arena](https://pypi.org/project/cli-arena/)), публичный репозиторий не обнаружен |
| **Harbor**         | Фреймворк для запуска оценок агентов и создания сред для RL от создателей Terminal-Bench. | [harbor-framework/harbor](https://github.com/harbor-framework/harbor)                                                       |
| **RA.Aid**         | Автономный агент разработки на LangGraph для тестирования на задачах SWE-Bench.           | [ai-christianson/RA.Aid](https://github.com/ai-christianson/RA.Aid)                                                         |
| **SWE-bench**      | Бенчмарк для оценки LLM на реальных задачах исправления багов из GitHub-репозиториев.     | [princeton-nlp/swe-bench](https://github.com/princeton-nlp/swe-bench)                                                       |
| **SWE-Gym**        | Набор задач для обучения и оценки агентов на реальных программных проектах.               | [swe-gym/swe-gym](https://github.com/swe-gym/swe-gym)                                                                       |
| **R2E-Gym**        | Среда для обучения с подкреплением агентов на задачах разработки ПО.                      | Не обнаружен                                                                                                                |
| **LiveCodeBench**  | Бенчмарк для оценки способности моделей генерировать код в реальном времени.              | [livecodebench/livecodebench](https://github.com/livecodebench/livecodebench)                                               |
| **MLE-bench**      | Бенчмарк для оценки AI-агентов на задачах машинного обучения.                             | [mlfoundations/mle-bench](https://github.com/mlfoundations/mle-bench)                                                       |
| **AstaBench**      | Бенчмарк научных способностей: анализ данных, планирование, кодирование и поиск.          | [asta-bench/asta-bench](https://github.com/asta-bench/asta-bench)                                                           |
| **xbench**         | Бенчмарк от Sequoia China для оценки верхней границы способностей AI-систем.              | Не обнаружен                                                                                                                |
| **PICARD**         | Фреймворк для генерации уникальных тестов через многослойную рандомизацию.                | Не обнаружен                                                                                                                |
| **MLflow**         | Платформа для оценки AI-агентов и ML-моделей (30M+ загрузок/мес).                         | [mlflow/mlflow](https://github.com/mlflow/mlflow)                                                                           |

---

### 🛠️ Тестирование конкретных навыков (Skills)

| Инструмент                               | Краткое описание                                                                           | GitHub                                                                                                                            |
|------------------------------------------|--------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **Waza**                                 | CLI-фреймворк от Microsoft на Go для тестирования навыков в стиле TDD.                     | [microsoft/waza](https://github.com/microsoft/waza)                                                                               |
| **Skilljack Evals**                      | Node.js-библиотека для измерения обнаруживаемости навыка и качества результата.            | [olaservo/skilljack-evals](https://github.com/olaservo/skilljack-evals)                                                           |
| **SkillTester**                          | Агентно-ориентированная бенчмарк-система с проверкой полезности и безопасности.            | [skilltester-ai/skilltester](https://github.com/skilltester-ai/skilltester)                                                       |
| **UPskill**                              | Инструмент от Hugging Face для генерации навыков на основе трассировок агентов-«учителей». | [huggingface/upskill](https://github.com/huggingface/upskill)                                                                     |
| **ClawEnvKit**                           | Инструментарий для создания тестовых сред для claw-подобных агентов (OpenClaw).            | [xirui-li/ClawEnvKit](https://github.com/xirui-li/ClawEnvKit)                                                                     |
| **Skilldoc**                             | Автоматический генератор SKILL.md из вывода --help CLI-инструментов.                       | Доступен через npm ([npmjs.com/package/skilldoc](https://www.npmjs.com/package/skilldoc)), отдельный репозиторий не обнаружен     |
| **Skillscore**                           | Стандарт оценки качества SKILL.md-файлов по 7 категориям.                                  | Доступен через npm ([npmjs.com/package/skillscore](https://www.npmjs.com/package/skillscore)), отдельный репозиторий не обнаружен |
| **Tool Calling Benchmark (MikeVeerman)** | Бенчмарк для оценки способности моделей определять, когда вызывать инструмент.             | [MikeVeerman/tool-calling-benchmark](https://github.com/MikeVeerman/tool-calling-benchmark)                                       |
| **Tool Calling Bench (Zux1U)**           | Бенчмарк для моделей llama.cpp: качество вызова, restraint, согласованность.               | [Zux1U/tool_calling_bench](https://github.com/Zux1U/tool_calling_bench)                                                           |

---

### ⚙️ Специализированные инструменты

#### 🔒 Тестирование безопасности
| Инструмент      | Краткое описание                                                    | GitHub                                                  |
|-----------------|---------------------------------------------------------------------|---------------------------------------------------------|
| **CheckAgent**  | Плагин для pytest, прогоняющий до 101 атакующего запроса на агента. | [xydac/checkagent](https://github.com/xydac/checkagent) |

#### 📊 Тестирование через трассировку
| Инструмент     | Краткое описание                                                             | GitHub                                                                |
|----------------|------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| **Agentevals** | Фреймворк от LangChain для оценки поведения агента по трассам OpenTelemetry. | [langchain-ai/agentevals](https://github.com/langchain-ai/agentevals) |

#### 🎭 Сценарное / приемочное тестирование
| Инструмент            | Краткое описание                                                      | GitHub                                                                                |
|-----------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| **Agentest**          | Симуляция поведения пользователя с перехватом вызовов и LLM-судьей.   | [r-prem/agentest](https://github.com/r-prem/agentest)                                 |
| **Agentic Usability** | CLI от PSPDFKit для проверки, как AI-агенты используют ваш SDK.       | [PSPDFKit-labs/agentic-usability](https://github.com/PSPDFKit-labs/agentic-usability) |
| **Aethr**             | CLI-агент для запуска тестовых сценариев на естественном языке с MCP. | [autifyhq/aethr](https://github.com/autifyhq/aethr)                                   |

#### 📋 Анализ сессий
| Инструмент        | Краткое описание                                                                       | GitHub                                                              |
|-------------------|----------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| **Agent Harness** | Инструмент для сравнительного анализа сессий агента на основе структурированных логов. | [builtbyV/agent-builder](https://github.com/builtbyV/agent-builder) |

---

### 🧪 Тестирование MCP-серверов

#### ✅ Валидация и отладка
| Инструмент                         | Краткое описание                                                                                 | GitHub                                                                                                                                                    |
|------------------------------------|--------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| **MCP Test Kit**                   | CLI-фреймворк для комплексной проверки MCP-серверов: валидация, бенчмаркинг, аудит безопасности. | Доступен через npm ([npmjs.com/package/@cybeleri/mcp-test-kit](https://www.npmjs.com/package/@cybeleri/mcp-test-kit)), отдельный репозиторий не обнаружен |
| **MCP Server Tester**              | CLI-инструмент для автоматизированного тестирования: tools, evals, compliance.                   | [steviec/mcp-server-tester](https://github.com/steviec/mcp-server-tester)                                                                                 |
| **MCP Conformance Test Framework** | Официальный фреймворк для проверки соответствия спецификации MCP.                                | [modelcontextprotocol/conformance](https://github.com/modelcontextprotocol/conformance)                                                                   |
| **MCP Probe**                      | Клиентская библиотека и отладчик на Rust с TUI-интерфейсом.                                      | [conikeec/mcp-probe](https://github.com/conikeec/mcp-probe)                                                                                               |
| **MCP Debug**                      | Инструмент командной строки для отладки MCP-серверов с поддержкой streamable-http и OAuth 2.1.   | Доступен через pkg.go.dev ([pkg.go.dev/mcp-debug](https://pkg.go.dev/mcp-debug)), отдельный репозиторий не обнаружен                                      |
| **MCP Playground**                 | Веб-инструмент для интроспекции и тестирования MCP-серверов из браузера.                         | [emergent-lab/mcp-playground](https://github.com/emergent-lab/mcp-playground)                                                                             |
| **MCP Tester**                     | «Швейцарский нож» для проверки протокола, тестирования инструментов и диагностики.               | Доступен через docs.rs ([docs.rs/mcp-tester](https://docs.rs/mcp-tester)), отдельный репозиторий не обнаружен                                             |
| **Hoot**                           | Инструмент тестирования MCP в стиле Postman с OAuth 2.1 поддержкой.                              | [Portkey-AI/hoot](https://github.com/Portkey-AI/hoot)                                                                                                     |
| **MCP Inspector**                  | Визуальный отладчик от Anthropic с GUI для MCP-серверов.                                         | [modelcontextprotocol/inspector](https://github.com/modelcontextprotocol/inspector)                                                                       |
| **MCP Tools**                      | Универсальный CLI для взаимодействия с MCP-серверами (обнаружение, вызов, mock-серверы).         | [f/mcptools](https://github.com/f/mcptools)                                                                                                               |

#### 🛡️ Безопасность и стресс-тестирование
| Инструмент               | Краткое описание                                                        | GitHub                                                                                                                 |
|--------------------------|-------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| **MCPSafe**              | Опенсорсный фреймворк для аудита безопасности работающих MCP-серверов.  | Доступен через PyPI ([pypi.org/project/mcpsafe](https://pypi.org/project/mcpsafe)), отдельный репозиторий не обнаружен |
| **Inspector Grizzly**    | Визуальный инструмент на базе MCP Inspector с анализатором уязвимостей. | [alpic-ai/grizzly](https://github.com/alpic-ai/grizzly)                                                                |
| **MCP Client Inspector** | «Злонамеренный» MCP-сервер для аудита безопасности клиентов.            | [TreRB/mcp-client-inspector](https://github.com/TreRB/mcp-client-inspector)                                            |

#### 🚀 Нагрузочное тестирование и бенчмаркинг
| Инструмент                       | Краткое описание                                                                             | GitHub                                                                                                                                  |
|----------------------------------|----------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| **MCP Benchmark Runner (mcpbr)** | Бенчмарк оценки влияния MCP-сервера на производительность агента на SWE-bench.               | Доступен через PyPI ([pypi.org/project/mcpbr](https://pypi.org/project/mcpbr)), отдельный репозиторий не обнаружен                      |
| **LiveMCPBench**                 | Бенчмарк для оценки навигации агентов среди 70 серверов и 527 инструментов.                  | [icip-cas/LiveMCPBench](https://github.com/icip-cas/LiveMCPBench)                                                                       |
| **MCPToolBench++**               | Бенчмарк с 4000+ MCP-серверов из 45+ категорий для оценки обнаружения и выбора инструментов. | [mcp-tool-bench/MCPToolBenchPP](https://github.com/mcp-tool-bench/MCPToolBenchPP)                                                       |
| **Benchmark MCP**                | Node.js клиент для нагрузочного тестирования MCP-серверов в разных режимах.                  | Доступен через npm ([npmjs.com/package/benchmark-mcp](https://www.npmjs.com/package/benchmark-mcp)), отдельный репозиторий не обнаружен |

#### ⚙️ Автоматизация и оценка
| Инструмент          | Краткое описание                                                                            | GitHub                                                                                                                                                            |
|---------------------|---------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **MCP Observatory** | Инструмент тестирования в виде MCP-сервера: автономное сканирование и верификация.          | Доступен через npm ([npmjs.com/package/@kryptosai/mcp-observatory](https://www.npmjs.com/package/@kryptosai/mcp-observatory)), отдельный репозиторий не обнаружен |
| **MCP-Eval**        | Фреймворк в стиле pytest для оценки MCP-серверов с метриками задержки, токенов и стоимости. | [lastmile-ai/mcp-eval](https://github.com/lastmile-ai/mcp-eval)                                                                                                   |
| **MCP Interviewer** | Python CLI от Microsoft Research для раннего выявления проблем MCP-серверов.                | [microsoft/mcp-interviewer](https://github.com/microsoft/mcp-interviewer)                                                                                         |
| **MCPVals**         | Библиотека для оценки MCP-серверов с покрытием всей спецификации, интеграция с vitest.      | [Kylejeong2/mcpvals](https://github.com/Kylejeong2/mcpvals)                                                                                                       |

---

### 📊 Тестирование LLM-моделей

| Инструмент                      | Краткое описание                                                                           | GitHub                                                                                                                                                     |
|---------------------------------|--------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Vertex AI Evaluation**        | Управляемый сервис Google Cloud для оценки качества, безопасности и полезности моделей.    | Входит в [Google Cloud Platform](https://cloud.google.com/vertex-ai), SDK: [googleapis/python-aiplatform](https://github.com/googleapis/python-aiplatform) |
| **Agent Development Kit (ADK)** | Инструментарий от Google для трассировки и оценки решений агентов.                         | [google/adk](https://github.com/google/adk)                                                                                                                |
| **DeepEval**                    | Фреймворк с открытым кодом в стиле pytest с 50+ метриками для RAG, агентов и безопасности. | [confident-ai/deepeval](https://github.com/confident-ai/deepeval)                                                                                          |
| **Deepchecks**                  | Платформа для непрерывной валидации LLM-приложений на смещения и устойчивость.             | [deepchecks/deepchecks](https://github.com/deepchecks/deepchecks)                                                                                          |
| **HELM (Stanford)**             | Фреймворк для целостной оценки языковых моделей от Stanford CRFM.                          | [stanford-crfm/helm](https://github.com/stanford-crfm/helm)                                                                                                |
| **LM Evaluation Harness**       | Фреймворк от EleutherAI с 60+ стандартными бенчмарками (MMLU, GSM8K, HellaSwag).           | [EleutherAI/lm-evaluation-harness](https://github.com/EleutherAI/lm-evaluation-harness)                                                                    |
| **Bloom (Anthropic)**           | Опенсорсный агентный фреймворк для генерации поведенческих оценок frontier-моделей.        | [anthropics/bloom](https://github.com/anthropics/bloom)                                                                                                    |
| **EvalScope**                   | Официальный фреймворк ModelScope для оценки LLM, мультимодальных и embedding-моделей.      | [modelscope/evalscope](https://github.com/modelscope/evalscope)                                                                                            |
| **AgentEval**                   | .NET тулкит для оценки AI-агентов: валидация инструментов, метрики RAG, red teaming.       | Доступен через NuGet ([nuget.org/packages/AgentEval](https://www.nuget.org/packages/AgentEval)), отдельный репозиторий не обнаружен                        |

---

### 🧪 Тестирование эмбеддеров и поисковых систем

| Инструмент                | Краткое описание                                                                    | GitHub                                                                             |
|---------------------------|-------------------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| **MTEB**                  | Мультимодальный бенчмарк для оценки эмбеддингов и retrieval-систем (1000+ языков).  | [embeddings-benchmark/mteb](https://github.com/embeddings-benchmark/mteb)          |
| **RTEB**                  | Новый стандарт для оценки истинной точности retrieval-эмбеддингов от Hugging Face.  | Входит в [embeddings-benchmark/mteb](https://github.com/embeddings-benchmark/mteb) |
| **NVIDIA NeMo Evaluator** | Контейнеризированное решение для оценки моделей, совместимое с MTEB.                | [NVIDIA-NeMo/Evaluator](https://github.com/NVIDIA-NeMo/Evaluator)                  |
| **VDBBench**              | Инструмент для бенчмаркинга векторных баз данных (Milvus, Elasticsearch, pgvector). | [zilliztech/VectorDBBench](https://github.com/zilliztech/VectorDBBench)            |
| **HUME**                  | Бенчмарк для измерения разрыва между человеком и моделью в задачах эмбеддингов.     | Входит в [embeddings-benchmark/mteb](https://github.com/embeddings-benchmark/mteb) |

---

### 🔍 Тестирование RAG-систем

| Инструмент              | Краткое описание                                                                        | GitHub                                                                    |
|-------------------------|-----------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| **RAGAS**               | Фреймворк для оценки RAG-пайплайнов: faithfulness, answer relevancy, context precision. | [explodinggradients/ragas](https://github.com/explodinggradients/ragas)   |
| **TruLens**             | Фреймворк с обратной связью и трассировкой OpenTelemetry для LLM-приложений.            | [truera/trulens](https://github.com/truera/trulens)                       |
| **RAGChecker**          | Продвинутый фреймворк от Amazon Science для диагностики RAG-систем.                     | [amazon-science/RAGChecker](https://github.com/amazon-science/RAGChecker) |
| **MLflow LLM Evaluate** | Модуль MLflow для оценки LLM с поддержкой кастомных пайплайнов.                         | Входит в [mlflow/mlflow](https://github.com/mlflow/mlflow)                |

---

### 🛡️ Безопасность и Red Teaming

| Инструмент                           | Краткое описание                                                                                   | GitHub                                                                                                                           |
|--------------------------------------|----------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------|
| **AI-Infra-Guard**                   | Платформа от Tencent Zhuque Lab для red teaming AI-экосистем: сканирование OpenClaw, MCP, агентов. | [Tencent/AI-Infra-Guard](https://github.com/Tencent/AI-Infra-Guard)                                                              |
| **Giskard**                          | Платформа для тестирования и red teaming LLM-агентов с автоматическим обнаружением уязвимостей.    | [giskard-ai/giskard](https://github.com/giskard-ai/giskard)                                                                      |
| **ai-blackteam**                     | Автоматизированный фреймворк для red teaming с 89 многоходовыми адаптивными атаками.               | Доступен через PyPI ([pypi.org/project/ai-blackteam](https://pypi.org/project/ai-blackteam)), отдельный репозиторий не обнаружен |
| **Highflame RED**                    | Инструмент для автономного адверсариального тестирования AI-агентов.                               | [highflame-ai/highflame](https://github.com/highflame-ai/highflame)                                                              |
| **HackMyAgent**                      | Тулкит безопасности с 209 проверками, семантическим анализом и симуляцией поведения.               | [opena2a-org/hackmyagent](https://github.com/opena2a-org/hackmyagent)                                                            |
| **Akto Agent Probe**                 | Движок автоматизированного red teaming для агентного AI с глубоким сканированием MCP.              | [akto-api-security/akto](https://github.com/akto-api-security/akto)                                                              |
| **AI Red Teaming Agent (Microsoft)** | Инструмент для проактивного поиска рисков безопасности в генеративных AI-системах.                 | Входит в [Azure/PyRIT](https://github.com/Azure/PyRIT)                                                                           |
| **Basilisk**                         | Опенсорсный фреймворк для red teaming с генетической эволюцией промптов (32 модуля атак).          | [regaan/basilisk](https://github.com/regaan/basilisk)                                                                            |

---

### 📌 Примечания

- **Инструменты без публичного GitHub-репозитория**: CLI Arena, SWE-Gym, MLE-bench, AstaBench, xbench, PICARD, Skilldoc, Skillscore, MCP Test Kit, MCP Debug, MCP Tester, MCPSafe, MCP Benchmark Runner, Benchmark MCP, MCP Observatory, AgentEval (.NET), ai-blackteam. Эти инструменты доступны через пакетные менеджеры (npm, PyPI, NuGet) или документацию соответствующих организаций.
- Для HUME и RTEB репозиторий [embeddings-benchmark/mteb](https://github.com/embeddings-benchmark/mteb) является общим хабом.
- Vertex AI Evaluation и AI Red Teaming Agent (Microsoft) являются частью более крупных платформ (Google Cloud и Azure/PyRIT соответственно).