FROM maven:3.6.1-jdk-8 AS lala
LABEL maintainer="Ob Rzwo <obr2pd@gmail.com>"
ARG HTTP_PROXY
ENV HTTP_PROXY ${HTTP_PROXY}
ARG HTTPS_PROXY
ENV HTTPS_PROXY ${HTTPS_PROXY}
ARG NO_PROXY
ENV NO_PROXY ${NO_PROXY}
ENV http_proxy ${HTTP_PROXY}
ENV https_proxy ${HTTPS_PROXY}
ENV no_proxy ${NO_PROXY}
ARG GIT_CLONE
ENV GIT_CLONE ${GIT_CLONE}
ARG MAVEN_OPTS
ENV MAVEN_OPTS ${MAVEN_OPTS}
WORKDIR /opt/
RUN git clone ${GIT_CLONE} core-geonetwork
WORKDIR /opt/core-geonetwork/
RUN sed -i -e 's|slave.masterURL=https://..../geonetwork|slave.masterURL=http://geonetworkmaster:8080/geonetwork|' ./slave/src/main/resources/slave.properties
# Use cached Maven stuff, if you have:
WORKDIR /root/.m2/
COPY ./m2repo_gn/ ./
WORKDIR /opt/core-geonetwork/
ARG MASTER_OR_SLAVE
ENV MASTER_OR_SLAVE ${MASTER_OR_SLAVE}
# Download Maven stuff step by step:
RUN mvn                                --projects=common dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common package install
# RUN ls -al /root/.m2/repository/org/geonetwork-opensource/common/3.7.0-SNAPSHOT/
RUN mvn                                --projects=common,cachingxslt dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt package install
RUN mvn                                --projects=common,cachingxslt,sde dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde package install
RUN mvn                                --projects=common,cachingxslt,sde,domain dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain package install
RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh package install
RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events dependency:go-offline
RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events package install
# At this point you cannot continue like this, because schema-iso19139 is needed. Create the rest at once, see below.
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release,workers dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release,workers package install
# RUN mvn                                --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release,workers,messaging dependency:go-offline
# RUN mvn -DskipTests ${MASTER_OR_SLAVE} --projects=common,cachingxslt,sde,domain,oaipmh,events,core,schemas,csw-server,harvesters,healthmonitor,services,wro4j,inspire-atom,doi,es,release,workers,messaging package install
RUN mvn -DskipTests ${MASTER_OR_SLAVE} package install

FROM tomcat:8.5.40-jre8
# Credits to: Jeroen Ticheler <jeroen.ticheler@geocat.net>
LABEL maintainer="Ob Rzwo <obr2pd@gmail.com>"
ARG HTTP_PROXY
ENV HTTP_PROXY ${HTTP_PROXY}
ARG HTTPS_PROXY
ENV HTTPS_PROXY ${HTTPS_PROXY}
ARG NO_PROXY
ENV NO_PROXY ${NO_PROXY}
ENV http_proxy ${HTTP_PROXY}
ENV https_proxy ${HTTPS_PROXY}
ENV no_proxy ${NO_PROXY}
COPY apt.conf /etc/apt/
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get -y update \
  && apt-get install -y postgresql-client \
  && rm -rf /var/lib/apt/lists/*
WORKDIR /usr/local/tomcat/webapps/
COPY --from=lala /opt/core-geonetwork/web/target/geonetwork.war ./
RUN mkdir -p ./geonetwork/ \
  && unzip -d ./geonetwork/ ./geonetwork.war
RUN sed -i -e 's|<session-timeout>35</session-timeout>|<session-timeout>999</session-timeout>|' ./geonetwork/WEB-INF/web.xml
RUN sed -i -e 's#<import resource="../config-db/h2.xml"/>#<!--<import resource="../config-db/h2.xml"/> -->#g' ./geonetwork/WEB-INF/config-node/srv.xml \
  && sed -i -e 's#<!--<import resource="../config-db/postgres.xml"/>-->#<import resource="../config-db/postgres.xml"/>#g' ./geonetwork/WEB-INF/config-node/srv.xml
COPY ./jdbc.properties ./geonetwork/WEB-INF/config-db/jdbc.properties
COPY ./docker-entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
CMD ["catalina.sh", "run"]
EXPOSE 8080
