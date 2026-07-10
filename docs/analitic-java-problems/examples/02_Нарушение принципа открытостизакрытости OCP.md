# Нарушение принципа открытости/закрытости (OCP)

**Категория:** Архитектурные принципы (SOLID)

**Описание:** Класс требует модификации для добавления нового поведения, вместо расширения через наследование или интерфейсы.

**Инструменты:** DesigniteJava, PMD

---

Вот пример, демонстрирующий нарушение принципа открытости/закрытости (OCP) и его исправление.

---

### Корректный пример

**Описание:** Используется интерфейс `Shape` и полиморфизм. Добавление новой фигуры (например, `Triangle`) не требует изменения существующего класса `AreaCalculator`.

```java
// Интерфейс для всех фигур
interface Shape {
    double calculateArea();
}

// Конкретная фигура: Прямоугольник
class Rectangle implements Shape {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double calculateArea() {
        return width * height;
    }
}

// Конкретная фигура: Круг
class Circle implements Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}

// Класс, который не нужно изменять при добавлении новых фигур
class AreaCalculator {
    public double calculateTotalArea(Shape[] shapes) {
        double total = 0;
        for (Shape shape : shapes) {
            total += shape.calculateArea(); // Полиморфизм
        }
        return total;
    }
}

// Использование
public class CorrectOCPExample {
    public static void main(String[] args) {
        Shape[] shapes = { new Rectangle(2, 3), new Circle(5) };
        AreaCalculator calculator = new AreaCalculator();
        System.out.println("Total area: " + calculator.calculateTotalArea(shapes));
    }
}
```

---

### Некорректный пример

**Описание:** Класс `AreaCalculator` содержит явные проверки типов (`instanceof`) и вынужден модифицироваться каждый раз при добавлении новой фигуры.

```java
// Плохой дизайн: нет общего интерфейса
class Rectangle {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

class Circle {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    public double getRadius() { return radius; }
}

// Класс, который нарушает OCP
class AreaCalculator {
    public double calculateTotalArea(Object[] shapes) {
        double total = 0;
        for (Object shape : shapes) {
            if (shape instanceof Rectangle) {
                Rectangle rect = (Rectangle) shape;
                total += rect.getWidth() * rect.getHeight();
            } else if (shape instanceof Circle) {
                Circle circle = (Circle) shape;
                total += Math.PI * circle.getRadius() * circle.getRadius();
            }
            // При добавлении Triangle потребуется новый else-if
        }
        return total;
    }
}

// Использование
public class IncorrectOCPExample {
    public static void main(String[] args) {
        Object[] shapes = { new Rectangle(2, 3), new Circle(5) };
        AreaCalculator calculator = new AreaCalculator();
        System.out.println("Total area: " + calculator.calculateTotalArea(shapes));
    }
}
```

---

### Объяснение и выявление инструментом

**Почему некорректный пример плох:**
1. **Нарушение OCP:** Для добавления новой фигуры (например, `Triangle`) необходимо:
   - Создать новый класс `Triangle`.
   - Изменить метод `calculateTotalArea`, добавив новый `else if` блок.
2. **Хрупкость:** Любое изменение в одном из классов фигур (например, переименование метода `getWidth()`) потребует правки в `AreaCalculator`.
3. **Плохая расширяемость:** Код становится запутанным при росте числа фигур.

**Выявление инструментами:**
- **DesigniteJava:** Обнаружит проблему через правило **"Long Method"** (длинный метод) или **"Complex Conditional"** (сложные условные конструкции), так как `calculateTotalArea` содержит множество `instanceof` проверок.
- **PMD:** Может сработать правило **"AvoidInstanceofChecksInCatchClause"** (но в общем виде — **"LawOfDemeter"** или **"GodClass"**). Наиболее точное правило — **"SwitchStmtsShouldHaveDefault"** (если бы использовался `switch`) или **"TooManyBasicTypeLiterals"**. Однако прямое обнаружение нарушения OCP требует анализа архитектуры, поэтому PMD может не указать на него явно, но **DesigniteJava** специализируется на таких архитектурных дефектах.

**Как именно сработает DesigniteJava:**
- Инструмент проанализирует зависимости и выявит, что класс `AreaCalculator` зависит от конкретных классов (`Rectangle`, `Circle`), а не от абстракции. Это будет отмечено как **"Feature Envy"** (зависть к данным) и **"Insufficient Modularization"** (недостаточная модульность), что прямо указывает на нарушение OCP.