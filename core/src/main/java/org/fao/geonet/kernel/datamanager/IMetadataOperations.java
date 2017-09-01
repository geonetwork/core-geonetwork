    package org.fao.geonet.kernel.datamanager;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;
import org.fao.geonet.domain.OperationAllowed;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.repository.UserGroupRepository;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

public interface IMetadataOperations {

        public void init(ServiceContext context, Boolean force) throws Exception;

        void deleteMetadataOper(ServiceContext context, String metadataId, boolean skipAllReservedGroup) throws Exception;

        void setOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

        void setOperation(ServiceContext context, String mdId, String grpId, ReservedOperation op) throws Exception;

        void copyDefaultPrivForGroup(ServiceContext context, String id, String groupId, boolean fullRightsForGroup) throws Exception;

        void forceUnsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

        void unsetOperation(ServiceContext context, int mdId, int groupId, int operId) throws Exception;

        boolean setOperation(ServiceContext context, int mdId, int grpId, int opId) throws Exception;

        Optional<OperationAllowed> getOperationAllowedToAdd(ServiceContext context, int mdId, int grpId, int opId);

        void checkOperationPermission(ServiceContext context, int grpId, UserGroupRepository userGroupRepo);

        void unsetOperation(ServiceContext context, String mdId, String grpId, ReservedOperation opId) throws Exception;

        void unsetOperation(ServiceContext context, String mdId, String grpId, String opId) throws Exception;

        boolean isUserMetadataOwner(int userId) throws Exception;

        boolean existsUser(ServiceContext context, int id) throws Exception;
}

  