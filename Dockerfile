FROM ubuntu:22.04

RUN apt-get update && apt-get install -y openjdk-17-jdk

WORKDIR /mypastebin

COPY target/mypastebin_backend-0.0.1-SNAPSHOT.jar /mypastebin/mypastebin_backend.jar

ENV SERVER_PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "mypastebin_backend.jar"]