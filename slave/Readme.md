This project gets included on GeoNetwork when compiled with the profile "slave":

mvn clean package install -P slave

When a GeoNetwork is compiled as read only:

 * Does not allow any login (intercepts all logins)
 * Does not allow adding or updating any users (intercepts all database changes)
 * [Optional] Remove all users except admin user, which is still used for internal GeoNetwork tasks. Admin user will not be available for login either.
 * [Optional] Remove all harvesters from the database
 * Create and run a harvester pointing to the master instance
 
 Configuration options are on file src/main/resources/slave.properties