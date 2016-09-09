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

package org.fao.geonet.guiservices.templates;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Edit;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.kernel.search.SearchManager;
import org.fao.geonet.kernel.search.SearcherType;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//=============================================================================

/**
 * A simple service that returns all metadata templates that can be added. User could also add
 * search parameter in order to limit the list of templates proposed to the user. By default the
 * search is restricted to template=y, extended=off and remote=off.
 *
 * see search parameters
 */
@Deprecated
public class Get implements Service {
    private String arParams[] =
        {
            "extended", "off",
            "remote", "off",
            "attrset", "geo",
            "template", "y"
        };

    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- API
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {

        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        SchemaManager schemaMan = gc.getBean(SchemaManager.class);

        Element result = search(params, context).setName(Jeeves.Elem.RESPONSE);

        @SuppressWarnings("unchecked")
        List<Element> list = result.getChildren();

        Element response = new Element("dummy");

        // heikki: geonovum: first build the list of response records
        List<Element> responseRecords = new ArrayList<Element>();

        for (Element elem : list) {
            Element info = elem.getChild(Edit.RootChild.INFO, Edit.NAMESPACE);

            if (!elem.getName().equals("metadata")) {
                continue;
            }

            String template = elem.getChildText("isTemplate");
            String displayOrder = elem.getChildText("displayOrder");
            String schema = info.getChildText("schema");
            String id = info.getChildText(Edit.Info.Elem.ID);

            if (template.equals("y") && schemaMan.existsSchema(schema)) {
                // heikki, GeoNovum: added displayOrder
                responseRecords.add(buildRecord(id, elem.getChildText("title"), schema, displayOrder));
            }
        }
        // heikki, Geonovum: then process them to ensure displayOrder is not empty and is unique
        List<Integer> displayOrderList = new ArrayList<Integer>();
        for (Element record : responseRecords) {
            String displayOrder = record.getChildText("displayorder");
            if (displayOrder == null ||
                displayOrder.equals("") ||
                displayOrder.equals("null")) {
                displayOrder = "-1";
            }
            Integer displayOrderI = Integer.parseInt(displayOrder);
            // not yet in list
            if (!displayOrderList.contains(displayOrderI)) {
                // add to list
                displayOrderList.add(displayOrderI);
            } else {
                // already in list
                // while in list
                int tmp = displayOrderI;
                while (displayOrderList.contains(tmp)) {
                    tmp++;
                }
                displayOrderI = tmp;
                // add to list
                displayOrderList.add(displayOrderI);
            }
            record.getChild("displayorder").setText(displayOrderI.toString());
            response.addContent(record);
        }
        return response;
    }

    //--------------------------------------------------------------------------
    //---
    //--- Private methods
    //---
    //--------------------------------------------------------------------------

    private Element search(Element par, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);

        context.info("Creating searcher");

        Element params = buildParams(par);
        ServiceConfig config = new ServiceConfig();

        SearchManager searchMan = gc.getBean(SearchManager.class);
        try (MetaSearcher searcher = searchMan.newSearcher(SearcherType.LUCENE, Geonet.File.SEARCH_LUCENE)) {

            searcher.search(context, params, config);

            params.addContent(new Element("from").setText("1"));
            params.addContent(new Element("to").setText(searcher.getSize() + ""));
            params.addContent(new Element("fast").setText("index"));

            return searcher.present(context, params, config);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Adding default params (i.e. force template search) to all other input parameters
     */
    private Element buildParams(Element par) {
        Element params = new Element(Jeeves.Elem.REQUEST);
        @SuppressWarnings("unchecked")
        List<Element> in = par.getChildren();
        for (Element el : in)
            params.addContent(new Element(el.getName()).setText(el.getText()));

        for (int i = 0; i < arParams.length / 2; i++)
            params.addContent(new Element(arParams[i * 2]).setText(arParams[i * 2 + 1]));

        return params;
    }

    //--------------------------------------------------------------------------
    // heikki, GeoNovum: added displayOrder
    private Element buildRecord(String id, String name, String schema, String displayOrder) {
        Element el = new Element("record");

        el.addContent(new Element("id").setText(id));
        el.addContent(new Element("name").setText(name));
        el.addContent(new Element("schema").setText(schema));
        el.addContent(new Element("displayorder").setText(displayOrder));

        return el;
    }
}

//=============================================================================

