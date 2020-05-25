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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.api.records.model.related.RelatedItemType;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataValidator;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataValidationRepository;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.repository.specification.MetadataValidationSpecs;
import org.fao.geonet.services.metadata.Show;
import org.fao.geonet.services.relations.Get;
import org.fao.geonet.utils.BinaryFile;
import org.fao.geonet.utils.IO;
import org.fao.geonet.utils.Log;
import org.jdom.Content;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Joiner;

import jeeves.constants.Jeeves;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

/**
 *
 */
public class MetadataUtils {
    public static final boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;

    private static final Logger LOGGER = LoggerFactory.getLogger(Geonet.SEARCH_ENGINE);

    /**
     * Constants for metadata origin:
     *
     *  - portal: the metadata is available in the current portal.
     *  - catalog: the metadata is not available in the current portal, but is available in the local catalog.
     *  - remote: the metadata is available in a remote resource, used for operatesOn resources.
     */
    private static final String ORIGIN_PORTAL = "portal";
    private static final String ORIGIN_CATALOG = "catalog";
    private static final String ORIGIN_REMOTE = "remote";

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

        String portalFilter = "";

        NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
        SourceRepository sourceRepository = ApplicationContextHolder.get().getBean(SourceRepository.class);
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Source portal = sourceRepository.findOne(node.getId());

            if (portal != null) {
                portalFilter = portal.getFilter();
            }
        }


        if(type == null || type.length == 0) {
            type = RelatedItemType.class.getEnumConstants();
        }
        List<RelatedItemType> listOfTypes = new ArrayList<RelatedItemType>(Arrays.asList(type));

        // Get the cached version (use by classic GUI)
        Element md = Show.getCached(context.getUserSession(), id);
        if (md == null) {
            // Get from DB
            md = dm.getMetadata(context, id, forEditing, withValidationErrors,
                keepXlinkAttributes);
        }

        String schemaIdentifier = dm.getMetadataSchema(id);
        SchemaPlugin instance = SchemaManager.getSchemaPlugin(schemaIdentifier);
        AssociatedResourcesSchemaPlugin schemaPlugin = null;
        if (instance instanceof AssociatedResourcesSchemaPlugin) {
            schemaPlugin = (AssociatedResourcesSchemaPlugin) instance;
        }

        // Search for children of this record
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.children)) {
            relatedRecords.addContent(calculateResults(uuid, "children", context, from, to, fast, null, null, portalFilter));
        }

        // Get parent record from this record
        if (schemaPlugin != null && (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.parent))) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (listOfUUIDs.size() > 0) {
                String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                relatedRecords.addContent(calculateResults(joinedUUIDs, "parent", context, from, to, fast, null, null, portalFilter));
            }
        }

        // Brothers and sisters are not returned by default
        // It is only on demand and output as siblings.
        if (schemaPlugin != null && listOfTypes.contains(RelatedItemType.brothersAndSisters)) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (listOfUUIDs.size() > 0) {
                String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                relatedRecords.addContent(calculateResults(joinedUUIDs, RelatedItemType.brothersAndSisters.value(), context, from, to, fast, uuid, null, portalFilter));
            }
        }

        // Get aggregates from this record
        if (schemaPlugin != null && (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.siblings))) {
            Element response = new Element("response");

            Set<AssociatedResource> listOfAssociatedResources = schemaPlugin.getAssociatedResourcesUUIDs(md);

            if (listOfAssociatedResources != null) {
                for (AssociatedResource resource : listOfAssociatedResources) {
                    String origin;
                    // Search in the index to use the portal filter and verify the metadata is available for the portal
                    Element searchResult = search(resource.getUuid(), RelatedItemType.siblings.value(), context, from, to, fast, null, false);
                    // If can't be find, skip the result.
                    if (hasResult(searchResult)) {
                        origin = ORIGIN_PORTAL;
                    } else {
                        origin = ORIGIN_CATALOG;
                    }

                    Element sibContent = getRecord(resource.getUuid(), context, dm);

                    if (sibContent != null) {
                        Element sibling = new Element("sibling");
                        sibling.setAttribute("origin", origin);
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
            relatedRecords.addContent(calculateResults(uuid, "associated", context, from, to, fast, null, null, portalFilter));
        }

        // Search for services
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.services)) {
            relatedRecords.addContent(calculateResults(uuid, "services", context, from, to, fast, null, null, portalFilter));
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
                /*Set<String> listOfUUIDs = schemaPlugin.getAssociatedDatasetUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "datasets", context, from, to, fast, null));
                }*/

                Set<String> listOfUUIDs = new HashSet<>();
                Set<String> listOfRemoteDatasets = new HashSet<>();

                Element result = search(uuid, "uuid", context, from, to, fast, null, "operatesOn", true);
                Element response = ((Element) (result.getChildren().get(0)));
                Element mdResult = ((Element) (response.getChildren().get(0)));
                List<Element> datasets = mdResult.getChildren("operatesOn");

                for(Element dataset : datasets) {
                    if (StringUtils.isNotEmpty(dataset.getValue())) {
                        String[] datasetInfo = dataset.getValue().split("\\|");
                        String datasetUuid = datasetInfo[0];
                        String refType = (datasetInfo.length > 1)?datasetInfo[1]:"L";

                        if (!refType.equals("R")) {
                            listOfUUIDs.add(datasetUuid);
                        } else {
                            listOfRemoteDatasets.add(dataset.getValue());
                        }
                    }
                }

                if (!listOfUUIDs.isEmpty()) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(calculateResults(joinedUUIDs, "datasets", context, from, to, fast, null, null, portalFilter));
                }

                if (!listOfRemoteDatasets.isEmpty()) {

                    if (relatedRecords.getChild("datasets") == null) {
                        relatedRecords.addContent(new Element("datasets").addContent(new Element("response")));
                    }

                    for(String remoteDataset : listOfRemoteDatasets) {
                        String[] remoteDatasetInfo = remoteDataset.split("\\|");

                        if(remoteDatasetInfo.length > 4) {
                            Element metadata = new Element("metadata");
                            metadata.setAttribute("origin", ORIGIN_REMOTE);
                            metadata.addContent(new Element("uuid").setText(remoteDatasetInfo[0]));
                            metadata.addContent(new Element("title").setText(remoteDatasetInfo[2]));
                            metadata.addContent(new Element("abstract").setText(remoteDatasetInfo[3]));
                            metadata.addContent(new Element("url").setText(remoteDatasetInfo[4]));

                            relatedRecords.getChild("datasets").getChild("response").addContent(metadata);
                        } else {
                            Log.warning(Geonet.SEARCH_ENGINE, "Remote dataset incomplete for uuid : " + uuid + " " + remoteDataset);
                        }
                    }
                }
            }
            // if source, return source datasets defined in the current record
            if (listOfTypes.size() == 0 ||
                listOfTypes.contains(RelatedItemType.sources)) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedSourceUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(calculateResults(joinedUUIDs, "sources", context, from, to, fast, null, null, portalFilter));
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
                        String origin;
                        // Search in the index to use the portal filter and verify the metadata is available for the portal
                        Element searchResult = search(fcat_uuid, RelatedItemType.fcats.value(), context, from, to, fast, null, false);
                        // If can't be find, skip the result.
                        if (hasResult(searchResult)) {
                            origin = ORIGIN_PORTAL;
                        } else {
                            origin = ORIGIN_CATALOG;
                        }

                        Element metadata = new Element("metadata");
                        Element response = new Element("response");
                        Element current = getRecord(fcat_uuid, context, dm);
                        if (current != null) {
                            metadata.setAttribute("origin", origin);
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
            relatedRecords.addContent(calculateResults(uuid, "hassources", context, from, to, fast, null, null, portalFilter));
        }

        // Relation table is preserved for backward compatibility but should not be used anymore.
        if (listOfTypes.size() == 0 ||
            listOfTypes.contains(RelatedItemType.related)) {
            // Related records could be feature catalogue defined in relation table
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(iId, "full", context)));
            // Or feature catalogue define in feature catalogue citation
            relatedRecords.addContent(calculateResults(uuid, "hasfeaturecats", context, from, to, fast, null, null, portalFilter));
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

    private static Element search(String uuid, String type, ServiceContext context, String from, String to,
                                  String fast, String exclude, boolean ignorePortalFilter) throws Exception {
        return search(uuid, type, context, from, to, fast, exclude, null, ignorePortalFilter);
    }

    private static Element search(String uuid, String type, ServiceContext context, String from, String to,
                                  String fast, String exclude, String extraDumpFields, boolean ignorePortalFilter) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getBean(SearchManager.class);

        // perform the search
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);

        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {
            // Creating parameters for search, fast only to retrieve uuid
            Element parameters = new Element(Jeeves.Elem.REQUEST);
            if ("children".equals(type))
                parameters.addContent(new Element("parentUuid").setText(uuid));
            else if ("brothersAndSisters".equals(type)) {
                parameters.addContent(new Element("parentUuid").setText(uuid));
            } else if ("services".equals(type))
                parameters.addContent(new Element("operatesOn").setText(uuid + " or " + uuid + "|*"));
            else if ("hasfeaturecats".equals(type))
                parameters.addContent(new Element("hasfeaturecat").setText(uuid));
            else if ("hassources".equals(type))
                parameters.addContent(new Element("hassource").setText(uuid));
            else if ("associated".equals(type)) {
                parameters.addContent(new Element("agg_associated").setText(uuid));
                parameters.addContent(new Element(Geonet.SearchResult.EXTRA_DUMP_FIELDS).setText("agg_*"));
            }
            else if ("datasets".equals(type) || "fcats".equals(type) ||
                "sources".equals(type) || "siblings".equals(type) ||
                "parent".equals(type) || "uuid".equals(type))
                parameters.addContent(new Element("uuid").setText(uuid));

            if (exclude != null) {
                parameters.addContent(new Element("without__uuid").setText(exclude));
            }

            if (StringUtils.isNotEmpty(extraDumpFields)) {
                parameters.addContent(new Element("extraDumpFields").addContent(extraDumpFields));
            }

            parameters.addContent(new Element("fast").addContent("index"));
            parameters.addContent(new Element("sortBy").addContent("title"));
            parameters.addContent(new Element("sortOrder").addContent("reverse"));
            parameters.addContent(new Element("buildSummary").addContent("false"));
            parameters.addContent(new Element("from").addContent(from));
            parameters.addContent(new Element("to").addContent(to));

            ServiceConfig config = new ServiceConfig();
            config.setValue(Geonet.SearchConfig.SEARCH_IGNORE_PORTAL_FILTER_OPTION, ignorePortalFilter + "");
            searcher.search(context, parameters, config);

            Element response = new Element(type.equals("brothersAndSisters") ? "siblings" : type);
            Element relatedElement = searcher.present(context, parameters, config);
            response.addContent(relatedElement);
            return response;
        }
    }

    /**
     * Run an XML query and return a list of UUIDs.
     *
     * @param uuid    Metadata identifier
     * @param query XML Request to run which will search for related metadata records to export
     * @return List of related UUIDs to export
     */
    public static Set<String> getUuidsToExport(String uuid,
                                         HttpServletRequest request,
                                         Element query) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        SearchManager searchMan = applicationContext.getBean(SearchManager.class);
        ServiceContext context = ApiUtils.createServiceContext(request);
        ServiceConfig _config = new ServiceConfig();
        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {

            Set<String> uuids = new HashSet<>();

            // perform the search
            searcher.search(context, query, _config);

            // If element type found, then get their uuid
            if (searcher.getSize() != 0) {
                Element elt = searcher.present(context, query, _config);

                // Get ISO records only
                @SuppressWarnings("unchecked")
                List<Element> isoElt = elt.getChildren();
                for (Element md : isoElt) {
                    // -- Only metadata record should be processed
                    if (!md.getName().equals("summary")) {
                        String mdUuid = md.getChild(Edit.RootChild.INFO,
                            Edit.NAMESPACE).getChildText(Edit.Info.Elem.UUID);
                        if (Log.isDebugEnabled(Geonet.MEF))
                            Log.debug(Geonet.MEF, "    Adding: " + mdUuid);
                        uuids.add(mdUuid);
                    }
                }
            }
            Log.info(Geonet.MEF, "  Found " + uuids.size() + " record(s).");
            return uuids;
        }
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
            dataManager.indexMetadata(metadata.getId() + "", true, null);
        }

        boolean isInvalid =
            (metadataValidationRepository.count(MetadataValidationSpecs.isInvalidAndRequiredForMetadata(metadata.getId())) > 0);

        return isInvalid;
    }


    /**
     * Checks if a result for a search query has results.
     *
     * Response examples:
     *
     * <siblings>
     *   <response from="1" to="0" />
     * </siblings>
     *
     *
     * <siblings>
     *   <response from="1" to="1">
     *      <metadata>...</metadata>
     *   </response>
     * </siblings>
     *
     * @param searchResponse
     * @return True it the response has results, False in other cases.
     */
    private static boolean hasResult(Element searchResponse) {

        if (searchResponse.getChildren().size() > 0) {
            Element containerResults = (Element) searchResponse.getChildren().get(0);
            if (containerResults.getChildren().size() > 0) {
                return true;
            }
        }

        return false;
    }


    /**
     * Process search results to add the origin of the metadata:
     *
     *  - portal: the metadata is available in the current portal.
     *  - catalog: the metadata is not available in the current portal, but is available in the local catalog.
     *
     * @param uuid
     * @param type
     * @param context
     * @param from
     * @param to
     * @param fast
     * @param exclude
     * @param extraDumpFields
     * @param portalFilter
     * @return
     * @throws Exception
     */
    private static Element calculateResults(String uuid, String type, ServiceContext context, String from, String to,
                                    String fast, String exclude, String extraDumpFields,
                                    String portalFilter) throws Exception {

        // Search related resources ignoring portal filter
        Element results = search(uuid, type, context, from, to, fast, exclude, extraDumpFields, true);

        // Check if the portal has a filter
        if (StringUtils.isNotEmpty(portalFilter)) {
            // Search related resources with the portal filter
            Element resultsForPortal = search(uuid, type, context, from, to, fast, exclude, null, false);

            // Build the set of uuids from portal results
            HashSet<String> portalResultsUuids = new HashSet<>();

            for(Element r : (List<Element>) resultsForPortal.getChild("response").getChildren()) {
                String uuidValue = r.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid");
                portalResultsUuids.add(uuidValue);
            }

            // Process the full results to add the origin depending if are available in the portal or not
            for(Element r : (List<Element>) results.getChild("response").getChildren()) {
                String origin = ORIGIN_CATALOG;

                String uuidValue = r.getChild("info", Geonet.Namespaces.GEONET).getChildText("uuid");

                // Is the result available in the portal?
                if (portalResultsUuids.contains(uuidValue)) {
                    origin = ORIGIN_PORTAL;
                }

                r.setAttribute("origin", origin);
            }
        } else {
            // No portal filter: set origin to portal
            for(Element r : (List<Element>) results.getChild("response").getChildren()) {
                r.setAttribute("origin", ORIGIN_PORTAL);
            }
        }

        return results;
    }
}
