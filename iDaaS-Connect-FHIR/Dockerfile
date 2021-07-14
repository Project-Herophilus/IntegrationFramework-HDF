FROM openjdk:8-jdk-alpine
FROM maven:3.5.2-jdk-8-alpine

USER root
RUN mkdir -p /app

COPY pom.xml /app/

COPY src /app/

RUN mvn clean install

CMD ["mvn", "spring-boot:run"]
