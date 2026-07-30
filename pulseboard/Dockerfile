# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies separately from source so `docker build` doesn't
# re-download everything every time a .java file changes. dependency:resolve
# only pulls the actual compile/runtime/test dependencies (unlike
# dependency:go-offline, which also tries to resolve every reporting/site
# plugin and is a common source of flaky, unrelated failures in Docker builds).
COPY pom.xml .
RUN mvn -B dependency:resolve dependency:resolve-plugins

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]