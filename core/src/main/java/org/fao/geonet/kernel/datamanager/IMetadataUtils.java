    package org.fao.geonet.kernel.datamanager;

import org.fao.geonet.domain.MetadataType;
import org.jdom.Element;

import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

public interface IMetadataUtils {

        public void init(ServiceContext context, Boolean force) throws Exception;

        void notifyMetadataChange(Element md, String metadataId) throws Exception;

        String getMetadataUuid(String id) throws Exception;

        void startEditingSession(ServiceContext context, String id) throws Exception;

        void cancelEditingSession(ServiceContext context, String id) throws Exception;

        void endEditingSession(String id, UserSession session);

        Element enumerateTree(Element md) throws Exception;

        String extractUUID(String schema, Element md) throws Exception;

        String extractDefaultLanguage(String schema, Element md) throws Exception;

        String extractDateModified(String schema, Element md) throws Exception;

        Element setUUID(String schema, String uuid, Element md) throws Exception;

        Element extractSummary(Element md) throws Exception;

        String getMetadataId(String uuid) throws Exception;

        String getVersion(String id);

        String getNewVersion(String id);

        void setTemplateExt(int id, MetadataType metadataType) throws Exception;

        void setTemplate(int id, MetadataType type, String title) throws Exception;

        void setHarvestedExt(int id, String harvestUuid) throws Exception;

        void setHarvested(int id, String harvestUuid) throws Exception;

        void setHarvestedExt(int id, String harvestUuid, Optional<String> harvestUri) throws Exception;

        void updateDisplayOrder(String id, String displayOrder) throws Exception;

        void increasePopularity(ServiceContext srvContext, String id) throws Exception;

        int rateMetadata(int metadataId, String ipAddress, int rating) throws Exception;

        Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception;

        Element getElementByRef(Element md, String ref);

        boolean existsMetadataUuid(String uuid) throws Exception;

        boolean existsMetadata(int id) throws Exception;

        Element getKeywords() throws Exception;

        Element getThumbnails(ServiceContext context, String metadataId) throws Exception;

        void setThumbnail(ServiceContext context, String id, boolean small, String file, boolean indexAfterChange) throws Exception;

        void unsetThumbnail(ServiceContext context, String id, boolean small, boolean indexAfterChange) throws Exception;

        void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename,
                String type) throws Exception;

        void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction,
                String licensename, String type) throws Exception;

        void setMetadataManager(IMetadataManager metadataManager);

        public String getMetadataTitle(String id) throws Exception;

        public void setSubtemplateTypeAndTitleExt(int id, String title) throws Exception;

}

  