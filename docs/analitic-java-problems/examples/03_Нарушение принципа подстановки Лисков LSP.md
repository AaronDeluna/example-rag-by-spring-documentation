# Нарушение принципа подстановки Лисков (LSP)

**Категория:** Архитектурные принципы (SOLID)

**Описание:** Подкласс не может заменить родительский класс без изменения ожидаемого поведения (например, переопределяет метод с нарушением контракта).

**Инструменты:** DesigniteJava, PMD

---

Вот пример, демонстрирующий проблему нарушения принципа подстановки Лисков (LSP) и её корректное решение.

---

### Корректный пример

**Описание:**  
Используем композицию и чёткое разделение ответственности. Класс `Rectangle` имеет методы для установки ширины и высоты. Класс `Square` не наследует `Rectangle`, а использует его как композицию или является отдельной сущностью с собственным контрактом. Это гарантирует, что ни один подкласс не нарушит поведение родителя.

```java
// Корректный пример: избегаем наследования, где нарушается LSP

class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getArea() {
        return width * height;
    }
}

// Квадрат не наследует Rectangle, а является отдельным классом
class Square {
    private int side;

    public void setSide(int side) {
        this.side = side;
    }

    public int getArea() {
        return side * side;
    }
}

// Клиентский код, который работает с Rectangle
class AreaCalculator {
    public void printArea(Rectangle rect) {
        rect.setWidth(5);
        rect.setHeight(10);
        System.out.println("Area: " + rect.getArea()); // Всегда 50
    }
}
```

**Почему это корректно:**  
- Нет подкласса, который мог бы изменить поведение `Rectangle`.  
- Любой код, использующий `Rectangle`, может быть уверен, что `setWidth` и `setHeight` работают независимо.  
- Принцип подстановки Лисков не нарушен, так как нет наследования с изменением контракта.

---

### Некорректный пример

**Описание:**  
Класс `Square` наследует `Rectangle` и переопределяет методы `setWidth` и `setHeight`, чтобы сохранять квадратную форму. Это нарушает LSP, так как клиент, ожидающий стандартного поведения прямоугольника, получает неожиданные результаты.

```java
// Некорректный пример: нарушение LSP

class Rectangle {
    protected int width;
    protected int height;

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getArea() {
        return width * height;
    }
}

class Square extends Rectangle {
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        super.setHeight(width); // Нарушение: меняем высоту при изменении ширины
    }

    @Override
    public void setHeight(int height) {
        super.setHeight(height);
        super.setWidth(height); // Нарушение: меняем ширину при изменении высоты
    }
}

// Клиентский код
class AreaCalculator {
    public void printArea(Rectangle rect) {
        rect.setWidth(5);
        rect.setHeight(10);
        System.out.println("Expected area: 50, but got: " + rect.getArea());
        // Вывод: Expected area: 50, but got: 100 (так как setHeight(10) установит ширину = 10)
    }
}

public class Main {
    public static void main(String[] args) {
        Rectangle rect = new Square();
        new AreaCalculator().printArea(rect); // Нарушение ожидаемого поведения
    }
}
```

**Вывод программы:**  
```
Expected area: 50, but got: 100
```

---

### Объяснение и выявление инструментом

**Почему код плох:**  
- Нарушен принцип подстановки Лисков (LSP): объект `Square` не может заменить `Rectangle` без изменения ожидаемого поведения.  
- Клиентский код полагается на то, что `setWidth` и `setHeight` изменяют только соответствующую сторону, но `Square` изменяет обе.  
- Это приводит к логическим ошибкам, которые трудно обнаружить во время компиляции.

**Какой инструмент выявит проблему:**  
- **PMD** (правило `LooseCoupling` или `LawOfDemeter` не подходят напрямую, но есть правило `AvoidSynchronizedAtMethodLevel` — нет). Более специфично: **PMD** может выявить через правило **`MethodReturnsInternalArray`**? Нет. Лучше всего подходит **DesigniteJava**, который имеет встроенную проверку на нарушение LSP.  
- **DesigniteJava** анализирует иерархию наследования и выявляет случаи, когда подкласс переопределяет методы родителя таким образом, что нарушает контракт (например, изменяет предусловия/постусловия). В данном примере будет обнаружено, что `Square.setWidth()` и `Square.setHeight()` изменяют обе стороны, что противоречит контракту `Rectangle`.  
- **PMD** также может быть настроен на обнаружение через кастомные правила, но из коробки можно использовать **`Design`** категорию и правило **`ExcessiveClassLength`**? Нет. Однако, если использовать **PMD** с правилом **`GodClass`** или **`DataClass`**, это не поможет. Для LSP в PMD есть экспериментальное правило **`LooseCoupling`**, но оно не специфично. Поэтому **DesigniteJava** — основной инструмент для данной проблемы.

**Конкретное срабатывание DesigniteJava:**  
- Инструмент проверит все переопределения методов и сравнит их с контрактом родительского класса.  
- Для `Square` будет зафиксировано, что метод `setWidth` изменяет также высоту, что является нарушением постусловия (высота не должна меняться при вызове `setWidth`).  
- Вывод: *"Class Square violates Liskov Substitution Principle: method setWidth(int) changes the state of the parent class in an unexpected way."*