# Stage 1: Builder
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos esenciales para descargar dependencias primero
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Dar permisos de ejecución al wrapper (necesario en alpine/linux) y descargar dependencias
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw dependency:go-offline

# Copiar el código fuente y construir el JAR sin ejecutar tests
COPY src src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiar el JAR generado desde la etapa builder
COPY --from=builder /app/target/*.jar /app.jar

# Exponer el puerto del utility-service
EXPOSE 8080

# Punto de entrada para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "/app.jar"]
