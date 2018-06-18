package org.fao.geonet.kernel.datamanager;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;

import jeeves.server.context.ServiceContext;

/**
 * Interface to handle all actions related to status of a record
 * 
 * @author delawen
 *
 */
public interface IMetadataStatus {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     * 
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Returns if the user has at least one metadata status associated
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    boolean isUserMetadataStatus(int userId) throws Exception;

    /**
     * If groupOwner match regular expression defined in setting metadata/workflow/draftWhenInGroup, then set status to draft to enable
     * workflow.
     */
    void activateWorkflowIfConfigured(ServiceContext context, String newId, String groupOwner) throws Exception;

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    MetadataStatus setStatusExt(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     * @return the saved status entity object
     */
    MetadataStatus setStatus(ServiceContext context, int id, int status, ISODate changeDate, String changeMessage) throws Exception;

    /**
     * Given a metadata id, return the name of the status of the metadata
     * 
     * @param metadataId
     * @return
     * @throws Exception
     */
    String getCurrentStatus(int metadataId) throws Exception;

    /**
     * Given a metadata id, return the status of the metadata
     * 
     * @param metadataId
     * @return
     * @throws Exception
     */
    MetadataStatus getStatus(int metadataId) throws Exception;
}
