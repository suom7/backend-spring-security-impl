# Technologies

1. Spring Boot - https://projects.spring.io/spring-boot/
2. MyBatis - http://www.mybatis.org/mybatis-3/ | http://www.mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/index.html
3. Flyway - https://flywaydb.org/
4. Lombok- https://projectlombok.org/
5. Swagger - http://swagger.io/
6. Docker - https://www.docker.com/

# Run
```
$ ./gradlew bootRun
```
# Create Flyway Patch
```
$ ./gradlew createFlywayPatch
```
# Build Docker
```
$ ./gradlew clean buildDocker
```
# Swagger - API document

UI : http://localhost:8080/swagger-ui.html

JSON : http://localhost:8080/v2/api-docs

# Initialize DB 

DROP DATABASE IF EXISTS pos;
CREATE DATABASE pos CHARACTER SET 'utf8' COLLATE 'utf8_general_ci';
USE pos;

DROP USER 'pos'@'localhost';
CREATE USER 'pos'@'localhost' IDENTIFIED BY 'DrQi3kclsqO4v';
GRANT  ALL PRIVILEGES ON pos.* TO 'pos'@'localhost';

# Banner

> http://patorjk.com/software/taag/#p=display&f=Ogre&t=Sovanndara%20

# User Login

> POST : http://localhost:8080/oauth/token
