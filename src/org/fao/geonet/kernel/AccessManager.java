//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ProfileManager;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

//=============================================================================

/** Handles the access to a metadata depending on the metadata/group
  */

public class AccessManager
{
	public static final String OPER_VIEW     = "0";
	public static final String OPER_DOWNLOAD = "1";
	public static final String OPER_EDIT     = "2";
	public static final String OPER_NOTIFY   = "3";
	public static final String OPER_ADMIN    = "4";
	public static final String OPER_DYNAMIC  = "5";
	public static final String OPER_FEATURED = "6";

	public static final String VIEW_SERVICE     = "metadata.show";
	public static final String EDIT_SERVICE     = "metadata.update";
	public static final String MD_ADMIN_SERVICE = "metadata.admin";
	public static final String DOWNLOAD_SERVICE = "resources.get";  // resources.get is public due to thumbnails

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Loads all permissions from database and caches them
	 */

	public AccessManager(SettingManager sm)
	{
		settMan = sm;

		//--- build Hashtable of all operations
		hsAllOps.add(OPER_VIEW);
		hsAllOps.add(OPER_DOWNLOAD);
		hsAllOps.add(OPER_EDIT);
		hsAllOps.add(OPER_NOTIFY);
		hsAllOps.add(OPER_ADMIN);
		hsAllOps.add(OPER_DYNAMIC);
		hsAllOps.add(OPER_FEATURED);
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Given a user(session) a list of groups and a metadata returns all operations that user
	  * can perform on that metadata (an hashtable of OPER_XXX as keys)
	  * If the user is authenticated the permissions are taken from the groups the user belong
	  * If the user is not authenticated, a dynamic group is assigned depending on user location
	  * (0 for internal and 1 for external)
	  *
	  */

	public HashSet<String> getOperations(ServiceContext context, String mdId, String ip)
														throws Exception
	{
		Dbms        dbms    = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		UserSession usrSess = context.getUserSession();
		String      profile = usrSess.getProfile();

		// if user is an administrator just allow any operation
		if (usrSess.isAuthenticated() && profile.equals(Geonet.Profile.ADMINISTRATOR))
			return hsAllOps;

		// build group list
		HashSet<String> groups    = getUserGroups(dbms, usrSess, ip);
		StringBuffer    groupList = new StringBuffer();

		for (Iterator i = groups.iterator(); i.hasNext(); )
		{
			String groupId = (String) i.next();
			groupList.append(groupId);

			if (i.hasNext())
				groupList.append(", ");
		}
		// get allowed operations
		StringBuffer query = new StringBuffer();

		query.append("SELECT DISTINCT operationId ");
		query.append("FROM   OperationAllowed ");
		query.append("WHERE  groupId IN (" + groupList.toString() + ") ");
		query.append("AND    metadataId = " + mdId);

		Element operations = dbms.select(query.toString());

		// build result
		HashSet<String> result = new HashSet<String>();

		ProfileManager pm = context.getProfileManager();
		if (profile == null) profile = ProfileManager.GUEST;

		for (Iterator iter= operations.getChildren().iterator() ; iter.hasNext(); )
		{
			Element record = (Element)iter.next();
			String  operId = record.getChildText("operationid");

			// no checking for OPER_NOTIFY, OPER_DYNAMIC and OPER_FEATURED
			if (operId.equals(OPER_VIEW)     && !pm.hasAccessTo(profile, VIEW_SERVICE))     continue;
			if (operId.equals(OPER_DOWNLOAD) && !pm.hasAccessTo(profile, DOWNLOAD_SERVICE)) continue;
			if (operId.equals(OPER_EDIT)     && !pm.hasAccessTo(profile, EDIT_SERVICE))     continue;
			if (operId.equals(OPER_ADMIN)    && !pm.hasAccessTo(profile, MD_ADMIN_SERVICE)) continue;

			result.add(operId);
		}
		return result;
	}

	//	--------------------------------------------------------------------------
	/** Returns all groups accessible by the user (a set of ids)
	  */

	public HashSet<String> getUserGroups(Dbms dbms, UserSession usrSess, String ip) throws Exception
	{
		HashSet<String> hs = new HashSet<String>();

		// add All (1) network group
		hs.add("1");

		if (isIntranet(ip))
			hs.add("0");

		// get other groups
		if (usrSess.isAuthenticated())
		{
			if (usrSess.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			{
				Element elUserGrp = dbms.select("SELECT id FROM Groups");

				List list = elUserGrp.getChildren();

				for(int i=0; i<list.size(); i++)
				{
					Element el = (Element) list.get(i);
					String groupId =el.getChildText("id");
					hs.add(groupId);
				}
			}
			else
			{
				Element elUserGrp = dbms.select("SELECT groupId FROM UserGroups WHERE userId=" + usrSess.getUserId());

				List list = elUserGrp.getChildren();

				for(int i=0; i<list.size(); i++)
				{
					Element el = (Element) list.get(i);
					String groupId =el.getChildText("groupid");
					hs.add(groupId);
				}
			}
		}
		return hs;
	}

	//--------------------------------------------------------------------------

	public static int getPrivilegeId(String descr)
	{
		for(int i=0; i<privDescr.length; i++)
			if (descr.equals(privDescr[i]))
				return i;

		return -1;
	}

	//--------------------------------------------------------------------------

	public static String getPrivilegeName(int id)
	{
		return privDescr[id];
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private boolean isIntranet(String ip)
	{
		//--- consider IPv4 & IPv6 loopback
		//--- we use 'startsWith' because some addresses can be 0:0:0:0:0:0:0:1%0

		if (ip.startsWith("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1"))
			return true;

		String network = settMan.getValue("system/intranet/network");
		String netmask = settMan.getValue("system/intranet/netmask");

		long lIntranetNet  = getAddress(network);
		long lIntranetMask = getAddress(netmask);
		long lAddress      = getAddress(ip);

		return (lAddress & lIntranetMask) == lIntranetNet ;
	}

	//--------------------------------------------------------------------------

	/** Converts an ip x.x.x.x into a long
	  */

	private long getAddress(String ip)
	{
		StringTokenizer st = new StringTokenizer(ip, ".");

		long a1 = Integer.parseInt(st.nextToken());
		long a2 = Integer.parseInt(st.nextToken());
		long a3 = Integer.parseInt(st.nextToken());
		long a4 = Integer.parseInt(st.nextToken());

		return a1<<24 | a2<<16 | a3<<8 | a4;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//--------------------------------------------------------------------------

	private static final String privDescr[] =
										{
											"view",  "download", "edit", "notify",
											"admin", "dynamic",  "featured"
										};

	//--------------------------------------------------------------------------

	private SettingManager  settMan;
	private HashSet<String> hsAllOps = new HashSet<String>();
}

//=============================================================================

