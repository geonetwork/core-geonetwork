//=============================================================================
//===	Copyright (C) 2001-2025 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.database;

import com.google.common.collect.Sets;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.kernel.harvest.harvester.csw.Aligner.applyBatchEdits;

class DatabaseHarvesterAligner extends BaseAligner<DatabaseHarvesterParams> implements IHarvester<HarvestResult> {
    private Logger log;
    private final ServiceContext context;
    private final DataManager dataMan;
    private final IMetadataManager metadataManager;
    private final IMetadataUtils metadataUtils;
    private final IMetadataIndexer metadataIndexer;
    private final IMetadataSchemaUtils metadataSchemaUtils;
    private final MetadataRepository metadataRepository;
    private HarvestResult result;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;
    private List<HarvestError> errors;
    private List<Integer> idsForHarvestingResult;
    private String processName;
    private Map<String, Object> processParams = new HashMap<>();

    public DatabaseHarvesterAligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, DatabaseHarvesterParams params, List<HarvestError> errors) {
        super(cancelMonitor);
        this.log = log;
        this.context = context;
        this.params = params;
        this.errors = errors;

        result = new HarvestResult();
        result.addedMetadata = 0;
        result.uuidSkipped = 0;
        result.datasetUuidExist = 0;
        result.couldNotInsert = 0;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        metadataManager = gc.getBean(IMetadataManager.class);
        metadataSchemaUtils = gc.getBean(IMetadataSchemaUtils.class);
        metadataUtils = gc.getBean(IMetadataUtils.class);
        metadataIndexer = gc.getBean(IMetadataIndexer.class);
        metadataRepository = gc.getBean(MetadataRepository.class);
    }

    @Override
    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        if (log.isDebugEnabled()) {
            log.debug("Retrieving remote metadata information for : " + params.getName());
        }

        DatabaseMetadataRetriever metadataRetriever = DatabaseMetadataRetrieverFactory.getDatabaseMetadataRetriever(params.getDatabaseType(), params.getServer(), params.getPort(),
            params.getDatabase(), params.getUsername(), params.getPassword(), log);

        log.info("Start of alignment for : " + params.getName());
        result = new HarvestResult();
        //----------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(metadataUtils, params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.getXslfilter());
        processName = filter.one();
        processParams = filter.two();


        metadataManager.flush();

        idsForHarvestingResult = new ArrayList<>();

        metadataRetriever.processMetadata(cancelMonitor, params, this);

        //
        // delete locally existing metadata from the same source if they were
        // not in this harvesting result
        //
        deleteLocalMetadataNotInDatabase(idsForHarvestingResult);

        return result;
    }

    public void align(String metadata) {
        if (cancelMonitor.get()) {
            return;
        }

        try {
            result.totalMetadata++;

            if (!StringUtils.hasLength(metadata)) {
                log.info("Processing empty metadata xml. Skipping");
                return;
            }

            // create JDOM element from String-XML
            Element metadataElement = Xml.loadString(metadata, false);
            String id = processMetadata(metadataElement);

            if (StringUtils.hasLength(id)) {
                idsForHarvestingResult.add(Integer.valueOf(id));
            }

        } catch (Exception ex) {
            log.error("Unable to process record from database (" + this.params.getName() + ")");
            log.error("   Record failed. Error is: " + ex.getMessage());
            log.error(ex);
            errors.add(new HarvestError(this.context, ex));
        } finally {
            result.originalMetadata++;
        }
    }

    private void deleteLocalMetadataNotInDatabase(List<Integer> idsForHarvestingResult) throws Exception {
        Set<Integer> idsResultHs = Sets.newHashSet(idsForHarvestingResult);
        List<Integer> existingMetadata = metadataRepository.findIdsBy(MetadataSpecs.hasHarvesterUuid(params.getUuid()));
        for (Integer existingId : existingMetadata) {
            if (cancelMonitor.get()) {
                return;
            }

            if (!idsResultHs.contains(existingId)) {
                log.debug("  Removing: " + existingId);
                metadataManager.deleteMetadata(context, existingId.toString());
                result.locallyRemoved++;
            }
        }
    }


    /**
     * Process a metadata to add it to the catalog and returns the identifier.
     *
     * @param metadataElement
     * @return
     * @throws Exception
     */
    private String processMetadata(Element metadataElement) throws Exception {

        String id = "";

        String schema = getMetadataSchema(metadataElement);

        if (schema == null) {
            log.info("Skipping metadata with unknown schema.");
            result.unknownSchema++;
            return id;
        }

        String uuid = metadataUtils.extractUUID(schema, metadataElement);

        if (!StringUtils.hasLength(uuid)) {
            log.info("No metadata uuid. Skipping.");
            result.badFormat++;
            return id;
        }

        log.info(String.format("Processing metadata with UUID: %s", uuid));

        try {
            Integer groupIdVal = null;
            if (StringUtils.hasLength(params.getOwnerIdGroup())) {
                groupIdVal = Integer.parseInt(params.getOwnerIdGroup());
            }

            params.getValidate().validate(dataMan, context, metadataElement, groupIdVal);
        } catch (Exception e) {
            log.error("Ignoring invalid metadata with uuid " + uuid);
            result.doesNotValidate++;
            return id;
        }

        setParams(params);

        //
        // add / update the metadata from this harvesting result
        //
        id = metadataUtils.getMetadataId(uuid);
        if (id == null) {
            //Record is new
            id = addMetadata(metadataElement, uuid, schema);
            result.addedMetadata++;
        } else if (localUuids.getID(uuid) == null) {
            //Record does not belong to this harvester
            result.datasetUuidExist++;

            switch (params.getOverrideUuid()) {
                case OVERRIDE:
                    updateMetadata(metadataElement, metadataUtils.findOneByUuid(uuid), true);
                    log.debug(String.format("Overriding record with uuid %s", uuid));
                    result.updatedMetadata++;
                    break;
                case RANDOM:
                    log.debug(String.format("Generating random uuid for remote record with uuid %s", uuid));
                    addMetadata(metadataElement, UUID.randomUUID().toString(), schema);
                    break;
                case SKIP:
                    log.debug(String.format("Skipping record with uuid %s", uuid));
                    result.uuidSkipped++;
                    break;
                default:
                    break;
            }
        } else {
            //record exists and belongs to this harvester
            updateMetadata(metadataElement, metadataUtils.findOne(id), false);
            result.updatedMetadata++;
        }

        return id;
    }

    private void updateMetadata(Element xml, AbstractMetadata originalMetadata, boolean force) throws Exception {
        String id = Integer.toString(originalMetadata.getId());
        log.info("Updating metadata with id: " + id);

        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        String language = context.getLanguage();

        String schema = metadataSchemaUtils.autodetectSchema(xml);
        String uuid = metadataUtils.extractUUID(schema, xml);

        String changeDate;
        try {
            changeDate = metadataUtils.extractDateModified(schema, xml);
        } catch (Exception ex) {
            log.error("Database harvester - updateMetadata - can't get metadata modified date for metadata id= " + id +
                ", using current date for modified date");
            changeDate = new ISODate().toString();
        }

        boolean updateSchema = false;
        if (StringUtils.hasLength(params.getXslfilter())) {
            xml = applyXSLTProcessToMetadata(context, xml, processName, processParams, log);
            String newSchema = metadataSchemaUtils.autodetectSchema(xml);
            updateSchema = (newSchema != null) && !newSchema.equals(schema);
            schema = newSchema;
        } else {
            if (!originalMetadata.getDataInfo().getSchemaId().equals(schema)) {
                log.warning("  - Detected schema '" + schema + "' is different from the one of the metadata in the catalog '" + originalMetadata.getDataInfo().getSchemaId() + "'. Using the detected one.");
                updateSchema = true;
            }
        }

        applyBatchEdits(uuid, xml, schema, params.getBatchEdits(), context, log);

        // Translate metadata
        if (params.isTranslateContent()) {
            xml = translateMetadataContent(context, xml, schema);
        }

        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, xml, validate, ufo, language, changeDate,
            true, IndexingMode.none);

        if (force || updateSchema) {
            if (force) {
                //change ownership of metadata to new harvester
                metadata.getHarvestInfo().setUuid(params.getUuid());
                metadata.getSourceInfo().setSourceId(params.getUuid());

            }

            if (updateSchema) {
                metadata.getDataInfo().setSchemaId(schema);
            }

            metadataManager.save(metadata);
        }

        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        operationAllowedRepository.deleteAllByMetadataId(Integer.parseInt(id));
        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, null, true);

        metadataManager.flush();
        metadataIndexer.indexMetadata(id, true, IndexingMode.full);
    }

    /**
     * Inserts a metadata into the database. Lucene index is updated after insertion.
     */
    private String addMetadata(Element xml, String uuid, String schema) throws Exception {
        log.info("  - Adding metadata with remote uuid: " + uuid);

        // If the xslfilter process changes the metadata uuid,
        // use that uuid (newMdUuid) for the new metadata to add to the catalogue.
        String newMdUuid = null;
        if (StringUtils.hasLength(params.getXslfilter())) {
            xml = applyXSLTProcessToMetadata(context, xml, processName, processParams, log);
            schema = metadataSchemaUtils.autodetectSchema(xml);
            // Get new uuid if modified by XSLT process
            newMdUuid = metadataUtils.extractUUID(schema, xml);
        }

        boolean newMdUuidFromXslt = StringUtils.hasLength(newMdUuid);

        if (!newMdUuidFromXslt) {
            applyBatchEdits(uuid, xml, schema, params.getBatchEdits(), context, log);
        } else {
            applyBatchEdits(newMdUuid, xml, schema, params.getBatchEdits(), context, log);
        }

        // Translate metadata
        if (params.isTranslateContent()) {
            xml = translateMetadataContent(context, xml, schema);
        }

        //
        // insert metadata
        //
        ISODate createDate;
        try {
            createDate = new ISODate(metadataUtils.extractDateModified(schema, xml));
        } catch (Exception ex) {
            log.error("Database harvester - addMetadata - can't get metadata modified date for metadata with uuid= " +
                uuid + ", using current date for modified date");
            createDate = new ISODate();
        }

        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(xml.getQualifiedName()).
            setType(MetadataType.METADATA).
            setCreateDate(createDate).
            setChangeDate(createDate);
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(Integer.parseInt(params.getOwnerId())).
            setGroupOwner(getGroupOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, xml, IndexingMode.none, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataIndexer.indexMetadata(id, true, IndexingMode.full);

        return id;
    }

    private String getMetadataSchema(Element metadataElement) {
        try {
            return metadataSchemaUtils.autodetectSchema(metadataElement, null);
        } catch (NoSchemaMatchesException ex) {
            return null;
        }
    }
}
