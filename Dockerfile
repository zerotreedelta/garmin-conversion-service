FROM maven:3.6.1-jdk-11 AS MAVEN_BUILD
COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package

# /TODO likely grab a base image from ecr
FROM openjdk:11-jdk

COPY --from=MAVEN_BUILD /tmp/target/garmin-conversion-service.jar /app.jar

ENV TZ=America/New_York
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# /TODO don't believe the java.sec prop needed
ENTRYPOINT java -Xmx1024m  -jar /app.jar