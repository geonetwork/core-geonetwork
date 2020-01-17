//=============================================================================
//===  Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===  United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===  and United Nations Environment Programme (UNEP)
//===
//===  This program is free software; you can redistribute it and/or modify
//===  it under the terms of the GNU General Public License as published by
//===  the Free Software Foundation; either version 2 of the License, or (at
//===  your option) any later version.
//===
//===  This program is distributed in the hope that it will be useful, but
//===  WITHOUT ANY WARRANTY; without even the implied warranty of
//===  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===  General Public License for more details.
//===
//===  You should have received a copy of the GNU General Public License
//===  along with this program; if not, write to the Free Software
//===  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===  Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===  Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.wfsfeatures;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.List;

//=============================================================================

public class WfsFeaturesParams extends AbstractParams {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public String url;

    //---------------------------------------------------------------------------
    //---
    //--- Create : called when a new entry must be added. Reads values from the
    //---          provided entry, providing default values
    //---
    //---------------------------------------------------------------------------
    public String icon;

    //---------------------------------------------------------------------------
    //---
    //--- Update : called when an entry has changed and variables must be updated
    //---
    //---------------------------------------------------------------------------
    public String lang;

    //---------------------------------------------------------------------------
    //---
    //--- Other API methods
    //---
    //---------------------------------------------------------------------------
    public String query;

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    public String outputSchema;
    public String stylesheet;
    public boolean streamFeatures;
    public boolean createSubtemplates;
    public String templateId;
    public String recordsCategory;
    public WfsFeaturesParams(DataManager dm) {
        super(dm);
    }

    private String getQuery(Element params) {
        String query = "";
        Element q = Util.getChild(params, "query");
        if (q != null) {
          List<Element> qu = q.getChildren();
          if (qu.size() == 1) {
            query = Xml.getString(qu.get(0));
          } else {
            // query string will be an escaped XML string as a result of
            // jdom Element.setText and XSLT processing, so fix it up
            query = StringEscapeUtils.unescapeXml(q.getText());
          }
        }
        return query;
    }

    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", "");
        icon = Util.getParam(site, "icon", "wfs.gif");
        lang = Util.getParam(opt, "lang", "");
        query = getQuery(opt);
        outputSchema = Util.getParam(opt, "outputSchema", "");
        stylesheet = Util.getParam(opt, "stylesheet", "");
        streamFeatures = Util.getParam(opt, "streamFeatures", true);
        createSubtemplates = Util.getParam(opt, "createSubtemplates", true);
        templateId = Util.getParam(opt, "templateId", "");
        recordsCategory = Util.getParam(opt, "recordsCategory", "");
    }

    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", url);
        icon = Util.getParam(site, "icon", "wfs.gif");
        lang = Util.getParam(opt, "lang", lang);
        query = getQuery(opt);
        outputSchema = Util.getParam(opt, "outputSchema", "");
        stylesheet = Util.getParam(opt, "stylesheet", "");
        streamFeatures = Util.getParam(opt, "streamFeatures", streamFeatures);
        createSubtemplates = Util.getParam(opt, "createSubtemplates", createSubtemplates);
        templateId = Util.getParam(opt, "templateId", "");
        recordsCategory = Util.getParam(opt, "recordsCategory", recordsCategory);
    }

    public WfsFeaturesParams copy() {
        WfsFeaturesParams copy = new WfsFeaturesParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.lang = lang;
        copy.query = query;
        copy.outputSchema = outputSchema;
        copy.stylesheet = stylesheet;
        copy.createSubtemplates = createSubtemplates;
        copy.templateId = templateId;
        copy.recordsCategory = recordsCategory;
        return copy;
    }

    @Override
    public String getIcon() {
        return icon;
    }
}

//=============================================================================


