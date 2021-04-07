Running Fully Inside GeoNetwork
===============================

I've set this up so you can easily run a local LDAP server (complete with GN and LDAP configuration).

Setup LDAP
----------

1. Download and install Apache Directory Studio
2. Add a new Server (might have to open the "Server" View)
3. Open a connection to the new Server
4. right-click on the connection and import a "ldif" file
5. choose "data_for_Apache_Directory_Studio.ldif" 
    * This is the same a data.ldif but it doesn't define the dc=example,dc=com folder (its pre-created)
6. Feel free to explore the LDAP tree in Apache Directory Studio    
  
NOTE: use port 3333  

Setup GeoNetwork
---------------- 

1. Remove the comment for `<import resource="config-security-ldap.xml"/>` in `web/src/main/webapp/WEB-INF/config-security/config-security.xml`
2. Replace the contents of `web/src/main/webapp/WEB-INF/config-security/config-security-ldap.xml` with the contents from `apache-directory-studio-security-context.xml`
3. Rebuild and run Geonework
4. Login as admin/admin user
5. in the admin menu, add a "GENERAL" group (all caps) 

Using GeoNetwork
----------------
1. You can login with the standard admin/admin user
2. You can also login with these users;
   dblasby@example.com/blasby1 -- has "Editor" rights in GENERAL group
   admin@example.com/admin1 -- has "Administrator" rights in GENERAL group
   jgee@example.com/jody1 -- no groups -- "RegisteredUser"
   
