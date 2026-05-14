# Dockerfile for the user-service (monolith) — Spring Boot 3 on Java 17
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
# Build the application JAR, skipping tests for faster Docker builds
RUN chmod +x gradlew && ./gradlew bootJar -x test -x spotlessJava -x spotlessJavaCheck --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# Health check using the actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
