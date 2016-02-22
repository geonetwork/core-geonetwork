#This Dockerfile is not working yet due to problems with the GeoNetwork installer itself.

#to build this docker image run: docker build -t geonetwork-geonetwork -f DockerfileInstaller .
#to run the image run: docker run geonetwork-geonetwork

FROM java:8
MAINTAINER Jeroen Ticheler<jeroen.ticheler@geocat.net>

RUN  export DEBIAN_FRONTEND=noninteractive
ENV  DEBIAN_FRONTEND noninteractive
RUN  dpkg-divert --local --rename --add /sbin/initctl

RUN apt-get -y update

#------ GeoNetwork specific stuff ------

WORKDIR /tmp

RUN if [ ! -f geonetwork.jar ]; then \
	wget -O geonetwork.jar http://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v3.0.3/geonetwork-install-3.0.3-0.jar/download; \
	fi; 
	
RUN	wget -O install.xml https://raw.githubusercontent.com/geonetwork/core-geonetwork/develop/docker/install.xml
	
RUN java -jar geonetwork.jar install.xml	

WORKDIR /opt/geonetwork/bin

ENV GEOSERVER_DATA_DIR="/opt/geonetwork/web/geonetwork/data/geoserver_data"

#CMD "/opt/geonetwork/bin/startup.sh"

EXPOSE 8080
