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

package org.fao.geonet.kernel.harvest.harvester.csw;

import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.OperationAllowedId_;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.HarvesterUtil;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.utils.AbstractHttpRequest.Method.GET;
import static org.fao.geonet.utils.AbstractHttpRequest.Method.POST;

//=============================================================================

public class Aligner extends BaseAligner {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    private Logger log;

    //--------------------------------------------------------------------------
    //---
    //--- Alignment method
    //---
    //--------------------------------------------------------------------------
    private ServiceContext context;

    //--------------------------------------------------------------------------
    //---
    //--- Private methods : addMetadata
    //---
    //--------------------------------------------------------------------------
    private CswParams params;
    private DataManager dataMan;

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------
    private CategoryMapper localCateg;

    //--------------------------------------------------------------------------
    private GroupMapper localGroups;
    private UUIDMapper localUuids;

    //--------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //--------------------------------------------------------------------------
    private HarvestResult result;
    private GetRecordByIdRequest request;
    private String processName;
    private Map<String, Object> processParams = new HashMap<String, Object>();
    public Aligner(AtomicBoolean cancelMonitor, Logger log, ServiceContext sc, CswServer server, CswParams params) throws OperationAbortedEx {
        super(cancelMonitor);
        this.log = log;
        this.context = sc;
        this.params = params;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        dataMan = gc.getBean(DataManager.class);
        result = new HarvestResult();

        //--- setup get-record-by-id request

        request = new GetRecordByIdRequest(sc);
        request.setElementSetName(ElementSetName.FULL);

        CswOperation oper = server.getOperation(CswServer.GET_RECORD_BY_ID);

        // Use the preferred HTTP method and check one exist.
        if (oper.getGetUrl() != null && Harvester.PREFERRED_HTTP_METHOD.equals("GET")) {
            request.setUrl(context, oper.getGetUrl());
            request.setMethod(GET);
        } else if (oper.getPostUrl() != null && Harvester.PREFERRED_HTTP_METHOD.equals("POST")) {
            request.setUrl(context, oper.getPostUrl());
            request.setMethod(POST);
        } else {
            if (oper.getGetUrl() != null) {
                request.setUrl(context, oper.getGetUrl());
                request.setMethod(GET);
            } else if (oper.getPostUrl() != null) {
                request.setUrl(context, oper.getPostUrl());
                request.setMethod(POST);
            } else {
                throw new OperationAbortedEx("No GET or POST DCP available in this service.");
            }
        }

        if (this.params.outputSchema != null && !this.params.outputSchema.isEmpty()) {
            request.setOutputSchema(this.params.outputSchema);
        } else if (oper.getPreferredOutputSchema() != null) {
            request.setOutputSchema(oper.getPreferredOutputSchema());
        }

        if (oper.getPreferredServerVersion() != null) {
            request.setServerVersion(oper.getPreferredServerVersion());
        }

        if (params.isUseAccount()) {
            request.setCredentials(params.getUsername(), params.getPassword());
        }

    }

    public HarvestResult align(Set<RecordInfo> records, List<HarvestError> errors) throws Exception {
        if (cancelMonitor.get()) {
            return result;
        }

        log.info("Start of alignment for : " + params.getName());

        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(MetadataRepository.class), params.getUuid());

        dataMan.flush();

        Pair<String, Map<String, Object>> filter =
            HarvesterUtil.parseXSLFilter(params.xslfilter, log);
        processName = filter.one();
        processParams = filter.two();

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
            }catch(Throwable t) {
                errors.add(new HarvestError(context, t, log));
                log.error("Unable to process record from csw (" + this.params.getName() + ")");
                log.error("   Record failed: " + ri.uuid + ". Error is: " + t.getMessage());
            } finally {
                result.originalMetadata++;
            }
        }
        dataMan.forceIndexChanges();

        log.info("End of alignment for : " + params.getName());

        return result;
    }

    private void addMetadata(RecordInfo ri) throws Exception {
        if (cancelMonitor.get()) {
            return;
        }

        Element md = retrieveMetadata(ri.uuid);

        if (md == null) {
            return;
        }

        String schema = dataMan.autodetectSchema(md, null);

        if (schema == null) {
            if (log.isDebugEnabled()) {
                log.debug("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid);
            }
            result.unknownSchema++;

            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("  - Adding metadata with remote uuid:" + ri.uuid + " schema:" + schema);
        }

        if (!params.xslfilter.equals("")) {
            md = processMetadata(context,
                md, processName, processParams, log);
        }
        //
        // insert metadata
        //
        final int ownerId;
        if (params.getOwnerId() == null) {
            if (context.getUserSession() != null) {
                ownerId = context.getUserSession().getUserIdAsInt();
            } else {
                ownerId = 1;
            }
        } else {
            ownerId = Integer.parseInt(params.getOwnerId());
        }
        Metadata metadata = new Metadata().setUuid(ri.uuid);
        metadata.getDataInfo().
            setSchemaId(schema).
            setRoot(md.getQualifiedName()).
            setType(MetadataType.METADATA).
            setChangeDate(new ISODate(ri.changeDate)).
            setCreateDate(new ISODate(ri.changeDate));
        metadata.getSourceInfo().
            setSourceId(params.getUuid()).
            setOwner(ownerId).
            setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
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

    //--------------------------------------------------------------------------
    //---
    //--- Private methods : updateMetadata
    //---
    //--------------------------------------------------------------------------
    private void updateMetadata(RecordInfo ri, String id) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);

        if (date == null) {
            if (log.isDebugEnabled()) {
                log.debug("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
            }
        } else {
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

                if (md == null) {
                    return;
                }
                String schema = dataMan.autodetectSchema(md, null);
                if (!params.xslfilter.equals("")) {
                    md = processMetadata(context,
                        md, processName, processParams, log);
                }

                //
                // update metadata
                //
                boolean validate = false;
                boolean ufo = false;
                boolean index = false;
                String language = context.getLanguage();
                final Metadata metadata = dataMan.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate, true);

                OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                repository.deleteAllByIdAttribute(OperationAllowedId_.metadataId, Integer.parseInt(id));

                addPrivileges(id, params.getPrivileges(), localGroups, dataMan, context, log);

                metadata.getMetadataCategories().clear();
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
        for (RecordInfo ri : records)
            if (uuid.equals(ri.uuid))
                return true;

        return false;
    }

    /**
     * Does CSW GetRecordById request. If validation is requested and the metadata does not
     * validate, null is returned.
     *
     * @param uuid uuid of metadata to request
     * @return metadata the metadata
     */
    private Element retrieveMetadata(String uuid) {
        request.clearIds();
        request.addId(uuid);

        try {
            if (log.isDebugEnabled()) {
                log.debug("Getting record from : " + request.getHost() + " (uuid:" + uuid + ")");
            }
            Element response = request.execute();
            if (log.isDebugEnabled()) {
                log.debug("Record got:\n" + Xml.getString(response));
            }

            @SuppressWarnings("unchecked")
            List<Element> list = response.getChildren();

            //--- maybe the metadata has been removed

            if (list.size() == 0) {
                return null;
            }

            response = list.get(0);
            response = (Element) response.detach();


            try {
                params.getValidate().validate(dataMan, context, response);
            } catch (Exception e) {
                log.info("Ignoring invalid metadata with uuid " + uuid);
                result.doesNotValidate++;
                return null;
            }

            if (params.rejectDuplicateResource) {
                if (foundDuplicateForResource(uuid, response)) {
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

    /**
     * Check for metadata in the catalog having the same resource identifier as the harvested
     * record.
     *
     * If one dataset (same MD_metadata/../identificationInfo/../identifier/../code) (eg. a NMA
     * layer for roads) is described in 2 or more catalogs with different metadata uuids. The
     * metadata may be slightly different depending on the author, but the resource is the same.
     * When harvesting, some users would like to have the capability to exclude "duplicate"
     * description of the same dataset.
     *
     * The check is made searching the identifier field in the index using {@link
     * org.fao.geonet.kernel.search.LuceneSearcher#getAllMetadataFromIndexFor(String, String,
     * String, java.util.Set, boolean)}
     *
     * @param uuid     the metadata unique identifier
     * @param response the XML document to check
     * @return true if a record with same resource identifier is found. false otherwise.
     */
    private boolean foundDuplicateForResource(String uuid, Element response) {
        String schema = dataMan.autodetectSchema(response);

        if (schema != null && schema.startsWith("iso19139")) {
            String resourceIdentifierXPath = "gmd:identificationInfo/*/gmd:citation/gmd:CI_Citation/gmd:identifier/*/gmd:code/gco:CharacterString";
            String resourceIdentifierLuceneIndexField = "identifier";
            String defaultLanguage = "eng";

            try {
                // Extract resource identifier
                XPath xp = XPath.newInstance(resourceIdentifierXPath);
                xp.addNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                xp.addNamespace("gco", "http://www.isotc211.org/2005/gco");
                @SuppressWarnings("unchecked")
                List<Element> resourceIdentifiers = xp.selectNodes(response);
                if (resourceIdentifiers.size() > 0) {
                    // Check if the metadata to import has a resource identifier
                    // existing in current catalog for a record with a different UUID

                    log.debug("  - Resource identifiers found : " + resourceIdentifiers.size());

                    for (Element identifierNode : resourceIdentifiers) {
                        String identifier = identifierNode.getTextTrim();
                        log.debug("    - Searching for duplicates for resource identifier: " + identifier);

                        Map<String, Map<String, String>> values = LuceneSearcher.getAllMetadataFromIndexFor(defaultLanguage, resourceIdentifierLuceneIndexField,
                            identifier, Collections.singleton("_uuid"), true);
                        log.debug("    - Number of resources with same identifier: " + values.size());
                        for (Map<String, String> recordFieldValues : values.values()) {
                            String indexRecordUuid = recordFieldValues.get("_uuid");
                            if (!indexRecordUuid.equals(uuid)) {
                                log.debug("      - UUID " + indexRecordUuid + " in index does not match harvested record UUID " + uuid);
                                log.warning("      - Duplicates found. Skipping record with UUID " + uuid + " and resource identifier " + identifier);

                                result.duplicatedResource++;
                                return true;
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                log.warning("      - Error when searching for resource duplicate " + uuid + ". Error is: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *
     * Filter the metadata if process parameter is set and corresponding XSL transformation
     * exists in xsl/conversion/import.
     *
     * @param context
     * @param md
     * @param processName
     * @param processParams
     * @param log
     * @return
     */
    private Element processMetadata(ServiceContext context,
                                    Element md,
                                    String processName,
                                    Map<String, Object> processParams,
                                    Logger log) {
        Path filePath = context.getAppPath().resolve(Geonet.Path.STYLESHEETS).resolve("conversion/import").resolve(processName + ".xsl");
        if (!Files.exists(filePath)) {
            log.info("     processing instruction  " + processName + ". Metadata not filtered.");
        } else {
            Element processedMetadata;
            try {
                processedMetadata = Xml.transform(md, filePath, processParams);
                if (log.isDebugEnabled()) log.debug("     metadata filtered.");
                md = processedMetadata;
            } catch (Exception e) {
                log.warning("     processing error (" + processName + "): "
                        + e.getMessage());
            }
        }
        return md;
    }
}
