FROM openjdk:8-jdk-alpine
FROM maven:3.5.2-jdk-8-alpine
USER ROOT

WORKDIR /app

COPY pom.xml ./app

COPY src ./app/src

RUN mvn clean install

CMD ["mvn", "spring-boot:run"]
