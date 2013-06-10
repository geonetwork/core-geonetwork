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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jeeves.resources.dbms.Dbms;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.domain.Group;
import org.fao.geonet.kernel.domain.Metadata;
import org.fao.geonet.kernel.domain.Operation;
import org.fao.geonet.kernel.domain.ReservedGroup;
import org.fao.geonet.kernel.domain.ReservedOperation;
import org.fao.geonet.kernel.domain.Setting;
import org.fao.geonet.kernel.repository.GroupRepository;
import org.fao.geonet.kernel.repository.MetadataRepository;
import org.fao.geonet.kernel.repository.OperationAllowedRepository;
import org.fao.geonet.kernel.repository.OperationRepository;
import org.fao.geonet.kernel.repository.SettingRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles the access to a metadata depending on the metadata/group.
 */
@Component
public class AccessManager {

    @Autowired
    private SettingRepository _settingRepository;

    @Autowired
    private OperationRepository _opRepository;
    
    @Autowired
    private MetadataRepository _metadataRepository;
    
    @Autowired
    private OperationAllowedRepository _opAllowedRepository;
    
    @Autowired
    private GroupRepository _groupRepository;
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
	public Set<Operation> getOperations(ServiceContext context, String mdId, String ip) throws Exception {
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
	public Set<Operation> getOperations(ServiceContext context, String mdId, String ip, Collection<Operation> operations) throws Exception {
		Set<Operation> results;
        // if user is an administrator OR is the owner of the record then allow all operations
		if (isOwner(context,mdId)) {
			results = new HashSet<Operation>(_opRepository.findAll());
		} else {
    		if (operations == null) {
    		    results = new HashSet<Operation>(getAllOperations(context, mdId, ip));
    		}
            else {
                results = new HashSet<Operation>(operations);
    		}

    		UserSession us = context.getUserSession();
            if (us.isAuthenticated() && us.getProfile().equals(Geonet.Profile.EDITOR)) {
                results.add(_opRepository.findReservedOperation(ReservedOperation.view));
            }
		}
		
		return results;
	}

    public Set<String> getOperationNames(ServiceContext context, String mdId, String ip, Collection<Operation> operations) throws Exception {
        Set<String> names = new HashSet<String>();
        
        for (Operation op : getOperations(context, mdId, ip, operations)) {
            names.add(op.getName());
        }
        
        return names;
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
	public Set<Operation> getAllOperations(ServiceContext context, String mdId, String ip) throws Exception {
		Metadata md = _metadataRepository.findByIdString(mdId);
		if(md == null) {
		    return Collections.emptySet();
		}
		return md.getOperations();
	}

    /**
     * Returns all groups accessible by the user (a set of ids).
     *
     * @param dbms
     * @param usrSess
     * @param ip
     * @param editingGroupsOnly TODO
     * @return
     * @throws Exception
     */
	public Set<String> getUserGroups(Dbms dbms, UserSession usrSess, String ip, boolean editingGroupsOnly) throws Exception {
		Set<String> hs = new HashSet<String>();

		// add All (1) network group
		hs.add("1");

		if (ip != null && isIntranet(ip))
			hs.add("0");

		// get other groups
		if (usrSess.isAuthenticated()) {
			// add (-1) GUEST group 
			hs.add("-1");

			if (Geonet.Profile.ADMINISTRATOR.equals(usrSess.getProfile())) {
				Element elUserGrp = dbms.select("SELECT id FROM Groups");

				@SuppressWarnings("unchecked")
                List<Element> list = elUserGrp.getChildren();

                for (Element el : list) {
                    String groupId = el.getChildText("id");
                    hs.add(groupId);
                }
			}
			else {
				StringBuffer query = new StringBuffer("SELECT distinct(groupId) FROM UserGroups WHERE ");
				if (editingGroupsOnly) {
					query.append("profile='"+Geonet.Profile.EDITOR+"' AND ");
				}
				query.append("userId=?");
				Element elUserGrp = dbms.select(query.toString(), usrSess.getUserIdAsInt());

				@SuppressWarnings("unchecked")
                List<Element> list = elUserGrp.getChildren();

                for (Element el : list) {
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
        if(StringUtils.isEmpty(userId)){
            Log.warning(Geonet.ACCESS_MANAGER, "userId is empty. AccessManager getVisibleGroups()");
            return new HashSet<String>();
        }
        int id = Integer.parseInt(userId);
		return getVisibleGroups(dbms,id);
	}

    /**
     * TODO javadoc.
     *
     * @param dbms
     * @param userId
     * @return
     * @throws Exception
     */
	public Set<String> getVisibleGroups(Dbms dbms, int userId) throws Exception {
		Set<String> hs = new HashSet<String>();

		String query= "SELECT * FROM Users WHERE id=?";
		@SuppressWarnings("unchecked")
        List<Element>   list = dbms.select(query, Integer.valueOf(userId)).getChildren();

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
			elUserGrp = dbms.select("SELECT groupId AS grp FROM UserGroups WHERE userId=?", Integer.valueOf(userId));
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
     *  - The user is a Reviewer and/or UserAdmin and the metadata groupOwner
     *    is one of his groups.
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
		DataManager   dm = gc.getBean(DataManager.class);
		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
		MdInfo info = dm.getMetadataInfo(dbms, id);

		//--- harvested metadata cannot be edited

//		if (info == null || info.isHarvested)
		if (info == null)
			return false;

		//--- check if the user is an administrator
		if (us.getProfile().equals(Geonet.Profile.ADMINISTRATOR))
			return true;

		//--- check if the user is the metadata owner
		//
		if (us.getUserId().equals(info.owner))
			return true;

		//--- check if the user is a reviewer or useradmin
		if (!us.getProfile().equals(Geonet.Profile.REVIEWER) && !us.getProfile().equals(Geonet.Profile.USER_ADMIN))
			return false;

		//--- if there is no group owner then the reviewer cannot review and the useradmin cannot administer
		if (info.groupOwner == null)
			return false;

		for (String userGroup : getUserGroups(dbms, us, null, true)) {
			if (userGroup.equals(info.groupOwner))
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
	private String join(Set<Integer> set, String delim) {
    	StringBuilder sb = new StringBuilder();
    	String loopDelim = "";
    	for(Integer s : set) {
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
    public Element getOwners(Dbms dbms, Set<Integer> metadataIds) throws Exception {
				String query=
				"SELECT m.id as metadataid, u.id as userid, u.name as name, u.surname as surname, u.email as email from Metadata m "+
				"JOIN Users u on u.id = m.owner "+
				"WHERE m.id IN (" + join(metadataIds,",") + ") "+
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
    public Element getContentReviewers(Dbms dbms, Set<Integer> metadataIds) throws Exception {
				String query=
				"SELECT m.id as metadataid, u.id as userid, u.name as name, u.surname as surname, u.email as email from Metadata m "+
				"JOIN UserGroups ug on m.groupOwner = ug.groupId "+
				"JOIN Users u on u.id = ug.userId "+
				"WHERE m.id IN (" + join(metadataIds,",") + ") "+
				"AND ug.profile = '"+Geonet.Profile.REVIEWER+"' "+
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
        Metadata metadata = _metadataRepository.findByIdString(metadataId);
        if(metadata == null) {
            return false;
        }
        else {
            Group allGroup = _groupRepository.findReservedGroup(ReservedGroup.all);
            int opId = ReservedOperation.view.getId();
            return hasPermission(metadata, allGroup, opId);
        }
    }

    /**
     * Check if the group has the permission.
     *
     * @param metadata the metadata object
     * @param group the group to check
     * @param opId the id of the operation to check for
     */
    public boolean hasPermission(Metadata metadata, Group group, int opId) {
        return _opAllowedRepository.findByGroupIdAndMetadataIdAndOperationId(group.getId(), metadata.getId(), opId) != null;
    }

    /**
     * Check if current user can edit the metadata according
     * to the groups where the metadata is editable.
     *
     * @param context
     * @param id    The metadata internal identifier
     * @return
     * @throws Exception
     */
    public boolean hasEditPermission(ServiceContext context, String id) throws Exception {
        UserSession us = context.getUserSession();
        if (!us.isAuthenticated())
            return false;
        
        //--- check if the user is an editor and has edit rights over the metadata record
        String isEditorQuery = "SELECT ug.groupId FROM UserGroups ug, OperationAllowed oa " +
                                "WHERE ug.groupId = oa.groupId AND operationId = ? AND " + 
                                "userId = ? AND profile = ? AND metadataId = ?";
        
        Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
        
        Element isEditorRes = dbms.select(isEditorQuery, 
                                ReservedOperation.editing.getId(), 
                                Integer.parseInt(us.getUserId()), 
                                Geonet.Profile.EDITOR, 
                                Integer.parseInt(id));
        
        if (isEditorRes.getChildren().size() != 0) {
            return true;
        }
        return false;
    }

    /**
     * TODO javadoc.
     *
     * @param name 
     * @return
     */
	public int getPrivilegeId(String name) {
		return _opRepository.findByName(name).getId();
	}

    /**
     * TODO javadoc.
     *
     * @param id
     * @return
     */
	public String getPrivilegeName(int id) {
		return _opRepository.findOne(id).getName();
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

		Setting network = _settingRepository.findOneByPath("system/intranet/network");
		Setting netmask = _settingRepository.findOneByPath("system/intranet/netmask");

        try {
            if (network != null && netmask != null) {
                long lIntranetNet = getAddress(network.getValue());
                long lIntranetMask = getAddress(netmask.getValue());
                long lAddress = getAddress(ip);
                return (lAddress & lIntranetMask) == lIntranetNet;
            }
        } catch (Exception nfe) {
			nfe.printStackTrace();
		}
        return false;
	}

    /**
     * Converts an ip x.x.x.x into a long.
     *
     * @param ip
     * @return
     */
	private long getAddress(String ip) {
		if(ip.trim().equals("?")) {
			return 0;
		} else {
			StringTokenizer st = new StringTokenizer(ip, ".");
			long a1 = Integer.parseInt(st.nextToken());
			long a2 = Integer.parseInt(st.nextToken());
			long a3 = Integer.parseInt(st.nextToken());
			long a4 = Integer.parseInt(st.nextToken());
			return a1<<24 | a2<<16 | a3<<8 | a4;
		}
	}
}