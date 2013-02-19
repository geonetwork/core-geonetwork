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

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.setting.SettingManager;
import org.jdom.Element;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

//=============================================================================

/** Handles the access to a metadata depending on the metadata/group
  */

public class AccessManager
{
	public static final String OPER_VIEW     				= "0";
	public static final String OPER_DOWNLOAD 				= "1";
	public static final String OPER_EDITING  				= "2";
	public static final String OPER_NOTIFY   				= "3";
	public static final String OPER_DYNAMIC  				= "5";
	public static final String OPER_FEATURED 				= "6";

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

	/** Loads all permissions from database and caches them
	 */

	public AccessManager(Dbms dbms, SettingManager sm) throws SQLException
	{
		settMan = sm;

		List operList = dbms.select("SELECT * FROM Operations").getChildren();

		for (Object o : operList)
		{
			Element oper = (Element) o;

			String id   = oper.getChildText("id");
			String name = oper.getChildText("name");

			//--- build Hashset of all operations
			hsAllOps.add(id);

			hmIdToName.put(Integer.parseInt(id), name);
			hmNameToId.put(name, Integer.parseInt(id));
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

	/** Given a user(session) a list of groups and a metadata returns all operations that user
	  * can perform on that metadata (an set of OPER_XXX as keys)
	  * If the user is authenticated the permissions are taken from the groups the user belong
	  * If the user is not authenticated, a dynamic group is assigned depending on user location
	  * (0 for internal and 1 for external)
	  *
	  */

	public HashSet<String> getOperations(ServiceContext context, String mdId, String ip) throws Exception {
		return getOperations(context, mdId, ip, null);
	}

	public HashSet<String> getOperations(ServiceContext context, String mdId, String ip, Element operations) throws Exception {

		UserSession us = context.getUserSession();

        // if user is an administrator OR is the owner of the record then allow all
		// operations
		if (isOwner(context,mdId)) {
			return hsAllOps;
		}

		// otherwise build result
		HashSet<String> out = new HashSet<String>();

		Element ops;
		if (operations == null) {
			ops = getAllOperations(context, mdId, ip);
		} else {
			ops = operations;
		}

		List operIds = Xml.selectNodes(ops, "record/operationid");
        for (Object operId : operIds) {
            Element elem = (Element) operId;
            out.add(elem.getText());
        }

		if (us.isAuthenticated() && us.getProfile().equals(Geonet.Profile.EDITOR) 
				 && out.contains(OPER_EDITING)) {
			out.add(OPER_VIEW);
		}
		
		return out;
	}

	//	--------------------------------------------------------------------------
	/** Returns all operations permitted by the user on a particular metadata
	  */

	public Element getAllOperations(ServiceContext context, String mdId, String ip) throws Exception {
		Dbms        dbms    = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		UserSession usrSess = context.getUserSession();

        // build group list
		Set<String>  groups    = getUserGroups(dbms, usrSess, ip);
		StringBuffer groupList = new StringBuffer();

		for (Iterator i = groups.iterator(); i.hasNext(); )
		{
			String groupId = (String) i.next();
			groupList.append(groupId);

			if (i.hasNext())
				groupList.append(", ");
		}
		// get allowed operations
		StringBuffer query = new StringBuffer();

		query.append("SELECT operationId, groupId ");
		query.append("FROM   OperationAllowed ");
		query.append("WHERE  groupId IN (");
		query.append(groupList.toString());
		query.append(") AND    metadataId = ?");
		
		Element operations = dbms.select(query.toString(), new Integer(mdId));

		// find out what they could do if they registered and offer that as
		// as a separate element
		if (!usrSess.isAuthenticated()) {
			query = new StringBuffer();
			query.append("SELECT operationId, groupId ");
			query.append("FROM   OperationAllowed ");
			query.append("WHERE  groupId = -1 ");
			query.append("AND    metadataId = ?");

			Element therecords = dbms.select(query.toString(), new Integer(mdId));
			if (therecords != null) {
				Element guestOperations = new Element("guestoperations");
				guestOperations.addContent(therecords.cloneContent());
				operations.addContent(guestOperations);
			}
		}

		return operations;
	}

	//	--------------------------------------------------------------------------
	/** Returns all groups accessible by the user (a set of ids)
	  */

	public Set<String> getUserGroups(Dbms dbms, UserSession usrSess, String ip) throws Exception
	{
		HashSet<String> hs = new HashSet<String>();

		// add All (1) network group
		hs.add("1");

		if (ip != null && isIntranet(ip))
			hs.add("0");

		// get other groups
		if (usrSess.isAuthenticated())
		{
			// add (-1) GUEST group 
			hs.add("-1");

			if (usrSess.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			{
				Element elUserGrp = dbms.select("SELECT id FROM Groups");

				List list = elUserGrp.getChildren();

                for (Object aList : list) {
                    Element el = (Element) aList;
                    String groupId = el.getChildText("id");
                    hs.add(groupId);
                }
			}
			else
			{
                Element elUserGrp = dbms.select("SELECT groupId FROM UserGroups WHERE userId=?",usrSess.getUserIdAsInt());

                List list = elUserGrp.getChildren();

                for (Object aList : list) {
                    Element el = (Element) aList;
                    String groupId = el.getChildText("groupid");
                    hs.add(groupId);
                }
			}
		}
		return hs;
	}

	//--------------------------------------------------------------------------

	public Set<String> getVisibleGroups(Dbms dbms, String userId) throws Exception
	{
		int id = Integer.parseInt(userId);
		return getVisibleGroups(dbms,id);
	}

	public Set<String> getVisibleGroups(Dbms dbms, int userId) throws Exception
	{
		HashSet<String> hs = new HashSet<String>();

		String query= "SELECT * FROM Users WHERE id=?";
		List   list = dbms.select(query, userId).getChildren();

		//--- return an empty list if the user does not exist

		if (list.size() == 0)
			return hs;

		Element user    = (Element) list.get(0);
		String  profile = user.getChildText("profile");

        Element elUserGrp;
        if (profile.equals(Geonet.Profile.ADMINISTRATOR)) {
            elUserGrp = dbms.select("SELECT id AS grp FROM Groups");
        }
        else {
            elUserGrp = dbms.select("SELECT groupId AS grp FROM UserGroups WHERE userId=?", new Integer(userId));
        }

		for(Object o : elUserGrp.getChildren())
		{
			Element el = (Element) o;
			String groupId =el.getChildText("grp");
			hs.add(groupId);
		}

		return hs;
	}

    /**
     *  Returns true if and only if at least one of these conditions is satisfied :
     *
     *  The user is the metadata owner
     *  The user is an Administrator
     *  The user has edit rights over the metadata
     *  The user is a Reviewer and/or UserAdmin and the metadata groupOwner is one of his groups.
     *
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
	public boolean canEdit(ServiceContext context, String id) throws Exception {
		return isOwner(context, id) || hasEditPermission(context, id);
	}

    /**
     *
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
	public boolean isOwner(ServiceContext context, String id) throws Exception {
		UserSession us = context.getUserSession();
		if (!us.isAuthenticated()) {
			return false;
		}

		//--- retrieve metadata info
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dm = gc.getDataManager();
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		MdInfo info = dm.getMetadataInfo(dbms, id);

		//--- harvested metadata cannot be edited
        //		if (info == null || info.isHarvested)
		if (info == null) {
			return false;
        }

		//--- check if the user is an administrator
		if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR)) {
			return true;
        }

		//--- check if the user is the metadata owner
		if (us.getUserId().equals(info.owner)) {
			return true;
        }

		//--- check if the user is a reviewer or useradmin
		if (!us.getProfile().equals(Geonet.Profile.REVIEWER) && !us.getProfile().equals(Geonet.Profile.USER_ADMIN)) {
			return false;
        }

		//--- if there is no group owner then the reviewer cannot review and the useradmin cannot administer
		if (info.groupOwner == null) {
			return false;
        }

		for (String userGroup : getUserGroups(dbms, us, null)) {
			if (userGroup.equals(info.groupOwner)) {
				return true;
            }
		}

		return false;
	}

    /**
     * Returns whether a particular metadata is visible to group 'all'.
     * @param dbms
     * @param metadataId
     * @return
     * @throws Exception
     */
    public boolean isVisibleToAll(Dbms dbms, String metadataId) throws Exception {
        // group 'all' has the magic id 1.
        String query = "SELECT operationId FROM OperationAllowed WHERE groupId = 1 AND metadataId = ?";
        Element result = dbms.select(query, new Integer(metadataId));
        if(result == null) {
            return false;
        }
        else {
            List<Element> records = result.getChildren("record");
            for(Element record : records) {
                String operationId = record.getChildText("operationid");
                if(operationId != null && operationId.equals(OPER_VIEW))  {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     *
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
	public boolean hasEditPermission(ServiceContext context, String id) throws Exception {
		UserSession us = context.getUserSession();
		if (!us.isAuthenticated()) {
			return false;
        }
		//--- check if the user is an editor and has edit rights over the metadata record
		if (us.getProfile().equals(Geonet.Profile.EDITOR)) {
			HashSet<String> hsOper = getOperations(context, id, context.getIpAddress());
			if (hsOper.contains(OPER_EDITING)) return true;
		}
		return false;
	}


    /**
     *
     * @param descr
     * @return
     */
	public int getPrivilegeId(String descr) {
		return hmNameToId.containsKey(descr) ? hmNameToId.get(descr) : -1;

	}

    /**
     *
     * @param id
     * @return
     */
	public String getPrivilegeName(int id) {
		return hmIdToName.get(id);
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    /**
     *
     * @param ip
     * @return
     */
	private boolean isIntranet(String ip) {
		//--- consider IPv4 & IPv6 loopback
		//--- we use 'startsWith' because some addresses can be 0:0:0:0:0:0:0:1%0

		if (ip.startsWith("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) return true;

        // IPv6 link-local
        String ipv6LinkLocalPrefix = "fe80:";
        if(ip.toLowerCase().startsWith(ipv6LinkLocalPrefix)) {
            return true;
        }
        // other IPv6
        else if(ip.indexOf(':') >= 0) {
            return false;
        }

        // IPv4

		String network = settMan.getValue("system/intranet/network");
		String netmask = settMan.getValue("system/intranet/netmask");

		try {
		long lIntranetNet  = getAddress(network);
		long lIntranetMask = getAddress(netmask);
		long lAddress      = getAddress(ip);
		return (lAddress & lIntranetMask) == lIntranetNet ;
		} catch (Exception nfe) {
			nfe.printStackTrace();
			return false;
		}
	}

    /**
     *  Converts an ip x.x.x.x into a long.
     *
     * @param ip
     * @return
     */
	private long getAddress(String ip){
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

	private SettingManager  settMan;
	private HashSet<String> hsAllOps = new HashSet<String>();

	private HashMap<Integer, String> hmIdToName = new HashMap<Integer, String>();
	private HashMap<String, Integer> hmNameToId = new HashMap<String, Integer>();
}

//=============================================================================

