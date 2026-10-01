FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/rideshare-rest-graphQL-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]