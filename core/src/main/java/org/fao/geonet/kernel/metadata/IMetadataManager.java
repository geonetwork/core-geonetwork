/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.io.IOException;

import org.fao.geonet.domain.IMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.jdom.Element;
import org.jdom.JDOMException;

import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Addon to {@link DataManager} to handle metadata actions
 * 
 * @author delawen
 * 
 * 
 */
public interface IMetadataManager {
    

    
    /**
     *FIXME
     * To remove when Spring autowiring works right
     * @param context
     */
    public void init(ServiceContext context);

    /**
     * Init Data manager and refresh index if needed. Can also be called after
     * GeoNetwork startup in order to rebuild the lucene index
     * 
     * Don't forget to synchronize on the implementation!
     *
     * @param context
     * @param force
     *            Force reindexing all from scratch
     *
     **/
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Start an editing session. This will record the original metadata record
     * in the session under the
     * {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES}
     * + id session property.
     * 
     * The record contains geonet:info element.
     * 
     * Note: Only the metadata record is stored in session. If the editing
     * session upload new documents or thumbnails, those documents will not be
     * cancelled. This needs improvements.
     * 
     * @param context
     * @param id
     * @param lock Should I lock the edit of this metadata
     * @throws Exception
     */
    public String startEditingSession(ServiceContext context, String id, Boolean lock)
            throws Exception;

    /**
     * @param context
     * @param id
     * @throws Exception
     */
    public void cancelEditingSession(ServiceContext context, String id)
            throws Exception;

    /**
     * @param id
     * @param session
     */
    public void endEditingSession(String id, UserSession session);

    /**
     * @param context
     * @param templateId
     * @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate
     * @param fullRightsForGroup
     * @param uuid
     * @throws JDOMException
     * @throws IOException
     * @throws Exception
     */
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup, String uuid)
                    throws IOException, JDOMException, Exception;

    /**
     * 
     * @param context
     * @param templateId
     * @param groupOwner
     * @param source
     * @param owner
     * @param parentUuid
     * @param isTemplate
     * @param fullRightsForGroup
     * @return
     * @throws Exception
     */
    public String createMetadata(ServiceContext context, String templateId,
            String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup) throws Exception;

    /**
     * 
     * @param context
     * @param schema
     * @param metadataXml
     * @param uuid
     * @param owner
     * @param groupOwner
     * @param source
     * @param metadataType
     * @param docType
     * @param category
     * @param createDate
     * @param changeDate
     * @param ufo
     * @param index
     * @return
     * @throws Exception
     */
    public String insertMetadata(ServiceContext context, String schema,
            Element metadataXml, String uuid, int owner, String groupOwner,
            String source, String metadataType, String docType, String category,
            String createDate, String changeDate, boolean ufo, boolean index)
                    throws Exception;

    /**
     * 
     * @param context
     * @param newMetadata
     * @param metadataXml
     * @param notifyChange
     * @param index
     * @param updateFixedInfo
     * @param updateDatestamp
     * @param fullRightsForGroup
     * @param forceRefreshReaders
     * @return
     * @throws Exception
     */
    public IMetadata insertMetadata(ServiceContext context, IMetadata newMetadata,
            Element metadataXml, boolean notifyChange, boolean index,
            boolean updateFixedInfo, UpdateDatestamp updateDatestamp,
            boolean fullRightsForGroup, boolean forceRefreshReaders)
                    throws Exception;

    /**
     * 
     * When implementing the method, remember that it should be synchronized to
     * avoid concurrent updates
     * 
     * @param context
     * @param metadataId
     * @param md
     * @param validate
     * @param ufo
     * @param index
     * @param lang
     * @param changeDate
     * @param updateDateStamp
     * @return
     * @throws Exception
     */
    public IMetadata updateMetadata(final ServiceContext context,
            final String metadataId, final Element md, final boolean validate,
            final boolean ufo, final boolean index, final String lang,
            final String changeDate, final boolean updateDateStamp)
                    throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id; adds editing information if
     * requested and validation errors if requested.
     *
     * @param srvContext
     * @param id
     * @param forEditing
     *            Add extra element to build metadocument
     *            {@link EditLib#expandElements(String, Element)}
     * @param withEditorValidationErrors
     * @param keepXlinkAttributes
     *            When XLinks are resolved in non edit mode, do not remove XLink
     *            attributes.
     * @return
     * @throws Exception
     */
    public Element getMetadata(ServiceContext srvContext, String id,
            boolean forEditing, boolean withEditorValidationErrors,
            boolean keepXlinkAttributes) throws Exception;

    /**
     * Update metadata record (not template) using update-fixed-info.xsl
     *
     *
     * @param schema
     * @param metadataId
     * @param uuid
     *            If the metadata is a new record (not yet saved), provide the
     *            uuid for that record
     * @param md
     * @param parentUuid
     * @param updateDatestamp
     *            FIXME ? updateDatestamp is not used when running XSL
     *            transformation
     * @return
     * @throws Exception
     */
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId,
            String uuid, Element md, String parentUuid,
            UpdateDatestamp updateDatestamp, ServiceContext context)
                    throws Exception;

    /**
     * Extract UUID from the metadata record using the schema XSL for UUID
     * extraction)
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    public String extractUUID(String schema, Element md) throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id with no geonet:info.
     * 
     * @param srvContext
     * @param id
     * @return
     * @throws Exception
     */
    public Element getMetadataNoInfo(ServiceContext srvContext, String id)
            throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id. Use this method when you must
     * retrieve a metadata in the same transaction.
     * 
     * @param id
     * @return
     * @throws Exception
     */
    public Element getMetadata(String id) throws Exception;

    /**
     * Retrieves a metadata given its id. Use this method when you must
     * retrieve a metadata in the same transaction.
     * 
     * @param id
     * @return
     * @throws Exception
     */
    public IMetadata getMetadataObject(Integer id) throws Exception;

    /**
     * Retrieves a metadata given its id. Use this method when you must
     * retrieve a metadata in the same transaction.
     * 
     * Does not check privileges
     * 
     * @param id
     * @return
     * @throws Exception
     */
    public IMetadata getMetadataObjectNoPriv(Integer id) throws Exception;
    
    /**
     * Retrieves a metadata given its uuid. Use this method when you must
     * retrieve a metadata in the same transaction.
     * 
     * @param uuid
     * @return
     * @throws Exception
     */
    public IMetadata getMetadataObject(String uuid) throws Exception;

    /**
     * Retrieves a metadata element given it's ref.
     *
     * @param md
     * @param ref
     * @return
     */
    public Element getElementByRef(Element md, String ref);

    /**
     * Returns true if the metadata exists in the database.
     * 
     * @param id
     * @return
     * @throws Exception
     */
    public boolean existsMetadata(int id) throws Exception;

    /**
     * Returns true if the metadata uuid exists in the database.
     * 
     * @param uuid
     * @return
     * @throws Exception
     */
    public boolean existsMetadataUuid(String uuid) throws Exception;

    /**
     * For update of owner info.
     * 
     * Do not forget to synchronize when implement this!
     *
     * @param id
     * @param owner
     * @param groupOwner
     * @throws Exception
     */
    public void updateMetadataOwner(final int id, final String owner,
            final String groupOwner) throws Exception;

    /**
     * Removes a metadata.
     * 
     * Do not forget to synchronize when implement this!
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    public void deleteMetadata(ServiceContext context, String metadataId)
            throws Exception;

    /**
     * 
     * Do not forget to synchronize when implement this!
     * 
     * @param context
     * @param metadataId
     * @throws Exception
     */
    public void deleteMetadataGroup(ServiceContext context, String metadataId)
            throws Exception;

    /**
     * Removes all operations stored for a metadata.
     * 
     * @param metadataId
     * @param skipAllIntranet
     * @throws Exception
     */
    public void deleteMetadataOper(ServiceContext context, String metadataId,
            boolean skipAllIntranet) throws Exception;

    public void setNamespacePrefixUsingSchemas(String schema, Element md)
            throws Exception;

    /**
     * @return
     */
    public EditLib getEditLib();
    
    /**
     * Saves the metadata on the database
     * @param md
     */
    public IMetadata save(IMetadata md);
}
