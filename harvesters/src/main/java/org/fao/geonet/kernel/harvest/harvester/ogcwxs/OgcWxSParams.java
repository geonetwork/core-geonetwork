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

package org.fao.geonet.kernel.harvest.harvester.ogcwxs;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

//=============================================================================

public class OgcWxSParams extends AbstractParams {
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
    public String ogctype;

    //---------------------------------------------------------------------------
    //---
    //--- Other API methods
    //---
    //---------------------------------------------------------------------------
    public String lang;

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    public String topic;
    public boolean createThumbnails;
    public boolean useLayer;
    public boolean useLayerMd;
    public String datasetCategory;
    public String serviceTemplateUuid;
    public String datasetTemplateUuid;
    public String outputSchema;

    @Override
    public String getIcon() {
        return icon;
    }

    public OgcWxSParams(DataManager dm) {
        super(dm);
    }

    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", "");
        icon = Util.getParam(site, "icon", "");
        ogctype = Util.getParam(site, "ogctype", "");
        lang = Util.getParam(opt, "lang", "");
        topic = Util.getParam(opt, "topic", "");
        createThumbnails = Util.getParam(opt, "createThumbnails", false);
        serviceTemplateUuid = Util.getParam(opt, "serviceTemplateUuid", "");
        datasetTemplateUuid = Util.getParam(opt, "datasetTemplateUuid", "");
        useLayer = Util.getParam(opt, "useLayer", false);
        useLayerMd = Util.getParam(opt, "useLayerMd", false);
        datasetCategory = Util.getParam(opt, "datasetCategory", "");
        outputSchema = Util.getParam(opt, "outputSchema", "");
    }

    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", url);
        icon = Util.getParam(site, "icon", icon);
        ogctype = Util.getParam(site, "ogctype", ogctype);

        lang = Util.getParam(opt, "lang", lang);
        topic = Util.getParam(opt, "topic", topic);
        createThumbnails = Util.getParam(opt, "createThumbnails", createThumbnails);
        serviceTemplateUuid = Util.getParam(opt, "serviceTemplateUuid", serviceTemplateUuid);
        datasetTemplateUuid = Util.getParam(opt, "datasetTemplateUuid", datasetTemplateUuid);
        useLayer = Util.getParam(opt, "useLayer", useLayer);
        useLayerMd = Util.getParam(opt, "useLayerMd", useLayerMd);
        datasetCategory = Util.getParam(opt, "datasetCategory", datasetCategory);
        outputSchema = Util.getParam(opt, "outputSchema", outputSchema);
    }

    public OgcWxSParams copy() {
        OgcWxSParams copy = new OgcWxSParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.ogctype = ogctype;
        copy.lang = lang;
        copy.topic = topic;
        copy.createThumbnails = createThumbnails;
        copy.serviceTemplateUuid = serviceTemplateUuid;
        copy.datasetTemplateUuid = datasetTemplateUuid;
        copy.useLayer = useLayer;
        copy.useLayerMd = useLayerMd;
        copy.datasetCategory = datasetCategory;
        copy.outputSchema = outputSchema;
        return copy;
    }
}

//=============================================================================


