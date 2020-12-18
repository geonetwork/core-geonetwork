FROM openjdk:11.0.9-jdk AS builder

COPY cas-overlay-template-6.2 /cas-overlay-template
RUN cd /cas-overlay-template/ && ./gradlew build


FROM tomcat:9.0-jdk11

COPY --from=builder /cas-overlay-template/build/libs/cas.war ${CATALINA_HOME}/webapps/
RUN cd $CATALINA_HOME/webapps                                                                                      && \
  unzip -d cas cas.war                                                                                             && \
  rm -f cas.war

COPY cas-overlay-template-6.2/etc/cas /etc/cas

