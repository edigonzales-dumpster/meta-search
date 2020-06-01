FROM adoptopenjdk/openjdk8:latest

RUN apt-get update && \
    apt-get install -y curl

EXPOSE 8080

WORKDIR /home/meta

ARG JAR_FILE
COPY ${JAR_FILE} /home/meta/app.jar
RUN chown -R 1001:0 /home/meta && \
    chmod -R g=u /home/meta

USER 1001

ENTRYPOINT ["java","-XX:MaxRAMPercentage=80.0","-Djava.security.egd=file:/dev/./urandom","-jar","/home/meta/app.jar"]

HEALTHCHECK --interval=30s --timeout=30s --start-period=60s CMD curl http://localhost:8080/actuator/health