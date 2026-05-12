# =========================================================
# Stage 1: Build
# =========================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia POM e baixa dependncias (cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia cdigo e empacota
COPY src ./src
RUN mvn clean package -DskipTests -B

# =========================================================
# Stage 2: Runtime
# =========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usurio no-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
