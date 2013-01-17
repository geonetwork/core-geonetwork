//=============================================================================
//===	Copyright (C) 2001-2012 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.kernel.security.ldap;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import jeeves.utils.SerialFactory;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

public class LDAPSynchronizerJob extends QuartzJobBean {
    
    private ApplicationContext applicationContext;
    
    private DefaultSpringSecurityContextSource contextSource;
    
    @Override
    protected void executeInternal(JobExecutionContext jobExecContext)
            throws JobExecutionException {
        try {
            if (Log.isDebugEnabled(Geonet.LDAP)) {
                Log.debug(Geonet.LDAP, "LDAPSynchronizerJob starting ...");
            }
            
            // Retrieve application context. A defautl SpringBeanJobFactory
            // will not provide the application context to the job. Use
            // AutowiringSpringBeanJobFactory.
            applicationContext = (ApplicationContext) jobExecContext
                    .getJobDetail().getJobDataMap().get("applicationContext");
            
            
            if (applicationContext == null) {
                Log.error(
                        Geonet.LDAP,
                        "  Application context is null. Be sure to configure SchedulerFactoryBean job factory property with AutowiringSpringBeanJobFactory.");
            }
            
            // Get LDAP information defining which users to sync
            final JobDataMap jdm = jobExecContext.getJobDetail()
                    .getJobDataMap();
            contextSource = (DefaultSpringSecurityContextSource) jdm
                    .get("contextSource");
            
            String ldapUserSearchFilter = (String) jdm
                    .get("ldapUserSearchFilter");
            String ldapUserSearchBase = (String) jdm.get("ldapUserSearchBase");
            String ldapUserSearchAttribute = (String) jdm
                    .get("ldapUserSearchAttribute");
            
            DirContext dc = contextSource.getReadOnlyContext();
            
            // Get database
            ResourceManager resourceManager = applicationContext
                    .getBean(ResourceManager.class);
            Dbms dbms = null;
            
            try {
                dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);
                
                // Users
                synchronizeUser(ldapUserSearchFilter, ldapUserSearchBase,
                        ldapUserSearchAttribute, dc, dbms);
                
                // And optionaly groups
                String createNonExistingLdapGroup = (String) jdm
                        .get("createNonExistingLdapGroup");
                
                if ("true".equals(createNonExistingLdapGroup)) {
                    SerialFactory serialFactory = applicationContext
                            .getBean(SerialFactory.class);
                    
                    String ldapGroupSearchFilter = (String) jdm
                            .get("ldapGroupSearchFilter");
                    String ldapGroupSearchBase = (String) jdm
                            .get("ldapGroupSearchBase");
                    String ldapGroupSearchAttribute = (String) jdm
                            .get("ldapGroupSearchAttribute");
                    String ldapGroupSearchPattern = (String) jdm
                            .get("ldapGroupSearchPattern");
                    
                    synchronizeGroup(ldapGroupSearchFilter,
                            ldapGroupSearchBase, ldapGroupSearchAttribute,
                            ldapGroupSearchPattern, dc, dbms, serialFactory);
                }
            } catch (NamingException e1) {
                e1.printStackTrace();
            } catch (Exception e) {
                try {
                    resourceManager.abort(Geonet.Res.MAIN_DB, dbms);
                    dbms = null;
                } catch (Exception e2) {
                    e.printStackTrace();
                    Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e2);
                }
                Log.error(
                        Geonet.LDAP,
                        "Unexpected error while synchronizing LDAP user in database",
                        e);
            } finally {
                try {
                    dc.close();
                } catch (NamingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (dbms != null) {
                    try {
                        resourceManager.close(Geonet.Res.MAIN_DB, dbms);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.error(Geonet.LDAP, "Error closing dbms" + dbms, e);
                    }
                }
            }
        } catch (Exception e) {
            Log.error(
                    Geonet.LDAP,
                    "Unexpected error while synchronizing LDAP user in database",
                    e);
            e.printStackTrace();
        }
        
        if (Log.isDebugEnabled(Geonet.LDAP)) {
            Log.debug(Geonet.LDAP, "LDAPSynchronizerJob done.");
        }
    }
    
    
    private void synchronizeUser(String ldapUserSearchFilter,
            String ldapUserSearchBase, String ldapUserSearchAttribute,
            DirContext dc, Dbms dbms) throws NamingException, SQLException {
        // Do something for LDAP users ? Currently user is updated on log
        // in only.
        NamingEnumeration<?> userList = dc.search(ldapUserSearchBase,
                ldapUserSearchFilter, null);
        
        // Build a list of LDAP users
        StringBuffer usernames = new StringBuffer();
        while (userList.hasMore()) {
            SearchResult sr = (SearchResult) userList.next();
            usernames.append("'");
            usernames.append(sr.getAttributes().get(ldapUserSearchAttribute)
                    .get());
            usernames.append("', ");
        }
        
        // Remove LDAP user available in db and not in LDAP if not linked to
        // metadata
        String query = "SELECT id FROM Users WHERE authtype=? AND username NOT IN ("
                + usernames.toString() + "'')";
        Element e = dbms.select(query, LDAPConstants.LDAP_FLAG);
        for (Object record : e.getChildren("record")) {
            Element r = (Element) record;
            int userId = new Integer(r.getChildText("id"));
            Log.debug(Geonet.LDAP, "  - Removing user: " + userId);
            try {
                dbms.execute("DELETE FROM UserGroups WHERE userId=?", userId);
                dbms.execute("DELETE FROM Users WHERE authtype=? AND id=?",
                        LDAPConstants.LDAP_FLAG, userId);
            } catch (Exception ex) {
                Log.error(Geonet.LDAP, "Failed to remove LDAP user with id "
                        + userId
                        + " in database. User is probably a metadata owner."
                        + " Transfer owner first.", ex);
            }
        }
    }
    
    
    private void synchronizeGroup(String ldapGroupSearchFilter,
            String ldapGroupSearchBase, String ldapGroupSearchAttribute,
            String ldapGroupSearchPattern, DirContext dc, Dbms dbms,
            SerialFactory serialFactory) throws NamingException, SQLException {
        
        NamingEnumeration<?> groupList = dc.search(ldapGroupSearchBase,
                ldapGroupSearchFilter, null);
        Pattern ldapGroupSearchPatternCompiled = null;
        if (!"".equals(ldapGroupSearchPattern)) {
            ldapGroupSearchPatternCompiled = Pattern
                    .compile(ldapGroupSearchPattern);
        }
        
        while (groupList.hasMore()) {
            SearchResult sr = (SearchResult) groupList.next();
            
            // TODO : should we retrieve LDAP group id and do an update of group
            // name
            // This will require to store in local db the remote id
            String groupName = (String) sr.getAttributes()
                    .get(ldapGroupSearchAttribute).get();
            
            if (!"".equals(ldapGroupSearchPattern)) {
                Matcher m = ldapGroupSearchPatternCompiled.matcher(groupName);
                boolean b = m.matches();
                if (b) {
                    groupName = m.group(1);
                }
            }
            
            Element groupIdRequest = dbms.select(
                    "SELECT id FROM Groups WHERE name = ?", groupName);
            Element groupRecord = groupIdRequest.getChild("record");
            String groupId = null;
            
            if (groupRecord == null) {
                if (Log.isDebugEnabled(Geonet.LDAP)) {
                    Log.debug(Geonet.LDAP, "  - Add non existing group '"
                            + groupName + "' in local database.");
                }
                
                // If LDAP group does not exist in local database, create it
                groupId = serialFactory.getSerial(dbms, "Groups") + "";
                String query = "INSERT INTO GROUPS(id, name) VALUES(?,?)";
                dbms.execute(query, new Integer(groupId), groupName);
                Lib.local.insert(dbms, "Groups", new Integer(groupId),
                        groupName);
            } else if (groupRecord != null) {
                groupId = groupRecord.getChildText("id");
                // Update something ?
                // Group description is only defined in catalog, not in LDAP for the time
                // being
            }
        }
    }
    
    public DefaultSpringSecurityContextSource getContextSource() {
        return contextSource;
    }
    
    public void setContextSource(
            DefaultSpringSecurityContextSource contextSource) {
        this.contextSource = contextSource;
    }
}