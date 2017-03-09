# A Java Application
FROM openjdk:8-jre-alpine

MAINTAINER Benjamin Lei version: 0.2

WORKDIR /
COPY build/libs/dncli-*.jar /dncli.jar

# hopefully no one really needs more than this
ENTRYPOINT ["java", "-Xms1024m", "-Xmx1024m", "-jar", "/dncli.jar"]
CMD ["-help"]
