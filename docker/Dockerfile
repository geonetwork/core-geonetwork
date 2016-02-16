FROM tomcat:8.0
MAINTAINER Jeroen Ticheler<jeroen.ticheler@geocat.net>

RUN  export DEBIAN_FRONTEND=noninteractive
ENV  DEBIAN_FRONTEND noninteractive
RUN  dpkg-divert --local --rename --add /sbin/initctl

#ADD 71-apt-cacher-ng /etc/apt/apt.conf.d/71-apt-cacher-ng

RUN apt-get -y update

#------ GeoNetwork specific stuff ------

WORKDIR /usr/local/tomcat/webapps

RUN if [ ! -f geonetwork.war ]; then \
	wget -O geonetwork.war http://sourceforge.net/projects/geonetwork/files/GeoNetwork_opensource/v3.0.3/geonetwork.war/download; \
	fi; 
	
WORKDIR /usr/local/tomcat

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -Xms48m -Xss2M -XX:MaxPermSize=512m -XX:+UseConcMarkSweepGC"

CMD ["catalina.sh", "run"]

EXPOSE 8080
