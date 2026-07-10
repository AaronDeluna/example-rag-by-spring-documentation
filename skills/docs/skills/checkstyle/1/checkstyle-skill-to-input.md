Скилл должен запускать проверку Checkstyle через bash без подключения зависимости Checkstyle к проекту.
Скрипты и кастомные проверки прдеставлены ниже.
Скрипт с кастомными простыми проверками (без написания Java-кода)

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