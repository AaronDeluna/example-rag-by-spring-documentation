# Assertion Roulette

**Категория:** Запахи тестов

**Описание:** Один тест содержит несколько проверок (`assert`), и при падении непонятно, какая из них вызвала ошибку.

**Инструменты:** DesigniteJava, TestSmellDetector

---

Вот пример для проблемы **Assertion Roulette** с корректным и некорректным кодом на Java.

---

### Корректный пример

В хорошем тесте каждая проверка (`assert`) должна быть изолирована в отдельном тестовом методе, чтобы при падении сразу было понятно, какое именно условие нарушено.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    // Корректно: один assert на один тест
    @Test
    void testAddition() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3), "2 + 3 должно равняться 5");
    }

    @Test
    void testSubtraction() {
        Calculator calc = new Calculator();
        assertEquals(1, calc.subtract(3, 2), "3 - 2 должно равняться 1");
    }

    @Test
    void testMultiplication() {
        Calculator calc = new Calculator();
        assertEquals(6, calc.multiply(2, 3), "2 * 3 должно равняться 6");
    }
}

// Упрощённый класс для примера
class Calculator {
    int add(int a, int b) { return a + b; }
    int subtract(int a, int b) { return a - b; }
    int multiply(int a, int b) { return a * b; }
}
```

**Почему это хорошо:**  
Каждый тест проверяет только одну операцию. Если тест `testAddition` упадёт, сообщение об ошибке сразу укажет на проблему в сложении. Нет необходимости гадать, какой из нескольких `assert` провалился.

---

### Некорректный пример

Здесь один тест содержит три проверки подряд. При падении любого из `assertEquals` непонятно, какая именно операция (сложение, вычитание или умножение) вызвала ошибку, если не смотреть на трассировку стека (что замедляет отладку).

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    // Некорректно: несколько assert'ов в одном тесте (Assertion Roulette)
    @Test
    void testAllOperations() {
        Calculator calc = new Calculator();
        assertEquals(5, calc.add(2, 3));
        assertEquals(1, calc.subtract(3, 2));
        assertEquals(6, calc.multiply(2, 3));
    }
}

// Тот же упрощённый класс
class Calculator {
    int add(int a, int b) { return a + b; }
    int subtract(int a, int b) { return a - b; }
    int multiply(int a, int b) { return a * b; }
}
```

**Почему это плохо:**  
- Если тест упадёт, в отчёте будет только сообщение типа `expected: <...> but was: <...>`, но без указания, какая именно операция проверялась (если не добавить сообщение в каждый `assertEquals`).  
- Приходится запускать тест повторно или вручную анализировать стек, чтобы понять, какая строка провалилась.  
- Нарушается принцип "один тест — одна проверка", что усложняет поддержку и понимание тестов.

---

### Объяснение и выявление инструментом

**Объяснение:**  
Проблема «Assertion Roulette» заключается в том, что один тестовый метод содержит несколько утверждений (`assert`), и при падении неочевидно, какое именно утверждение нарушено. Это увеличивает время отладки и снижает читаемость тестов.

**Выявление инструментом:**  
Инструмент **TestSmellDetector** (или **DesigniteJava**) может обнаружить этот запах.  
- **Правило/проверка:** «Multiple Assertions in a Single Test Method» (или аналогичное).  
- **Как работает:** Анализатор парсит AST (абстрактное синтаксическое дерево) тестового класса и подсчитывает количество вызовов методов `assert*` (например, `assertEquals`, `assertTrue`, `assertNotNull`) внутри одного метода. Если их больше одного, инструмент помечает метод как содержащий «Assertion Roulette».  
- В нашем некорректном примере метод `testAllOperations` содержит три вызова `assertEquals`, поэтому **TestSmellDetector** выдаст предупреждение, указывая на этот метод.

**Дополнительно:**  
DesigniteJava также может обнаружить этот запах, анализируя тестовые классы на наличие нескольких assert-выражений в одном методе. Оба инструмента используют статический анализ кода, не требуя выполнения тестов.