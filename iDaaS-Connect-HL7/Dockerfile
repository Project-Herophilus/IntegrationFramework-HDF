# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY pom.xml /app/
COPY src /app/src
RUN mvn -f /app/pom.xml clean package

# Run stage
FROM openjdk:8-jdk-alpine
COPY --from=build /app/target/io.connectedhealth_idaas.hl7-4.01.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]