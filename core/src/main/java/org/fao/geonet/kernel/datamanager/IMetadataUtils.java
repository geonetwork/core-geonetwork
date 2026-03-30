//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.api.exception.ResourceNotFoundException;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.MetadataSourceInfo;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.repository.SimpleMetadata;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.google.common.base.Optional;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

/**
 * Utility interface for records
 *
 * @author delawen
 *
 */
public interface IMetadataUtils {

    /**
     * This is a hopefully soon to be deprecated initialization function to replace the @Autowired annotation
     *
     * @param context
     * @param force
     * @throws Exception
     */
    public void init(ServiceContext context, Boolean force) throws Exception;

    /**
     * Return the uuid of the record with the defined id
     *
     * @param id
     * @return
     * @throws Exception
     */
    String getMetadataUuid(String id) throws Exception;

    /**
     * Start an editing session. This will record the original metadata record in the session under the
     * {@link org.fao.geonet.constants.Geonet.Session#METADATA_BEFORE_ANY_CHANGES} + id session property.
     *
     * The record contains geonet:info element.
     *
     * Note: Only the metadata record is stored in session. If the editing session upload new documents or thumbnails, those documents will
     * not be cancelled. This needs improvements.
     * @return id of the record to edit
     */
    Integer startEditingSession(ServiceContext context, String id) throws Exception;

    /**
     * Rollback to the record in the state it was when the editing session started (See
     * {@link #startEditingSession(ServiceContext, String)}).
     */
    void cancelEditingSession(ServiceContext context, String id) throws Exception;

    /**
     * Remove the original record stored in session.
     */
    void endEditingSession(String id, UserSession session);

    /**
     * Does a pre-order visit enumerating each node.
     */
    Element enumerateTree(Element md) throws Exception;

    /**
     * Extract UUID from the metadata record using the schema XSL for UUID extraction)
     */
    String extractUUID(String schema, Element md) throws Exception;


    String extractDefaultLanguage(String schema, Element md) throws Exception;

    /**
     * Extract Multilinugal titles from the metadata record using the schema XSL for title extraction)
     */
    LinkedHashMap<String, String> extractTitles(String schema, Element md) throws Exception;

    LinkedHashMap<String, String> extractTitles(@Nonnull String id) throws Exception;

    String getPermalink(String uuid, String language);

    String getDefaultUrl(String uuid, String language);

    String getDoi(String uuid) throws ResourceNotFoundException, IOException, JDOMException;

    String getResourceIdentifier(String uuid) throws ResourceNotFoundException, JDOMException, IOException;

    /**
     * Extract the last editing date from the record
     *
     * @param schema
     * @param md
     * @return
     * @throws Exception
     */
    String extractDateModified(String schema, Element md) throws Exception;

    /**
     * Modify the UUID of a record. Uses the proper XSL transformation from the schema
     *
     * @param schema
     * @param uuid
     * @param md
     * @return
     * @throws Exception
     */
    Element setUUID(String schema, String uuid, Element md) throws Exception;

    /**
     * Returns the summary of the md record
     *
     * @param md
     * @return
     * @throws Exception
     */
    Element extractSummary(Element md) throws Exception;

    /**
     * Returns the identifier of the record with uuid uuid
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    String getMetadataId(String uuid) throws Exception;

    /**
     * Returns the version of the record with identifier id
     *
     * @param id
     * @return
     */
    String getVersion(String id);

    /**
     *
     * Returns the version of a metadata, incrementing it if necessary.
     *
     * @param id
     * @return
     */
    String getNewVersion(String id);

    /**
     * Mark a record as template (or not)
     *
     * @param id
     * @param metadataType
     * @throws Exception
     */
    void setTemplateExt(int id, MetadataType metadataType) throws Exception;

    /**
     * Mark a record as template (or not)
     *
     * @param id
     * @param metadataType
     * @throws Exception
     */
    void setTemplate(int id, MetadataType type, String title) throws Exception;

    /**
     * Mark a record as harvested
     *
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    void setHarvestedExt(int id, String harvestUuid) throws Exception;

    /**
     * Mark a record as harvested
     *
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    void setHarvested(int id, String harvestUuid) throws Exception;

    /**
     * Mark a record as harvested from the harvestUri
     *
     * @param id
     * @param harvestUuid
     * @throws Exception
     */
    void setHarvestedExt(int id, String harvestUuid, Optional<String> harvestUri) throws Exception;

    /**
     * Set the display order. A hint for ordering templates when displayed in a list. May also be used when displaying sub-templates.
     *
     *
     * @param id
     * @param displayOrder
     * @throws Exception
     */
    void updateDisplayOrder(String id, String displayOrder) throws Exception;

    /**
     * Increases the popularity of the record defined by the id
     *
     * @param srvContext
     * @param id
     * @throws Exception
     */
    void increasePopularity(ServiceContext srvContext, String id) throws Exception;

    /**
     * Rates a metadata.
     *
     * @param ipAddress ipAddress IP address of the submitting client
     * @param rating range should be 1..5
     * @throws Exception hmm
     */
    int rateMetadata(int metadataId, String ipAddress, int rating) throws Exception;

    /**
     * Retrieves a metadata (in xml) given its id with no geonet:info.
     */
    Element getMetadataNoInfo(ServiceContext srvContext, String id) throws Exception;

    /**
     * remove the geonet:info element from the supplied metadata.
     */
    Element removeMetadataInfo(Element md) throws Exception;

    /**
     * Retrieves a metadata element given it's ref.
     */
    Element getElementByRef(Element md, String ref);

    /**
     * Returns true if the metadata uuid exists in the database.
     */
    boolean existsMetadataUuid(String uuid) throws Exception;

    /**
     * Returns true if the metadata exists in the database.
     */
    boolean existsMetadata(int id) throws Exception;

    boolean isMetadataPublished(int metadataId) throws Exception;

    boolean isMetadataApproved(int metadataId) throws Exception;

    boolean isMetadataDraft(int metadataId) throws Exception;

    /**
     * Returns all the keywords in the system.
     */
    Element getKeywords() throws Exception;

    /**
     * Add data commons to the record defined with the id
     *
     * @param context
     * @param id
     * @throws Exception
     */
    void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename,
            String type) throws Exception;

    /**
     * Add creative commons to the record defined with the id
     *
     * @param context
     * @param id
     * @throws Exception
     */
    void setCreativeCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename,
            String type) throws Exception;

    /**
     * Helper function to prevent loop dependency
     *
     * @param metadataManager
     */
    void setMetadataManager(IMetadataManager metadataManager);

    /**
     * Extract the title field from the Metadata Repository. This is only valid for subtemplates as the title can be stored with the
     * subtemplate (since subtemplates don't have a title) - metadata records don't store the title here as this is part of the metadata.
     *
     * @param id metadata id to retrieve
     */
    public String getMetadataTitle(String id) throws Exception;

    /**
     * Set metadata type to subtemplate and set the title. Only subtemplates need to persist the title as it is used to give a meaningful
     * title for use when offering the subtemplate to users in the editor.
     *
     * @param id Metadata id to set to type subtemplate
     * @param title Title of metadata of subtemplate/fragment
     */
    public void setSubtemplateTypeAndTitleExt(int id, String title) throws Exception;

    /**
     * Count how many records are associated to a user
     *
     * @param ownedByUser
     * @return
     */
    public long count(Specification<? extends AbstractMetadata> ownedByUser);

    /**
     * Given an identifier, return the record associated to it
     *
     * @param id
     * @return
     */
    public AbstractMetadata findOne(int id);

    /**
     * Find all the ids of the records that fits the specification
     *
     * @param specs
     * @return
     */
    List<Integer> findAllIdsBy(Specification<? extends AbstractMetadata> specs);

    /**
     * Count the total number of records available on the platform
     *
     * @return
     */
    public long count();

    /**
     * Find the record with the UUID uuid
     *
     * @param firstMetadataId
     *
     * @param uuid
     * @return
     */
    public AbstractMetadata findOneByUuid(String uuid);


    /**
     * Find all records with the UUID uuid
     *
     * @param uuid
     * @return
     */
    public List<? extends AbstractMetadata> findAllByUuid(String uuid);

    /**
     * Find the record that fits the specification
     *
     * @param spec
     * @return
     */
    public AbstractMetadata findOne(Specification<? extends AbstractMetadata> spec);

    /**
     * Find the record that fits the id
     *
     * @param id
     * @return
     */
    public AbstractMetadata findOne(String id);

    /**
     * Find all metadata harvested by the identified harvester.
     *
     * @param uuid the uuid of the harvester
     * @return all metadata harvested by the identified harvester.
     */
    @Nonnull
    List<? extends AbstractMetadata> findAllByHarvestInfo_Uuid(@Nonnull String uuid);

    /**
     * Find all the metadata with the identifiers
     *
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     * @param keySet
     * @return
     */
    public Iterable<? extends AbstractMetadata> findAll(Set<Integer> keySet);

    /**
     * Find all the metadata with the identifiers
     *
     * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.Iterable)
     * @param spec
     * @param order
     * @return
     */
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> spec, Sort order);

    /**
     * Returns all entities matching the given {@link Specification}.
     *
     * @param spec
     * @return
     */
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> spec);

    /**
     * Load only the basic info for a metadata. Used in harvesters, mostly.
     *
     * @param harvestUuid
     * @return
     */
    public List<SimpleMetadata> findAllSimple(String harvestUuid);

    /**
     * Check if a record with identifier iId exists
     *
     * @param iId
     * @return
     */
    public boolean exists(Integer iId);

    /**
     * Load all records that satisfy the criteria provided and convert each to XML of the form:
     *
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     * </pre>
     *
     * @param sort the order to sort the results by
     * @return all entities in XML.
     */
    @Nonnull
    public Element findAllAsXml(Specification<? extends AbstractMetadata> spec, Sort sortByChangeDateDesc);

    /**
     * Load all entities that satisfy the criteria provided and convert each to XML of the form:
     *
     * <pre>
     *  &lt;entityName&gt;
     *      &lt;property&gt;propertyValue&lt;/property&gt;
     *      ...
     *  &lt;/entityName&gt;
     * </pre>
     *
     * @param specification A specification of the criteria that must be satisfied for entity to be selected.
     * @param pageable The paging/sorting strategy
     * @return all entities in XML.
     */
    @Nonnull
    Element findAllAsXml(@Nullable Specification<? extends AbstractMetadata> specification, @Nullable Pageable pageable);

    /**
     * Return an object that contains functions for calculating several different statistical calculations (related to the metadata) based
     * on the data in the database.
     *
     * @return an object for performing statistic calculation queries.
     */
    MetadataReportsQueries getMetadataReports();

    /**
     * Check if another record exist with that UUID. This is not allowed
     * and would return a DataIntegrityViolationException
     *
     * @param uuid  The UUID to check for
     * @param id    The current record id to compare with other record which may be found
     * @return      An exception if another record is found, false otherwise
     */
    boolean checkMetadataWithSameUuidExist(String uuid, int id);

    /**
     * Find the list of Metadata Ids and changes dates for the metadata.
     * <p>
     * When constructing sort objects use the MetaModel objects:
     * <ul>
     * <li><code>new Sort(Metadata_.id.getName())</code></li>
     * <li><code>new Sort(Sort.Direction.ASC, Metadata_.id.getName())</code></li>
     * </ul>
     * </p>
     *
     * @param pageable if non-null then control which subset of the results to return (and how to sort the results).
     * @return List of &lt;MetadataId, changeDate&gt;
     */
    @Nonnull
    Page<Pair<Integer, ISODate>> findAllIdsAndChangeDates(@Nonnull Pageable pageable);

    /**
     * Load the source info objects for all the metadata selected by the spec.
     *
     * @param spec the specification identifying the metadata of interest
     * @return a map of metadataId -> SourceInfo
     */
    Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> spec);

    /**
     * Copy the files from the original metadata to the destination metadata.
     * Used when creating a draft version.
     *
     * @param original
     * @param dest
     */
    void cloneFiles(AbstractMetadata original, AbstractMetadata dest);

    /**
     * Merge the files from the original metadata to the destination metadata.
     * Used when merging creating a draft version to approved copy
     * In this case the files that no longer exists in the draft will be removed from the dest
     *
     * @param original
     * @param dest
     */
    void replaceFiles(AbstractMetadata original, AbstractMetadata dest);

    /**
     * Checks if the metadata is available in the current portal.
     *
     * @param id
     * @return
     */
    boolean isMetadataAvailableInPortal(int id);


    /**
     * Get the metadata after preforming a search and replace on it.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @return The metadata with the search and replace applied.
     */
    String selectOneWithSearchAndReplace(String uuid, String search, String replace);

    /**
     * Get the metadata after preforming a regex search and replace on it.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @return The metadata with the search and replace applied.
     */
    String selectOneWithRegexSearchAndReplaceWithFlags(String uuid, String search, String replace, String flags);

    /**
     * Get the metadata after preforming a regex search and replace on it.
     * @param uuid    The UUID of the metadata to search for.
     * @param search  The string to search for.
     * @param replace The string to replace the search string with.
     * @return The metadata with the search and replace applied.
     */
    String selectOneWithRegexSearchAndReplace(String uuid, String search, String replace);

}
