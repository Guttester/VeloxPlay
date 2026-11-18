FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN mvn -q -DskipTests dependency:go-offline || true
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd -r -u 1001 velox
COPY --from=build /app/target/*.jar app.jar
USER velox
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
