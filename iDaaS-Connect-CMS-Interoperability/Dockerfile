# Build stage
FROM maven:3.6.0-jdk-11-slim AS build
#COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN mvn clean install
COPY *.jar /app/target/
