# Spring Boot 애플리케이션 멀티 스테이지 빌드입니다.
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x ./gradlew

COPY src src

RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG APP_UID=10001
ARG APP_GID=10001

RUN addgroup --system --gid ${APP_GID} spring \
    && adduser --system --uid ${APP_UID} --ingroup spring spring

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/build/libs/*-SNAPSHOT.jar app.jar

RUN chown -R spring:spring /app
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
