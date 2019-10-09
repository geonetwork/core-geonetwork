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

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasMetadataId;
import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasOperation;
import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.Operation;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.User_;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.GroupRepositoryCustom;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.OperationRepository;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Handles the access to a metadata depending on the metadata/group.
 */
public class AccessManager {
    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------

    /**
     * Given a user(session) a list of groups and a metadata returns all operations that user can
     * perform on that metadata (an set of OPER_XXX as keys). If the user is authenticated the
     * permissions are taken from the groups the user belong. If the user is not authenticated, a
     * dynamic group is assigned depending on user location (0 for internal and 1 for external).
     */
    public Set<Operation> getOperations(ServiceContext context, String mdId, String ip) throws Exception {
        return getOperations(context, mdId, ip, null);
    }

    /**
     * TODO javadoc.
     */
    public Set<Operation> getOperations(ServiceContext context, String mdId, String ip, Collection<Operation> operations) throws Exception {
        OperationRepository _opRepository = context.getApplicationContext().getBean(OperationRepository.class);

        Set<Operation> results;
        // if user is an administrator OR is the owner of the record then allow all operations
        if (isOwner(context, mdId)) {
            results = new HashSet<>(_opRepository.findAll());
        } else {
            if (operations == null) {
                results = new HashSet<>(getAllOperations(context, mdId, ip));
            } else {
                results = new HashSet<>(operations);
            }

            UserSession us = context.getUserSession();
            if (us.isAuthenticated() && us.getProfile() == Profile.Editor && us.getProfile() == Profile.Reviewer) {
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
     */
    public Set<Operation> getAllOperations(ServiceContext context, String mdId, String ip) throws Exception {
        OperationRepository _opRepository = context.getBean(OperationRepository.class);
        OperationAllowedRepository _opAllowedRepository = context.getBean(OperationAllowedRepository.class);

        HashSet<Operation> operations = new HashSet<Operation>();
        Set<Integer> groups = getUserGroups(context.getUserSession(),
            ip, false);
        for (OperationAllowed opAllow : _opAllowedRepository.findByMetadataId(mdId)) {
            if (groups.contains(opAllow.getId().getGroupId())) {
                operations.add(_opRepository.findOne(opAllow.getId().getOperationId()));
            }
        }
        return operations;
    }

    /**
     * Returns all groups accessible by the user (a set of ids).
     *
     * @param editingGroupsOnly TODO
     */
    public Set<Integer> getUserGroups(UserSession usrSess, String ip, boolean editingGroupsOnly) throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        GroupRepository _groupRepository = applicationContext.getBean(GroupRepository.class);
        UserGroupRepository _userGroupRepository = applicationContext.getBean(UserGroupRepository.class);

        Set<Integer> hs = new HashSet<Integer>();

        // add All (1) network group
        hs.add(ReservedGroup.all.getId());

        if (ip != null && isIntranet(ip))
            hs.add(ReservedGroup.intranet.getId());

        // get other groups
        if (usrSess.isAuthenticated()) {
            // add (-1) GUEST group
            hs.add(ReservedGroup.guest.getId());

            if (Profile.Administrator == usrSess.getProfile()) {
                List<Integer> allGroupIds = _groupRepository.findIds();

                hs.addAll(allGroupIds);
            } else {
                Specification<UserGroup> spec = UserGroupSpecs.hasUserId(usrSess.getUserIdAsInt());
                if (editingGroupsOnly) {
                    spec = Specifications.where(spec).and(UserGroupSpecs.hasProfile(Profile.Editor));
                }

                hs.addAll(_userGroupRepository.findGroupIds(spec));
            }
        }
        return hs;
    }

    public Set<Integer> getReviewerGroups(UserSession usrSess) throws Exception {
        Set<Integer> hs = new HashSet<Integer>();
        UserGroupRepository _userGroupRepository = ApplicationContextHolder.get().getBean(UserGroupRepository.class);

        // get other groups
        if (usrSess.isAuthenticated()) {
            Specification<UserGroup> spec =
                UserGroupSpecs.hasUserId(usrSess.getUserIdAsInt());
            spec = Specifications
                .where(spec)
                .and(UserGroupSpecs.hasProfile(Profile.Reviewer));

            hs.addAll(_userGroupRepository.findGroupIds(spec));
        }
        return hs;
    }

    /**
     * TODO javadoc.
     *
     * @param userId the id of the user
     */
    public Set<Integer> getVisibleGroups(final int userId) throws Exception {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);
        GroupRepository groupRepository = applicationContext.getBean(GroupRepository.class);

        Set<Integer> hs = new HashSet<Integer>();

        User user = userRepository.findOne(userId);

        if (user == null) {
            return hs;
        }

        Profile profile = user.getProfile();

        List<Integer> groupIds;
        if (profile == Profile.Administrator) {
            groupIds = groupRepository.findIds();
        } else {
            groupIds = userGroupRepository.findGroupIds(UserGroupSpecs.hasUserId(user.getId()));
        }

        hs.addAll(groupIds);

        return hs;
    }

    /**
     * Returns true if, and only if, at least one of these conditions is satisfied: <ul> <li>the
     * user is owner (@see #isOwner)</li> <li>the user has edit rights over the metadata</li> </ul>
     *
     * @param id The metadata internal identifier
     */
    public boolean canEdit(final ServiceContext context, final String id) throws Exception {
        return isOwner(context, id) || hasEditPermission(context, id);
    }

    /**
     * Returns true if, and only if, at least one of these conditions is satisfied: <ul> <li>the
     * user is owner (@see #isOwner)</li> <li>the user has reviewing rights over the metadata</li> </ul>
     *
     * @param id The metadata internal identifier
     */
    public boolean canReview(final ServiceContext context, final String id) throws Exception {
        return isOwner(context, id) || hasReviewPermission(context, id);
    }
    
    /**
     * Returns true if, and only if, at least one of these conditions is satisfied: <ul> <li>the
     * user is owner (@see #isOwner)</li> <li>the user has reviewing rights over owning group of the metadata</li> </ul>
     *
     * @param id The metadata internal identifier
     */
    public boolean canChangeStatus(final ServiceContext context, final String id) throws Exception {
        return hasOnwershipReviewPermission(context, id) || hasReviewPermission(context, id);
    }
    
    /**
     * Return true if the current user is: <ul> <li>administrator</li> <li>the metadata owner (the
     * user who created the record)</li> <li>reviewer in the group the metadata was created</li>
     * </ul>
     *
     * Note: old GeoNetwork was also restricting editing on harvested record. This is not restricted
     * on the server side anymore. If a record is harvested it could be edited by default but the
     * client application may restrict this condition.
     *
     * @param id The metadata internal identifier
     */
    public boolean isOwner(final ServiceContext context, final String id) throws Exception {

        //--- retrieve metadata info
        AbstractMetadata info = context.getBean(IMetadataUtils.class).findOne(id);

        if (info == null)
            return false;
        final MetadataSourceInfo sourceInfo = info.getSourceInfo();
        return isOwner(context, sourceInfo);
    }

    /**
     * Return true if the current user is: <ul> <li>administrator</li> <li>the metadata owner (the
     * user who created the record)</li> <li>reviewer in the group the metadata was created</li>
     * </ul>
     *
     * Note: old GeoNetwork was also restricting editing on harvested record. This is not restricted
     * on the server side anymore. If a record is harvested it could be edited by default but the
     * client application may restrict this condition.
     *
     * @param sourceInfo The metadata source/owner information
     */
    public boolean isOwner(ServiceContext context, MetadataSourceInfo sourceInfo) throws Exception {

        UserSession us = context.getUserSession();
        if (us == null || !us.isAuthenticated()) {
            return false;
        }

        //--- check if the user is an administrator
        final Profile profile = us.getProfile();
        if (profile == Profile.Administrator)
            return true;

        //--- check if the user is the metadata owner
        //
        if (us.getUserIdAsInt() == sourceInfo.getOwner())
            return true;

        //--- check if the user is a reviewer or useradmin
        if (profile != Profile.Reviewer && profile != Profile.UserAdmin)
            return false;

        //--- if there is no group owner then the reviewer cannot review and the useradmin cannot administer
        final Integer groupOwner = sourceInfo.getGroupOwner();
        if (groupOwner == null) {
            return false;
        }
        for (Integer userGroup : getReviewerGroups(us)) {
            if (userGroup == groupOwner.intValue())
                return true;
        }
        return false;
    }

    /**
     * TODO javadoc.
     */
    private String join(Set<Integer> set, String delim) {
        StringBuilder sb = new StringBuilder();
        String loopDelim = "";
        for (Integer s : set) {
            sb.append(loopDelim);
            sb.append(s + "");
            loopDelim = delim;
        }
        return sb.toString();
    }

    /**
     * Returns content reviewers for metadata records.
     */
    public Element getContentReviewers(ServiceContext context, final Set<Integer> metadataIds) throws Exception {
        List<Pair<Integer, User>> results = context.getBean(UserRepository.class).findAllByGroupOwnerNameAndProfile(metadataIds,
            Profile.Reviewer, SortUtils.createSort(User_.name));

        Element resultEl = new Element("results");
        for (Pair<Integer, User> integerUserPair : results) {
            User user = integerUserPair.two();
            resultEl.addContent(new Element("record").
                addContent(new Element("userid").setText(user.getId() + "")).
                addContent(new Element("name").setText(user.getName())).
                addContent(new Element("surname").setText(user.getSurname())).
                addContent(new Element("email").setText(user.getEmail()))
            );
        }
        return resultEl;
    }

    /**
     * Returns whether a particular metadata is visible to group 'all'.
     *
     * @param metadataId the id of the metadata
     */
    public boolean isVisibleToAll(final String metadataId) throws Exception {
        AbstractMetadata metadata = ApplicationContextHolder.get().getBean(IMetadataUtils.class).findOne(metadataId);
        if (metadata == null) {
            return false;
        } else {
            GroupRepositoryCustom groupRepository = ApplicationContextHolder.get().getBean(GroupRepository.class);
            Group allGroup = groupRepository.findReservedGroup(ReservedGroup.all);
            int opId = ReservedOperation.view.getId();
            return hasPermission(metadata, allGroup, opId);
        }
    }

    /**
     * Returns whether a particular metadata is downloadable.
     */
    public boolean canDownload(final ServiceContext context, final String id) throws Exception {
        if (isOwner(context, id)) {
            return true;
        }
        int downloadId = ReservedOperation.download.getId();
        Set<Operation> ops = getOperations(context, id, context.getIpAddress());
        for (Operation op : ops) {
            if (op.getId() == downloadId) {
                return true;
            }
        }
        return false;
    }

    public boolean canDynamic(final ServiceContext context, final String id) throws Exception {
        if (isOwner(context, id)) {
            return true;
        }
        int dynamicId = ReservedOperation.dynamic.getId();
        Set<Operation> ops = getOperations(context, id, context.getIpAddress());
        for (Operation op : ops) {
            if (op.getId() == dynamicId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the group has the permission.
     *
     * @param metadata the metadata object
     * @param group    the group to check
     * @param opId     the id of the operation to check for
     */
    public boolean hasPermission(final AbstractMetadata metadata, final Group group, final int opId) {
        OperationAllowedRepository opAllowedRepository = ApplicationContextHolder.get().getBean(OperationAllowedRepository.class);
        return opAllowedRepository.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(group.getId(), metadata.getId(), opId) != null;
    }

    /**
     * Check if current user can edit the metadata according to the groups where the metadata is
     * editable.
     *
     * @param id The metadata internal identifier
     */
    public boolean hasEditPermission(final ServiceContext context, final String id) throws Exception {
        return hasEditingPermissionWithProfile(context, id, Profile.Editor);

    }
    
    /**
     * Check if current user can review the metadata according to the groups where the metadata is
     * editable.
     *
     * @param id The metadata internal identifier
     */
    public boolean hasReviewPermission(final ServiceContext context, final String id) throws Exception {
        return hasEditingPermissionWithProfile(context, id, Profile.Reviewer);
    }


    /**
     * Check if current user has permission for the metadata according to the groups where the metadata is
     * editable and specific user profile.
     *
     * @param id The metadata internal identifier
     */
    private boolean hasEditingPermissionWithProfile(final ServiceContext context, final String id, Profile profile) throws Exception {
        UserSession us = context.getUserSession();
        if (us == null || !us.isAuthenticated())
            return false;


        OperationAllowedRepository opAllowedRepository = context.getBean(OperationAllowedRepository.class);
        UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
        List<OperationAllowed> allOpAlloweds = opAllowedRepository.findAll(where(hasMetadataId(id)).and(hasOperation(ReservedOperation
            .editing)));

        if (allOpAlloweds.isEmpty()) {
            return false;
        }

        Specifications spec = where(UserGroupSpecs.hasProfile(profile)).and(UserGroupSpecs.hasUserId(us.getUserIdAsInt()));

        List<Integer> opAlloweds = new ArrayList<Integer>();
        for (OperationAllowed opAllowed : allOpAlloweds) {
            opAlloweds.add(opAllowed.getId().getGroupId());
        }
        spec = spec.and(UserGroupSpecs.hasGroupIds(opAlloweds));

        return (!userGroupRepository.findAll(spec).isEmpty());

    }

    /**
     * Check if current user is reviewer of the owner group for this metadata
     *
     * @param id The metadata internal identifier
     */
    public boolean hasOnwershipReviewPermission(final ServiceContext context, final String id) throws Exception {
        UserSession us = context.getUserSession();
        if (us == null || !us.isAuthenticated())
            return false;


        OperationAllowedRepository opAllowedRepository = context.getBean(OperationAllowedRepository.class);
        UserGroupRepository userGroupRepository = context.getBean(UserGroupRepository.class);
        IMetadataUtils metadataUtils = context.getBean(IMetadataUtils.class);
        
        Specifications spec = where(UserGroupSpecs.hasProfile(Profile.Reviewer)).and(UserGroupSpecs.hasUserId(us.getUserIdAsInt()));

        List<Integer> opAlloweds = new ArrayList<Integer>();
        opAlloweds.add(metadataUtils.findOne(id).getSourceInfo().getGroupOwner());
        
        spec = spec.and(UserGroupSpecs.hasGroupIds(opAlloweds));

        return (!userGroupRepository.findAll(spec).isEmpty());
    }

    /**
     * TODO javadoc.
     */
    public int getPrivilegeId(final String name) {
        OperationRepository opRepository = ApplicationContextHolder.get().getBean(OperationRepository.class);
        final Operation op = opRepository.findByName(name);
        if (op == null) {
            throw new IllegalArgumentException("No Operation/privilege found with name: " + name);
        }
        return op.getId();
    }

    /**
     * TODO javadoc.
     */
    public String getPrivilegeName(int id) {
        OperationRepository opRepository = ApplicationContextHolder.get().getBean(OperationRepository.class);
        return opRepository.findOne(id).getName();
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean isIntranet(String ip) {
        //--- consider IPv4 & IPv6 loopback
        //--- we use 'startsWith' because some addresses can be 0:0:0:0:0:0:0:1%0

        if (ip.startsWith("0:0:0:0:0:0:0:1") || ip.equals("127.0.0.1")) return true;

        // IPv6 link-local
        String ipv6LinkLocalPrefix = "fe80:";
        if (ip.toLowerCase().startsWith(ipv6LinkLocalPrefix)) {
            return true;
        }
        // other IPv6
        else if (ip.indexOf(':') >= 0) {
            return false;
        }

        // IPv4

        SettingRepository settingRepository= ApplicationContextHolder.get().getBean(SettingRepository.class);
        Setting network = settingRepository.findOne(Settings.SYSTEM_INTRANET_NETWORK);
        Setting netmask = settingRepository.findOne(Settings.SYSTEM_INTRANET_NETMASK);

        try {
            if (network != null && netmask != null &&
                StringUtils.isNotEmpty(network.getValue()) && StringUtils.isNotEmpty(netmask.getValue())) {
                long lIntranetNet = getAddress(network.getValue());
                long lIntranetMask = getAddress(netmask.getValue());
                long lAddress = getAddress(ip.split(",")[0]);
                return (lAddress & lIntranetMask) == (lIntranetNet & lIntranetMask);
            }
        } catch (Exception nfe) {
            Log.error(Geonet.ACCESS_MANAGER,"isIntranet error: " + nfe.getMessage(), nfe);
        }
        return false;
    }

    /**
     * Converts an ip x.x.x.x into a long.
     */
    private long getAddress(String ip) {
        if (ip.trim().equals("?")) {
            return 0;
        } else {
            StringTokenizer st = new StringTokenizer(ip, ".");
            if (!st.hasMoreElements()) {
                return 0;
            }
            long a1 = Integer.parseInt(st.nextToken());
            if (!st.hasMoreElements()) {
                return 0;
            }
            long a2 = Integer.parseInt(st.nextToken());
            if (!st.hasMoreElements()) {
                return 0;
            }
            long a3 = Integer.parseInt(st.nextToken());
            if (!st.hasMoreElements()) {
                return 0;
            }
            long a4 = Integer.parseInt(st.nextToken());
            return a1 << 24 | a2 << 16 | a3 << 8 | a4;
        }
    }
}
