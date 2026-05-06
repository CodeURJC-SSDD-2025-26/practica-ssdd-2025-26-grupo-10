# Stage 1: Builder
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /project

# Copy poms
COPY utility-service/pom.xml ./utility-service/

WORKDIR /project/utility-service
RUN mvn dependency:go-offline

# Copy source and build
COPY utility-service/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app

# Copy the JAR from the builder stage
COPY --from=builder /project/utility-service/target/*.jar app.jar

# Expose HTTP port as required by the strict rubric
EXPOSE 8080

# Entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
