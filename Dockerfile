# Dockerfile for the main app (user-service / monolith)
# Multi-stage build: Gradle build -> slim JRE runtime
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test -x spotlessJava -x spotlessJavaCheck -x jacocoTestCoverageVerification --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
# Health check using actuator endpoint
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
