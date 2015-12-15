/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataStatus;

import jeeves.server.context.ServiceContext;

/**
 * trunk-core
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataStatus {

    /**
     * Return all status records for the metadata id - current status is the
     * first child due to sort by DESC on changeDate
     *
     * @param metadataId
     * @return
     * @throws Exception
     *
     */
    public MetadataStatus getStatus(int metadataId) throws Exception;

    /**
     * Return status of metadata id.
     *
     * @param metadataId
     * @return
     * @throws Exception
     *
     */
    public String getCurrentStatus(int metadataId) throws Exception;

    /**
     * Set status of metadata id and reindex metadata id afterwards.
     *
     *
     * @param context
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     *
     * @return the saved status entity object
     */
    public MetadataStatus setStatus(ServiceContext context, int id, int status,
            ISODate changeDate, String changeMessage) throws Exception;

    /**
     * Set status of metadata id and do not reindex metadata id afterwards.
     *
     *
     * @param context
     * @param id
     * @param status
     * @param changeDate
     * @param changeMessage
     * @throws Exception
     *
     * @return the saved status entity object
     */
    public MetadataStatus setStatusExt(ServiceContext context, int id,
            int status, ISODate changeDate, String changeMessage)
                    throws Exception;

    /**
     * If groupOwner match regular expression defined in setting
     * metadata/workflow/draftWhenInGroup, then set status to draft to enable
     * workflow.
     *
     * @param context
     * @param newId
     * @param groupOwner
     * @throws Exception
     */
    public void activateWorkflowIfConfigured(ServiceContext context,
            String newId, String groupOwner) throws Exception;
}
