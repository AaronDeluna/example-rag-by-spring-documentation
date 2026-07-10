# Пустые блоки catch

**Категория:** Стиль кодирования (Clean Code)

**Описание:** Игнорирование исключений без логирования.

**Инструменты:** PMD, SpotBugs

---

Вот пример, демонстрирующий проблему пустых блоков `catch` и правильный подход к её решению.

---

### Корректный пример

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileProcessor {
    private static final Logger LOGGER = Logger.getLogger(FileProcessor.class.getName());

    public String readFirstLine(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            // Корректно: логируем исключение с уровнем SEVERE
            LOGGER.log(Level.SEVERE, "Ошибка при чтении файла: " + filePath, e);
            // Возвращаем значение по умолчанию или пробрасываем исключение дальше
            return null; // или throw new RuntimeException(e);
        }
    }
}
```

**Почему это хорошо:**
- Исключение не игнорируется.
- Выполняется логирование с указанием контекста (имя файла).
- Используется встроенный логгер Java (можно заменить на любой другой).
- Код явно сообщает о проблеме и не скрывает её.

---

### Некорректный пример

```java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileProcessorBad {
    public String readFirstLine(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            // ПУСТОЙ БЛОК CATCH — проблема!
        }
        return null;
    }
}
```

---

### Объяснение и выявление инструментом

**Почему это плохо:**
1. **Игнорирование исключения** — программа продолжает работу, как будто ошибки не было, хотя файл не был прочитан.
2. **Потеря диагностической информации** — ни логов, ни уведомлений об ошибке не создаётся.
3. **Сложность отладки** — если в будущем возникнет проблема, разработчик не сможет понять, почему метод вернул `null`.
4. **Нарушение принципов Clean Code** — блок `catch` должен либо обрабатывать ошибку, либо пробрасывать её выше, но не быть пустым.

**Какие инструменты и как выявят проблему:**

- **PMD** — правило `EmptyCatchBlock` (по умолчанию в наборе правил `java-errorprone`).  
  Срабатывает на любой пустой блок `catch` без комментария или с комментарием, но без реальной обработки.
  
- **SpotBugs** — детектор `REC_CATCH_EXCEPTION` (правило `REC: Exception is caught when Exception is not thrown`) или `DE_MIGHT_IGNORE` (если пустой catch явно игнорирует исключение).  
  В данном случае SpotBugs выдаст предупреждение: *"Found empty catch block"*.

**Пример вывода PMD:**
```
FileProcessorBad.java:9: EmptyCatchBlock: Avoid empty catch blocks
```

**Пример вывода SpotBugs:**
```
Bug: REC_CATCH_EXCEPTION - Exception is caught when Exception is not thrown in FileProcessorBad.readFirstLine(String)
```

Оба инструмента легко настраиваются в CI/CD и могут блокировать сборку при наличии таких проблем.