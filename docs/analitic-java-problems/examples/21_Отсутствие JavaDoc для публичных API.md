# Отсутствие JavaDoc для публичных API

**Категория:** Стиль кодирования (Google Java Style Guide)

**Описание:** Не описано назначение и параметры публичных методов и классов.

**Инструменты:** Checkstyle

---

Вот примеры для проблемы «Отсутствие JavaDoc для публичных API».

---

### Корректный пример

```java
/**
 * Сервис для обработки заказов в интернет-магазине.
 * Отвечает за создание, обновление и проверку статуса заказов.
 */
public class OrderService {

    /**
     * Создаёт новый заказ на основе переданных данных.
     *
     * @param customerId идентификатор клиента (должен быть положительным)
     * @param items      список товаров в заказе (не может быть null или пустым)
     * @return объект созданного заказа с присвоенным уникальным номером
     * @throws IllegalArgumentException если customerId &lt;= 0 или items == null/пусто
     */
    public Order createOrder(long customerId, List<Item> items) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items list must not be null or empty");
        }
        return new Order(customerId, items);
    }

    /**
     * Возвращает текущий статус заказа по его идентификатору.
     *
     * @param orderId уникальный номер заказа
     * @return статус заказа в виде строки (например, "CREATED", "SHIPPED", "DELIVERED")
     * @throws IllegalArgumentException если orderId &lt;= 0
     */
    public String getOrderStatus(long orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("orderId must be positive");
        }
        // имитация получения статуса
        return "CREATED";
    }
}
```

---

### Некорректный пример

```java
public class OrderService {

    public Order createOrder(long customerId, List<Item> items) {
        if (customerId <= 0) {
            throw new IllegalArgumentException("customerId must be positive");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items list must not be null or empty");
        }
        return new Order(customerId, items);
    }

    public String getOrderStatus(long orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException("orderId must be positive");
        }
        return "CREATED";
    }
}
```

---

### Объяснение и выявление инструментом

**Почему некорректный пример плох:**
- Класс `OrderService` и оба его публичных метода (`createOrder`, `getOrderStatus`) не имеют JavaDoc-комментариев.
- Другие разработчики (или будущий автор) не могут понять:
  - Какие параметры принимают методы и что они возвращают.
  - Какие исключения могут быть выброшены и при каких условиях.
  - Какое общее назначение класса.
- Это снижает читаемость, усложняет поддержку и увеличивает риск ошибок при использовании API.

**Какой инструмент выявит проблему и как:**
- **Checkstyle** с подключенным правилом `JavadocMethod` и `JavadocType` (из набора `Sun Checks` или Google Checks).
- **Конкретное правило:**
  - Для класса: `JavadocType` — проверяет, что публичные классы и интерфейсы имеют JavaDoc.
  - Для методов: `JavadocMethod` — проверяет, что публичные методы (и защищённые) имеют JavaDoc.
- **Пример срабатывания:** при запуске Checkstyle на некорректном примере будут выданы ошибки:
  ```
  Missing a Javadoc comment. [JavadocType] (для класса)
  Missing a Javadoc comment. [JavadocMethod] (для метода createOrder)
  Missing a Javadoc comment. [JavadocMethod] (для метода getOrderStatus)
  ```

**Примечание:** В реальных проектах часто настраивают Checkstyle так, чтобы требовать JavaDoc только для публичных (`public`) элементов, а для `private` — не требовать.