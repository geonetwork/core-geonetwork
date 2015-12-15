/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.Collection;

import org.fao.geonet.domain.MetadataCategory;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataCategory {

    /**
     * Adds a category to a metadata. Metadata is not reindexed.
     *
     * @param mdId
     * @param categId
     * @throws Exception
     */
    public void setCategory(ServiceContext context, String mdId, String categId)
            throws Exception;

    /**
     *
     * @param mdId
     * @param categId
     * @return
     * @throws Exception
     */
    public boolean isCategorySet(final String mdId, final int categId)
            throws Exception;

    /**
     *
     * @param mdId
     * @param categId
     * @throws Exception
     */
    public void unsetCategory(final ServiceContext context, final String mdId,
            final int categId) throws Exception;

    /**
     *
     * @param mdId
     * @return
     * @throws Exception
     */
    public Collection<MetadataCategory> getCategories(final String mdId)
            throws Exception;

}
