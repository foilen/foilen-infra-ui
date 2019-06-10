FROM openjdk:11-jre-slim

RUN export TERM=dumb ; \
  apt-get update && apt-get install -y \
    curl \
    haproxy supervisor \
  && apt-get clean && rm -rf /var/lib/apt/lists/*

COPY app/ /app

EXPOSE 8080

WORKDIR /app
ENTRYPOINT ["/app/bin/foilen-infra-ui"]
