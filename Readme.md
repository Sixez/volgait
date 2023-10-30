# Проект сервиса аренды транспорта simbir.go
Задача поставлена олимпиадой volga-it.org 2023 в рамках полуфинального этапа квалификации BackEnd разработка

## Требования:
- java 19
- Postgres 15

## Сборка проекта:
1. ./gradlew bootJar

## Конфигурация БД:
1. Проверить конфигурацию системы, в переменной среды path должен находиться путь к директории с PostgreSQL/bin
2. psql -U postgres
3. Ввод пароля суперпользователя БД
4. Ввод запроса из файла: _src/main/resources/db/create_db.sql_
5. подключение к созданной БД: \c volgait
6. Поочередный ввод команд из следующих файлов:
   1. _src/main/resources/db/migrate_db.sql_
   2. _src/main/resources/db/populate_db.sql_

## Запуск проекта:
1. cd build/libs/
2. java -jar volgait-1.0.jar

### Существующие пользователи (login/password):
- user/user
- admin/admin

## [Swagger UI (URL)](http://localhost:8081/swagger-ui/index.html)