# /TODO likely grab a base image from ecr
FROM openjdk:11-jdk

ENV TZ=America/New_York
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8

# Add the service itself - latest version of the spotify plugin allows for a build argument to pass this
#ARG JAR_FILE
#ADD ${JAR_FILE} /app.jar

ADD garmin-conversion-service.jar /app.jar

# /TODO don't believe the java.sec prop needed
ENTRYPOINT java -Xmx1024m  -jar /app.jar