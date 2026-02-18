# MCP Security

> **Примечание:** Это все еще в процессе разработки. Документация и API могут измениться в будущих релизах.

Модуль безопасности Spring AI MCP предоставляет комплексную поддержку безопасности на основе OAuth 2.0 и API-ключей для реализаций Протокола Контекста Модели в Spring AI. Этот проект, управляемый сообществом, позволяет разработчикам защищать как серверы, так и клиенты MCP с помощью стандартных механизмов аутентификации и авторизации.

> **Примечание:** Этот модуль является частью проекта [spring-ai-community/mcp-security](https://github.com/spring-ai-community/mcp-security) и в настоящее время работает только с веткой 1.1.x Spring AI. Этот проект управляется сообществом и пока не имеет официальной поддержки от Spring AI или проекта MCP.

## Обзор

Модуль безопасности MCP предоставляет три основных компонента:

- **Безопасность сервера MCP** - ресурсный сервер OAuth 2.0 и аутентификация на основе API-ключей для серверов MCP Spring AI
- **Безопасность клиента MCP** - поддержка клиента OAuth 2.0 для клиентов MCP Spring AI
- **Сервер авторизации MCP** - улучшенный сервер авторизации Spring с функциями, специфичными для MCP

Проект позволяет разработчикам:

- Защищать серверы MCP с помощью аутентификации OAuth 2.0 и доступа на основе API-ключей
- Конфигурировать клиентов MCP с помощью потоков авторизации OAuth 2.0
- Настраивать серверы авторизации, специально разработанные для рабочих процессов MCP
- Реализовывать детализированный контроль доступа к инструментам и ресурсам MCP

## Безопасность сервера MCP

Модуль безопасности сервера MCP предоставляет возможности ресурсного сервера OAuth 2.0 для xref:api/mcp/mcp-server-boot-starter-docs.adoc[серверов MCP Spring AI]. 
Он также предоставляет базовую поддержку аутентификации на основе API-ключей.

> **Важно:** Этот модуль совместим только с серверами на основе Spring WebMVC.

### Зависимости

Добавьте следующие зависимости в ваш проект:

[tabs]
======
Maven::
+
```xml
<dependencies>
    <dependency>
        <groupId>org.springaicommunity</groupId>
        <artifactId>mcp-server-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OPTIONAL: Для поддержки OAuth2 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>
</dependencies>
```

Gradle::
+
```groovy
implementation 'org.springaicommunity:mcp-server-security'
implementation 'org.springframework.boot:spring-boot-starter-security'

// OPTIONAL: Для поддержки OAuth2
implementation 'org.springframework.boot:spring-boot-starter-oauth2-resource-server'
```
======

### Конфигурация OAuth 2.0

#### Основная настройка OAuth 2.0Первым делом, включите сервер MCP в вашем `application.properties`:

```properties
spring.ai.mcp.server.name=my-cool-mcp-server
# Поддерживаемые протоколы: STREAMABLE, STATELESS
spring.ai.mcp.server.protocol=STREAMABLE
```

Затем настройте безопасность, используя стандартные API Spring Security с предоставленным конфигуратором MCP:

```java
@Configuration
@EnableWebSecurity
class McpServerConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Обязательная аутентификация с токеном на КАЖДОМ запросе
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                // Настройка OAuth2 на сервере MCP
                .with(
                        McpServerOAuth2Configurer.mcpServerOAuth2(),
                        (mcpAuthorization) -> {
                            // ОБЯЗАТЕЛЬНО: issuerURI
                            mcpAuthorization.authorizationServer(issuerUrl);
                            // НЕОБЯЗАТЕЛЬНО: обеспечить проверку `aud` в JWT токене.
                            // Не все серверы авторизации поддерживают индикаторы ресурсов,
                            // поэтому он может отсутствовать. По умолчанию `false`.
                            // См. RFC 8707 Индикаторы ресурсов для OAuth 2.0
                            // https://www.rfc-editor.org/rfc/rfc8707.html
                            mcpAuthorization.validateAudienceClaim(true);
                        }
                )
                .build();
    }
}
```

#### Защита вызовов инструментов толькоВы можете настроить сервер так, чтобы он защищал только вызовы инструментов, оставляя другие операции MCP (такие как `initialize` и `tools/list`) открытыми:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Включить безопасность на основе аннотаций
class McpServerConfiguration {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUrl;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Открыть каждый запрос на сервере
                .authorizeHttpRequests(auth -> {
                    auth.requestMatcher("/mcp").permitAll();
                    auth.anyRequest().authenticated();
                })
                // Настроить OAuth2 на сервере MCP
                .with(
                        McpResourceServerConfigurer.mcpServerOAuth2(),
                        (mcpAuthorization) -> {
                            // ОБЯЗАТЕЛЬНО: issuerURI
                            mcpAuthorization.authorizationServer(issuerUrl);
                        }
                )
                .build();
    }
}
```

Затем защитите ваши вызовы инструментов, используя аннотацию `@PreAuthorize` с [методической безопасностью](https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html):

```java
@Service
public class MyToolsService {

    @PreAuthorize("isAuthenticated()")
    @McpTool(name = "greeter", description = "Инструмент, который приветствует вас на выбранном языке")
    public String greet(
            @ToolParam(description = "Язык для приветствия (например: английский, французский, ...)") String language
    ) {
        if (!StringUtils.hasText(language)) {
            language = "";
        }
        return switch (language.toLowerCase()) {
            case "english" -> "Hello you!";
            case "french" -> "Salut toi!";
            default -> "Я не понимаю язык \"%s\". Поэтому я просто скажу Hello!".formatted(language);
        };
    }
}
```

Вы также можете получить доступ к текущей аутентификации непосредственно из метода инструмента, используя `SecurityContextHolder`:

```java
@McpTool(name = "greeter", description = "Инструмент, который приветствует пользователя по имени на выбранном языке")
@PreAuthorize("isAuthenticated()")
public String greet(
        @ToolParam(description = "Язык для приветствия (например: английский, французский, ...)") String language
) {
    if (!StringUtils.hasText(language)) {
        language = "";
    }
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    var name = authentication.getName();
    return switch (language.toLowerCase()) {
        case "english" -> "Hello, %s!".formatted(name);
        case "french" -> "Salut %s!".formatted(name);
        default -> ("Я не понимаю язык \"%s\". " +
                    "Поэтому я просто скажу Hello %s!").formatted(language, name);
    };
}
```

### Аутентификация по API-ключу```markdown
Модуль безопасности сервера MCP также поддерживает аутентификацию на основе API-ключей. Вам необходимо предоставить собственную реализацию `ApiKeyEntityRepository` для хранения объектов `ApiKeyEntity`.

Пример реализации доступен с `InMemoryApiKeyEntityRepository` вместе с стандартным `ApiKeyEntityImpl`:

> **Внимание:** `InMemoryApiKeyEntityRepository` использует bcrypt для хранения API-ключей, что является ресурсоемким процессом. Он не подходит для использования в условиях высокой нагрузки в производственной среде. Для производства реализуйте собственный `ApiKeyEntityRepository`.

```java
@Configuration
@EnableWebSecurity
class McpServerConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(authz -> authz.anyRequest().authenticated())
                .with(
                        mcpServerApiKey(),
                        (apiKey) -> {
                            // ОБЯЗАТЕЛЬНО: репозиторий для API-ключей
                            apiKey.apiKeyRepository(apiKeyRepository());

                            // НЕОБЯЗАТЕЛЬНО: имя заголовка, содержащего API-ключ.
                            // Здесь, например, API-ключи будут отправляться с "CUSTOM-API-KEY: <value>"
                            // Заменяет .authenticationConverter(...) (см. ниже)
                            //
                            // apiKey.headerName("CUSTOM-API-KEY");

                            // НЕОБЯЗАТЕЛЬНО: пользовательский конвертер для преобразования http-запроса
                            // в объект аутентификации. Полезно, когда заголовок
                            // "Authorization: Bearer <value>".
                            // Заменяет .headerName(...) (см. выше)
                            //
                            // apiKey.authenticationConverter(request -> {
                            //     var key = extractKey(request);
                            //     return ApiKeyAuthenticationToken.unauthenticated(key);
                            // });
                        }
                )
                .build();
    }

    /**
     * Предоставьте репозиторий {@link ApiKeyEntity}.
     */
    private ApiKeyEntityRepository<ApiKeyEntityImpl> apiKeyRepository() {
        var apiKey = ApiKeyEntityImpl.builder()
                .name("тестовый API-ключ")
                .id("api01")
                .secret("mycustomapikey")
                .build();

        return new InMemoryApiKeyEntityRepository<>(List.of(apiKey));
    }
}
```

С этой конфигурацией вы можете вызывать ваш сервер MCP с заголовком `X-API-key: api01.mycustomapikey`.

### Известные ограничения

[ВАЖНО]
====

- Устаревший транспорт SSE не поддерживается. Используйте xref:api/mcp/mcp-streamable-http-server-boot-starter-docs.adoc[Streamable HTTP] или xref:api/mcp/mcp-stateless-server-boot-starter-docs.adoc[бессостояние].
- Серверы на основе WebFlux не поддерживаются.
- Непрозрачные токены не поддерживаются. Используйте JWT.

====

## Безопасность клиента MCP

Модуль безопасности клиента MCP предоставляет поддержку OAuth 2.0 для xref:api/mcp/mcp-client-boot-starter-docs.adoc[клиентов MCP от Spring AI], поддерживая как клиентов на основе HttpClient (из `spring-ai-starter-mcp-client`), так и клиентов на основе WebClient (из `spring-ai-starter-mcp-client-webflux`).

> **Важно:** Этот модуль поддерживает только `McpSyncClient`.

### Зависимости

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-client-security</artifactId>
</dependency>
```

Gradle::
+
```groovy
implementation 'org.springaicommunity:mcp-client-security'
```
======

### Потоки авторизации
```Три потока OAuth 2.0 доступны для получения токенов:

- **Authorization Code Flow** - Для разрешений на уровне пользователя, когда каждый запрос MCP выполняется в контексте запроса пользователя
- **Client Credentials Flow** - Для случаев использования "машина-машина", когда человек не участвует в процессе
- **Hybrid Flow** - Сочетает оба потока для сценариев, когда некоторые операции (например, `initialize` или `tools/list`) происходят без присутствия пользователя, но вызовы инструментов требуют разрешений на уровне пользователя

> **Совет:** Используйте поток авторизации, когда у вас есть разрешения на уровне пользователя и все запросы MCP происходят в контексте пользователя. Используйте клиентские учетные данные для связи "машина-машина". Используйте гибридный поток, когда используете свойства Spring Boot для конфигурации клиента MCP, так как обнаружение инструментов происходит при запуске без присутствия пользователя.

### Общая настройка

Для всех потоков активируйте поддержку клиента OAuth2 в вашем `application.properties`:

```properties
# Убедитесь, что клиенты MCP синхронизированы
spring.ai.mcp.client.type=SYNC

# Для потока authorization_code или гибридного потока
spring.security.oauth2.client.registration.authserver.client-id=<THE CLIENT ID>
spring.security.oauth2.client.registration.authserver.client-secret=<THE CLIENT SECRET>
spring.security.oauth2.client.registration.authserver.authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.authserver.provider=authserver

# Для потока client_credentials или гибридного потока
spring.security.oauth2.client.registration.authserver-client-credentials.client-id=<THE CLIENT ID>
spring.security.oauth2.client.registration.authserver-client-credentials.client-secret=<THE CLIENT SECRET>
spring.security.oauth2.client.registration.authserver-client-credentials.authorization-grant-type=client_credentials
spring.security.oauth2.client.registration.authserver-client-credentials.provider=authserver

# Конфигурация сервера авторизации
spring.security.oauth2.client.provider.authserver.issuer-uri=<THE ISSUER URI OF YOUR AUTH SERVER>
```

Затем создайте класс конфигурации, активирующий возможности клиента OAuth2:

```java
@Configuration
@EnableWebSecurity
class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // в этом примере клиентское приложение не имеет безопасности на своих конечных точках
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // включить поддержку OAuth2
                .oauth2Client(Customizer.withDefaults())
                .build();
    }
}
```

### Клиенты на основе HttpClient

При использовании `spring-ai-starter-mcp-client` настройте бин `McpSyncHttpClientRequestCustomizer`:

```java
@Configuration
class McpConfiguration {

    @Bean
    McpSyncClientCustomizer syncClientCustomizer() {
        return (name, syncSpec) ->
                syncSpec.transportContextProvider(
                        new AuthenticationMcpTransportContextProvider()
                );
    }

    @Bean
    McpSyncHttpClientRequestCustomizer requestCustomizer(
            OAuth2AuthorizedClientManager clientManager
    ) {
        // Имя clientRegistration, "authserver",
        // должно совпадать с именем в application.properties
        return new OAuth2AuthorizationCodeSyncHttpRequestCustomizer(
                clientManager,
                "authserver"
        );
    }
}
```

Доступные кастомизаторы:

- `OAuth2AuthorizationCodeSyncHttpRequestCustomizer` - Для потока авторизации
- `OAuth2ClientCredentialsSyncHttpRequestCustomizer` - Для потока клиентских учетных данных
- `OAuth2HybridSyncHttpRequestCustomizer` - Для гибридного потока

### Клиенты на основе WebClient```markdown
При использовании `spring-ai-starter-mcp-client-webflux` настройте `WebClient.Builder` с MCP `ExchangeFilterFunction`:

```java
@Configuration
class McpConfiguration {

    @Bean
    McpSyncClientCustomizer syncClientCustomizer() {
        return (name, syncSpec) ->
                syncSpec.transportContextProvider(
                        new AuthenticationMcpTransportContextProvider()
                );
    }

    @Bean
    WebClient.Builder mcpWebClientBuilder(OAuth2AuthorizedClientManager clientManager) {
        // Имя clientRegistration, "authserver", должно совпадать с именем в application.properties
        return WebClient.builder().filter(
                new McpOAuth2AuthorizationCodeExchangeFilterFunction(
                        clientManager,
                        "authserver"
                )
        );
    }
}
```

Доступные функции фильтров:

- `McpOAuth2AuthorizationCodeExchangeFilterFunction` - Для потока авторизации по коду
- `McpOAuth2ClientCredentialsExchangeFilterFunction` - Для потока клиентских учетных данных
- `McpOAuth2HybridExchangeFilterFunction` - Для гибридного потока

### Обход автоконфигурации Spring AI

Автоконфигурация Spring AI инициализирует MCP-клиентов при запуске, что может вызвать проблемы с аутентификацией на основе пользователя. Чтобы избежать этого:

#### Вариант 1: Отключить автоконфигурацию @Tool

Отключите автоконфигурацию `@Tool` Spring AI, опубликовав пустой бин `ToolCallbackResolver`:

```java
@Configuration
public class McpConfiguration {

    @Bean
    ToolCallbackResolver resolver() {
        return new StaticToolCallbackResolver(List.of());
    }
}
```

#### Вариант 2: Программная конфигурация клиента

Настройте MCP-клиентов программно, а не с помощью свойств Spring Boot. Для клиентов на основе HttpClient:

```java
@Bean
McpSyncClient client(
        ObjectMapper objectMapper,
        McpSyncHttpClientRequestCustomizer requestCustomizer,
        McpClientCommonProperties commonProps
) {
    var transport = HttpClientStreamableHttpTransport.builder(mcpServerUrl)
            .clientBuilder(HttpClient.newBuilder())
            .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
            .httpRequestCustomizer(requestCustomizer)
            .build();

    var clientInfo = new McpSchema.Implementation("client-name", commonProps.getVersion());

    return McpClient.sync(transport)
            .clientInfo(clientInfo)
            .requestTimeout(commonProps.getRequestTimeout())
            .transportContextProvider(new AuthenticationMcpTransportContextProvider())
            .build();
}
```

Для клиентов на основе WebClient:

```java
@Bean
McpSyncClient client(
        WebClient.Builder mcpWebClientBuilder,
        ObjectMapper objectMapper,
        McpClientCommonProperties commonProperties
) {
    var builder = mcpWebClientBuilder.baseUrl(mcpServerUrl);
    var transport = WebClientStreamableHttpTransport.builder(builder)
            .jsonMapper(new JacksonMcpJsonMapper(objectMapper))
            .build();

    var clientInfo = new McpSchema.Implementation("clientName", commonProperties.getVersion());

    return McpClient.sync(transport)
            .clientInfo(clientInfo)
            .requestTimeout(commonProperties.getRequestTimeout())
            .transportContextProvider(new AuthenticationMcpTransportContextProvider())
            .build();
}
```

Затем добавьте клиента в ваш чат-клиент:

```java
var chatResponse = chatClient.prompt("Попросите LLM сделать это")
        .toolCallbacks(new SyncMcpToolCallbackProvider(mcpClient1, mcpClient2, mcpClient3))
        .call()
        .content();
```

### Известные ограничения

[ВАЖНО]
====

- Серверы Spring WebFlux не поддерживаются.
- Автоконфигурация Spring AI инициализирует MCP-клиентов при запуске приложения, что требует обходных путей для аутентификации на основе пользователя.
- В отличие от серверного модуля, реализация клиента поддерживает транспорт SSE как с `HttpClient`, так и с `WebClient`.

====
## MCP Сервер авторизации
``````
Модуль MCP Authorization Server расширяет [OAuth 2.0 Authorization Server от Spring Security](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/authorization-server/index.html) функциями, относящимися к [спецификации авторизации MCP](https://modelcontextprotocol.io/specification/2025-06-18/basic/authorization), такими как динамическая регистрация клиентов и индикаторы ресурсов.

### Зависимости

[tabs]
======
Maven::
+
```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-authorization-server</artifactId>
</dependency>
```

Gradle::
+
```groovy
implementation 'org.springaicommunity:mcp-authorization-server'
```
======

### Конфигурация

Настройте сервер авторизации в вашем `application.yml`:

```yaml
spring:
  application:
    name: sample-authorization-server
  security:
    oauth2:
      authorizationserver:
        client:
          default-client:
            token:
              access-token-time-to-live: 1h
            registration:
              client-id: "default-client"
              client-secret: "{noop}default-secret"
              client-authentication-methods:
                - "client_secret_basic"
                - "none"
              authorization-grant-types:
                - "authorization_code"
                - "client_credentials"
              redirect-uris:
                - "http://127.0.0.1:8080/authorize/oauth2/code/authserver"
                - "http://localhost:8080/authorize/oauth2/code/authserver"
                # mcp-inspector
                - "http://localhost:6274/oauth/callback"
                # claude code
                - "https://claude.ai/api/mcp/auth_callback"
    user:
      # Один пользователь с именем "user"
      name: user
      password: password

server:
  servlet:
    session:
      cookie:
        # Переопределите имя cookie по умолчанию (JSESSIONID).
        # Это позволяет запускать несколько приложений Spring на localhost, и у каждого будет своя cookie.
        # В противном случае, поскольку cookies не учитывают порт, они будут путаться.
        name: MCP_AUTHORIZATION_SERVER_SESSIONID
```

Затем активируйте возможности сервера авторизации с помощью цепочки фильтров безопасности:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            // все запросы должны быть аутентифицированы
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            // включить настройки сервера авторизации
            .with(McpAuthorizationServerConfigurer.mcpAuthorizationServer(), withDefaults())
            // включить форму входа, для пользователя "user"/"password"
            .formLogin(withDefaults())
            .build();
}
```

### Известные ограничения

[ВАЖНО]
====

- Серверы Spring WebFlux не поддерживаются.
- Каждый клиент поддерживает ВСЕ идентификаторы `resource`.

====

## Примеры и интеграции

Директория [samples](https://github.com/spring-ai-community/mcp-security/tree/main/samples) содержит рабочие примеры для всех модулей в этом проекте, включая интеграционные тесты.

С помощью `mcp-server-security` и поддерживающего `mcp-authorization-server` вы можете интегрироваться с:

- Cursor
- Claude Desktop
- [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector)

> **Примечание:** При использовании [MCP Inspector](https://modelcontextprotocol.io/docs/tools/inspector) вам может потребоваться отключить защиту CSRF и CORS.

## Дополнительные ресурсы
```- [Спецификация авторизации MCP](https://modelcontextprotocol.io/specification/2025-06-18/basic/authorization#communication-security)
- [Репозиторий MCP Security на GitHub](https://github.com/spring-ai-community/mcp-security)
- [Пример приложений](https://github.com/spring-ai-community/mcp-security/tree/main/samples)
- [Спецификация авторизации MCP](https://modelcontextprotocol.io/specification/2025-06-18/basic/authorization)
- [Сервер ресурсов Spring Security OAuth 2.0](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Клиент Spring Security OAuth 2.0](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
- [Сервер авторизации Spring](https://docs.spring.io/spring-security/reference/7.0/servlet/oauth2/authorization-server/index.html)
