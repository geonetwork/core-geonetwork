//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.harvest.harvester.geoPREST;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.GeonetHttpRequestFactory;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.utils.XmlRequest;
import org.jdom.Element;

import jeeves.server.context.ServiceContext;

//=============================================================================

public class Aligner extends BaseAligner<GeoPRESTParams> {

    private Logger log;
    private ServiceContext context;
    private XmlRequest request;
    private DataManager dataMan;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;
    private HarvestResult result;

    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext sc, GeoPRESTParams params) throws Exception {
        super(cancelMonitor);
        this.log = log;
        this.context = sc;
        this.params = params;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        result = new HarvestResult();

        //--- setup REST operation rest/document?id={uuid}

        request = context.getBean(GeonetHttpRequestFactory.class).createXmlRequest(new URL(params.baseUrl + "/rest/document"));

    }

    public HarvestResult align(Set<RecordInfo> records, List<HarvestError> errors) throws Exception {
        log.info("Start of alignment for : " + params.getName());

        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        dataMan.flush();

        //-----------------------------------------------------------------------
        //--- remove old metadata

        for (String uuid : localUuids.getUUIDs()) {
            if (cancelMonitor.get()) {
                return result;
            }

            if (!exists(records, uuid)) {
                String id = localUuids.getID(uuid);

                if (log.isDebugEnabled())
                    log.debug("  - Removing old metadata with local id:" + id);
                dataMan.deleteMetadata(context, id);

                dataMan.flush();

                result.locallyRemoved++;
            }
        }

        //-----------------------------------------------------------------------
        //--- insert/update new metadata

        for (RecordInfo ri : records) {
            if (cancelMonitor.get()) {
                return result;
            }

            try {
                String id = dataMan.getMetadataId(ri.uuid);

                if (id == null) addMetadata(ri);
                else updateMetadata(ri, id);
                result.totalMetadata++;

            }catch (Throwable t) {
                errors.add(new HarvestError(context, t));
                log.error("Unable to process record from csw (" + this.params.getName() + ")");
                log.error("   Record failed: " + ri.uuid);
            }
        }

        dataMan.forceIndexChanges();

        log.info("End of alignment for : " + params.getName());

        return result;
    }

    private void addMetadata(RecordInfo ri) throws Exception {
        Element md = retrieveMetadata(ri.uuid);

        if (md == null) return;

        String schema = dataMan.autodetectSchema(md, null);

        if (schema == null) {
            if (log.isDebugEnabled()) {
                log.debug("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid);
            }
            result.unknownSchema++;
            return;
        }

        if (log.isDebugEnabled())
            log.debug("  - Adding metadata with remote uuid:" + ri.uuid + " schema:" + schema);

        //
        // insert metadata
        //
        int userid = 1;
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(ri.uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA).
            setChangeDate(new ISODate(ri.changeDate)).
            setCreateDate(new ISODate(ri.changeDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(userid);
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        addCategories(metadata, params.getCategories(), localCateg, context, log, null, false);

        metadata = dataMan.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

        dataMan.indexMetadata(id, Math.random() < 0.01, null);
        result.addedMetadata++;
    }

    private void updateMetadata(RecordInfo ri, String id) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);

        if (date == null) {
            if (log.isDebugEnabled()) {
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("  - Comparing date " + date + " with harvested date " + ri.changeDate + " Comparison: " + ri.isMoreRecentThan(date));
            }
            if (!ri.isMoreRecentThan(date)) {
                if (log.isDebugEnabled()) {
                    log.debug("  - Metadata XML not changed for uuid:" + ri.uuid);
                }
                result.unchangedMetadata++;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("  - Updating local metadata for uuid:" + ri.uuid);
                }
                Element md = retrieveMetadata(ri.uuid);

                if (md == null) return;

                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                boolean index = false;
                String language = context.getLanguage();
                final AbstractMetadata metadata = dataMan.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate, false);

                OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));
                addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

                metadata.getCategories().clear();
                addCategories(metadata, params.getCategories(), localCateg, context, log, null, true);
                dataMan.flush();

                dataMan.indexMetadata(id, Math.random() < 0.01, null);
                result.updatedMetadata++;
            }
        }
    }

    /**
     * Returns true if the uuid is present in the remote node.
     */
    private boolean exists(Set<RecordInfo> records, String uuid) {
        for (RecordInfo ri : records) {
            if (uuid.equals(ri.uuid)) return true;
        }

        return false;
    }

    /**
     * Does REST document request. If validation is requested and the metadata does not validate,
     * null is returned. If transformation is requested then metadata is transformed.
     *
     * @param uuid uuid of metadata to request
     * @return metadata the metadata
     */
    private Element retrieveMetadata(String uuid) {
        request.clearParams();
        //request.addParam("id","{"+uuid+"}");
        request.addParam("id", uuid);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting record from : " + request.getHost() + " (uuid:" + uuid + ")");
            }
            Element response = null;
            try {
                response = request.execute();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Getting record from GeoPortal REST raised exception: " + e.getMessage());
                log.error("Sent request " + request.getSentData());
                throw new Exception(e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Record got:\n" + Xml.getString(response));
            }

            try {
                params.getValidate().validate(dataMan, context, response);
            } catch (Exception e) {
                log.info("Ignoring invalid metadata with uuid " + uuid);
                result.doesNotValidate++;
                return null;
            }

            // transform it here if requested
            if (!params.getImportXslt().equals("none")) {
                Path thisXslt = context.getAppPath().resolve(Geonet.Path.IMPORT_STYLESHEETS).
                    resolve(params.getImportXslt());
                try {
                    response = Xml.transform(response, thisXslt);
                } catch (Exception e) {
                    log.info("Cannot transform XML " + Xml.getString(response) + ", ignoring. Error was: " + e.getMessage());
                    result.badFormat++;
                    return null;
                }
            }
            return response;
        } catch (Exception e) {
            log.warning("Raised exception while getting record : " + e);
            e.printStackTrace();
            result.unretrievable++;

            //--- we don't raise any exception here. Just try to go on
            return null;
        }
    }
}

//=============================================================================


