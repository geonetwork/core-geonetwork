    package org.fao.geonet.kernel.datamanager;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;

import jeeves.server.context.ServiceContext;

public interface IMetadataStatus {

        public void init(ServiceContext context, Boolean force) throws Exception;

        boolean isUserMetadataStatus(int userId) throws Exception;

        void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception;

        MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

        MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

        String getCurrentStatus(int metadataId) throws Exception;

        MetadataStatus getStatus(int metadataId) throws Exception;
}

  