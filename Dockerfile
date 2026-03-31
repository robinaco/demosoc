# Dockerfile - Multi-stage build para seguridad
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Copiar archivos de Gradle primero (para cache)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Dar permisos y construir dependencias
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copiar código fuente
COPY src src

# Construir la aplicación (excluyendo pruebas y tareas de cobertura)
RUN ./gradlew build -x test -x jacocoTestReport -x jacocoTestCoverageVerification -x weightedCoverageCheck --no-daemon

# Etapa runtime - imagen ligera y segura
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar JAR desde builder
COPY --from=builder /app/build/libs/*.jar app.jar

# Cambiar a usuario no-roots
USER appuser


EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]