# Eager Test

**Категория:** Запахи тестов

**Описание:** Тест проверяет сразу множество различных условий, нарушая принцип единой ответственности в тесте.

**Инструменты:** DesigniteJava, TestSmellDetector

---

Вот пример для проблемы **Eager Test** (нетерпеливый тест), когда один тест проверяет слишком много разных аспектов работы класса.

---

### Корректный пример

Каждый тест проверяет только одну конкретную операцию или состояние. Используется один assert на тест (или логически связанная группа assert'ов, относящаяся к одному условию).

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final UserService service = new UserService();

    @Test
    void shouldReturnTrueWhenUserIsAdult() {
        User user = new User(25);
        assertTrue(service.isAdult(user));
    }

    @Test
    void shouldReturnFalseWhenUserIsUnderage() {
        User user = new User(16);
        assertFalse(service.isAdult(user));
    }

    @Test
    void shouldActivateUser() {
        User user = new User(30);
        service.activate(user);
        assertTrue(user.isActive());
    }

    @Test
    void shouldDeactivateUser() {
        User user = new User(30);
        service.activate(user);
        service.deactivate(user);
        assertFalse(user.isActive());
    }

    // Вспомогательные классы для самодостаточности
    static class User {
        private final int age;
        private boolean active;

        User(int age) { this.age = age; }
        int getAge() { return age; }
        boolean isActive() { return active; }
        void setActive(boolean active) { this.active = active; }
    }

    static class UserService {
        boolean isAdult(User user) { return user.getAge() >= 18; }
        void activate(User user) { user.setActive(true); }
        void deactivate(User user) { user.setActive(false); }
    }
}
```

---

### Некорректный пример

Один тест проверяет сразу несколько разных функций: проверку возраста, активацию, деактивацию и даже граничные значения. Всё в одном методе.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserServiceEagerTest {

    private final UserService service = new UserService();

    @Test
    void testAllUserOperations() {
        // Проверяем isAdult для 3 разных значений
        User youngUser = new User(16);
        assertFalse(service.isAdult(youngUser));

        User adultUser = new User(18);
        assertTrue(service.isAdult(adultUser));

        User oldUser = new User(65);
        assertTrue(service.isAdult(oldUser));

        // Тут же проверяем активацию
        service.activate(adultUser);
        assertTrue(adultUser.isActive());

        // И деактивацию
        service.deactivate(adultUser);
        assertFalse(adultUser.isActive());

        // И ещё проверяем, что активация не влияет на возраст
        assertTrue(service.isAdult(adultUser));
    }

    // Вспомогательные классы (те же, что в корректном примере)
    static class User {
        private final int age;
        private boolean active;

        User(int age) { this.age = age; }
        int getAge() { return age; }
        boolean isActive() { return active; }
        void setActive(boolean active) { this.active = active; }
    }

    static class UserService {
        boolean isAdult(User user) { return user.getAge() >= 18; }
        void activate(User user) { user.setActive(true); }
        void deactivate(User user) { user.setActive(false); }
    }
}
```

---

### Объяснение и выявление инструментом

**Почему некорректный пример плох:**
1. **Нарушение единой ответственности** – тест `testAllUserOperations` проверяет 4 разных аспекта: возрастные границы (16, 18, 65), активацию, деактивацию и неизменность возраста после активации.
2. **Плохая диагностика при падении** – если упадет первый `assertFalse`, мы не узнаем, сломалась ли логика `isAdult` или тест просто неправильно настроен. Если упадет последний `assertTrue`, непонятно, это проблема `isAdult` или побочный эффект от `deactivate`.
3. **Сложность поддержки** – при изменении логики активации придется переписывать весь тест, хотя изменения могли касаться только одного метода.
4. **Нарушение принципа F.I.R.S.T.** – тест не является `Focused` (сфокусированным).

**Как инструмент выявит проблему:**

| Инструмент | Правило/Детектор | Как сработает |
|-----------|------------------|---------------|
| **DesigniteJava** | `EagerTest` в категории Test Smells | Обнаружит тест, содержащий больше одного логического утверждения (assert) или больше одной операции над объектом без пересоздания состояния. |
| **TestSmellDetector** | `Eager Test` | Анализирует количество assert'ов и количество вызовов методов тестируемого класса. Если в одном тесте >2 assert'ов, относящихся к разным методам или разным состояниям, помечает как Eager Test. |

**Конкретное срабатывание для нашего примера:**
- TestSmellDetector увидит 6 assert'ов, проверяющих 3 разных метода (`isAdult`, `activate`, `deactivate`), и выдаст предупреждение: *"Test 'testAllUserOperations' is an Eager Test: it tests multiple behaviors (age validation, activation, deactivation) in a single test method. Consider splitting into focused tests."*
- DesigniteJava в своём отчёте покажет: *"Class 'UserServiceEagerTest' contains Eager Test smell in method 'testAllUserOperations'."*