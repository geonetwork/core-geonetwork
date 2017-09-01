    package org.fao.geonet.kernel.datamanager;

import java.util.Map;
import java.util.Set;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.jdom.Element;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

public interface IMetadataManager {

        public void init(ServiceContext context, Boolean force) throws Exception;

        void deleteMetadata(ServiceContext context, String metadataId) throws Exception;

        void deleteMetadataGroup(ServiceContext context, String metadataId) throws Exception;

        String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
                String isTemplate, boolean fullRightsForGroup) throws Exception;

        String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
                String isTemplate, boolean fullRightsForGroup, String uuid) throws Exception;

        String insertMetadata(ServiceContext context, String schema, Element metadataXml, String uuid, int owner, String groupOwner,
                String source, String metadataType, String docType, String category, String createDate, String changeDate, boolean ufo,
                boolean index) throws Exception;

        Metadata insertMetadata(ServiceContext context, Metadata newMetadata, Element metadataXml, boolean notifyChange, boolean index,
                boolean updateFixedInfo, UpdateDatestamp updateDatestamp, boolean fullRightsForGroup, boolean forceRefreshReaders)
                throws Exception;

        Element getMetadata(String id) throws Exception;

        Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean withEditorValidationErrors,
                boolean keepXlinkAttributes) throws Exception;

        void updateMetadataOwner(int id, String owner, String groupOwner) throws Exception;

        Metadata updateMetadata(ServiceContext context, String metadataId, Element md, boolean validate, boolean ufo, boolean index,
                String lang, String changeDate, boolean updateDateStamp) throws Exception;

        void buildPrivilegesMetadataInfo(ServiceContext context, Map<String, Element> mdIdToInfoMap) throws Exception;

        Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, Object> params)
                throws Exception;

        Element updateFixedInfo(String schema, Optional<Integer> metadataId, String uuid, Element md, String parentUuid,
                UpdateDatestamp updateDatestamp, ServiceContext context) throws Exception;

        void flush();

        EditLib getEditLib();
}

  