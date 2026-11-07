# syntax=docker/dockerfile:1

# Imagen de la API afiliados.
# Construcción:  docker build -t afiliados-api:local .
# Las imágenes base se fijan por versión exacta y por digest: el mismo Dockerfile
# produce siempre la misma base, y un cambio de versión queda visible en un PR.

# ---------- Etapa 1: compilación (JDK + Maven; no llega a la imagen final) ----------
FROM maven:3.10.0-eclipse-temurin-17-alpine@sha256:ceddb301782ac0b8c4feab8356f15e97c46190e17fd3aa4318317ec0f14adc29 AS build

WORKDIR /build
COPY pom.xml .
COPY src/main ./src/main

# Los tests no corren aquí: Testcontainers necesita Docker y ya se ejecutan en `mvn verify` (local y CI).
# La caché de ~/.m2 acelera las reconstrucciones sin quedar guardada en ninguna capa.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q package -Dmaven.test.skip=true \
    && cp target/afiliados-*.jar /build/app.jar

# ---------- Etapa 2: ejecución (solo JRE + el JAR) ----------
FROM eclipse-temurin:24.0.2_12-jre-alpine@sha256:4044b6c87cb088885bcd0220f7dc7a8a4aab76577605fa471945d2e98270741f

# Usuario de sistema sin privilegios, sin home ni shell de login. UID fijo para poder auditarlo.
RUN addgroup -S -g 10001 afiliados \
    && adduser -S -D -H -u 10001 -G afiliados -s /sbin/nologin afiliados

WORKDIR /app

# El JAR pertenece a root y es de solo lectura: el proceso de la app no puede modificarse a sí mismo.
COPY --from=build --chown=root:root --chmod=0444 /build/app.jar /app/app.jar

USER 10001:10001

EXPOSE 8080

# wget viene en busybox (Alpine): no hace falta instalar curl. start-period cubre el arranque y Flyway.
# Forma exec (JSON): no depende de un shell; wget termina con código != 0 si la app no responde 2xx.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["wget", "-q", "-O", "/dev/null", "http://127.0.0.1:8080/actuator/health"]

# MaxRAMPercentage: la JVM respeta el límite de memoria del contenedor.
# ExitOnOutOfMemoryError: ante un OOM el proceso termina y Docker lo reinicia, en vez de quedar zombi.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
