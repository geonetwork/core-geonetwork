/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.util.Calendar;
import java.util.List;

import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

/**
 * Addon to {@link DataManager} to handle metadata actions related to indexing
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataIndexer {
    /**
     * Search for all records having XLinks (ie. indexed with _hasxlinks flag),
     * clear the cache and reindex all records found.
     *
     * Remember to make this function synchronous to avoid problems on index
     * writing
     *
     * @param context
     * @throws Exception
     */
    public void rebuildIndexXLinkedMetadata(final ServiceContext context)
            throws Exception;

    /**
     * Reindex all records in current selection.
     *
     * @param context
     * @param clearXlink
     * @throws Exception
     */
    public void rebuildIndexForSelection(final ServiceContext context,
            boolean clearXlink) throws Exception;

    /**
     * Index multiple metadata in a separate thread. Wait until the current
     * transaction commits before starting threads (to make sure that all
     * metadata are committed).
     *
     * @param context
     *            context object
     * @param metadataIds
     *            the metadata ids to index
     */
    public void batchIndexInThreadPool(ServiceContext context,
            List<?> metadataIds);

    public boolean isIndexing();

    public void indexMetadata(final List<String> metadataIds) throws Exception;

    /**
     * TODO javadoc.
     *
     * @param metadataId
     * @throws Exception
     */
    public void indexMetadata(final String metadataId,
            boolean forceRefreshReaders) throws Exception;

    /**
     *
     * @param beginAt
     * @param interval
     * @throws Exception
     */
    public void rescheduleOptimizer(Calendar beginAt, int interval)
            throws Exception;

    /**
     * @throws Exception
     */
    void disableOptimizer() throws Exception;

    public int batchDeleteMetadataAndUpdateIndex(
            Specification<Metadata> specification) throws Exception;
}
