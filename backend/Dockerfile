# -----------------------------------------------------------------------------
# SIGER - Backend Dockerfile (multi-stage)
# Build: Maven 3.9 + JDK 21 -> Runtime: Eclipse Temurin JRE 21
# -----------------------------------------------------------------------------

# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias primero para aprovechar cache
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copiar código fuente y compilar
COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Instalar curl y netcat para healthcheck/wait
RUN apt-get update && apt-get install -y --no-install-recommends curl netcat-openbsd && rm -rf /var/lib/apt/lists/*

# Usuario no-root
RUN groupadd -r siger && useradd -r -g siger siger

# Copiar JAR generado
COPY --from=build /app/target/*.jar app.jar
COPY wait-for.sh /app/wait-for.sh

# Permisos
RUN chmod +x /app/wait-for.sh && chown -R siger:siger /app
USER siger

# Exponer puerto
EXPOSE 8080

# actuator/health
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=5 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Arranque con espera a DB
ENTRYPOINT sh -c '/app/wait-for.sh ${DB_HOST:-db}:${MYSQL_PORT:-3306} -- java -jar /app/app.jar'
