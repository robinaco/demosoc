# Dockerfile - Multi-stage build para seguridad
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
# Primero copiar gradle wrapper para cache
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew
# Descargar dependencias (cacheable)
RUN ./gradlew dependencies --no-daemon
# Copiar código fuente y construir
COPY src src
RUN ./gradlew build -x test --no-daemon

# Etapa runtime - imagen más pequeña y segura
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
# Copiar JAR desde builder
COPY --from=builder /app/build/libs/*.jar app.jar
# Cambiar a usuario no-root
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]