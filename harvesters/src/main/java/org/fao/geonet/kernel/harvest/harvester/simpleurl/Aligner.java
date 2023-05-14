//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.simpleurl;

import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.jdom.Element;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.kernel.harvest.harvester.csw.Aligner.applyBatchEdits;

public class Aligner extends BaseAligner<SimpleUrlParams> {

    private ServiceContext context;
    private DataManager dataMan;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;

    private IMetadataUtils metadataUtils;
    private IMetadataManager metadataManager;
    private IMetadataIndexer metadataIndexer;

    private HarvestResult result;

    public HarvestResult getResult() {
        return result;
    }
    private Map<String, Object> processParams = new HashMap<String, Object>();
    private Logger log;

    public Aligner(AtomicBoolean cancelMonitor, ServiceContext sc, SimpleUrlParams params, Logger log) throws OperationAbortedEx {
        super(cancelMonitor);
        this.context = sc;
        this.params = params;
        this.log = log;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        metadataUtils = gc.getBean(IMetadataUtils.class);
        metadataManager = gc.getBean(IMetadataManager.class);
        metadataIndexer = gc.getBean(IMetadataIndexer.class);
        result = new HarvestResult();
        result.unretrievable = 0;
        result.uuidSkipped = 0;
        result.couldNotInsert = 0;
    }

    public HarvestResult align(Map<String, Element> records, Collection<HarvestError> errors) throws Exception {
        if (cancelMonitor.get()) {
            return result;
        }

        log.debug("Start of alignment for : " + params.getName());

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        insertOrUpdate(records, errors);
        log.debug("End of alignment for : " + params.getName());

        return result;
    }

    private void insertOrUpdate(Map<String, Element> records, Collection<HarvestError> errors) {
        records.entrySet().forEach(e -> {
            if (cancelMonitor.get()) {
                return;
            }

            try {
                String id = metadataUtils.getMetadataId(e.getKey());

                if (id == null) {
                    //record doesn't exist (so it doesn't belong to this harvester)
                    log.debug("Adding record with uuid " + e.getKey());
                    addMetadata(e, null);
                } else if (localUuids.getID(e.getKey()) == null) {
                    //Record does not belong to this harvester
                    result.datasetUuidExist++;

                    switch (params.getOverrideUuid()) {
                        case OVERRIDE:
                            updateMetadata(e, Integer.toString(metadataUtils.findOneByUuid(e.getKey()).getId()), true);
                            log.debug("Overriding record with uuid " + e.getKey());
                            result.updatedMetadata++;
                            break;
                        case RANDOM:
                            log.debug("Generating random uuid for remote record with uuid " + e.getKey());
                            addMetadata(e, UUID.randomUUID().toString());
                            break;
                        case SKIP:
                            log.debug("Skipping record with uuid " + e.getKey());
                            result.uuidSkipped++;
                        default:
                            break;
                    }
                } else {
                    //record exists and belongs to this harvester
                    updateMetadata(e, id, false);
                    if (params.isIfRecordExistAppendPrivileges()) {
                        addPrivileges(id, params.getPrivileges(), localGroups, context);
                        result.privilegesAppendedOnExistingRecord++;
                    }
                }
                result.totalMetadata++;
            } catch (Throwable t) {
                errors.add(new HarvestError(this.context, t));
                log.error("Unable to process record from csw (" + this.params.getName() + ")");
                log.error("   Record failed: " + e.getKey() + ". Error is: " + t.getMessage());
                log.error(t);
            } finally {
                result.originalMetadata++;
            }
        });
    }

    /**
     * Remove records no longer on the remote CSW server
     *
     * @param records
     * @throws Exception
     */
    @Transactional(value = TxType.REQUIRES_NEW)
    public HarvestResult cleanupRemovedRecords(Set<String> records) throws Exception {

        if (cancelMonitor.get()) {
            return result;
        }

        for (String uuid : localUuids.getUUIDs()) {
            if (!records.contains(uuid)) {
                String id = localUuids.getID(uuid);
                log.debug("  - Removing old metadata with local id:" + id);
                metadataManager.deleteMetadata(context, id);
                result.locallyRemoved ++;
            }
        }
        dataMan.forceIndexChanges();

        return result;
    }


    private void addMetadata(Map.Entry<String, Element> record, String overrideUuidValue) throws Exception {
        if (cancelMonitor.get()) {
            return;
        }

        Element xml = record.getValue();
        if (xml == null) {
            result.unretrievable++;
            return;
        }

        String schema = dataMan.autodetectSchema(xml, null);
        if (schema == null) {
            log.debug("  - Metadata skipped due to unknown schema. uuid:" + record.getKey());
            result.unknownSchema++;
            return;
        }

        String uuid = record.getKey();
        if (overrideUuidValue != null) {
            log.debug(String.format("  - Overriding UUID %s by %s", record.getKey(), overrideUuidValue));
            uuid = overrideUuidValue;
            xml = dataMan.setUUID(schema, uuid, record.getValue());
        }

        applyBatchEdits(uuid, xml, schema, params.getBatchEdits(), context, null);

        log.debug("  - Adding metadata with uuid:" + uuid + " schema:" + schema);

        final String dateModified = dataMan.extractDateModified(schema, xml);

        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        Integer ownerId = getOwner();
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(xml.getQualifiedName()).
            setType(MetadataType.METADATA).
            setChangeDate(new ISODate(dateModified)).
            setCreateDate(new ISODate(dateModified));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(ownerId).
            setGroupOwner(getGroupOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        metadata.getSourceInfo().setGroupOwner(getGroupOwner());

        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, xml, IndexingMode.none, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataIndexer.indexMetadata(id, true, IndexingMode.full);
        result.addedMetadata++;
    }


    @Transactional(value = TxType.REQUIRES_NEW)
    boolean updateMetadata(Map.Entry<String, Element> ri, String id, Boolean force) throws Exception {
        Element md = ri.getValue();
        if (md == null) {
            result.unchangedMetadata++;
            return false;
        }

        boolean validate = false;
        boolean ufo = false;
        String language = context.getLanguage();
        String schema = dataMan.autodetectSchema(md, null);
        final String dateModified = dataMan.extractDateModified(schema, ri.getValue());

        applyBatchEdits(ri.getKey(), md, schema, params.getBatchEdits(), context, null);

        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, md, validate, ufo,
            language, dateModified, true, IndexingMode.none);

        if (force) {
            //change ownership of metadata to new harvester
            metadata.getHarvestInfo().setUuid(params.getUuid());
            metadata.getSourceInfo().setSourceId(params.getUuid());

            metadataManager.save((Metadata) metadata);
        }

        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
        repository.deleteAllByMetadataId(Integer.parseInt(id));

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, null, true);
        result.updatedMetadata++;
        return true;
    }
}
