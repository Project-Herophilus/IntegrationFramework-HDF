FROM openjdk:8-jdk-alpine
FROM maven:3.5.2-jdk-8-alpine
USER root
RUN mkdir -p /app
# Expose port to listen to
EXPOSE 8080

WORKDIR /app

COPY pom.xml ./

COPY src ./src

RUN mvn clean install

CMD ["mvn", "spring-boot:run"]
