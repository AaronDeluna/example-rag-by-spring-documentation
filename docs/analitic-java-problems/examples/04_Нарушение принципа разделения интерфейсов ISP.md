# Нарушение принципа разделения интерфейсов (ISP)

**Категория:** Архитектурные принципы (SOLID)

**Описание:** «Толстый» интерфейс, который заставляет классы реализовывать неиспользуемые методы.

**Инструменты:** DesigniteJava, PMD

---

Вот пример, демонстрирующий нарушение принципа разделения интерфейсов (ISP) на Java, а также его исправление.

---

### Корректный пример

Вместо одного «толстого» интерфейса создаются несколько узкоспециализированных интерфейсов. Каждый класс реализует только те интерфейсы, которые ему действительно нужны.

```java
// Узкие интерфейсы
interface Worker {
    void work();
}

interface Eater {
    void eat();
}

interface Sleeper {
    void sleep();
}

// Класс Human реализует все три интерфейса
class Human implements Worker, Eater, Sleeper {
    @Override
    public void work() {
        System.out.println("Human working");
    }

    @Override
    public void eat() {
        System.out.println("Human eating");
    }

    @Override
    public void sleep() {
        System.out.println("Human sleeping");
    }
}

// Класс Robot реализует только один интерфейс
class Robot implements Worker {
    @Override
    public void work() {
        System.out.println("Robot working");
    }
}

// Пример использования
public class Main {
    public static void main(String[] args) {
        Worker human = new Human();
        Worker robot = new Robot();
        human.work();
        robot.work();
        
        // Robot не может есть или спать, но это и не требуется
        Eater eater = new Human();
        eater.eat();
    }
}
```

**Почему это корректно:**  
- Каждый интерфейс имеет единственную ответственность.  
- Классы не вынуждены реализовывать методы, которые им не нужны (например, `Robot` не реализует `eat()` и `sleep()`).  
- Код гибкий и легко расширяется.

---

### Некорректный пример

«Толстый» интерфейс `Worker` содержит методы, которые не нужны всем его реализациям.

```java
// Толстый интерфейс
interface Worker {
    void work();
    void eat();
    void sleep();
}

// Класс Human вынужден реализовать все три метода — это нормально
class Human implements Worker {
    @Override
    public void work() {
        System.out.println("Human working");
    }

    @Override
    public void eat() {
        System.out.println("Human eating");
    }

    @Override
    public void sleep() {
        System.out.println("Human sleeping");
    }
}

// Класс Robot вынужден реализовать методы, которые ему не нужны
class Robot implements Worker {
    @Override
    public void work() {
        System.out.println("Robot working");
    }

    @Override
    public void eat() {
        // Робот не ест, но метод нужно реализовать
        throw new UnsupportedOperationException("Robot cannot eat");
    }

    @Override
    public void sleep() {
        // Робот не спит, но метод нужно реализовать
        throw new UnsupportedOperationException("Robot cannot sleep");
    }
}

// Пример использования
public class Main {
    public static void main(String[] args) {
        Worker robot = new Robot();
        robot.work();
        // При вызове неиспользуемого метода — ошибка
        // robot.eat(); // UnsupportedOperationException
    }
}
```

**Почему это плохо:**  
- Класс `Robot` вынужден реализовывать методы `eat()` и `sleep()`, которые не имеют смысла для робота.  
- Реализация через `throw UnsupportedOperationException` — это «запах кода» (code smell), который сигнализирует о нарушении ISP.  
- При добавлении нового класса, например `Dog`, ему придётся реализовывать `work()`, хотя собаки не работают.  
- Нарушается принцип подстановки Барбары Лисков (LSP), так как вызов `eat()` у `Robot` приводит к исключению.

---

### Объяснение и выявление инструментом

**Какой инструмент обнаружит проблему и как:**

1. **DesigniteJava**  
   - Правило: **«Interface Segregation Principle Violation»** (ISP).  
   - Как сработает: инструмент анализирует все реализации интерфейса и проверяет, есть ли классы, которые не используют некоторые методы интерфейса (например, выбрасывают исключения или оставляют пустую реализацию). В примере с `Robot` методы `eat()` и `sleep()` не используются — будет зафиксировано нарушение ISP.

2. **PMD**  
   - Правило: **`UncommentedEmptyMethodBody`** или **`AvoidThrowingUnsupportedOperationException`** (некоторые конфигурации).  
   - Как сработает: PMD может обнаружить пустые методы или методы, которые всегда выбрасывают `UnsupportedOperationException`. Это косвенно укажет на то, что интерфейс слишком «толстый» для данной реализации.  
   - Также можно настроить кастомное правило для поиска классов, реализующих интерфейс, но не использующих все его методы (через XPath или Java-правила).

**Вывод:**  
Некорректный пример явно нарушает ISP, заставляя `Robot` реализовывать методы, которые ему не нужны. Инструменты статического анализа, такие как DesigniteJava и PMD, способны выявить это через проверку пустых/исключительных реализаций или явное правило для ISP.