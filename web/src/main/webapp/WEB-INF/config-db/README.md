This folder contains the spring database configuration files.  

Database Configuration Parameters
---------------------------------

The main parameters are in the jdbc.properties file and are described below
GeoNetwork uses Apache Commons Database Connection Pooling (DBCP) code.
You can specify a JNDI datasource and place the DBCP config params in the 
container context.xml (eg. for tomcat this is conf/context.xml) or 
you can config a subset of the DBCP parameters explicitly for h2, mckoi, 
oracle, postgres/gis, db2 and sql-server as follows:

	maxActive - pool size/maximum number of active connections (default 10)
	maxIdle   - pool size/maximum number of idle connections 
	            (default maxActive)
	minIdle   - pool size/minimum number of idle connections (default 0)
	maxWait   - number of milliseconds to wait for a connection to become
	            available (default 200)
	validationQuery - optional, sql statement for verifying a connection, 
							the statement must return a least one row, empty 
							result sets may cause problems
	timeBetweenEvictionRunsMillis - time between eviction runs 
							(default -1 which means the next three params are ignored)
	testWhileIdle - test connections when idle (default false)
	minEvictableIdleTimeMillis - idle time before connection can be evicted 
							(default 30 x 60 x 1000 millisecs)
	numTestsPerEvictionRun - number of connections tested per eviction run
							(default 3)

The following params are set by GeoNetwork:

	removeAbandoned - true
	removeAbandonedTimeout - 60 x 60 seconds = 1 hour
	logAbandoned - true
	testOnBorrow - true
	defaultReadOnly - false
	defaultAutoCommit - false
	initialSize - maxActive

Note: Some firewalls kill idle connections to databases after say
1 hour (= 3600 secs) 
to keep idle connections alive by testing them with 'select 1', 
set minEvictableIdleTimeMillis to something less than timeout
interval (eg. 2 mins = 120 secs = 120000 millisecs), 
set testWhileIdle to true and 
set timeBetweenEvictionRunsMillis and numTestsPerEvictionRun
high enough to visit connections frequently
eg 15 mins = 900 secs = 900000 millisecs and 4 connections per test
	
	eg.

		<testWhileIdle>true</testWhileIdle>
		<minEvictableIdleTimeMillis>120000</minEvictableIdleTimeMillis>
		<timeBetweenEvictionRunsMillis>900000</timeBetweenEvictionRunsMillis>
		<numTestsPerEvictionRun>4</numTestsPerEvictionRun>

			!!!!!!!!!!!!!!!!!!! WARNING !!!!!!!!!!!!!!!!!!! 
			
When changing the resource to use, you could point to an old version of
a GeoNetwork database instance (eg. 2.4.3).
In such a case, GeoNetwork will try to migrate the database on startup
to the current version (eg. 2.5.0). This will only occur if a migration
script is found. Migration scripts are located in WEB-INF/classes/setup/sql/migrate
folder. Migration to 2.4.3 to 2.5.0 will be in WEB-INF/classes/setup/sql/migrate/2.4.3-to-2.5.0.

During the migration process only the database will be migrated. Catalogue
administrator still need to migrate logos, data, thesaurus.

Using an old database with a new instance is not recommended (check the log
on startup which display webapp version and database version).
