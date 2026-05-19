# Dockerfile for the main monolith service (user-service after extraction).
# Multi-stage build: Gradle build stage + lightweight JRE runtime stage.
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY src src
# Build the application, skipping checks that require full environment
RUN chmod +x gradlew && \
    ./gradlew build -x spotlessJava -x spotlessJavaCheck \
    -x jacocoTestCoverageVerification -x test --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
# Health check using Spring Boot Actuator endpoint
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
