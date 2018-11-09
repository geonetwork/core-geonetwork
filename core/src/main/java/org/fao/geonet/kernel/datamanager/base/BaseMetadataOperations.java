package org.fao.geonet.kernel.datamanager.base;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.events.md.MetadataPublished;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.datamanager.IMetadataOperations;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.repository.specification.UserSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

public class BaseMetadataOperations implements IMetadataOperations, ApplicationEventPublisherAware  {

    @Autowired
    private IMetadataUtils metadataUtils;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OperationAllowedRepository opAllowedRepo;
    @Autowired
    private UserGroupRepository userGroupRepo;
    @Autowired
    @Lazy
    private SettingManager settingManager;
    @Autowired(required=false)
    private SvnManager svnManager;

    private ApplicationEventPublisher eventPublisher;
    
    /**
     * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
     */
    @Override
    public void setApplicationEventPublisher(
        ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }


    public void init(ServiceContext context, Boolean force) throws Exception {
        userRepository = context.getBean(UserRepository.class);
        metadataUtils = context.getBean(IMetadataUtils.class);
        opAllowedRepo = context.getBean(OperationAllowedRepository.class);
        userGroupRepo = context.getBean(UserGroupRepository.class);
        settingManager = context.getBean(SettingManager.class);
        svnManager = context.getBean(SvnManager.class);
    }

    /**
     * Removes all operations stored for a metadata.
     */
    @Override
    public void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception {
        if (skipAllReservedGroup) {
            int[] exclude = new int[] { ReservedGroup.all.getId(), ReservedGroup.intranet.getId(), ReservedGroup.guest.getId() };
            opAllowedRepo.deleteAllByMetadataIdExceptGroupId(Integer.parseInt(metadataId), exclude);
        } else {
            opAllowedRepo.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(metadataId));
        }
    }

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    @Override
    public void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception {
        setOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId), op.getId());
    }

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    @Override
    public void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        setOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId), Integer.valueOf(opId));
    }

    /**
     * Set metadata privileges.
     *
     * Administrator can set operation for any groups.
     *
     * For reserved group (ie. Internet, Intranet & Guest), user MUST be reviewer of one group. For other group, if "Only set privileges to
     * user's groups" is set in catalog configuration user MUST be a member of the group.
     *
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * @return true if the operation was set.
     */
    @Override
    public boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception {
        Optional<OperationAllowed> opAllowed = getOperationAllowedToAdd(context, mdId, grpId, opId);

        // Set operation
        if (opAllowed.isPresent()) {
            return forceSetOperation(context, mdId, grpId, opId);
        }

        return false;
    }

    /**
     * Set metadata privileges.
     *
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * @return true if the operation was set.
     */
    @Override
    public boolean forceSetOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception {
        Optional<OperationAllowed> opAllowed = _getOperationAllowedToAdd(context, mdId, grpId, opId, false);

        if(opAllowed.isPresent()) {
	        opAllowedRepo.save(opAllowed.get());
	        svnManager.setHistory(mdId + "", context);
	        
	        //If it is published/unpublished, throw event
	        if(opId == ReservedOperation.view.getId() 
	                && grpId == ReservedGroup.all.getId()) {
	            this.eventPublisher.publishEvent(new MetadataPublished(
	                    metadataUtils.findOne(Integer.valueOf(mdId))));
	        }
	        
	        return true;
        }
        
        return false;
    }

    /**
     * Check that the operation has not been added and if not that it can be added.
     * <ul>
     * <li>If the operation can be added then an non-empty optional is return.</li>
     * <li>If it has already been added the return empty optional</li>
     * <li>If it is not permitted to be added throw exception.</li>
     * </ul>
     */
    @Override
    public Optional<OperationAllowed> getOperationAllowedToAdd(final ServiceContext context, final int mdId, final int grpId,
            final int opId) {
        return _getOperationAllowedToAdd(context, mdId, grpId, opId, true);
    }

    private Optional<OperationAllowed> _getOperationAllowedToAdd(final ServiceContext context, final int mdId, final int grpId,
            final int opId, boolean shouldCheckPermission) {
        final OperationAllowed operationAllowed = opAllowedRepo.findOneById_GroupIdAndId_MetadataIdAndId_OperationId(grpId, mdId, opId);

        if (operationAllowed == null && shouldCheckPermission) {
            checkOperationPermission(context, grpId, userGroupRepo);
        }

        if (operationAllowed == null) {
            return Optional.of(new OperationAllowed(new OperationAllowedId().setGroupId(grpId).setMetadataId(mdId).setOperationId(opId)));
        } else {
            return Optional.absent();
        }
    }

    @Override
    public void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo) {
        // Check user privileges
        // Session may not be defined when a harvester is running
        if (context.getUserSession() != null) {
            Profile userProfile = context.getUserSession().getProfile();
            if (!(userProfile == Profile.Administrator || userProfile == Profile.UserAdmin)) {
                int userId = context.getUserSession().getUserIdAsInt();
                // Reserved groups
                if (ReservedGroup.isReserved(grpId)) {

                    Specification<UserGroup> hasUserIdAndProfile = where(UserGroupSpecs.hasProfile(Profile.Reviewer))
                            .and(UserGroupSpecs.hasUserId(userId));
                    List<Integer> groupIds = userGroupRepo.findGroupIds(hasUserIdAndProfile);

                    if (groupIds.isEmpty()) {
                        throw new ServiceNotAllowedEx(
                                "User can't set operation for group " + grpId + " because the user in not a " + "Reviewer of any group.");
                    }
                } else {
                    String userGroupsOnly = settingManager.getValue(Settings.SYSTEM_METADATAPRIVS_USERGROUPONLY);
                    if (userGroupsOnly.equals("true")) {
                        // If user is member of the group, user can set operation

                        if (userGroupRepo.exists(new UserGroupId().setGroupId(grpId).setUserId(userId))) {
                            throw new ServiceNotAllowedEx(
                                    "User can't set operation for group " + grpId + " because the user in not" + " member of this group.");
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    @Override
    public void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception {
        unsetOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId), opId.getId());
    }

    /**
     *
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    @Override
    public void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception {
        unsetOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId), Integer.valueOf(opId));
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Check User Id to avoid foreign key problems
    // ---
    // --------------------------------------------------------------------------

    /**
     * @param mdId metadata id
     * @param groupId group id
     * @param operId operation id
     */
    @Override
    public void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception {
        checkOperationPermission(context, groupId, context.getBean(UserGroupRepository.class));
        forceUnsetOperation(context, mdId, groupId, operId);
    }

    /**
     * Unset operation without checking if user privileges allows the operation. This may be useful when a user is an editor and internal
     * operations needs to update privilages for reserved group. eg. {@link org.fao.geonet.kernel.metadata.DefaultStatusActions}
     */
    @Override
    public void forceUnsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception {
        OperationAllowedId id = new OperationAllowedId().setGroupId(groupId).setMetadataId(mdId).setOperationId(operId);
        if (opAllowedRepo.exists(id)) {
            opAllowedRepo.delete(id);
            if (svnManager != null) {
                svnManager.setHistory(mdId + "", context);
            }
            
            //If it is published/unpublished, throw event
            if(operId == ReservedOperation.view.getId() 
                    && groupId == ReservedGroup.all.getId()) {

                this.eventPublisher.publishEvent(new MetadataUnpublished(
                        metadataUtils.findOne(Integer.valueOf(mdId))));
            }

        }
    }

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context service context
     * @param id metadata id
     * @param groupId group id
     * @param fullRightsForGroup 
     * @throws Exception hmmm
     */
    @Override
    public void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception {
        if (StringUtils.isBlank(groupId)) {
            Log.info(Geonet.DATA_MANAGER, "Attempt to set default privileges for metadata " + id + " to an empty groupid");
            return;
        }
        // --- store access operations for group

        setOperation(context, id, groupId, ReservedOperation.view);
        setOperation(context, id, groupId, ReservedOperation.notify);
        //
        // Restrictive: new and inserted records should not be editable,
        // their resources can't be downloaded and any interactive maps can't be
        // displayed by users in the same group
        if (fullRightsForGroup) {
            setOperation(context, id, groupId, ReservedOperation.editing);
            setOperation(context, id, groupId, ReservedOperation.download);
            setOperation(context, id, groupId, ReservedOperation.dynamic);
        }
        // Ultimately this should be configurable elsewhere
    }

    @Override
    public boolean isUserMetadataOwner(int userId) throws Exception {
        return metadataUtils.count(MetadataSpecs.isOwnedByUser(userId)) > 0;
    }

    @Override
    public boolean existsUser(ServiceContext context, int id) throws Exception {
        return userRepository.count(where(UserSpecs.hasUserId(id))) > 0;
    }

    @Override
    public Collection<OperationAllowed> getAllOperations(int id) {
        return opAllowedRepo.findAllById_MetadataId(id);
  }
}
