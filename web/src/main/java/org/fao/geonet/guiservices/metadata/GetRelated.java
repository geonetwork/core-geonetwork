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
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
import jeeves.utils.Xml;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.lib.Lib;
import org.fao.geonet.services.Utils;
import org.fao.geonet.services.metadata.Show;
import org.fao.geonet.services.relations.Get;
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
 *
 * Parameters:
 * <ul>
 * <li>type: service|children|related|parent|dataset|source|fcat|null (ie. all)</li>
 * <li>from: start record</li>
 * <li>to: end record</li>
 * <li>id or uuid: could be optional if call in Jeeves service forward call. In
 * that case geonet:info/uuid is used.</li>
 * </ul>
 *
 */
public class GetRelated implements Service {

    private ServiceConfig _config;
    private static Namespace gmd = Namespace.getNamespace("gmd",
            "http://www.isotc211.org/2005/gmd");
    private static Namespace srv = Namespace.getNamespace("srv",
            "http://www.isotc211.org/2005/srv");
    private static Namespace gco = Namespace.getNamespace("gco",
            "http://www.isotc211.org/2005/gco");
    private static List<Namespace> nsList = Arrays.asList(gmd, gco, srv);

    public void init(String appPath, ServiceConfig config) throws Exception {
        _config = config;
    }

    public Element exec(Element params, ServiceContext context)
            throws Exception {
        // Check for one of service|children|related|null (ie. all)
        String type = Util.getParam(params, "type", "");
        String fast = Util.getParam(params, "fast", "true");
        String from = Util.getParam(params, "from", "1");
        String to = Util.getParam(params, "to", "1000");

        Log.info(Geonet.SEARCH_ENGINE,
                "GuiService param is " + _config.getValue("guiService"));

        Element info = params.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);
        int id;
        String uuid;
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        DataManager dm = gc.getDataManager();
        Dbms dbms = null;

        if (info == null) {
            String mdId = Utils.getIdentifierFromParameters(params, context);
            if (mdId == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
            uuid = dm.getMetadataUuid(dbms, mdId);
            if (uuid == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            id = Integer.parseInt(mdId);
        } else {
            uuid = info.getChildText(Params.UUID);
            id = Integer.parseInt(info.getChildText(Params.ID));
        }

        Element relatedRecords = new Element("relations");

        Element md = Show.getCached(context.getUserSession(), Integer.toString(id));
        if (type.equals("") || type.contains("children")) {
            relatedRecords.addContent(search(uuid, "children", context, from,
                    to, fast));
        }
        if (type.equals("") || type.contains("parent")) {
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            if(md == null) {
                md = gc.getDataManager().getMetadata(context,
                        String.valueOf(id), forEditing, withValidationErrors,
                        keepXlinkAttributes);
            }
            if (md != null) {
                Element parent = md.getChild("parentIdentifier", gmd);
                if (parent != null) {
                    String parentUuid = parent.getChildText("CharacterString", gco);

									if(dbms == null) dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
									Element parentContent = getRecord(parentUuid, context, dbms, dm);
									if (parentContent != null) {
										relatedRecords.addContent(new Element("parent")
											.addContent(new Element("response")
											.addContent(parentContent)));
									}
								}
            }
        }
        if (type.equals("") || type.contains("siblings")) {
            boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;
            if(md == null) {
                md = gc.getDataManager().getMetadata(context,
                        String.valueOf(id), forEditing, withValidationErrors,
                        keepXlinkAttributes);
            }
						Element response = new Element("response");
            if (md != null) {
                List<?> sibs = Xml.selectNodes(md, "*//gmd:aggregationInfo/*[gmd:aggregateDataSetIdentifier/*/gmd:code and gmd:initiativeType/gmd:DS_InitiativeTypeCode and string(gmd:associationType/gmd:DS_AssociationTypeCode/@codeListValue)='crossReference']", nsList);
								for (Object o : sibs) {
									if (o instanceof Element) {
										Element sib = (Element)o;
										Element agId = (Element)sib
														.getChild("aggregateDataSetIdentifier", gmd)
										        .getChildren().get(0);
                  	String sibUuid = agId
														.getChild("code", gmd)
														.getChildText("CharacterString", gco);
										String initType = sib.getChild("initiativeType", gmd) 
																 .getChild("DS_InitiativeTypeCode", gmd).getAttributeValue("codeListValue");

										if(dbms == null) dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);
										Element sibContent = getRecord(sibUuid, context, dbms, dm);
										if (sibContent != null) {
											Element sibling = new Element("sibling");
											sibling.setAttribute("initiative",initType);
											response.addContent(sibling.addContent(sibContent));
										}
									}
								}
            }
						relatedRecords.addContent(new Element("siblings")
																				.addContent(response));
        }
        if (type.equals("") || type.contains("service")) {
            relatedRecords.addContent(search(uuid, "services", context, from,
                    to, fast));
        }
        // Related record from uuiref attributes in metadata record
        if (type.equals("") || type.contains("dataset")
                || type.contains("fcat") || type.contains("source")) {
            boolean forEditing = false, withValidationErrors = false;
            if(md == null) {
                md = gc.getDataManager()
                    .getMetadata(context, String.valueOf(id), forEditing,
                            withValidationErrors, false);
            }

            // Get datasets related to service search
            if (type.equals("") || type.contains("dataset")) {
                ElementFilter el = new ElementFilter("operatesOn", srv);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(),
                            "datasets", context, from, to, fast));
                }
            }
            // if source
            if (type.equals("") || type.contains("source")) {
                ElementFilter el = new ElementFilter("source", gmd);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(),
                            "sources", context, from, to, fast));
                }
            }
            // if fcat
            if (type.equals("") || type.contains("fcat")) {
                ElementFilter el = new ElementFilter(
                        "featureCatalogueCitation", gmd);
                StringBuffer uuids = filterMetadata(md, el);
                if (uuids.length() > 0) {
                    relatedRecords.addContent(search(uuids.toString(), "fcats",
                            context, from, to, fast));
                }
            }
        }

        if (type.equals("") || type.contains("related")) {
            // Related records could be feature catalogue defined in relation table
            relatedRecords.addContent(new Element("related").addContent(Get.getRelation(id, "full", context)));
            // Or feature catalogue define in feature catalogue citation
            relatedRecords.addContent(search(uuid, "hasfeaturecat", context, from,
                    to, fast));

        }

        return relatedRecords;

    }

    private StringBuffer filterMetadata(Element md, ElementFilter el) {
        Iterator<Element> i = md.getDescendants(el);
        StringBuffer uuids = new StringBuffer("");
        boolean first = true;
        while (i.hasNext()) {
            Element e = i.next();
            if (first) {
                uuids.append(e.getAttributeValue("uuidref"));
                first = false;
            } else {
                uuids.append(" or " + e.getAttributeValue("uuidref"));
            }
        }
        return uuids;
    }

    private Element search(String uuid, String type, ServiceContext context,
            String from, String to, String fast) throws Exception {
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getSearchmanager();

        // perform the search
        if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
            Log.debug(Geonet.SEARCH_ENGINE, "Searching for: " + type);
        MetaSearcher searcher = searchMan.newSearcher(SearchManager.LUCENE,
                Geonet.File.SEARCH_LUCENE);

        try {
            // Creating parameters for search, fast only to retrieve uuid
            Element parameters = new Element(Jeeves.Elem.REQUEST);
            if ("children".equals(type))
                parameters.addContent(new Element("parentUuid").setText(uuid));
            else if ("services".equals(type))
                parameters.addContent(new Element("operatesOn").setText(uuid));
            else if ("hasfeaturecat".equals(type))
                parameters.addContent(new Element("hasfeaturecat").setText(uuid));
            else if ("datasets".equals(type) || "fcats".equals(type) || "sources".equals(type) || "siblings".equals(type))
                parameters.addContent(new Element("uuid").setText(uuid));
            parameters.addContent(new Element("fast").addContent("index"));
            parameters.addContent(new Element("sortBy").addContent("title"));
            parameters.addContent(new Element("sortOrder").addContent("reverse"));
            parameters.addContent(new Element("from").addContent(from));
            parameters.addContent(new Element("to").addContent(to));

            searcher.search(context, parameters, _config);

            Element response = new Element(type);
            Element relatedElement = searcher.present(context, parameters,
                    _config);
            response.addContent(relatedElement);
            return response;
        } finally {
            searcher.close();
        }
    }

		private Element getRecord(String uuid, ServiceContext context, Dbms dbms, DataManager dm) {
			Element content = null;
			try {
				String id = dm.getMetadataId(dbms, uuid);
				boolean forEditing = false, withValidationErrors = false, keepXlinkAttributes = false;

				Lib.resource.checkPrivilege(context, id, AccessManager.OPER_VIEW);
				content = dm.getMetadata(context, id,
																	forEditing, withValidationErrors,
                                	keepXlinkAttributes);
			} catch (Exception e) {
				if (Log.isDebugEnabled(Geonet.SEARCH_ENGINE))
					Log.debug(Geonet.SEARCH_ENGINE, "Metadata "+uuid+" record is not visible for user.");
			}
			return content;
		}
}
