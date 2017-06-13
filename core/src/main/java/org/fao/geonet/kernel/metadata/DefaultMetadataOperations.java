/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import static org.springframework.data.jpa.domain.Specifications.where;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.OperationAllowedId;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.domain.UserGroupId;
import org.fao.geonet.events.md.MetadataPublished;
import org.fao.geonet.events.md.MetadataUnpublished;
import org.fao.geonet.exceptions.ServiceNotAllowedEx;
import org.fao.geonet.kernel.SvnManager;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public class DefaultMetadataOperations implements IMetadataOperations, ApplicationEventPublisherAware {

    @Autowired
    private OperationAllowedRepository operationAllowedRepository;

    /**
     * @param context
     */
    @Override
    public void init(ServiceContext context) {
        this.operationAllowedRepository = context
                .getBean(OperationAllowedRepository.class);
    }
    
    private ApplicationEventPublisher eventPublisher;
    
    /**
     * @see org.springframework.context.ApplicationEventPublisherAware#setApplicationEventPublisher(org.springframework.context.ApplicationEventPublisher)
     */
    @Override
    public void setApplicationEventPublisher(
        ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    @Override
    public void setOperation(ServiceContext context, String mdId, String grpId,
            ReservedOperation op) throws Exception {
        setOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId),
                op.getId());
    }

    @Override
    public void setOperation(ServiceContext context, String mdId, String grpId,
            String opId) throws Exception {
        setOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId),
                Integer.valueOf(opId));
    }

    @Override
    public boolean setOperation(ServiceContext context, Integer mdId, Integer grpId,
            ReservedOperation opId) throws Exception {
        return setOperation(context, mdId, grpId, opId.getId());
    }

    @Override
    public Boolean forceSetOperation(ServiceContext context, int mdId, int grpId,
            int opId) throws Exception {
        Optional<OperationAllowed> opAllowed = getOperationAllowedToAdd(context,
                mdId, grpId, opId, false);

        // Set operation
        if (opAllowed.isPresent()) {
            operationAllowedRepository.save(opAllowed.get());
            context.getBean(SvnManager.class).setHistory(mdId + "", context);
            
            //If it is published/unpublished, throw event
            if(opId == ReservedOperation.view.getId() 
                    && grpId == ReservedGroup.all.getId()) {
                IMetadataManager mdManager = context
                        .getBean(IMetadataManager.class);
                this.eventPublisher.publishEvent(new MetadataPublished(
                        mdManager.getMetadataObject(Integer.valueOf(mdId))));
            }
            
            return true;
        }

        return false;
    }
    
    @Override
    public boolean setOperation(ServiceContext context, int mdId, int grpId,
            int opId) throws Exception {
        Optional<OperationAllowed> opAllowed = getOperationAllowedToAdd(context,
                mdId, grpId, opId);

        // Set operation
        if (opAllowed.isPresent()) {
            operationAllowedRepository.save(opAllowed.get());
            context.getBean(SvnManager.class).setHistory(mdId + "", context);
            
            //If it is published/unpublished, throw event
            if(opId == ReservedOperation.view.getId() 
                    && grpId == ReservedGroup.all.getId()) {
                IMetadataManager mdManager = context
                        .getBean(IMetadataManager.class);
                this.eventPublisher.publishEvent(new MetadataPublished(
                        mdManager.getMetadataObject(Integer.valueOf(mdId))));
            }
            
            return true;
        }

        return false;
    }


    @Override
    public Optional<OperationAllowed> getOperationAllowedToAdd(
            final ServiceContext context, final int mdId, final int grpId,
            final int opId) {
        return getOperationAllowedToAdd(context, mdId, grpId, opId, true);
    }

    public Optional<OperationAllowed> getOperationAllowedToAdd(
            final ServiceContext context, final int mdId, final int grpId,
            final int opId, boolean checkPrivileges) {
        UserGroupRepository userGroupRepo = context
                .getBean(UserGroupRepository.class);
        final OperationAllowed operationAllowed = operationAllowedRepository
                .findOneById_GroupIdAndId_MetadataIdAndId_OperationId(grpId, mdId, opId);

            if (operationAllowed == null && checkPrivileges) {
                checkOperationPermission(context, grpId, userGroupRepo);
            }

            if (operationAllowed == null) {
                return Optional.of(new OperationAllowed(new OperationAllowedId().setGroupId(grpId).setMetadataId(mdId).setOperationId(opId)));
            } else {
                return Optional.absent();
            }
    }

    public void checkOperationPermission(ServiceContext context, int grpId,
            UserGroupRepository userGroupRepo) {
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
                        throw new ServiceNotAllowedEx("User can't set operation for group " + grpId + " because the user in not a "
                            + "Reviewer of any group.");
                    }
                } else {
                    String userGroupsOnly = context
                            .getBean(SettingManager.class)
                            .getValue(Settings.SYSTEM_METADATAPRIVS_USERGROUPONLY);
                    if (userGroupsOnly.equals("true")) {
                        // If user is member of the group, user can set operation

                        if (userGroupRepo.exists(new UserGroupId().setGroupId(grpId).setUserId(userId))) {
                            throw new ServiceNotAllowedEx("User can't set operation for group " + grpId + " because the user in not"
                                + " member of this group.");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, ReservedOperation opId) throws Exception {
        unsetOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId),
                opId.getId());
    }

    @Override
    public void unsetOperation(ServiceContext context, String mdId,
            String grpId, String opId) throws Exception {
        unsetOperation(context, Integer.parseInt(mdId), Integer.parseInt(grpId),
                Integer.valueOf(opId));
    }

    @Override
    public void unsetOperation(ServiceContext context, int mdId, int groupId,
            int operId) throws Exception {
        checkOperationPermission(context, groupId,
                context.getBean(UserGroupRepository.class));
        forceUnsetOperation(context, mdId, groupId, operId);
    }

    @Override
    public void forceUnsetOperation(ServiceContext context, int mdId,
            int groupId, int operId) throws Exception {
        OperationAllowedId id = new OperationAllowedId().setGroupId(groupId)
                .setMetadataId(mdId).setOperationId(operId);
        if (operationAllowedRepository.exists(id)) {
          operationAllowedRepository.delete(id);
            SvnManager svnManager = context.getBean(SvnManager.class);
            if (svnManager != null) {
                svnManager.setHistory(mdId + "", context);
            }
        }
        //If it is published/unpublished, throw event
        if(operId == ReservedOperation.view.getId() 
                && groupId == ReservedGroup.all.getId()) {

            IMetadataManager mdManager = context
                    .getBean(IMetadataManager.class);
            this.eventPublisher.publishEvent(new MetadataUnpublished(
                    mdManager.getMetadataObject(Integer.valueOf(mdId))));
        }
    }

    @Override
    public void copyDefaultPrivForGroup(ServiceContext context, String id,
            String groupId, boolean fullRightsForGroup) throws Exception {
        if (StringUtils.isBlank(groupId)) {
            Log.info(Geonet.DATA_MANAGER,
                    "Attempt to set default privileges for metadata " + id
                            + " to an empty groupid");
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
    public List<OperationAllowed> getAllOperations(Integer mdId) {
      Specification<OperationAllowed> spec = OperationAllowedSpecs.hasMetadataId(mdId);
      return operationAllowedRepository.findAll(spec );
    }
}
