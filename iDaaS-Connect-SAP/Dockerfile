# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
COPY target/idaas-connect-*.jar /app/target/

# old -JDK 8
# FROM openjdk:8-jdk-alpine
# JDK 11
FROM openjdk:11
COPY --from=build /app/target/idaas-connect-*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]