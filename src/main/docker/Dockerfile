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

ADD newrelic /
ADD aws-ses-enrollment-service.jar /app.jar

RUN rm -f keystore.jks && keytool -genkeypair -alias server \
       -keyalg RSA -keysize 2048 \
       -keystore keystore.p12 \
       -storepass changeit \
       -keypass changeit \
       -dname "CN=LFG, O=Digital, L=Philadelphia, ST=PA, C=US"

# /TODO don't believe the java.sec prop needed
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Xmx256m -javaagent:newrelic-agent.jar -jar /app.jar