# ---- Etapa 1: compilar ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- Etapa 2: ejecutar (imagen liviana) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/centro-salud-api-1.0.0.jar app.jar
# Memoria acotada para planes gratuitos de 512 MB
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
