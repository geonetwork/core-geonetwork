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

import com.fasterxml.jackson.databind.ObjectMapper;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.csw.common.CswOperation;
import org.fao.geonet.csw.common.CswServer;
import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.requests.GetRecordByIdRequest;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.Pair;
import org.fao.geonet.exceptions.OperationAbortedEx;
import org.fao.geonet.kernel.AddElemValue;
import org.fao.geonet.kernel.BatchEditParameter;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.EditLib;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.datamanager.IMetadataIndexer;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestError;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.harvest.harvester.HarvesterUtil;
import org.fao.geonet.kernel.harvest.harvester.RecordInfo;
import org.fao.geonet.kernel.harvest.harvester.UUIDMapper;
import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.kernel.search.index.LuceneIndexLanguageTracker;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.fao.geonet.kernel.setting.Settings.SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS;
import static org.fao.geonet.utils.AbstractHttpRequest.Method.GET;
import static org.fao.geonet.utils.AbstractHttpRequest.Method.POST;


public class Aligner extends BaseAligner<CswParams> {

    private ServiceContext context;
    private DataManager dataMan;
    private CategoryMapper localCateg;
    private GroupMapper localGroups;
    private UUIDMapper localUuids;

    private IMetadataUtils metadataUtils;
    private IMetadataManager metadataManager;
    private IMetadataIndexer metadataIndexer;

    private HarvestResult result;
    private GetRecordByIdRequest request;
    private String processName;
    private Map<String, Object> processParams = new HashMap<String, Object>();
    private Logger log;

    public Aligner(AtomicBoolean cancelMonitor, ServiceContext sc, CswServer server, CswParams params, Logger log) throws OperationAbortedEx {
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
        result.xpathFilterExcluded = 0;

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

    public HarvestResult align(Collection<RecordInfo> records, Collection<HarvestError> errors) throws Exception {
        if (cancelMonitor.get()) {
            return result;
        }

        log.debug("Start of alignment for : " + params.getName());

        //-----------------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node

        localCateg = new CategoryMapper(context);
        localGroups = new GroupMapper(context);
        localUuids = new UUIDMapper(context.getBean(IMetadataUtils.class), params.getUuid());

        Pair<String, Map<String, Object>> filter = HarvesterUtil.parseXSLFilter(params.xslfilter);
        processName = filter.one();
        processParams = filter.two();

        insertOrUpdate(records, errors);
        log.debug("End of alignment for : " + params.getName());

        return result;
    }

    private void insertOrUpdate(Collection<RecordInfo> records, Collection<HarvestError> errors) {
        for (RecordInfo ri : records) {

            if (cancelMonitor.get()) {
                return;
            }
            try {
                String id = metadataUtils.getMetadataId(ri.uuid);

                if (id == null) {
                    //record doesn't exist (so it doesn't belong to this harvester)
                    log.debug("Adding record with uuid " + ri.uuid);
                    addMetadata(ri, ri.uuid);
                } else if (localUuids.getID(ri.uuid) == null) {
                    //Record does not belong to this harvester
                    result.datasetUuidExist++;

                    switch (params.getOverrideUuid()) {
                        case OVERRIDE:
                            updateMetadata(ri, Integer.toString(metadataUtils.findOneByUuid(ri.uuid).getId()), true);
                            log.debug("Overriding record with uuid " + ri.uuid);

                            if (params.isIfRecordExistAppendPrivileges()) {
                                addPrivileges(id, params.getPrivileges(), localGroups, context);
                                result.privilegesAppendedOnExistingRecord++;
                            }
                            break;
                        case RANDOM:
                            log.debug("Generating random uuid for remote record with uuid " + ri.uuid);
                            addMetadata(ri, UUID.randomUUID().toString());
                            break;
                        case SKIP:
                            log.debug("Skipping record with uuid " + ri.uuid);
                            result.uuidSkipped++;
                        default:
                            break;
                    }
                } else {
                    //record exists and belongs to this harvester
                    updateMetadata(ri, id, false);

                    if (params.isIfRecordExistAppendPrivileges()) {
                        addPrivileges(id, params.getPrivileges(), localGroups, context);
                        result.privilegesAppendedOnExistingRecord++;
                    }
                }

                context.getBean(LuceneIndexLanguageTracker.class).commit();

                result.totalMetadata++;
            } catch (Throwable t) {
                errors.add(new HarvestError(this.context, t));
                log.error("Unable to process record from csw (" + this.params.getName() + ")");
                log.error("   Record failed: " + ri.uuid + ". Error is: " + t.getMessage());
                log.error(t);
            } finally {
                result.originalMetadata++;
            }
        }
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
                result.locallyRemoved++;
            }
        }
        dataMan.forceIndexChanges();

        return result;
    }


    private void addMetadata(RecordInfo ri, String uuid) throws Exception {
        if (cancelMonitor.get()) {
            return;
        }

        Element md = retrieveMetadata(ri.uuid);

        if (md == null) {
            result.unretrievable++;
            return;
        }

        String schema = dataMan.autodetectSchema(md, null);
        if (schema == null) {
            log.debug("  - Metadata skipped due to unknown schema. uuid:" + ri.uuid);
            result.unknownSchema++;
            return;
        }

        if (StringUtils.isNotEmpty(params.xpathFilter)) {
            Object xpathResult = Xml.selectSingle(md, params.xpathFilter, new ArrayList<Namespace>(dataMan.getSchema(schema).getNamespaces()));
            boolean match = xpathResult instanceof Boolean && ((Boolean) xpathResult).booleanValue();
            if(!match) {
                result.xpathFilterExcluded ++;
                return;
            }
        }

        log.debug("  - Adding metadata with remote uuid:" + ri.uuid + " schema:" + schema);

        String mdUuid = ri.uuid;
        if (!params.xslfilter.equals("")) {
            md = processMetadata(context, md, processName, processParams);
            // Get new uuid if modified by XSLT process
            mdUuid = metadataUtils.extractUUID(schema, md);
            if (mdUuid == null) {
                mdUuid = ri.uuid;
            }
        }

        if (StringUtils.isNotEmpty(params.getBatchEdits())) {
            SchemaManager _schemaManager = context.getBean(SchemaManager.class);
            EditLib editLib = new EditLib(_schemaManager);
            ObjectMapper mapper = new ObjectMapper();

            BatchEditParameter[] listOfUpdates = mapper.readValue(params.getBatchEdits(), BatchEditParameter[].class);
            if (listOfUpdates.length > 0) {
                boolean metadataChanged = false;
                boolean createXpathNodeIfNotExists =
                    context.getBean(SettingManager.class).getValueAsBool(SYSTEM_CSW_TRANSACTION_XPATH_UPDATE_CREATE_NEW_ELEMENTS);
                MetadataSchema metadataSchema = _schemaManager.getSchema(schema);

                Iterator<BatchEditParameter> listOfUpdatesIterator =
                    Arrays.asList(listOfUpdates).iterator();
                while (listOfUpdatesIterator.hasNext()) {
                    BatchEditParameter batchEditParameter =
                        listOfUpdatesIterator.next();

                    AddElemValue propertyValue =
                        new AddElemValue(batchEditParameter.getValue());

                    boolean applyEdit = true;
                    if (StringUtils.isNotEmpty(batchEditParameter.getCondition())) {
                        final Object node = Xml.selectSingle(md, batchEditParameter.getCondition(), metadataSchema.getNamespaces());
                        applyEdit = (node != null) || (node instanceof Boolean && (Boolean)node != false);
                    }
                    if (applyEdit) {
                        metadataChanged = editLib.addElementOrFragmentFromXpath(
                            md,
                            metadataSchema,
                            batchEditParameter.getXpath(),
                            propertyValue,
                            createXpathNodeIfNotExists
                        ) || metadataChanged;
                    }
                }
                if (metadataChanged) {
                    log.debug("  - Record updated by batch edit configuration:" + ri.uuid);
                }
            }
        }
        //
        // insert metadata
        //
        AbstractMetadata metadata = new Metadata();
        metadata.setUuid(uuid);
        if (!uuid.equals(ri.uuid)) {
            md = metadataUtils.setUUID(schema, uuid, md);
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

        metadata = metadataManager.insertMetadata(context, metadata, md, true, false, false, UpdateDatestamp.NO, false, false);

        String id = String.valueOf(metadata.getId());

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadataIndexer.indexMetadata(id, true, null);
        result.addedMetadata++;
    }

    private void updateMetadata(RecordInfo ri, String id, Boolean force) throws Exception {
        String date = localUuids.getChangeDate(ri.uuid);

        if (date == null && !force) {
            log.debug("  - Skipped metadata managed by another harvesting node. uuid:" + ri.uuid + ", name:" + params.getName());
        } else {
            if (!force && !ri.isMoreRecentThan(date)) {
                log.debug("  - Metadata XML not changed for uuid:" + ri.uuid);
                result.unchangedMetadata++;
            } else {
                log.debug("  - Updating local metadata for uuid:" + ri.uuid);
                if (updatingLocalMetadata(ri, id, force)) {
                    metadataIndexer.indexMetadata(id, true, null);
                    result.updatedMetadata++;
                }
            }
        }
    }
    @Transactional(value = TxType.REQUIRES_NEW)
    private boolean updatingLocalMetadata(RecordInfo ri, String id, Boolean force) throws Exception {
        Element md = retrieveMetadata(ri.uuid);

        if (md == null) {
            result.unchangedMetadata++;
            return false;
        }

        if (!params.xslfilter.equals("")) {
            md = processMetadata(context, md, processName, processParams);
        }

        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        boolean index = false;
        String language = context.getLanguage();

        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, md, validate, ufo, index, language, ri.changeDate, true);

        if (force) {
            //change ownership of metadata to new harvester
            metadata.getHarvestInfo().setUuid(params.getUuid());
            metadata.getSourceInfo().setSourceId(params.getUuid());

            metadataManager.save(metadata);
        }

        addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        addCategories(metadata, params.getCategories(), localCateg, context, null, true);

        return true;
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
            log.debug("Getting record from : " + request.getHost() + " (uuid:" + uuid + ")");

            Element response = request.execute();
            if (log.isDebugEnabled()) {
                log.debug("Record got: " + Xml.getString(response) + "\n");
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
                Integer groupIdVal = null;
                if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                    groupIdVal = Integer.parseInt(params.getOwnerIdGroup());
                }

                params.getValidate().validate(dataMan, context, response, groupIdVal);
            } catch (Exception e) {
                log.debug("Ignoring invalid metadata with uuid " + uuid);
                result.doesNotValidate++;
                return null;
            }

            if (params.rejectDuplicateResource) {
                if (foundDuplicateForResource(uuid, response)) {
                    result.unchangedMetadata++;
                    return null;
                }
            }

            return response;
        } catch (Exception e) {
            log.error("Raised exception while getting record : " + e);
            log.error(e);
            result.unretrievable++;

            //--- we don't raise any exception here. Just try to go on
            return null;
        }
    }

    /**
     * Check for metadata in the catalog having the same resource identifier as the harvested
     * record.
     * <p>
     * If one dataset (same MD_metadata/../identificationInfo/../identifier/../code) (eg. a NMA
     * layer for roads) is described in 2 or more catalogs with different metadata uuids. The
     * metadata may be slightly different depending on the author, but the resource is the same.
     * When harvesting, some users would like to have the capability to exclude "duplicate"
     * description of the same dataset.
     * <p>
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
            }
        }
        return false;
    }

    /**
     * Filter the metadata if process parameter is set and corresponding XSL transformation
     * exists in xsl/conversion/import.
     *
     * @param context
     * @param md
     * @param processName
     * @param processParams
     * @return
     */
    private Element processMetadata(ServiceContext context,
                                    Element md,
                                    String processName,
                                    Map<String, Object> processParams) {
        Path filePath = context.getAppPath().resolve(Geonet.Path.STYLESHEETS).resolve("conversion/import").resolve(processName + ".xsl");
        if (!Files.exists(filePath)) {
            log.debug("     processing instruction  " + processName + ". Metadata not filtered.");
        } else {
            Element processedMetadata;
            try {
                processedMetadata = Xml.transform(md, filePath, processParams);
                log.debug("     metadata filtered.");
                md = processedMetadata;
            } catch (Exception e) {
                log.warning("     processing error " + processName + "}): " + e.getMessage());
            }
        }
        return md;
    }
}
