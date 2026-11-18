# ==============================
# BUILD - MAVEN + JAVA 21
# ==============================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B \
    && find target -maxdepth 1 -name "*.jar" \
       ! -name "*.original" \
       ! -name "*-sources.jar" \
       ! -name "*-javadoc.jar" \
       -exec cp {} /build/app.jar \;

# ==============================
# RUNTIME - JAVA 21
# ==============================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring \
    && adduser -S spring -G spring

COPY --from=build --chown=spring:spring \
    /build/app.jar /app/app.jar

USER spring
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70.0 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
