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
package org.fao.geonet.kernel.harvest.harvester.sftp;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.Logger;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataSchemaUtils;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.HarvesterUtil;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.harvest.harvester.sftp.client.SftpClient;
import org.fao.geonet.kernel.harvest.harvester.sftp.client.SftpFileInfo;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.springframework.data.jpa.domain.Specification;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Aligner extends BaseAligner<SftpParams> {
    private final ServiceContext context;
    private final IMetadataUtils metadataUtils;
    private final IMetadataManager metadataManager;
    private final IMetadataIndexer metadataIndexer;
    private final IMetadataSchemaUtils metadataSchemaUtils;
    private final Logger log;

    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;

    private HarvestResult result;
    private String processName;
    private Map<String, Object> processParams = new HashMap<>();
    private final Set<Integer> idsForHarvestingResult = new HashSet<>();

    protected Aligner(AtomicBoolean cancelMonitor, ServiceContext sc, SftpParams params, Logger log) {
        super(cancelMonitor);

        metadataUtils = sc.getBean(IMetadataUtils.class);
        metadataManager = sc.getBean(IMetadataManager.class);
        metadataIndexer = sc.getBean(IMetadataIndexer.class);
        metadataSchemaUtils = sc.getBean(IMetadataSchemaUtils.class);
        this.context = sc;
        this.params = params;
        this.log = log;
    }

    public HarvestResult align(Collection<HarvestError> errors) throws Exception {
        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.xslfilter);
        processName = filter.one();
        processParams = filter.two();

        result = new HarvestResult();

        SftpClient sftpClient = new SftpClient(params.server, Integer.parseInt(params.port), params.getUsername());
        try {
            if (params.useAuthKey) {
                sftpClient.authKey(SftpHarvesterUtil.getPrivateKeyFilePath(context, params.getUuid()).toString(), "");
            } else {
                sftpClient.authPassword(params.getPassword());
            }

            String remoteFolder = normalizeFolderPath(this.params.folder);

            List<SftpFileInfo> remoteFiles = sftpClient.listFiles(remoteFolder, "xml", this.params.recurse);

            for(SftpFileInfo remoteFile : remoteFiles) {
                String fileContent = sftpClient.getFileAsText( normalizeFolderPath(remoteFile.getFolder()) + remoteFile.getFileName());
                Element md = Xml.loadString(fileContent, false);
                String schema = metadataSchemaUtils.autodetectSchema(md, null);
                String uuid = metadataUtils.extractUUID(schema, md);
                String modified = metadataUtils.extractDateModified(schema, md);

                if (schema == null) {
                    log.debug("  - Metadata skipped due to unknown schema. uuid:" + uuid);
                    result.unknownSchema++;
                    return result;
                }

                RecordInfo ri = new RecordInfo(uuid, modified);
                insertOrUpdate(ri, md, errors);
            }
        } finally {
            sftpClient.close();
        }

        log.debug("Starting to delete locally existing metadata " +
            "from the same source if they " +
            " were not in this harvesting result...");
        List<Integer> existingMetadata = context.getBean(MetadataRepository.class).findIdsBy((Specification<Metadata>) MetadataSpecs.hasHarvesterUuid(params.getUuid()));
        for (Integer existingId : existingMetadata) {
            if (cancelMonitor.get()) {
                return this.result;
            }
            if (!idsForHarvestingResult.contains(existingId)) {
                log.debug("  Removing: " + existingId);
                metadataManager.deleteMetadata(context, existingId.toString());
                result.locallyRemoved++;
            }
        }

        return result;
    }

    private void insertOrUpdate(RecordInfo ri, Element md, Collection<HarvestError> errors) {
        try {
            String id = metadataUtils.getMetadataId(ri.uuid);

            if (id == null) {
                //record doesn't exist (so it doesn't belong to this harvester)
                log.debug("Adding record with uuid " + ri.uuid);
                addMetadata(ri, md, ri.uuid);
            } else if (localUuids.getID(ri.uuid) == null) {
                //Record does not belong to this harvester
                result.datasetUuidExist++;

                switch (params.getOverrideUuid()) {
                    case OVERRIDE:
                        updateMetadata(ri, Integer.toString(metadataUtils.findOneByUuid(ri.uuid).getId()), md, true);
                        log.debug("Overriding record with uuid " + ri.uuid);

                        if (params.isIfRecordExistAppendPrivileges()) {
                            addPrivileges(id, params.getPrivileges(), localGroups, context);
                            result.privilegesAppendedOnExistingRecord++;
                        }
                        break;
                    case RANDOM:
                        log.debug("Generating random uuid for remote record with uuid " + ri.uuid);
                        addMetadata(ri, md, UUID.randomUUID().toString());
                        break;
                    case SKIP:
                        log.debug("Skipping record with uuid " + ri.uuid);
                        result.uuidSkipped++;
                        break;
                    default:
                        break;
                }
            } else {
                //record exists and belongs to this harvester
                updateMetadata(ri, id, md, false);

                if (params.isIfRecordExistAppendPrivileges()) {
                    addPrivileges(id, params.getPrivileges(), localGroups, context);
                    result.privilegesAppendedOnExistingRecord++;
                }
            }

            result.totalMetadata++;
        } catch (Exception t) {
            errors.add(new HarvestError(this.context, t));
            log.error("Unable to process record from sftp (" + this.params.getName() + ")");
            log.error("   Record failed: " + ri.uuid + ". Error is: " + t.getMessage());
            log.error(t);
        } finally {
            result.originalMetadata++;
        }
    }
    /**
     * Adds a new metadata.
     *
     * @param ri                Metadata information.
     * @param uuidToAssign      UUID to assign. It can be different from ri.uuid, depending on the override uuid policy.
     * @throws Exception
     */
    private void addMetadata(RecordInfo ri, Element md, String uuidToAssign) throws Exception {
        if (cancelMonitor.get()) {
            return;
        }

        if (md == null) {
            return;
        }

        String schema = metadataSchemaUtils.autodetectSchema(md, null);
        if (schema == null) {
            log.debug("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid);
            result.unknownSchema++;
            return;
        }

        log.debug("  - Adding metadata with remote uuid:" + ri.uuid + " schema:" + schema);

        // If the xslfilter process changes the metadata uuid,
        // use that uuid (newMdUuid) for the new metadata to add to the catalogue.
        String newMdUuid = null;
        if (!params.xslfilter.isEmpty()) {
            md = HarvesterUtil.processMetadata(metadataSchemaUtils.getSchema(schema), md, processName, processParams);
            schema = metadataSchemaUtils.autodetectSchema(md);
            // Get new uuid if modified by XSLT process
            newMdUuid = metadataUtils.extractUUID(schema, md);
        }

        boolean newMdUuidFromXslt = !StringUtils.isBlank(newMdUuid);

        // Translate metadata
        if (params.isTranslateContent()) {
            md = translateMetadataContent(context, md, schema);
        }

        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();

        if (newMdUuidFromXslt) {
            // Use the UUID from the xslt
            metadata.setUuid(newMdUuid);
        } else {
            // Use the uuid provided in the uuidToAssign parameter
            metadata.setUuid(uuidToAssign);
            if (!uuidToAssign.equals(ri.uuid)) {
                md = metadataUtils.setUUID(schema, uuidToAssign, md);
            }
        }

        Integer ownerId = getOwner();
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA).
            setChangeDate(new ISODate(ri.changeDate)).
            setCreateDate(new ISODate(ri.changeDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(ownerId).
            setGroupOwner(getGroupOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        metadata.getSourceInfo().setGroupOwner(getGroupOwner());

        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataIndexer.indexMetadata(id, true, IndexingMode.full);
        result.addedMetadata++;
        idsForHarvestingResult.add(metadata.getId());
    }

    private void updateMetadata(RecordInfo ri, String id, Element md, boolean force) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);

        if (date == null && !force) {
            log.debug("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
        } else {
            if (!force && !ri.isMoreRecentThan(date)) {
                log.debug("  - Metadata XML not changed for uuid:" + ri.uuid);
                result.unchangedMetadata++;
            } else {
                log.debug("  - Updating local metadata for uuid:" + ri.uuid);
                if (updatingLocalMetadata(ri, id, md, force)) {
                    metadataIndexer.indexMetadata(id, true, IndexingMode.full);
                    result.updatedMetadata++;
                }
            }
        }

        idsForHarvestingResult.add(Integer.parseInt(id));
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    boolean updatingLocalMetadata(RecordInfo ri, String id, Element md, boolean force) throws Exception {
        if (md == null) {
            return false;
        }

        String schema = metadataSchemaUtils.autodetectSchema(md, null);

        boolean updateSchema = false;

        if (!params.xslfilter.isEmpty()) {
            md = HarvesterUtil.processMetadata(metadataSchemaUtils.getSchema(schema), md, processName, processParams);
            String newSchema = metadataSchemaUtils.autodetectSchema(md);
            updateSchema = !newSchema.equals(schema);
            schema = newSchema;
        }

        boolean validate = false;
        boolean ufo = false;
        String language = context.getLanguage();
        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, md, validate, ufo, language, ri.changeDate, true, IndexingMode.none);

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

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, null, true);

        return true;
    }

    private String normalizeFolderPath(String path) {
        String fixedPath;
        if (path == null) {
            fixedPath = "/";
        } else if (!path.startsWith("/")) {
            fixedPath = "/" + path;
        } else {
            fixedPath = path;
        }

        fixedPath += fixedPath.endsWith("/") ? "" :"/";

        return fixedPath;
    }
}
