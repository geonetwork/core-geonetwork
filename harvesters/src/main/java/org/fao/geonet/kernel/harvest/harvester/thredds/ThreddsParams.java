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

package org.fao.geonet.kernel.harvest.harvester.thredds;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadInputEx;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.harvest.harvester.AbstractParams;
import org.jdom.Element;

//=============================================================================

public class ThreddsParams extends AbstractParams {
    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    static public final String DEFAULT = "default";

    //---------------------------------------------------------------------------
    //---
    //--- Update : called when an entry has changed and variables must be updated
    //---
    //---------------------------------------------------------------------------
    public String url;

    //---------------------------------------------------------------------------
    //---
    //--- Other API methods
    //---
    //---------------------------------------------------------------------------
    public String icon;

    //---------------------------------------------------------------------------
    //---
    //--- Variables
    //---
    //---------------------------------------------------------------------------
    public String lang;
    public String topic;
    public boolean createServiceMd;
    public String outputSchema;
    public String datasetTitle;
    public String datasetAbstract;
    public String datasetCategory;
    public String serviceCategory;

    @Override
    public String getIcon() {
        return icon;
    }

    public ThreddsParams(DataManager dm) {
        super(dm);
    }

    public void create(Element node) throws BadInputEx {
        super.create(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", "");
        icon = Util.getParam(site, "icon", "");
        lang = Util.getParam(opt, "lang", "");
        topic = Util.getParam(opt, "topic", "");
        createServiceMd = Util.getParam(opt, "createServiceMd", false);
        outputSchema = Util.getParam(opt, "outputSchema", "iso19139");
        datasetTitle = Util.getParam(opt, "datasetTitle", "");
        datasetAbstract = Util.getParam(opt, "datasetAbstract", "");
        serviceCategory = Util.getParam(opt, "serviceCategory", "");
        datasetCategory = Util.getParam(opt, "datasetCategory", "");
    }

    //---------------------------------------------------------------------------
    //---
    //--- Constants
    //---
    //---------------------------------------------------------------------------

    public void update(Element node) throws BadInputEx {
        super.update(node);

        Element site = node.getChild("site");
        Element opt = node.getChild("options");

        url = Util.getParam(site, "url", url);
        icon = Util.getParam(site, "icon", icon);

        lang = Util.getParam(opt, "lang", lang);
        topic = Util.getParam(opt, "topic", topic);
        createServiceMd = Util.getParam(opt, "createServiceMd", createServiceMd);
        outputSchema = Util.getParam(opt, "outputSchema", outputSchema);
        datasetTitle = Util.getParam(opt, "datasetTitle", datasetTitle);
        datasetAbstract = Util.getParam(opt, "datasetAbstract", datasetTitle);
        serviceCategory = Util.getParam(opt, "serviceCategory", serviceCategory);
        datasetCategory = Util.getParam(opt, "datasetCategory", datasetCategory);
    }

    public ThreddsParams copy() {
        ThreddsParams copy = new ThreddsParams(dm);
        copyTo(copy);

        copy.url = url;
        copy.icon = icon;
        copy.lang = lang;
        copy.topic = topic;
        copy.createServiceMd = createServiceMd;
        copy.outputSchema = outputSchema;
        copy.datasetTitle = datasetTitle;
        copy.datasetAbstract = datasetAbstract;
        copy.serviceCategory = serviceCategory;
        copy.datasetCategory = datasetCategory;
        return copy;
    }
}

//=============================================================================


