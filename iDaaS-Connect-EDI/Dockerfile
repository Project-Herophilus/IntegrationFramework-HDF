# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY target/io.connectedhealth_idaas.fhir-4.05.jar /app/target/

FROM openjdk:8-jdk-alpine
COPY --from=build /app/target/io.connectedhealth_idaas.fhir-4.05.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]