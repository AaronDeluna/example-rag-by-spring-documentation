# Нарушение принципа единственной ответственности (SRP)

**Категория:** Архитектурные принципы (SOLID)

**Описание:** Класс выполняет несколько несвязанных задач (например, одновременно работает с БД, генерирует отчёты и отправляет email).

**Инструменты:** DesigniteJava, PMD

---

Вот пример, демонстрирующий нарушение и соблюдение принципа единственной ответственности (Single Responsibility Principle, SRP) на Java.

---

### Корректный пример

В этом примере каждый класс отвечает ровно за одну задачу: `UserRepository` — за работу с базой данных, `ReportGenerator` — за генерацию отчета, `EmailService` — за отправку писем. Класс `UserService` координирует их работу, но не смешивает логику.

```java
import java.util.List;

// 1. Класс для работы с БД
class UserRepository {
    public List<String> getActiveUsers() {
        // Логика запроса к БД
        return List.of("user1@example.com", "user2@example.com");
    }
}

// 2. Класс для генерации отчётов
class ReportGenerator {
    public String generateReport(List<String> users) {
        // Логика формирования отчёта
        return "Report: " + String.join(", ", users);
    }
}

// 3. Класс для отправки email
class EmailService {
    public void sendEmail(String to, String subject, String body) {
        // Логика отправки письма
        System.out.println("Sending email to " + to + ": " + subject);
    }
}

// 4. Класс-координатор (сервис)
class UserService {
    private final UserRepository userRepository;
    private final ReportGenerator reportGenerator;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       ReportGenerator reportGenerator,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.reportGenerator = reportGenerator;
        this.emailService = emailService;
    }

    public void sendReportToActiveUsers() {
        List<String> activeUsers = userRepository.getActiveUsers();
        String report = reportGenerator.generateReport(activeUsers);
        for (String user : activeUsers) {
            emailService.sendEmail(user, "Monthly Report", report);
        }
    }
}

// Точка входа
public class Main {
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        ReportGenerator generator = new ReportGenerator();
        EmailService email = new EmailService();
        UserService service = new UserService(repo, generator, email);
        service.sendReportToActiveUsers();
    }
}
```

**Почему это корректно:**  
Каждый класс имеет единственную причину для изменения. Если поменяется логика отправки email, изменится только `EmailService`. Если изменится формат отчёта — только `ReportGenerator`. Класс `UserService` только координирует вызовы, не содержа собственной сложной бизнес-логики.

---

### Некорректный пример

Здесь один класс `UserManager` выполняет сразу три несвязанные задачи: работает с БД, генерирует отчёт и отправляет email.

```java
import java.util.List;

class UserManager {
    // 1. Работа с БД
    public List<String> getActiveUsers() {
        // Логика запроса к БД
        return List.of("user1@example.com", "user2@example.com");
    }

    // 2. Генерация отчёта
    public String generateReport(List<String> users) {
        // Логика формирования отчёта
        return "Report: " + String.join(", ", users);
    }

    // 3. Отправка email
    public void sendEmail(String to, String subject, String body) {
        // Логика отправки письма
        System.out.println("Sending email to " + to + ": " + subject);
    }

    // 4. Всё вместе
    public void sendReportToActiveUsers() {
        List<String> activeUsers = getActiveUsers();
        String report = generateReport(activeUsers);
        for (String user : activeUsers) {
            sendEmail(user, "Monthly Report", report);
        }
    }
}

public class Main {
    public static void main(String[] args) {
        UserManager manager = new UserManager();
        manager.sendReportToActiveUsers();
    }
}
```

---

### Объяснение и выявление инструментом

**Почему некорректный пример плох:**

1. **Нарушение SRP:** Класс `UserManager` имеет как минимум три причины для изменения:
   - изменение способа хранения/получения пользователей;
   - изменение формата или содержания отчёта;
   - изменение логики отправки email (например, смена SMTP-сервера).
2. **Сложность тестирования:** Чтобы протестировать отправку email, придётся поднимать БД. Нельзя легко заменить БД на мок.
3. **Низкая переиспользуемость:** Нельзя повторно использовать логику генерации отчёта без копирования кода, если она понадобится в другом месте.
4. **Трудность сопровождения:** Один класс становится большим и "раздутым", его трудно читать и модифицировать.

**Какой инструмент выявит проблему и как:**

**Инструмент:** PMD  
**Правило:** `GodClass` (или `ExcessivePublicCount`, `TooManyMethods`, `CyclomaticComplexity`)  
**Механизм:** PMD анализирует метрики класса. Если класс содержит слишком много методов, полей или имеет высокую цикломатическую сложность, он помечается как «God Class» (класс-бог). В данном примере `UserManager` имеет 4 метода, но каждый из них реализует разные функциональности, что типично для нарушения SRP.

**Пример срабатывания PMD:**  
При запуске PMD с правилом `java-design/GodClass` будет выдано предупреждение:
```
UserManager.java:1: GodClass: Class 'UserManager' has too many methods (4) and fields (0) with high complexity (3). Consider refactoring it into smaller classes.
```

**Инструмент:** DesigniteJava  
**Механизм:** DesigniteJava также выявляет «God Class» на основе метрик связности (cohesion) — чем ниже связность методов внутри класса, тем выше вероятность нарушения SRP. В `UserManager` методы `getActiveUsers`, `generateReport`, `sendEmail` не связаны между собой по смыслу (низкое cohesion), что будет отмечено как нарушение.

**Дополнительно (ручная проверка):**  
Любой статический анализатор, поддерживающий метрики (например, SonarQube с правилом `"class should have single responsibility"`), также укажет на этот класс как на проблемный.

---

### Резюме

| Пример | Соответствие SRP | Инструменты выявления |
|--------|------------------|------------------------|
| Корректный (`UserService` + `UserRepository` + `ReportGenerator` + `EmailService`) | Да | Не требуется |
| Некорректный (`UserManager`) | Нет | PMD (`GodClass`), DesigniteJava (низкая связность) |