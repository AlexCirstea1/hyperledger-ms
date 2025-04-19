#
# Build stage
#
FROM maven:3.9.6-amazoncorretto-21 AS build
COPY ./src src/
COPY ./pom.xml pom.xml
RUN mvn clean package

#
# Package stage
#
FROM amazoncorretto:21
COPY --from=build /target/*.jar /app/app.jar

EXPOSE 8082

# Set the active profile
ENV SPRING_PROFILES_ACTIVE=test

ENTRYPOINT ["java","-Dspring.profiles.active=test","-jar","/app/app.jar"]