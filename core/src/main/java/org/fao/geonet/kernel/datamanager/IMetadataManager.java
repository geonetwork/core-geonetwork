//=============================================================================
//===	Copyright (C) 2001-2011 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.datamanager;

import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.BatchUpdateQuery;
import org.fao.geonet.repository.PathSpec;
import org.fao.geonet.repository.Updater;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.context.ServiceContext;

/**
 * Utility interface to handle record insertions, removals and updates
 *
 * @author delawen
 *
 */
public interface IMetadataManager {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     *
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Removes the record with the id metadataId
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    void deleteMetadata(ServiceContext context, String metadataId) throws Exception;

    /**
     * Delete the record with the id metadataId and additionally take care of cleaning up resources, send events, ...
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    void purgeMetadata(ServiceContext context, String metadataId, boolean withBackup) throws Exception;

    /**
     * Removes a record without notifying.
     *
     * FIXME explain better why this and not {@link #deleteMetadata(ServiceContext, String)}
     *
     * @param context
     * @param metadataId
     * @throws Exception
     */
    void deleteMetadataGroup(ServiceContext context, String metadataId) throws Exception;

    /**
     * Creates a new metadata duplicating an existing template creating a random uuid.
     *
     * @param isTemplate
     * @param fullRightsForGroup
     */
    String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup) throws Exception;

    /**
     * Creates a new metadata duplicating an existing template with an specified uuid.
     *
     * @param isTemplate
     * @param fullRightsForGroup
     */
    String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner, String parentUuid,
            String isTemplate, boolean fullRightsForGroup, String uuid) throws Exception;

    /**
     * Inserts a metadata into the database, optionally indexing it, and optionally applying automatic changes to it (update-fixed-info).
     *
     * @param context the context describing the user and service
     * @param schema XSD this metadata conforms to
     * @param metadataXml the metadata to store
     * @param uuid unique id for this metadata
     * @param owner user who owns this metadata
     * @param groupOwner group this metadata belongs to
     * @param source id of the origin of this metadata (harvesting source, etc.)
     * @param metadataType whether this metadata is a template
     * @param docType ?!
     * @param category category of this metadata
     * @param createDate date of creation
     * @param changeDate date of modification
     * @param ufo whether to apply automatic changes
     * @param indexingMode whether to index this metadata
     * @return id, as a string
     * @throws Exception hmm
     */
    String insertMetadata(ServiceContext context, String schema, Element metadataXml, String uuid, int owner, String groupOwner,
                          String source, String metadataType, String docType, String category, String createDate, String changeDate, boolean ufo,
                          IndexingMode indexingMode) throws Exception;

    /**
     * /** Inserts a metadata into the database, optionally indexing it, and optionally applying automatic changes to it
     * (update-fixed-info).
     *
     * @param context
     * @param newMetadata
     * @param metadataXml
     * @param indexingMode
     * @param updateFixedInfo
     * @param updateDatestamp
     * @param fullRightsForGroup
     * @param forceRefreshReaders
     * @return
     * @throws Exception
     */
    AbstractMetadata insertMetadata(ServiceContext context, AbstractMetadata newMetadata, Element metadataXml, IndexingMode indexingMode,
                                    boolean updateFixedInfo, UpdateDatestamp updateDatestamp, boolean fullRightsForGroup, boolean forceRefreshReaders)
            throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id. Use this method when you must retrieve a metadata in the same transaction.
     */
    Element getMetadata(String id) throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id; adds editing information if requested and validation errors if requested.
     *
     * @param forEditing Add extra element to build metadocument {@link EditLib#expandElements(String, Element)}
     * @param applyOperationsFilters Filter elements based on operation filters
     *                               eg. Remove WMS if not dynamic. For example, when processing
     *                               a record, the complete records need to be processed and saved (not a filtered version), set it to false.
     *                               If editing, set it to false.
     * @param keepXlinkAttributes When XLinks are resolved in non edit mode, do not remove XLink attributes.
     */
    Element getMetadata(ServiceContext srvContext, String id,
                        boolean forEditing, boolean applyOperationsFilters,
                        boolean withEditorValidationErrors, boolean keepXlinkAttributes) throws Exception;

    /**
     * Update of owner info.
     */
    void updateMetadataOwner(int id, String owner, String groupOwner) throws Exception;

    /**
     * Updates a metadata record. Deletes validation report currently in session (if any). If user asks for validation the validation report
     * will be (re-)created then.
     *
     * @return metadata if the that was updated
     */
    AbstractMetadata updateMetadata(ServiceContext context, String metadataId, Element md, boolean validate, boolean ufo,
                                    String lang, String changeDate, boolean updateDateStamp, IndexingMode indexingMode) throws Exception;

    /**
     * Add privileges information about metadata record which depends on context and usually could not be stored in db or Lucene index
     * because depending on the current user or current client IP address.
     *
     * @param mdIdToInfoMap a map from the metadata Id -> the info element to which the privilege information should be added.
     */
    void buildPrivilegesMetadataInfo(ServiceContext context, Map<String, Element> mdIdToInfoMap) throws Exception;

    /**
     * Updates all children of the selected parent. Some elements are protected in the children according to the stylesheet used in
     * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
     *
     * Children MUST be editable and also in the same schema of the parent. If not, child is not updated.
     *
     * @param srvContext service context
     * @param parentUuid parent uuid
     * @param children children
     * @param params parameters
     */
    Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, Object> params)
            throws Exception;

    /**
     * Update metadata record (not template) using update-fixed-info.xsl
     *
     * @param uuid If the metadata is a new record (not yet saved), provide the uuid for that record
     * @param updateDatestamp updateDatestamp is not used when running XSL transformation
     */
    Element updateFixedInfo(String schema, Optional<Integer> metadataId, String uuid, Element md, String parentUuid,
            UpdateDatestamp updateDatestamp, ServiceContext context) throws Exception;

    /**
     * You should not use a direct flush. If you need to use this to properly run your code, you are missing something. Check the
     * transaction annotations and try to comply to Spring/Hibernate
     */
    @Deprecated
    void flush();

    /**
     * Returns a helpful EditLib for other utility classes
     *
     * @return
     */
    EditLib getEditLib();

    /**
     * Saves an AbstractMetadata into the database. Useful to avoid using the MetadataRepository classes directly, who may not know how to handle
     * AbstractMetadata types
     *
     * @param info
     */
    public AbstractMetadata save(AbstractMetadata info);

    /**
     * Load a record, modify it and save it again.
     * <p>
     * This method loads the domain object identified domain object, passes the domain object to the function and then saves the domain
     * object passed to the function.
     * </p>
     *
     * @param id the id of the domain object to load.
     * @param updater the function that updates the domain object before saving.
     * @return the saved domain object.
     */
    public AbstractMetadata update(int id, @Nonnull Updater<? extends AbstractMetadata> md);

    /**
     * Delete all records that matches the specification
     *
     * @param specification
     */
    public void deleteAll(Specification<? extends AbstractMetadata> specification);

    /**
     * Remove the record with the identifier id
     *
     * @param id
     */
    public void delete(Integer id);

    boolean isValid(Integer id);

    /**
     * Create a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     *
     * @param pathToUpdate the path of the attribute to update with the new value. More paths and values can be added to the
     *            {@link BatchUpdateQuery} object after it is created.
     * @param newValue the value to set on the attribute of all the affected entities
     * @param spec a specification for controlling which entities will be affected by update.
     * @param <V> The type of the attribute
     * @return a {@link BatchUpdateQuery} object to allow for updating multiple objects in a single query.
     */
    public void createBatchUpdateQuery(PathSpec<? extends AbstractMetadata, String> servicesPath, String newUuid,
            Specification<? extends AbstractMetadata> harvested);


	public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> specs);
}
