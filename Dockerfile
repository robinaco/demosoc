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

RUN apk add --no-cache curl
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