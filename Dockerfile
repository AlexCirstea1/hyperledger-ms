FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

# Copy and build
COPY pom.xml .
COPY src/ src/
RUN mvn clean package -DskipTests

FROM amazoncorretto:21
COPY --from=build /target/*.jar /app/app.jar

EXPOSE 8082

# Set the active profile
ENV SPRING_PROFILES_ACTIVE=test

ENTRYPOINT ["java","-Dspring.profiles.active=test","-jar","/app/app.jar"]