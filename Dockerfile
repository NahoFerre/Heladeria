# syntax=docker/dockerfile:1

# 1) Frontend Angular (produccion)
FROM node:22-bookworm-slim AS frontend
WORKDIR /src/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npx ng build --configuration production

# 2) Backend Spring Boot, con el frontend empaquetado en static/ (mismo paso que deploy.ps1)
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /src/backend
COPY backend/ ./
COPY --from=frontend /src/frontend/dist/frontend/browser/ src/main/resources/static/
RUN chmod +x mvnw && ./mvnw -B clean package -DskipTests

# 3) Imagen final liviana, sin Maven ni Node, con usuario no root
FROM eclipse-temurin:21-jre
RUN useradd --system --no-create-home heladeria
WORKDIR /app
COPY --from=backend /src/backend/target/*.jar app.jar
USER heladeria
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
