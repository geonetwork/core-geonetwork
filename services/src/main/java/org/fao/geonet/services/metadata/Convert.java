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

package org.fao.geonet.services.metadata;

import java.nio.file.Path;

import org.fao.geonet.GeonetContext;
import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.AbstractMetadata;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.exceptions.MetadataNotFoundEx;
import org.fao.geonet.kernel.SchemaManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.kernel.oaipmh.Lib;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.services.Utils;
import org.jdom.Element;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

//=============================================================================

/**
 * Converts a particular metadata using the supplied XSLT name.
 */

public class Convert implements Service {
    //--------------------------------------------------------------------------
    //---
    //--- Init
    //---
    //--------------------------------------------------------------------------

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    //--------------------------------------------------------------------------
    //---
    //--- Service
    //---
    //--------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context) throws Exception {
        GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
        SchemaManager sm = gc.getBean(SchemaManager.class);

        String id = Utils.getIdentifierFromParameters(params, context);
        if (id == null) throw new MetadataNotFoundEx("Metadata not found.");

        Element elMd = new Show().exec(params, context);
        if (elMd == null) throw new MetadataNotFoundEx(id);

        //--- get XSLT converter name from params
        String styleSheet = Util.getParam(params, Params.STYLESHEET);

        //--- get metadata info and create an env that works with oai translators
        final AbstractMetadata metadata = context.getBean(IMetadataUtils.class).findOne(id);
        Path schemaDir = sm.getSchemaDir(metadata.getDataInfo().getSchemaId());
        final String baseUrl = context.getBaseUrl();
        final ISODate changeDate = metadata.getDataInfo().getChangeDate();
        final String uuid = metadata.getUuid();
        final String siteURL = context.getBean(SettingManager.class).getSiteURL(context);
        final String siteName = gc.getBean(SettingManager.class).getSiteName();
        Element env = Lib.prepareTransformEnv(uuid, changeDate.getDateAndTime(), baseUrl, siteURL, siteName);

        //--- transform the metadata with the created env and specified stylesheet
        return Lib.transform(schemaDir, env, elMd, styleSheet);
    }

}
//=============================================================================

