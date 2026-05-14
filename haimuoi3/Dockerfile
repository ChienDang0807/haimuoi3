# syntax=docker/dockerfile:1
# Spring Boot 3 — Java 17
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY pom.xml .
# Tải dependency vào layer riêng: khi chỉ đổi code trong src, Docker tái sử dụng cache layer này.
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -D spring
USER spring:spring

COPY --from=build /build/target/haimuoi3-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8090

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
