Using LDAPUserDetailsContextMapperWithProfileSearchEnhanced
===========================================================

`LDAPUserDetailsContextMapperWithProfileSearch` was modified so it works in more situations, is much simpler, uses strategy objects, and has a good set of test cases.

This extends the original `LDAPUserDetailsContextMapperWithProfileSearch` so it can support more LDAP configurations.

The old `LDAPUserDetailsContextMapperWithProfileSearch` should work as before - existing configurations should not need any changes.

Please see the test cases (and the corresponding README.md).  This contains;

1. Tests that show functionality
2. The test cases run an LDAP server 
3. There are easy instructions for setting up a configured LDAP server so you can run GeoNetwork against it.
 
There two biggest differences with `LDAPUserDetailsContextMapperWithProfileSearch` are;

1. Moved functionality to Strategy Object
     * cf. `SearchingLdapUsernameToDnMapper`
          * instead of apriori knowing where (in the dir hierarchy) a user is stored, this will search for it
     * cf. `LDAPRoleConverterGroupNameParser`
          * this parses an LDAP Group name to determine its GN-Group and GN-profile
              * ie. GCAT_general_editor --> GN-group=general, GN-Profile=Editor
     * cf. `LDAPRoleConverterGroupNameConverter`
          * this is a simple conversion between an LDAP group name and a set of GN Roles
2. I've removed assumptions about how the LDAP is structured
     * it searches in the dir hierarchy for Users and Groups 
         * old implementation assumed they were all in one directory


I've left the main `AbstractLDAPUserDetailsContextMapper` unchanged, and the old LDAP infrastructure should still work.  

However, if you are setting up a new LDAP configuration, I recommend you use the new infrastructure since its much easier to understand.
Here's the recommended process - its a bit lengthy, but it gives you the best understanding of what's going on.

1. Create an .ldif that sets up an LDAP server that matches your configuration
     * c.f. `data.ldif` in test resource 
2. Modify the `LDAPUserDetailsContextMapperWithProfileSearchEnhancedTest-context.xml` so it matches your configuration
3. The existing test cases use the configuration in `data.ldif`, but you can run these and modify them to ensure your configuration is working
4. See the instructions (README.md in the test cases dir) to run the LDAP server (with your .ldif) and GeoNetwork


