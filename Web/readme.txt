Build:

mvn package

Run:

java -jar target/KubeServerMain-0.1-jar-with-dependencies.jar

Note:

web-content must be in the same directory as the java command

Build docker image:
docker build -t team12web ./

tag it, then push to Azure
