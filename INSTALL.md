This file provides you a guide to install Geonetwork-Myocean

Note: If you want to update the web application (application already exists and you use an existing tomcat instance and an existing profile), you just have to follow procedure IV Deploy web application.
If you want to deploy the web application on a new machine with a new profile, you have to:
1. Configure the new tomcat instance (I Configure new geonetwork tomcat instance)
2. Manage database (II Manage database)
3. Create a new profile (III Create new profile)
4. deploy web application (IV Deploy web application)


I) Configure new geonetwork tomcat instance
Note : Step 1-5 are specific to the Ifremer environment. This can be summary to configure your tomcat instance

1. Add your instance in the README_Ports file. Define available ports
2. Create your tomcat instance folder
3. Copy/paste folders and files “conf” and “setenv.sh” from another tomcat web application.
4. Configure “server.xml” and “tomcat-users.xml” (located in “conf” folder). Especially define the right ports.
5. Configure "setenv.sh (define INSTANCE and PORT_JMX)
6. Create a folder “lib” in your tomcat instance and import the necessary libraries. You can copy them from another myocean web application.
7. Create folders “logs” and “webapps” in your tomcat instance.
8. Create a directory 'geonetwork' and then, inside of it, a directory 'data' (with read/write right) in yout tomcat instance
you should have /export/home/tomcat/instances/<tomcat instance>/geonetwork/data
9. Paste “sextant-probe.war” in the “webapps” folder.


II) Manage database

A database is specific to a tomcat instance (due to the table setting which has as input the name of the host and the port used by the instance). 
Two profiles using two different tomcat instances can't point to the same base.

If you want to use a new profile on a new instance, you have to:
1. Create a dump of the existing database
2. Create a new database and restore her with the dump previously created
3. Modify 2 parameters in the settings table
•	system/server/host: with the name of the host (eg visi-common-tomcat1)
•	system/server/port: with the http port used by the tomcat instance of the application


III) Configuring the application

In the source code (follow step 1 and 2 of part IV to know how get it):
1. Create a new property file to the following path: /web/src/main/filters/deployment. His name must be: config_<profile name>.properties. 
   You can take example of a profile file which already exists. Be sure to fill in correctly the overrides.props option (define it with the profile name) and the application paths (gn.datadir, gn.webappdir and jcs.path). You also can configure the log options of the application deployment.
2. Use env variables to configure the DB to use (or modify the default in the root pom.xml) eg.
```
mvn clean install -DskipTests -Ddb.name=sextant -Ddb.host=db.serveur -Ddb.username=catalogue -Ddb.pa
ssword=secured -Dsxt.properties=ifr-exp
```

IV) Deploy web application

1. Get source code from github:
create folder where you want to have the source code
git init
git clone --recursive https://gitlab.ifremer.fr/sextant/geonetwork.git

2.	go to sextant-geonetwork
cd sextant-geonetwork
optional: switch to a specific branch of geonetwork:
git fetch origin; git checkout <name of branch>

3.	compile web application with proper profile:
mvn clean install -U -Dsxt.properties=<profile name> 

4.	
	• If this is an update and the application is already deployed then follow the next pre-steps:
		1. Go to tomcat server bin directory:
		cd /export/home/tomcat/bin
		2. Stop the web application:
		./stop.sh <tomcat instance>
		3. Go to the instance webapp directory
		cd ../instances/<tomcat instance>/webapps 
		4. Delete geonetwork.war file and geonetwork directory
		rm -rf geonetwork
		rm geonetwork.war
	• If this is a new application, follow the procedure of the part I.
	
5. copy web application on tomcat instance
scp ./web/target/geonetwork.war isi_exp@<server machine>:/export/home/tomcat/instances/<tomcat instance>/webapps/

6. Go to tomcat server bin directory
cd /export/home/tomcat/bin

7. start tomcat instance
./run.sh <tomcat instance> ; tail -f ../instances/<tomcat instance>/logs/catalina.out

8. If this is a new application, copy/paste “codelist” config folder from the application source to the geonetwork config directory and restart tomcat instance.
cp -R /<source_code folder>/sextant-geonetwork/web/src/main/webapp/WEB-INF/data/config/codelist /export/home/tomcat/instances/sextant/geonetwork/data/config
./restart.sh <tomcat instance>

9.	You can stop the server with the following command:
./stop.sh <tomcat instance>
