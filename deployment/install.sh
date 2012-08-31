#!/bin/sh

export TOMCAT_VERSION=7.0.29
export LIFERAY_VERSION=6.1.1-ce-ga2-20120731132656558
export SEXTANT_SOURCE_HOME=/home/francois/Workspace/github/sextant
export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
echo "#############################"
echo "    Installing Sextant ..."
echo "      Tomcat version : $TOMCAT_VERSION"
echo "      Liferay version : $LIFERAY_VERSION"
echo "      Sextant source folder : $SEXTANT_SOURCE_HOME"
echo "      Java home : $JAVA_HOME"
echo "#############################"

# TODO : test folder exist

echo "# Cleaning tomcat ..."
rm -fr apache-tomcat-$TOMCAT_VERSION

echo "# Install tomcat ..."
tar xvfz apache-tomcat-$TOMCAT_VERSION.tar.gz

echo "#   configuration (copy server.xml with SSL connector, server keystore for localhost) ..."
cp $SEXTANT_SOURCE_HOME/deployment/tomcat/conf/* apache-tomcat-$TOMCAT_VERSION/conf/.
cp $SEXTANT_SOURCE_HOME/deployment/tomcat/bin/* apache-tomcat-$TOMCAT_VERSION/bin/.
echo "#   install keystore to Java certificates ($JAVA_HOME/jre/lib/security/cacerts) as sudoers ..."
echo "#   Note: Java certificate password is usually 'changeit'."
sudo keytool -import -keystore $JAVA_HOME/jre/lib/security/cacerts -file $SEXTANT_SOURCE_HOME/deployment/tomcat/conf/server.der -alias localhost



echo "#############################"
echo "# Install liferay ..."
echo "#   ext lib ..."
unzip ext.zip
mv ext apache-tomcat-$TOMCAT_VERSION/lib/.
echo "#   configuration ..."
mkdir liferay-portal-$LIFERAY_VERSION
cp liferay-portal-$LIFERAY_VERSION.war liferay-portal-$LIFERAY_VERSION/.
cd liferay-portal-$LIFERAY_VERSION
unzip liferay-portal-$LIFERAY_VERSION.war
cp $SEXTANT_SOURCE_HOME/deployment/liferay/config/portal-ext.properties WEB-INF/classes/.
cd ..

echo "#   publish to webapp as ROOT ..."
rm -fr apache-tomcat-$TOMCAT_VERSION/webapps/ROOT
mv liferay-portal-$LIFERAY_VERSION apache-tomcat-$TOMCAT_VERSION/webapps/ROOT

echo "#############################"
echo "# Install cas in webapp ..."
cp $SEXTANT_SOURCE_HOME/cas/target/cas.war apache-tomcat-$TOMCAT_VERSION/webapps/.

echo "#############################"
echo "# Install geonetwork in webapp ..."
cp $SEXTANT_SOURCE_HOME/web/target/geonetwork.war apache-tomcat-$TOMCAT_VERSION/webapps/.

echo "# Done."
echo ""
echo "You can now start Tomcat with"
echo "cd apache-tomcat-$TOMCAT_VERSION/bin/;./startup.sh"
echo "#############################"
