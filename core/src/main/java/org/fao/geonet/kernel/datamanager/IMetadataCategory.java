    package org.fao.geonet.kernel.datamanager;

import java.util.Collection;

import org.fao.geonet.domain.MetadataCategory;

import jeeves.server.context.ServiceContext;

public interface IMetadataCategory {
        public void init(ServiceContext context, Boolean force) throws Exception;

        boolean isCategorySet(String mdId, int categId) throws Exception;

        void setCategory(ServiceContext context, String mdId, String categId) throws Exception;

        void unsetCategory(ServiceContext context, String mdId, int categId) throws Exception;

        Collection<MetadataCategory> getCategories(String mdId) throws Exception;
}

  