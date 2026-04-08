# # Dockerfile - Multi-stage build para seguridad
# FROM gradle:8.5-jdk17 AS builder
# WORKDIR /app

# # Copiar archivos de Gradle primero (para cache)
# COPY gradlew .
# COPY gradle gradle
# COPY build.gradle .
# COPY settings.gradle .

# # Dar permisos y construir dependencias
# RUN chmod +x gradlew
# RUN ./gradlew dependencies --no-daemon

# # Copiar código fuente
# COPY src src

# # Construir la aplicación (excluyendo pruebas y tareas de cobertura)
# RUN ./gradlew build -x test -x jacocoTestReport -x jacocoTestCoverageVerification -x weightedCoverageCheck --no-daemon

# # Etapa runtime - imagen ligera y segura
# FROM eclipse-temurin:17-jre-alpine
# WORKDIR /app

# # Crear usuario no-root
# RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# # Copiar JAR desde builder
# COPY --from=builder /app/build/libs/*.jar app.jar

# # Cambiar a usuario no-root
# USER appuser

# EXPOSE 80
# ENTRYPOINT ["java", "-jar", "app.jar"]

# Etapa de construcción
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew build -x test --no-daemon

# Etapa de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear directorio para archivos temporales con permisos
RUN mkdir -p /app/temp && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app

COPY --from=builder /app/build/libs/*.jar app.jar

USER appuser

# Configurar directorio temporal
ENV JAVA_OPTS="-Djava.io.tmpdir=/app/temp"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]