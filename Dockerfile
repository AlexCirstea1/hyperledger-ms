#
# Build stage: compile your Spring Boot fat‑JAR
#
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy and build
COPY pom.xml .
COPY src/ src/
RUN mvn clean package -DskipTests

#
# Runtime stage: install tar, wire up entrypoint, drop in the JAR
#
FROM amazoncorretto:21
WORKDIR /app

# Install tar so we can extract certs.tgz at startup
RUN yum install -y tar gzip \
 && yum clean all

# Copy the fat JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy & make executable our entrypoint script
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose your app port
EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=test

# Kick off the entrypoint (which unpacks certs and then starts Java)
ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
