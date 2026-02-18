## Provision SAP HANA Cloud trial account

Ниже приведены шаги дляProvision SAP Hana Database с использованием пробного аккаунта.

Начнем с создания [временной электронной почты](https://temp-mail.org/en/) для регистрации.

![width=800](hanadb/0.png)

> **Совет:** Не закрывайте это окно, иначе будет сгенерирован новый адрес электронной почты.

Перейдите на [sap.com](https://sap.com/) и перейдите в раздел `products` -> `Trials and Demos`.

![width=800](hanadb/1.png)

Нажмите `Advanced Trials`.

![width=800](hanadb/2.png)

Нажмите `SAP BTP Trial`.

![width=800](hanadb/3.png)

Нажмите `Start your free 90-day trial`.

![width=800](hanadb/4.png)

Вставьте `временной адрес электронной почты`, который мы создали на первом шаге, и нажмите `Next`.

![width=800](hanadb/5.png)

Заполните свои данные и нажмите `Submit`.

![width=800](hanadb/6.png)

Теперь пора проверить входящие сообщения нашего временного почтового аккаунта.

![width=800](hanadb/7.png)

Обратите внимание, что в наш временный почтовый аккаунт пришло письмо.

![width=800](hanadb/8.png)

Откройте письмо и `нажмите для активации` пробного аккаунта.

![width=800](hanadb/9.png)

Вам будет предложено создать `пароль`. Укажите пароль и нажмите `Submit`.

![width=800](hanadb/10.png)

Пробный аккаунт теперь создан. Нажмите, чтобы `начать пробный период`.

![width=800](hanadb/11.png)

Укажите свой номер телефона и нажмите `Continue`.

![width=800](hanadb/13.png)

Мы получаем OTP на указанный номер телефона. Укажите `код` и нажмите `continue`.

![width=800](hanadb/14.png)

Выберите `регион` как `US East (VA) - AWS`.

![width=800](hanadb/15.png)

Нажмите `Continue`.

![width=800](hanadb/16.png)

Пробный аккаунт `SAP BTP` готов. Нажмите `Go to your Trial account`.

![width=800](hanadb/17.png)

Нажмите на `Trial` подаккаунт.

![width=800](hanadb/18.png)

Откройте `Instances and Subscriptions`.

![width=800](hanadb/19.png)

Теперь пора создать подписку. Нажмите кнопку `Create`.

![width=800](hanadb/20.1.png)

При создании подписки выберите `service` как `SAP Hana Cloud` и `Plan` как `tools`, затем нажмите `Create`.

![width=800](hanadb/20.2.png)

Обратите внимание, что подписка на `SAP Hana Cloud` теперь создана. Нажмите `Users` на левой панели.

![width=800](hanadb/21.png)

Выберите имя пользователя (временный адрес электронной почты, который мы указали ранее) и нажмите `Assign Role Collection`.

![width=800](hanadb/22.png)

Поиск `hana` и выберите все 3 коллекции ролей, которые отображаются. Нажмите `Assign Role Collection`.

![width=800](hanadb/23.png)

Наш `пользователь` теперь имеет все 3 коллекции ролей. Нажмите `Instances and Subscriptions`.

![width=800](hanadb/24.png)

Теперь нажмите на приложение `SAP Hana Cloud` в разделе подписок.

![width=800](hanadb/25.png)

Пока нет экземпляров. Давайте нажмем `Create Instance`.

![width=800](hanadb/26.png)

Выберите тип как `SAP HANA Cloud, SAP HANA Database`. Нажмите `Next Step`.

![width=800](hanadb/27.png)

Укажите `Имя экземпляра`, `Описание`, `пароль` для администратора DBADMIN. Выберите последнюю версию `2024.2 (QRC 1/2024)`. Нажмите `Next Step`.

![width=800](hanadb/28.png)

Оставьте все по умолчанию. Нажмите `Next Step`.

![width=800](hanadb/29.png)

Нажмите `Next Step`.

![width=800](hanadb/30.png)

Выберите `Allow all IP addresses` и нажмите `Next Step`.

![width=800](hanadb/31.png)

Нажмите `Review and Create`.

![width=800](hanadb/32.png)

Нажмите `Create Instance`.

![width=800](hanadb/33.png)

Обратите внимание, что началосьProvision SAP Hana Database экземпляра. Это займет некоторое время, пожалуйста, наберитесь терпения.

![width=800](hanadb/34.1.png)

Как только экземпляр будетProvision (статус отображается как `Running`), мы можем получить URL источника данных (`SQL Endpoint`), нажав на экземпляр и выбрав `Connections`.

![width=800](hanadb/34.2.png)

Мы переходим в `SAP Hana Database Explorer`, нажав на `...`.

![width=800](hanadb/35.png)

Укажите учетные данные администратора и нажмите `OK`.

![width=800](hanadb/36.png)

Откройте SQL консоль и создайте таблицу `CRICKET_WORLD_CUP` с помощью следующего DDL-запроса:
[sql]
```
CREATE TABLE CRICKET_WORLD_CUP (
    _ID VARCHAR2(255) PRIMARY KEY,
    CONTENT CLOB,
    EMBEDDING REAL_VECTOR(1536)
)
```

![width=800](hanadb/37.png)

Перейдите в `hana_dev_db -> Catalog -> Tables`, чтобы найти нашу таблицу `CRICKET_WORLD_CUP`.

![width=800](hanadb/38.png)

Щелкните правой кнопкой мыши на таблице и нажмите `Open Data`.

![width=800](hanadb/39.png)

Обратите внимание, что данные таблицы теперь отображаются. В таблице нет строк, так как мы еще не создали никаких встраиваний.

![width=800](hanadb/40.png)

Следующие шаги: xref:api/vectordbs/hana.adoc[SAP Hana Vector Engine]
