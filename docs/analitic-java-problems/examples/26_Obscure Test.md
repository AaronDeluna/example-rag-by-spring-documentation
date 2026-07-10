# Obscure Test

**Категория:** Запахи тестов

**Описание:** Тест сложно понять из-за плохого именования, избыточной подготовки или отсутствия комментариев.

**Инструменты:** DesigniteJava

---

Вот пример для проблемы **Obscure Test** (непонятный тест) с использованием Java и JUnit.

---

### Корректный пример

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Test
    void shouldReturnDiscountedPriceForLoyalCustomer() {
        // Arrange
        double basePrice = 100.0;
        boolean isLoyalCustomer = true;
        OrderService orderService = new OrderService();

        // Act
        double result = orderService.calculatePrice(basePrice, isLoyalCustomer);

        // Assert
        assertEquals(90.0, result, 0.001, "Цена для постоянного клиента должна быть со скидкой 10%");
    }

    @Test
    void shouldReturnFullPriceForNewCustomer() {
        // Arrange
        double basePrice = 100.0;
        boolean isLoyalCustomer = false;
        OrderService orderService = new OrderService();

        // Act
        double result = orderService.calculatePrice(basePrice, isLoyalCustomer);

        // Assert
        assertEquals(100.0, result, 0.001, "Новый клиент платит полную цену");
    }
}

class OrderService {
    public double calculatePrice(double basePrice, boolean isLoyalCustomer) {
        if (isLoyalCustomer) {
            return basePrice * 0.9; // 10% discount
        }
        return basePrice;
    }
}
```

**Почему корректно:**
- Имена тестов читаемые и описывают сценарий.
- Используется AAA (Arrange-Act-Assert) с явными секциями.
- Понятные имена переменных (`basePrice`, `isLoyalCustomer`).
- Есть сообщение в assert для пояснения.
- Минимум магии — сразу видно, что тестируется.

---

### Некорректный пример

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Test1 {

    @Test
    void test1() {
        // подготовка
        OrderService s = new OrderService();
        double a = 100.0;
        boolean b = true;
        double c = s.calc(a, b);
        // проверка
        assertEquals(90.0, c);
    }

    @Test
    void test2() {
        OrderService s = new OrderService();
        double x = 100.0;
        boolean y = false;
        double z = s.calc(x, y);
        assertEquals(100.0, z);
    }
}

class OrderService {
    public double calc(double p, boolean f) {
        if (f) {
            return p * 0.9;
        }
        return p;
    }
}
```

**Почему некорректно (признаки Obscure Test):**
1. **Плохое именование:**  
   - `Test1` — ничего не говорит.  
   - `test1`, `test2` — неясно, что тестируется.  
   - `s`, `a`, `b`, `c`, `x`, `y`, `z` — однобуквенные имена, скрывающие смысл.  
2. **Избыточная/непонятная подготовка:**  
   - Нет комментариев, что означают `b = true` или `y = false`.  
   - Непонятно, что такое `f` в методе `calc`.  
3. **Отсутствие пояснений:**  
   - Нет сообщения в `assertEquals`, нет AAA-структуры.  
   - Тесты не показывают, какой сценарий проверяется (скидка для лояльного клиента или полная цена для нового).  

**Как это выявит DesigniteJava:**
- **Правило:** `Obscure Test` (категория «Test Smells»).  
- **Конкретная проверка:**  
  - Анализирует имена тестовых методов — если они не содержат слов-описаний (например, `should*`, `test*` с осмысленным окончанием) или используют `test1`, `test2`, то выдаёт предупреждение.  
  - Также проверяет наличие «магических чисел» и однобуквенных переменных в тестах — это считается признаком нечитаемости.  
- **Вывод DesigniteJava:**  
  > `Test method 'test1' has a non-descriptive name. Consider renaming it to clearly indicate the scenario being tested.`  
  > `Test method 'test2' contains obscure variable names ('x', 'y', 'z') that reduce readability.`

---

### Объяснение и выявление инструментом

**Проблема:** Тесты непонятны другому разработчику (или тому же через месяц).  
**Инструмент:** **DesigniteJava** (статический анализатор кода).  
**Как сработает:**  
- Для каждого тестового метода проверяется, что его имя содержит хотя бы одно слово, описывающее ожидаемое поведение (например, `shouldReturn`, `whenConditionThenResult`).  
- Анализируются имена локальных переменных в тестах — если длина имени < 2 символов и не является общепринятой (i, j для циклов), выдаётся предупреждение.  
- Проверяется наличие комментариев или AAA-секций — их отсутствие ухудшает оценку читаемости теста.  

**Рекомендация:**  
- Всегда называйте тесты по шаблону `should[ExpectedBehavior]When[Condition]`.  
- Используйте осмысленные имена переменных (`basePrice`, `expectedPrice`, `actualPrice`).  
- Разделяйте AAA-секции комментариями или пустыми строками.