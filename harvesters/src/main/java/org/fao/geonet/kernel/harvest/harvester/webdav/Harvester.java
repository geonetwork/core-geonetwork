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
package org.fao.geonet.kernel.harvest.harvester.webdav;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.*;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;

import jeeves.server.context.ServiceContext;

//=============================================================================

interface RemoteRetriever {
    public void init(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WebDavParams params);

    public List<RemoteFile> retrieve() throws Exception;

    public void destroy();
}

//=============================================================================

interface RemoteFile {
    public String getPath();

    public ISODate getChangeDate();

    public Element getMetadata(SchemaManager schemaMan) throws Exception;

    public boolean isMoreRecentThan(String localDate);
}

//=============================================================================

class Harvester extends BaseAligner<WebDavParams> implements IHarvester<HarvestResult> {

    private Logger log;
    private ServiceContext context;
    private DataManager dataMan;
    private IMetadataManager metadataManager;
    private MetadataRepository metadataRepository;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UriMapper localUris;
    private HarvestResult result;
    private SchemaManager schemaMan;
    private List<HarvestError> errors;
    private String processName;
    private Map<String, Object> processParams = new HashMap<>();

    public Harvester(AtomicBoolean cancelMonitor, Logger log, ServiceContext context, WebDavParams params, List<HarvestError> errors) {
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
        schemaMan = gc.getBean(SchemaManager.class);
        metadataRepository = gc.getBean(MetadataRepository.class);
    }

    //---------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //---------------------------------------------------------------------------
    @Override
    public HarvestResult harvest(Logger log) throws Exception {
        this.log = log;
        if (log.isDebugEnabled())
            log.debug("Retrieving remote metadata information for : " + params.getName());
        RemoteRetriever rr = null;
        if (params.subtype.equals("webdav")) {
            rr = new WebDavRetriever();
        } else if (params.subtype.equals("waf")) {
            rr = new WAFRetriever();
        } else {
            throw new IllegalArgumentException(params.subtype + " is not one of webdav or waf");
        }
        try {
            Log.info(Log.SERVICE, "webdav harvest subtype : " + params.subtype);
            rr.init(cancelMonitor, log, context, params);
            List<RemoteFile> files = rr.retrieve();
            log.info("Number of remote files found : " + files.size());
            align(files);
        } finally {
            rr.destroy();
        }
        return result;
    }

    private void align(final List<RemoteFile> files) throws Exception {
        log.info("Start of alignment for : " + params.getName());
        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node
        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUris = new UriMapper(context, params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.xslfilter);
        processName = filter.one();
        processParams = filter.two();

        //-----------------------------------------------------------------------
        //--- remove old metadata
        for (final String uri : localUris.getUris()) {
            if (cancelMonitor.get()) {
                return;
            }

            if (!exists(files, uri)) {
                // only one metadata record created per uri by this harvester
                String id = localUris.getRecords(uri).get(0).id;
                if (log.isDebugEnabled()) {
                    log.debug("  - Removing old metadata with local id:" + id);
                }
                try {
                    metadataManager.deleteMetadataGroup(context, id);
                } catch (Exception e) {
                    log.error("Error occurred while deleting metadata id");
                }
                metadataManager.flush();
                result.locallyRemoved++;

            }
        }
        //-----------------------------------------------------------------------
        //--- insert/update new metadata

        for (RemoteFile rf : files) {
            if (cancelMonitor.get()) {
                return;
            }

            result.totalMetadata++;
            List<RecordInfo> records = localUris.getRecords(rf.getPath());
            if (records == null) {
                addMetadata(rf);
            } else {
                // only one metadata record created per uri by this harvester
                updateMetadata(rf, records.get(0), false);
            }
        }
        log.info("End of alignment for : " + params.getName());
    }

    /**
     * Returns true if the uri is present in the remote folder
     */
    private boolean exists(List<RemoteFile> files, String uri) {
        for (RemoteFile rf : files) {
            if (uri.equals(rf.getPath())) {
                return true;
            }
        }
        return false;
    }

    /**
     * To determine the UUID we are going to use the following mechanism: 1.- Look for the file
     * identifier on the metadata xml 2.- If there is no file identifier, then use the name of the
     * file 3.- If there is a collision of uuid with existent metadata, use a random one 4.- If we
     * still don't have a clear UUID, use a random one (backup plan)
     **/
    private void addMetadata(RemoteFile rf) throws Exception {
        Element md = retrieveMetadata(rf);
        if (md == null) {
            return;
        }
        //--- schema handled check already done
        String schema = dataMan.autodetectSchema(md);


        // 1.- Look for the file identifier on the metadata xml
        String uuid = dataMan.extractUUID(schema, md);

        // 2.- If there is no file identifier, then use the name of the file
        if (uuid == null) {
            String path = rf.getPath();
            int start = path.lastIndexOf("/") + 1;
            uuid = path.substring(start, path.length() - 4);
        }

        // 3.- If there is a collision of uuid with existent metadata, use a
        // random one
        if (dataMan.existsMetadataUuid(uuid)) {
            result.datasetUuidExist++;
            switch(params.getOverrideUuid()){
            case OVERRIDE:
                Metadata existingMetadata = metadataRepository.findOneByUuid(uuid);
                RecordInfo existingRecordInfo = new RecordInfo(existingMetadata);
                updateMetadata(rf, existingRecordInfo, true);
                log.info("Overriding record with uuid " + uuid);
                result.updatedMetadata++;

                if (params.isIfRecordExistAppendPrivileges()) {
                    addPrivileges(dataMan.getMetadataId(uuid),
                        params.getPrivileges(), localGroups, context);
                    result.privilegesAppendedOnExistingRecord++;
                }
                return;
            case RANDOM:
                log.info("Generating random uuid for remote record with uuid " + uuid);
                uuid = null;
                break;
            case SKIP:
                log.info("Skipping record with uuid " + uuid);
                result.uuidSkipped++;
                return;
            default:
                return;
            }
        }

        // 4.- If we still don't have a clear UUID, use a random one (backup
        // plan)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            log.debug("  - Setting uuid for metadata with remote path : "
                + rf.getPath());

            // --- set uuid inside metadata and get new xml
            try {
                md = dataMan.setUUID(schema, uuid, md);
                result.addedMetadata++;
            } catch (Exception e) {
                log.error("  - Failed to set uuid for metadata with remote path : "
                    + rf.getPath());
                errors.add(new HarvestError(this.context, e));
                result.couldNotInsert++;
                return;
            }
        }

        if (log.isDebugEnabled())
            log.debug("  - Adding metadata with remote path : " + rf.getPath());

        // Translate metadata
        if (params.isTranslateContent()) {
            md = translateMetadataContent(context, md, schema);
        }

        if (StringUtils.isNotEmpty(params.xslfilter)) {
            md = HarvesterUtil.processMetadata(dataMan.getSchema(schema),
                md, processName, processParams);

            schema = dataMan.autodetectSchema(md);
        }

        //
        // insert metadata
        //

        // Get the change date from the metadata content. If not possible, get it from the file change date if available
        // and if not possible use current date
        ISODate date = null;

        try {
            date = new ISODate(dataMan.extractDateModified(schema, md));
        } catch (Exception ex) {
            log.error("WebDavHarvester - addMetadata - Can't get metadata modified date for metadata uuid= " + uuid +
                ", using current date for modified date");
            // WAF harvester, rf.getChangeDate() returns null
            if (rf.getChangeDate() != null) {
                date = rf.getChangeDate();
            }
        }

        if (date == null) {
            date = new ISODate();
        }

        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setChangeDate(date).
            setCreateDate(date).
            setType(MetadataType.METADATA);
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(getOwner());
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid()).
            setUri(rf.getPath());
        addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        try {
            metadata.getSourceInfo().setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        } catch (NumberFormatException e) {
        }

        metadata = metadataManager.insertMetadata(context, metadata, md, IndexingMode.none, false, UpdateDatestamp.NO, false, false);
        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataManager.flush();

        dataMan.indexMetadata(id, true);
        result.addedMetadata++;
    }

    private Element retrieveMetadata(RemoteFile rf) {
        try {
            if (log.isDebugEnabled()) log.debug("Getting remote file : " + rf.getPath());
            Element md = rf.getMetadata(schemaMan);
            if (log.isDebugEnabled()) {
                log.debug("Record got:\n" + Xml.getString(md));
            }
            // check that it is a known schema
            dataMan.autodetectSchema(md);

            try {
                Integer groupIdVal = null;
                if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                    groupIdVal = getGroupOwner();
                }

                params.getValidate().validate(dataMan, context, md, groupIdVal);
                return (Element) md.detach();
            } catch (Exception e) {
                log.info("Skipping metadata that does not validate. Path is : " + rf.getPath());
                result.doesNotValidate++;
            }
        } catch (NoSchemaMatchesException e) {
            log.warning("Skipping metadata with unknown schema. Path is : " + rf.getPath());
            result.unknownSchema++;
        } catch (JDOMException e) {
            log.warning("Skipping metadata with bad XML format. Path is : " + rf.getPath());
            result.badFormat++;
        } catch (Exception e) {
            log.warning("Raised exception while getting metadata file : " + e);
            result.unretrievable++;
        }
        //--- we don't raise any exception here. Just try to go on
        return null;
    }

    /**
     * Updates the record on the database. The force parameter allows you to force an update even
     * if the date is not more updated, to make sure transformation and attributes assigned by the
     * harvester are applied. Also, it changes the ownership of the record so it is assigned to the
     * new harvester that last updated it.
        * @param rf
        * @param recordInfo
        * @param force
        * @throws Exception
     */
    private void updateMetadata(RemoteFile rf, RecordInfo recordInfo, boolean force) throws Exception {
        Element md = null;

        // Get the change date from the metadata content. If not possible, get it from the file change date if available
        // and if not possible use current date
        String date = null;
        //--- set uuid inside metadata (on metadata add it's created a new uuid ignoring fileIdentifier uuid).
        //--- In update we should use db uuid to update the xml uuid and keep in sych both.
        String schema = null;

        if (rf instanceof WAFRemoteFile) {
            md = retrieveMetadata(rf);

            if (md == null) {
                return;
            }

            try {
                schema = dataMan.autodetectSchema(md);

                //Update only if different
                String uuid = dataMan.extractUUID(schema, md);
                if (!recordInfo.uuid.equals(uuid)) {
                    md = dataMan.setUUID(schema, recordInfo.uuid, md);
                }
            } catch (Exception e) {
                log.error("  - Failed to set uuid for metadata with remote path : " + rf.getPath());
                return;
            }


            try {
                date = dataMan.extractDateModified(schema, md);
            } catch (Exception ex) {
                log.error("WebDavHarvester - updateMetadata - Can't get metadata modified date for metadata id= "
                    + recordInfo.id + ", using current date for modified date");
                // WAF harvester, rf.getChangeDate() returns null
                if (rf.getChangeDate() != null) {
                    date = rf.getChangeDate().getDateAndTime();
                }
            }
            ((WAFRemoteFile) rf).setChangeDate(date);
        }


        if (!force && !rf.isMoreRecentThan(recordInfo.changeDate)) {
            if (log.isDebugEnabled())
                log.debug("  - Metadata XML not changed for path : " + rf.getPath());
            result.unchangedMetadata++;
        } else {
            if (log.isDebugEnabled())
                log.debug("  - Updating local metadata for path : " + rf.getPath());

            if (!(rf instanceof WAFRemoteFile)) {
                md = retrieveMetadata(rf);

                if (md == null) {
                    return;
                }

                try {
                    schema = dataMan.autodetectSchema(md);

                    //Update only if different
                    String uuid = dataMan.extractUUID(schema, md);
                    if (!recordInfo.uuid.equals(uuid)) {
                        md = dataMan.setUUID(schema, recordInfo.uuid, md);
                    }
                } catch (Exception e) {
                    log.error("  - Failed to set uuid for metadata with remote path : " + rf.getPath());
                    return;
                }


                try {
                    date = dataMan.extractDateModified(schema, md);
                } catch (Exception ex) {
                    log.error("WebDavHarvester - updateMetadata - Can't get metadata modified date for metadata id= "
                        + recordInfo.id + ", using current date for modified date");
                    // WAF harvester, rf.getChangeDate() returns null
                    if (rf.getChangeDate() != null) {
                        date = rf.getChangeDate().getDateAndTime();
                    }
                }
            }

            // Translate metadata
            if (params.isTranslateContent()) {
                md = translateMetadataContent(context, md, schema);
            }

            if (StringUtils.isNotEmpty(params.xslfilter)) {
                md = HarvesterUtil.processMetadata(dataMan.getSchema(schema),
                    md, processName, processParams);
            }

            //
            // update metadata
            //
            boolean validate = false;
            boolean ufo = false;
            String language = context.getLanguage();

            final AbstractMetadata metadata = metadataManager.updateMetadata(context, recordInfo.id, md, validate, ufo, language,
                date, false, IndexingMode.none);

            if(force) {
                //change ownership of metadata to new harvester
                metadata.getHarvestInfo().setUuid(params.getUuid());
                metadata.getSourceInfo().setSourceId(params.getUuid());

                context.getBean(IMetadataManager.class).save(metadata);
            }

            //--- the administrator could change privileges and categories using the
            //--- web interface so we have to re-set both
            OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
            repository.deleteAllByMetadataId(Integer.parseInt(recordInfo.id));
            addPrivileges(recordInfo.id, params.getPrivileges(), localGroups, context);

            metadata.getCategories().clear();
            addCategories(metadata, params.getCategories(), localCateg, context, null, true);

            dataMan.flush();

            dataMan.indexMetadata(recordInfo.id, true);
        }
    }
}

//=============================================================================
