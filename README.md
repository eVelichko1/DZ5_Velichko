# DZ5_Velichko — первый автотест (large_form)

Автоматизация заполнения формы [https://the-internet.herokuapp.com/large_form](https://the-internet.herokuapp.com/large_form):
все поля, проверка совпадения пароля и подтверждения, отправка и сверка данных на странице результата.

Стек: Java 21, Maven, Selenium 4 (`RemoteWebDriver`), JUnit 5, AssertJ, WebDriverManager, Log4j2.

## Запуск

Логин и пароль **не зашиты в код** — передаются параметрами:

```bash
mvn test -Dlogin=qa.user -Dpassword=Secret123!
```

Опционально явно задать подтверждение пароля (по умолчанию равно `password`):

```bash
mvn test -Dlogin=qa.user -Dpassword=Secret123! -DpasswordConfirm=Secret123!
```

WebDriver скачивается автоматически (WebDriverManager). Chrome должен быть установлен в системе.

### Selenium Grid / Standalone

```bash
mvn test -Dlogin=qa.user -Dpassword=Secret123! -Dselenium.remote.url=http://localhost:4444
```

Без `selenium.remote.url` тест сам поднимает локальный ChromeDriver и подключается к нему через `RemoteWebDriver`.

## Что проверяет тест

1. Совпадение `password` и `passwordConfirm` до заполнения формы.
2. Заполнение всех полей Account / Details / Preferences / Terms.
3. После Submit на странице «Form Submitted» в таблице `#submitted-data` отображаются введённые значения.

Негативный кейс: при разных пароле и подтверждении выбрасывается ошибка, форма не отправляется.

## Логи

Пишутся одновременно:
- в консоль;
- в файл `logs/autotest.log`.

## Структура

- `driver/WebDriverFactory` — создание `RemoteWebDriver` (локально или Grid)
- `model/FormData` — данные формы и проверка паролей
- `pages/LargeFormPage`, `pages/FormResultPage` — Page Object
- `LargeFormTest` — сценарии
