[[getting-started]]
# Начало работы

Этот раздел предлагает отправные точки для того, как начать использовать Spring AI.

Вы должны следовать шагам в каждом из следующих разделов в зависимости от ваших потребностей.

> **Примечание:** Spring AI поддерживает Spring Boot 3.4.x и 3.5.x.

[[spring-initializr]]
## Spring Initializr

Перейдите на https://start.spring.io/[start.spring.io] и выберите AI модели и векторные хранилища, которые вы хотите использовать в своих новых приложениях.

[[artifact-repositories]]
## Репозитории артефактов

### Релизы - Используйте Maven Central

Spring AI 1.0.0 и более поздние версии доступны в Maven Central. 
Дополнительная конфигурация репозитория не требуется. Просто убедитесь, что у вас включен Maven Central в вашем файле сборки.

[tabs]
======
Maven::
+
```xml,indent=0,subs="verbatim,quotes"
<!-- Maven Central включен по умолчанию в сборки Maven.
     Обычно вам не нужно настраивать его явно,
     но он показан здесь для ясности. -->
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

Gradle::
+
```groovy,indent=0,subs="verbatim,quotes"
repositories {
    mavenCentral()
}
```
======


### Снапшоты - Добавьте репозитории для снапшотов

Чтобы использовать последние версии разработки (например, `1.1.0-SNAPSHOT`) или более старые версии milestone до 1.0.0, вам нужно добавить следующие репозитории для снапшотов в ваш файл сборки.

Добавьте следующие определения репозиториев в ваш файл сборки Maven или Gradle:

[tabs]
======
Maven::
+
```xml,indent=0,subs="verbatim,quotes"
  <repositories>
    <repository>
      <id>spring-snapshots</id>
      <name>Spring Snapshots</name>
      <url>https://repo.spring.io/snapshot</url>
      <releases>
        <enabled>false</enabled>
      </releases>
    </repository>
    <repository>
      <name>Central Portal Snapshots</name>
      <id>central-portal-snapshots</id>
      <url>https://central.sonatype.com/repository/maven-snapshots/</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```

Gradle::
+
```groovy,indent=0,subs="verbatim,quotes"
repositories {
  mavenCentral()
  maven { url 'https://repo.spring.io/milestone' }
  maven { url 'https://repo.spring.io/snapshot' }
  maven {
    name = 'Central Portal Snapshots'
    url = 'https://central.sonatype.com/repository/maven-snapshots/'
  }  
}
```
======

**ПРИМЕЧАНИЕ:** При использовании Maven с снапшотами Spring AI обратите внимание на вашу конфигурацию зеркала Maven. Если вы настроили зеркало в вашем `settings.xml` следующим образом:

```xml
<mirror>
    <id>my-mirror</id>
    <mirrorOf>*</mirrorOf>
    <url>https://my-company-repository.com/maven</url>
</mirror>
```

Знак подстановки `*` перенаправит все запросы к репозиториям на ваше зеркало, что предотвратит доступ к репозиториям снапшотов Spring. Чтобы исправить это, измените конфигурацию `mirrorOf`, чтобы исключить репозитории Spring:

```xml
<mirror>
    <id>my-mirror</id>
    <mirrorOf>*,!spring-snapshots,!central-portal-snapshots</mirrorOf>
    <url>https://my-company-repository.com/maven</url>
</mirror>
```

Эта конфигурация позволяет Maven получать доступ к репозиториям снапшотов Spring напрямую, при этом используя ваше зеркало для других зависимостей.

[[dependency-management]]
## Управление зависимостямиThe Spring AI Bill of Materials (BOM) объявляет рекомендуемые версии всех зависимостей, используемых в конкретном релизе Spring AI. Это версия только для BOM, и она содержит только управление зависимостями без объявлений плагинов или прямых ссылок на Spring или Spring Boot. Вы можете использовать родительский POM Spring Boot или использовать BOM от Spring Boot (`spring-boot-dependencies`) для управления версиями Spring Boot.

Добавьте BOM в ваш проект:

[tabs]
======
Maven::
+
```xml,indent=0,subs="verbatim,quotes"
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Gradle::
+
```groovy,indent=0,subs="verbatim,quotes"
dependencies {
  implementation platform("org.springframework.ai:spring-ai-bom:1.0.0")
  // Замените следующее на конкретные зависимости модулей (например, spring-ai-openai) или стартер-модули (например, spring-ai-starter-model-openai), которые вы хотите использовать
  implementation 'org.springframework.ai:spring-ai-openai'
}
```
+
Пользователи Gradle также могут использовать BOM Spring AI, используя нативную поддержку Gradle (5.0+) для объявления ограничений зависимостей с помощью Maven BOM. Это реализуется добавлением метода обработчика зависимости 'platform' в секцию зависимостей вашего скрипта сборки Gradle.
======

[[add-dependencies]]
## Добавьте зависимости для конкретных компонентов

Каждый из следующих разделов в документации показывает, какие зависимости вам нужно добавить в вашу систему сборки проекта.

- xref:api/chatmodel.adoc[Модели чата]
- xref:api/embeddings.adoc[Модели встраивания]
- xref:api/imageclient.adoc[Модели генерации изображений]
- xref:api/audio/transcriptions.adoc[Модели транскрипции]
- xref:api/audio/speech.adoc[Модели синтеза речи (TTS)]
- xref:api/vectordbs.adoc[Векторные базы данных]

## Примеры Spring AI

Пожалуйста, обратитесь к https://github.com/spring-ai-community/awesome-spring-ai[этой странице] для получения дополнительных ресурсов и примеров, связанных со Spring AI.
