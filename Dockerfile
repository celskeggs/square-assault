FROM java:7
MAINTAINER Colby Skeggs
COPY Server.jar /app/Server.jar
COPY launch-server.sh /app/launch-server.sh
COPY Editor/complex.map /app/main.map
WORKDIR /app
EXPOSE 2015
CMD /app/launch-server.sh
