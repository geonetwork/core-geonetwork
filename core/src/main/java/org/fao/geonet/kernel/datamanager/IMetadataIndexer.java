package org.fao.geonet.kernel.datamanager;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.kernel.search.ISearchManager;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import jeeves.server.context.ServiceContext;

/**
 * Interface to handle all indexing operations
 * 
 * @author delawen
 *
 */
public interface IMetadataIndexer {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Force the index to wait until all changes are processed and the next reader obtained will get the latest data.
     */
    void forceIndexChanges() throws IOException;

    /**
     * Remove the records that matches the specification
     * 
     * @param specification
     * @return
     * @throws Exception
     */
    int batchDeleteMetadataAndUpdateIndex(Specification<? extends IMetadata> specification) throws Exception;

    /**
     * Search for all records having XLinks (ie. indexed with _hasxlinks flag), clear the cache and reindex all records found.
     */
    void rebuildIndexXLinkedMetadata(ServiceContext context) throws Exception;

    /**
     * Reindex all records in current selection.
     */
    void rebuildIndexForSelection(ServiceContext context, String bucket, boolean clearXlink) throws Exception;

    /**
     * Index multiple metadata in a separate thread. Wait until the current transaction commits before starting threads (to make sure that
     * all metadata are committed).
     *
     * @param context context object
     * @param metadataIds the metadata ids to index
     */
    void batchIndexInThreadPool(ServiceContext context, List<?> metadataIds);

    /**
     * Is the platform currently indexing?
     * 
     * @return
     */
    boolean isIndexing();

    /**
     * Index the list of records passed as parameter in order.
     * 
     * @param metadataIds
     * @throws Exception
     */
    void indexMetadata(List<String> metadataIds) throws Exception;

    /**
     * Index one record defined by metadataId
     * 
     * @param metadataId
     * @param forceRefreshReaders
     * @param searchManager
     * @throws Exception
     */
    void indexMetadata(String metadataId, boolean forceRefreshReaders, ISearchManager searchManager) throws Exception;

    /**
     * Start record versioning
     * 
     * @param context
     * @param id
     * @param md
     * @throws Exception
     */
    void versionMetadata(ServiceContext context, String id, Element md) throws Exception;

    /**
     * Reschedule the Index Optimizer Manager (Lucene)
     * 
     * @param beginAt
     * @param interval
     * @throws Exception
     */
    void rescheduleOptimizer(Calendar beginAt, int interval) throws Exception;

    /**
     * Disable the Index Optimizer (Lucene)
     * 
     * @throws Exception
     */
    void disableOptimizer() throws Exception;

    /**
     * Helper function to avoid loop circular dependencies
     * 
     * @param metadataUtils
     */
    void setMetadataUtils(IMetadataUtils metadataUtils);

    /**
     * Helper function to avoid loop circular dependencies
     * 
     * @param metadataUtils
     */
    void setMetadataManager(IMetadataManager baseMetadataManager);

}
