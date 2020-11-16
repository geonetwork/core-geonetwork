FROM maven:3.6.3-jdk-8 AS builder

COPY cas-overlay-template-4.2 /cas-overlay-template
RUN cd /cas-overlay-template/ && mvn clean package


FROM tomcat:8.5-jdk8

COPY --from=builder /cas-overlay-template/target/cas.war ${CATALINA_HOME}/webapps/
RUN cd $CATALINA_HOME/webapps                                                                                      && \
  unzip -d cas cas.war                                                                                             && \
  rm -f cas.war

COPY cas.properties ${CATALINA_HOME}/webapps/cas/WEB-INF/classes/
COPY deployerConfigContext.xml ${CATALINA_HOME}/webapps/cas/WEB-INF/
COPY casLoginView.jsp ${CATALINA_HOME}/webapps/cas/WEB-INF/view/jsp/default/ui/

