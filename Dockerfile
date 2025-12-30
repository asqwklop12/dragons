# ==========================
# 1) Build stage
# ==========================
FROM --platform=linux/amd64 eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

# Copy Gradle wrapper and settings first (better layer caching)
COPY gradlew settings.gradle build.gradle /workspace/
COPY gradle /workspace/gradle

# Copy module build files
COPY apps/dragons_api/build.gradle /workspace/apps/dragons_api/build.gradle
COPY modules /workspace/modules
COPY supports /workspace/supports

# Copy sources last
COPY apps /workspace/apps

# Build only the bootable jar for apps:dragons_api (skip tests for faster image build)
RUN chmod +x ./gradlew && ./gradlew :apps:dragons_api:bootJar -x test --no-daemon

# ==========================
# 2) Runtime stage
# ==========================
FROM --platform=linux/amd64 eclipse-temurin:21-jre
WORKDIR /app

# Copy the Spring Boot fat jar from builder
COPY --from=builder /workspace/apps/dragons_api/build/libs/*.jar /app/app.jar

# Expose the default dev port (overridden by profile/env if needed)
EXPOSE 8083

# JVM options can be tuned via JAVA_OPTS env var
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

