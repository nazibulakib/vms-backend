FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress verify

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/vms-backend-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
