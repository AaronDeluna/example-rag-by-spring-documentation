# Conditional Test Logic

**Категория:** Запахи тестов

**Описание:** Внутри теста присутствуют условные операторы (`if`, `switch`), что делает тест недетерминированным и сложным.

**Инструменты:** DesigniteJava, AromaDr

---

Вот пример для проблемы **Conditional Test Logic** (условная логика в тестах).

---

### Корректный пример

В этом тесте нет условных операторов. Каждый тестовый метод проверяет ровно одно условие, используя разные входные данные.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountCalculatorTest {

    @Test
    void shouldApplyNoDiscountForRegularCustomer() {
        DiscountCalculator calculator = new DiscountCalculator();
        double result = calculator.calculateDiscount("REGULAR", 100.0);
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void shouldApplyTenPercentDiscountForVipCustomer() {
        DiscountCalculator calculator = new DiscountCalculator();
        double result = calculator.calculateDiscount("VIP", 100.0);
        assertEquals(10.0, result, 0.001);
    }

    @Test
    void shouldApplyFifteenPercentDiscountForPremiumCustomer() {
        DiscountCalculator calculator = new DiscountCalculator();
        double result = calculator.calculateDiscount("PREMIUM", 100.0);
        assertEquals(15.0, result, 0.001);
    }
}

class DiscountCalculator {
    public double calculateDiscount(String customerType, double amount) {
        switch (customerType) {
            case "VIP":     return amount * 0.10;
            case "PREMIUM": return amount * 0.15;
            default:        return 0.0;
        }
    }
}
```

**Почему это хорошо:**  
- Каждый тест проверяет только один сценарий.  
- Нет ветвлений внутри тестового метода.  
- Легко понять, какой тест упал и почему.  
- Тесты детерминированы — результат всегда одинаков для одних и тех же входных данных.

---

### Некорректный пример

Здесь внутри одного теста используется `if`, что делает его недетерминированным и сложным для анализа.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscountCalculatorBadTest {

    @Test
    void testDiscountWithConditionalLogic() {
        DiscountCalculator calculator = new DiscountCalculator();
        String customerType = System.getenv("CUSTOMER_TYPE"); // внешняя зависимость

        double discount = calculator.calculateDiscount(customerType, 100.0);

        if ("VIP".equals(customerType)) {
            assertEquals(10.0, discount, 0.001);
        } else if ("PREMIUM".equals(customerType)) {
            assertEquals(15.0, discount, 0.001);
        } else {
            assertEquals(0.0, discount, 0.001);
        }
    }
}
```

**Почему это плохо:**  
1. **Недетерминированность** — результат теста зависит от переменной окружения `CUSTOMER_TYPE`, которая может меняться.  
2. **Усложнённая диагностика** — при падении теста непонятно, какая именно ветка `if` выполнилась.  
3. **Нарушение принципа "один тест — одна проверка"** — тест проверяет три разных сценария, а не один.  
4. **Сложность поддержки** — чтобы добавить новый тип клиента, придётся модифицировать существующий тест, а не добавить новый.

---

### Объяснение и выявление инструментом

**Какой инструмент обнаружит проблему:**  
**DesigniteJava** (или **AromaDr**, если он также анализирует тестовый код).

**Какое правило сработает:**  
В DesigniteJava есть правило **"Conditional Test Logic"** (или аналогичное в категории "Test Smells"). Оно анализирует AST (абстрактное синтаксическое дерево) тестовых методов и обнаруживает:
- наличие `if`, `switch`, `ternary operator` внутри методов, помеченных `@Test`;
- ветвления, которые не являются частью assert-выражений (например, `assertTrue(a > b ? true : false)` — тоже запах, но более тонкий).

**Как именно сработает:**  
Инструмент найдёт метод `testDiscountWithConditionalLogic`, увидит внутри `if-else if-else` и выдаст предупреждение:  
> "Test method contains conditional logic (if/else/switch). This makes the test non-deterministic and harder to understand. Consider splitting into separate test methods."

**Дополнительно:**  
AromaDr (если настроен на тестовые запахи) также может выявить эту проблему, анализируя частоту и типы управляющих конструкций в тестах.

---

### Резюме

| Аспект | Корректный пример | Некорректный пример |
|--------|-------------------|---------------------|
| Структура | Один тест — один сценарий | Один тест — много сценариев через `if` |
| Детерминированность | Всегда одинаковый результат | Зависит от внешней переменной |
| Обнаружение | Не будет проблем | DesigniteJava / AromaDr укажут на "Conditional Test Logic" |
| Исправление | Разделить на отдельные тесты | Убрать `if`, сделать отдельные тесты |