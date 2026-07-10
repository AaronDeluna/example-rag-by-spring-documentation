# Mystery Guest

**Категория:** Запахи тестов

**Описание:** Использование внешних данных или конфигураций, которые не очевидны из кода теста.

**Инструменты:** DesigniteJava

---

Вот пример для проблемы **Mystery Guest** (использование неочевидных внешних данных/конфигураций в тестах), оформленный по вашим требованиям.

---

### Корректный пример

Тест явно создаёт и передаёт все необходимые данные, не полагаясь на скрытые внешние источники.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest {

    // Корректный тест: все входные данные явно определены в тесте
    @Test
    public void testCalculateBonus_ExplicitData() {
        // Given - явное создание тестовых данных
        User testUser = new User("test_user", 1000.0);
        UserService userService = new UserService();

        // When
        double bonus = userService.calculateBonus(testUser);

        // Then - ожидаемый результат вычислен на основе очевидных входных данных
        double expectedBonus = 100.0; // 10% от 1000
        assertEquals(expectedBonus, bonus, 0.001);
    }
}

// Вспомогательные классы для примера
class User {
    private String name;
    private double balance;

    public User(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public double getBalance() {
        return balance;
    }
}

class UserService {
    public double calculateBonus(User user) {
        return user.getBalance() * 0.1; // 10% бонус
    }
}
```

**Почему корректно:**  
- Все данные (`testUser`, его баланс) создаются прямо в тесте.  
- Нет скрытых конфигурационных файлов, глобальных переменных или внешних сервисов.  
- Любой разработчик может сразу понять, какой результат ожидается.

---

### Некорректный пример

Тест использует данные из внешнего файла или системной переменной, что делает его непрозрачным.

```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserServiceTest {

    // Некорректный тест: использует "Mystery Guest" - скрытые внешние данные
    @Test
    public void testCalculateBonus_MysteryGuest() {
        // Данные берутся из внешнего файла или конфигурации, неочевидной из кода
        User testUser = ExternalUserLoader.loadUserFromConfig(); // <-- Mystery Guest
        UserService userService = new UserService();

        // Когда
        double bonus = userService.calculateBonus(testUser);

        // Тогда: ожидаемое значение тоже "магическое" - читатель не знает, откуда оно взялось
        assertEquals(250.0, bonus, 0.001); // Почему именно 250? Непонятно.
    }
}

// Вспомогательные классы для примера
class ExternalUserLoader {
    // Загрузка из скрытого файла (например, test-config.properties)
    public static User loadUserFromConfig() {
        // В реальном коде это чтение из файла, БД или системной проперти
        // Здесь упрощённо возвращаем фиктивного пользователя
        return new User("mystery_guest", 2500.0); // баланс 2500, но это не видно в тесте
    }
}
```

**Почему плохо (признаки проблемы "Mystery Guest"):**  
1. **Неявная зависимость от внешнего источника** – метод `loadUserFromConfig()` может читать данные из файла, системных переменных или удалённого сервиса.  
2. **Тест несамодостаточен** – чтобы понять, какие данные используются, нужно идти в другой класс/файл/конфигурацию.  
3. **Магическое ожидаемое значение** – `250.0` появляется из ниоткуда; без знания баланса пользователя (2500.0) невозможно проверить корректность.  
4. **Тест хрупкий** – если изменится внешний конфигурационный файл, тест упадёт без изменения кода самого теста.

---

### Объяснение и выявление инструментом

**Какой инструмент обнаруживает проблему:**  
**DesigniteJava** (инструмент статического анализа Java-кода, специализирующийся на запахах дизайна и тестов).

**Какое правило/проверка сработает:**  
- В DesigniteJava есть категория **"Test Smell"**, а внутри неё — правило **"Mystery Guest"** (или "Mystery Guest Test Smell").  
- Инструмент анализирует тестовые методы и ищет вызовы внешних ресурсов, которые не являются частью теста (например, чтение из файлов, системных переменных, глобальных синглтонов, баз данных).  
- В нашем некорректном примере сработает детектор **"External Resource Dependency"** — он заметит, что тест вызывает `ExternalUserLoader.loadUserFromConfig()`, который, скорее всего, обращается к внешнему источнику (файлу, проперти).  
- DesigniteJava также может выявить **"Magic Number Test"** для ожидаемого значения `250.0`, если оно не выведено из явных данных.

**Как именно будет выглядеть предупреждение (пример):**  
```
TestSmell: Mystery Guest
Location: UserServiceTest.testCalculateBonus_MysteryGuest()
Reason: Test relies on external data source (ExternalUserLoader.loadUserFromConfig) 
        which is not obvious from the test code.
Recommendation: Inline test data or use test doubles to avoid hidden dependencies.
```

**Вывод:**  
Корректный пример делает данные видимыми, а некорректный — прячет их за внешним вызовом, что и обнаруживает DesigniteJava как "Mystery Guest".