# Test Code Duplication

**Категория:** Запахи тестов

**Описание:** Один и тот же код подготовки данных или проверок повторяется в нескольких тестах.

**Инструменты:** DesigniteJava, TestSmellDetector

---

Вот пример, демонстрирующий проблему **Test Code Duplication** (дублирование кода в тестах) и её исправление.

---

### Корректный пример

Используется общий метод `setUp()` и вспомогательные методы для подготовки данных (DRY-принцип).

```java
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserServiceTest {
    
    private UserService userService;
    private User testUser;

    @Before
    public void setUp() {
        userService = new UserService();
        // Подготовка данных вынесена в один метод, избегая дублирования
        testUser = createTestUser("john_doe", "john@example.com");
    }

    @Test
    public void testUserCreation() {
        User created = userService.createUser(testUser);
        assertNotNull(created);
        assertEquals("john_doe", created.getUsername());
    }

    @Test
    public void testUserUpdate() {
        userService.createUser(testUser);
        User updated = userService.updateUser(testUser.getId(), "new_username");
        assertEquals("new_username", updated.getUsername());
    }

    @Test
    public void testUserDeletion() {
        userService.createUser(testUser);
        boolean deleted = userService.deleteUser(testUser.getId());
        assertTrue(deleted);
    }

    // Вспомогательный метод для создания тестового пользователя
    private User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return user;
    }
}

// Вспомогательный класс
class User {
    private int id;
    private String username;
    private String email;
    
    // геттеры и сеттеры (опущены для краткости)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

class UserService {
    public User createUser(User user) { return user; }
    public User updateUser(int id, String username) { return new User(); }
    public boolean deleteUser(int id) { return true; }
}
```

**Почему это корректно:**
- Подготовка данных (`createTestUser`) вынесена в отдельный метод.
- Инициализация объекта `userService` и `testUser` происходит один раз в `@Before`.
- Каждый тест фокусируется только на своей логике, не дублируя boilerplate-код.

---

### Некорректный пример

Дублирование кода подготовки данных и проверок в каждом тесте.

```java
import org.junit.Test;
import static org.junit.Assert.*;

public class UserServiceTestBAD {
    
    @Test
    public void testUserCreation() {
        UserService userService = new UserService();  // Дублирование 1
        User testUser = new User();                   // Дублирование 2
        testUser.setUsername("john_doe");             // Дублирование 3
        testUser.setEmail("john@example.com");        // Дублирование 4
        
        User created = userService.createUser(testUser);
        assertNotNull(created);
        assertEquals("john_doe", created.getUsername());
    }

    @Test
    public void testUserUpdate() {
        UserService userService = new UserService();  // Дублирование 1
        User testUser = new User();                   // Дублирование 2
        testUser.setUsername("john_doe");             // Дублирование 3
        testUser.setEmail("john@example.com");        // Дублирование 4
        
        userService.createUser(testUser);
        User updated = userService.updateUser(testUser.getId(), "new_username");
        assertEquals("new_username", updated.getUsername());
    }

    @Test
    public void testUserDeletion() {
        UserService userService = new UserService();  // Дублирование 1
        User testUser = new User();                   // Дублирование 2
        testUser.setUsername("john_doe");             // Дублирование 3
        testUser.setEmail("john@example.com");        // Дублирование 4
        
        userService.createUser(testUser);
        boolean deleted = userService.deleteUser(testUser.getId());
        assertTrue(deleted);
    }
}
```

---

### Объяснение и выявление инструментом

**Почему это плохо:**
1. **Нарушение DRY (Don't Repeat Yourself):** Строки 6-9, 16-19, 26-29 идентичны. Любое изменение (например, добавление нового поля `User`) потребует правки во всех трёх тестах.
2. **Усложнение поддержки:** При добавлении нового теста разработчик вынужден копировать тот же блок кода, что увеличивает вероятность ошибок.
3. **Снижение читаемости:** Основная логика теста теряется на фоне повторяющегося boilerplate-кода.

**Как инструмент выявит проблему:**
- **DesigniteJava** (или **TestSmellDetector**) обнаружит этот **запах теста "Test Code Duplication"**.
- **Конкретное правило:** Инструмент анализирует AST (абстрактное синтаксическое дерево) и ищет идентичные или похожие блоки кода в разных тестовых методах. Порог срабатывания обычно — более 5-10 строк повторяющегося кода.
- **Пример детекции:** DesigniteJava выдаст предупреждение вида:  
  `"Test Code Duplication detected in methods testUserCreation(), testUserUpdate(), testUserDeletion() - common code block found (lines 6-9, 16-19, 26-29)"`.

**Итог:** Некорректный пример демонстрирует классический случай дублирования тестового кода, который легко устраняется выносом общей логики в `@Before` или вспомогательные методы (как показано в корректном примере).