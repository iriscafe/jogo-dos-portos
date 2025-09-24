# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev

COPY --from=builder /workspace/target/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["/bin/sh","-c","java $JAVA_OPTS -jar /app/app.jar"]


