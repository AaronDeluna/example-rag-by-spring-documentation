# Нарушение принципа инверсии зависимостей (DIP)

**Категория:** Архитектурные принципы (SOLID)

**Описание:** Код зависит от конкретных реализаций, а не от абстракций (интерфейсов), что усложняет тестирование и замену компонентов.

**Инструменты:** DesigniteJava, PMD

---

Вот пример, демонстрирующий нарушение и соблюдение принципа инверсии зависимостей (DIP) на Java.

---

### Корректный пример

Код зависит от абстракции (интерфейса `NotificationSender`), а не от конкретного класса. Это позволяет легко подменять реализации (например, для тестирования или замены способа отправки).

```java
// Абстракция (интерфейс)
interface NotificationSender {
    void send(String message);
}

// Конкретная реализация 1
class EmailSender implements NotificationSender {
    @Override
    public void send(String message) {
        System.out.println("Sending email: " + message);
    }
}

// Конкретная реализация 2 (например, для тестов)
class MockSender implements NotificationSender {
    @Override
    public void send(String message) {
        // Ничего не делаем, просто логируем
        System.out.println("Mock send: " + message);
    }
}

// Класс, который использует абстракцию
class NotificationService {
    private final NotificationSender sender;

    // Зависимость внедряется через конструктор (Dependency Injection)
    public NotificationService(NotificationSender sender) {
        this.sender = sender;
    }

    public void notifyUser(String message) {
        sender.send(message);
    }
}

// Пример использования
public class Main {
    public static void main(String[] args) {
        NotificationSender sender = new EmailSender();
        NotificationService service = new NotificationService(sender);
        service.notifyUser("Hello, DIP!");
    }
}
```

**Почему это корректно:**
- `NotificationService` не знает о конкретной реализации `EmailSender` или `MockSender`.
- Легко писать unit-тесты, подставляя `MockSender`.
- Соответствует принципу DIP: модули верхнего уровня не зависят от модулей нижнего уровня, оба зависят от абстракций.

---

### Некорректный пример

Код жёстко привязан к конкретной реализации (`EmailSender`). Это делает класс `NotificationService` трудным для тестирования и расширения.

```java
// Конкретная реализация (без интерфейса)
class EmailSender {
    public void sendEmail(String message) {
        System.out.println("Sending email: " + message);
    }
}

// Класс, жёстко зависящий от конкретной реализации
class NotificationService {
    private final EmailSender sender;

    public NotificationService() {
        // Нарушение DIP: создаём конкретный объект внутри класса
        this.sender = new EmailSender();
    }

    public void notifyUser(String message) {
        sender.sendEmail(message);
    }
}

// Пример использования
public class Main {
    public static void main(String[] args) {
        NotificationService service = new NotificationService();
        service.notifyUser("Hello, DIP!");
    }
}
```

**Почему это плохо:**
- Для тестирования `NotificationService` невозможно подменить `EmailSender` на мок-объект (например, чтобы не отправлять реальные письма).
- Если потребуется другой способ отправки (SMS, push), придётся модифицировать класс `NotificationService`.
- Нарушен принцип открытости/закрытости (OCP) и инверсии зависимостей (DIP).

---

### Объяснение и выявление инструментом

**Какой инструмент обнаружит проблему:**  
**PMD** (правило `LooseCoupling`).

**Как именно сработает проверка:**  
- PMD анализирует, что поле `sender` имеет тип `EmailSender` (конкретный класс), а не интерфейс.  
- Правило `LooseCoupling` (категория `design`) предупреждает:  
  *"Avoid using implementation types like 'EmailSender'; use the interface instead."*  
- В отчёте PMD будет указано что-то вроде:  
  `Found violation: NotificationService uses concrete class EmailSender instead of an interface.`

**Дополнительно:**  
Инструмент **DesigniteJava** также может выявить это нарушение как часть анализа принципов SOLID (например, как *"DIP violation"*), но PMD более распространён и имеет прямое правило для данной ситуации.

**Резюме:**  
Некорректный пример жёстко связывает класс с конкретной реализацией, что нарушает DIP. PMD с правилом `LooseCoupling` обнаружит эту проблему на этапе статического анализа кода.