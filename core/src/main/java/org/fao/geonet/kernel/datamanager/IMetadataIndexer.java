    package org.fao.geonet.kernel.datamanager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.search.ISearchManager;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

public interface IMetadataIndexer {

        public void init(ServiceContext context, Boolean force) throws Exception;

        void forceIndexChanges() throws IOException;

        int batchDeleteMetadataAndUpdateIndex(Specification<Metadata> specification) throws Exception;

        void rebuildIndexXLinkedMetadata(ServiceContext context) throws Exception;

        void rebuildIndexForSelection(ServiceContext context, String bucket, boolean clearXlink) throws Exception;

        void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds);

        boolean isIndexing();

        void indexMetadata(List<String> metadataIds) throws Exception;

        void indexMetadata(String metadataId, boolean forceRefreshReaders, ISearchManager searchManager) throws Exception;

        void versionMetadata(ServiceContext context, String id, Element md) throws Exception;

        void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception;

        void disableOptimizer() throws Exception;

        void setMetadataUtils(IMetadataUtils metadataUtils);
}

  