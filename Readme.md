# Проект сервиса аренды транспорта simbir.go
Задача поставлена олимпиадой **volga-it.org 2023** в рамках полуфинального этапа квалификации "BackEnd разработка"
>[Swagger UI](http://localhost:8081/swagger-ui/index.html) является модулем проекта и работает на его локальном сервере. 
## Требования:
* java 19
* Postgres 15

## Сборка проекта:
`./gradlew bootJar`

## Конфигурация БД:
1. Проверить конфигурацию системы, в переменной среды `path` должен находиться путь к директории с PostgreSQL/bin
2. `psql -U postgres`
3. Ввод пароля суперпользователя БД
4. Ввод запроса из файла: `src/main/resources/db/create_db.sql`
5. подключение к созданной БД: `\c volgait`
6. Поочередный ввод команд из следующих файлов:
   `src/main/resources/db/migrate_db.sql`\
   `src/main/resources/db/populate_db.sql`

### Существующие пользователи (login/password):
- user/user
- admin/admin

## Запуск проекта:
1. `cd build/libs/`
2. `java -jar volgait-1.0.jar`

