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

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;

import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
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

		if (Log.isDebugEnabled(Geonet.LDAP)) {
			Log.debug(Geonet.LDAP, "LDAPSynchronizerJob starting ...");
		}

		// Retrieve application context. A defautl SpringBeanJobFactory
		// will not provide the application context to the job. Use
		// AutowiringSpringBeanJobFactory.
		applicationContext = (ApplicationContext) jobExecContext.getJobDetail()
				.getJobDataMap().get("applicationContext");

		if (applicationContext == null) {
			Log.error(
					Geonet.LDAP,
					"  Application context is null. Be sure to configure SchedulerFactoryBean job factory property with AutowiringSpringBeanJobFactory.");
		}

		// Get LDAP information defining which users to sync
		final JobDataMap jdm = jobExecContext.getJobDetail().getJobDataMap();
		contextSource = (DefaultSpringSecurityContextSource) jdm
				.get("contextSource");
		String ldapSearchFilter = (String) jdm.get("ldapSearchFilter");
		String ldapSearchBase = (String) jdm.get("ldapSearchBase");
		DirContext dc = contextSource.getReadOnlyContext();

		// Get database
		ResourceManager resourceManager = applicationContext
				.getBean(ResourceManager.class);
		Dbms dbms = null;

		try {
			dbms = (Dbms) resourceManager.openDirect(Geonet.Res.MAIN_DB);

			// Do something for LDAP users ? Currently user is updated on log
			// in only.
			NamingEnumeration<?> userList = dc.search(ldapSearchBase,
					ldapSearchFilter, null);

			// Build a list of LDAP users
			StringBuffer usernames = new StringBuffer();
			while (userList.hasMore()) {
				SearchResult sr = (SearchResult) userList.next();
				usernames.append("'");
				usernames.append(sr.getAttributes().get("uid").get());
				usernames.append("', ");
			}


			// Remove LDAP user available in db and not in LDAP if not linked to metadata
			String query = "SELECT id FROM Users WHERE authtype=? AND username NOT IN ("
					+ usernames.toString() + "'')";
			Element e = dbms.select(query, LDAPConstants.LDAP_FLAG);
			for (Object record : e.getChildren("record")) {
				Element r = (Element) record;
				int userId = new Integer(r.getChildText("id"));
				Log.debug(Geonet.LDAP, "  - Removing user: " + userId);
				try {
					dbms.execute("DELETE FROM UserGroups WHERE userId=?",
							userId);
					dbms.execute("DELETE FROM Users WHERE authtype=? AND id=?", LDAPConstants.LDAP_FLAG, userId);
				} catch (Exception ex) {
					Log.error(
							Geonet.LDAP,
							"Failed to remove LDAP user with id "
									+ userId
									+ " in database. User is probably a metadata owner."
									+ " Transfer owner first.",
							ex);
				}
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

		if (Log.isDebugEnabled(Geonet.LDAP)) {
			Log.debug(Geonet.LDAP, "LDAPSynchronizerJob done.");
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
