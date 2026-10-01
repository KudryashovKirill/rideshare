FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ride-grpc-client-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]