FROM java:8

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /web

# Prepare by downloading dependencies
ADD pom.xml /web/pom.xml
ADD Main/pom.xml /web/Main/pom.xml

# Adding source and content, compile and package into a fat jar
ADD Main/src /web/Main/src
ADD Main/web-content /web/Main/web-content

WORKDIR /web/Main

RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]
RUN ["mvn", "package"]

CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/KubeServerMain-0.1-jar-with-dependencies.jar"]