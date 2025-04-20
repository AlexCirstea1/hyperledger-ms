#
# Build stage
#
FROM maven:3.9.6-amazoncorretto-21 AS build
WORKDIR /app

COPY ./src src/
COPY ./pom.xml pom.xml
RUN mvn clean package

#
# Package stage
#
FROM amazoncorretto:21
COPY --from=build /target/*.jar /app/app.jar

# Copy in the cert bundle and connection profiles
COPY --from=builder /home/alex/fabric-mini/vars/certs.tgz  /opt/fabric/certs.tgz
COPY --from=builder /home/alex/fabric-mini/vars/profiles     /opt/fabric/profiles

# Unpack “keyfiles/…” into the organizations tree
RUN mkdir -p /opt/fabric/organizations \
    && tar -xzf /opt/fabric/certs.tgz \
         --strip-components=1 \
         -C /opt/fabric/organizations \
         keyfiles

EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=test

ENTRYPOINT ["java","-Dspring.profiles.active=test","-jar","/app/app.jar"]