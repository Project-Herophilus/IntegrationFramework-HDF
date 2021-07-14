FROM openjdk:8-jdk-alpine
FROM maven:3.5.2-jdk-8-alpine

COPY src /app/

COPY pom.xml /tmp/pom.xml
RUN mvn -B -f /tmp/pom.xml -s /usr/share/maven/ref/settings-docker.xml dependency:resolve

WORKDIR /app

CMD ["mvn", "spring-boot:run"]
