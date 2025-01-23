FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/web-application-1.0.jar app/web-application-1.0.jar
COPY scripts /app/scripts

ENTRYPOINT ["java", "-jar", "app/web-application-1.0.jar", "--init", "--seed"]
