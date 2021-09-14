FROM maven:3.5.2-jdk-8-alpine AS MAVEN_BUILD

COPY pom.xml /build/
COPY src /build/src/

WORKDIR /build/
RUN mvn package

FROM openjdk:8-jre-alpine

WORKDIR /app

COPY --from=MAVEN_BUILD /build/target/io.connectedhealth_idaas.hl7-4.01.jar /app/

ENTRYPOINT ["java", "-jar", "io.connectedhealth_idaas.fhir-4.05.jar"]