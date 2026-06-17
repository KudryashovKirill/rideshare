FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ride-grpc-server-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]