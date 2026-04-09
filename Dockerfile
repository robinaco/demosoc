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



# Dockerfile - Multi-stage build para seguridad
#FROM gradle:8.5-jdk17 AS builder
#WORKDIR /app
#
## Copiar archivos de Gradle primero (para cache)
#COPY gradlew .
#COPY gradle gradle
#COPY build.gradle .
#COPY settings.gradle .
#
## Dar permisos y construir dependencias
#RUN chmod +x gradlew
#RUN ./gradlew dependencies --no-daemon
#
## Copiar código fuente
#COPY src src
#
## Construir la aplicación (excluyendo pruebas y tareas de cobertura)
#RUN ./gradlew build -x test -x jacocoTestReport -x jacocoTestCoverageVerification -x weightedCoverageCheck --no-daemon
#
## Etapa runtime - imagen ligera y segura
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
#
## Crear directorio temporal y usuario no-root
#RUN mkdir -p /app/temp && \
#    addgroup -S appgroup && \
#    adduser -S appuser -G appgroup && \
#    chown -R appuser:appgroup /app
#
## Copiar JAR desde builder
#COPY --from=builder /app/build/libs/*.jar app.jar
#
## Cambiar a usuario no-root
## USER appuser
#
## Configurar directorio temporal para Tomcat/Spring Boot
#ENV JAVA_OPTS="-Djava.io.tmpdir=/tmp"
#
#EXPOSE 8080
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew build -x test -x jacocoTestReport -x jacocoTestCoverageVerification -x weightedCoverageCheck --no-daemon

# Etapa runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear directorios necesarios
RUN mkdir -p /temp && \
    addgroup -S appgroup && \
    adduser -S appuser -G appgroup && \
    chown -R appuser:appgroup /app /temp

COPY --from=builder /app/build/libs/*.jar /app/app.jar

USER appuser

ENV JAVA_OPTS="-Djava.io.tmpdir=/temp"
ENV SERVER_PORT=80

EXPOSE 80

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]