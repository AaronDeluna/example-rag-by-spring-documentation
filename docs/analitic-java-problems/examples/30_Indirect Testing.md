# Indirect Testing

**Категория:** Запахи тестов

**Описание:** Тест проверяет поведение через побочные эффекты, а не через явные результаты.

**Инструменты:** TestSmellDetector

---

Вот пример для проблемы **Indirect Testing** (косвенное тестирование) на Java.

---

### Корректный пример

Тест напрямую проверяет возвращаемое значение метода, а не побочные эффекты (например, запись в лог или изменение внешнего состояния).

```java
// Класс калькулятора
public class Calculator {
    public int add(int a, int b) {
        return a + b;
    }
}

// Тест (JUnit 5)
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorTest {
    @Test
    public void testAdd() {
        Calculator calc = new Calculator();
        int result = calc.add(2, 3);
        assertEquals(5, result); // проверка явного результата
    }
}
```

**Почему корректно:**  
Тест проверяет **возвращаемое значение** метода `add()`. Это прямое тестирование функциональности без обращения к побочным эффектам.

---

### Некорректный пример

Тест проверяет работу метода через изменение внешнего объекта (например, логирование), а не через возвращаемое значение.

```java
// Класс с логированием
public class CalculatorWithLog {
    private final Logger logger;

    public CalculatorWithLog(Logger logger) {
        this.logger = logger;
    }

    public int add(int a, int b) {
        int result = a + b;
        logger.log("Result: " + result); // побочный эффект
        return result;
    }
}

// Вспомогательный класс для логирования
public class Logger {
    private String lastMessage;

    public void log(String message) {
        this.lastMessage = message;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}

// Тест (JUnit 5)
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CalculatorWithLogTest {
    @Test
    public void testAdd() {
        Logger logger = new Logger();
        CalculatorWithLog calc = new CalculatorWithLog(logger);
        calc.add(2, 3);
        // Косвенная проверка через побочный эффект (логирование)
        assertEquals("Result: 5", logger.getLastMessage());
    }
}
```

**Почему некорректно:**  
- Тест проверяет не возвращаемое значение `add()`, а состояние внешнего объекта `Logger`.  
- Если изменится реализация логирования (например, формат сообщения), тест сломается, хотя математическая логика останется верной.  
- Это **Indirect Testing**: тест зависит от внутренних деталей реализации, а не от контракта метода.

---

### Объяснение и выявление инструментом

**Инструмент:** `TestSmellDetector` (специализированный анализатор тестовых запахов).  
**Правило:** «Indirect Testing» (или «Indirect Test»).  
**Как сработает:**  
Инструмент проанализирует тест и обнаружит, что утверждение (`assertEquals`) проверяет не возвращаемое значение тестируемого метода, а состояние **другого объекта** (`Logger`). Это будет отмечено как косвенная проверка.  
Дополнительно инструмент может выявить, что тест использует `getLastMessage()` — метод, не связанный напрямую с проверяемой функцией.

**Почему это проблема:**  
- Тест хрупкий (ломается при изменении внутренней реализации).  
- Снижает доверие к тестам (может пропустить реальные ошибки в логике).  
- Затрудняет рефакторинг.

**Исправление:**  
Убрать проверку лога и тестировать только возвращаемое значение `add()` (как в корректном примере). Логирование можно протестировать отдельно, если это критично.