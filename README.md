# HTTP Cats — модуль 9

У цьому домашньому завданні я зробив невелику консольну програму для роботи із сайтом [http.cat](https://http.cat). Вона отримує посилання на картинку за HTTP-кодом, завантажує її та зберігає в папку проєкту.

Використав стандартний `HttpClient` із Java, тому додаткові бібліотеки для HTTP не потрібні. Проєкт зібраний на Gradle.

## Що зроблено

- `HttpStatusChecker` — метод `getStatusImage(int code)` відправляє GET на `https://http.cat/<CODE>.jpg`. Якщо сервер відповів `200`, повертає посилання; якщо `404` — викидає `HttpStatusImageNotFoundException`.
- `HttpStatusImageDownloader` — метод `downloadStatusImage(int code)` спочатку перевіряє посилання через `HttpStatusChecker`, потім завантажує байти картинки в `<CODE>.jpg`.
- `HttpImageStatusCli` — метод `askStatus()` запитує число в консолі й викликає downloader. Якщо ввели текст, показує `Please enter valid number`. Якщо картинки немає, показує `There is not image for HTTP status <CODE>`.

Також додав `HttpCatException` для помилок мережі/збереження і окремий `HttpStatusImageNotFoundException` для випадку, коли картинки немає. Це потрібно, щоб не плутати відповідь `404` із проблемами інтернету.

## Запуск

Відкрити проєкт в IntelliJ IDEA та дочекатися Gradle Sync. У PowerShell у папці проєкту:

```powershell
.\gradlew.bat run --console=plain
```

У відповідь на `Enter HTTP status code` ввести, наприклад, `200`. Картинка `200.jpg` збережеться в корені проєкту. Для `10000` буде повідомлення, що картинки немає. Якщо ввести `test`, отримаємо `Please enter valid number`.

Можна запускати `HttpImageStatusCli.main()` безпосередньо з IntelliJ — результат такий самий.

## Перевірка

```powershell
.\gradlew.bat clean check
```

У проєкті є локальна перевірка `HttpCatSelfTest`. Вона піднімає маленький тестовий HTTP-сервер, щоб перевірити класи навіть без доступу до `http.cat`. Можна запустити її окремо:

```powershell
.\gradlew.bat selfTest
```

Перевіряється доступний код, відсутня картинка, помилка сервера, збереження байтів файлу та текст повідомлень CLI. Реальний сайт `http.cat` потрібно перевірити окремо запуском `run`.

Завантажені картинки додані в `.gitignore`, тому випадково не потраплять у GitHub-репозиторій.
