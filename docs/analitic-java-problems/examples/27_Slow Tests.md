# Slow Tests

**Категория:** Запахи тестов

**Описание:** Тесты выполняются слишком долго (например, используют реальную БД, сеть, `Thread.sleep`).

**Инструменты:** DesigniteJava

---

Вот пример для проблемы **Slow Tests**, оформленный по вашему запросу.

---

### Корректный пример

Используется in-memory база данных (H2) и моки для внешних вызовов, что делает тесты быстрыми и изолированными.

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test") // использует H2 in-memory
public class UserRepositoryFastTest {

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private ExternalPaymentService paymentService;

    @Test
    public void shouldFindUserByName() {
        // given
        User user = new User("john_doe", "john@example.com");
        userRepository.save(user);

        // when
        User found = userRepository.findByName("john_doe");

        // then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    public void shouldReturnTrueWhenPaymentIsValid() {
        // given
        when(paymentService.validate("valid-token")).thenReturn(true);

        // when
        boolean result = paymentService.validate("valid-token");

        // then
        assertThat(result).isTrue();
    }
}
```

**Почему это корректно:**
- Используется `@DataJpaTest` с H2 (in-memory), нет реальной БД.
- Внешний сервис замокан (`@MockBean`), нет сетевых вызовов.
- Нет `Thread.sleep()`, тесты выполняются за миллисекунды.

---

### Некорректный пример

Тест использует реальную PostgreSQL через Docker, реальный HTTP-вызов к платежному шлюзу и содержит `Thread.sleep(5000)`.

```java
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRepositorySlowTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExternalPaymentService paymentService; // реальный HTTP-клиент

    @Test
    public void shouldFindUserByName() throws InterruptedException {
        // given
        User user = new User("john_doe", "john@example.com");
        userRepository.save(user);

        // Имитация долгой валидации через внешний сервис
        Thread.sleep(5000); // ПЛОХО: искусственная задержка

        // Реальный HTTP-вызов к платежному шлюзу
        boolean paymentValid = paymentService.validate("real-token");

        // when
        User found = userRepository.findByName("john_doe");

        // then
        assertThat(found).isNotNull();
        assertThat(paymentValid).isTrue();
    }
}
```

**Почему это плохо:**
1. **Реальная БД** — PostgreSQL через Testcontainers запускается ~5-10 секунд, каждый тест ждет.
2. **Сетевой вызов** — `paymentService.validate("real-token")` обращается к реальному API (latency ~200-500ms + возможные таймауты).
3. **`Thread.sleep(5000)`** — искусственная задержка в 5 секунд, которая не нужна для логики теста.
4. **Всё вместе** — тест может выполняться 15-20 секунд вместо миллисекунд.

---

### Объяснение и выявление инструментом

**Инструмент:** DesigniteJava  
**Проверка (правило):** *Slow Test Detection* (категория *Test Smells*)  
**Как сработает:**

DesigniteJava анализирует тестовые классы и ищет:
- Использование `Thread.sleep()` в тестовых методах.
- Подключение к реальным базам данных (через JDBC-коннекторы, не in-memory).
- Вызовы внешних HTTP-сервисов без моков.
- Длительные операции в `@BeforeEach` / `@BeforeAll` (например, запуск Docker-контейнеров).

В данном некорректном примере инструмент выдаст предупреждение:
```
Test Smell: Slow Test
Location: UserRepositorySlowTest.shouldFindUserByName()
Reason: Contains Thread.sleep(5000) and real database connection (PostgreSQL via Testcontainers)
Recommendation: Replace with in-memory DB and mock external dependencies.
```

**Детектируемые признаки:**
- `Thread.sleep()` — прямой признак искусственной задержки.
- `PostgreSQLContainer` + `@SpringBootTest` — признак использования реальной БД (не in-memory).
- Отсутствие `@MockBean` для `ExternalPaymentService` — признак реального сетевого вызова.

**Итог:** DesigniteJava маркирует такой тест как **Slow Test**, указывая точные строки кода и причину (реальная БД, сеть, sleep).