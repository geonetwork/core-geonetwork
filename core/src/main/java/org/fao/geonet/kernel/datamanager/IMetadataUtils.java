package org.fao.geonet.kernel.datamanager;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.repository.SimpleMetadata;
import org.fao.geonet.repository.reports.MetadataReportsQueries;
import org.jdom.Element;
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
     * Notify a metadata modification
     * 
     * @param md
     * @param metadataId
     * @throws Exception
     */
    void notifyMetadataChange(Element md, String metadataId) throws Exception;

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
     */
    void startEditingSession(ServiceContext context, String id) throws Exception;

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

    /**
     * Returns all the keywords in the system.
     */
    Element getKeywords() throws Exception;

    /**
     * Returns the thumbnails associated to the record with id metadataId
     * 
     * @param context
     * @param metadataId
     * @return
     * @throws Exception
     */
    Element getThumbnails(ServiceContext context, String metadataId) throws Exception;

    /**
     * Add thumbnail to the record defined with the id
     * 
     * @param context
     * @param id
     * @param small
     * @param indexAfterChange
     * @throws Exception
     */
    void setThumbnail(ServiceContext context, String id, boolean small, String file, boolean indexAfterChange) throws Exception;

    /**
     * Remove thumbnail from the record defined with the id
     * 
     * @param context
     * @param id
     * @param small
     * @param indexAfterChange
     * @throws Exception
     */
    void unsetThumbnail(ServiceContext context, String id, boolean small, boolean indexAfterChange) throws Exception;

    /**
     * Add data commons to the record defined with the id
     * 
     * @param context
     * @param id
     * @param small
     * @param indexAfterChange
     * @throws Exception
     */
    void setDataCommons(ServiceContext context, String id, String licenseurl, String imageurl, String jurisdiction, String licensename,
            String type) throws Exception;

    /**
     * Add creative commons to the record defined with the id
     * 
     * @param context
     * @param id
     * @param small
     * @param indexAfterChange
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
     * @return
     */
    public AbstractMetadata findOneByUuid(String firstMetadataId);

    /**
     * Find the record that fits the specification
     * 
     * @param spec
     * @return
     */
    public AbstractMetadata findOne(Specification<Metadata> spec);

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
     * Returns all entities matching the given {@link Specification}.
     * 
     * @param spec
     * @return
     */
    public List<? extends AbstractMetadata> findAll(Specification<? extends AbstractMetadata> hasHarvesterUuid);

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
}
