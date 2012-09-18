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

import jeeves.exceptions.OperationNotAllowedEx;
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
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Handles the access to a metadata depending on the metadata/group.
 */
public class AccessManager {
	public static final String OPER_VIEW     				= "0";
	public static final String OPER_DOWNLOAD 				= "1";
	public static final String OPER_EDITING  				= "2";
	public static final String OPER_NOTIFY   				= "3";
	public static final String OPER_DYNAMIC  				= "5";
	public static final String OPER_FEATURED 				= "6";

	public static final Map<String,String> ops = new HashMap<String,String>();

	static {
		ops.put("0","VIEW");
		ops.put("1","DOWNLOAD");
		ops.put("2","EDITING");
		ops.put("3","NOTIFY");
		ops.put("5","DYNAMIC");
		ops.put("6","FEATURED");
	};

	//--------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//--------------------------------------------------------------------------

    /**
     * Loads all permissions from database and caches them.
     *
     * @param dbms
     * @param sm
     * @throws SQLException
     */
	public AccessManager(Dbms dbms, SettingManager sm) throws SQLException {
		settMan = sm;
		List operList = dbms.select("SELECT * FROM Operations").getChildren();

		for (Object o : operList) {
			Element oper = (Element) o;
			String id   = oper.getChildText("id");
			String name = oper.getChildText("name");

			//--- build Hashset of all operations
			hsAllOps.add(id);
			hmIdToName.put(id, name);
			hmNameToId.put(name, id);
		}
	}

	//--------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//--------------------------------------------------------------------------

    /**
     * Given a user(session) a list of groups and a metadata returns all operations that user can perform on that
     * metadata (an set of OPER_XXX as keys).
     * If the user is authenticated the permissions are taken from the groups the user belong.
     * If the user is not authenticated, a dynamic group is assigned depending on user location (0 for internal and 1
     * for external).
     * @param context
     * @param mdId
     * @param ip
     * @return
     * @throws Exception
     */
	public Set<String> getOperations(ServiceContext context, String mdId, String ip) throws Exception {
		return getOperations(context, mdId, ip, null);
	}

    /**
     * TODO javadoc.
     *
     * @param context
     * @param mdId
     * @param ip
     * @param operations
     * @return
     * @throws Exception
     */
	public Set<String> getOperations(ServiceContext context, String mdId, String ip, Element operations) throws Exception {
        // if user is an administrator OR is the owner of the record then allow all operations
		if (isOwner(context,mdId)) {
			return hsAllOps;
		}
		// otherwise build result
		Element ops;
		if (operations == null) {
			ops = getAllOperations(context, mdId, ip);
		}
        else {
			ops = operations;
		}
        Set<String> out = getAllOperationsSet(context, mdId, ip, ops);
		return out;
	}

    public Set<String> getAllOperationsSet(ServiceContext context, String mdId, String ip, Element operations) throws Exception {
        UserSession us = context.getUserSession();
		Set<String> out = new HashSet<String>();

		Element ops;
		if (operations == null) {
			ops = getAllOperations(context, mdId, ip);
		}
        else {
			ops = operations;
		}

		List operIds = Xml.selectNodes(ops, "record/operationid");
        for (Object operId : operIds) {
            Element elem = (Element) operId;
            out.add(elem.getText());
        }
		if (us.isAuthenticated() && us.getProfile().equals(Geonet.Profile.EDITOR) && out.contains(OPER_EDITING)) {
			out.add(OPER_VIEW);
		}
		
		return out;
	}

    /**
     * Returns all operations permitted by the user on a particular metadata.
     *
     * @param context
     * @param mdId
     * @param ip
     * @return
     * @throws Exception
     */
	public Element getAllOperations(ServiceContext context, String mdId, String ip) throws Exception {
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		UserSession usrSess = context.getUserSession();

        // build group list
		Set<String>  groups = getUserGroups(dbms, usrSess, ip);
		StringBuffer groupList = new StringBuffer();

		for (Iterator i = groups.iterator(); i.hasNext(); ) {
			String groupId = (String) i.next();
			groupList.append('\'' + groupId + '\'');

            if(i.hasNext()) {
				groupList.append(", ");
		}
		}
		// get allowed operations
		StringBuffer query = new StringBuffer();

		query.append("SELECT operationId, groupId ");
		query.append("FROM   OperationAllowed ");
		query.append("WHERE  groupId IN (");
		query.append(groupList.toString());
		query.append(") AND    metadataId = ?");
		
		Element operations = dbms.select(query.toString(), mdId);

		// find out what they could do if they registered and offer that as as a separate element
		if (!usrSess.isAuthenticated()) {
			query = new StringBuffer();
			query.append("SELECT operationId, groupId ");
			query.append("FROM   OperationAllowed ");
			query.append("WHERE  groupId = '-1' ");
			query.append("AND    metadataId = ?");
			
			Element therecords = dbms.select(query.toString(), mdId);
			if (therecords != null) {
				Element guestOperations = new Element("guestoperations");
				guestOperations.addContent(therecords.cloneContent());
				operations.addContent(guestOperations);
			}
		}
		return operations;
	}

    /**
     * Returns all groups accessible by the user (a set of ids).
     *
     * @param dbms
     * @param usrSess
     * @param ip
     * @return
     * @throws Exception
     */
	public Set<String> getUserGroups(Dbms dbms, UserSession usrSess, String ip) throws Exception {
		Set<String> hs = new HashSet<String>();

		// add All (1) network group
		hs.add("1");

        if(ip != null && isIntranet(ip)) {
			hs.add("0");
        }

		// get other groups
		if (usrSess.isAuthenticated()) {
			// add (-1) GUEST group 
			hs.add("-1");

			if (usrSess.getProfile().equals(Geonet.Profile.ADMINISTRATOR)) {
				Element elUserGrp = dbms.select("SELECT id FROM Groups");

				List list = elUserGrp.getChildren();

                for (Object aList : list) {
                    Element el = (Element) aList;
                    String groupId = el.getChildText("id");
                    hs.add(groupId);
                }
			}
			else {
				Element elUserGrp = dbms.select("SELECT groupId FROM UserGroups WHERE userId=?", usrSess.getUserId());

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

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param userId
     * @return
     * @throws Exception
     */
	public Set<String> getVisibleGroups(Dbms dbms, String userId) throws Exception {
		Set<String> hs = new HashSet<String>();

		String query= "SELECT * FROM Users WHERE id=?";
		List   list = dbms.select(query, userId).getChildren();

		//--- return an empty list if the user does not exist

        if(list.size() == 0) {
			return hs;
        }

		Element user    = (Element) list.get(0);
		String  profile = user.getChildText("profile");

		Element elUserGrp;
		if (profile.equals(Geonet.Profile.ADMINISTRATOR)) {
			elUserGrp = dbms.select("SELECT id AS grp FROM Groups");
		}
        else {
			elUserGrp = dbms.select("SELECT groupId AS grp FROM UserGroups WHERE userId=?", userId);
		}

		for(Object o : elUserGrp.getChildren()) {
			Element el = (Element) o;
			String groupId =el.getChildText("grp");
			hs.add(groupId);
		}

		return hs;
	}

    /**
     * Returns true if, and only if, at least one of these conditions is satisfied:
     *  - The user is the metadata owner
     *  - The user is an Administrator
     *	 - The user has edit rights over the metadata
     *  - The user is a Reviewer and/or UserAdmin and the metadata has edit rights for one of his groups.
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
     * TODO javadoc.
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
        if(info == null) {
			return false;
        }

		//--- check if the user is an administrator
        if(us.getProfile().equals(Geonet.Profile.ADMINISTRATOR)) {
			return true;
        }

		//--- check if the user is the metadata owner
		//
        if(us.getUserId().equals(info.owner)) {
			return true;
        }

		//--- check if the user is a reviewer or useradmin
        if(! us.getProfile().equals(Geonet.Profile.REVIEWER) && ! us.getProfile().equals(Geonet.Profile.USER_ADMIN)) {
			return false;
        }
        /*
		//--- if there is no group owner then the reviewer cannot review and the useradmin cannot administer
		if (info.groupOwner == null)
			return false;

		for (String userGroup : getUserGroups(dbms, us, null)) {
			if (userGroup.equals(info.groupOwner))
				return true;
		}
         */

        // check if the user's groups are in operationallowed/editing for this md
        Set<String> hsOper = getAllOperationsSet(context, id, context.getIpAddress(), null);
        if (hsOper.contains(OPER_EDITING)) {
            return true;
        }

		return false;
	}

    /**
     * TODO javadoc.
     *
     * @param set
     * @param delim
     * @return
     */
	private String join(Set<String> set, String delim) {
    	StringBuilder sb = new StringBuilder();
    	String loopDelim = "";
    	for(String s : set) {
        sb.append(loopDelim);
        sb.append(s+"");            
        loopDelim = delim;
    	}
    	return sb.toString();
	}

    /**
     * Returns owners of metadata records.
     *
     * @param dbms
     * @param metadataIds
     * @return
     * @throws Exception
     */
    public Element getOwners(Dbms dbms, Set<String> metadataIds) throws Exception {

        StringBuffer metadataIdsList = new StringBuffer();
        for (Iterator i = metadataIds.iterator(); i.hasNext(); ) {
            String metadataId = (String) i.next();
            metadataIdsList.append('\'' + metadataId + '\'');

            if(i.hasNext()) {
                metadataIdsList.append(", ");
            }
        }

				String query=
				"SELECT m.id as metadataid, u.id as userid, u.name as name, u.surname as surname, u.email as email from Metadata m "+
				"JOIN Users u on u.id = m.owner "+
            "WHERE m.id IN (" + metadataIdsList + ") "+
				"ORDER BY u.id";

        return dbms.select(query);
    }

    /**
     * Returns content reviewers for metadata records.
     *
     * @param dbms
     * @param metadataIds
     * @return
     * @throws Exception
     */
    public Element getContentReviewers(Dbms dbms, Set<String> metadataIds) throws Exception {

        StringBuffer metadataIdsList = new StringBuffer();
        for (Iterator i = metadataIds.iterator(); i.hasNext(); ) {
            String metadataId = (String) i.next();
            metadataIdsList.append('\'' + metadataId + '\'');

            if(i.hasNext()) {
                metadataIdsList.append(", ");
            }
        }

				String query=
          /*  "SELECT m.id as metadataid, u.id as userid, u.name as name, u.surname as surname, u.email as email from Metadata m "+
				"JOIN Users u on u.id = ug.userId "+
            "WHERE m.id IN (" + metadataIdsList + ") "+
            "AND m.id IN (SELECT metadataid FROM OperationAllowed WHERE operationid = 2 AND groupid IN (SELECT groupid FROM UserGroups WHERE userid=u.id)) "+
            "AND u.profile = '"+Geonet.Profile.REVIEWER+"' "+
            "ORDER BY u.id";
            */
        "SELECT m.id as metadataid, u.id as userid, u.name as name, u.surname as surname, u.email as email from Metadata m, Users u "+
                "WHERE m.id IN (" + metadataIdsList + ") "+
                "AND m.id IN (SELECT metadataid FROM OperationAllowed WHERE operationid = '2' AND groupid IN (SELECT groupid FROM UserGroups WHERE userid=u.id)) "+
				"AND u.profile = '"+Geonet.Profile.REVIEWER+"' "+
				"ORDER BY u.id";

        return dbms.select(query);
    }

    /**
     * Returns whether a particular metadata is visible to group 'all'.
     *
     * @param dbms
     * @param metadataId
     * @return
     * @throws Exception
     */
    public boolean isVisibleToAll(Dbms dbms, String metadataId) throws Exception {
        // group 'all' has the magic id 1.
        String query = "SELECT operationId FROM OperationAllowed WHERE groupId = '1' AND metadataId = ?";
        Element result = dbms.select(query, metadataId);
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
     * TODO javadoc.
     *
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
	public boolean hasEditPermission(ServiceContext context, String id) throws Exception {
		UserSession us = context.getUserSession();
        if(! us.isAuthenticated()) {
			return false;
        }
		//--- check if the user is an editor and has edit rights over the metadata record
		if (us.getProfile().equals(Geonet.Profile.EDITOR)) {
			Set<String> hsOper = getOperations(context, id, context.getIpAddress());
            if(hsOper.contains(OPER_EDITING)) {
                return true;
            }
		}
		return false;
	}

    /**
     * TODO javadoc.
     *
     * @param descr
     * @return
     */
	public String getPrivilegeId(String descr) {
		return hmNameToId.containsKey(descr) ? hmNameToId.get(descr) : "-1";
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     */
	public String getPrivilegeName(String id) {
		return hmIdToName.get(id);
	}

    //
    // access for lock operations
    //

    /**
     * Whether the current user is allowed to reassign a metadata's lock to a particular other user.
     *
     * @param userProfile profile of current user
     * @param userId id of current user
     * @param targetUserId id of user to assign lock to
     * @param metadataId id of metadata
     * @param dbms dbms
     * @param symbolicLocking whether symbolic locking is enabled
     * @return true or false
     * @throws Exception hmm
     */
    public boolean grabLockAllowed(String userProfile, String userId, String targetUserId, String metadataId, Dbms dbms, boolean symbolicLocking) throws Exception {
        // if the user is an admin, just allow
        if(userProfile.equals(Geonet.Profile.ADMINISTRATOR)) {
            return true;
        }
        // if the user is a reviewer or useradmin, allow grabbing lock from metadata that has edit permission for the user's groups, and
        // reassign it to another user in the user's groups
        else if(userProfile.equals(Geonet.Profile.REVIEWER) || userProfile.equals(Geonet.Profile.USER_ADMIN)) {
            return grabLockAllowed(userId, targetUserId, metadataId, dbms);
        }
        // if the user is an editor
        else if(userProfile.equals(Geonet.Profile.EDITOR) && symbolicLocking) {
            return grabLockAllowed(userId, targetUserId, metadataId, dbms);
        }
        return false;
    }

    /**
     * Whether the current user is allow to grab a metadata's lock and assign it to another user. Returns true if the
     * metadata has Edit privilege in at least one of the current user's groups, and the targeted user is member of at
     * least one of the current user's groups.
     *
     * @param userId
     * @param targetUserId
     * @param metadataId
     * @param dbms
     * @return
     * @throws Exception
     */
    private boolean grabLockAllowed(String userId, String targetUserId, String metadataId, Dbms dbms) throws Exception {
        // check md has edit permission for at least one of the user's groups
        String query = "SELECT count(*) as numr FROM OperationAllowed " +
                "WHERE operationid = '2' AND groupid IN (SELECT groupid FROM UserGroups WHERE userid=?) " +
                "AND metadataid=?" ;
        List result = dbms.select(query, userId, metadataId).getChildren();
        int count = Integer.parseInt(((Element) result.get(0)).getChildText("numr"));
        if(count == 0) {
            throw new OperationNotAllowedEx("User " + userId + " attempted to grab lock of metadata " + metadataId + " that has no edit privilege for any of his groups");
        }
        // check targeted user is in at least one of this user's groups
        query = "SELECT count(*) as numr FROM UserGroups " +
                "WHERE userid=? AND groupid IN (SELECT groupid FROM UserGroups WHERE userid=?)";
        result = dbms.select(query, targetUserId, userId).getChildren();
        count = Integer.parseInt(((Element) result.get(0)).getChildText("numr"));
        if(count == 0) {
            throw new OperationNotAllowedEx("User " + userId + " attempted to grab lock of metadata and assign it to user " + targetUserId + "  that is not in any of his groups");
        }

        // if you got here it's OK
        return true;
    }

    /**
     * Whether the current user is allowed to unlock this metadata. It's only allowed if this user owns the lock.
     *
     * @param userId
     * @param metadataId
     * @param dbms
     * @return
     * @throws Exception
     */
    public boolean unlockAllowed(String userId, String metadataId, Dbms dbms) throws Exception {
        String query = "SELECT lockedBy from Metadata WHERE id=?";
        Element resultList = dbms.select(query, metadataId);
        if(resultList.getChildren().size() == 0) {
            throw new OperationNotAllowedEx("User " + userId + " attempted to unlock metadata " + metadataId + " which can't be found");
        }
        Element result = (Element)resultList.getChildren().get(0);
        String lockedBy = result.getChildText("lockedby");
        if(lockedBy.equals(userId)) {
            return true;
        }
        else {
            throw new OperationNotAllowedEx("User " + userId + " attempted to unlock metadata " + metadataId + " but doesn't own the lock");
        }
    }


	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

    /**
     * TODO javadoc.
     *
     * @param ip
     * @return
     */
	private boolean isIntranet(String ip) {
		//--- consider IPv4 & IPv6 loopback
		//--- we use 'startsWith' because some addresses can be 0:0:0:0:0:0:0:1%0

        if(ip.startsWith("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) {
            return true;
        }

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
     * Converts an ip x.x.x.x into a long.
     *
     * @param ip
     * @return
     */
	private long getAddress(String ip) {
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
	private Set<String> hsAllOps = new HashSet<String>();
	private Map<String, String> hmIdToName = new HashMap<String, String>();
	private Map<String, String> hmNameToId = new HashMap<String, String>();
}
