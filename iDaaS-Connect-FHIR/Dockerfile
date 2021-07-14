FROM openjdk:8-jdk-alpine
FROM maven:3.5.2-jdk-8-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY pom.xml ./

COPY src ./src

RUN mvn clean install

CMD ["mvn", "spring-boot:run"]
