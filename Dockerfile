FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy and build
COPY pom.xml .
COPY src/ src/
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
WORKDIR /app

# Copy the fat JAR
COPY --from=build /app/target/*.jar app.jar

# Copy & make executable our entrypoint
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

# Expose port
EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=test

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]