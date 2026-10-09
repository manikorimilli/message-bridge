# Build stage: compile and package the app with the Maven wrapper.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src src
RUN ./mvnw -q package -DskipTests

# Run stage: only the JRE and the jar, running as a non-root user.
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home app
COPY --from=build /app/target/message-bridge-*.jar app.jar
USER app
EXPOSE 8080
# Let the JVM size its heap from the container's memory limit (512 MB on Render's free plan).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
