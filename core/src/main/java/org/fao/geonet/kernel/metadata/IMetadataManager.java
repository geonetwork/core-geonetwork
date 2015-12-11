/**
 * 
 */
package org.fao.geonet.kernel.metadata;

import java.io.IOException;

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
     * @throws Exception
     */
    public void startEditingSession(ServiceContext context, String id)
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
    public Metadata insertMetadata(ServiceContext context, Metadata newMetadata,
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
    public Metadata updateMetadata(final ServiceContext context,
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
     * TODO javadoc.
     *
     * @param id
     * @return
     * @throws Exception
     */
    public String getMetadataSchema(String id) throws Exception;

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

}
