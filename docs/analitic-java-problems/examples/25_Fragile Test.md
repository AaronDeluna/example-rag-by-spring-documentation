# Fragile Test

**Категория:** Запахи тестов

**Описание:** Тест ломается при изменениях, не связанных с тестируемой логикой (например, при изменении имени метода).

**Инструменты:** DesigniteJava

---

Вот пример для проблемы **Fragile Test** (Хрупкий тест).

---

### Корректный пример

Тест проверяет поведение через публичный контракт класса, не привязываясь к внутренней реализации (именам методов, полям).

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

public class CalculatorTest {
    private final Calculator calculator = new Calculator();

    @Test
    void shouldReturnSumOfTwoNumbers() {
        // Проверяем только результат, а не способ его получения
        int result = calculator.add(2, 3);
        assertEquals(5, result, "2 + 3 должно быть равно 5");
    }
}
```

**Почему это корректно:**
- Тест проверяет **конечный результат** работы метода.
- Даже если внутренняя реализация `add` изменится (например, метод переименуют в `sum`, но оставят старый `add` для обратной совместимости), тест не сломается.
- Тест не зависит от приватных полей, вспомогательных методов или порядка вызовов внутри `Calculator`.

---

### Некорректный пример

Тест жёстко привязан к имени метода, которое может измениться при рефакторинге.

```java
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;

public class FragileCalculatorTest {
    @Test
    void testAdditionByReflection() throws Exception {
        // ПЛОХО: используем рефлексию для вызова метода по строковому имени
        Class<?> clazz = Class.forName("Calculator");
        Object instance = clazz.getDeclaredConstructor().newInstance();
        
        Method method = clazz.getMethod("add", int.class, int.class);
        int result = (int) method.invoke(instance, 2, 3);
        
        assertEquals(5, result);
    }
}
```

**Дополнительный хрупкий вариант (без рефлексии, но с Mockito):**

```java
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

class UserService {
    public String getUserName(int id) {
        return "John"; // реальная логика
    }
}

public class FragileMockTest {
    @Test
    void testUserName() {
        UserService mockService = mock(UserService.class);
        // ПЛОХО: привязка к конкретному методу через when().thenReturn()
        when(mockService.getUserName(1)).thenReturn("MockedJohn");
        
        String name = mockService.getUserName(1);
        assertEquals("MockedJohn", name);
    }
}
```

---

### Объяснение и выявление инструментом

**Почему некорректные примеры плохи:**

1. **Рефлексивный тест** — жёстко завязан на строку `"add"`. Если метод переименуют в `sum`, тест упадёт с `NoSuchMethodException`, хотя логика `Calculator` не изменилась.
2. **Mock-тест** — при изменении имени метода `getUserName` на `fetchUserName` тест перестанет компилироваться или упадёт в рантайме, хотя поведение системы осталось прежним.

**Какой инструмент обнаружит:** **DesigniteJava**

**Правило/проверка, которая сработает:**
- Для рефлексивного теста: **`Fragile Test`** (категория *Test Smell*). DesigniteJava анализирует тесты на использование рефлексии (`getMethod`, `invoke`), что является явным признаком хрупкости.
- Для mock-теста: **`GeneralFixture`** или **`Indirect Testing`**. Инструмент заметит, что тест проверяет не реальное поведение, а поведение мока, и изменение имени метода в production-классе приведёт к ошибке компиляции теста.

**Как именно сработает:**
DesigniteJava сканирует тестовые классы и находит:
- Вызовы `Class.getMethod(String name, ...)` — это прямой признак хрупкости из-за привязки к строковому имени.
- Использование `when(mock.methodName(...))` без проверки реального вызова — инструмент маркирует как потенциально хрупкий тест, который сломается при рефакторинге production-кода.