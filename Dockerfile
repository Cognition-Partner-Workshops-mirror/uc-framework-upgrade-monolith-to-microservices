# User Service (Monolith) Dockerfile — Spring Boot 3.2.4 on Java 17
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from Gradle build output
COPY build/libs/spring-boot-realworld-example-app-0.0.1-SNAPSHOT.jar app.jar

# Expose the monolith/user-service port
EXPOSE 8080

# Health check using the actuator endpoint
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
