/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records;

import com.google.common.base.Joiner;
import jeeves.server.context.ServiceContext;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.setting.SettingInfo;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.relations.Get;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Content;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fao.geonet.kernel.search.EsSearchManager.FIELDLIST_CORE;
import static org.fao.geonet.kernel.search.EsSearchManager.relatedIndexFields;


/**
 *
 */
public class MetadataUtils {
    public static final boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.SEARCH_ENGINE);

    public static Element getRelated(ServiceContext context, int iId, String uuid,
                                     RelatedItemType[] type,
                                     int from_, int to_, boolean fast_)
        throws Exception {
        final String id = String.valueOf(iId);
        final String from = "" + from_;
        final String to = "" + to_;
        final String fast = "" + fast_;
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        Element relatedRecords = new Element("relations");

        if (type == null || type.length == 0) {
            type = RelatedItemType.class.getEnumConstants();
        }
        List<RelatedItemType> listOfTypes = new ArrayList<RelatedItemType>(Arrays.asList(type));

        Element md = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);

        String schemaIdentifier = dm.getMetadataSchema(id);
        SchemaPlugin instance = SchemaManager.getSchemaPlugin(schemaIdentifier);
        AssociatedResourcesSchemaPlugin schemaPlugin = null;
        if (instance instanceof AssociatedResourcesSchemaPlugin) {
            schemaPlugin = (AssociatedResourcesSchemaPlugin) instance;
        }

        // Search for children of this record
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.children)) {
            relatedRecords.addContent(search(uuid, "children", context, from, to, fast, null));
        }

        // Get parent record from this record
        if (schemaPlugin != null && (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.parent))) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (listOfUUIDs.size() > 0) {
                String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                relatedRecords.addContent(search(joinedUUIDs, "parent", context, from, to, fast, null));
            }
        }

        // Brothers and sisters are not returned by default
        // It is only on demand and output as siblings.
        if (schemaPlugin != null && listOfTypes.contains(RelatedItemType.brothersAndSisters)) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (listOfUUIDs.size() > 0) {
                String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                relatedRecords.addContent(search(joinedUUIDs, RelatedItemType.brothersAndSisters.value(), context, from, to, fast, uuid));
            }
        }

        // Get aggregates from this record
        if (schemaPlugin != null && (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.siblings))) {
            Element response = new Element("response");

            Set<AssociatedResource> listOfAssociatedResources = schemaPlugin.getAssociatedResourcesUUIDs(md);

            if (listOfAssociatedResources != null) {
                for (AssociatedResource resource : listOfAssociatedResources) {
                    Element sibContent = getRecord(resource.getUuid(), context, dm);
                    if (sibContent != null) {
                        Element sibling = new Element("sibling");
                        sibling.setAttribute("initiative", resource.getInitiativeType());
                        sibling.setAttribute("association", resource.getAssociationType());
                        response.addContent(sibling.addContent(sibContent));
                    }
                }
            }
            relatedRecords.addContent(new Element("siblings").addContent(response));
        }

        // Search for records where an aggregate point to this record
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.associated)) {
            relatedRecords.addContent(search(uuid, "associated", context, from, to, fast, null));
        }

        // Search for services
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.services)) {
            relatedRecords.addContent(search(uuid, "services", context, from, to, fast, null));
        }

        // Related record from uuiref attributes in metadata record
        if (schemaPlugin != null && (
            listOfTypes.size() == 0 ||
                listOfTypes.contains(RelatedItemType.datasets) ||
                listOfTypes.contains(RelatedItemType.fcats) ||
                listOfTypes.contains(RelatedItemType.sources)
        )) {
            // Get datasets related to service search
            if (listOfTypes.size() == 0 ||
                listOfTypes.contains(RelatedItemType.datasets)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedDatasetUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "datasets", context, from, to, fast, null));
                }
            }
            // if source, return source datasets defined in the current record
            if (listOfTypes.size() == 0 ||
                listOfTypes.contains(RelatedItemType.sources)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedSourceUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "sources", context, from, to, fast, null));
                }
            }
            // if fcat
            if (listOfTypes.size() == 0 ||
                listOfTypes.contains(RelatedItemType.fcats)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedFeatureCatalogueUUIDs(md);
                Element fcat = null;

                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {

                    fcat = new Element("fcats");

                    for (String fcat_uuid : listOfUUIDs) {
                        Element metadata = new Element("metadata");
                        Element response = new Element("response");
                        Element current = getRecord(fcat_uuid, context, dm);
                        if (current != null) {
                            metadata.addContent(current);
                        } else {
                            LOGGER.error("Feature catalogue with UUID {} referenced in {} was not found.", fcat_uuid, uuid);
                        }
                        response.addContent(metadata);
                        fcat.addContent(response);
                    }
                }

                if (fcat != null) {
                    relatedRecords.addContent(fcat);
                }
            }
        }

        //
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.hassources)) {
            // Return records where this record is a source dataset
            relatedRecords.addContent(search(uuid, "hassources", context, from, to, fast, null));
        }

        // Relation table is preserved for backward compatibility but should not be used anymore.
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.related)) {
            // Related records could be feature catalogue defined in relation table
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(iId, "full", context)));
            // Or feature catalogue define in feature catalogue citation
            relatedRecords.addContent(search(uuid, "hasfeaturecats", context, from, to, fast, null));
        }

        // XSL transformation is used on the metadata record to extract
        // distribution information or thumbnails
        if (md != null && (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.onlines) ||
            listOfTypes.contains(RelatedItemType.thumbnails))) {
            relatedRecords.addContent(new Element("metadata").addContent((Content) md.clone()));
        }

        return relatedRecords;
    }


    private static Element search(String uuid, String type, ServiceContext context, String from, String to, String fast, String exclude) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);

        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);

        int fromValue = Integer.parseInt(from);
        int toValue = Integer.parseInt(to);

        String excludeQuery = "";
        if (exclude != null) {
            excludeQuery = String.format(" -uuid:%s", exclude);
        }

        final SearchResponse result = searchMan.query(
            String.format("+%s:\"%s\"%s", relatedIndexFields.get(type), uuid, excludeQuery), null, FIELDLIST_CORE, fromValue, (toValue -fromValue));

        Element typeResponse = new Element(type);
        if (result.getHits().getTotalHits().value > 0) {
            // Build the old search service response format
            Element response = new Element("response");
            Arrays.asList(result.getHits().getHits()).forEach(e -> {
                Element record = new Element("metadata");
                final Map<String, Object> source = e.getSourceAsMap();
                record.addContent(new Element("id").setText((String)source.get(Geonet.IndexFieldNames.ID)));
                record.addContent(new Element("uuid").setText((String)source.get(Geonet.IndexFieldNames.UUID)));
                record.addContent(new Element("title").setText((String)source.get(Geonet.IndexFieldNames.RESOURCETITLE)));
                record.addContent(new Element("abstract").setText((String)source.get(Geonet.IndexFieldNames.RESOURCEABSTRACT)));
                response.addContent(record);
            });
            typeResponse.addContent(response);
        }
        return typeResponse;
    }

    /**
     * Run a Lucene query expression and return a list of UUIDs.
     *
     * @param query
     * @return List of UUIDs to export
     */
    public static Set<String> getUuidsToExport(String query) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        EsSearchManager searchMan = applicationContext.getBean(EsSearchManager.class);

        Set<String> uuids = new HashSet<>();
        Set<String> field = new HashSet<>(1);
        field.add(Geonet.IndexFieldNames.UUID);

        int from = 0;
        SettingInfo si = applicationContext.getBean(SettingInfo.class);
        int size = Integer.parseInt(si.getSelectionMaxRecords());

        final SearchResponse result = searchMan.query(query, null, from, size);
        if (result.getHits().getTotalHits().value > 0) {
            final SearchHit[] elements = result.getHits().getHits();
            Arrays.asList(elements).forEach(e -> uuids.add((String) e.getSourceAsMap().get(Geonet.IndexFieldNames.UUID)));
        }
        Log.info(Geonet.MEF, "  Found " + uuids.size() + " record(s).");
        return uuids;
    }

    /**
     * TODO-API : replace by ApiUtils.
     */
    private static Element getRecord(String uuid, ServiceContext context, DataManager dm) {
        Element content = null;
        try {
            String id = dm.getMetadataId(uuid);
            Lib.resource.checkPrivilege(context, id, ReservedOperation.view);
            content = dm.getMetadata(context, id, forEditing, withValidationErrors, keepXlinkAttributes);
        } catch (Exception e) {
            if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
                Log.debug(Geonet.SEARCH_ENGINE, "Metadata " + uuid + " record is not visible for user.");
        }
        return content;
    }

    public static void backupRecord(AbstractMetadata metadata, ServiceContext context) {
        Log.trace(Geonet.DATA_MANAGER, "Backing up record " + metadata.getId());
        Path outDir = Lib.resource.getRemovedDir(metadata.getId());
        Path outFile;
        try {
            // When metadata records contains character not supported by filesystem
            // it may be an issue. eg. acri-st.fr/96443
            outFile = outDir.resolve(URLEncoder.encode(metadata.getUuid(), Constants.ENCODING) + ".zip");
        } catch (UnsupportedEncodingException e1) {
            outFile = outDir.resolve(String.format(
                "backup-%s-%s.mef",
                new Date(), metadata.getUuid()));
        }

        Path file = null;
        try {
            file = MEFLib.doExport(context, metadata.getUuid(), "full", false, true, false, false, true);
            Files.createDirectories(outDir);
            try (InputStream is = IO.newInputStream(file);
                 OutputStream os = Files.newOutputStream(outFile)) {
                BinaryFile.copy(is, os);
            }
        } catch (Exception e) {
            Log.error(Geonet.GEONETWORK,"Backup record. Error: " + e.getMessage(), e);
        } finally {
            if (file == null) {
                IO.deleteFile(file, false, Geonet.MEF);
            }
        }
    }


    /**
     * Returns the metadata validation status from the database, calculating/storing the validation if not stored.
     *
     * @param metadata
     * @param context
     * @return
     */
    public static boolean retrieveMetadataValidationStatus(AbstractMetadata metadata, ServiceContext context) throws Exception {
        MetadataValidationRepository metadataValidationRepository = context.getBean(MetadataValidationRepository.class);
        IMetadataValidator validator = context.getBean(IMetadataValidator.class);
        DataManager dataManager = context.getBean(DataManager.class);

        boolean hasValidation =
            (metadataValidationRepository.count(MetadataValidationSpecs.hasMetadataId(metadata.getId())) > 0);

        if (!hasValidation) {
            validator.doValidate(metadata, context.getLanguage());
            dataManager.indexMetadata(metadata.getId() + "", true);
        }

        boolean isInvalid =
            (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

        return isInvalid;
    }
}
