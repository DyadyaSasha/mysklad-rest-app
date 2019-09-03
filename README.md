[![Build Status](https://travis-ci.org/DyadyaSasha/mysklad-rest-app.svg?branch=master)](https://travis-ci.org/DyadyaSasha/mysklad-rest-app) [![codecov](https://codecov.io/gh/DyadyaSasha/mysklad-rest-app/branch/master/graph/badge.svg)](https://codecov.io/gh/DyadyaSasha/mysklad-rest-app) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/f96963029d9b442488b00218dca6c266)](https://www.codacy.com/app/DyadyaSasha/mysklad-rest-app?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=DyadyaSasha/mysklad-rest-app&amp;utm_campaign=Badge_Grade)

# mysklad-rest-app
Тестовое задание (компания "МойСклад") 

## Установка
Используйте [maven](https://maven.apache.org/download.cgi) для сборки проекта.<br/>
В корневой папке проекта с pom.xml файлом выполните:

```bash
mvn clean package
```

## Использование
Возможные способы использования:<br/>
1. Запустить в IDE (Eclipse, IDEA) метод main() класса RunServer.
2. В корневой папке проекта с pom.xml файлом выполнить:
```bash
mvn jetty:run
```
3. Разместить .war архив из папки target в любой заранее установленный контейнер сервлетов (например Tomcat - папка webapps).

REST сервисы доступны по следующему URL: http://localhost:8080/app/api.<br/>
Документация swagger доступна по URL: http://localhost:8080/swagger-ui. Там же можно протестировать приложение.

## TODO
- [ ] исправить ошибку, не позволяющую запускать .jar архив

