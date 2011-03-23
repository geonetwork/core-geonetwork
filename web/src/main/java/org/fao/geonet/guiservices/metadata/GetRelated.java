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

package org.fao.geonet.guiservices.metadata;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import jeeves.utils.Log;
import jeeves.utils.Util;
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
import org.fao.geonet.services.relations.Get;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

import java.util.Iterator;

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
 * <li>type: service|children|related|parent|dataset|null (ie. all)</li>
 * <li>from: start record</li>
 * <li>to: end record</li>
 * <li>id or uuid: could be optional if call in Jeeves service forward call. In
 * that case geonet:info/uuid is used.</li>
 * </ul>
 * 
 */
public class GetRelated implements Service {

    private ServiceConfig _config;

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
        Dbms dbms = (Dbms) context.getResourceManager()
                .open(Geonet.Res.MAIN_DB);
        
        if (info == null) {
            String mdId = Utils.getIdentifierFromParameters(params, context);

            uuid = dm.getMetadataUuid(dbms, mdId);
            if (uuid == null)
                throw new MetadataNotFoundEx("Metadata not found.");

            id = Integer.parseInt(mdId);
        } else {
            uuid = info.getChildText(Params.UUID);
            id = Integer.parseInt(info.getChildText(Params.ID));
        }

        Element relatedRecords = new Element("relations");

        if (type.equals("") || type.equals("children")) {
            relatedRecords.addContent(search(uuid, "children", context, from, to, fast));
        }
        if (type.equals("") || type.equals("parent")) {
            boolean forEditing = false, withValidationErrors = false;
            Element md = gc.getDataManager().getMetadata(context, String.valueOf(id), forEditing, withValidationErrors);
            if (md != null) {
                Namespace gmd = Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd");
                Namespace gco = Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco");
                Element parent = md.getChild("parentIdentifier", gmd);
                if (parent != null) {
                    String parentUuid = parent.getChildText("CharacterString", gco);
                    String parentId = dm.getMetadataId(dbms, parentUuid);

                    try {
                        Lib.resource.checkPrivilege(context, parentId, AccessManager.OPER_VIEW);
                        Element content = dm.getMetadata(context, parentId, forEditing, withValidationErrors);
                        relatedRecords.addContent(new Element("parent").addContent(new Element("response").addContent(content)));
                    }
                    catch (Exception e) {
                        Log.debug(Geonet.SEARCH_ENGINE, "Parent metadata " + parentId + " record is not visible for user.");
                    }
                }
            }
        }
        if (type.equals("") || type.equals("service")) {
            relatedRecords.addContent(search(uuid, "services", context, from, to, fast));
        }
        // Get datasets related to service search
        if (type.equals("") || type.equals("dataset")) {
            boolean forEditing = false, withValidationErrors = false;
            Element md = gc.getDataManager().getMetadata(context, String.valueOf(id), forEditing, withValidationErrors);

            Iterator<Element> i = md.getDescendants(new ElementFilter ("operatesOn", Namespace.getNamespace("http://www.isotc211.org/2005/srv")));
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
            
            if (uuids.length() > 0) {
                relatedRecords.addContent(search(uuids.toString(), "datasets", context, from,
                        to, fast));
            }
        }
        if (type.equals("") || type.equals("related")) {
            Element relation = Get.getRelation(id, "full", context);
            relatedRecords.addContent(new Element("related")
                    .addContent(relation));
        }

        return relatedRecords;

    }

    private Element search(String uuid, String type, ServiceContext context,
            String from, String to, String fast) throws Exception {
        GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
        SearchManager searchMan = gc.getSearchmanager();

        // perform the search
        Log.info(Geonet.SEARCH_ENGINE,
                "Creating metadata for children searcher");
        MetaSearcher searcher = searchMan.newSearcher(SearchManager.LUCENE,
                Geonet.File.SEARCH_LUCENE);

				try {
        	// Creating parameters for search, fast only to retrieve uuid
        	Element parameters = new Element(Jeeves.Elem.REQUEST);
        	if ("children".equals(type))
            	parameters.addContent(new Element("parentUuid").setText(uuid));
        	else if ("services".equals(type))
            	parameters.addContent(new Element("operatesOn").setText(uuid));
        	else if ("datasets".equals(type))
            	parameters.addContent(new Element("uuid").setText(uuid));
        	parameters.addContent(new Element("fast").addContent(fast));
        	parameters.addContent(new Element("from").addContent(from));
        	parameters.addContent(new Element("to").addContent(to));
	
        	searcher.search(context, parameters, _config);

        	Log.info(Geonet.SEARCH_ENGINE, "Getting children search summary");

        	Element response = new Element(type);
        	Element relatedElement = searcher.present(context, parameters, _config);
        	response.addContent(relatedElement);
        	return response;
				} finally {
					Log.info(Geonet.SEARCH_ENGINE, "Closing searcher");
					searcher.close();
				}
    }
}
