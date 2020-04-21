FROM tomcat:8.5-jdk8

ENV GN_FILE geonetwork.war
ENV DATA_DIR=$CATALINA_HOME/webapps/geonetwork/WEB-INF/data
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom -Djava.awt.headless=true -server -Xms512m -Xmx2024m -XX:NewSize=512m -XX:MaxNewSize=1024m -XX:+UseConcMarkSweepGC -Dspring.profiles.active=es"

ENV ES_HOST localhost
ENV ES_PROTOCOL http
ENV ES_PORT 9200
ENV ES_USERNAME ""
ENV ES_PASSWORD ""
ENV ES_INDEX_SEARCHLOGS "gn-searchlogs"
ENV ES_INDEX_RECORDS "gn-records"
ENV ES_INDEX_FEATURES "gn-features"
ENV KB_URL http://localhost:5601

WORKDIR $CATALINA_HOME/webapps

COPY ./geonetwork.war ./geonetwork.war

RUN  unzip -d geonetwork geonetwork.war && \
     rm geonetwork.war

COPY ./docker-entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

CMD ["catalina.sh", "run"]

