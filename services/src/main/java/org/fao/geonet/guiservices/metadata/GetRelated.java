//=============================================================================
//===    Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===    United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===    and United Nations Environment Programme (UNEP)
//===
//===    This program is free software; you can redistribute it and/or modify
//===    it under the terms of the GNU General Public License as published by
//===    the Free Software Foundation; either version 2 of the License, or (at
//===    your option) any later version.
//===
//===    This program is distributed in the hope that it will be useful, but
//===    WITHOUT ANY WARRANTY; without even the implied warranty of
//===    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===    General Public License for more details.
//===
//===    You should have received a copy of the GNU General Public License
//===    along with this program; if not, write to the Free Software
//===    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===    Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===    Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.guiservices.metadata;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.server.dispatchers.ServiceManager;
import org.fao.geonet.Constants;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.kernel.RelatedMetadata;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.schema.AssociatedResource;
import org.fao.geonet.kernel.schema.AssociatedResourcesSchemaPlugin;
import org.fao.geonet.kernel.schema.SchemaPlugin;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Show;
import org.fao.geonet.services.relations.Get;
import org.fao.geonet.utils.Log;
import org.fao.geonet.utils.Xml;
import org.jdom.Content;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.file.Path;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 * Perform a search and return all children metadata record for current record.
 * 
 * In some cases, related records found :
 * <ul>
 * <li>could not be readable by current user.</li>
 * <li>could not be visible by current user.</li>
 * </ul>
 * so results depend on user privileges for related records.
 * 
 * Parameters:
 * <ul>
 * <li>type: online|thumbnail|service|dataset|parent|children|source|fcat|siblings|associated|related|null (ie. all)</li>
 * <li>from: start record</li>
 * <li>to: end record (default 1000)</li>
 * <li>id or uuid: could be optional if call in Jeeves service forward call. 
 *  In that case geonet:info/uuid is used.</li>
 * </ul>
 * 
 * Relations are usually defined in records using ISO19139 or ISO19115-3 standards
 * or profiles. Therefore, some other schema plugin may also support association
 * of resources like Dublin Core using isPartOf element. In all types of
 * association, the target document may be in a different schema
 * (eg. ISO19110 for feature catalog, Dublin core for cross reference
 * to a document, SensorML for a sensor description, ...).
 *
 * 3 types of relations may be used:
 * <ul>
 *     <li>Relation to a metadata record stored in the metadata document to be analyzed.
 *     In that case, the XML document is filtered by method defined
 *     in the SchemaPlugin specific bean. eg. parent/child relation.</li>
 *     <li>Relation to specific resources (eg. online source) stored in the metadata document.
 *     In that case, the XML document is filtered by the schema/process/extract-relation.xsl.</li>
 *     <li>References stored in the target document. eg. the link to a dataset
 *     is defined in the service metadata record. In that case, the search
 *     is made in the index.</li>
 * </ul>
 * 
 * Note about each type of associations:
 * <h3>online</h3>
 * List of online resources (see <schema>/process/extract-relations.xsl for details).
 * 
 * <h3>thumbnail</h3>
 * List of thumbnails (see <schema>/process/extract-relations.xsl for details).
 * 
 * <h3>service</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Search for all records having an operatesOn element pointing to the requested 
 * metadata record UUID (see indexing to know how operatesOn element is indexed).
 * 
 * <h3>parent</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Get the parentIdentifier from the requested 
 * metadata record
 * 
 * <h3>children</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Search for all records having an parentUuid element pointing to the requested 
 * metadata record UUID (see indexing to know how parent/child relation is indexed).
 * 
 * 
 * <h3>dataset</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Get all records defined in operatesOn element (the current metadata is supposed
 * to be a service metadata in that case).
 * 
 * <h3>source</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Get all records defined in source element (in data quality section).
 * 
 * <h3>hassource</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Get all records where this record is defined has source (in data quality section).
 * 
 * <h3>fcat</h3>
 * Only apply to ISO19110, ISO19139, ISO19115-3 and ISO profiles.
 * Get all records defined in featureCatalogueCitation.
 * 
 * <h3>siblings</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Get all aggregationInfo records. This relation provides
 * information about association type and initiative type.
 * 
 * <h3>associated</h3>
 * Only apply to ISO19139, ISO19115-3 and ISO profiles.
 * Search for all records having an agg_associated field pointing to the requested
 * metadata record (inverse direction of siblings). This relation does not
 * inform about association type and initiative type.
 * 
 * <h3>related</h3>
 * (deprecated) Use to link ISO19110 and ISO19139 record using database table.
 *
 * @see org.fao.geonet.kernel.schema.SchemaPlugin for more details
 * on how specific schema plugin implement relations extraction.
 * 
 */
@Controller
@Qualifier("getRelated")
public class GetRelated implements Service, RelatedMetadata {

    private ServiceConfig _config = new ServiceConfig();
    private static int maxRecords = 1000;
    private static boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
    @Autowired
    private ServiceManager serviceManager;
    @Autowired
    private GeonetworkDataDirectory dataDirectory;
    @Autowired
    private DataManager dataManager;
    @Autowired
    MetadataRepository metadataRepository;

    public void init(Path appPath, ServiceConfig config) throws Exception {
        _config = config;
    }

    @RequestMapping(value="/{lang}/xml.relation")
    public HttpEntity<byte[]> exec(@PathVariable String lang,
                       @RequestParam (required = false) Integer id,
                       @RequestParam (required = false) String uuid,
                       @RequestParam (defaultValue = "") String type,
                       @RequestParam (defaultValue = "1") int from,
                       @RequestParam (defaultValue = "-1") int to,
                       boolean fast,
                       HttpServletRequest request) throws Exception {
        if (to < 0) {
            to = maxRecords;
        }

        final ServiceContext context = serviceManager.createServiceContext("xml.relation", lang, request);

        Metadata md;
        if (id != null) {
             md = metadataRepository.findOne(id);

            if (md == null) {
                throw new IllegalArgumentException("No Metadata found with id " + id);
            }
        } else {
            md = metadataRepository.findOneByUuid(uuid);

            if (md == null) {
                throw new IllegalArgumentException("No Metadata found with uuid " + uuid);
            }
        }
        id = md.getId();
        uuid = md.getUuid();

        Element raw = new Element("root").addContent(getRelated(context, id, uuid, type, from, to, fast));
        Path relatedXsl = dataDirectory.getWebappDir().resolve("xsl/metadata/relation.xsl");

        final Element transform = Xml.transform(raw, relatedXsl);
        final Set<String> acceptContentType = Sets.newHashSet(Iterators.forEnumeration(request.getHeaders("Accept")));

        byte[] response;
        String contentType;
        if (acceptContentType == null ||
            acceptsType(acceptContentType, "xml") ||
            acceptContentType.contains("*/*")||
            acceptContentType.contains("text/plain")) {
            response = Xml.getString(transform).getBytes(Constants.CHARSET);
            contentType = "application/xml";
        } else if (acceptsType(acceptContentType, "json")) {
            response = Xml.getJSON(transform).getBytes(Constants.CHARSET);
            contentType = "application/json";
        } else {
            throw new IllegalArgumentException(acceptContentType + " is not supported");
        }

        MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("Content-Type", contentType);

        return new HttpEntity<>(response, headers);
    }

    private boolean acceptsType(Set<String> acceptContentType, String toCheck) {
        for (String acceptable : acceptContentType) {
            if (acceptable.contains(toCheck)) {
                return true;
            }
        }
        return false;
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String type = Util.getParam(params, "type", "");
        String fast = Util.getParam(params, "fast", "true");
        int from = Util.getParam(params, "from", 1);
        int to = Util.getParam(params, "to", maxRecords);

        Log.info(Geonet.SEARCH_ENGINE, "GuiService param is " + _config.getValue("guiService"));

        Element info = params.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        int iId;
        String uuid;

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);

        if (info == null) {
            String mdId = Utils.getIdentifierFromParameters(params, context);
            if (mdId == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            uuid = dm.getMetadataUuid(mdId);
            if (uuid == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            iId = Integer.parseInt(mdId);
        } else {
            uuid = info.getChildText(Params.UUID);
            iId = Integer.parseInt(info.getChildText(Params.ID));
        }

        return getRelated(context, iId, uuid, type, from, to, Boolean.parseBoolean(fast));
    }

    @Override
    public Element getRelated(ServiceContext context, int iId, String uuid, String type, int from_, int to_, boolean fast_)
            throws Exception {
        final String id = String.valueOf(iId);
        final String from = "" + from_;
        final String to = "" + to_;
        final String fast = "" + fast_;
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getBean(DataManager.class);
        Element relatedRecords = new Element("relations");

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
        if (type.equals("") || type.contains("children")) {
            relatedRecords.addContent(search(uuid, "children", context, from, to, fast));
        }

        // Get parent record from this record
        if (schemaPlugin != null && (type.equals("") || type.contains("parent"))) {
            Set<String> listOfUUIDs = schemaPlugin.getAssociatedParentUUIDs(md);
            if (listOfUUIDs.size() > 0) {
                String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                relatedRecords.addContent(search(joinedUUIDs, "parent", context, from, to, fast));
            }
        }

        // Get aggregates from this record
        if (schemaPlugin != null && (type.equals("") || type.contains("siblings"))) {
            Element response = new Element("response");

            Set<AssociatedResource> listOfAssociatedResources = schemaPlugin.getAssociatedResourcesUUIDs(md);

            if (listOfAssociatedResources != null) {
                for (AssociatedResource resource : listOfAssociatedResources) {
                    Element sibContent = getRecord(resource.getUuid(), context, dm);
                    if (sibContent != null) {
                        Element sibling = new Element("sibling");
                        sibling.setAttribute("initiative", resource.getInitiativeType());
                        response.addContent(sibling.addContent(sibContent));
                    }
                }
            }
            relatedRecords.addContent(new Element("siblings").addContent(response));
        }

        // Search for records where an aggregate point to this record
        if (type.equals("") || type.contains("associated")) {
            relatedRecords.addContent(search(uuid, "associated", context, from, to, fast));
        }

        // Search for services
        if (type.equals("") || type.contains("service")) {
            relatedRecords.addContent(search(uuid, "services", context, from, to, fast));
        }

        // Related record from uuiref attributes in metadata record
        if (schemaPlugin != null && (
                type.equals("") || type.contains("dataset") || type.contains("fcat") || type.contains("source")
        )) {
            // Get datasets related to service search
            if (type.equals("") || type.contains("dataset")) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedDatasetUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "datasets", context, from, to, fast));
                }
            }
            // if source, return source datasets defined in the current record
            if (type.equals("") || type.contains("source")) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedSourceUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "sources", context, from, to, fast));
                }
            }
            // if fcat
            if (type.equals("") || type.contains("fcat")) {
                Set<String> listOfUUIDs = schemaPlugin.getAssociatedFeatureCatalogueUUIDs(md);
                if (listOfUUIDs != null && listOfUUIDs.size() > 0) {
                    String joinedUUIDs = Joiner.on(" or ").join(listOfUUIDs);
                    relatedRecords.addContent(search(joinedUUIDs, "fcats", context, from, to, fast));
                }
            }
        }

        // 
        if (type.equals("") || type.contains("hassource")) {
            // Return records where this record is a source dataset
            relatedRecords.addContent(search(uuid, "hassource", context, from, to, fast));
        }

        // Relation table is preserved for backward compatibility but should not be used anymore.
        if (type.equals("") || type.contains("related")) {
            // Related records could be feature catalogue defined in relation table
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(iId, "full", context)));
            // Or feature catalogue define in feature catalogue citation
            relatedRecords.addContent(search(uuid, "hasfeaturecat", context, from, to, fast));
        }

        // XSL transformation is used on the metadata record to extract
        // distribution information or thumbnails
        if (md != null && (type.equals("") || type.contains("online") || type.contains("thumbnail"))) {
            relatedRecords.addContent(new Element("metadata").addContent((Content) md.clone()));
        }

        return relatedRecords;
    }


    private Element search(String uuid, String type, ServiceContext context, String from, String to, String fast) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getBean(SearchManager.class);

        // perform the search
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);
        MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE);

        try {
            // Creating parameters for search, fast only to retrieve uuid
            Element parameters = new Element(Jeeves.Elem.REQUEST);
            if ("children".equals(type))
                parameters.addContent(new Element("parentUuid").setText(uuid));
            else if ("services".equals(type))
                parameters.addContent(new Element("operatesOn").setText(uuid));
            else if ("hasfeaturecat".equals(type))
                parameters.addContent(new Element("hasfeaturecat").setText(uuid));
            else if ("hassource".equals(type))
                parameters.addContent(new Element("hassource").setText(uuid));
            else if ("associated".equals(type))
                parameters.addContent(new Element("agg_associated").setText(uuid));
            else if ("datasets".equals(type) || "fcats".equals(type) ||
                    "sources".equals(type) || "siblings".equals(type) ||
                    "parent".equals(type))
                parameters.addContent(new Element("uuid").setText(uuid));

            parameters.addContent(new Element("fast").addContent("index"));
            parameters.addContent(new Element("sortBy").addContent("title"));
            parameters.addContent(new Element("sortOrder").addContent("reverse"));
            parameters.addContent(new Element("from").addContent(from));
            parameters.addContent(new Element("to").addContent(to));

            searcher.search(context, parameters, _config);

            Element response = new Element(type);
            Element relatedElement = searcher.present(context, parameters, _config);
            response.addContent(relatedElement);
            return response;
        } finally {
            searcher.close();
        }
    }

    private Element getRecord(String uuid, ServiceContext context, DataManager dm) {
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
}