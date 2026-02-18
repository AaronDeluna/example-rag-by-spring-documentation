```markdown
[[ChatMemory]]
# Чат-память

Большие языковые модели (LLM) являются статeless, что означает, что они не сохраняют информацию о предыдущих взаимодействиях. Это может быть ограничением, когда вы хотите поддерживать контекст или состояние в нескольких взаимодействиях. Чтобы решить эту проблему, Spring AI предоставляет функции чат-памяти, которые позволяют вам хранить и извлекать информацию в ходе нескольких взаимодействий с LLM.

Абстракция `ChatMemory` позволяет вам реализовать различные типы памяти для поддержки различных случаев использования. Хранение сообщений обрабатывается `ChatMemoryRepository`, чья единственная ответственность заключается в хранении и извлечении сообщений. Решение о том, какие сообщения сохранять и когда их удалять, остается за реализацией `ChatMemory`. Примеры стратегий могут включать сохранение последних N сообщений, сохранение сообщений на определенный период времени или сохранение сообщений до достижения определенного лимита токенов.

Перед выбором типа памяти важно понять разницу между чат-памятью и историей чата.

- **Чат-память**. Информация, которую большая языковая модель сохраняет и использует для поддержания контекстуального осознания в ходе разговора.
- **История чата**. Вся история разговора, включая все сообщения, обмененные между пользователем и моделью.

Абстракция `ChatMemory` предназначена для управления _чат-памятью_. Она позволяет вам хранить и извлекать сообщения, которые имеют отношение к текущему контексту разговора. Однако она не является наилучшим выбором для хранения _истории чата_. Если вам нужно поддерживать полный учет всех обмененных сообщений, вам следует рассмотреть возможность использования другого подхода, например, полагаться на Spring Data для эффективного хранения и извлечения полной истории чата.

## Быстрый старт

Spring AI автоматически настраивает бин `ChatMemory`, который вы можете использовать непосредственно в своем приложении. По умолчанию он использует репозиторий в памяти для хранения сообщений (`InMemoryChatMemoryRepository`) и реализацию `MessageWindowChatMemory` для управления историей разговора. Если другой репозиторий уже настроен (например, Cassandra, JDBC или Neo4j), Spring AI будет использовать его вместо этого.

```java
@Autowired
ChatMemory chatMemory;
```

В следующих разделах будет подробнее описано различные типы памяти и репозитории, доступные в Spring AI.

## Типы памяти

Абстракция `ChatMemory` позволяет вам реализовать различные типы памяти, чтобы соответствовать различным случаям использования. Выбор типа памяти может значительно повлиять на производительность и поведение вашего приложения. В этом разделе описаны встроенные типы памяти, предоставляемые Spring AI, и их характеристики.

### Чат-память с окном сообщений

`MessageWindowChatMemory` поддерживает окно сообщений до указанного максимального размера. Когда количество сообщений превышает максимум, более старые сообщения удаляются, при этом системные сообщения сохраняются. Размер окна по умолчанию составляет 20 сообщений.

```java
MessageWindowChatMemory memory = MessageWindowChatMemory.builder()
    .maxMessages(10)
    .build();
```

Это тип сообщения по умолчанию, используемый Spring AI для автоматической настройки бина `ChatMemory`.

## Хранение памяти

Spring AI предлагает абстракцию `ChatMemoryRepository` для хранения чат-памяти. В этом разделе описаны встроенные репозитории, предоставляемые Spring AI, и как их использовать, но вы также можете реализовать свой собственный репозиторий, если это необходимо.

### Репозиторий в памяти

`InMemoryChatMemoryRepository` хранит сообщения в памяти, используя `ConcurrentHashMap`.

По умолчанию, если другой репозиторий не настроен, Spring AI автоматически настраивает бин `ChatMemoryRepository` типа `InMemoryChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
ChatMemoryRepository chatMemoryRepository;
```

Если вы хотите создать `InMemoryChatMemoryRepository` вручную, вы можете сделать это следующим образом:

```java
ChatMemoryRepository repository = new InMemoryChatMemoryRepository();
```

### JdbcChatMemoryRepository

`JdbcChatMemoryRepository` — это встроенная реализация, которая использует JDBC для хранения сообщений в реляционной базе данных. Она поддерживает несколько баз данных из коробки и подходит для приложений, которые требуют постоянного хранения чат-памяти.

Сообщения извлекаются в порядке возрастания временной метки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

Сначала добавьте следующую зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-jdbc'
}
```
======

Spring AI предоставляет автоматическую настройку для `JdbcChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
JdbcChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `JdbcChatMemoryRepository` вручную, вы можете сделать это, предоставив экземпляр `JdbcTemplate` и `JdbcChatMemoryRepositoryDialect`:

```java
ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
    .jdbcTemplate(jdbcTemplate)
    .dialect(new PostgresChatMemoryRepositoryDialect())
    .build();

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Поддерживаемые базы данных и абстракция диалектов

Spring AI поддерживает несколько реляционных баз данных через абстракцию диалектов. Следующие базы данных поддерживаются из коробки:

- PostgreSQL
- MySQL / MariaDB
- SQL Server
- HSQLDB
- Oracle Database

Правильный диалект может быть автоматически определен из JDBC URL при использовании `JdbcChatMemoryRepositoryDialect.from(DataSource)`. Вы можете расширить поддержку других баз данных, реализовав интерфейс `JdbcChatMemoryRepositoryDialect`.

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.chat.memory.repository.jdbc.initialize-schema` | Управляет, когда инициализировать схему. Значения: `embedded` (по умолчанию), `always`, `never`. | `embedded` |
| `spring.ai.chat.memory.repository.jdbc.schema` | Местоположение скрипта схемы для инициализации. Поддерживает `classpath:` URL и плейсхолдеры платформы. | `classpath:org/springframework/ai/chat/memory/repository/jdbc/schema-@@platform@@.sql` |
| `spring.ai.chat.memory.repository.jdbc.platform` | Платформа, используемая в скриптах инициализации, если используется плейсхолдер @@platform@@. | _автоопределяемый_ |

#### Инициализация схемы

Автоматическая настройка автоматически создаст таблицу `SPRING_AI_CHAT_MEMORY` при запуске, используя специфичный для поставщика SQL-скрипт для вашей базы данных. По умолчанию инициализация схемы выполняется только для встроенных баз данных (H2, HSQL, Derby и т. д.).

Вы можете управлять инициализацией схемы, используя свойство `spring.ai.chat.memory.repository.jdbc.initialize-schema`:

```properties
spring.ai.chat.memory.repository.jdbc.initialize-schema=embedded # Только для встроенных БД (по умолчанию)
spring.ai.chat.memory.repository.jdbc.initialize-schema=always   # Всегда инициализировать
spring.ai.chat.memory.repository.jdbc.initialize-schema=never    # Никогда не инициализировать (полезно с Flyway/Liquibase)
```

Чтобы переопределить местоположение скрипта схемы, используйте:

```properties
spring.ai.chat.memory.repository.jdbc.schema=classpath:/custom/path/schema-mysql.sql
```

#### Расширение диалектов

Чтобы добавить поддержку для новой базы данных, реализуйте интерфейс `JdbcChatMemoryRepositoryDialect` и предоставьте SQL для выбора, вставки и удаления сообщений. Затем вы можете передать свой пользовательский диалект в сборщик репозитория.

```java
ChatMemoryRepository chatMemoryRepository = JdbcChatMemoryRepository.builder()
    .jdbcTemplate(jdbcTemplate)
    .dialect(new MyCustomDbDialect())
    .build();
```

### CassandraChatMemoryRepository

`CassandraChatMemoryRepository` использует Apache Cassandra для хранения сообщений. Он подходит для приложений, которые требуют постоянного хранения чат-памяти, особенно для доступности, надежности, масштабируемости и использования функции времени жизни (TTL).

`CassandraChatMemoryRepository` имеет схему временных рядов, сохраняя записи всех прошлых окон чата, что полезно для управления и аудита. Рекомендуется установить время жизни на некоторое значение, например, три года.

Сообщения извлекаются в порядке возрастания временной метки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

Чтобы использовать `CassandraChatMemoryRepository`, сначала добавьте зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-cassandra</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-cassandra'
}
```
======

Spring AI предоставляет автоматическую настройку для `CassandraChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
CassandraChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `CassandraChatMemoryRepository` вручную, вы можете сделать это, предоставив экземпляр `CassandraChatMemoryRepositoryConfig`:

```java
ChatMemoryRepository chatMemoryRepository = CassandraChatMemoryRepository
    .create(CassandraChatMemoryRepositoryConfig.builder().withCqlSession(cqlSession));

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.cassandra.contactPoints` | Хост(ы) для инициализации обнаружения кластера | `127.0.0.1` |
| `spring.cassandra.port` | Порт нативного протокола Cassandra для подключения | `9042` |
| `spring.cassandra.localDatacenter` | Центр обработки данных Cassandra для подключения | `datacenter1` |
| `spring.ai.chat.memory.cassandra.time-to-live` | Время жизни (TTL) для сообщений, записанных в Cassandra |  |
| `spring.ai.chat.memory.cassandra.keyspace` | Пространство имен Cassandra | `springframework` |
| `spring.ai.chat.memory.cassandra.messages-column` | Имя столбца Cassandra для сообщений | `springframework` |
| `spring.ai.chat.memory.cassandra.table` | Таблица Cassandra | `ai_chat_memory` |
| `spring.ai.chat.memory.cassandra.initialize-schema` | Нужно ли инициализировать схему при запуске. | `true` |

#### Инициализация схемы

Автоматическая настройка автоматически создаст таблицу `ai_chat_memory`.

Вы можете отключить инициализацию схемы, установив свойство `spring.ai.chat.memory.repository.cassandra.initialize-schema` в `false`.

### Neo4j ChatMemoryRepository

`Neo4jChatMemoryRepository` — это встроенная реализация, которая использует Neo4j для хранения сообщений чата в виде узлов и связей в графовой базе данных свойств. Он подходит для приложений, которые хотят использовать возможности графа Neo4j для постоянного хранения чат-памяти.

Сообщения извлекаются в порядке возрастания индекса сообщения (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

Сначала добавьте следующую зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-neo4j</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-neo4j'
}
```
======

Spring AI предоставляет автоматическую настройку для `Neo4jChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
Neo4jChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `Neo4jChatMemoryRepository` вручную, вы можете сделать это, предоставив экземпляр `Driver` Neo4j:

```java
ChatMemoryRepository chatMemoryRepository = Neo4jChatMemoryRepository.builder()
    .driver(driver)
    .build();

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.chat.memory.repository.neo4j.sessionLabel` | Метка для узлов, которые хранят сессии разговоров | `Session` |
| `spring.ai.chat.memory.repository.neo4j.messageLabel` | Метка для узлов, которые хранят сообщения | `Message` |
| `spring.ai.chat.memory.repository.neo4j.toolCallLabel` | Метка для узлов, которые хранят вызовы инструментов (например, в сообщениях помощника) | `ToolCall` |
| `spring.ai.chat.memory.repository.neo4j.metadataLabel` | Метка для узлов, которые хранят метаданные сообщений | `Metadata` |
| `spring.ai.chat.memory.repository.neo4j.toolResponseLabel` | Метка для узлов, которые хранят ответы инструментов | `ToolResponse` |
| `spring.ai.chat.memory.repository.neo4j.mediaLabel` | Метка для узлов, которые хранят медиа, связанные с сообщением | `Media` |

#### Инициализация индексов

Репозиторий Neo4j автоматически обеспечит создание индексов для идентификаторов разговоров и индексов сообщений для оптимизации производительности. Если вы используете пользовательские метки, индексы будут созданы и для этих меток. Инициализация схемы не требуется, но вы должны убедиться, что ваш экземпляр Neo4j доступен для вашего приложения.

### CosmosDBChatMemoryRepository

`CosmosDBChatMemoryRepository` — это встроенная реализация, которая использует API NoSQL Azure Cosmos DB для хранения сообщений. Он подходит для приложений, которые требуют глобально распределенной, высокомасштабируемой документной базы данных для постоянного хранения чат-памяти. Репозиторий использует идентификатор разговора в качестве ключа раздела для обеспечения эффективного распределения данных и быстрого извлечения.

Сообщения извлекаются в порядке возрастания временной метки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

Сначала добавьте следующую зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-cosmos-db</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-cosmos-db'
}
```
======

Spring AI предоставляет автоматическую настройку для `CosmosDBChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
CosmosDBChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `CosmosDBChatMemoryRepository` вручную, вы можете сделать это, предоставив экземпляр `CosmosDBChatMemoryRepositoryConfig`:

```java
ChatMemoryRepository chatMemoryRepository = CosmosDBChatMemoryRepository
    .create(CosmosDBChatMemoryRepositoryConfig.builder()
        .withCosmosClient(cosmosAsyncClient)
        .withDatabaseName("chat-memory-db")
        .withContainerName("conversations")
        .build());

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.chat.memory.repository.cosmosdb.endpoint` | URI конечной точки Azure Cosmos DB. Обязательно для автоматической настройки. |  |
| `spring.ai.chat.memory.repository.cosmosdb.key` | Основной или вторичный ключ Azure Cosmos DB. Если не предоставлен, будет использована аутентификация Azure Identity. |  |
| `spring.ai.chat.memory.repository.cosmosdb.connection-mode` | Режим подключения для клиента Cosmos DB (`direct` или `gateway`). | `gateway` |
| `spring.ai.chat.memory.repository.cosmosdb.database-name` | Имя базы данных Cosmos DB. | `SpringAIChatMemory` |
| `spring.ai.chat.memory.repository.cosmosdb.container-name` | Имя контейнера Cosmos DB. | `ChatMemory` |
| `spring.ai.chat.memory.repository.cosmosdb.partition-key-path` | Путь ключа раздела для контейнера. | `/conversationId` |

#### Аутентификация

Репозиторий чат-памяти Cosmos DB поддерживает два метода аутентификации:

1. **Аутентификация на основе ключа**: Укажите свойство `spring.ai.chat.memory.repository.cosmosdb.key` с вашим основным или вторичным ключом Cosmos DB.
2. **Аутентификация Azure Identity**: Когда ключ не предоставлен, репозиторий использует Azure Identity (`DefaultAzureCredential`) для аутентификации с управляемой идентичностью, служебным принципалом или другими источниками учетных данных Azure.

#### Инициализация схемы

Автоматическая настройка автоматически создаст указанную базу данных и контейнер, если они не существуют. Контейнер настроен с идентификатором разговора в качестве ключа раздела (`/conversationId`), чтобы обеспечить оптимальную производительность для операций чат-памяти. Не требуется ручная настройка схемы.

Вы можете настроить имена базы данных и контейнера, используя вышеупомянутые свойства конфигурации.

### MongoChatMemoryRepository

`MongoChatMemoryRepository` — это встроенная реализация, которая использует MongoDB для хранения сообщений. Он подходит для приложений, которые требуют гибкой, документно-ориентированной базы данных для постоянного хранения чат-памяти.

Сообщения извлекаются в порядке возрастания временной метки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM. Этот порядок согласован для всех реализаций репозитория чат-памяти.

Сначала добавьте следующую зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-mongodb</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-mongodb'
}
```
======

Spring AI предоставляет автоматическую настройку для `MongoChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
MongoChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `MongoChatMemoryRepository` вручную, вы можете сделать это, предоставив экземпляр `MongoTemplate`:

```java
ChatMemoryRepository chatMemoryRepository = MongoChatMemoryRepository.builder()
    .mongoTemplate(mongoTemplate)
    .build();

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.chat.memory.repository.mongo.create-indices` | Должны ли индексы создаваться или пересоздаваться автоматически при запуске. Обратите внимание: изменение |  |
| `spring.ai.chat.memory.repository.mongo.ttl` | Время жизни (TTL) для сообщений, записанных в MongoDB, в секундах. Если не установлено, сообщения будут храниться бесконечно. | `0` |

#### Инициализация коллекции
Автоматическая настройка автоматически создаст коллекцию `ai_chat_memory` при запуске, если она еще не существует.

### RedisChatMemoryRepository

`RedisChatMemoryRepository` — это встроенная реализация, которая использует Redis Stack (с Redis Query Engine и RedisJSON) для хранения сообщений чата. Он подходит для приложений, которые требуют высокой производительности, низкой задержки и поддержки времени жизни (TTL) с расширенными возможностями запросов.

Репозиторий хранит сообщения в виде JSON-документов и создает поисковый индекс для эффективного выполнения запросов. Он также предоставляет расширенные возможности запросов через интерфейс `AdvancedRedisChatMemoryRepository` для поиска сообщений по содержимому, типу, диапазону времени и метаданным.

Сообщения извлекаются в порядке возрастания временной метки (от старых к новым), что является ожидаемым форматом для истории разговоров LLM.

Сначала добавьте следующую зависимость в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-redis</artifactId>
</dependency>
```

Gradle::
+
```groovy
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-chat-memory-repository-redis'
}
```
======

Spring AI предоставляет автоматическую настройку для `RedisChatMemoryRepository`, который вы можете использовать непосредственно в своем приложении.

```java
@Autowired
RedisChatMemoryRepository chatMemoryRepository;

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

Если вы хотите создать `RedisChatMemoryRepository` вручную, вы можете сделать это, предоставив клиент `JedisPooled`:

```java
JedisPooled jedisClient = new JedisPooled("localhost", 6379);

ChatMemoryRepository chatMemoryRepository = RedisChatMemoryRepository.builder()
    .jedisClient(jedisClient)
    .indexName("my-chat-index")
    .keyPrefix("my-chat:")
    .timeToLive(Duration.ofHours(24))
    .build();

ChatMemory chatMemory = MessageWindowChatMemory.builder()
    .chatMemoryRepository(chatMemoryRepository)
    .maxMessages(10)
    .build();
```

#### Свойства конфигурации

[cols="2,5,1",stripes=even]
| Свойство | Описание | Значение по умолчанию |
| --- | --- | --- |
| `spring.ai.chat.memory.redis.host` | Хост сервера Redis | `localhost` |
| `spring.ai.chat.memory.redis.port` | Порт сервера Redis | `6379` |
| `spring.ai.chat.memory.redis.index-name` | Имя поискового индекса Redis | `chat-memory-idx` |
| `spring.ai.chat.memory.redis.key-prefix` | Префикс ключа для записей чат-памяти | `chat-memory:` |
| `spring.ai.chat.memory.redis.time-to-live` | Время жизни для записей чат-памяти (например, `24h`, `30d`) | _без истечения_ |
| `spring.ai.chat.memory.redis.initialize-schema` | Нужно ли инициализировать схему Redis при запуске | `true` |
| `spring.ai.chat.memory.redis.max-conversation-ids` | Максимальное количество идентификаторов разговоров для возврата | `1000` |
| `spring.ai.chat.memory.redis.max-messages-per-conversation` | Максимальное количество сообщений для возврата за разговор | `1000` |

#### Расширенные возможности запросов

`RedisChatMemoryRepository` также реализует `AdvancedRedisChatMemoryRepository`, который предоставляет расширенные возможности запросов:

```java
// Приведение типа для доступа к расширенным функциям
AdvancedRedisChatMemoryRepository advancedRepo = (AdvancedRedisChatMemoryRepository) chatMemoryRepository;

// Найти сообщения по типу во всех разговорах
List<MessageWithConversation> userMessages = advancedRepo.findByType(MessageType.USER, 100);

// Найти сообщения, содержащие определенное содержимое
List<MessageWithConversation> results = advancedRepo.findByContent("Spring AI", 50);

// Найти сообщения в пределах временного диапазона
List<MessageWithConversation> recentMessages = advancedRepo.findByTimeRange(
    conversationId,
    Instant.now().minus(Duration.ofHours(1)),
    Instant.now(),
    100
);

// Найти сообщения по метаданным
List<MessageWithConversation> priorityMessages = advancedRepo.findByMetadata("priority", "high", 50);

// Выполнить пользовательские запросы Redis
List<MessageWithConversation> customResults = advancedRepo.executeQuery("@type:USER @content:Redis", 100);
```

#### Индексация полей метаданных

Чтобы включить эффективный запрос по пользовательским полям метаданных, вы можете настроить определения полей метаданных:

```properties
spring.ai.chat.memory.redis.metadata-fields[0].name=priority
spring.ai.chat.memory.redis.metadata-fields[0].type=tag
spring.ai.chat.memory.redis.metadata-fields[1].name=score
spring.ai.chat.memory.redis.metadata-fields[1].type=numeric
spring.ai.chat.memory.redis.metadata-fields[2].name=category
spring.ai.chat.memory.redis.metadata-fields[2].type=tag
```

Поддерживаемые типы полей: `tag` (для фильтрации по точному совпадению), `text` (для полнотекстового поиска) и `numeric` (для диапазонных запросов).

#### Инициализация схемы

Автоматическая настройка автоматически создаст поисковый индекс Redis при запуске, если он еще не существует. Вы можете отключить это поведение, установив `spring.ai.chat.memory.redis.initialize-schema=false`.

#### Требования

- Redis Stack 7.0 или выше (включает модули Redis Query Engine и RedisJSON)
- Библиотека клиента Jedis (включена в качестве зависимости)

## Память в клиенте чата

При использовании API ChatClient вы можете предоставить реализацию `ChatMemory`, чтобы поддерживать контекст разговора в ходе нескольких взаимодействий.

Spring AI предоставляет несколько встроенных советников, которые вы можете использовать для настройки поведения памяти `ChatClient` в зависимости от ваших потребностей.

> **Внимание:** В настоящее время промежуточные сообщения, обмененные с большой языковой моделью при выполнении вызовов инструментов, не сохраняются в памяти. Это ограничение текущей реализации будет устранено в будущих версиях. Если вам нужно сохранить эти сообщения, обратитесь к инструкциям по xref:api/tools.adoc#_user_controlled_tool_execution[Управляемому пользователем выполнению инструментов].

- `MessageChatMemoryAdvisor`. Этот советник управляет памятью разговора, используя предоставленную реализацию `ChatMemory`. При каждом взаимодействии он извлекает историю разговора из памяти и включает ее в подсказку в виде набора сообщений.
- `PromptChatMemoryAdvisor`. Этот советник управляет памятью разговора, используя предоставленную реализацию `ChatMemory`. При каждом взаимодействии он извлекает историю разговора из памяти и добавляет ее к системной подсказке в виде обычного текста.
- `VectorStoreChatMemoryAdvisor`. Этот советник управляет памятью разговора, используя предоставленную реализацию `VectorStore`. При каждом взаимодействии он извлекает историю разговора из векторного хранилища и добавляет ее к системному сообщению в виде обычного текста.

Например, если вы хотите использовать `MessageWindowChatMemory` с `MessageChatMemoryAdvisor`, вы можете настроить его следующим образом:

```java
ChatMemory chatMemory = MessageWindowChatMemory.builder().build();

ChatClient chatClient = ChatClient.builder(chatModel)
    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
    .build();
```

При выполнении вызова к `ChatClient` память будет автоматически управляться `MessageChatMemoryAdvisor`. История разговора будет извлечена из памяти на основе указанного идентификатора разговора:

```java
String conversationId = "007";

chatClient.prompt()
    .user("У меня есть лицензия на программирование?")
    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
    .call()
    .content();
```

### PromptChatMemoryAdvisor

#### Пользовательский шаблон

`PromptChatMemoryAdvisor` использует шаблон по умолчанию для дополнения системного сообщения извлеченной памятью разговора. Вы можете настроить это поведение, предоставив свой собственный объект `PromptTemplate` через метод сборки `.promptTemplate()`.

> **Примечание:** Предоставленный здесь `PromptTemplate` настраивает, как советник объединяет извлеченную память с системным сообщением. Это отличается от настройки `TemplateRenderer` на самом `ChatClient` (с использованием `.templateRenderer()`), что влияет на рендеринг содержимого первоначальной подсказки пользователя/системы **до** выполнения советника. См. xref:api/chatclient.adoc#_prompt_templates[Шаблоны подсказок ChatClient] для получения дополнительной информации о рендеринге шаблонов на уровне клиента.

Пользовательский `PromptTemplate` может использовать любую реализацию `TemplateRenderer` (по умолчанию используется `StPromptTemplate`, основанный на https://www.stringtemplate.org/[StringTemplate] engine). Важное требование заключается в том, что шаблон должен содержать следующие два плейсхолдера:

- плейсхолдер `instructions` для получения оригинального системного сообщения.
- плейсхолдер `memory` для получения извлеченной памяти разговора.

### VectorStoreChatMemoryAdvisor

#### Пользовательский шаблон

`VectorStoreChatMemoryAdvisor` использует шаблон по умолчанию для дополнения системного сообщения извлеченной памятью разговора. Вы можете настроить это поведение, предоставив свой собственный объект `PromptTemplate` через метод сборки `.promptTemplate()`.

> **Примечание:** Предоставленный здесь `PromptTemplate` настраивает, как советник объединяет извлеченную память с системным сообщением. Это отличается от настройки `TemplateRenderer` на самом `ChatClient` (с использованием `.templateRenderer()`), что влияет на рендеринг содержимого первоначальной подсказки пользователя/системы **до** выполнения советника. См. xref:api/chatclient.adoc#_prompt_templates[Шаблоны подсказок ChatClient] для получения дополнительной информации о рендеринге шаблонов на уровне клиента.

Пользовательский `PromptTemplate` может использовать любую реализацию `TemplateRenderer` (по умолчанию используется `StPromptTemplate`, основанный на https://www.stringtemplate.org/[StringTemplate] engine). Важное требование заключается в том, что шаблон должен содержать следующие два плейсхолдера:

- плейсхолдер `instructions` для получения оригинального системного сообщения.
- плейсхолдер `long_term_memory` для получения извлеченной памяти разговора.

## Память в модели чата

Если вы работаете непосредственно с `ChatModel`, а не с `ChatClient`, вы можете управлять памятью явно:

```java
// Создание экземпляра памяти
ChatMemory chatMemory = MessageWindowChatMemory.builder().build();
String conversationId = "007";

// Первое взаимодействие
UserMessage userMessage1 = new UserMessage("Меня зовут Джеймс Бонд");
chatMemory.add(conversationId, userMessage1);
ChatResponse response1 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
chatMemory.add(conversationId, response1.getResult().getOutput());

// Второе взаимодействие
UserMessage userMessage2 = new UserMessage("Как меня зовут?");
chatMemory.add(conversationId, userMessage2);
ChatResponse response2 = chatModel.call(new Prompt(chatMemory.get(conversationId)));
chatMemory.add(conversationId, response2.getResult().getOutput());

// Ответ будет содержать "Джеймс Бонд"
``` 
```
