# Redundant Assertion

**Категория:** Запахи тестов

**Описание:** Тест содержит проверки, которые всегда истинны (например, проверка того же значения, которое только что было установлено).

**Инструменты:** TestSmellDetector

---

Вот пример для проблемы **Redundant Assertion** (Избыточное утверждение) в тестах Java.

---

### Корректный пример

В этом примере тест проверяет, что метод `setName` действительно изменяет поле, но не дублирует уже известное значение. Утверждение осмысленно и проверяет результат работы метода, а не тривиальное присваивание.

```java
public class User {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

// Корректный тест
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testSetNameUpdatesCorrectly() {
        User user = new User();
        String expectedName = "Alice";
        
        user.setName(expectedName);
        
        // Проверяем, что имя действительно изменилось на ожидаемое
        assertEquals(expectedName, user.getName());
    }
}
```

**Почему корректно:**  
- Утверждение `assertEquals(expectedName, user.getName())` проверяет **результат вызова метода**, а не константу или только что присвоенное значение.  
- Тест имеет смысл: он верифицирует, что `setName` работает правильно (не игнорирует вызов, не сохраняет другое значение и т.д.).

---

### Некорректный пример

Здесь тест содержит избыточное утверждение, которое всегда истинно, так как проверяет значение, которое только что было установлено в той же строке.

```java
public class User {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

// Некорректный тест
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testRedundantAssertion() {
        User user = new User();
        String expectedName = "Bob";
        
        user.setName(expectedName);
        
        // Избыточная проверка: мы только что присвоили expectedName переменной,
        // и утверждение всегда будет истинным (оно проверяет саму константу).
        assertEquals(expectedName, expectedName);  // <-- Проблема!
        
        // Другая форма той же проблемы:
        assertEquals(user.getName(), user.getName());  // <-- Всегда true
    }
}
```

**Почему некорректно:**  
- Первое утверждение `assertEquals(expectedName, expectedName)` сравнивает переменную саму с собой. Это бессмысленно, так как результат всегда `true`.  
- Второе утверждение `assertEquals(user.getName(), user.getName())` также бессмысленно: оно проверяет, что значение, полученное дважды из одного и того же вызова, равно самому себе.  
- Такие проверки не тестируют логику приложения, а только загромождают код и могут скрывать реальные ошибки (например, если `getName()` возвращает `null`, тест всё равно пройдёт).  
- Это классический **Redundant Assertion** — запах теста, когда утверждение не добавляет ценности.

---

### Объяснение и выявление инструментом

**Почему это плохо:**  
- Тест теряет диагностическую ценность — он не проверяет поведение системы.  
- Создаётся ложное чувство уверенности в покрытии кода.  
- Увеличивается время выполнения тестов без пользы.  
- В больших проектах такие утверждения маскируют настоящие баги.

**Как инструмент TestSmellDetector обнаружит проблему:**  
- **Правило/детектор:** `RedundantAssertionDetector` (или аналогичное в TestSmellDetector).  
- **Механизм:** Инструмент анализирует AST (абстрактное синтаксическое дерево) тестового кода и ищет утверждения, где оба аргумента являются одним и тем же идентификатором или выражением (например, `assertEquals(a, a)` или `assertEquals(obj.method(), obj.method())`).  
- **Срабатывание:** Для некорректного примера выше инструмент выдаст предупреждение:  
  > "Redundant Assertion: Assertion always passes because both arguments are the same expression (`expectedName` vs `expectedName`). Consider removing or replacing with a meaningful check."

**Дополнительно:**  
- TestSmellDetector также может обнаружить эту проблему на уровне констант (например, `assertEquals(42, 42)`) или при сравнении объекта с самим собой через `assertSame`.  
- В реальных проектах такие запахи часто возникают при рефакторинге или копировании кода, когда разработчик забывает заменить второй аргумент на актуальное значение.