FROM openjdk:8-jre-slim

COPY app/ /app

EXPOSE 8080

WORKDIR /app
ENTRYPOINT ["java", "-jar", "/app/foilen-infra-ui.jar"]
