### runtime image
FROM openjdk:13-jdk-alpine
COPY "target/vodafone-api-server-*.jar" vodafone-api-server.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "vodafone-api-server.jar"]
