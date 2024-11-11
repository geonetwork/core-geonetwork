//=============================================================================
//===	Copyright (C) 2001-2009 Food and Agriculture Organization of the
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
package org.fao.geonet.kernel.harvest.harvester.arcsde;

import com.google.common.collect.Sets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Logger;
import org.fao.geonet.arcgis.ArcSDEConnection;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.exceptions.NoSchemaMatchesException;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.UpdateDatestamp;
import org.fao.geonet.kernel.harvest.BaseAligner;
import org.fao.geonet.kernel.harvest.harvester.AbstractHarvester;
import org.fao.geonet.kernel.harvest.harvester.CategoryMapper;
import org.fao.geonet.kernel.harvest.harvester.GroupMapper;
import org.fao.geonet.kernel.harvest.harvester.HarvestResult;
import org.fao.geonet.kernel.search.IndexingMode;
import org.fao.geonet.kernel.search.submission.DirectIndexSubmittor;
import org.fao.geonet.kernel.search.submission.batch.BatchingDeletionSubmittor;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.schema.iso19139.ISO19139Namespaces;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.data.jpa.domain.Specification;

import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Harvester from ArcSDE. Requires the propietary ESRI libraries containing their API. Since those
 * are not committed to our CVS, you'll need to replace the dummy library arcsde-dummy.jar with the
 * real ones for this to work.
 *
 * @author heikki doeleman
 */
public class ArcSDEHarvester extends AbstractHarvester<HarvestResult, ArcSDEParams> {

    private static final String ARC_TO_ISO19115_TRANSFORMER = "ArcCatalog8_to_ISO19115.xsl";
    private static final String ISO19115_TO_ISO19139_TRANSFORMER = "fromISO19115.xsl";

    @Override
    protected void storeNodeExtra(ArcSDEParams params, String path, String siteId, String optionsId) throws SQLException {
        setParams(params);
        harvesterSettingsManager.add("id:" + siteId, "icon", params.icon);
        harvesterSettingsManager.add("id:" + siteId, "server", params.server);
        harvesterSettingsManager.add("id:" + siteId, "port", params.port);
        harvesterSettingsManager.add("id:" + siteId, "username", params.getUsername());
        harvesterSettingsManager.add("id:" + siteId, "password", params.getPassword());
        harvesterSettingsManager.add("id:" + siteId, "database", params.database);
        harvesterSettingsManager.add("id:" + siteId, "version", params.version);
        harvesterSettingsManager.add("id:" + siteId, "connectionType", params.connectionType);
        harvesterSettingsManager.add("id:" + siteId, "databaseType", params.databaseType);
    }

    @Override
    protected ArcSDEParams createParams() {
        return new ArcSDEParams(dataMan);
    }

    @Override
    public Element getResult() {
        Element res = new Element("result");

        if (result != null) {
            add(res, "total", result.totalMetadata);
            add(res, "added", result.addedMetadata);
            add(res, "updated", result.updatedMetadata);
            add(res, "unchanged", result.unchangedMetadata);
            add(res, "unknownSchema", result.unknownSchema);
            add(res, "removed", result.locallyRemoved);
            add(res, "unretrievable", result.unretrievable);
            add(res, "badFormat", result.badFormat);
            add(res, "doesNotValidate", result.doesNotValidate);
        }

        return res;
    }

    @Override
    public void doHarvest(Logger l) throws Exception {
        log.info("ArcSDE harvest starting");

        ArcSDEConnectionFactory connectionFactory = context.getBean(ArcSDEConnectionFactory.class);

         ArcSDEConnection connection = connectionFactory.getConnection(
                 params.connectionType, params.databaseType, params.server, params.port,
                 params.database, params.getUsername(), params.getPassword());
        Map<String, String> metadataList = connection.retrieveMetadata(cancelMonitor, params.version);
        align(metadataList);

        log.info("ArcSDE harvest finished");
    }

    private void align(Map<String, String> metadataList) throws Exception {
        log.info("Start of alignment for : " + params.getName());

        result = new HarvestResult();
        //----------------------------------------------------------------
        //--- retrieve all local categories and groups
        //--- retrieve harvested uuids for given harvesting node
        CategoryMapper localCateg = new CategoryMapper(context);
        GroupMapper localGroups = new GroupMapper(context);

        metadataManager.flush();

        GeonetworkDataDirectory dataDirectory = ApplicationContextHolder.get().getBean(GeonetworkDataDirectory.class);
        Path ArcToISO19115Transformer =
            dataDirectory.getXsltConversion("schema:iso19139:convert/" + ARC_TO_ISO19115_TRANSFORMER);
        Path ISO19115ToISO19139Transformer =
            dataDirectory.getXsltConversion("schema:iso19139:convert/" + ISO19115_TO_ISO19139_TRANSFORMER);


        List<Integer> idsForHarvestingResult = new ArrayList<Integer>();
        //-----------------------------------------------------------------------
        //--- insert/update metadata
        for (Map.Entry<String, String> entry : metadataList.entrySet()) {
            log.info("Processing UUID: " + entry.getKey());

            String uuid =  entry.getKey();
            String metadata =  entry.getValue();

            if (cancelMonitor.get()) {
                return;
            }

            try {
                result.totalMetadata++;

                if (StringUtils.isEmpty(uuid)) {
                    log.info("Processing empty UUID. Skipping");
                    continue;
                }

                if (StringUtils.isEmpty(metadata)) {
                    log.info("Processing empty metadata xml for UUID: " + uuid + ". Skipping");
                    continue;
                }

                String thumbnailContent = "";

                // create JDOM element from String-XML
                Element metadataElement = Xml.loadString(metadata, false);

                String schema = null;

                try {
                    schema = dataMan.autodetectSchema(metadataElement, null);
                } catch (NoSchemaMatchesException ex) {
                    // Ignore
                }

                List<Namespace> esriMdNamespaces = new ArrayList<>();
                esriMdNamespaces.add(metadataElement.getNamespace());
                esriMdNamespaces.addAll(metadataElement.getAdditionalNamespaces());

                esriMdNamespaces.add(ISO19139Namespaces.GMD);

                // select all nodes that match the XPath
                Element iso19139Element = Xml.selectElement(metadataElement, "gmd:MD_Metadata", esriMdNamespaces);

                // Check if the ESRI metadata has an embedded iso19139 metadata
                boolean hasIso19139Embedded = false;
                if (schema == null) {

                    if (iso19139Element != null) {
                        try {
                            schema = dataMan.autodetectSchema(iso19139Element, null);
                        } catch (NoSchemaMatchesException ex) {
                            // Ignore
                        }

                        hasIso19139Embedded = (schema != null);

                        log.info("Metadata has ISO13139 embedded - " + hasIso19139Embedded);
                    }
                }

                // No schema detected, try to convert from default ESRI md to ISO1939
                if (schema == null) {
                    log.info("Convert metadata to ISO19139 - start");

                    // Extract picture if available
                    // select all nodes that match the XPath
                    Element thumbnailEl = Xml.selectElement(metadataElement, "Binary/Thumbnail/Data", esriMdNamespaces);

                    if (thumbnailEl != null) {
                        thumbnailContent = thumbnailEl.getText();
                    }

                        // transform ESRI output to ISO19115
                        Element iso19115 = Xml.transform(metadataElement, ArcToISO19115Transformer);

                        // transform ISO19115 to ISO19139
                        metadataElement = Xml.transform(iso19115, ISO19115ToISO19139Transformer);

                    log.info("Convert metadata to ISO19139 - end");

                    try {
                        schema = dataMan.autodetectSchema(metadataElement, null);
                    } catch (NoSchemaMatchesException ex) {
                        // Ignore
                    }

                } else {
                    if (hasIso19139Embedded) {
                        metadataElement = iso19139Element;
                    }
                }

                if (schema == null) {
                    log.info("Skipping metadata with unknown schema.");
                    result.unknownSchema++;
                } else {
                    log.info("Metadata schema: " + schema);
                    log.info("Assigning metadata uuid: " + uuid);

                    metadataElement = dataMan.setUUID(schema, uuid, metadataElement);

                    // the xml is recognizable  format
                    //String uuid = dataMan.extractUUID(schema, metadataElement);

                    if (StringUtils.isEmpty(uuid)) {
                        log.info("No metadata uuid. Skipping.");
                        result.badFormat++;

                    } else {

                        try {
                            Integer groupIdVal = null;
                            if (StringUtils.isNotEmpty(params.getOwnerIdGroup())) {
                                groupIdVal = Integer.parseInt(params.getOwnerIdGroup());
                            }

                            params.getValidate().validate(dataMan, context, metadataElement, groupIdVal);
                        } catch (Exception e) {
                            log.error("Ignoring invalid metadata with uuid " + uuid);
                            result.doesNotValidate++;
                            continue;
                        }

                        try (BaseAligner<ArcSDEParams> aligner = new BaseAligner<>(cancelMonitor) {}) {
                            aligner.setParams(params);
                            //
                            // add / update the metadata from this harvesting result
                            //
                            String id = dataMan.getMetadataId(uuid);
                            if (id == null) {
                                id = addMetadata(metadataElement, uuid, schema, localGroups, localCateg, aligner);
                                result.addedMetadata++;
                            } else {
                                updateMetadata(metadataElement, id, localGroups, localCateg, aligner);
                                result.updatedMetadata++;
                            }
                            if (StringUtils.isNotEmpty(thumbnailContent)) {
                                loadMetadataThumbnail(thumbnailContent, id, uuid);
                            }

                            idsForHarvestingResult.add(Integer.valueOf(id));
                        }
                    }
                }
            }catch(Throwable t) {
                log.error("Unable to process record from arcsde (" + this.params.getName() + ")");
                log.error("   Record failed. Error is: " + t.getMessage());
                log.error(t);
            } finally {
                result.originalMetadata++;
            }

        }
        //
        // delete locally existing metadata from the same source if they were
        // not in this harvesting result
        //
        Set<Integer> idsResultHs = Sets.newHashSet(idsForHarvestingResult);
        List<Integer> existingMetadata = context.getBean(MetadataRepository.class).findIdsBy((Specification<Metadata>) MetadataSpecs.hasHarvesterUuid(params.getUuid()));
        try (BatchingDeletionSubmittor submittor = new BatchingDeletionSubmittor()) {
            for (Integer existingId : existingMetadata) {

                if (cancelMonitor.get()) {
                    return;
                }
                if (!idsResultHs.contains(existingId)) {
                    log.debug("  Removing: " + existingId);
                    metadataManager.deleteMetadata(context, existingId.toString(), submittor);
                    result.locallyRemoved++;
                }
            }
        }
    }

    private void updateMetadata(Element xml, String id, GroupMapper localGroups, final CategoryMapper localCateg, BaseAligner<ArcSDEParams> aligner) throws Exception {
        log.info("Updating metadata with id: " + id);

        //
        // update metadata
        //
        boolean validate = false;
        boolean ufo = false;
        String language = context.getLanguage();

        String changeDate = null;
        try {
            String schema = dataMan.autodetectSchema(xml);
            changeDate = dataMan.extractDateModified(schema, xml);
        } catch (Exception ex) {
            log.error("ArcSDEHarverter - updateMetadata - can't get metadata modified date for metadata id= " + id +
                ", using current date for modified date");
            changeDate = new ISODate().toString();
        }

        final AbstractMetadata metadata = metadataManager.updateMetadata(context, id, xml, validate, ufo, language, changeDate,
            true, IndexingMode.none);

        OperationAllowedRepository operationAllowedRepository = context.getBean(OperationAllowedRepository.class);
        operationAllowedRepository.deleteAllByMetadataId(Integer.parseInt(id));
        aligner.addPrivileges(id, params.getPrivileges(), localGroups, context);

        metadata.getCategories().clear();
        aligner.addCategories(metadata, params.getCategories(), localCateg, context, null, true);

        metadataManager.flush();

        dataMan.indexMetadata(id, DirectIndexSubmittor.INSTANCE);
    }

    /**
     * Inserts a metadata into the database. Lucene index is updated after insertion.
     */
    private String addMetadata(Element xml, String uuid, String schema, GroupMapper localGroups, final CategoryMapper localCateg,
                               BaseAligner<ArcSDEParams> aligner) throws Exception {
        log.info("  - Adding metadata with remote uuid: " + uuid);

        //
        // insert metadata
        //
        ISODate createDate = null;
        try {
            createDate = new ISODate(dataMan.extractDateModified(schema, xml));
        } catch (Exception ex) {
            log.error("ArcSDEHarverter - addMetadata - can't get metadata modified date for metadata with uuid= " +
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
            setOwner(Integer.parseInt(params.getOwnerId()));
        metadata.getHarvestInfo().
            setHarvested(true).
            setUuid(params.getUuid());

        try {
            metadata.getSourceInfo().setGroupOwner(Integer.valueOf(params.getOwnerIdGroup()));
        } catch (NumberFormatException e) {
        }

        aligner.addCategories(metadata, params.getCategories(), localCateg, context, null, false);

        metadata = metadataManager.insertMetadata(context, metadata, xml, IndexingMode.none, false, UpdateDatestamp.NO, false, DirectIndexSubmittor.INSTANCE);

        String id = String.valueOf(metadata.getId());

        aligner.addPrivileges(id, params.getPrivileges(), localGroups, context);

        dataMan.indexMetadata(id, DirectIndexSubmittor.INSTANCE);

        return id;
    }

    private void loadMetadataThumbnail(String thumbnail, String metadataId, String uuid) {
        log.info("  - Creating thumbnail for metadata uuid: " + uuid);

        try {
            String filename = uuid + ".png";
            Path dir = context.getUploadDir();

            byte[] thumbnailImg =  Base64.decodeBase64(thumbnail);
            // TODO use FileStore API
//            metadataManager.flush();
//            result.thumbnails++;

        } catch (Exception e) {
            log.warning("  - Failed to set thumbnail for metadata: " + e.getMessage());
            log.error(e);
            result.thumbnailsFailed++;
        }
    }
}
