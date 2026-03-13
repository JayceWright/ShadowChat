# Shadow-Net Terminal (WebSocket Real-Time Chat)

Многопользовательский чат в реальном времени, стилизованный под хакерский терминал. Проект демонстрирует работу с постоянными двунаправленными соединениями через протокол WebSockets.

<img width="1919" height="1079" alt="image" src="https://github.com/user-attachments/assets/efa17f70-1f63-4d7c-bf52-bc744c5d3342" />

## 🛠 Технологический стек
* **Backend:** Java 17+, Maven, `org.java-websocket`.
* **Frontend:** Vanilla JS, Native `WebSocket` API, CSS3.
* **Data Exchange:** Строго типизированный JSON-протокол.

## Архитектурные решения
* **Real-Time Связь:** Мгновенный обмен сообщениями без HTTP-overhead (polling).
* **State Management:** In-Memory хранение истории чата (последние 50 сообщений).
* **Событийная модель:** Поддержка системных уведомлений (подключение/отключение пользователей) и командной строки (например, `/users`, `/clear`).
* **JSON Parsing:** Структурированная передача метаданных (никнейм, таймстамп, тип сообщения).

## Сборка и Запуск
1. Соберите бэкенд с помощью Maven:
   `cd backend`
   `mvn clean package`
2. Запустите скомпилированный `.jar` файл:
   `java -jar target/backend-1.0-SNAPSHOT.jar`
   *(Сервер запустится на порту 8080).*
3. Откройте `frontend/index.html` через Live Server в нескольких вкладках браузера для симуляции мультиплеера.
