package org.fao.geonet.kernel.datamanager;

import java.util.Collection;

import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.UserGroupRepository;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * Interface for hanlding record privileges
 * 
 * @author delawen
 *
 */
public interface IMetadataOperations {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Removes all operations stored for a metadata.
     */
    void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception;

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

    /**
     * Adds a permission to a group. Metadata is not reindexed.
     */
    void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception;

    /**
     * Sets VIEW and NOTIFY privileges for a metadata to a group.
     *
     * @param context service context
     * @param id metadata id
     * @param groupId group id
     * @param fullRightsForGroup
     * @throws Exception hmmm
     */
    void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception;

    /**
     * Unset operation without checking if user privileges allows the operation. This may be useful when a user is an editor and internal
     * operations needs to update privilages for reserved group. eg. {@link org.fao.geonet.kernel.metadata.DefaultStatusActions}
     */
    void forceUnsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

    /**
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param groupId
     * @param operId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

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
    boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception;

    /**
     * Set metadata privileges even if the user logged in does not have privileges
     *
     * @param mdId The metadata identifier
     * @param grpId The group identifier
     * @param opId The operation identifier
     * @return true if the operation was set.
     */
    boolean forceSetOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception;

    /**
     * Check that the operation has not been added and if not that it can be added.
     * <ul>
     * <li>If the operation can be added then an non-empty optional is return.</li>
     * <li>If it has already been added the return empty optional</li>
     * <li>If it is not permitted to be added throw exception.</li>
     * </ul>
     */
    Optional<OperationAllowed> getOperationAllowedToAdd(ServiceContext context, int mdId, int grpId, int opId);

    /**
     * Check if the user calling have privileges over a group
     * 
     * @param context
     * @param grpId
     * @param userGroupRepo
     */
    void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo);

    /**
     * 
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception;

    /**
     * 
     * Removes a privilege if the user calling this function have enough privileges.
     * 
     * This is the "safe" version of {@link #forceUnsetOperation(ServiceContext, int, int, int)}
     * 
     * @param context
     * @param mdId
     * @param grpId
     * @param opId
     * @throws Exception
     */
    void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

    /**
     * Checks if a user owns metadata
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    boolean isUserMetadataOwner(int userId) throws Exception;

    /**
     * Checks if a user exists
     * 
     * @param context
     * @param id
     * @return
     * @throws Exception
     */
    boolean existsUser(ServiceContext context, int id) throws Exception;

    /**
     * Return all operations related to one record
     * 
     * @param id
     * @return
     */
    public Collection<OperationAllowed> getAllOperations(int id);
}
