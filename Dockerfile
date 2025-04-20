#
# Build stage: compile your Spring Boot JAR
#
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy source & POM
COPY pom.xml .
COPY src/ src/

# Build
RUN mvn clean package -DskipTests

#
# Runtime stage: package the JAR and the entrypoint script
#
FROM amazoncorretto:21
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the entrypoint that will unpack certs.tgz at runtime
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose your application port
EXPOSE 8082

# Use the test profile by default
ENV SPRING_PROFILES_ACTIVE=test

# Launch via our entrypoint wrapper
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
