FROM tomcat:8.5-jdk8

ARG GN_UID
ARG GN_GID
ARG GN_FILE

ENV DATA_DIR=$CATALINA_HOME/webapps/geonetwork/WEB-INF/data
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -server -Xms512m -Xmx2024m -XX:NewSize=512m -XX:MaxNewSize=1024m -XX:+UseConcMarkSweepGC -Dspring.profiles.active=es -Dgeonetwork.schema.dir=/usr/local/tomcat/webapps/geonetwork/WEB-INF/data/config/schema_plugins  -Dgeonetwork.formatter.dir=/usr/local/tomcat/webapps/geonetwork/WEB-INF/data/data/formatter"

ENV ES_HOST localhost
ENV ES_PROTOCOL http
ENV ES_PORT 9200
ENV ES_USERNAME ""
ENV ES_PASSWORD ""
ENV ES_INDEX_SEARCHLOGS "gn-searchlogs"
ENV ES_INDEX_RECORDS "gn-records"
ENV ES_INDEX_FEATURES "gn-features"
ENV KB_URL http://localhost:5601

ENV LDAP_URL "ldap://ldap.ifremer.fr:389"

ENV CAS_URL "http://localhost:8080/cas"
ENV CAS_TICKET_VALIDATION_URL "http://cas:8080/cas"
ENV GN_URL "http://localhost:8080/geonetwork"

ENV PANIER_XML_PATH_LOGGED "/export/home/tomcat/instances_data/sextant-test/panier/folders/aTraiter/intranet"
ENV PANIER_XML_PATH_ANONYMOUS "/export/home/tomcat/instances_data/sextant-test/panier/folders/aTraiter/anonymous"

WORKDIR $CATALINA_HOME/webapps

COPY ./geonetwork.war ./${GN_FILE}.war

RUN apt update && \
      apt install -y augeas-tools && \
      rm -rf /var/lib/apt/lists/*

RUN  unzip -d geonetwork ./${GN_FILE}.war && \
     rm ./${GN_FILE}.war

RUN chown -R ${GN_UID}:${GN_GID} ${GN_FILE}/ && \
  chown -R ${GN_UID}:${GN_GID} /usr/local/tomcat/conf

COPY ./docker-entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD ["catalina.sh", "run"]

