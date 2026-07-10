Конечно, я приведу три готовых bash-скрипта, соответствующих трём способам добавления проверок. Все они запускают Checkstyle **без подключения к Maven/Gradle**, используя только JAR-файл.

---

## 1. Скрипт с заранее подготовленным (стандартным) файлом конфигурации

Использует готовый конфиг, например `google_checks.xml` (встроенный в JAR) или скачанный отдельно.

```bash
#!/usr/bin/env bash
# Скрипт: run-checkstyle-standard.sh
# Использует стандартную конфигурацию Google Java Style.

CHECKSTYLE_JAR="./lib/checkstyle-10.18.1-all.jar"
# Можно указать встроенный ресурс (внутри JAR) или путь к своему файлу
CONFIG="/google_checks.xml"   # встроенный, или "./config/my_checks.xml"
SOURCE_PATH="./src/main/java"

if [ ! -f "$CHECKSTYLE_JAR" ]; then
    echo "Ошибка: JAR-файл Checkstyle не найден!"
    exit 1
fi

echo "Запуск Checkstyle со стандартным конфигом..."
java -jar "$CHECKSTYLE_JAR" -c "$CONFIG" "$SOURCE_PATH"
```

**Примечание:** если вы хотите использовать свой XML-файл, замените `CONFIG` на путь к нему, например `./config/my_checks.xml`.

---

## 2. Скрипт с кастомными простыми проверками (без написания Java-кода)

В этом случае все правила описываются прямо в XML-конфиге с помощью встроенных модулей (`RegexpSingleline`, `MatchXpath` и т.д.). Скрипт остаётся таким же, как и в первом случае, но используется другой файл конфигурации.

### Пример файла `my_simple_checks.xml`
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
          "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
          "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
    <module name="TreeWalker">
        <!-- Запрещаем звездчатый импорт -->
        <module name="AvoidStarImport"/>
        
        <!-- Запрещаем использование System.out.println() -->
        <module name="RegexpSingleline">
            <property name="format" value="System\.out\.println"/>
            <property name="message" value="Использование System.out.println() запрещено."/>
        </module>
        
        <!-- Запрещаем printStackTrace() -->
        <module name="RegexpSingleline">
            <property name="format" value="\.printStackTrace\(\)"/>
            <property name="message" value="Использование printStackTrace() запрещено."/>
        </module>
        
        <!-- Проверяем, что все публичные методы имеют JavaDoc -->
        <module name="JavadocMethod"/>
    </module>
</module>
```

### Сам скрипт
```bash
#!/usr/bin/env bash
# Скрипт: run-checkstyle-simple-custom.sh
# Использует конфиг с простыми регулярными проверками.

CHECKSTYLE_JAR="./lib/checkstyle-10.18.1-all.jar"
CONFIG="./config/my_simple_checks.xml"   # путь к вашему XML
SOURCE_PATH="./src/main/java"

if [ ! -f "$CHECKSTYLE_JAR" ]; then
    echo "Ошибка: JAR-файл Checkstyle не найден!"
    exit 1
fi

if [ ! -f "$CONFIG" ]; then
    echo "Ошибка: файл конфигурации $CONFIG не найден!"
    exit 1
fi

echo "Запуск Checkstyle с простыми кастомными проверками..."
java -jar "$CHECKSTYLE_JAR" -c "$CONFIG" "$SOURCE_PATH"
```

---

## 3. Скрипт с написанием собственного Java-класса проверки (для сложной логики)

Здесь вы компилируете свои проверки в отдельный JAR-файл, а затем подключаете его через `-cp` вместе с основным JAR-файлом Checkstyle.

### Пример вашего класса проверки (кратко)
```java
package com.mycompany.checkstyle;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
// ... реализация вашего сложного правила
```

После компиляции вы получите `my-custom-checks.jar`. Теперь скрипт выглядит так:

```bash
#!/usr/bin/env bash
# Скрипт: run-checkstyle-with-custom-class.sh
# Подключает внешний JAR с кастомными проверками.

CHECKSTYLE_JAR="./lib/checkstyle-10.18.1-all.jar"
CUSTOM_JAR="./lib/my-custom-checks.jar"      # ваш JAR с классами проверок
CONFIG="./config/my_checks_with_custom.xml"  # конфиг, где прописан ваш класс
SOURCE_PATH="./src/main/java"

# Проверяем наличие файлов
if [ ! -f "$CHECKSTYLE_JAR" ] || [ ! -f "$CUSTOM_JAR" ]; then
    echo "Ошибка: один из JAR-файлов не найден!"
    exit 1
fi

if [ ! -f "$CONFIG" ]; then
    echo "Ошибка: файл конфигурации $CONFIG не найден!"
    exit 1
fi

# Формируем classpath (разделитель ":" для Linux/macOS, ";" для Windows)
CLASSPATH="$CUSTOM_JAR:$CHECKSTYLE_JAR"

echo "Запуск Checkstyle с кастомными классами проверок..."
# Запускаем главный класс Checkstyle напрямую
java -cp "$CLASSPATH" com.puppycrawl.tools.checkstyle.Main -c "$CONFIG" "$SOURCE_PATH"
```

**Важно:** в вашем XML-конфиге (`my_checks_with_custom.xml`) вы должны указать полное имя вашего класса, например:

```xml
<module name="TreeWalker">
    <module name="com.mycompany.checkstyle.MyCustomCheck"/>
</module>
```

---