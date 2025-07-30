//=============================================================================
//===	Copyright (C) 2001-2020 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.datamanager.base;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.transaction.TransactionManager;
import jeeves.transaction.TransactionTask;
import jeeves.xlink.Processor;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.records.attachments.Store;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.events.history.RecordDeletedEvent;
import org.fao.geonet.events.md.MetadataPreRemove;
import org.fao.geonet.exceptions.UnAuthorizedException;
import org.fao.geonet.kernel.*;
import org.fao.geonet.kernel.datamanager.*;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.index.BatchOpsMetadataReindexer;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataFileUploadSpecs;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.TransactionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.where;

public class BaseMetadataManager implements IMetadataManager {

    private static final Logger LOGGER_DATA_MANAGER = LoggerFactory.getLogger(Geonet.DATA_MANAGER);

    @Autowired
    protected IMetadataUtils metadataUtils;
    @Autowired
    private IMetadataIndexer metadataIndexer;
    @Autowired
    private IMetadataValidator metadataValidator;
    @Autowired
    private IMetadataOperations metadataOperations;
    @Autowired
    private IMetadataSchemaUtils metadataSchemaUtils;
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private MetadataStatusRepository metadataStatusRepository;
    @Autowired
    private MetadataValidationRepository metadataValidationRepository;
    @Autowired
    private MetadataRepository metadataRepository;
    @Autowired
    private EsSearchManager searchManager;

    private EditLib editLib;
    @Autowired
    private MetadataRatingByIpRepository metadataRatingByIpRepository;
    @Autowired
    private MetadataFileUploadRepository metadataFileUploadRepository;
    @Autowired(required = false)
    private XmlSerializer xmlSerializer;
    @Autowired
    @Lazy
    private SettingManager settingManager;
    @Autowired
    private MetadataCategoryRepository metadataCategoryRepository;
    @Autowired(required = false)
    private HarvestInfoProvider harvestInfoProvider;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SchemaManager schemaManager;
    @Autowired
    private ThesaurusManager thesaurusManager;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    private UserSavedSelectionRepository userSavedSelectionRepository;

    private static final int METADATA_BATCH_PAGE_SIZE = 50000;

    @Autowired
    private ApplicationContext _applicationContext;
    @PersistenceContext
    private EntityManager _entityManager;

    @Override
    public EditLib getEditLib() {
        return editLib;
    }

    /**
     * To avoid cyclic references on autowired
     */
    @PostConstruct
    public void init() {
        editLib = new EditLib(schemaManager);
        metadataValidator.setMetadataManager(this);
        metadataUtils.setMetadataManager(this);
        metadataIndexer.setMetadataManager(this);
    }

    public void init(ServiceContext context, Boolean force) throws Exception {
        try {
            harvestInfoProvider = context.getBean(HarvestInfoProvider.class);
        } catch (Exception e) {
            // If it doesn't exist, that's fine
        }

        // From DataManager:
        searchManager.init(false, java.util.Optional.empty());
    }

    /**
     * Refresh index if needed. Can also be called after GeoNetwork startup in
     * order to rebuild the lucene index
     * t
     *
     * @param force        Force reindexing all from scratch
     * @param asynchronous
     **/
    public void synchronizeDbWithIndex(ServiceContext context, Boolean force, Boolean asynchronous) throws Exception {

        // get lastchangedate of all metadata in index
        Map<String, String> docs = searchManager.getDocsChangeDate();

        // set up results HashMap for post processing of records to be indexed
        List<String> toIndex = new ArrayList<String>();

        LOGGER_DATA_MANAGER.debug("INDEX CONTENT:");

        Sort sortByMetadataChangeDate = SortUtils.createSort(Sort.Direction.DESC, Metadata_.dataInfo, MetadataDataInfo_.changeDate);
        int currentPage = 0;
        Page<Pair<Integer, ISODate>> results = metadataUtils.findAllIdsAndChangeDates(
            PageRequest.of(currentPage, METADATA_BATCH_PAGE_SIZE, sortByMetadataChangeDate));

        // index all metadata in DBMS if needed
        while (results.getNumberOfElements() > 0) {
            for (Pair<Integer, ISODate> result : results) {

                // get metadata
                String id = String.valueOf(result.one());

                LOGGER_DATA_MANAGER.debug("- record ({})", id);

                String idxLastChange = docs.get(id);

                // if metadata is not indexed index it
                if (idxLastChange == null) {
                    LOGGER_DATA_MANAGER.debug("-  will be indexed");
                    toIndex.add(id);

                    // else, if indexed version is not the latest index it
                } else {
                    docs.remove(id);

                    String lastChange = result.two().toString();

                    LOGGER_DATA_MANAGER.debug("- lastChange: {}", lastChange);
                    LOGGER_DATA_MANAGER.debug("- idxLastChange: {}", idxLastChange);

                    // date in index contains 't', date in DBMS contains 'T'
                    if (force || !idxLastChange.equalsIgnoreCase(lastChange)) {
                        LOGGER_DATA_MANAGER.debug("-  will be indexed");
                        toIndex.add(id);
                    }
                }
            }

            currentPage++;
            results = metadataRepository.findIdsAndChangeDates(
                PageRequest.of(currentPage, METADATA_BATCH_PAGE_SIZE, sortByMetadataChangeDate));
        }

        // if anything to index then schedule it to be done after servlet is
        // up so that any links to local fragments are resolvable
        if (toIndex.size() > 0) {
            if (asynchronous) {
                Set<Integer> integerList = toIndex.stream().map(Integer::parseInt).collect(Collectors.toSet());
                new BatchOpsMetadataReindexer(
                    context.getBean(DataManager.class),
                    integerList).process(settingManager.getSiteId(), false);
            } else {
                metadataIndexer.batchIndexInThreadPool(context, toIndex);
            }
        }

        if (docs.size() > 0) { // anything left?
            LOGGER_DATA_MANAGER.debug("INDEX HAS RECORDS THAT ARE NOT IN DB:");
        }

        // remove from index metadata not in DBMS
        for (String id : docs.keySet()) {
            getSearchManager().delete(String.format("+id:%s", id));
            LOGGER_DATA_MANAGER.debug("- removed record ({}) from index", id);
        }
    }

    protected EsSearchManager getSearchManager() {
        return searchManager;
    }

    /**
     * You should not use a direct flush. If you need to use this to properly run
     * your code, you are missing something. Check the transaction annotations and
     * try to comply to Spring/Hibernate
     */
    @Override
    @Deprecated
    public void flush() {
        TransactionManager.runInTransaction("DataManager flush()", getApplicationContext(),
            TransactionManager.TransactionRequirement.CREATE_ONLY_WHEN_NEEDED, TransactionManager.CommitBehavior.ALWAYS_COMMIT, false,
            new TransactionTask<Object>() {
                @Override
                public Object doInTransaction(TransactionStatus transaction) throws Throwable {
                    _entityManager.flush();
                    return null;
                }
            });

    }

    private ApplicationContext getApplicationContext() {
        final ConfigurableApplicationContext applicationContext = ApplicationContextHolder.get();
        return applicationContext == null ? _applicationContext : applicationContext;
    }

    protected void deleteMetadataFromDB(ServiceContext context, String id) throws Exception {
        AbstractMetadata metadata = metadataUtils.findOne(Integer.valueOf(id));
        if (!settingManager.getValueAsBool(Settings.SYSTEM_XLINK_ALLOW_REFERENCED_DELETION)
            && metadata.getDataInfo().getType() == MetadataType.SUB_TEMPLATE) {
            if (this.hasReferencingMetadata(context, metadata)) {
                throw new UnAuthorizedException("This template is referenced.", metadata);
            }
        }

        // --- remove operations
        metadataOperations.deleteMetadataOper(context, id, false);

        int intId = Integer.parseInt(id);
        metadataRatingByIpRepository.deleteAllById_MetadataId(intId);
        metadataValidationRepository.deleteAllById_MetadataId(intId);
        userSavedSelectionRepository.deleteAllByUuid(metadataUtils.getMetadataUuid(id));

        // Logical delete for metadata file uploads
        PathSpec<MetadataFileUpload, String> deletedDatePathSpec = new PathSpec<MetadataFileUpload, String>() {
            @Override
            public javax.persistence.criteria.Path<String> getPath(Root<MetadataFileUpload> root) {
                return root.get(MetadataFileUpload_.deletedDate);
            }
        };

        metadataFileUploadRepository
            .createBatchUpdateQuery(deletedDatePathSpec, new ISODate().toString(), MetadataFileUploadSpecs.isNotDeletedForMetadata(intId));

        // --- remove metadata
        getXmlSerializer().delete(id, context);
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Metadata thumbnail API
    // ---
    // --------------------------------------------------------------------------

    private XmlSerializer getXmlSerializer() {
        return xmlSerializer;
    }

    /**
     * Removes a metadata.
     */
    @Override
    public void deleteMetadata(ServiceContext context, String metadataId) throws Exception {
        AbstractMetadata findOne = metadataUtils.findOne(metadataId);
        if (findOne != null) {
            boolean isMetadata = findOne.getDataInfo().getType() == MetadataType.METADATA;

            deleteMetadataFromDB(context, metadataId);
        }

        // --- update search criteria
        getSearchManager().delete(String.format("+id:%s", metadataId));
        // _entityManager.flush();
        // _entityManager.clear();
    }

    /**
     * Delete the record with the id metadataId and additionally take care of cleaning up resources, send events, ...
     */
    @Override
    public void purgeMetadata(ServiceContext context, String metadataId, boolean withBackup) throws Exception {
        AbstractMetadata metadata = metadataUtils.findOne(metadataId);
        Store store = context.getBean("resourceStore", Store.class);

        MetadataPreRemove preRemoveEvent = new MetadataPreRemove(metadata);
        ApplicationContextHolder.get().publishEvent(preRemoveEvent);

        if (metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE
            && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE && withBackup) {
            MEFLib.backupRecord(metadata, context);
        }

        boolean approved = true;
        if (metadata instanceof MetadataDraft) {
            approved = false;
        }

        store.delResources(context, metadata.getUuid(), approved);

        RecordDeletedEvent recordDeletedEvent = new RecordDeletedEvent(
            metadata.getId(), metadata.getUuid(), new LinkedHashMap<>(),
            context.getUserSession().getUserIdAsInt(), metadata.getData());
        deleteMetadata(context, metadataId);
        recordDeletedEvent.publish(ApplicationContextHolder.get());
    }

    /**
     * @param context
     * @param metadataId
     * @throws Exception
     */
    @Override
    public void deleteMetadataGroup(ServiceContext context, String metadataId) throws Exception {
        deleteMetadataFromDB(context, metadataId);
        getSearchManager().delete(String.format("+id:%s", metadataId));
    }

    /**
     * Creates a new metadata duplicating an existing template creating a random
     * uuid.
     *
     * @param isTemplate
     * @param fullRightsForGroup
     */
    @Override
    public String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner,
                                 String parentUuid, String isTemplate, boolean fullRightsForGroup) throws Exception {

        return createMetadata(context, templateId, groupOwner, source, owner, parentUuid, isTemplate, fullRightsForGroup,
            UUID.randomUUID().toString());
    }

    /**
     * Creates a new metadata duplicating an existing template with a specified uuid.
     *
     * @param isTemplate
     * @param fullRightsForGroup
     */
    @Override
    public String createMetadata(ServiceContext context, String templateId, String groupOwner, String source, int owner,
                                 String parentUuid, String isTemplate, boolean fullRightsForGroup, String uuid) throws Exception {
        AbstractMetadata templateMetadata = metadataUtils.findOne(templateId);
        if (templateMetadata == null) {
            throw new IllegalArgumentException("Template id not found : " + templateId);
        }

        String schema = templateMetadata.getDataInfo().getSchemaId();
        String data = templateMetadata.getData();
        data = updateMetadataUuidReferences(data, templateMetadata.getUuid(), uuid);

        Element xml = Xml.loadString(data, false);

        boolean isMetadata = templateMetadata.getDataInfo().getType() == MetadataType.METADATA;
        MetadataType type = MetadataType.lookup(isTemplate);
        setMetadataTitle(schema, xml, context.getLanguage(), !isMetadata);
        if (isMetadata) {
            xml = updateFixedInfo(schema, Optional.<Integer>absent(), uuid, xml, parentUuid, UpdateDatestamp.NO, context);

            xml = duplicateMetadata(schema, xml, context);
        } else if (type == MetadataType.SUB_TEMPLATE
            || type == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            xml.setAttribute("uuid", uuid);
        }

        final Metadata newMetadata = new Metadata();
        newMetadata.setUuid(uuid);
        newMetadata.getDataInfo()
            .setChangeDate(new ISODate())
            .setCreateDate(new ISODate())
            .setSchemaId(schema)
            .setRoot(templateMetadata.getDataInfo().getRoot())
            .setType(type)
            .setRoot(xml.getQualifiedName());
        newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner)).setOwner(owner).setSourceId(source);

        // If there is a default category for the group, use it:
        java.util.Optional<Group> group = groupRepository.findById(Integer.valueOf(groupOwner));
        if (group.isPresent() && (group.get().getDefaultCategory() != null)) {
            newMetadata.getMetadataCategories().add(group.get().getDefaultCategory());
        }
        Collection<MetadataCategory> filteredCategories = Collections2
            .filter(templateMetadata.getCategories(), new Predicate<MetadataCategory>() {
                @Override
                public boolean apply(@Nullable MetadataCategory input) {
                    return input != null;
                }
            });

        newMetadata.getMetadataCategories().addAll(filteredCategories);

        int finalId = insertMetadata(context, newMetadata, xml, IndexingMode.full, true, UpdateDatestamp.YES,
            fullRightsForGroup, true).getId();

        return String.valueOf(finalId);
    }

    /**
     * Replace oldUuid references by newUuid.
     * This will update metadata identifier, but also other usages
     * which may be in graphicOverview URLs, resources identifier,
     * metadata point of truth URL, ...
     */
    private String updateMetadataUuidReferences(String data, String oldUuid, String newUuid) {
        return data.replace(oldUuid, newUuid);
    }

    /**
     * Update XML document title as defined by schema plugin in xpathTitle property.
     */
    private void setMetadataTitle(String schema, Element xml, String language, boolean fromTemplate) {
        ResourceBundle messages = ResourceBundle.getBundle("org.fao.geonet.api.Messages", new Locale(language));

        SchemaPlugin schemaPlugin = SchemaManager.getSchemaPlugin(schema);
        List<String> xpathTitle = schemaPlugin.getXpathTitle();
        if (xpathTitle != null) {
            xpathTitle.forEach(path -> {
                List<?> titleNodes = null;
                try {
                    titleNodes = Xml.selectNodes(xml, path, new ArrayList<Namespace>(schemaPlugin.getNamespaces()));

                    for (Object o : titleNodes) {
                        if (o instanceof Element) {
                            Element title = (Element) o;
                            // Use settings defined timezone to format the date/time
                            title.setText(String
                                .format(messages.getString("metadata.title.createdFrom" + (fromTemplate ? "Template" : "Record")),
                                    title.getTextTrim(), ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)));
                        }
                    }
                } catch (JDOMException e) {
                    LOGGER_DATA_MANAGER
                        .debug("Check xpath '{}' for schema plugin '{}'. Error is '{}'.", new Object[]{path, schema, e.getMessage()});
                }
            });
        }
    }

    /**
     * Inserts a metadata into the database, optionally indexing it, and optionally
     * applying automatic changes to it (update-fixed-info).
     *
     * @param context      the context describing the user and service
     * @param schema       XSD this metadata conforms to
     * @param metadataXml  the metadata to store
     * @param uuid         unique id for this metadata
     * @param owner        user who owns this metadata
     * @param groupOwner   group this metadata belongs to
     * @param source       id of the origin of this metadata (harvesting source, etc.)
     * @param metadataType whether this metadata is a template
     * @param docType      ?!
     * @param category     category of this metadata
     * @param createDate   date of creation
     * @param changeDate   date of modification
     * @param ufo          whether to apply automatic changes
     * @param indexingMode whether to index this metadata
     * @return id, as a string
     * @throws Exception hmm
     */
    @Override
    public String insertMetadata(ServiceContext context, String schema, Element metadataXml, String uuid, int owner,
                                 String groupOwner, String source, String metadataType, String docType, String category, String createDate,
                                 String changeDate, boolean ufo, IndexingMode indexingMode) throws Exception {
        if (source == null) {
            source = settingManager.getSiteId();
        }

        if (StringUtils.isBlank(metadataType)) {
            metadataType = MetadataType.METADATA.codeString;
        }
        final Metadata newMetadata = new Metadata();
        newMetadata.setUuid(uuid);
        final ISODate isoChangeDate = changeDate != null ? new ISODate(changeDate) : new ISODate();
        final ISODate isoCreateDate = createDate != null ? new ISODate(createDate) : new ISODate();
        newMetadata.getDataInfo().setChangeDate(isoChangeDate).setCreateDate(isoCreateDate).setSchemaId(schema).setDoctype(docType)
            .setRoot(metadataXml.getQualifiedName()).setType(MetadataType.lookup(metadataType));
        newMetadata.getSourceInfo().setOwner(owner).setSourceId(source);
        if (StringUtils.isNotEmpty(groupOwner)) {
            newMetadata.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
        }
        if (StringUtils.isNotEmpty(category)) {
            MetadataCategory metadataCategory = metadataCategoryRepository.findOneByName(category);
            if (metadataCategory == null) {
                throw new IllegalArgumentException("No category found with name: " + category);
            }
            newMetadata.getMetadataCategories().add(metadataCategory);
        } else if (StringUtils.isNotEmpty(groupOwner)) {
            // If the group has a default category, use it
            java.util.Optional<Group> group = groupRepository.findById(Integer.valueOf(groupOwner));
            if (group.isPresent() && (group.get().getDefaultCategory() != null)) {
                newMetadata.getMetadataCategories().add(group.get().getDefaultCategory());
            }
        }

        boolean fullRightsForGroup = false;

        int finalId = insertMetadata(context, newMetadata, metadataXml, indexingMode, ufo, UpdateDatestamp.NO,
            fullRightsForGroup, true).getId();

        return String.valueOf(finalId);
    }

    @Override
    public AbstractMetadata insertMetadata(ServiceContext context, AbstractMetadata newMetadata, Element metadataXml,
                                           IndexingMode indexingMode, boolean updateFixedInfo, UpdateDatestamp updateDatestamp,
                                           boolean fullRightsForGroup, boolean forceRefreshReaders) throws Exception {
        final String schema = newMetadata.getDataInfo().getSchemaId();

        // Check if the schema is allowed by settings
        String mdImportSetting = settingManager.getValue(Settings.METADATA_IMPORT_RESTRICT);
        if (mdImportSetting != null) {
            // Remove spaces from the list so that "iso19115-3.2018, dublin-core" will also work
            mdImportSetting = mdImportSetting.replace(" ", "");
        }
        if (!StringUtils.isBlank(mdImportSetting)) {
            if (!newMetadata.getHarvestInfo().isHarvested() && !Arrays.asList(mdImportSetting.split(",")).contains(schema)) {
                throw new IllegalArgumentException("The system setting '" + Settings.METADATA_IMPORT_RESTRICT
                    + "' doesn't allow to import " + schema
                    + " metadata records (they can still be harvested). "
                    + "Apply an import stylesheet to convert file to one of the allowed schemas: " + mdImportSetting);
            }
        }

        // --- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);

        if (updateFixedInfo && newMetadata.getDataInfo().getType() == MetadataType.METADATA) {
            String parentUuid = null;
            metadataXml = updateFixedInfo(schema, Optional.absent(), newMetadata.getUuid(), metadataXml, parentUuid,
                updateDatestamp, context);
        }

        // --- store metadata
        final AbstractMetadata savedMetadata = getXmlSerializer().insert(newMetadata, metadataXml, context);

        final String stringId = String.valueOf(savedMetadata.getId());
        String groupId = null;
        final Integer groupIdI = newMetadata.getSourceInfo().getGroupOwner();
        if (groupIdI != null) {
            groupId = String.valueOf(groupIdI);
        }
        metadataOperations.copyDefaultPrivForGroup(context, stringId, groupId, fullRightsForGroup);

        if (indexingMode != IndexingMode.none) {
            metadataIndexer.indexMetadata(stringId, forceRefreshReaders, indexingMode);
        }

        return savedMetadata;
    }

    /**
     * Retrieves a metadata (in xml) given its id; adds editing information if
     * requested and validation errors if requested.
     *
     * @param forEditing             Add extra element to build metadocument
     *                               {@link EditLib#expandElements(String, Element)}
     * @param applyOperationsFilters Filter elements based on operation filters
     *                               eg. Remove WMS if not dynamic. For example, when processing
     *                               a record, the complete records need to be processed and saved (not a filtered version). If editing, set it to false.
     *                               {@link EditLib#expandElements(String, Element)}
     * @param keepXlinkAttributes    When XLinks are resolved in non edit mode, do not remove XLink
     *                               attributes.
     */
    @Override
    public Element getMetadata(ServiceContext srvContext, String id, boolean forEditing, boolean applyOperationsFilters,
                               boolean withEditorValidationErrors, boolean keepXlinkAttributes) throws Exception {
        boolean doXLinks = getXmlSerializer().resolveXLinks();
        Element metadataXml = getXmlSerializer().selectNoXLinkResolver(id, false, applyOperationsFilters);
        if (metadataXml == null)
            return null;

        String version = null;

        if (forEditing) { // copy in xlink'd fragments but leave xlink atts to editor
            if (doXLinks)
                Processor.processXLink(metadataXml, srvContext);
            String schema = metadataSchemaUtils.getMetadataSchema(id);

            // Inflate metadata
            metadataXml = inflateMetadata(metadataXml, schema, srvContext.getLanguage());

            if (withEditorValidationErrors) {
                final Pair<Element, String> versionAndReport = metadataValidator
                    .doValidate(srvContext.getUserSession(), schema, id, metadataXml, srvContext.getLanguage(), forEditing);
                version = versionAndReport.two();
                // Add the validation report to the record
                // under a geonet:report element. The report
                // contains both XSD and schematron errors.
                // The report is used when building the editor form
                // to display errors related to elements.
                metadataXml.addContent(versionAndReport.one());
            } else {
                editLib.expandElements(schema, metadataXml);
                version = editLib.getVersionForEditing(schema, id, metadataXml);
            }
        } else {
            if (doXLinks) {
                if (keepXlinkAttributes) {
                    Processor.processXLink(metadataXml, srvContext);
                } else {
                    Processor.detachXLink(metadataXml, srvContext);
                }
            }
        }

        metadataXml.addNamespaceDeclaration(Edit.NAMESPACE);
        Element info = buildInfoElem(srvContext, id, version);
        metadataXml.addContent(info);

        metadataXml.detach();
        return metadataXml;
    }

    /**
     * Retrieves a metadata (in xml) given its id. Use this method when you must
     * retrieve a metadata in the same transaction.
     */
    @Override
    public Element getMetadata(String id) throws Exception {
        Element md = getXmlSerializer().selectNoXLinkResolver(id, false, true);
        if (md == null)
            return null;
        md.detach();
        return md;
    }

    /**
     * For update of owner info.
     */
    @Override
    public synchronized void updateMetadataOwner(final int id, final String owner, final String groupOwner) throws Exception {
        metadataRepository.update(id, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setGroupOwner(Integer.valueOf(groupOwner));
                entity.getSourceInfo().setOwner(Integer.valueOf(owner));
            }
        });
    }

    /**
     * Updates a metadata record. Deletes validation report currently in session (if
     * any). If user asks for validation the validation report will be (re-)created
     * then.
     *
     * @return metadata if the that was updated
     */
    @Override
    public synchronized AbstractMetadata updateMetadata(final ServiceContext context, final String metadataId, final Element md,
                                                        final boolean validate, final boolean ufo, final String lang, String changeDate,
                                                        final boolean updateDateStamp, final IndexingMode indexingMode) throws Exception {
        Log.trace(Geonet.DATA_MANAGER, "Update record with id " + metadataId);

        Element metadataXml = md;

        // when invoked from harvesters, session is null?
        UserSession session = context.getUserSession();
        if (session != null) {
            session.removeProperty(Geonet.Session.VALIDATION_REPORT + metadataId);
        }
        String schema = metadataSchemaUtils.getMetadataSchema(metadataId);

        final AbstractMetadata metadata = metadataUtils.findOne(metadataId);

        if (updateDateStamp) {
            if (StringUtils.isEmpty(changeDate)) {
                changeDate = new ISODate().toString();
                metadata.getDataInfo().setChangeDate(new ISODate());
            } else {
                metadata.getDataInfo().setChangeDate(new ISODate(changeDate));
            }
        }

        String uuidBeforeUfo = null;
        if (ufo) {
            String parentUuid = null;
            Integer intId = Integer.valueOf(metadataId);

            uuidBeforeUfo = findUuid(metadataXml, schema, metadata);

            metadataXml = updateFixedInfo(schema, Optional.of(intId), uuidBeforeUfo, metadataXml, parentUuid,
                (updateDateStamp ? UpdateDatestamp.YES : UpdateDatestamp.NO), context);
        }

        // --- force namespace prefix for iso19139 metadata
        setNamespacePrefixUsingSchemas(schema, metadataXml);

        String uuid = findUuid(metadataXml, schema, metadata);

        metadataUtils.checkMetadataWithSameUuidExist(uuid, metadata.getId());

        // --- write metadata to dbms
        getXmlSerializer().update(metadataId, metadataXml, changeDate, updateDateStamp, uuid, context);

        try {
            // --- do the validation last - it throws exceptions
            if (session != null && validate) {
                metadataValidator.doValidate(session, schema, metadataId, metadataXml, lang, false);
            }
        } finally {
            if (indexingMode != IndexingMode.none) {
                // Delete old record if UUID changed
                if (uuidBeforeUfo != null && !uuidBeforeUfo.equals(uuid)) {
                    getSearchManager().delete(String.format("+uuid:\"%s\"", uuidBeforeUfo));
                }
                metadataIndexer.indexMetadata(metadataId, true, indexingMode);
            }
        }

//		  TODO: TODOES Searhc for related records with an XLink pointing to this subtemplate
//        if (metadata.getDataInfo().getType() == MetadataType.SUB_TEMPLATE) {
//            MetaSearcher searcher = searcherForReferencingMetadata(context, metadata);
//            Map<Integer, AbstractMetadata> result = ((LuceneSearcher) searcher).getAllMdInfo(context, 500);
//            for (Integer id : result.keySet()) {
//                IndexingList list = context.getBean(IndexingList.class);
//                list.add(id);
//            }
//        }

        Log.trace(Geonet.DATA_MANAGER, "Finishing update of record with id " + metadataId);
        // Return an up to date metadata record
        return metadataUtils.findOne(metadataId);
    }

    private String findUuid(Element metadataXml, String schema, AbstractMetadata metadata) throws Exception {
        String uuid = null;

        if (schemaManager.getSchema(schema).isReadwriteUUID() && metadata.getDataInfo().getType() != MetadataType.SUB_TEMPLATE
            && metadata.getDataInfo().getType() != MetadataType.TEMPLATE_OF_SUB_TEMPLATE) {
            uuid = metadataUtils.extractUUID(schema, metadataXml);
        }
        return uuid;
    }

    /**
     * buildInfoElem contains similar portion of code with indexMetadata
     */
    private Element buildInfoElem(ServiceContext context, String id, String version) throws Exception {
        AbstractMetadata metadata = metadataUtils.findOne(id);
        final MetadataDataInfo dataInfo = metadata.getDataInfo();
        String schema = dataInfo.getSchemaId();
        String createDate = dataInfo.getCreateDate().getDateAndTime();
        String changeDate = dataInfo.getChangeDate().getDateAndTime();
        String source = metadata.getSourceInfo().getSourceId();
        String isTemplate = dataInfo.getType().codeString;
        @SuppressWarnings("deprecation")
        String title = dataInfo.getTitle();
        String uuid = metadata.getUuid();
        String isHarvested = "" + Constants.toYN_EnabledChar(metadata.getHarvestInfo().isHarvested());
        String harvestUuid = metadata.getHarvestInfo().getUuid();
        String popularity = "" + dataInfo.getPopularity();
        String rating = "" + dataInfo.getRating();
        String owner = "" + metadata.getSourceInfo().getOwner();
        Integer groupOwner = metadata.getSourceInfo().getGroupOwner();
        String displayOrder = "" + dataInfo.getDisplayOrder();

        Element info = new Element(Edit.RootChild.INFO, Edit.NAMESPACE);

        addElement(info, Edit.Info.Elem.ID, id);
        addElement(info, Edit.Info.Elem.SCHEMA, schema);
        addElement(info, Edit.Info.Elem.CREATE_DATE, createDate);
        addElement(info, Edit.Info.Elem.CHANGE_DATE, changeDate);
        addElement(info, Edit.Info.Elem.IS_TEMPLATE, isTemplate);
        addElement(info, Edit.Info.Elem.TITLE, title);
        addElement(info, Edit.Info.Elem.SOURCE, source);
        addElement(info, Edit.Info.Elem.UUID, uuid);
        addElement(info, Edit.Info.Elem.IS_HARVESTED, isHarvested);
        addElement(info, Edit.Info.Elem.POPULARITY, popularity);
        addElement(info, Edit.Info.Elem.RATING, rating);
        addElement(info, Edit.Info.Elem.DISPLAY_ORDER, displayOrder);

        if (metadata.getHarvestInfo().isHarvested()) {
            if (harvestInfoProvider != null) {
                info.addContent(harvestInfoProvider.getHarvestInfo(harvestUuid, id, uuid));
            }
        }
        if (version != null) {
            addElement(info, Edit.Info.Elem.VERSION, version);
        }

        Map<String, Element> map = Maps.newHashMap();
        map.put(id, info);
        buildPrivilegesMetadataInfo(context, map);

        // add owner name
        java.util.Optional<User> user = userRepository.findById(Integer.parseInt(owner));
        if (user.isPresent()) {
            addElement(info, Edit.Info.Elem.OWNERID, user.get().getId());
            String ownerName = user.get().getName();
            addElement(info, Edit.Info.Elem.OWNERNAME, ownerName);
        }

        // add groupowner name
        if (groupOwner != null) {
            java.util.Optional<Group> group = groupRepository.findById(groupOwner);
            if (group.isPresent()) {
                String groupOwnerName = group.get().getName();
                addElement(info, Edit.Info.Elem.GROUPOWNERNAME, groupOwnerName);
            }
        }

        for (MetadataCategory category : metadata.getCategories()) {
            addElement(info, Edit.Info.Elem.CATEGORY, category.getName());
        }

        // add subtemplates
        /*
         * -- don't add as we need to investigate indexing for the fields -- in the
         * metadata table used here List subList = getSubtemplates(dbms, schema); if
         * (subList != null) { Element subs = new Element(Edit.Info.Elem.SUBTEMPLATES);
         * subs.addContent(subList); info.addContent(subs); }
         */

        // Add validity information
        List<MetadataValidation> validationInfo = metadataValidationRepository.findAllById_MetadataId(Integer.parseInt(id));
        if (validationInfo == null || validationInfo.size() == 0) {
            addElement(info, Edit.Info.Elem.VALID, "-1");
        } else {
            String isValid = "1";
            for (Object elem : validationInfo) {
                MetadataValidation vi = (MetadataValidation) elem;
                String type = vi.getId().getValidationType();
                if (!vi.isValid()) {
                    isValid = "0";
                }

                String ratio = "xsd".equals(type) ? "" : vi.getNumFailures() + "/" + vi.getNumTests();

                info.addContent(new Element(Edit.Info.Elem.VALID + "_details").addContent(new Element("type").setText(type))
                    .addContent(new Element("status").setText(vi.isValid() ? "1" : "0").addContent(new Element("ratio").setText(ratio))));
            }
            addElement(info, Edit.Info.Elem.VALID, isValid);
        }

        // add baseUrl of this site (from settings)
        SettingInfo si = new SettingInfo();
        addElement(info, Edit.Info.Elem.BASEURL, si.getSiteUrl() + context.getBaseUrl());
        addElement(info, Edit.Info.Elem.LOCSERV, "/srv/en");
        return info;
    }

    /**
     * Update metadata record (not template) using update-fixed-info.xsl
     *
     * @param uuid            If the metadata is a new record (not yet saved), provide the uuid
     *                        for that record
     * @param updateDatestamp updateDatestamp is not used when running XSL transformation
     */
    @Override
    public Element updateFixedInfo(String schema, Optional<Integer> metadataId, String uuid, Element md, String parentUuid,
                                   UpdateDatestamp updateDatestamp, ServiceContext context) throws Exception {
        boolean autoFixing = settingManager.getValueAsBool(Settings.SYSTEM_AUTOFIXING_ENABLE, true);
        if (autoFixing) {
            LOGGER_DATA_MANAGER.debug("Autofixing is enabled, trying update-fixed-info (updateDatestamp: {})", updateDatestamp.name());

            AbstractMetadata metadata = null;
            if (metadataId.isPresent()) {
                metadata = metadataUtils.findOne(metadataId.get());
            }

            String currentUuid = metadata != null ? metadata.getUuid() : null;
            String id = metadata != null ? metadata.getId() + "" : null;
            uuid = uuid == null ? currentUuid : uuid;

            // --- setup environment
            Element env = new Element("env");
            env.addContent(new Element("id").setText(id));
            env.addContent(new Element("uuid").setText(uuid));


            env.addContent(thesaurusManager.buildResultfromThTable(context));

            Element schemaLoc = new Element("schemaLocation");
            schemaLoc.setAttribute(schemaManager.getSchemaLocation(schema, context));
            env.addContent(schemaLoc);
            env.addContent(new Element("newRecord").setText(String.valueOf(metadata == null)));

            if (updateDatestamp == UpdateDatestamp.YES) {
                String changeDate = new ISODate().toString();
                String createDate = "";
                if (metadata != null) {
                    changeDate = metadata.getDataInfo().getChangeDate().getDateAndTime();
                    createDate = metadata.getDataInfo().getCreateDate().getDateAndTime();
                } else {
                    createDate = new ISODate().toString();
                }
                env.addContent(new Element("changeDate").setText(changeDate));
                env.addContent(new Element("createDate").setText(createDate));
            }
            if (parentUuid != null) {
                env.addContent(new Element("parentUuid").setText(parentUuid));
            }
            if (metadataId.isPresent()) {
                final Path resourceDir = Lib.resource.getDir(Params.Access.PRIVATE, metadataId.get());
                env.addContent(new Element("datadir").setText(resourceDir.toString()));
            }

            // add user information to env if user is authenticated (should be)
            Element elUser = new Element("user");
            UserSession usrSess = context.getUserSession();
            if (usrSess.isAuthenticated()) {
                String myUserId = usrSess.getUserId();
                User user = getApplicationContext().getBean(UserRepository.class).findOne(myUserId);
                if (user != null) {
                    Element elUserDetails = new Element("details");
                    elUserDetails.addContent(new Element("surname").setText(user.getSurname()));
                    elUserDetails.addContent(new Element("firstname").setText(user.getName()));
                    elUserDetails.addContent(new Element("organisation").setText(user.getOrganisation()));
                    elUserDetails.addContent(new Element("username").setText(user.getUsername()));
                    elUser.addContent(elUserDetails);
                    env.addContent(elUser);
                }
            }

            // add original metadata to result
            Element result = new Element("root");
            // Remove the 'geonet' namespace to avoid adding it to the
            // processed elements in updated-fixed-info
            md.removeNamespaceDeclaration(Geonet.Namespaces.GEONET);
            result.addContent(md);
            // add 'environment' to result
            env.addContent(new Element("siteURL").setText(settingManager.getSiteURL(context)));
            env.addContent(new Element("nodeURL").setText(settingManager.getNodeURL()));
            env.addContent(new Element("node").setText(context.getNodeId()));

            // Settings were defined as an XML starting with root named config
            // Only second level elements are defined (under system).
            List<?> config = settingManager.getAllAsXML(true).cloneContent();
            for (Object c : config) {
                Element settings = (Element) c;
                env.addContent(settings);
            }

            result.addContent(env);
            // apply update-fixed-info.xsl
            Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(
                metadata != null
                    && (
                    metadata.getDataInfo().getType() == MetadataType.SUB_TEMPLATE
                        || metadata.getDataInfo().getType() == MetadataType.TEMPLATE_OF_SUB_TEMPLATE) ?
                    Geonet.File.UPDATE_FIXED_INFO_SUBTEMPLATE :
                    Geonet.File.UPDATE_FIXED_INFO);
            result = Xml.transform(result, styleSheet);
            return result;
        } else {
            LOGGER_DATA_MANAGER.debug("Autofixing is disabled, not applying update-fixed-info");
            return md;
        }
    }

    /**
     * Updates all children of the selected parent. Some elements are protected in
     * the children according to the stylesheet used in
     * xml/schemas/[SCHEMA]/update-child-from-parent-info.xsl.
     * <p>
     * Children MUST be editable and also in the same schema of the parent. If not,
     * child is not updated.
     *
     * @param srvContext service context
     * @param parentUuid parent uuid
     * @param children   children
     * @param params     parameters
     */
    @Override
    public Set<String> updateChildren(ServiceContext srvContext, String parentUuid, String[] children, Map<String, Object> params)
        throws Exception {
        String parentId = (String) params.get(Params.ID);
        String parentSchema = (String) params.get(Params.SCHEMA);

        // --- get parent metadata in read/only mode
        boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
        Element parent = getMetadata(srvContext, parentId, forEditing, false, withValidationErrors, keepXlinkAttributes);

        Element env = new Element("update");
        env.addContent(new Element("parentUuid").setText(parentUuid));
        env.addContent(new Element("siteURL").setText(settingManager.getSiteURL(srvContext)));
        env.addContent(new Element("parent").addContent(parent));

        // Set of untreated children (out of privileges, different schemas)
        Set<String> untreatedChildSet = new HashSet<String>();

        // only get iso19139 records
        for (String childId : children) {

            // Check privileges
            if (!accessManager.canEdit(srvContext, childId)) {
                untreatedChildSet.add(childId);
                LOGGER_DATA_MANAGER.debug("Could not update child ({}) because of privileges.", childId);
                continue;
            }

            Element child = getMetadata(srvContext, childId, forEditing, false, withValidationErrors, keepXlinkAttributes);

            String childSchema = child.getChild(Edit.RootChild.INFO, Edit.NAMESPACE).getChildText(Edit.Info.Elem.SCHEMA);

            // Check schema matching. CHECKME : this suppose that parent and
            // child are in the same schema (even not profil different)
            if (!childSchema.equals(parentSchema)) {
                untreatedChildSet.add(childId);
                LOGGER_DATA_MANAGER.debug("Could not update child ({}) because schema ({}) is different from the parent one ({}).",
                    new Object[]{childId, childSchema, parentSchema});
                continue;
            }
            LOGGER_DATA_MANAGER.debug("Updating child ({}) ...", childId);

            // --- setup xml element to be processed by XSLT

            Element rootEl = new Element("root");
            Element childEl = new Element("child").addContent(child.detach());
            rootEl.addContent(childEl);
            rootEl.addContent(env.detach());

            // --- do an XSL transformation

            Path styleSheet = metadataSchemaUtils.getSchemaDir(parentSchema).resolve(Geonet.File.UPDATE_CHILD_FROM_PARENT_INFO);
            Element childForUpdate = Xml.transform(rootEl, styleSheet, params);

            getXmlSerializer().update(childId, childForUpdate, new ISODate().toString(), true, null, srvContext);

            rootEl = null;
        }

        return untreatedChildSet;
    }

    // ---------------------------------------------------------------------------
    // ---
    // --- Static methods are for external modules like GAST to be able to use
    // --- them.
    // ---
    // ---------------------------------------------------------------------------

    /**
     * Add privileges information about metadata record which depends on context and
     * usually could not be stored in db or Lucene index because depending on the
     * current user or current client IP address.
     *
     * @param mdIdToInfoMap a map from the metadata Id -> the info element to which the
     *                      privilege information should be added.
     */
    @Override
    public void buildPrivilegesMetadataInfo(ServiceContext context, Map<String, Element> mdIdToInfoMap) throws Exception {
        Collection<Integer> metadataIds = Collections2.transform(mdIdToInfoMap.keySet(), new Function<String, Integer>() {
            @Nullable
            @Override
            public Integer apply(String input) {
                return Integer.valueOf(input);
            }
        });
        Specification<OperationAllowed> operationAllowedSpec = OperationAllowedSpecs.hasMetadataIdIn(metadataIds);

        final Collection<Integer> allUserGroups = accessManager.getUserGroups(context.getUserSession(), context.getIpAddress(), false);
        final SetMultimap<Integer, ReservedOperation> operationsPerMetadata = loadOperationsAllowed(context,
            where(operationAllowedSpec).and(OperationAllowedSpecs.hasGroupIdIn(allUserGroups)));
        final Set<Integer> visibleToAll = loadOperationsAllowed(context,
            where(operationAllowedSpec).and(OperationAllowedSpecs.isPublic(ReservedOperation.view))).keySet();
        final Set<Integer> downloadableByGuest = loadOperationsAllowed(context,
            where(operationAllowedSpec).and(OperationAllowedSpecs.hasGroupId(ReservedGroup.guest.getId()))
                .and(OperationAllowedSpecs.hasOperation(ReservedOperation.download))).keySet();
        final Map<Integer, MetadataSourceInfo> allSourceInfo = findAllSourceInfo(
            (Specification<Metadata>) MetadataSpecs.hasMetadataIdIn(metadataIds));

        for (Map.Entry<String, Element> entry : mdIdToInfoMap.entrySet()) {
            Element infoEl = entry.getValue();
            final Integer mdId = Integer.valueOf(entry.getKey());
            MetadataSourceInfo sourceInfo = allSourceInfo.get(mdId);
            Set<ReservedOperation> operations = operationsPerMetadata.get(mdId);
            if (operations == null) {
                operations = Collections.emptySet();
            }

            boolean isOwner = accessManager.isOwner(context, sourceInfo);

            if (isOwner) {
                operations = Sets.newHashSet(Arrays.asList(ReservedOperation.values()));
            }

            if (isOwner || operations.contains(ReservedOperation.editing)) {
                addElement(infoEl, Edit.Info.Elem.EDIT, "true");
            }

            if (isOwner) {
                addElement(infoEl, Edit.Info.Elem.OWNER, "true");
            }

            addElement(infoEl, Edit.Info.Elem.IS_PUBLISHED_TO_ALL, visibleToAll.contains(mdId));
            addElement(infoEl, ReservedOperation.view.name(), operations.contains(ReservedOperation.view));
            addElement(infoEl, ReservedOperation.notify.name(), operations.contains(ReservedOperation.notify));
            addElement(infoEl, ReservedOperation.download.name(), operations.contains(ReservedOperation.download));
            addElement(infoEl, ReservedOperation.dynamic.name(), operations.contains(ReservedOperation.dynamic));
            addElement(infoEl, ReservedOperation.featured.name(), operations.contains(ReservedOperation.featured));

            if (!operations.contains(ReservedOperation.download)) {
                addElement(infoEl, Edit.Info.Elem.GUEST_DOWNLOAD, downloadableByGuest.contains(mdId));
            }
        }
    }

    protected SetMultimap<Integer, ReservedOperation> loadOperationsAllowed(ServiceContext context,
                                                                            Specification<OperationAllowed> operationAllowedSpec) {
        final OperationAllowedRepository operationAllowedRepo = context.getBean(OperationAllowedRepository.class);
        List<OperationAllowed> operationsAllowed = operationAllowedRepo.findAll(operationAllowedSpec);
        SetMultimap<Integer, ReservedOperation> operationsPerMetadata = HashMultimap.create();
        for (OperationAllowed allowed : operationsAllowed) {
            final OperationAllowedId id = allowed.getId();
            operationsPerMetadata.put(id.getMetadataId(), ReservedOperation.lookup(id.getOperationId()));
        }
        return operationsPerMetadata;
    }

    /**
     * @param md
     * @throws Exception
     */
    private void setNamespacePrefixUsingSchemas(String schema, Element md) throws Exception {
        // --- if the metadata has no namespace or already has a namespace prefix
        // --- then we must skip this phase
        Namespace ns = md.getNamespace();
        if (ns == Namespace.NO_NAMESPACE)
            return;

        MetadataSchema mds = schemaManager.getSchema(schema);

        // --- get the namespaces and add prefixes to any that are
        // --- default (ie. prefix is '') if namespace match one of the schema
        ArrayList<Namespace> nsList = new ArrayList<Namespace>();
        nsList.add(ns);
        @SuppressWarnings("unchecked")
        List<Namespace> additionalNamespaces = md.getAdditionalNamespaces();
        nsList.addAll(additionalNamespaces);
        for (Object aNsList : nsList) {
            Namespace aNs = (Namespace) aNsList;
            if (aNs.getPrefix().equals("")) { // found default namespace
                String prefix = mds.getPrefix(aNs.getURI());
                if (prefix == null) {
                    LOGGER_DATA_MANAGER.warn(
                        "Metadata record contains a default namespace {} (with no prefix) which does not match any {} schema's namespaces.",
                        aNs.getURI(), schema);
                }
                ns = Namespace.getNamespace(prefix, aNs.getURI());
                metadataValidator.setNamespacePrefix(md, ns);
                if (!md.getNamespace().equals(ns)) {
                    md.removeNamespaceDeclaration(aNs);
                    md.addNamespaceDeclaration(ns);
                }
            }
        }
    }

    /**
     * Applies a xslt process when duplicating a metadata, typically to remove identifiers
     * or other information like DOI (Digital Object Identifiers) and returns the updated metadata.
     *
     * @param schema     Metadata schema.
     * @param md         Metadata to duplicate.
     * @param srvContext
     * @return If the xslt process exists, the metadata processed, otherwise the original metadata.
     * @throws Exception
     */
    private Element duplicateMetadata(String schema, Element md, ServiceContext srvContext) throws Exception {
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(
            Geonet.File.DUPLICATE_METADATA);

        if (Files.exists(styleSheet)) {
            // --- setup environment
            Element env = new Element("env");
            env.addContent(new Element("lang").setText(srvContext.getLanguage()));

            // add original metadata to result
            Element result = new Element("root");
            result.addContent(md);
            result.addContent(env);

            result = Xml.transform(result, styleSheet);

            return result;
        } else {
            return md;
        }
    }

    /**
     * @param root
     * @param name
     * @param value
     */
    protected static void addElement(Element root, String name, Object value) {
        root.addContent(new Element(name).setText(value == null ? "" : value.toString()));
    }

    @Override
    public AbstractMetadata save(AbstractMetadata info) {
        if (info instanceof Metadata) {
            return metadataRepository.save((Metadata) info);
        } else {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + info.getClass().getName());
        }
    }

    @Override
    public AbstractMetadata update(int id, @Nonnull Updater<? extends AbstractMetadata> updater) {
        try {
            return metadataRepository.update(id, (Updater<Metadata>) updater);
        } catch (ClassCastException t) {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + updater.getClass().getName());
        }
    }

    @Override
    public void deleteAll(Specification<? extends AbstractMetadata> specs) {
        try {
            metadataRepository.deleteAll((Specification<Metadata>) specs);
        } catch (ClassCastException t) {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
        }
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Log.trace(Geonet.DATA_MANAGER, "Deleting record with id " + id);
        if (metadataRepository.existsById(id)) {
            metadataRepository.deleteById(id);
        }
    }

    @Override
    public void createBatchUpdateQuery(PathSpec<? extends AbstractMetadata, String> servicesPath, String newUuid,
                                       Specification<? extends AbstractMetadata> harvested) {
        try {
            metadataRepository
                .createBatchUpdateQuery((PathSpec<Metadata, String>) servicesPath, newUuid, (Specification<Metadata>) harvested);
        } catch (ClassCastException t) {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + servicesPath.getClass().getName());
        }
    }

    @Override
    public Map<Integer, MetadataSourceInfo> findAllSourceInfo(Specification<? extends AbstractMetadata> specs) {
        try {
            return metadataRepository.findSourceInfo((Specification<Metadata>) specs);
        } catch (ClassCastException t) {
            throw new ClassCastException("Unknown AbstractMetadata subtype: " + specs.getClass().getName());
        }
    }

    @Override
    public boolean isValid(Integer id) {
        List<MetadataValidation> validationInfo = metadataValidationRepository.findAllById_MetadataId(id);
        if (validationInfo == null || validationInfo.size() == 0) {
            return false;
        }
        for (Object elem : validationInfo) {
            MetadataValidation vi = (MetadataValidation) elem;
            if (!vi.isValid() && vi.isRequired()) {
                return false;
            }
        }
        return true;
    }

    boolean hasReferencingMetadata(ServiceContext context, AbstractMetadata metadata) throws Exception {
        StringBuilder query = new StringBuilder(String.format("xlink:\"%s\"", metadata.getUuid()));
        return this.searchManager.query(query.toString(), null, 0, 0).hits().total().value() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element inflateMetadata(Element metadataXml, String schema, String lang) throws Exception {
        // Resolve the path to the XSLT stylesheet based on the schema.
        Path styleSheet = metadataSchemaUtils.getSchemaDir(schema).resolve(Geonet.File.INFLATE_METADATA);

        // If the stylesheet does not exist, return the original metadata.
        if (!Files.exists(styleSheet)) {
            return metadataXml; // No stylesheet to apply, return original metadata
        }

        // Create an environment element to pass additional parameters to the transformation.
        Element env = new Element("env");
        env.addContent(new Element("lang").setText(lang)); // Add the language parameter.

        // Prepare the root element containing the metadata and the environment.
        Element result = new Element("root");
        result.addContent(metadataXml); // Add the original metadata.
        result.addContent(env); // Add the environment.

        // Apply the XSLT transformation and return the transformed metadata.
        return Xml.transform(result, styleSheet);
    }

}
