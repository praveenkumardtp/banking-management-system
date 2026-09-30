# Multi-stage Docker build for Banking Management System
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build production JAR (skip tests during container build)
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /build/target/*.jar app.jar

USER appuser
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
