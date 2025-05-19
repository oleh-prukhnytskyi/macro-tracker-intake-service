FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-alpine
COPY --from=build /target/macro-tracker-intake-service-0.0.1-SNAPSHOT.jar macro-tracker-intake-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "macro-tracker-intake-service.jar"]
