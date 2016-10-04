# A Java Application
FROM openjdk:8-jre-alpine

MAINTAINER Benjamin Lei version: 0.1

WORKDIR /
COPY build/libs/dncli-*.jar /dncli.jar

ENTRYPOINT ["java", "-jar", "dncli.jar"]
CMD ["-help"]