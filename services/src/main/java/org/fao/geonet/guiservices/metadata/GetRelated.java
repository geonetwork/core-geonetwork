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

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.kernel.schema.MetadataSchema;
import org.fao.geonet.utils.Log;
import org.fao.geonet.Util;
import org.fao.geonet.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Show;
import org.fao.geonet.services.relations.Get;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
 * In general, relations are defined in ISO19139 records an ISO profiles. The
 * target document may be in a different schema (eg. ISO19110 for feature
 * catalog, Dublin core for cross reference to a document). 
 * 
 * 
 * Note about each type:
 * <h3>online</h3>
 * List of online resources (see <schema>/process/extract-relations.xsl for details).
 * 
 * <h3>thumbnail</h3>
 * List of thumbnails (see <schema>/process/extract-relations.xsl for details).
 * 
 * <h3>service</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Search for all records having an operatesOn element pointing to the requested 
 * metadata record UUID (see indexing to know how operatesOn element is indexed).
 * 
 * <h3>parent</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Get the parentIdentifier from the requested 
 * metadata record
 * 
 * <h3>children</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Search for all records having an parentUuid element pointing to the requested 
 * metadata record UUID (see indexing to know how operatesOn element is indexed).
 * 
 * 
 * <h3>dataset</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Get all records defined in operatesOn element (the current metadata is supposed
 * to be a service metadata in that case).
 * 
 * <h3>source</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Get all records defined in source element (in data quality section).
 * 
 * <h3>hassource</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Get all records where this record is defined has source (in data quality section).
 * 
 * <h3>fcat</h3>
 * Only apply to ISO19110, ISO19139 and ISO profiles.
 * Get all records defined in featureCatalogueCitation.
 * 
 * <h3>siblings</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Get all aggregationInfo records. This relation provides
 * information about association type and initiative type.
 * 
 * <h3>associated</h3>
 * Only apply to ISO19139 and ISO profiles.
 * Search for all records having an agg_associated field pointing to the requested
 * metadata record (inverse direction of sibilings). This relation does not
 * inform about association type and initiative type.
 * 
 * <h3>related</h3>
 * (deprecated) Use to link ISO19110 and ISO19139 record.
 *
 *
 *
 * TODO: Part of this should be moved to schema plugins as it might
 * be different according to schema. extract-relations.xsl cover part
 * of the needs.
 * 
 */
public class GetRelated implements Service {

    private ServiceConfig _config;
    private static Namespace dct = Namespace.getNamespace("dct", "http://purl.org/dc/terms/");
    private static Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
    private static Namespace srv = Namespace.getNamespace("srv", "http://www.isotc211.org/2005/srv");
    private static Namespace gco = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
    private static final String XPATH_FOR_AGGRGATIONINFO = "*//gmd:aggregationInfo/*" + 
            "[gmd:aggregateDataSetIdentifier/*/gmd:code " + 
            "and gmd:initiativeType/gmd:DS_InitiativeTypeCode/@codeListValue != '' " + 
            "and gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue!='']";
    private static List<Namespace> nsList = Arrays.asList(gmd, gco, srv);
    private static String maxRecords = "1000";
    private static boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
    
    public void init(String appPath, ServiceConfig config) throws Exception {
        _config = config;
    }

    public Element exec(Element params, ServiceContext context) throws Exception {
        String type = Util.getParam(params, "type", "");
        String fast = Util.getParam(params, "fast", "true");
        String from = Util.getParam(params, "from", "1");
        String to = Util.getParam(params, "to", maxRecords);

        Log.info(Geonet.SEARCH_ENGINE, "GuiService param is " + _config.getValue("guiService"));

        Element info = params.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        int id;
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

            id = Integer.parseInt(mdId);
        } else {
            uuid = info.getChildText(Params.UUID);
            id = Integer.parseInt(info.getChildText(Params.ID));
        }

        Element relatedRecords = new Element("relations");

        // Get the cached version (use by classic GUI)
        Element md = Show.getCached(context.getUserSession(), Integer.toString(id));
        if (md == null) {
            // Get from DB
            md = gc.getBean(DataManager.class).getMetadata(context, String.valueOf(id), forEditing, withValidationErrors,
                    keepXlinkAttributes);
        }


        // Search for children of this record
        if (type.equals("") || type.contains("children")) {
            relatedRecords.addContent(search(uuid, "children", context, from, to, fast));
        }

        // Get parent record from this record
        if (type.equals("") || type.contains("parent")) {
            String schema = dm.getMetadataSchema(id + "");
            // Assume ISO19139 record
            String parentNodeName = "parentIdentifier";
            Namespace parentNodeNamespace = gmd;
            String elementName = "CharacterString";
            Namespace elementNamespace = gco;
            if (schema.equals("dublin-core")) {
                parentNodeName = "isPartOf";
                parentNodeNamespace = dct;
                elementName = null;
                elementNamespace = null;
            }
            ElementFilter el = new ElementFilter(parentNodeName, parentNodeNamespace);
            StringBuffer uuids = filterMetadataAndGetElement(md, el, elementName, elementNamespace);
            if (uuids.length() > 0) {
                relatedRecords.addContent(search(uuids.toString(), "parent", context, from, to, fast));
            }
        }

        // Get aggregates from this record
        if (type.equals("") || type.contains("siblings")) {
            Element response = new Element("response");
            List<?> sibs = Xml
                    .selectNodes(
                            md,
                            XPATH_FOR_AGGRGATIONINFO,
                            nsList);
            for (Object o : sibs) {
                if (o instanceof Element) {
                    Element sib = (Element) o;
                    Element agId = (Element) sib.getChild("aggregateDataSetIdentifier", gmd).getChildren().get(0);
                    String sibUuid = agId.getChild("code", gmd).getChildText("CharacterString", gco);
                    String initType = sib.getChild("initiativeType", gmd).getChild("DS_InitiativeTypeCode", gmd)
                            .getAttributeValue("codeListValue");

                    Element sibContent = getRecord(sibUuid, context, dm);
                    if (sibContent != null) {
                        Element sibling = new Element("sibling");
                        sibling.setAttribute("initiative", initType);
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
        if (type.equals("") || type.contains("dataset") || type.contains("fcat") || type.contains("source")) {
            // Get datasets related to service search
            if (type.equals("") || type.contains("dataset")) {
                ElementFilter el = new ElementFilter("operatesOn", srv);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(), "datasets", context, from, to, fast));
                }
            }
            // if source, return source datasets defined in the current record
            if (type.equals("") || type.contains("source")) {
                ElementFilter el = new ElementFilter("source", gmd);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(), "sources", context, from, to, fast));
                }
            }
            // if fcat
            if (type.equals("") || type.contains("fcat")) {
                ElementFilter el = new ElementFilter("featureCatalogueCitation", gmd);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(), "fcats", context, from, to, fast));
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
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(id, "full", context)));
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

    /**
     * Search in metadata all matching element for the filter 
     * and return a list of uuid separated by or to be used in a
     * search on uuid. Extract uuid from attribute uuidref of
     * matched element.
     * 
     * @param md
     * @param el
     * @return
     */
    private StringBuffer filterMetadata(Element md, ElementFilter el) {
        @SuppressWarnings("unchecked")
        Iterator<Element> i = md.getDescendants(el);
        StringBuffer uuids = new StringBuffer("");
        boolean first = true;
        while (i.hasNext()) {
            Element e = i.next();
            String uuid = e.getAttributeValue("uuidref");
            if (!"".equals(uuid)) {
                if (first) {
                    uuids.append(uuid);
                    first = false;
                } else {
                    uuids.append(" or " + uuid);
                }
            }
        }
        return uuids;
    }

    /**
     * Search in metadata all matching element for the filter
     * and return a list of uuid separated by or to be used in a
     * search on uuid. Extract uuid from matched element if
     * elementName is null or from the elementName child.
     *
     * @param md
     * @param el
     * @param elementName
     * @param elementNamespace
     * @return
     */
    private StringBuffer filterMetadataAndGetElement(Element md,
                                                     ElementFilter el,
                                                     String elementName,
                                                     Namespace elementNamespace) {
        @SuppressWarnings("unchecked")
        Iterator<Element> i = md.getDescendants(el);
        StringBuffer uuids = new StringBuffer("");
        boolean first = true;
        while (i.hasNext()) {
            Element e = i.next();
            String uuid = elementName == null ?
                    e.getText() :
                    e.getChildText(elementName, elementNamespace);
            if (!"".equals(uuid)) {
                if (first) {
                    uuids.append(uuid);
                    first = false;
                } else {
                    uuids.append(" or " + uuid);
                }
            }
        }
        return uuids;
    }

    private Element search(String uuid, String type, ServiceContext context, String from, String to, String fast) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getBean(SearchManager.class);

        // perform the search
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);
        MetaSearcher searcher = searchMan.newSearcher(SearchManager.LUCENE, Geonet.File.SEARCH_LUCENE);

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