# ---- build stage ----
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# Copy only Gradle wrapper and build files first (better layer caching)
COPY demo/gradlew demo/gradlew.bat demo/settings.gradle demo/build.gradle ./demo/
COPY demo/gradle ./demo/gradle

# Download dependencies (leverages caching)
WORKDIR /app/demo
RUN gradle dependencies --no-daemon || true

# Copy the rest of the source
WORKDIR /app
COPY demo ./demo

# Build jar (skip tests for faster CI builds; adjust as needed)
WORKDIR /app/demo
RUN gradle -x test bootJar --no-daemon

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy fat jar from build stage
COPY --from=build /app/demo/build/libs/*.jar app.jar

# Render may set PORT; Spring Boot will read server.port from env/property
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

