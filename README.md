# LibreODT Reader — Проект Android APK

Готовый оффлайн-проект для компиляции Android-приложения **.apk** с движком рендеринга **LibreOffice Writer (.odt)**.

## Что внутри архива
- `app/src/main/assets/www/index.html` — автономный ODT-ридер без внешних CDN.
- `app/src/main/assets/www/libs/jszip.min.js` — локальная библиотека JSZip.
- `app/src/main/assets/www/libs/styles.css` — все скомпилированные стили интерфейса и листа LibreOffice.
- `app/src/main/java/com/libreodt/reader/MainActivity.kt` — нативный WebView с системным выбором файлов и поддержкой открытия `.odt` напрямую из Telegram, почты и Файлового менеджера.
- `.github/workflows/build-apk.yml` — готовый сценарий облачной сборки APK.

---

## Способ 1: Собрать APK через Android Studio (на ПК)
1. Распакуйте архив в отдельную папку.
2. Откройте папку в **Android Studio** (File -> Open).
3. Дождитесь завершения синхронизации Gradle.
4. Нажмите **Build -> Build Bundle(s) / APK(s) -> Build APK(s)**.
5. Готовый файл появится в `app/build/outputs/apk/debug/app-debug.apk`.

## Способ 2: Собрать APK через командную строку (Gradle 8.7 + JDK 17)
```bash
gradle assembleDebug
```

## Способ 3: Собрать APK в облаке через GitHub (даже с телефона!)
1. Создайте новый репозиторий на GitHub и загрузите туда распакованные файлы.
2. Перейдите во вкладку **Actions -> Build Android APK -> Run workflow**.
3. Через 2 минуты скачайте готовый `app-debug.apk` из раздела **Artifacts**.
